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
import eu.edisonproject.classification.distance.CosineSimilarityMatrix;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;

public class CompetencesDistanceDriver extends Configured implements Tool {

  private static Map<String, Map<String, Double>> CATEGORIES_LIST;
//    public static final TableName JOB_POST_COMETENCE_TBL_NAME = TableName.valueOf("categories");

  public static class CompetencesDistanceMapper extends Mapper<LongWritable, Text, Text, Text> {

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
//      Logger.getLogger(CompetencesDistanceMapper.class.getName()).log(Level.INFO, "key: {0} value: {1}", new Object[]{key, value});
      String[] keyValues = value.toString().split("\t");
      String documentID = keyValues[0];
      String word = keyValues[1].split("/")[0];
      String tfidf = keyValues[1].split("/")[1];
//            WriterFile rf = new WriterFile(System.getProperty("user.home") + "/" + this.getClass().getName() + ".dbg");
//            rf.writeFile(documentID + " , " + word + "@" + tfidf);
//            Logger.getLogger(CompetencesDistanceMapper.class.getName()).log(Level.INFO, "{0} , {1}@{2}", new Object[]{documentID, word, tfidf});
      context.write(new Text(documentID), new Text(word + "@" + tfidf));

      writeToLog(documentID + "," + word + "@" + tfidf);

    }

