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
import eu.edisonproject.utility.file.ConfigHelper;
import eu.edisonproject.utility.file.MyProperties;
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
public class TFIDFDriverImpl implements ITFIDFDriver {

  /**
   *
   * @param inputPath
   * @param outputPath
   * @param competencesVectorPath
   * @param prop
   * @param propertiesFilePath
   * @param useToolRunner
   * @throws java.io.IOException
   */
  @Override
  public void executeTFIDF(String inputPath, String outputPath, String competencesVectorPath, String propFilePath, Boolean useToolRunner) throws IOException {
    File docs = new File(inputPath);
    if (!docs.exists()) {
      throw new IOException("Folder " + inputPath + " not found");
    }
    MyProperties prop = ConfigHelper.getProperties(propFilePath);

    try {
      String itemsetFilePath = prop.getProperty("itemset.file", ".." + File.separator + "etc" + File.separator + "allTerms.csv");
      File items = new File(itemsetFilePath);
      if (!items.exists()) {
        throw new IOException(items.getAbsoluteFile() + " not found");
      }
      String numOfLines = prop.getProperty("map.reduce.num.of.lines", "200");
      String stopwordsFilePath = prop.getProperty("stop.words.file", ".." + File.separator + "etc" + File.separator + "stopwords.csv");
      String outputPath1 = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + "-TFIDFDriverImpl-1-word-freq";
      if (items.length() < 200000000) {
        Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.INFO, "Starting text2Avro");
        String avroFile = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + "-TFIDFDriverImpl-avro";
        text2Avro(inputPath, avroFile, stopwordsFilePath);

        Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.INFO, "Starting WordFrequencyInDocDriver: {0},{1},{2},{3},{4}", new Object[]{avroFile, outputPath1, itemsetFilePath, numOfLines, stopwordsFilePath});
        String[] args1 = {avroFile, outputPath1, itemsetFilePath, stopwordsFilePath, propFilePath};
        WordFrequencyInDocDriver wfInDoc = new WordFrequencyInDocDriver();
        if (useToolRunner) {
          ToolRunner.run(wfInDoc, args1);
        } else {
          wfInDoc.run(args1);
        }

      } else {
        Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.INFO, "Starting TermWordFrequency");
        String[] args1 = {itemsetFilePath, outputPath1, inputPath, stopwordsFilePath, numOfLines};
        ToolRunner.run(new TermWordFrequency(), args1);
      }

      String outputPath2 = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + "-TFIDFDriverImpl-2-word-counts";
      String[] args2 = {outputPath1, outputPath2};
      ToolRunner.run(new WordCountsForDocsDriver(), args2);

      File[] files = docs.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.toLowerCase().endsWith(".txt");
        }
      });
      Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.INFO, "docs:{0}", docs.getAbsolutePath());
      int numberOfDocuments = files.length;
      // where to read the data for the MapReduce#3
      String outputPath3 = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + "-TFIDFDriverImpl-3-tf-idf";
      String[] args3 = {outputPath2, outputPath3, String.valueOf(numberOfDocuments)};
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

      String outputPath4 = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + "-TFIDFDriverImpl-4-distances";
      String[] args4 = {outputPath3, outputPath4, competencesVectorPath, fileNames.toString()};
      Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.INFO, "args4:{0}", Arrays.toString(args4));
      ToolRunner.run(new CompetencesDistanceDriver(), args4);

      Configuration conf = new Configuration();

      FileSystem fs = FileSystem.get(conf);
      Path hdfsRes = new Path(outputPath4);
      Path res = fs.resolvePath(hdfsRes);
      Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.INFO, "Output result", res.toUri());

      FileStatus[] results = fs.listStatus(hdfsRes);
      for (FileStatus s : results) {
        Path dest = new Path(outputPath + "/" + s.getPath().getName());
        Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.INFO, "Copy: {0} to: {1}", new Object[]{s.getPath(), dest});
        fs.copyToLocalFile(s.getPath(), dest);
      }
//      fs.delete(hdfsRes, true);

    } catch (Exception ex) {
      Logger.getLogger(TFIDFDriverImpl.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

//    public void readDistancesOutputAndPrintCSV() {
//        ReaderFile rf = new ReaderFile(outputPath4 + File.separator + "part-r-00000");
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
