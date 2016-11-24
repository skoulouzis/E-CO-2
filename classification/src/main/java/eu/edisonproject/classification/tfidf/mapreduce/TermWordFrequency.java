/*
 * Copyright 2016 Michele Sparamonti & Spiros Koulouzis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except dictionary compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to dictionary writing, software
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
import eu.edisonproject.utility.file.ConfigHelper;
import eu.edisonproject.utility.text.processing.StanfordLemmatizer;
import eu.edisonproject.utility.text.processing.StopWord;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.lucene.analysis.util.CharArraySet;

public class TermWordFrequency extends Configured implements Tool {

  // hashmap for the terms
  private static StopWord cleanStopWord;

  public static class TermWordFrequencyMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    private FileStatus[] files;
    private StanfordLemmatizer cleanLemmatisation;

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
      super.cleanup(context);
    }

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
//            long start = System.currentTimeMillis();
      if (context.getCacheFiles() != null && context.getCacheFiles().length > 0) {
        URI[] uris = context.getCacheFiles();
        URI stopwordFile = uris[0];
        FileSystem fs = FileSystem.get(context.getConfiguration());
        if (cleanStopWord == null) {
          CharArraySet stopWordArraySet = new CharArraySet(ConfigHelper.loadStopWords(fs.open(new Path(stopwordFile)).getWrappedStream()), true);
          cleanStopWord = new StopWord(stopWordArraySet);
        }

        URI hdfsDocs = uris[1];

        Path docPath = new Path(hdfsDocs);
        files = fs.listStatus(docPath);

      }
      cleanLemmatisation = new StanfordLemmatizer();
    }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
      FileSystem fs = FileSystem.get(context.getConfiguration());

      String s = value.toString();

      s = s.replaceAll("_", " ").trim();
      cleanStopWord.setDescription(s);
      cleanLemmatisation.setDescription(cleanStopWord.execute().trim());
      s = cleanLemmatisation.execute().trim();

      s = trim(s);

      for (FileStatus stat : files) {
        Path filePath = stat.getPath();
        if (FilenameUtils.getExtension(filePath.getName()).endsWith("txt")) {
          String line;
          StringBuilder sb = new StringBuilder();
          try (BufferedReader br = new BufferedReader(
                  new InputStreamReader(fs.open(filePath)))) {
            while ((line = br.readLine()) != null) {
              sb.append(line);
            }
          }
          String description = sb.toString();
          cleanStopWord.setDescription(description);
          cleanLemmatisation.setDescription(cleanStopWord.execute().trim());
          description = trim(cleanLemmatisation.execute().trim());

          while (description.contains(" " + s + " ")) {
            StringBuilder valueBuilder = new StringBuilder();
            valueBuilder.append(s);
            valueBuilder.append("@");
            valueBuilder.append(stat.getPath().getName());
//                        valueBuilder.append("@");
//                        valueBuilder.append(stat.getPath().getName());
//                        valueBuilder.append("@");
//                        valueBuilder.append(date);
            context.write(new Text(valueBuilder.toString()), new IntWritable(1));
//                        System.err.println(valueBuilder.toString());
            description = description.replaceFirst(" " + s + " ", "");
          }

          Pattern p = Pattern.compile("\\w+");
          Matcher m = p.matcher(description);
          // build the values and write <k,v> pairs through the context
          while (m.find()) {
            String matchedKey = m.group().toLowerCase();
            StringBuilder valueBuilder = new StringBuilder();
            valueBuilder.append(matchedKey);
            valueBuilder.append("@");
            valueBuilder.append(stat.getPath().getName());
            valueBuilder.append("@");
            valueBuilder.append(stat.getPath().getName());
//                valueBuilder.append("@");
//                valueBuilder.append(date);
            // emit the partial <k,v>
            context.write(new Text(valueBuilder.toString()), new IntWritable(1));
            System.err.println(valueBuilder.toString());
          }
        }
      }

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
  }

  public static class TermWordFrequencyReducer extends Reducer<Text, IntWritable, Text, Integer> {

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {

      Integer sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
//            System.err.println(key + " " + sum);
      context.write(key, sum);

    }
  } // end of reducer class

  // runWordFrequencyInDocDriver --> run (args[])
  @Override
  public int run(String[] args) throws Exception {
    Configuration conf = getConf();

    conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
    conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());

    conf.set("yarn.resourcemanager.address", "localhost:8032");
    conf.set("fs.default.name", "hdfs://localhost:9000");

    Job job = Job.getInstance(conf);
    FileSystem fs = FileSystem.get(conf);
    fs.delete(new Path(args[1]), true);
    Path dictionary = new Path(args[0]);
    Path dictionaryHdfs = dictionary;

    Path localDocs = new Path(args[2]);
    Path hdfsDocs = localDocs;

    Path stopwordsLocal = new Path(args[3]);
    Path stopwordsHDFS = stopwordsLocal;
    if (!conf.get(FileSystem.FS_DEFAULT_NAME_KEY).startsWith("file")) {
      dictionaryHdfs = new Path(dictionary.getName());
      if (!fs.exists(dictionaryHdfs)) {
        fs.copyFromLocalFile(dictionary, dictionaryHdfs);
      }
      hdfsDocs = new Path(localDocs.getName());
      fs.mkdirs(hdfsDocs);
      fs.deleteOnExit(hdfsDocs);

      File[] stats = new File(localDocs.toString()).listFiles();

      for (File stat : stats) {
        Path filePath = new Path(stat.getAbsolutePath());
        if (FilenameUtils.getExtension(filePath.getName()).endsWith("txt")) {
          Path dest = new Path(hdfsDocs.toUri() + "/" + filePath.getName());
          fs.copyFromLocalFile(filePath, dest);
        }
      }
      stopwordsHDFS = new Path(stopwordsLocal.getName());
      if (!fs.exists(stopwordsHDFS)) {
        fs.copyFromLocalFile(stopwordsLocal, stopwordsHDFS);
      }
    }

    FileStatus stopwordsStatus = fs.getFileStatus(stopwordsHDFS);
    stopwordsHDFS = stopwordsStatus.getPath();
    job.addCacheFile(stopwordsHDFS.toUri());

    job.addCacheFile(hdfsDocs.toUri());

    job.setJarByClass(TermWordFrequency.class);
    job.setJobName("Word Frequency Term Driver");

    FileInputFormat.setInputPaths(job, dictionaryHdfs);
    FileOutputFormat.setOutputPath(job, new Path(args[1]));

//        job.setInputFormatClass(TextInputFormat.class);
    job.setInputFormatClass(NLineInputFormat.class);
    NLineInputFormat.addInputPath(job, dictionaryHdfs);
    NLineInputFormat.setNumLinesPerSplit(job, Integer.valueOf(args[4]));
    NLineInputFormat.setMaxInputSplitSize(job, 500);

    job.setMapperClass(TermWordFrequencyMapper.class);

    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(IntWritable.class);

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Integer.class);
    job.setReducerClass(TermWordFrequencyReducer.class);

    return (job.waitForCompletion(true) ? 0 : 1);

  }

}
