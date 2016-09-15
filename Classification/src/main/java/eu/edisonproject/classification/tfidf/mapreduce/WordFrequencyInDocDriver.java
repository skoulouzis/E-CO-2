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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

public class WordFrequencyInDocDriver extends Configured implements Tool {

//    private static List<String> itemset;
    public static class WordFrequencyInDocMapper extends Mapper<AvroKey<Document>, NullWritable, Text, IntWritable> {

        private static final List<String> TERMS = new ArrayList<>();

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            super.cleanup(context);
        }

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            if (context.getCacheFiles() != null && context.getCacheFiles().length > 0) {
                URI[] uris = context.getCacheFiles();
                Path dictionaryFilePath = new Path(uris[0]);
                FileSystem fs = FileSystem.get(context.getConfiguration());
                String s;
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(fs.open(dictionaryFilePath)))) {
                    while ((s = br.readLine()) != null) {
                        s = s.replaceAll("_", " ").trim();
                        TERMS.add(s);
                    }
                }
            }
        }

        @Override
        protected void map(AvroKey<Document> key, NullWritable value, Context context)
                throws IOException, InterruptedException {

            String documentId = key.datum().getDocumentId().toString();
//            String title = key.datum().getTitle().toString();
            String description = key.datum().getDescription().toString().toLowerCase();
//            String date = key.datum().getDate().toString();

            for (String s : TERMS) {
//                System.err.println(s);
                while (description.contains(" " + s + " ")) {
                    StringBuilder valueBuilder = new StringBuilder();
                    valueBuilder.append(s);
                    valueBuilder.append("@");
                    valueBuilder.append(documentId);
//                        valueBuilder.append("@");
//                        valueBuilder.append(title);
//                        valueBuilder.append("@");
//                        valueBuilder.append(date);
                    context.write(new Text(valueBuilder.toString()), new IntWritable(1));
                    description = description.replaceFirst(" " + s + " ", "");
                }
            }

//            for (String s : itemset) {
//                if (description.contains(" " + s + " ")) {
//                    while (description.contains(" " + s + " ")) {
////                        System.err.println(s);
//                        StringBuilder valueBuilder = new StringBuilder();
//                        valueBuilder.append(s);
//                        valueBuilder.append("@");
//                        valueBuilder.append(documentId);
//                        valueBuilder.append("@");
//                        valueBuilder.append(title);
//                        valueBuilder.append("@");
//                        valueBuilder.append(date);
//                        context.write(new Text(valueBuilder.toString()), new IntWritable(1));
//                        description = description.replaceFirst(" " + s + " ", "");
//                    }
//                }
//            }
            // Compile all the words using regex
//            Pattern p = Pattern.compile("\\w+");
//            Matcher m = p.matcher(description);
//
//            // build the values and write <k,v> pairs through the context
//            while (m.find()) {
//                String matchedKey = m.group().toLowerCase();
//                StringBuilder valueBuilder = new StringBuilder();
//                valueBuilder.append(matchedKey);
//                valueBuilder.append("@");
//                valueBuilder.append(documentId);
//                valueBuilder.append("@");
//                valueBuilder.append(title);
//                valueBuilder.append("@");
//                valueBuilder.append(date);
//                // emit the partial <k,v>
//                context.write(new Text(valueBuilder.toString()), new IntWritable(1));
//            }
        }
    }

    public static class WordFrequencyInDocReducer extends Reducer<Text, IntWritable, Text, Integer> { //AvroKey<Text>, AvroValue<Integer>> {

        @Override
        protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {

            Integer sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            context.write(key, sum);
        }
    } // end of reducer class

    @Override
    public int run(String[] args) throws Exception {
//        itemset = new LinkedList<String>();
//        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[2])));
//        String line;
//        while ((line = br.readLine()) != null) {
//            String[] components = line.split("/");
//            itemset.add(components[0]);
//        }
        Configuration conf = new Configuration();
        Job job = new Job(conf, "");
        job.setJarByClass(WordFrequencyInDocDriver.class);
        job.setJobName("Word Frequency In Doc Driver");

        FileSystem fs = FileSystem.get(conf);
        fs.delete(new Path(args[1]), true);
        Path in = new Path(args[0]);
        Path inHdfs = in;

        Path dictionaryLocal = new Path(args[2]);
        Path dictionaryHDFS = dictionaryLocal;
        if (!conf.get(FileSystem.FS_DEFAULT_NAME_KEY).startsWith("file")) {
            inHdfs = new Path(in.getName());
            fs.delete(inHdfs, true);
            fs.copyFromLocalFile(in, inHdfs);
            fs.deleteOnExit(inHdfs);

            dictionaryHDFS = new Path(dictionaryLocal.getName());
            if (!fs.exists(dictionaryHDFS)) {
                fs.copyFromLocalFile(dictionaryLocal, dictionaryHDFS);
            }
        }

        FileStatus dictionaryStatus = fs.getFileStatus(dictionaryHDFS);
        dictionaryHDFS = dictionaryStatus.getPath();
        job.addCacheFile(dictionaryHDFS.toUri());

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
        return (job.waitForCompletion(true) ? 0 : 1);
    }

}
