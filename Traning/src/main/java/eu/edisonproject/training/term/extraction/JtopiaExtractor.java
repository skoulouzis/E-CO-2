/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.edisonproject.training.term.extraction;

import com.sree.textbytes.jtopia.Configuration;
import com.sree.textbytes.jtopia.TermDocument;
import com.sree.textbytes.jtopia.TermsExtractor;
import eu.edisonproject.utility.file.ConfigHelper;
import eu.edisonproject.utility.text.processing.StopWord;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 *
 * @author S. Koulouzis
 */
public class JtopiaExtractor implements TermExtractor {

    private String stopWordsPath;
    private StopWord tokenizer;
//    private StanfordLemmatizer lematizer;

    @Override
    public void configure(Properties prop) {

        String taggerType = System.getProperty("tagger.type");
        if (taggerType == null) {
            taggerType = prop.getProperty("tagger.type", "stanford");
        }

        stopWordsPath = System.getProperty("stop.words.file");

        if (stopWordsPath == null) {
            stopWordsPath = prop.getProperty("stop.words.file", ".." + File.separator + "etc" + File.separator + "stopwords.csv");
        }

        String modelPath = System.getProperty("model.path");
        if (modelPath == null) {
            modelPath = prop.getProperty("model.path", ".." + File.separator + "etc" + File.separator + "model");
        }

        if (modelPath.endsWith("/")) {
            modelPath = modelPath.substring(0, modelPath.length() - 1);
        }
        switch (taggerType) {
            case "stanford":
                Configuration.setModelFileLocation(modelPath + File.separator
                        + "stanford" + File.separator + "english-left3words-distsim.tagger");
                Configuration.setTaggerType("stanford");
                break;
            case "openNLP":
                Configuration.setModelFileLocation(modelPath + File.separator
                        + "openNLP" + File.separator + "en-pos-maxent.bin");
                Configuration.setTaggerType("openNLP");
                break;
            case "default":
                Configuration.setModelFileLocation(modelPath + File.separator
                        + "default" + File.separator + "english-lexicon.txt");
                Configuration.setTaggerType("default");
                break;
            default:
                Configuration.setModelFileLocation(modelPath + File.separator
                        + "stanford" + File.separator + "english-left3words-distsim.tagger");
                Configuration.setTaggerType("stanford");
                break;
        }
        Integer singleStrength = Integer.valueOf(prop.getProperty("single.strength", "3"));
        Configuration.setSingleStrength(singleStrength);

        Integer noLimitStrength = Integer.valueOf(prop.getProperty("no.limit.strength", "2"));
        Configuration.setNoLimitStrength(noLimitStrength);
    }

    @Override
    public Map<String, Double> termXtraction(String inDir) throws IOException {
        File dir = new File(inDir);
        TermsExtractor termExtractor = new TermsExtractor();
        TermDocument topiaDoc = new TermDocument();
        HashMap<String, Double> keywordsDictionaray = new HashMap();
        int count = 0;

        CharArraySet stopwordsCharArray = new CharArraySet(ConfigHelper.loadStopWords(stopWordsPath), true);
        tokenizer = new StopWord(stopwordsCharArray);
//        lematizer = new StanfordLemmatizer();

        Set<String> terms = new HashSet<>();

        if (dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                if (FilenameUtils.getExtension(f.getName()).endsWith("txt")) {
                    count++;
                    Logger.getLogger(JtopiaExtractor.class.getName()).log(Level.INFO, "{0}: {1} of {2}", new Object[]{f.getName(), count, dir.list().length});
                    terms.addAll(extractFromFile(f, termExtractor, topiaDoc));
                }
            }
        } else if (dir.isFile()) {
            if (FilenameUtils.getExtension(dir.getName()).endsWith("txt")) {
                terms.addAll(extractFromFile(dir, termExtractor, topiaDoc));
            }

        }
        for (String t : terms) {
            Double tf;
            String term = t.toLowerCase().trim().replaceAll(" ", "_");
            while (term.endsWith("_")) {
                term = term.substring(0, term.lastIndexOf("_"));
            }
            while (term.startsWith("_")) {
                term = term.substring(term.indexOf("_") + 1, term.length());
            }
            if (keywordsDictionaray.containsKey(term)) {
                tf = keywordsDictionaray.get(term);
                tf++;
            } else {
                tf = 1.0;
            }
            keywordsDictionaray.put(term, tf);
        }
        return keywordsDictionaray;
    }

    private Set<String> extractFromFile(File f, TermsExtractor termExtractor, TermDocument topiaDoc) throws IOException {
        Set<String> terms = null;
        if (FilenameUtils.getExtension(f.getName()).endsWith("txt")) {

            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                StringBuilder fileContents = new StringBuilder();
                for (String text; (text = br.readLine()) != null;) {
                    fileContents.append(text.toLowerCase()).append(" ");
                }
                fileContents.deleteCharAt(fileContents.length() - 1);
                fileContents.setLength(fileContents.length());

                String contents = fileContents.toString().replaceAll("_", " ");
                contents = contents.replaceAll("\\s{2,}", " ");

                tokenizer.setDescription(contents);
                String cleanText = tokenizer.execute();
//                lematizer.setDescription(cleanText);
                String lematizedText = cleanText;//lematizer.execute();

                topiaDoc = termExtractor.extractTerms(lematizedText);
                terms = topiaDoc.getFinalFilteredTerms().keySet();
            }
        }
        return terms;
    }

    @Override
    public Map<String, Double> rank(String inDir) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
