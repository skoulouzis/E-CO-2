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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
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
import term.avro.Term;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;

public class WordFrequencyInDocDriver extends Configured implements Tool {

    // hashmap for the itemset
    private static List<String> itemset;

    private boolean isHaddopOn() {
        SocketAddress sockaddr = new InetSocketAddress("fs0.das4.cs.vu.nl", 8080);
// Create your socket
        Socket socket = new Socket();
        boolean online = true;
// Connect with 10 s timeout
        try {
            socket.connect(sockaddr, 200);
        } catch (SocketTimeoutException stex) {
            // treating timeout errors separately from other io exceptions
            // may make sense
            online = false;
        } catch (IOException iOException) {
            online = false;
        } finally {
            // As the close() operation can also throw an IOException
            // it must caught here
            try {
                socket.close();
            } catch (IOException ex) {
                // feel free to do something moderately useful here, eg log the event
            }

        }
        return online;
    }

    public static class WordFrequencyInDocMapper extends Mapper<AvroKey<Term>, NullWritable, Text, IntWritable> {

        public WordFrequencyInDocMapper() {
        }

        protected void map(AvroKey<Term> key, NullWritable value, Context context)
                throws IOException, InterruptedException {
            String uid = key.datum().getUid().toString();
            List<CharSequence> descriptionList = key.datum().getGlosses();

            String description = "";
            for (CharSequence text : descriptionList) {
                description += text + " ";
            }

            description = description.toLowerCase();

            for (String s : itemset) {
                if (description.contains(s)) {
                    while (description.contains(s)) {
                        StringBuilder valueBuilder = new StringBuilder();
                        valueBuilder.append(s);
                        valueBuilder.append("@");
                        valueBuilder.append(uid);
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
                valueBuilder.append(uid);
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
            context.write(new AvroKey<Text>(key), new AvroValue<Integer>(sum));

        }
    } // end of reducer class

    // runWordFrequencyInDocDriver --> run (args[])
    @Override
    public int run(String[] args) throws Exception {
        Configuration jobconf = getConf();

//        if (isHaddopOn()) {
//            jobconf.set("fs.defaultFS", "hdfs://master.ib.cluster:8020");
//            jobconf.set("mapred.job.tracker", "localhost:9001");
//        }

//        try {
        new Path(args[1]).getFileSystem(jobconf).delete(new Path(args[1]), true);
//        } catch (java.net.ConnectException ex) {
//
//            jobconf.set("fs.defaultFS", "file:///");
//            jobconf.set("mapred.job.tracker", null);
//            new Path(args[1]).getFileSystem(jobconf).delete(new Path(args[1]), true);
//        }

        itemset = new LinkedList<String>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[2])));
        String line;
        while ((line = br.readLine()) != null) {
            String[] components = line.split("/");
            itemset.add(components[0]);
        }
        Job job = new Job(jobconf);
        job.setJarByClass(WordFrequencyInDocDriver.class);
        job.setJobName("Word Frequency In Doc Driver");

        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setInputFormatClass(AvroKeyInputFormat.class);
        job.setMapperClass(WordFrequencyInDocMapper.class);
        AvroJob.setInputKeySchema(job, Term.getClassSchema());

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Integer.class);
        job.setReducerClass(WordFrequencyInDocReducer.class);

        return (job.waitForCompletion(true) ? 0 : 1);

    }

}
