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

import eu.edisonproject.training.tfidf.avro.TfidfDocument;
import eu.edisonproject.utility.file.FileIO;
import eu.edisonproject.utility.file.WriterFile;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.io.DatumReader;
import org.apache.avro.specific.SpecificDatumReader;

/**
 *
 * @author Michele Sparamonti (michele.sparamonti@eng.it)
 */
public class TFIDFDriver {

    //where to read the frequent itemset
    private String INPUT_ITEMSET = ".." + File.separator + "etc" + File.separator + "itemset.csv";
    //where to read the data for MapReduce#1
    private String INPUT_PATH1 = ".." + File.separator + "etc" + File.separator + "input";
    //where to put the data in hdfs when MapReduce#1 will finish
    private String OUTPUT_PATH1 = ".." + File.separator + "etc" + File.separator + "1-word-freq";

    // where to read the data for the MapReduce#2
    private String INPUT_PATH2 = ".." + File.separator + "etc" + File.separator + "1-word-freq";
    // where to put the data in hdfs when the MapReduce#2 will finish
    private String OUTPUT_PATH2 = ".." + File.separator + "etc" + File.separator + "2-word-counts";

    // where to read the data for the MapReduce#3
    private String INPUT_PATH3 = ".." + File.separator + "etc" + File.separator + "2-word-counts";
    // where to put the data in hdfs when the MapReduce#3 will finish
    private String OUTPUT_PATH3 = ".." + File.separator + "etc" + File.separator + "3-tf-idf";

    // where to read the data for the MapReduce#4.
    private String INPUT_PATH4 = ".." + File.separator + "etc" + File.separator + "3-tf-idf";
    // where to put the data in hdfs when the MapReduce# will finish
    private String OUTPUT_PATH4 = ".." + File.separator + "etc" + File.separator + "4-tf-idf-document";
    
    private String TFIDFCSV_PATH = ".." + File.separator + "etc" + File.separator + "5-csv";

    public void executeTFIDF() {

        int numberOfDocuments = 0;
        File file = new File(INPUT_PATH1);
        File[] filesInDir = file.listFiles();
        numberOfDocuments = filesInDir.length;

        try {
            WordFrequencyInDocDriver wf = new WordFrequencyInDocDriver();
            String[] args = {INPUT_PATH1, OUTPUT_PATH1, INPUT_ITEMSET};
            wf.runWordFrequencyInDocDriver(args);
        } catch (Exception ex) {
            Logger.getLogger(TFIDFDriver.class.getName()).log(Level.SEVERE, "Word Frequency In Doc Driver fail", ex);
        }
        try {
            WordCountsForDocsDriver wc = new WordCountsForDocsDriver();
            String[] args = {INPUT_PATH2, OUTPUT_PATH2};
            wc.runWordCountsForDocsDriver(args);
        } catch (Exception ex) {
            Logger.getLogger(TFIDFDriver.class.getName()).log(Level.SEVERE, "Word Counts For Docs Driver fail", ex);
        }
        try {
            WordsInCorpusTFIDFDriver wtfidf = new WordsInCorpusTFIDFDriver();
            String[] args = {INPUT_PATH3, OUTPUT_PATH3, String.valueOf(numberOfDocuments)};
            wtfidf.runWordsInCorpusTFIDFDriver(args);
        } catch (Exception ex) {
            Logger.getLogger(TFIDFDriver.class.getName()).log(Level.SEVERE, "Words in Corpus TFIDF Driver fail", ex);
        }
        try {
            WordsGroupByTitleDriver wtfidf = new WordsGroupByTitleDriver();
            String[] args = {INPUT_PATH4, OUTPUT_PATH4};
            wtfidf.runWordsGroupByTitleDriver(args);
        } catch (Exception ex) {
            Logger.getLogger(TFIDFDriver.class.getName()).log(Level.SEVERE, "Word Group By Title Driver fail", ex);
        }

        //Now from the .avro produce in hdfs we want a .csv
        readAvroAndPrintCSV();
    }

    public void readAvroAndPrintCSV() {
        List<String> words = new LinkedList<>();
        List<List<String>> line = new LinkedList<>();
        File file = new File(OUTPUT_PATH4);
        File[] filesInDir = file.listFiles();
        for (File f : filesInDir) {
            if (f.getName().contains(".avro")) {
                //deserializing		
                DatumReader<TfidfDocument> tfidfDatumReader = new SpecificDatumReader<TfidfDocument>(TfidfDocument.class);
                DataFileReader<TfidfDocument> dataFileReader;
                try {
                    dataFileReader = new DataFileReader<TfidfDocument>(f, tfidfDatumReader);
                    TfidfDocument tfidfDoc = null;
                    while (dataFileReader.hasNext()) {
                        //Reuse user object by passing it to next(). This saves us from
                        // allocating and garbage collecting many objects for files with
                        // many items.
                        List<String> values = new LinkedList<>();
                        for (int i = 0; i < tfidfDoc.getWords().size(); i++) {
                            String word = tfidfDoc.getWords().get(i).toString();
                            String value = tfidfDoc.getValues().get(i).toString();
                            if (words.contains(word)) {
                                values.add(words.indexOf(word), value);
                            } else {
                                words.add(word);
                                values.add(words.indexOf(word), value);
                            }
                        }
                        line.add(values);

                        tfidfDoc = dataFileReader.next(tfidfDoc);
                        System.out.println(tfidfDoc);

                    }
                } catch (IOException ex) {
                    Logger.getLogger(TFIDFDriver.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
        
        WriterFile fileWriter = new WriterFile(TFIDFCSV_PATH);
        String text = "";
        for(String w: words)
            text+=w+",";
        
        text+="\n";
        for(List<String> val: line){
            for(String value:val){
                text+=value+",";
            }
            text+="\n";
        }
        fileWriter.writeFile(text);
            

    }

}
