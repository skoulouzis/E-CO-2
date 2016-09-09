/*
 * Copyright 2016 S. Koulouzis.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.edisonproject.training.term.extraction;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import eu.edisonproject.training.context.corpus.Apriori;
import eu.edisonproject.utility.file.ConfigHelper;
import eu.edisonproject.utility.text.processing.StopWord;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
public class AprioriExtraction implements TermExtractor {

    private static String stopWordsPath;
    private static StopWord cleanStopWord;
//    private static StanfordLemmatizer cleanLemmatisation;
    private static String taggerPath;
    private static final String[] rejectPOS = new String[]{"JJ", "JJR", "JJS", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "RB", "RBR", "RBS"};
    private Double minSup;

    @Override
    public void configure(Properties prop) {
        stopWordsPath = System.getProperty("stop.words.file");

        if (stopWordsPath == null) {
            stopWordsPath = prop.getProperty("stop.words.file", ".." + File.separator + "etc" + File.separator + "stopwords.csv");
        }
        CharArraySet stopwordsCharArray = new CharArraySet(ConfigHelper.loadStopWords(stopWordsPath), true);
        cleanStopWord = new StopWord(stopwordsCharArray);

//        cleanLemmatisation = new StanfordLemmatizer();
        taggerPath = System.getProperty("tagger.file");

        if (taggerPath == null) {
            taggerPath = prop.getProperty("tagger.file", ".." + File.separator + "etc" + File.separator + "model" + File.separator + "stanford" + File.separator + "english-left3words-distsim.tagger");
        }
        if (System.getProperty("apriory.min.setup") != null) {
            minSup = Double.valueOf(System.getProperty("apriory.min.setup"));
        }

        if (minSup == null) {
            minSup = Double.valueOf(prop.getProperty("apriory.min.setup", "0.01"));
        }

    }

    public String clean(String description) {

        cleanStopWord.setDescription(description);
        String descriptionWithoutStopWord = cleanStopWord.execute();
//        cleanLemmatisation.setDescription(descriptionWithoutStopWord);
//        String descriptionLemma = cleanLemmatisation.execute();

        return descriptionWithoutStopWord;

    }

    @Override
    public Map<String, Double> termXtraction(String inDir) throws IOException {
        try {
            int count = 0;
            HashMap<String, Double> keywordsDictionaray = new HashMap();
            File dir = new File(inDir);

            Set<String> terms = new HashSet<>();

            if (dir.isDirectory()) {
                for (File f : dir.listFiles()) {
                    if (FilenameUtils.getExtension(f.getName()).endsWith("txt")) {
                        count++;
                        Logger.getLogger(AprioriExtraction.class.getName()).log(Level.INFO, "{0}: {1} of {2}", new Object[]{f.getName(), count, dir.list().length});
                        terms.addAll(extractFromFile(f));
                    }
                }
            } else if (dir.isFile()) {
                if (FilenameUtils.getExtension(dir.getName()).endsWith("txt")) {
                    terms.addAll(extractFromFile(dir));
                }

            }
            MaxentTagger tagger = new MaxentTagger(taggerPath);
            for (String t : terms) {
                Double tf = 0.0;
                String term = t.toLowerCase().trim().replaceAll(" ", "_").split("/")[0];
                while (term.endsWith("_")) {
                    term = term.substring(0, term.lastIndexOf("_"));
                }
                while (term.startsWith("_")) {
                    term = term.substring(term.indexOf("_") + 1, term.length());
                }
                String tagged = null;
//                if (!term.contains("_")) {
                tagged = tagger.tagString(term);
//                }
                boolean add = true;
                if (tagged != null) {
                    if (!tagged.contains("NN") || tagged.contains("RB")) {
                        add = false;
                    }
//                    }
                } else {
                    add = true;
                }
                if (add) {
                    if (keywordsDictionaray.containsKey(term)) {
                        tf = keywordsDictionaray.get(term);
                        tf++;
                    } else {
                        tf = 1.0;
                    }
                    keywordsDictionaray.put(term, tf);
                }

            }
            return keywordsDictionaray;

        } catch (Exception ex) {
            Logger.getLogger(AprioriExtraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Map<String, Double> rank(String inDir) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Collection<? extends String> extractFromFile(File f) throws IOException, Exception {
        List<String> terms = null;
        if (FilenameUtils.getExtension(f.getName()).endsWith("txt")) {
            StringBuilder fileContents = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                for (String text; (text = br.readLine()) != null;) {
                    String cleanedDescription = clean(text);
                    fileContents.append(cleanedDescription).append("\n");
                }
            }

//            String contents = fileContents.toString().replaceAll("_", " ");
//            contents = contents.replaceAll("\\s{2,}", " ");
            String[] args = new String[2];
            args[0] = fileContents.toString();
            args[1] = "0.005";
            Apriori apriori = new Apriori(args);
            terms = apriori.go();
        }

        return terms;
    }

}
