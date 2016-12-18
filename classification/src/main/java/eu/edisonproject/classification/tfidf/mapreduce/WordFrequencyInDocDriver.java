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
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Map;
import org.apache.avro.mapreduce.AvroJob;
import org.apache.avro.mapreduce.AvroKeyInputFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;

public class WordFrequencyInDocDriver extends Configured implements Tool {

  @Override
  public int run(String[] args) throws Exception {

    Job job = getJob(args);

    return (job.waitForCompletion(true) ? 0 : 1);
  }

  private Job getJob(String[] args) throws IOException {
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
    job.setMapperClass(TermFrequencyMapper.class);
    AvroJob.setInputKeySchema(job, Document.getClassSchema());
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(IntWritable.class);

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Integer.class);
    job.setReducerClass(SumReducer.class);
    return job;
  }

  @Override
  public Configuration getConf() {
    Configuration configuration = super.getConf();
    if (configuration == null) {
      configuration = new Configuration();
    }
    return configuration;
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
      conf.set("mapreduce.job.map.class", TermFrequencyMapper.class.getName());
      conf.set("mapreduce.job.reduce.class", SumReducer.class.getName());
    } else {
      conf.set("mapreduce.framework.name", "local");
      conf.set("mapred.job.tracker", "local");
      conf.set("ffs.defaultFS", "file:///");
    }

//    for (Map.Entry<String, String> entry : conf) {
//      System.out.println(entry.getKey() + " = " + entry.getValue());
//    }
    return conf;
  }

}
