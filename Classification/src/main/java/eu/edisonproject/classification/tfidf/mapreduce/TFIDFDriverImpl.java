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
package eu.edisonproject.classification.tfidf.mapreduce;

import distances.avro.Distances;
import document.avro.Document;
import eu.edisonproject.utility.file.ReaderFile;
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
import org.apache.hadoop.util.ToolRunner;
import term.avro.Term;

/**
 *
 * @author Michele Sparamonti (michele.sparamonti@eng.it)
 */
public class TFIDFDriverImpl implements ITFIDFDriver {

    //where to read the frequent itemset
    public static String INPUT_ITEMSET;
    //where to read the data for MapReduce#1
    public static String INPUT_PATH1;
    //where to put the data in hdfs when MapReduce#1 will finish
    public static String OUTPUT_PATH1;
    // where to read the data for the MapReduce#2
    public static String INPUT_PATH2;
    // where to put the data in hdfs when the MapReduce#2 will finish
    public static String OUTPUT_PATH2;
    // where to read the data for the MapReduce#3
    public static String INPUT_PATH3;
// where to put the data in hdfs when the MapReduce#3 will finish
    public static String OUTPUT_PATH3;

    // where to read the data for the MapReduce#4.
    public static String INPUT_PATH4;
    // where to put the data in hdfs when the MapReduce# will finish
    public static String OUTPUT_PATH4;

    public static String DISTANCES_VECTOR_PATH;
    // where to put the csv with the tfidf
    public static String COMPETENCES_PATH;

//    private final String contextName;
    private String finalOutputPath;
//    private final List<Distances> distancesValues;

    private static final Logger LOGGER = Logger.getLogger(TFIDFDriverImpl.class.getName());

//    private Connection conn;
//    private final String[] families = {"info","data analytics","data management curation","data science engineering","scientific research methods","domain knowledge"};
    public TFIDFDriverImpl(String contextName, String inputRootPath) {

        INPUT_ITEMSET = inputRootPath + File.separator + "etc" + File.separator + "itemset.csv";
        OUTPUT_PATH1 = inputRootPath + File.separator + "etc" + File.separator + "Classification" + File.separator + "1-word-freq";
        INPUT_PATH2 = inputRootPath + File.separator + "etc" + File.separator + "Classification" + File.separator + "1-word-freq";
        OUTPUT_PATH2 = inputRootPath + File.separator + "etc" + File.separator + "Classification" + File.separator + "2-word-counts";
        INPUT_PATH3 = inputRootPath + File.separator + "etc" + File.separator + "Classification" + File.separator + "2-word-counts";
        OUTPUT_PATH3 = inputRootPath + File.separator + "etc" + File.separator + "Classification" + File.separator + "3-tf-idf";
        INPUT_PATH4 = inputRootPath + File.separator + "etc" + File.separator + "Classification" + File.separator + "3-tf-idf";
        OUTPUT_PATH4 = inputRootPath + File.separator + "etc" + File.separator + "Classification" + File.separator + "4-distances";
        DISTANCES_VECTOR_PATH = inputRootPath + File.separator + "etc" + File.separator + "Classification" + File.separator + "5-tf-idf-csv";
        COMPETENCES_PATH = inputRootPath + File.separator + "etc" + File.separator + "Classification" + File.separator + "competences-vector";

//        this.contextName = contextName + ".csv";
//        this.distancesValues = new LinkedList<>();
        File f = new File(DISTANCES_VECTOR_PATH);
        if (!f.exists()) {
            f.mkdir();
        }
        this.finalOutputPath = DISTANCES_VECTOR_PATH + File.separator + contextName;

    }

    /**
     *
     * @param inputPath
     */
    @Override
    public void executeTFIDF(String inputPath) {

//        try {
//            createTable(TableName.valueOf("job post distance"), families);
//        } catch (IOException ex) {
//            Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.SEVERE, null, ex);
//        }
        INPUT_PATH1 = inputPath;
        int numberOfDocuments = 0;
        File file = new File(INPUT_PATH1);
        File[] filesInDir;
        if (file.isDirectory()) {
            filesInDir = file.listFiles();
            for (File fileSplit : filesInDir) {
//                DatumReader<Term> documentDatumReader = new SpecificDatumReader<>(Term.class);
//                DataFileReader<Term> dataFileReader;
                DatumReader<Document> documentDatumReader = new SpecificDatumReader<>(Document.class);
                DataFileReader<Document> dataFileReader;
                try {
                    dataFileReader = new DataFileReader<>(fileSplit, documentDatumReader);

                    while (dataFileReader.hasNext()) {
                        //Count the number of rows inside the .avro
                        Document d = dataFileReader.next();
                        numberOfDocuments++;
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, ex, null);
                }

            }
            LOGGER.log(Level.INFO, "Number of Documents {0}", numberOfDocuments);

            try {
                String[] args1 = {INPUT_PATH1, OUTPUT_PATH1, INPUT_ITEMSET};
                ToolRunner.run(new WordFrequencyInDocDriver(), args1);

                String[] args2 = {INPUT_PATH2, OUTPUT_PATH2};
                ToolRunner.run(new WordCountsForDocsDriver(), args2);
                String[] args3 = {INPUT_PATH3, OUTPUT_PATH3, String.valueOf(numberOfDocuments)};
                ToolRunner.run(new WordsInCorpusTFIDFDriver(), args3);
                String[] args4 = {INPUT_PATH4, OUTPUT_PATH4, COMPETENCES_PATH};
                ToolRunner.run(new CompetencesDistanceDriver(), args4);
            } catch (Exception ex) {
                Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.SEVERE, "MapReduce Fail", ex);
            }
        } else {
            Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.SEVERE, "You must specify the input folder not a specific document", file);
        }

    }

    public void readDistancesOutputAndPrintCSV() {
        ReaderFile rf = new ReaderFile(OUTPUT_PATH4 + File.separator + "part-r-00000");
        String text = rf.readFileWithN();
        String[] textLine = text.split("\n");
        WriterFile fileWriter = new WriterFile(finalOutputPath);
        String textToPrint = "";
        for (String line : textLine) {
            String[] keyValue = line.split("\t");
            String[] field = keyValue[0].split("@");
            String[] distances = keyValue[1].split(";");
            textToPrint += field[1] + ";" + field[0] + ";" + field[2] + ";";
            for (String d : distances) {
                textToPrint += d + ";";
            }
            textToPrint += "\n";
        }
        fileWriter.writeFile(textToPrint);
    }
}
