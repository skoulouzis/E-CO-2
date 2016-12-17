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
import java.util.Properties;
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

//  public static String INPUT_ITEMSET;
//  public static String categotiesPath;
//  public static String numOfLines;
//  public static String stopwordsPath = ".." + File.separator + "etc" + File.separator + "stopwords.csv";
//  public String OUT;
  /**
   *
   * @param inputPath
   * @param outputPath
   * @param categotiesPath
   * @param props
   */
  public void executeTFIDF(String inputPath, String outputPath, String categotiesPath, Properties props) {
    long start = System.currentTimeMillis();
    String itemsetFilePath = props.getProperty("itemset.file", System.getProperty("user.home") + "workspace" + File.separator + "E-CO-2" + File.separator + "etc" + File.separator + "allTerms.csv");

    String stopwordsPath = props.getProperty("stop.words.file", System.getProperty("user.home") + "workspace" + File.separator + "E-CO-2" + File.separator + "etc" + File.separator + "stopwords.csv");
    String numOfLines = props.getProperty("num.of.terms", "2000");

    String etcHadoop = props.getProperty("hadoop.conf.base.dir", "");
    try {
      File items = new File(itemsetFilePath);
      if (!items.exists()) {
        throw new IOException(items.getAbsoluteFile() + " not found");
      }

      String wordFreqOutPath = System.currentTimeMillis() + "_" + UUID.randomUUID() + "-TFIDFDriverImpl-1-word-freq";

      if (items.length() < 200000000) {
        String avroFilePath = System.currentTimeMillis() + "_" + UUID.randomUUID() + "-TFIDFDriverImpl-avro";
        Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.INFO, "Starting text2Avro");
        text2Avro(inputPath, avroFilePath, stopwordsPath);

        String[] args1 = {avroFilePath, wordFreqOutPath, itemsetFilePath, stopwordsPath, etcHadoop};
        ToolRunner.run(new WordFrequencyInDocDriver(), args1);
      } else {
        Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.INFO, "Starting TermWordFrequency");
        String[] args1 = {itemsetFilePath, wordFreqOutPath, inputPath, stopwordsPath, numOfLines, etcHadoop};
        ToolRunner.run(new TermWordFrequency(), args1);
      }
      String docFreqOutputPath = System.currentTimeMillis() + "_" + UUID.randomUUID() + "-TFIDFDriverImpl-2-word-counts";
      String[] args2 = {wordFreqOutPath, docFreqOutputPath, etcHadoop};
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
      String tfIdfOutputPath = System.currentTimeMillis() + "_" + UUID.randomUUID() + "-TFIDFDriverImpl-3-tf-idf";
      String[] args3 = {docFreqOutputPath, tfIdfOutputPath, String.valueOf(numberOfDocuments), etcHadoop};
      Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.INFO, "args3:{0}", Arrays.toString(args3));
      ToolRunner.run(new WordsInCorpusTFIDFDriver(), args3);

      StringBuilder fileNames = new StringBuilder();
      String prefix = "";
      for (File name : files) {
        if (name.isFile() && FilenameUtils.getExtension(name.getName()).endsWith("txt")) {
          Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.INFO, "fileNames:{0}", name);
          fileNames.append(prefix);
          prefix = ",";
          fileNames.append(FilenameUtils.removeExtension(name.getName()).replaceAll("_", ""));
        }
      }

      String cosineSimOutputPath = System.currentTimeMillis() + "_" + UUID.randomUUID() + "-TFIDFDriverImpl-4-distances";
      String[] args4 = {tfIdfOutputPath, cosineSimOutputPath, categotiesPath, fileNames.toString(), etcHadoop};
      Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.INFO, "args4:{0}", Arrays.toString(args4));
      ToolRunner.run(new CompetencesDistanceDriver(), args4);

      Configuration conf = new Configuration();
      FileSystem fs = FileSystem.get(conf);
      Path hdfsRes = new Path(cosineSimOutputPath);
      FileStatus[] results = fs.listStatus(hdfsRes);
      for (FileStatus s : results) {
        Path dest = new Path(outputPath + "" + File.separator + "" + s.getPath().getName());
        Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.INFO, "Copy: {0} to: {1}", new Object[]{s.getPath(), dest});
        fs.copyToLocalFile(s.getPath(), dest);
      }
      fs.delete(hdfsRes, true);

    } catch (Exception ex) {
      Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.SEVERE, null, ex);
    }
    Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.INFO, "Elapsed: {0} ms", (System.currentTimeMillis() - start));
  }

//    public void readDistancesOutputAndPrintCSV() {
//        ReaderFile rf = new ReaderFile(cosineSimOutputPath + File.separator + "part-r-00000");
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
  public static void text2Avro(String inputPath, String outputPath, String stopwordsPath) {
    Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.INFO, "Start");
    File out = new File(outputPath);
    out.getAbsoluteFile().delete();
    out.getAbsoluteFile().mkdirs();

    IDataPrepare dp = new DataPrepare(inputPath, outputPath, stopwordsPath);
    dp.execute();

  }
}
