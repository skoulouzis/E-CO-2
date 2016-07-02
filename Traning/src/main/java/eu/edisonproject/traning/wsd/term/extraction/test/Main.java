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
package eu.edisonproject.traning.wsd.term.extraction.test;

import eu.edisonproject.traning.wsd.term.extraction.JtopiaExtractor;
import eu.edisonproject.traning.wsd.term.extraction.LuceneExtractor;
import eu.edisonproject.traning.wsd.term.extraction.TermExtractor;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author S. Koulouzis
 */
public class Main {

    public static void main(String args[]) {
        testExtractors();
    }

    private static void testExtractors() {
        try {

            String stopwordsPath = ".." + File.separator + "etc" + File.separator + "stopwords.csv";
            Properties prop = new Properties();
            prop.setProperty("stop.words.file", stopwordsPath);
            prop.setProperty("max.ngrams", "4");
            String itemsFilePath = ".." + File.separator + "etc" + File.separator + "itemset.csv";
            prop.setProperty("itemset.file", itemsFilePath);

            String modelPath = ".." + File.separator + "etc" + File.separator + "model";
            prop.setProperty("model.path", modelPath);

            TermExtractor le = new LuceneExtractor();
            le.configure(prop);
            String keyWordsFile = ".." + File.separator + "documentation" + File.separator + "keywordFiles" + File.separator + "BigDataFrameworks.txt";
            Map<String, Double> terms = le.termXtraction(keyWordsFile);
            System.err.println(terms);

            TermExtractor je = new JtopiaExtractor();
            je.configure(prop);
            terms = je.termXtraction(keyWordsFile);
            System.err.println(terms);

        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
