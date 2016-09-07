/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.edisonproject.utility.commons;

import eu.edisonproject.utility.file.ConfigHelper;
import eu.edisonproject.utility.file.ReaderFile;
import eu.edisonproject.utility.text.processing.StopWord;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.util.CharArraySet;
import org.unix4j.Unix4j;
import org.unix4j.unix.grep.GrepOptionSets;

/**
 *
 * @author S. Koulouzis
 */
public class IDFSort implements SortTerms {

    private final StopWord cleanStopWord;

    public IDFSort(String stopWordsPath) {
        CharArraySet stopWordArraySet = new CharArraySet(ConfigHelper.loadStopWords(stopWordsPath), true);
        cleanStopWord = new StopWord(stopWordArraySet);
    }

    @Override
    public Map<String, Double> sort(Map<String, Double> termDictionaray, String dirPath) throws IOException, InterruptedException {
        Map<String, Double> newTermDictionaray = new HashMap<>();
        File dir = new File(dirPath);
        File[] docs = dir.listFiles();
        GrepOptionSets go = new GrepOptionSets();
        for (String term : termDictionaray.keySet()) {
            int numOfDocsWithTerm = 0;
            for (File f : docs) {
                int count = 0;
                if (FilenameUtils.getExtension(f.getName()).endsWith("txt")) {
                    count++;
                    Logger.getLogger(IDFSort.class.getName()).log(Level.FINE, "{0}: {1} of {2}", new Object[]{f.getName(), count, docs.length});
                    ReaderFile rf = new ReaderFile(f.getAbsolutePath());
                    String contents = rf.readFileWithN();
                    cleanStopWord.setDescription(contents);
                    contents = cleanStopWord.execute();

                    cleanStopWord.setDescription(term.replaceAll("_", " "));
                    String cTerm = cleanStopWord.execute();

//                    int lastIndex = 0;
//                    int wcount = 0;
//
//                    while (lastIndex != -1) {
//
//                        lastIndex = contents.indexOf(cTerm, lastIndex);
//
//                        if (lastIndex != -1) {
//                            wcount++;
//                            lastIndex += cTerm.length();
//                        }
//                    }
//                    System.out.println(f.getName() + "," + cTerm + "," + wcount);
//
//                    wcount = StringUtils.countMatches(contents, cTerm);
//                    System.out.println(f.getName() + "," + cTerm + "," + wcount);
                    if (contents.contains(cTerm)) {
                        numOfDocsWithTerm++;
//                        System.out.println(f.getName() + "," + cTerm + "," + numOfDocsWithTerm);
                    }
//                    try (InputStream fis = new FileInputStream(f)) {
//                        String result = Unix4j.from(fis).grep(go.i.count, cTerm).toStringResult();
//                        Integer lineCount = Integer.valueOf(result);
//                        if (lineCount > 0) {
//                            numOfDocsWithTerm++;
//                        }
//                    }
                }

            }
            double idf = 0;
            if (numOfDocsWithTerm > 0) {
                idf = Math.log((double) docs.length / (double) numOfDocsWithTerm);
            }
            newTermDictionaray.put(term, idf);
        }
        return newTermDictionaray;
    }
}
