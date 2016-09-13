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

import static eu.edisonproject.training.tfidf.mapreduce.TFIDFDriverImpl.CONTEXT_PATH;
import eu.edisonproject.utility.commons.ValueComparator;
import eu.edisonproject.utility.file.WriterFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.ToolRunner;
//import org.apache.avro.hadoop.io.AvroSerialization;

/**
 *
 * @author S. Koulouzis
 */
public class TFIDFTermsDriver implements ITFIDFDriver {

    //where to read the frequent itemset
    public static String TEXT_FILES_DIR_PATH;
    //where to read the data for MapReduce#1
    //where to put the data in hdfs when MapReduce#1 will finish
    public static String OUTPUT_PATH1 = "1-word-freq";

    // where to read the data for the MapReduce#2
    public static String INPUT_PATH2 = OUTPUT_PATH1;
    // where to put the data in hdfs when the MapReduce#2 will finish
    public static String OUTPUT_PATH2 = "2-word-counts";

    // where to read the data for the MapReduce#3
    public static String INPUT_PATH3 = OUTPUT_PATH2;
    // where to put the data in hdfs when the MapReduce#3 will finish
    public static String OUTPUT_PATH3 = "3-tf-idf";

    // where to read the data for the MapReduce#4.
    public static String INPUT_PATH4 = OUTPUT_PATH3;
    // where to put the data in hdfs when the MapReduce# will finish
    public static String OUTPUT_PATH4 = "4-tf-idf-document";

    // where to put the csv with the tfidf
    public static String TFIDFCSV_PATH = "5-csv";

    // where to put the csv with the context vector
    public static String OUT;

    // the list of all words
    private final List<String> allWords;
    // the list of all value for each transaction
    private final List<String[]> transactionValues;

    private HashMap<String, Double> wordTfidf;

    private double threshold;
    public static String STOPWORDS_PATH = ".." + File.separator + "etc" + File.separator + "stopwords.csv";
    public static String NUM_OF_LINES;

    public TFIDFTermsDriver() {
        this.allWords = new LinkedList<>();
        this.transactionValues = new LinkedList<>();
        wordTfidf = new HashMap<>();
    }

    @Override
    public void executeTFIDF(String inputPath) {
        try {

            String[] args1 = {inputPath, OUTPUT_PATH1, TEXT_FILES_DIR_PATH, STOPWORDS_PATH, NUM_OF_LINES};
            ToolRunner.run(new TermWordFrequency(), args1);
//            INPUT_PATH2 = "/tmp/1473765100879/1-word-freq";
            String[] args2 = {INPUT_PATH2, OUTPUT_PATH2};
            ToolRunner.run(new WordCountsForDocsDriver(), args2);

            File docs = new File(TEXT_FILES_DIR_PATH);
            File[] files = docs.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".txt");
                }
            });

            String[] args3 = {INPUT_PATH3, OUTPUT_PATH3, String.valueOf(docs.length())};
            ToolRunner.run(new WordsInCorpusTFIDFDriver(), args3);

            String[] args4 = {INPUT_PATH4, OUTPUT_PATH4};
            ToolRunner.run(new WordsGroupByTitleDriver(), args4);

            Configuration conf = new Configuration();
            FileSystem fs = FileSystem.get(conf);
            Path hdfsRes = new Path(OUTPUT_PATH4 + File.separator + "part-r-00000");
            hdfsRes = fs.getFileStatus(hdfsRes).getPath();

            readTFIDFResult(fs, hdfsRes);

//            Path outPath = new Path(OUT);
//            printCSV(fs, outPath.ge);
            List<Double> sum = computeSum(transactionValues);
            for (int i = 0; i < sum.size(); i++) {
                wordTfidf.put(allWords.get(i), sum.get(i));
            }

            computeMean();
            // Resize the hashmap wordtfidf
            wordTfidf = resizeVector(wordTfidf);
            writeResizedOutputIntoCSV(OUT, wordTfidf);

        } catch (Exception ex) {
            Logger.getLogger(TFIDFTermsDriver.class.getName()).log(Level.SEVERE, "TFIDF fail", ex);
        }

    }

    public void readTFIDFResult(FileSystem fs, Path p) throws IOException {
//        File file = new File(OUTPUT_PATH4 + File.separator + "part-r-00000");
//        ReaderFile rf = new ReaderFile(file.getPath());
//        String text = rf.readFileWithN();

        String line;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(fs.open(p)))) {
            while ((line = br.readLine()) != null) {
                String[] keyValue = line.split("\t");
                String[] pairWordValue = keyValue[1].split("/");
                for (String pair : pairWordValue) {
                    String[] s = pair.split(":");
                    String word = s[0];
//                String value = s[1];
                    if (!allWords.contains(word)) {
                        allWords.add(word);
                    }
                }

            }
        }
        line = null;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(fs.open(p)))) {
            while ((line = br.readLine()) != null) {
                String[] keyValue = line.split("\t");
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
    }

    @Override
    public void driveProcessResizeVector() {
    }

    public void printCSV(FileSystem fs, Path fileOutputPath) throws IOException {
//        fs.mkdirs(fileOutputPath.getParent());

        try (PrintWriter out = new PrintWriter(fs.create(fileOutputPath))) {
            for (String w : allWords) {
                out.print(w + ";");
            }
            out.print("\n");
            for (String[] val : transactionValues) {
                for (String value : val) {
                    out.print(value + ";");
                }
                out.print("\n");
            }
        }
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

    public void writeResizedOutputIntoCSV(String fileOutputPath, Map<String, Double> wordSum) throws IOException {

        ValueComparator bvc = new ValueComparator(wordTfidf);
        Map<String, Double> sorted_map = new TreeMap(bvc);
        sorted_map.putAll(wordTfidf);

        try (PrintWriter out = new PrintWriter(new File(fileOutputPath))) {
            for (String key : sorted_map.keySet()) {
                Double value = wordTfidf.get(key);
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
    }
}
