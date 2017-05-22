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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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

public class WordCountsForDocsDriver extends Configured implements Tool {

    public static class WordCountsForDocsMapper extends Mapper<LongWritable, Text, Text, Text> {

        private static final Logger LOGGER = Logger.getLogger(WordCountsForDocsMapper.class.getName());

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String[] keyValues = value.toString().split("\t");
            LOGGER.log(Level.FINE, value.toString());
            String[] wordsKey = keyValues[0].split("@");
            String documentId = wordsKey[1];
//            String title = wordsKey[2];
//            String date = wordsKey[3];
            String word = wordsKey[0];
            String sum = keyValues[1];

//            String newKey = documentId + "@" + title + "@" + date;
            String newKey = documentId;
            String newValue = word + "=" + sum;
            LOGGER.log(Level.FINE, "{0}new value {1}", new Object[]{newKey, newValue});
            context.write(new Text(newKey), new Text(newValue));
        }
    } // end of mapper class

    public static class WordCountsForDocsReducer extends Reducer<Text, Text, Text, Text> {

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            int sumOfWordsInDocument = 0;
            Map<String, Integer> tempCounter = new HashMap<>();
            for (Text val : values) {
                String[] wordCounter = val.toString().split("=");
                tempCounter.put(wordCounter[0], Integer.valueOf(wordCounter[1]));
                sumOfWordsInDocument += Integer.parseInt(val.toString().split("=")[1]);
            }
            for (String wordKey : tempCounter.keySet()) {
                Text newKey = new Text(wordKey + "@" + key.toString());
                Text newValue = new Text(tempCounter.get(wordKey) + "/" + sumOfWordsInDocument);
                context.write(new Text(newKey), new Text(newValue));
            }

        }
    } // end of reducer class

    @Override
    public int run(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job  = Job.getInstance(conf);

        job.setJarByClass(WordCountsForDocsDriver.class);
        job.setJobName("Word Counts For Docs Driver");

        Path inPath = new Path(args[0]);
        Path outPath = new Path(args[1]);

        FileInputFormat.setInputPaths(job, inPath);
        FileOutputFormat.setOutputPath(job, outPath);
        outPath.getFileSystem(conf).delete(outPath, true);

        job.setMapperClass(WordCountsForDocsMapper.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setReducerClass(WordCountsForDocsReducer.class);

        return (job.waitForCompletion(true) ? 0 : 1);
    }

}
