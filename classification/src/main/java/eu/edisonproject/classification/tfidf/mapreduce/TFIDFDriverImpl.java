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

import eu.edisonproject.classification.prepare.controller.DataPrepare;
import eu.edisonproject.classification.prepare.controller.IDataPrepare;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.ToolRunner;

/**
 *
 * @author Michele Sparamonti (michele.sparamonti@eng.it)
 */
public class TFIDFDriverImpl {

  //where to read the frequent itemset
  public static String INPUT_ITEMSET;
  //where to put the data in hdfs when MapReduce#1 will finish
//  public static String OUTPUT_PATH1 = System.currentTimeMillis() + "-TFIDFDriverImpl-1-word-freq";
  // where to read the data for the MapReduce#2
//  public static String INPUT_PATH2 = OUTPUT_PATH1;
  // where to put the data in hdfs when the MapReduce#2 will finish
//  public static String OUTPUT_PATH2 = System.currentTimeMillis() + "-TFIDFDriverImpl-2-word-counts";
  // where to read the data for the MapReduce#3
//  public static String INPUT_PATH3 = OUTPUT_PATH2;
// where to put the data in hdfs when the MapReduce#3 will finish
//  public static String OUTPUT_PATH3 = System.currentTimeMillis() + "-TFIDFDriverImpl-3-tf-idf";

  // where to read the data for the MapReduce#4.
//  public static String INPUT_PATH4 = OUTPUT_PATH3;
  // where to put the data in hdfs when the MapReduce# will finish
//  public static String OUTPUT_PATH4 = System.currentTimeMillis() + "-TFIDFDriverImpl-4-distances";
  // where to put the csv with the tfidf
  public static String COMPETENCES_PATH;

//    private String finalOutputPath;
  public static String NUM_OF_LINES;
  public static String STOPWORDS_PATH = ".." + File.separator + "etc" + File.separator + "stopwords.csv";
  public String OUT;
//  public String AVRO_FILE = System.currentTimeMillis() + "-TFIDFDriverImpl-avro";

  public TFIDFDriverImpl() {
  }

  /**
   *
   * @param inputPath
   */
  public void executeTFIDF(String inputPath) {

//        try {
//            createTable(TableName.valueOf("job post distance"), families);
//        } catch (IOException ex) {
//            Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.SEVERE, null, ex);
//        }
    try {
      File items = new File(INPUT_ITEMSET);
      if (!items.exists()) {
        throw new IOException(items.getAbsoluteFile() + " not found");
      }

      String OUTPUT_PATH1 = System.currentTimeMillis() + "_" + UUID.randomUUID() + "-TFIDFDriverImpl-1-word-freq";

      if (items.length() < 200000000) {
        String AVRO_FILE = System.currentTimeMillis() + "_" + UUID.randomUUID() + "-TFIDFDriverImpl-avro";
        Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.INFO, "Starting text2Avro");
        text2Avro(inputPath, AVRO_FILE);

        Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.INFO, "Starting WordFrequencyInDocDriver: {0},{1},{2},{3},{4}", new Object[]{AVRO_FILE, OUTPUT_PATH1, INPUT_ITEMSET, NUM_OF_LINES, STOPWORDS_PATH});
        String[] args1 = {AVRO_FILE, OUTPUT_PATH1, INPUT_ITEMSET, STOPWORDS_PATH, "/cm/shared/package/hadoop/hadoop-2.5.0/etc/hadoop/"};
        ToolRunner.run(new WordFrequencyInDocDriver(), args1);
      } else {
        Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.INFO, "Starting TermWordFrequency");
        String[] args1 = {INPUT_ITEMSET, OUTPUT_PATH1, inputPath, STOPWORDS_PATH, NUM_OF_LINES};
        ToolRunner.run(new TermWordFrequency(), args1);
      }
      String OUTPUT_PATH2 = System.currentTimeMillis() + "_" + UUID.randomUUID() + "-TFIDFDriverImpl-2-word-counts";;
      String[] args2 = {OUTPUT_PATH1, OUTPUT_PATH2};
      ToolRunner.run(new WordCountsForDocsDriver(), args2);

      File docs = new File(inputPath);

      File[] files = docs.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.toLowerCase().endsWith(".txt");
        }
      });
      Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.INFO, "docs:{0}", docs.getAbsolutePath());
      int numberOfDocuments = files.length;
      String OUTPUT_PATH3 = System.currentTimeMillis() + "_" + UUID.randomUUID() + "-TFIDFDriverImpl-3-tf-idf";
      String[] args3 = {OUTPUT_PATH2, OUTPUT_PATH3, String.valueOf(numberOfDocuments)};
      ToolRunner.run(new WordsInCorpusTFIDFDriver(), args3);

      StringBuilder fileNames = new StringBuilder();
      String prefix = "";
      for (File name : files) {
        if (name.isFile() && FilenameUtils.getExtension(name.getName()).endsWith("txt")) {
          fileNames.append(prefix);
          prefix = ",";
          fileNames.append(FilenameUtils.removeExtension(name.getName()).replaceAll("_", ""));
        }
      }
      String OUTPUT_PATH4 = System.currentTimeMillis() + "_" + UUID.randomUUID() + "-TFIDFDriverImpl-4-distances";
      String[] args4 = {OUTPUT_PATH3, OUTPUT_PATH4, COMPETENCES_PATH, fileNames.toString()};
      Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.INFO, "args4:{0}", Arrays.toString(args4));
      ToolRunner.run(new CompetencesDistanceDriver(), args4);

      Configuration conf = new Configuration();
      FileSystem fs = FileSystem.get(conf);
      Path hdfsRes = new Path(OUTPUT_PATH4);
      FileStatus[] results = fs.listStatus(hdfsRes);
      for (FileStatus s : results) {
        Path dest = new Path(OUT + "/" + s.getPath().getName());
        Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.INFO, "Copy: {0} to: {1}", new Object[]{s.getPath(), dest});
        fs.copyToLocalFile(s.getPath(), dest);
      }
      fs.delete(hdfsRes, true);

    } catch (Exception ex) {
      Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

//    public void readDistancesOutputAndPrintCSV() {
//        ReaderFile rf = new ReaderFile(OUTPUT_PATH4 + File.separator + "part-r-00000");
//        String text = rf.readFileWithN();
//        String[] textLine = text.split("\n");
//        WriterFile fileWriter = new WriterFile(finalOutputPath);
//        String textToPrint = "";
//        for (String line : textLine) {
//            String[] keyValue = line.split("\t");
//            String[] field = keyValue[0].split("@");
//            String[] distances = keyValue[1].split(";");
//            textToPrint += field[1] + ";" + field[0] + ";" + field[2] + ";";
//            for (String d : distances) {
//                textToPrint += d + ";";
//            }
//            textToPrint += "\n";
//        }
//        fileWriter.writeFile(textToPrint);
//    }
  public static void text2Avro(String inputPath, String outputPath) {
    Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.INFO, "Start");
    File out = new File(outputPath);
    out.getAbsoluteFile().delete();
    out.getAbsoluteFile().mkdirs();

    IDataPrepare dp = new DataPrepare(inputPath, outputPath, STOPWORDS_PATH);
    dp.execute();

  }
}
