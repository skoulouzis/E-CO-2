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
import document.avro.Document;
import eu.edisonproject.utility.file.ConfigHelper;
import eu.edisonproject.utility.file.MyProperties;
import eu.edisonproject.utility.text.processing.StanfordLemmatizer;
import eu.edisonproject.utility.text.processing.StopWord;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.avro.mapred.AvroKey;
import org.apache.avro.mapreduce.AvroJob;
import org.apache.avro.mapreduce.AvroKeyInputFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.lucene.analysis.util.CharArraySet;

public class WordFrequencyInDocDriver extends Configured implements Tool {

  public static class WordFrequencyInDocMapper extends Mapper<AvroKey<Document>, NullWritable, Text, IntWritable> {

    private static final List<String> TERMS = new ArrayList<>();
    private static StopWord cleanStopWord;
    private StanfordLemmatizer cleanLemmatisation;

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
      super.cleanup(context);
    }

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
      if (context.getCacheFiles() != null && context.getCacheFiles().length > 0) {
        URI[] uris = context.getCacheFiles();
        FileSystem fs = FileSystem.get(context.getConfiguration());
        if (TERMS == null || TERMS.size() < 1) {
          Path dictionaryFilePath = new Path(uris[0]);
          String s;
          try (BufferedReader br = new BufferedReader(
                  new InputStreamReader(fs.open(dictionaryFilePath)))) {
            while ((s = br.readLine()) != null) {
              s = s.replaceAll("_", " ").trim();
              TERMS.add(s);
            }
          }
        }

        URI stopwordFile = uris[1];
        if (cleanStopWord == null) {
          CharArraySet stopWordArraySet = new CharArraySet(ConfigHelper.loadStopWords(fs.open(new Path(stopwordFile)).getWrappedStream()), true);
          cleanStopWord = new StopWord(stopWordArraySet);
        }
        cleanLemmatisation = new StanfordLemmatizer();
      }

      Logger.getLogger(WordFrequencyInDocDriver.class.getName()).log(Level.INFO, "terms array has :{0} elemnts", TERMS.size());
    }

    private String trim(String s) {
      while (s.endsWith(" ")) {
        s = s.substring(0, s.lastIndexOf(" "));
      }
      while (s.startsWith(" ")) {
        s = s.substring(s.indexOf(" ") + 1, s.length());
      }
      return s;
    }

    @Override
    protected void map(AvroKey<Document> key, NullWritable value, Context context)
            throws IOException, InterruptedException {

      String documentId = key.datum().getDocumentId().toString();
      String description = key.datum().getDescription().toString().toLowerCase();

      for (String s : TERMS) {
        s = trim(s.replaceAll("_", " "));
        cleanStopWord.setDescription(s);

        cleanLemmatisation.setDescription(cleanStopWord.execute());
        s = trim(cleanLemmatisation.execute());
        while (description.contains(" " + s + " ")) {
          StringBuilder valueBuilder = new StringBuilder();
          valueBuilder.append(s);
          valueBuilder.append("@");
          valueBuilder.append(documentId);
          context.write(new Text(valueBuilder.toString()), new IntWritable(1));
          description = description.replaceFirst(" " + s + " ", " ");
        }
      }

//             Compile all the words using regex
      Pattern p = Pattern.compile("\\w+");
      Matcher m = p.matcher(description);

      // build the values and write <k,v> pairs through the context
      while (m.find()) {
        String matchedKey = m.group().toLowerCase();
        StringBuilder valueBuilder = new StringBuilder();
        valueBuilder.append(matchedKey);
        valueBuilder.append("@");
        valueBuilder.append(documentId);
        valueBuilder.append("@");
        context.write(new Text(valueBuilder.toString()), new IntWritable(1));
      }
    }
  }

  public static class WordFrequencyInDocReducer extends Reducer<Text, IntWritable, Text, Integer> { //AvroKey<Text>, AvroValue<Integer>> {

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {

      Integer sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      System.err.println(key + "," + sum);
      context.write(key, sum);
    }
  }

  @Override
  public int run(String[] args) throws Exception {
    Job job = getJob(args);
    return (job.waitForCompletion(true) ? 0 : 1);
  }

  public Job getJob(String[] args) throws IOException {
    Configuration conf = getConf();
    conf = addPropertiesToConf(conf, args[4]);

    Job job = Job.getInstance(conf);
    job.setJarByClass(this.getClass());
    job.setJobName(this.getClass().getName());

    FileSystem fs = FileSystem.get(conf);
    fs.delete(new Path(args[1]), true);
    Path in = new Path(args[0]);
    Path inHdfs = in;

    Path dictionaryLocal = new Path(args[2]);
    Path dictionaryHDFS = dictionaryLocal;

    Path stopwordsLocal = new Path(args[3]);
    Path stopwordsHDFS = stopwordsLocal;

    if (!conf.get(FileSystem.FS_DEFAULT_NAME_KEY).startsWith("file")) {
      inHdfs = new Path(in.getName());
      fs.delete(inHdfs, true);
      fs.copyFromLocalFile(in, inHdfs);
      fs.deleteOnExit(inHdfs);

      dictionaryHDFS = new Path(dictionaryLocal.getName());
      if (!fs.exists(dictionaryHDFS)) {
        fs.copyFromLocalFile(dictionaryLocal, dictionaryHDFS);
      }
      stopwordsHDFS = new Path(stopwordsLocal.getName());
      if (!fs.exists(stopwordsHDFS)) {
        fs.copyFromLocalFile(stopwordsLocal, stopwordsHDFS);
      }
    }

    FileStatus dictionaryStatus = fs.getFileStatus(dictionaryHDFS);
    dictionaryHDFS = dictionaryStatus.getPath();
    job.addCacheFile(dictionaryHDFS.toUri());

    FileStatus stopwordsStatus = fs.getFileStatus(stopwordsHDFS);
    stopwordsHDFS = stopwordsStatus.getPath();
    job.addCacheFile(stopwordsHDFS.toUri());

    FileInputFormat.setInputPaths(job, inHdfs);
    FileOutputFormat.setOutputPath(job, new Path(args[1]));

    job.setInputFormatClass(AvroKeyInputFormat.class);
    job.setMapperClass(WordFrequencyInDocMapper.class);

    AvroJob.setInputKeySchema(job, Document.getClassSchema());
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(IntWritable.class);

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Integer.class);
    job.setReducerClass(WordFrequencyInDocReducer.class);
    return job;
  }

  private Configuration addPropertiesToConf(Configuration conf, String arg) throws FileNotFoundException, IOException {
    MyProperties prop = ConfigHelper.getProperties(arg);
    Set<Object> keys = prop.keySet();
    for (Object key : keys) {
      String val = prop.getProperty((String) key);
      conf.set((String) key, val);
    }

    String baseEtc = prop.getProperty("hadoop.conf.base.dir");
    if (baseEtc != null) {
      File etc = new File(baseEtc);

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
      }
    }

    conf.set("mapreduce.map.class", WordFrequencyInDocMapper.class.getName());
    conf.set("mapreduce.reduce.class", WordFrequencyInDocReducer.class.getName());
//    conf.set("mapred.jar", jar_Output_Folder+ java.io.File.separator + className+".jar");
    return conf;
  }

  @Override
  public Configuration getConf() {
    Configuration conf = super.getConf();
    if (conf == null) {
      conf = new Configuration();
    }
    return conf;
  }

}
