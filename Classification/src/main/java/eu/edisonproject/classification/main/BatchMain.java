/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
 /*
 * Copyright 2016 Michele Sparamonti, Spiros Koulouzis
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
package eu.edisonproject.classification.main;

import eu.edisonproject.utility.file.MyProperties;
import eu.edisonproject.classification.distance.CosineSimilarityMatrix;
import eu.edisonproject.classification.tfidf.mapreduce.TFIDFDriverImpl;
import eu.edisonproject.utility.commons.ValueComparator;
import eu.edisonproject.utility.file.CSVFileReader;
import eu.edisonproject.utility.file.ConfigHelper;
import eu.edisonproject.utility.file.ReaderFile;
import eu.edisonproject.utility.file.WriterFile;
import eu.edisonproject.utility.text.processing.StanfordLemmatizer;
import eu.edisonproject.utility.text.processing.StopWord;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 *
 * @author Michele Sparamonti (Michele.Sparamonti@eng.it), S. Koulouzis
 */
public class BatchMain {

    private static MyProperties prop;

    public static void main(String[] args) throws Exception {
        try {
//            args = new String[1];
//            args[0] = "..";
//            TestDataFlow.execute(args);
//            System.exit(0);
//            TestTFIDF.execute(args);

            Options options = new Options();

            Option operation = new Option("op", "operation", true, "type of operation to perform. "
                    + "To convert txt to avro 'a'.\n"
                    + "For running clasification on avro documents 'c'");
            operation.setRequired(true);
            options.addOption(operation);

            Option input = new Option("i", "input", true, "input path");
            input.setRequired(false);
            options.addOption(input);

            Option output = new Option("o", "output", true, "output file");
            output.setRequired(false);
            options.addOption(output);

            Option competencesVector = new Option("c", "competences-vector", true, "competences vectors");
            competencesVector.setRequired(false);
            options.addOption(competencesVector);

            Option v1 = new Option("v1", "vector1", true, "");
            v1.setRequired(false);
            options.addOption(v1);

            Option v2 = new Option("v2", "vector2", true, "");
            v2.setRequired(false);
            options.addOption(v2);

            Option popertiesFile = new Option("p", "properties", true, "path for a properties file");
            popertiesFile.setRequired(false);
            options.addOption(popertiesFile);

            CommandLineParser parser = new BasicParser();
            CommandLine cmd = parser.parse(options, args);
            String propPath = cmd.getOptionValue("properties");
            if (propPath == null) {
                prop = ConfigHelper.getProperties(".." + File.separator + "etc" + File.separator + "configure.properties");
            } else {
                prop = ConfigHelper.getProperties(propPath);
            }

            switch (cmd.getOptionValue("operation")) {
                case "a":
                    text2Avro(cmd.getOptionValue("input"), cmd.getOptionValue("output"));
                    break;
                case "c":
                    calculateTFIDF(cmd.getOptionValue("input"), cmd.getOptionValue("output"), cmd.getOptionValue("competences-vector"));
                    break;
                case "p":
//                    -op p -v2 $HOME/Downloads/msc.csv -v1 $HOME/Downloads/job.csv -p $HOME/workspace/E-CO-2/etc/classification.properties
                    profile(cmd.getOptionValue("v1"), cmd.getOptionValue("v2"));
                    break;
            }

        } catch (IllegalArgumentException | ParseException | IOException ex) {
            Logger.getLogger(BatchMain.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static void calculateTFIDF(String in, String out, String competencesVectorPath) throws IOException {

        try {
            TFIDFDriverImpl tfidfDriver = new TFIDFDriverImpl();

            if (tfidfDriver.NUM_OF_LINES == null) {
                tfidfDriver.NUM_OF_LINES = prop.getProperty("map.reduce.num.of.lines", "200");
            }

            TFIDFDriverImpl.STOPWORDS_PATH = System.getProperty("stop.words.file");

            if (TFIDFDriverImpl.STOPWORDS_PATH == null) {
                TFIDFDriverImpl.STOPWORDS_PATH = prop.getProperty("stop.words.file", ".." + File.separator + "etc" + File.separator + "stopwords.csv");
            }
            tfidfDriver.INPUT_ITEMSET = System.getProperty("itemset.file");
            if (tfidfDriver.INPUT_ITEMSET == null) {
                tfidfDriver.INPUT_ITEMSET = prop.getProperty("itemset.file", ".." + File.separator + "etc" + File.separator + "dictionaryAll.csv");
            }

            tfidfDriver.COMPETENCES_PATH = competencesVectorPath;
            tfidfDriver.OUT = out;
            tfidfDriver.executeTFIDF(in);

        } finally {
        }
    }

    private static void text2Avro(String inputPath, String outputPath) {
        String stopWordsPath = System.getProperty("stop.words.file");

        if (stopWordsPath == null) {
            stopWordsPath = prop.getProperty("stop.words.file", ".." + File.separator + "etc" + File.separator + "stopwords.csv");
        }
        CharArraySet stopWordArraySet = new CharArraySet(ConfigHelper.loadStopWords(stopWordsPath), true);
        StopWord cleanStopWord = new StopWord(stopWordArraySet);
        StanfordLemmatizer cleanLemmatisation = new StanfordLemmatizer();
        File filesInDir = new File(inputPath);
        for (File f : filesInDir.listFiles()) {
            if (f.isFile() && FilenameUtils.getExtension(f.getName()).endsWith("txt")) {
                ReaderFile rf = new ReaderFile(f.getAbsolutePath());
                String contents = rf.readFile();
                cleanStopWord.setDescription(contents);
                String cleanCont = cleanStopWord.execute().toLowerCase();
                cleanLemmatisation.setDescription(cleanCont);
                cleanCont = cleanLemmatisation.execute();
                WriterFile wf = new WriterFile(outputPath + File.separator + f.getName());
                wf.writeFile(cleanCont);
            }
        }
//        IDataPrepare dp = new DataPrepare(inputPath, outputPath, stopWordsPath);
//        dp.execute();

    }

    private static void profile(String csvFile1, String csvFile2) throws IOException, Exception {
        Map<String, Collection<Double>> cv = CSVFileReader.csvCompetanceToMap(csvFile1, ",", Boolean.TRUE);
        Map<String, Collection<Double>> jobVec = CSVFileReader.csvCompetanceToMap(csvFile2, ",", Boolean.TRUE);
        CosineSimilarityMatrix cosineFunction = new CosineSimilarityMatrix();
        String k1 = cv.keySet().iterator().next();
        Map<String, Double> winners = new HashMap<>();
        for (String k : jobVec.keySet()) {
            Collection<Double> j = jobVec.get(k);
            double d = cosineFunction.computeDistance(cv.get(k1), j);
            if (!Double.isNaN(d)) {
                winners.put(k, d);
            }

        }

        System.err.println(k1 + "," + cv.get(k1));
        ValueComparator bvc = new ValueComparator(winners);
        Map<String, Double> sorted_map = new TreeMap(bvc);
        sorted_map.putAll(winners);
        for (String k : sorted_map.keySet()) {
            System.err.println(k + "," + jobVec.get(k));
        }

    }
}
