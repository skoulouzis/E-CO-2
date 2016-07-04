/*
 * Copyright 2016 Michele Sparamonti & Spiros Koulouzis.
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
package eu.edisonproject.training.tfidf.mapreduce;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michele Sparamonti (michele.sparamonti@eng.it)
 */
public class TFIDFDriver {

    public void executeTFIDF() {

        int numberOfDocuments = 0;
        String INPUT_PATH = ".." + File.separator + "etc" + File.separator + "input";
        File file = new File(INPUT_PATH);
	File[] filesInDir = file.listFiles();
	numberOfDocuments = filesInDir.length;	

        try {
            WordFrequencyInDocDriver wfindd = new WordFrequencyInDocDriver();
            wfindd.runWordFrequencyInDocDriver();
        } catch (Exception ex) {
            Logger.getLogger(TFIDFDriver.class.getName()).log(Level.SEVERE, "Word Frequency In Doc Driver fail", ex);
        }
        try {
            WordCountsForDocsDriver wfindd = new WordCountsForDocsDriver();
            wfindd.runWordCountsForDocsDriver();
        } catch (Exception ex) {
            Logger.getLogger(TFIDFDriver.class.getName()).log(Level.SEVERE, "Word Frequency In Doc Driver fail", ex);
        }
        try {
            WordsInCorpusTFIDFDriver wfindd = new WordsInCorpusTFIDFDriver();
            wfindd.runWordsInCorpusTFIDFDriver(String.valueOf(numberOfDocuments));
        } catch (Exception ex) {
            Logger.getLogger(TFIDFDriver.class.getName()).log(Level.SEVERE, "Word Frequency In Doc Driver fail", ex);
        }
    }

}