    private void writeToLog(String data) {
      try (FileWriter fstream = new FileWriter(System.getProperty("user.home") + "/" + this.getClass().toString(), true)) {
        try (BufferedWriter out = new BufferedWriter(fstream)) {
          out.write(data);
        }
      } catch (IOException ex) {
        Logger.getLogger(CompetencesDistanceDriver.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

  }

//    public static class CompetencesDistanceReducer extends TableReducer<Text, Text, ImmutableBytesWritable> {
//    public static class CompetencesDistanceReducer extends Reducer<Text, Text, ImmutableBytesWritable, Put> {
  public static class CompetencesDistanceReducer extends Reducer<Text, Text, Text, Text> {

    private MultipleOutputs mos;

    @Override
    protected void reduce(Text text, Iterable<Text> values, Context context) throws IOException, InterruptedException {
      String fileName = FilenameUtils.removeExtension(text.toString()).replaceAll("_", "");
      //The object are grouped for them documentId
      Map<String, Double> distancesNameAndValue = new HashMap<>();
      Map<String, Double> documentWords = new HashMap<>();
//            List<CharSequence> wordToWrite = new LinkedList<>();
//            List<CharSequence> valuesToWrite = new LinkedList<>();

      for (Text value : values) {
//                Logger.getLogger(CompetencesDistanceMapper.class.getName()).log(Level.INFO, "key: " + text + " value: " + value);
        String[] line = value.toString().split("@");
        documentWords.put(line[0], Double.parseDouble(line[1].replace(",", ".")));
      }

      //  List<Double> distances = new LinkedList<Double>();
      CosineSimilarityMatrix cosineFunction = new CosineSimilarityMatrix();

      //for (HashMap<String, Double> competence : listOfCompetencesVector) {
      Set<String> names = CATEGORIES_LIST.keySet();
      Iterator<String> iter = names.iterator();
      while (iter.hasNext()) {
        String key = iter.next();
        Map<String, Double> competence = CATEGORIES_LIST.get(key);
//                HashMap<String, Double> documentToCompetenceSpace = new HashMap<>();

        //Change to the common sub space
        Set<String> words = competence.keySet();
        List<Double> competenceValue = new LinkedList<>();
        List<Double> documentValue = new LinkedList<>();
        for (String word : words) {
          //Align the term written in the csv with the term analysed by MR
          //The terms comosed by two or more words in MR are separeted by whitespace
          //Instead the terms into the csv file are separeteb by "_" char
          String originalWord = word;
          if (word.contains("_")) {
            word = word.replaceAll("_", " ");
          }
          if (documentWords.containsKey(word)) {
            documentValue.add(documentWords.get(word));
            //documentToCompetenceSpace.put(word, documentWords.get(word));
          } else {
            documentValue.add(0.0);
          }

          competenceValue.add(competence.get(originalWord));
        }
//                if (key.equals("DSDA02")) {
//                    System.err.println(words);
//                    System.err.println(competenceValue);
//                    System.err.println(documentValue);
//                }

        if (!competenceValue.isEmpty()) {
          try {
            double distance = cosineFunction.computeDistance(competenceValue, documentValue);
            distancesNameAndValue.put(key, distance);
          } catch (Exception ex) {
            Logger.getLogger(CompetencesDistanceDriver.class.getName()).log(Level.SEVERE, null, ex);
          }
        } else {
          distancesNameAndValue.put(key, 0.0);
        }

      }
//            String[] docIdAndDate = text.toString().split("@");
      List<String> families = new ArrayList<>();
//            families.add("info");

      for (String family : distancesNameAndValue.keySet()) {
        String columnFamily = family.split("-")[0];
        boolean isPresent = false;
        for (String fam : families) {
          if (fam.equals(columnFamily)) {
            isPresent = true;
          }
        }
        if (!isPresent) {
          families.add(columnFamily);
        }
      }

      StringBuilder sb = new StringBuilder();
//            sb.append(docIdAndDate[0]).append("\n");

      sb.append(fileName);
      for (String family : distancesNameAndValue.keySet()) {
        //String key = family; //iterColumn.next();
        Double d = distancesNameAndValue.get(family);
//                        String columnFamily = family.split("-")[0];
//                        String columnQualifier = family.split("-")[1];
//                put.addColumn(Bytes.toBytes(family), Bytes.toBytes(family), Bytes.toBytes(d));
        sb.append(family).append(",").append(d).append("\n");
//                context.write(new Text(docIdAndDate[0] + "\t" + family), new Text(d.toString()));
        context.write(new Text(fileName + "\t" + family), new Text(d.toString()));
//                mos.write(FilenameUtils.removeExtension(docIdAndDate[0]), family, new Text(d.toString()));
        mos.write(fileName, family, new Text(d.toString()));
      }
//            System.err.println(sb.toString());
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
      super.cleanup(context);
      mos.close();
    }

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
      if (context.getCacheFiles() != null && context.getCacheFiles().length > 0) {
        URI[] uris = context.getCacheFiles();
        URI compPath = uris[0];
        Path docPath = new Path(compPath);
        CATEGORIES_LIST = new HashMap<>();
        FileSystem fs = FileSystem.get(context.getConfiguration());

        readFolder(docPath, fs);

      }
      mos = new MultipleOutputs(context);
//            Configuration config = context.getConfiguration();
//            String names = config.get("file.names");
//            Set<String> fileName = new HashSet<>();

    }

    private void readFile(FileStatus stat, FileSystem fs) throws IOException {
      Map<String, Double> categoriesFile = new HashMap<>();
      BufferedReader br = new BufferedReader(new InputStreamReader(fs.open(stat.getPath())));
      String line;
      String delimeter = ",";
      while ((line = br.readLine()) != null) {
        String[] value = line.split(delimeter);
        categoriesFile.put(value[0], Double.parseDouble(value[1]));
      }
      CATEGORIES_LIST.put(stat.getPath().getName().replace(".csv", ""), categoriesFile);
    }

    private void readFolder(Path p, FileSystem fs) throws IOException {
      FileStatus[] files = fs.listStatus(p);

      for (FileStatus stat : files) {
        if (stat.isDirectory()) {
          readFolder(stat.getPath(), fs);
        } else if (stat.isFile() && FilenameUtils.getExtension(stat.getPath().getName()).endsWith("csv")) {
          readFile(stat, fs);
        }
      }

    }
  }

  @Override
  public int run(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
    Job job = getJob(args);

    return (job.waitForCompletion(true) ? 0 : 1);
  }

  private Job getJob(String[] args) throws IOException {
    Configuration conf = getConf();
    conf = addPropertiesToConf(conf, args[4]);
    Job job = Job.getInstance(conf);

    //TableMapReduceUtil.addDependencyJars(job); 
    job.setJarByClass(CompetencesDistanceDriver.class);
    //This row must be changed
    job.setJobName(this.getClass().getName());

    Path inPath = new Path(args[0]);
    Path outPath = new Path(args[1]);

    Path competencesPath = new Path(args[2]);
    Path competencesPathHDFS = competencesPath;
    FileSystem fs = FileSystem.get(conf);

    if (!conf.get(FileSystem.FS_DEFAULT_NAME_KEY).startsWith("file")) {
      competencesPathHDFS = new Path(competencesPath.getName());
      if (!fs.exists(competencesPathHDFS)) {
        fs.mkdirs(competencesPathHDFS);
        File[] stats = new File(competencesPath.toString()).listFiles();
        for (File stat : stats) {
          Path filePath = new Path(stat.getAbsolutePath());
          if (FilenameUtils.getExtension(filePath.getName()).endsWith("csv")) {
            Path dest = new Path(competencesPathHDFS.toUri() + "/" + filePath.getName());
            fs.copyFromLocalFile(filePath, dest);
          }
        }
      }
    }
    job.addCacheFile(competencesPathHDFS.toUri());

    FileInputFormat.setInputPaths(job, inPath);

    FileOutputFormat.setOutputPath(job, outPath);
    fs.delete(outPath, true);

    job.setMapperClass(CompetencesDistanceMapper.class);

    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(Text.class);

    job.setReducerClass(CompetencesDistanceReducer.class);
//            job.setOutputFormatClass(TableOutputFormat.class);
//            job.getConfiguration().set(TableOutputFormat.OUTPUT_TABLE, "jobpostcompetence");
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);

    String[] fileNames = args[3].split(",");
    for (String n : fileNames) {
      MultipleOutputs.addNamedOutput(job, n, TextOutputFormat.class,
              Text.class, Text.class);
    }
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
