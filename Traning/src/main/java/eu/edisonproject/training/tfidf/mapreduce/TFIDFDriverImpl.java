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

import term.avro.Term;
import eu.edisonproject.utility.file.ReaderFile;
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
import org.apache.hadoop.util.ToolRunner;
//import org.apache.avro.hadoop.io.AvroSerialization;

/**
 *
 * @author Michele Sparamonti (michele.sparamonti@eng.it)
 */
public class TFIDFDriverImpl implements ITFIDFDriver {

    //where to read the frequent itemset
    public static String INPUT_ITEMSET = ".." + File.separator + "etc" + File.separator + "itemset.csv";
    //where to read the data for MapReduce#1
    private String INPUT_PATH1;
    //where to put the data in hdfs when MapReduce#1 will finish
    public static String OUTPUT_PATH1 = ".." + File.separator + "etc" + File.separator + "Training" + File.separator + "1-word-freq";

    // where to read the data for the MapReduce#2
    public static String INPUT_PATH2 = ".." + File.separator + "etc" + File.separator + "Training" + File.separator + "1-word-freq";
    // where to put the data in hdfs when the MapReduce#2 will finish
    public static String OUTPUT_PATH2 = ".." + File.separator + "etc" + File.separator + "Training" + File.separator + "2-word-counts";

    // where to read the data for the MapReduce#3
    public static String INPUT_PATH3 = ".." + File.separator + "etc" + File.separator + "Training" + File.separator + "2-word-counts";
    // where to put the data in hdfs when the MapReduce#3 will finish
    public static String OUTPUT_PATH3 = ".." + File.separator + "etc" + File.separator + "Training" + File.separator + "3-tf-idf";

    // where to read the data for the MapReduce#4.
    public static String INPUT_PATH4 = ".." + File.separator + "etc" + File.separator + "Training" + File.separator + "3-tf-idf";
    // where to put the data in hdfs when the MapReduce# will finish
    public static String OUTPUT_PATH4 = ".." + File.separator + "etc" + File.separator + "Training" + File.separator + "4-tf-idf-document";

    // where to put the csv with the tfidf
    private final String TFIDFCSV_PATH = ".." + File.separator + "etc" + File.separator + "Training" + File.separator + "5-csv";
    // where to put the csv with the context vector
    public static String CONTEXT_PATH = ".." + File.separator + "etc" + File.separator + "Training" + File.separator + "6-context-vector";

    // the name of the context (categories) that it is under analysis
    private final String contextName;

    // the list of all words
    private final List<String> allWords;
    // the list of all value for each transaction
    private final List<String[]> transactionValues;

    private HashMap<String, Double> wordTfidf;

    private double threshold;

