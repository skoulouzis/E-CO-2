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

import document.avro.Document;
import document.avro.DocumentAvroSerializer;
import eu.edisonproject.utility.commons.ValueComparator;
import eu.edisonproject.utility.file.CSVFileReader;
import eu.edisonproject.utility.file.ConfigHelper;
import term.avro.Term;
import eu.edisonproject.utility.file.ReaderFile;
import eu.edisonproject.utility.file.WriterFile;
import eu.edisonproject.utility.text.processing.StopWord;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.util.ToolRunner;
import org.apache.lucene.analysis.util.CharArraySet;
//import org.apache.avro.hadoop.io.AvroSerialization;

/**
 *
 * @author S. Koulouzis
 */
public class TFIDFTermsDriver implements ITFIDFDriver {

    //where to read the frequent itemset
    public static String TEXT_FILES_DIR_PATH;
    //where to read the data for MapReduce#1
    private String TERMS_FILE_PATH;
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
    public static String TFIDFCSV_PATH = ".." + File.separator + "etc" + File.separator + "Training" + File.separator + "5-csv";
    // where to put the csv with the context vector
    public static String TERMS = ".." + File.separator + "etc" + File.separator + "Training" + File.separator + "terms.csv";

    // the list of all words
    private final List<String> allWords;
    // the list of all value for each transaction
    private final List<String[]> transactionValues;

    private HashMap<String, Double> wordTfidf;

    private double threshold;
    public static String STOPWORDS_PATH = ".." + File.separator + "etc" + File.separator + "stopwords.csv";
    private StopWord cleanStopWord;

    public TFIDFTermsDriver() {
        this.allWords = new LinkedList<String>();
        this.transactionValues = new LinkedList<String[]>();
        wordTfidf = new HashMap<>();

    }

    @Override
    public void executeTFIDF(String inputPath) {
        TERMS_FILE_PATH = inputPath;

        try {
            String[] args1 = {TERMS_FILE_PATH, OUTPUT_PATH1, TEXT_FILES_DIR_PATH, STOPWORDS_PATH};
            ToolRunner.run(new TermWordFrequency(), args1);

            String[] args2 = {INPUT_PATH2, OUTPUT_PATH2, TEXT_FILES_DIR_PATH, STOPWORDS_PATH};
            ToolRunner.run(new TFIDF(), args2);
            Map<String, Double> map = CSVFileReader.csvFileToMapDoubleValue(OUTPUT_PATH2 + File.separator + "part-r-00000", "\t");

            ValueComparator bvc = new ValueComparator(map);
            Map<String, Double> sorted_map = new TreeMap(bvc);
            sorted_map.putAll(map);

            try (PrintWriter out = new PrintWriter(TERMS)) {
                for (String key : sorted_map.keySet()) {
                    Double value = map.get(key);
                    key = key.toLowerCase().trim().replaceAll(" ", "_");
                                  while (key.endsWith("_")) {
                    key = key.substring(0, key.lastIndexOf("_"));
                }
                while (key.startsWith("_")) {
                    key = key.substring(key.indexOf("_") + 1, key.length());
                }
                    out.print(key + "," + value + "\n");
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(TFIDFTermsDriver.class.getName()).log(Level.SEVERE, "TFIDF fail", ex);
        }

    }

    @Override
    public void driveProcessResizeVector() {

    }

    public void printCSV(String fileOutputPath, String fileName) {

    }

    public List<Double> computeSum(List<String[]> values) {
        List<Double> sumOfValues = new LinkedList<>();

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

    @Override
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
