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
import eu.edisonproject.training.utility.term.avro.Term;
import eu.edisonproject.utility.file.WriterFile;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.io.DatumReader;
import org.apache.avro.specific.SpecificDatumReader;
//import org.apache.avro.hadoop.io.AvroSerialization;

/**
 *
 * @author Michele Sparamonti (michele.sparamonti@eng.it)
 */
public class TFIDFDriver implements ITFIDFDriver {

    //where to read the frequent itemset
    private String INPUT_ITEMSET = ".." + File.separator + "etc" + File.separator + "itemset.csv";
    //where to read the data for MapReduce#1
    private String INPUT_PATH1;
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

    // where to put the csv with the tfidf
    private String TFIDFCSV_PATH = ".." + File.separator + "etc" + File.separator + "5-csv";
    // where to put the csv with the context vector
    private String CONTEXT_PATH = ".." + File.separator + " etc" + File.separator + "6-context-vector";

    // the name of the context (categories) that it is under analysis
    private String contextName;

    // the list of all words
    private List<String> allWords;
    // the list of all value for each transaction
    private List<List<String>> transactionValues;

    private HashMap<String, Double> wordTfidf;

    private double threshold;

    public TFIDFDriver(String contextName) {
        this.contextName = contextName + ".csv";
        this.allWords = new LinkedList<String>();
        this.transactionValues = new LinkedList<List<String>>();
        wordTfidf = new HashMap<>();
    }

    @Override
    public void executeTFIDF(String inputPath) {
        INPUT_PATH1 = inputPath;
        int numberOfDocuments = 0;
        File file = new File(INPUT_PATH1);
        File[] filesInDir;
        if (file.isDirectory()) {
            filesInDir = file.listFiles();
            for (File fileSplit : filesInDir) {
                DatumReader<Term> termDatumReader = new SpecificDatumReader<>(Term.class);
                DataFileReader<Term> dataFileReader;
                try {
                    dataFileReader = new DataFileReader<Term>(fileSplit, termDatumReader);

                    while (dataFileReader.hasNext()) {
                        //Count the number of rows inside the .avro
                        dataFileReader.next();
                        numberOfDocuments++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

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

        } else {
            Logger.getLogger(TFIDFDriver.class.getName()).log(Level.SEVERE, "You must specify the input folder not a specific document", file);
        }
        
    }
            
    

    @Override
    public void driveProcessResizeVector() {
        // Read the output from avro file
        readAvro();

        // Now from the .avro produce in hdfs we want a .csv
        printCSV(TFIDFCSV_PATH + File.separator + this.contextName);

        // Compute the sum group by word
        List<Double> sum = computeSum(transactionValues);
        for (int i = 0; i < sum.size(); i++) {
            wordTfidf.put(allWords.get(i), sum.get(i));
        }
        // Resize the hashmap wordtfidf
        wordTfidf = resizeVector(wordTfidf);

        writeResizedOutputIntoCSV(CONTEXT_PATH + File.separator + this.contextName, wordTfidf);

    }

    public void printCSV(String fileOutputPath) {
        WriterFile fileWriter = new WriterFile(fileOutputPath);
        String text = "";
        for (String w : allWords) {
            text += w + ",";
        }

        text += "\n";
        for (List<String> val : transactionValues) {
            for (String value : val) {
                text += value + ",";
            }
            text += "\n";
        }
        fileWriter.writeFile(text);
    }

    public void readAvro() {
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
                            if (allWords.contains(word)) {
                                values.add(allWords.indexOf(word), value);
                            } else {
                                allWords.add(word);
                                values.add(allWords.indexOf(word), value);
                            }
                        }
                        transactionValues.add(values);

                        tfidfDoc = dataFileReader.next(tfidfDoc);
                        System.out.println(tfidfDoc);

                    }
                } catch (IOException ex) {
                    Logger.getLogger(TFIDFDriver.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }

    public List<Double> computeSum(List<List<String>> values) {
        List<Double> sumOfValues = new LinkedList<Double>();

        for (int i = 0; i < values.get(0).size(); i++) {
            double wordIValue = 0.0;
            for (List<String> value : values) {
                wordIValue += Double.parseDouble(value.get(i));
            }
            sumOfValues.add(wordIValue);
        }
        return sumOfValues;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public double getThreshold() {
        return this.threshold;
    }

    @Override
    public void computeMean() {
        double meanTfidf = 0.0;
        Collection<Double> values = wordTfidf.values();
        for (Double d : values) {
            meanTfidf += d;
        }
        meanTfidf = meanTfidf / values.size();
        this.setThreshold(meanTfidf);
    }

    public HashMap<String, Double> resizeVector(HashMap<String, Double> wordsValue) {
        HashMap<String, Double> resizedVector = new HashMap<>();

        Set<String> words = wordsValue.keySet();
        for (String key : words) {
            if (wordsValue.get(key) >= this.getThreshold()) {
                resizedVector.put(key, wordsValue.get(key));
            }
        }
        return resizedVector;
    }

    public void writeResizedOutputIntoCSV(String fileOutputPath, HashMap<String, Double> wordSum) {
        WriterFile fo = new WriterFile(fileOutputPath);
        String textToWrite = "";
        Set<String> words = wordSum.keySet();
        for (String word : words) {
            textToWrite += word + "," + String.valueOf(wordSum.get(word)) + "\n";
        }
        fo.writeFile(textToWrite);
    }
}
