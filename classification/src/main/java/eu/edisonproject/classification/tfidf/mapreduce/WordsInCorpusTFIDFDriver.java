/*
 * Copyright 2016 Michele Sparamonti & Spiros Koulouzis
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

/**
 *
 * @author Michele Sparamonti (michele.sparamonti@eng.it)
 */
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;

public class WordsInCorpusTFIDFDriver extends Configured implements Tool {

  public static class WordsInCorpusTFIDFMapper extends Mapper<LongWritable, Text, Text, Text> {

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
      String[] pairKeyValue = value.toString().split("\t");
      String[] keyValues = pairKeyValue[0].split("@");
      String outKey = "";
      for (int i = 1; i < keyValues.length; i++) {
        outKey += keyValues[i] + "@";
      }
      String valueString = pairKeyValue[1];
      context.write(new Text(keyValues[0]), new Text(outKey + valueString));
    }
  } // end of mapper class

//	public static class WordsInCorpusTFIDFReducer extends Reducer<Text, Text, AvroKey<Text>, AvroValue<Tfidf>> {
  public static class WordsInCorpusTFIDFReducer extends Reducer<Text, Text, Text, Text> {

    private static final DecimalFormat DF = new DecimalFormat("###.########");
    private Double numberOfDocumentsInCorpus;

    @Override
    protected void setup(Reducer.Context context) throws IOException, InterruptedException {
      Configuration conf = context.getConfiguration();
      String numberOfDocs = conf.get("number.of.documents");
      numberOfDocumentsInCorpus = Double.valueOf(numberOfDocs);
    }

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
      // total frequency of this word
      int numberOfDocumentsInCorpusWhereKeyAppears = 0;
      Map<String, String> tempFrequencies = new HashMap<>();
      for (Text val : values) {
        String[] documentAndFrequencies = val.toString().split("@");
        numberOfDocumentsInCorpusWhereKeyAppears++;
        tempFrequencies.put(documentAndFrequencies[0], documentAndFrequencies[1]);
//                tempFrequencies.put(documentAndFrequencies[0] + "@" + documentAndFrequencies[1] + "@" + documentAndFrequencies[2], documentAndFrequencies[3]);
      }

//            String lineValue = "";
      for (String document : tempFrequencies.keySet()) {
        String[] wordFrequenceAndTotalWords = tempFrequencies.get(document).split("/");

        //Term frequency is the quocient of the number of terms in document and the total number of terms in doc
        double tf = Double.valueOf(wordFrequenceAndTotalWords[0]) / Double.valueOf(wordFrequenceAndTotalWords[1]);

        //interse document frequency quocient between the number of docs in corpus and number of docs the term appears
        double idf = (double) numberOfDocumentsInCorpus / (double) numberOfDocumentsInCorpusWhereKeyAppears;

        //given that log(10) = 0, just consider the term frequency in documents
        double tfIdf = numberOfDocumentsInCorpus == numberOfDocumentsInCorpusWhereKeyAppears
                ? tf : tf * Math.log10(idf);

//                String[] documentFields = document.split("@");
//                lineValue += documentFields[0] + ";" + key.toString() + ";" + DF.format(tfIdf) + "\n";
//                String newKey = documentFields[0] + "@" + documentFields[1] + "@" + documentFields[2];
        String newKey = document;
        String newValue = key.toString() + "/" + DF.format(tfIdf);
        System.err.println(newKey + "," + newValue);
        context.write(new Text(newKey), new Text(newValue));

      }
    }
  } // end of reducer class
  //changed run(String[]) in runWordsInCorpusTFIDFDriver(String[])

  @Override
  public int run(String[] args) throws IOException, InterruptedException, ClassNotFoundException {

    Job job = getJob(args);

    return (job.waitForCompletion(true) ? 0 : 1);
  }

  private Job getJob(String[] args) throws IOException {
    Configuration conf = getConf();
    conf = addPropertiesToConf(conf, args[3]);
    conf.set("number.of.documents", args[2]);
    Job job = Job.getInstance(conf);

    job.setJarByClass(WordsInCorpusTFIDFDriver.class);
    //This row must be changed
    job.setJobName(this.getClass().getName());

    Path inPath = new Path(args[0]);
    Path outPath = new Path(args[1]);

    FileInputFormat.setInputPaths(job, inPath);
    FileOutputFormat.setOutputPath(job, outPath);
    outPath.getFileSystem(conf).delete(outPath, true);

    job.setMapperClass(WordsInCorpusTFIDFMapper.class);

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    job.setReducerClass(WordsInCorpusTFIDFReducer.class);
    return job;
  }

  private Configuration addPropertiesToConf(Configuration conf, String etcPath) {
    File etc = new File(etcPath);
    File[] files = etc.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith(".xml");
      }
    });
    if (files != null) {
      for (File f : files) {
        conf.addResource(new org.apache.hadoop.fs.Path(f.getAbsolutePath()));
      }
      conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
    }
    return conf;
  }

  @Override
  public Configuration getConf() {
    Configuration configuration = super.getConf();
    if (configuration == null) {
      configuration = new Configuration();
    }
    return configuration;
  }

}
