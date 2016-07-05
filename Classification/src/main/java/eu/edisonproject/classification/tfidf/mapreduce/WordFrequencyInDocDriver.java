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
import eu.edisonproject.classification.avro.Document;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.avro.Schema;
import org.apache.avro.mapred.AvroKey;
import org.apache.avro.mapred.AvroValue;
import org.apache.avro.mapreduce.AvroJob;
import org.apache.avro.mapreduce.AvroKeyInputFormat;
import org.apache.avro.mapreduce.AvroKeyValueOutputFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class WordFrequencyInDocDriver {

    // where to read the frequent itemset
    // private static final String ITEMSET_PATH = ".." + File.separator + "etc" + File.separator + "itemset.csv";
    // where to put the data in hdfs when we're done
    //private static final String OUTPUT_PATH = ".." + File.separator + "etc" + File.separator + "1-word-freq";
    // where to read the data from.
    //private static final String INPUT_PATH = ".." + File.separator + "etc" + File.separator + "input";
    // hashmap for the itemset
    private static List<String> itemset;

    public static class WordFrequencyInDocMapper extends Mapper<AvroKey<Document>, NullWritable, Text, IntWritable> {

        public WordFrequencyInDocMapper() {
        }
   
        protected void map(AvroKey<Document> key, NullWritable value, Context context)
                throws IOException, InterruptedException {
            String title = key.datum().getTitle().toString();
            String description = key.datum().getDescription().toString();
            String date = key.datum().getDate().toString();

            description = description.toLowerCase();

            for (String s : itemset) {
                if (description.contains(s)) {
                    while (description.contains(s)) {
                        StringBuilder valueBuilder = new StringBuilder();
                        valueBuilder.append(s);
                        valueBuilder.append("@");
                        valueBuilder.append(title);
                        valueBuilder.append("@");
                        valueBuilder.append(date);
                        context.write(new Text(valueBuilder.toString()), new IntWritable(1));
                        description = description.replaceFirst(s, "");
                    }
                }
            }

            // Compile all the words using regex
            Pattern p = Pattern.compile("\\w+");
            Matcher m = p.matcher(description);

            // build the values and write <k,v> pairs through the context
            while (m.find()) {
                String matchedKey = m.group().toLowerCase();
                StringBuilder valueBuilder = new StringBuilder();
                valueBuilder.append(matchedKey);
                valueBuilder.append("@");
                valueBuilder.append(title);
                valueBuilder.append("@");
                valueBuilder.append(date);
                // emit the partial <k,v>
                context.write(new Text(valueBuilder.toString()), new IntWritable(1));
            }

        }
    }

    public static class WordFrequencyInDocReducer extends Reducer<Text, IntWritable, AvroKey<Text>, AvroValue<Integer>> {

        public WordFrequencyInDocReducer() {
        }

        protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {

            Integer sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            System.out.println(key.toString());
            System.out.println(sum.toString());
            context.write(new AvroKey<Text>(key), new AvroValue<Integer>(sum));

        }
    } // end of reducer class

    // runWordFrequencyInDocDriver --> run (args[])
    public int runWordFrequencyInDocDriver(String[] args) throws Exception {

        itemset = new LinkedList<String>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[2])));
        String line;
        while ((line = br.readLine()) != null) {
            String[] components = line.split(",");
            itemset.add(components[0]);
        }
        Configuration conf = new Configuration();
        Job job = new Job(conf, "WordFrequencyInDocDriver");

        job.setJarByClass(WordFrequencyInDocDriver.class);
        job.setJobName("Word Frequency In Doc Driver");

        Path inPath = new Path(args[0]);
        Path outPath = new Path(args[1]);

        FileInputFormat.setInputPaths(job, inPath);
        FileOutputFormat.setOutputPath(job, outPath);
        outPath.getFileSystem(conf).delete(outPath, true);

        job.setInputFormatClass(AvroKeyInputFormat.class);
        job.setMapperClass(WordFrequencyInDocMapper.class);
        AvroJob.setInputKeySchema(job, Document.getClassSchema());

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputFormatClass(AvroKeyValueOutputFormat.class);
        job.setReducerClass(WordFrequencyInDocReducer.class);
        AvroJob.setOutputKeySchema(job, Schema.create(Schema.Type.STRING));
        AvroJob.setOutputValueSchema(job, Schema.create(Schema.Type.INT));

        return (job.waitForCompletion(true) ? 0 : 1);
    }
}
