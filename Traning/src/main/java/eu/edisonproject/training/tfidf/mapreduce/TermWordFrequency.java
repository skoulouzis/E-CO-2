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
package eu.edisonproject.training.tfidf.mapreduce;

/**
 *
 * @author Michele Sparamonti (michele.sparamonti@eng.it)
 */
import eu.edisonproject.utility.file.ConfigHelper;
import eu.edisonproject.utility.file.ReaderFile;
import eu.edisonproject.utility.text.processing.StopWord;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
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
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.lucene.analysis.util.CharArraySet;

public class TermWordFrequency extends Configured implements Tool {

    // hashmap for the terms
    private static StopWord cleanStopWord;
    public static Map<String, String> docs;

    public static class TermWordFrequencyMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

        public TermWordFrequencyMapper() {
        }

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            int totalWcount = 0;
            String term = value.toString().split(",")[0];
            if (term.length() > 1) {
                cleanStopWord.setDescription(term.replaceAll("_", " ").trim());
                String cTerm = cleanStopWord.execute();
                while (cTerm.endsWith(" ")) {
                    cTerm = cTerm.substring(0, cTerm.lastIndexOf(" "));
                }
                while (cTerm.startsWith(" ")) {
                    cTerm = cTerm.substring(cTerm.indexOf(" ") + 1, cTerm.length());
                }

                for (String k : docs.keySet()) {
                    String d = docs.get(k);
                    cleanStopWord.setDescription(d.toLowerCase());
                    String contents = cleanStopWord.execute();
                    int count = StringUtils.countMatches(contents, cTerm);
                    totalWcount += count;
//                    context.write(new Text(k + "@" + term), new IntWritable(count));                
                    context.write(new Text(term + "@" + k), new IntWritable(count));
//                    if (wcount > 0) {
//                        System.err.println(term + ", " + wcount + ", " + d);
//                    }

                }

//                System.err.println(new Text(term) + " " + new IntWritable(wcount));
//                context.write(new Text(term), new IntWritable(wcount));
            }
        }
    }

    public static class TermWordFrequencyReducer extends Reducer<Text, IntWritable, Text, Integer> {

        public TermWordFrequencyReducer() {
        }

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
        Configuration jobconf = getConf();

        if (docs == null) {
            docs = new HashMap<>();
        }

        if (docs.isEmpty()) {
            CharArraySet stopWordArraySet = new CharArraySet(ConfigHelper.loadStopWords(args[3]), true);
            cleanStopWord = new StopWord(stopWordArraySet);
            File docsDir = new File(args[2]);
            for (File f : docsDir.listFiles()) {
                if (FilenameUtils.getExtension(f.getName()).endsWith("txt")) {
                    ReaderFile rf = new ReaderFile(f.getAbsolutePath());
                    cleanStopWord.setDescription(rf.readFile());
                    docs.put(f.getName(), cleanStopWord.execute());
                }
            }
        }
        FileSystem fs = FileSystem.get(jobconf);
        fs.delete(new Path(args[1]), true);
        Path in = new Path(args[0]);
        Path inHdfs = new Path(in.getName());
        fs.copyFromLocalFile(in, inHdfs);
        Logger.getLogger(TermWordFrequency.class.getName()).log(Level.INFO, "Copied " + in.toUri() + " to " + inHdfs.toUri());

        Job job = new Job(jobconf);
        job.setJarByClass(TermWordFrequency.class);
        job.setJobName("Word Frequency Term Driver");

        FileInputFormat.setInputPaths(job, inHdfs);
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setInputFormatClass(TextInputFormat.class);
        job.setMapperClass(TermWordFrequencyMapper.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Integer.class);
        job.setReducerClass(TermWordFrequencyReducer.class);

        return (job.waitForCompletion(true) ? 0 : 1);

    }

}