    public TFIDFDriverImpl(String contextName) {
        this.contextName = contextName + ".csv";
        this.allWords = new LinkedList<String>();
        this.transactionValues = new LinkedList<String[]>();
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
                //if (FilenameUtils.getExtension(fileSplit.getName()).endsWith(".avro")) {
                DatumReader<Term> termDatumReader = new SpecificDatumReader<>(Term.class);
                DataFileReader<Term> dataFileReader;
                try {
                    dataFileReader = new DataFileReader<Term>(fileSplit, termDatumReader);

                    while (dataFileReader.hasNext()) {
                        //Count the number of rows inside the .avro
                        dataFileReader.next();
                        numberOfDocuments++;
                    }
                    Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.INFO, "Num. of Documents: {0}", numberOfDocuments);
                } catch (IOException e) {
                    Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.SEVERE,null, e);
                }
                //}
            }

            try {
                String[] args1 = {INPUT_PATH1, OUTPUT_PATH1, INPUT_ITEMSET};
                ToolRunner.run(new WordFrequencyInDocDriver(), args1);

                String[] args2 = {INPUT_PATH2, OUTPUT_PATH2};
                ToolRunner.run(new WordCountsForDocsDriver(), args2);

                String[] args3 = {INPUT_PATH3, OUTPUT_PATH3, String.valueOf(numberOfDocuments)};
                ToolRunner.run(new WordsInCorpusTFIDFDriver(), args3);

                String[] args4 = {INPUT_PATH4, OUTPUT_PATH4};
                ToolRunner.run(new WordsGroupByTitleDriver(), args4);
            } catch (Exception ex) {
                Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.SEVERE, "TFIDF fail", ex);
            }

        } else {
            Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.SEVERE, "You must specify the input folder not a specific document", file);
        }

    }

    @Override
    public void driveProcessResizeVector() {
        // Read the output from avro file
        readAvro();

        // Now from the .avro produce in hdfs we want a .csv
        printCSV(TFIDFCSV_PATH, this.contextName);

        // Compute the sum group by word
        List<Double> sum = computeSum(transactionValues);
        for (int i = 0; i < sum.size(); i++) {
            wordTfidf.put(allWords.get(i), sum.get(i));
        }

        computeMean();
        // Resize the hashmap wordtfidf
        wordTfidf = resizeVector(wordTfidf);
        writeResizedOutputIntoCSV(CONTEXT_PATH, this.contextName, wordTfidf);

    }

    public void printCSV(String fileOutputPath, String fileName) {

        File f = new File(fileOutputPath);
        if (!f.exists()) {
            f.mkdir();
        }

        WriterFile fileWriter = new WriterFile(fileOutputPath + File.separator + fileName);
        String text = "";
        for (String w : allWords) {
            text += w + ";";
        }

        text += "\n";
        for (String[] val : transactionValues) {
            for (String value : val) {
                text += value + ";";
            }
            text += "\n";
        }
        fileWriter.writeFile(text);
    }

    public void readAvro() {
        File file = new File(OUTPUT_PATH4 + File.separator + "part-r-00000");
        File[] filesInDir = file.listFiles();
        ReaderFile rf = new ReaderFile(file.getPath());
        String text = rf.readFileWithN();
        String[] fields = text.split("\n");
        for (String field : fields) {
            String[] keyValue = field.split("\t");
            String[] pairWordValue = keyValue[1].split("/");
            for (String pair : pairWordValue) {
                String[] s = pair.split(":");
                String word = s[0];
                String value = s[1];
                if (!allWords.contains(word)) {
                    allWords.add(word);
                }
            }
        }

        ReaderFile rf2 = new ReaderFile(file.getPath());
        String text2 = rf2.readFileWithN();
        String[] fields2 = text2.split("\n");
        for (String field : fields2) {
            List<String> values = new LinkedList<>();
            String[] keyValue = field.split("\t");
            String[] pairWordValue = keyValue[1].split("/");
            List<Integer> index = new LinkedList<>();
            String[] lineValues = new String[allWords.size()];
            for (String pair : pairWordValue) {
                String[] s = pair.split(":");
                String word = s[0];
                String value = s[1];
                lineValues[allWords.indexOf(word)] = value;
                index.add(allWords.indexOf(word));
            }
            for (int i = 0; i < lineValues.length; i++) {
                if (!index.contains(i)) {
                    lineValues[i] = "0";
                }
            }
            transactionValues.add(lineValues);
        }
    }

    public List<Double> computeSum(List<String[]> values) {
        List<Double> sumOfValues = new LinkedList<Double>();

        for (int i = 0; i < values.get(0).length; i++) {
            double wordIValue = 0.0;
            for (int j = 0; j < values.size(); j++) {
                if (!values.get(j)[i].equals("0") || !values.get(j)[i].contains("-∞")) {
                    wordIValue += Double.parseDouble(values.get(j)[i].replace(",", "."));
                }
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

    public void writeResizedOutputIntoCSV(String fileOutputPath, String fileName, HashMap<String, Double> wordSum) {
        File f = new File(fileOutputPath);
        if (!f.exists());
        f.mkdir();
        WriterFile fo = new WriterFile(fileOutputPath + File.separator + fileName);
        String textToWrite = "";
        Set<String> words = wordSum.keySet();
        for (String word : words) {
            textToWrite += word + ";" + String.valueOf(wordSum.get(word)) + "\n";
        }
        fo.writeFile(textToWrite);
    }
}
