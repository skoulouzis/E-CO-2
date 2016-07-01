package eu.edisonproject.traning.wsd.term.extraction;

import eu.edisonproject.utility.text.processing.Cleaner;
import eu.edisonproject.utility.text.processing.StanfordLemmatizer;
import eu.edisonproject.utility.text.processing.StopWord;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
//import net.didion.jwnl.JWNLException;
//import net.didion.jwnl.data.POS;
//import nl.uva.sne.commons.SemanticUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;
//import nl.uva.sne.commons.ValueComparator;
//import org.json.simple.parser.ParseException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author S. Koulouzis
 */
public class LuceneExtractor implements TermExtractor {

    private int maxNgrams;
    private StopWord tokenizer;
    private StanfordLemmatizer lematizer;
    private String itemsFilePath;

    @Override
    public void configure(Properties prop) {
        maxNgrams = Integer.valueOf(prop.getProperty("max.ngrams", "4"));

        String stopWordsPath = System.getProperty("stop.words.file");

        if (stopWordsPath == null) {
            stopWordsPath = prop.getProperty("stop.words.file", "etc" + File.separator + "stopwords");
        }

        itemsFilePath = System.getProperty("items.file");
        if (itemsFilePath == null) {
            itemsFilePath = prop.getProperty("items.file", "etc" + File.separator + "items.csv");
        }
    }

    @Override
    public Map<String, Double> termXtraction(String inDir) throws IOException, FileNotFoundException, MalformedURLException {
        File dir = new File(inDir);
        Map<String, Double> termDictionaray = new HashMap();
        int count = 0;
        try {
            if (dir.isDirectory()) {

                Collection c = null;
                CharArraySet stopwordsCharArray = new CharArraySet(c, true);
                tokenizer = new StopWord(stopwordsCharArray);
                lematizer = new StanfordLemmatizer();

                for (File f : dir.listFiles()) {
                    count++;
                    Logger.getLogger(LuceneExtractor.class.getName()).log(Level.INFO, "{0}: {1} of {2}", new Object[]{f.getName(), count, dir.list().length});
                    if (FilenameUtils.getExtension(f.getName()).endsWith("txt")) {
                        termDictionaray.putAll(extractFromFile(f));
                    }
                }
            } else if (dir.isFile()) {
                termDictionaray.putAll(extractFromFile(dir));
            }
        } catch (Exception ex) {
            Logger.getLogger(LuceneExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return termDictionaray;
    }

    private Map<String, Double> extractFromFile(File f) throws IOException, MalformedURLException {
        Map<String, Double> termDictionaray = new HashMap();
        StringBuilder fileContents = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            for (String text; (text = br.readLine()) != null;) {
                fileContents.append(text.toLowerCase()).append(" ");
            }
        }
        fileContents.deleteCharAt(fileContents.length() - 1);
        fileContents.setLength(fileContents.length());
        tokenizer.setDescription(fileContents.toString());
        String cleanText = tokenizer.execute();

        lematizer.setDescription(cleanText);
        String lematizedText = lematizer.execute();

        //Read asosiation rules file and check if we have the same terms 
        try (BufferedReader br = new BufferedReader(new FileReader(new File(itemsFilePath)))) {
            for (String text; (text = br.readLine()) != null;) {

                for (String t : tokens) {

                    Double tf;
                    if (termDictionaray.containsKey(t)) {
                        tf = termDictionaray.get(t);
                        tf++;
                    } else {
                        tf = 1.0;
                    }
                    termDictionaray.put(t, tf);
                }

                for (String t : ngrams) {
                    Double tf;
                    if (termDictionaray.containsKey(t)) {
                        tf = termDictionaray.get(t);
                        tf++;
                    } else {
                        tf = 1.0;
                    }
                    termDictionaray.put(t, tf);
                }
            }
        }

        return termDictionaray;
    }
}
