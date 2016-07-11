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

import eu.edisonproject.classification.avro.Distances;
import document.avro.Document;
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
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.io.DatumReader;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.hadoop.util.ToolRunner;

/**
 *
 * @author Michele Sparamonti (michele.sparamonti@eng.it)
 */
public class TFIDFDriver implements ITFIDFDriver {

    //where to read the frequent itemset
    private final String INPUT_ITEMSET = ".." + File.separator + "etc" + File.separator + "itemset.csv";
    //where to read the data for MapReduce#1
    private String INPUT_PATH1;
    //where to put the data in hdfs when MapReduce#1 will finish
    private final String OUTPUT_PATH1 = ".." + File.separator + "etc" + File.separator + "Classification" + File.separator + "1-word-freq";

    // where to read the data for the MapReduce#2
    private final String INPUT_PATH2 = ".." + File.separator + "etc" + File.separator + "Classification" + File.separator + "1-word-freq";
    // where to put the data in hdfs when the MapReduce#2 will finish
    private final String OUTPUT_PATH2 = ".." + File.separator + "etc" + File.separator + "Classification" + File.separator + "2-word-counts";

    // where to read the data for the MapReduce#3
    private final String INPUT_PATH3 = ".." + File.separator + "etc" + File.separator + "Classification" + File.separator + "2-word-counts";
    // where to put the data in hdfs when the MapReduce#3 will finish
    private final String OUTPUT_PATH3 = ".." + File.separator + "etc" + File.separator + "Classification" + File.separator + "3-tf-idf";

    // where to read the data for the MapReduce#4.
    private final String INPUT_PATH4 = ".." + File.separator + "etc" + File.separator + "Classification" + File.separator + "3-tf-idf";
    // where to put the data in hdfs when the MapReduce# will finish
    private final String OUTPUT_PATH4 = ".." + File.separator + "etc" + File.separator + "Classification" + File.separator + "4-tf-idf-document";

    private final String TFIDFCSV_PATH = ".." + File.separator + "etc" + File.separator + "Classification" + File.separator + "5-tf-idf-csv";
    // where to put the csv with the tfidf
    private final String COMPETENCES_PATH = ".." + File.separator + "etc" + File.separator + "training" + File.separator + "competences";

    private final String contextName;
    private final String finalOutputPath;
    private List<Distances> distancesValues;

    public TFIDFDriver(String contextName) {
        this.contextName = contextName + ".csv";
        this.finalOutputPath = COMPETENCES_PATH + File.separator + contextName;
    }

    /**
     *
     * @param inputPath
     */
    @Override
    public void executeTFIDF(String inputPath) {
        INPUT_PATH1 = inputPath;
        int numberOfDocuments = 0;
        File file = new File(INPUT_PATH1);
        File[] filesInDir;
        if (file.isDirectory()) {
            filesInDir = file.listFiles();
            for (File fileSplit : filesInDir) {
                DatumReader<Document> documentDatumReader = new SpecificDatumReader<>(Document.class);
                DataFileReader<Document> dataFileReader;
                try {
                    dataFileReader = new DataFileReader<>(fileSplit, documentDatumReader);

                    while (dataFileReader.hasNext()) {
                        //Count the number of rows inside the .avro
                        dataFileReader.next();
                        numberOfDocuments++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            System.out.println("Number of Documents "+numberOfDocuments);

            try {
                String[] args1 = {INPUT_PATH1, OUTPUT_PATH1, INPUT_ITEMSET};
                ToolRunner.run(new WordFrequencyInDocDriver(), args1);
                String[] args2 = {INPUT_PATH2, OUTPUT_PATH2};
                ToolRunner.run(new WordCountsForDocsDriver(), args2);
                String[] args3 = {INPUT_PATH3, OUTPUT_PATH3, String.valueOf(numberOfDocuments)};
                ToolRunner.run(new WordsInCorpusTFIDFDriver(),args3);
                String[] args4 = {INPUT_PATH4, OUTPUT_PATH4, COMPETENCES_PATH};
                ToolRunner.run(new CompetencesDistanceDriver(),args4);
            } catch (Exception ex) {
                Logger.getLogger(TFIDFDriver.class.getName()).log(Level.SEVERE, "MapReduce Fail", ex);
            }
        }else{
            Logger.getLogger(TFIDFDriver.class.getName()).log(Level.SEVERE, "You must specify the input folder not a specific document", file);
        }

    }

    public void driveProcessResizeVector() {
        // Read the output from avro file
        readAvro();
        printCSV();
    }

    public void printCSV() {
        WriterFile fileWriter = new WriterFile(finalOutputPath);
        String text = "";
        for (Distances d : distancesValues) {
            text += d.getDocumentId() + " ," + d.getDate() + " ,";
            for (Double distance : d.getDistances()) {
                text += d + ",";
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
                DatumReader<Distances> distanceDatumReader = new SpecificDatumReader<>(Distances.class);
                DataFileReader<Distances> dataFileReader;
                try {
                    dataFileReader = new DataFileReader<>(f, distanceDatumReader);
                    Distances distanceDoc = null;
                    while (dataFileReader.hasNext()) {
                        //Reuse user object by passing it to next(). This saves us from
                        // allocating and garbage collecting many objects for files with
                        // many items.
                        distanceDoc = dataFileReader.next();
                        distancesValues.add(distanceDoc);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(TFIDFDriver.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }
}
