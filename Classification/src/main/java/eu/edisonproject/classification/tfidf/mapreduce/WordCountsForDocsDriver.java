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

import org.apache.avro.Schema;
import org.apache.avro.mapred.AvroKey;
import org.apache.avro.mapred.AvroValue;
import org.apache.avro.mapreduce.AvroJob;
import org.apache.avro.mapreduce.AvroKeyValueInputFormat;
import org.apache.avro.mapreduce.AvroKeyValueOutputFormat;
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

public class WordCountsForDocsDriver extends Configured implements Tool{

    // where to put the data in hdfs when we're done     private static final String OUTPUT_PATH = ".."+File.separator+"etc"+File.separator+"2-word-counts";

    // where to read the data from.    private static final String INPUT_PATH = ".."+File.separator+"etc"+File.separator+"1-word-freq";

    public static class WordCountsForDocsMapper extends Mapper<LongWritable, Text, Text, Text> {

        public WordCountsForDocsMapper() {
        }

        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String[] keyValues = value.toString().split("\t");
            System.out.println(value.toString());
            String[] wordsKey = keyValues[0].toString().split("@");
            String documentId = wordsKey[1];
            String title = wordsKey[2];
            String date = wordsKey[3];
            String word = wordsKey[0];
            String sum = keyValues[1];
            
            String newKey = documentId+"@"+title+"@"+date;
            String newValue = word+"="+sum;
            System.out.println(newKey + "new value "+newValue);
            context.write(new Text(newKey),new Text(newValue));
        }
    } // end of mapper class

    public static class WordCountsForDocsReducer extends Reducer<Text, Text, Text, Text> {

        public WordCountsForDocsReducer() {
        }

        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            int sumOfWordsInDocument = 0;
            Map<String, Integer> tempCounter = new HashMap<String, Integer>();
            for (Text val : values) {
                String[] wordCounter = val.toString().split("=");
                tempCounter.put(wordCounter[0], Integer.valueOf(wordCounter[1]));
                sumOfWordsInDocument += Integer.parseInt(val.toString().split("=")[1]);
            }
            for (String wordKey : tempCounter.keySet()) {
                Text newKey = new Text(wordKey + "@" + key.toString());
                Text newValue = new Text(tempCounter.get(wordKey) + "/" + sumOfWordsInDocument);
                System.out.println(newKey + "  ,  " + newValue);
                context.write(new Text(newKey), new Text(newValue));
            }

        }
    } // end of reducer class
    
    @Override
    public int run(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = new Job(conf, "WordsCountsForDocsDriver");

        job.setJarByClass(WordCountsForDocsDriver.class);
        job.setJobName("Word Counts For Docs Driver");

        Path inPath = new Path(args[0]);
        Path outPath = new Path(args[1]);

        FileInputFormat.setInputPaths(job, inPath);
        FileOutputFormat.setOutputPath(job, outPath);
        outPath.getFileSystem(conf).delete(outPath, true);

        job.setMapperClass(WordCountsForDocsMapper.class);
//        job.setInputFormatClass(AvroKeyValueInputFormat.class);
//        job.setMapperClass(WordCountsForDocsMapper.class);
//        AvroJob.setInputKeySchema(job, Schema.create(Schema.Type.STRING));
//        AvroJob.setInputValueSchema(job, Schema.create(Schema.Type.INT));
//
//        job.setMapOutputKeyClass(Text.class);
//        job.setMapOutputValueClass(Text.class);
        /*Here it is possible put the combiner class
		job.setCombinerClass(AvroAverageCombiner.class);
         */
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setReducerClass(WordCountsForDocsReducer.class);
//        job.setOutputFormatClass(AvroKeyValueOutputFormat.class);
//        job.setReducerClass(WordCountsForDocsReducer.class);
//        AvroJob.setOutputKeySchema(job, Schema.create(Schema.Type.STRING));
//        AvroJob.setOutputValueSchema(job, Schema.create(Schema.Type.STRING));

        return (job.waitForCompletion(true) ? 0 : 1);
    }

}
