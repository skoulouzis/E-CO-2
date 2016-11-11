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
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
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
import org.apache.lucene.analysis.util.CharArraySet;

public class TFIDF extends Configured implements Tool {

    // hashmap for the terms
    private static StopWord cleanStopWord;
    private static Set<String> docs;

    public static class IDFMapper extends Mapper<LongWritable, Text, Text, Text> {

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String[] input = value.toString().split("\t");
            String term = input[0];
            String tf = input[1];
            int df = 0;
            if (term.length() > 1) {
                cleanStopWord.setDescription(term.replaceAll("_", " ").trim());
                String cTerm = cleanStopWord.execute();
                while (cTerm.endsWith(" ")) {
                    cTerm = cTerm.substring(0, cTerm.lastIndexOf(" "));
                }
                while (cTerm.startsWith(" ")) {
                    cTerm = cTerm.substring(term.indexOf(" ") + 1, cTerm.length());
                }
                for (String d : docs) {
                    if (d.contains(cTerm)) {
                        df++;
                    }
                }
                context.write(new Text(term), new Text(df + "," + tf));
            }
        }
    }

    public static class IDFReducer extends Reducer<Text, Text, Text, Double> {
        
        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            Integer df = 0;
            Integer tf = 0;
            for (Text val : values) {
                String[] dfidf = val.toString().split(",");
                df += Integer.valueOf(dfidf[0]);
                tf += Integer.valueOf(dfidf[1]);
//                System.err.println(val);
            }

            Double idf = 0.0;
            if (df > 0) {
                idf = Math.log((double) docs.size() / (double) df);
            }
//            System.err.println(key + " " + (tf * idf));
            double tfidf = tf * idf;
            if (tfidf > 0) {
                context.write(key, tfidf);
            }

        }
    }

    @Override
    public int run(String[] args) throws Exception {

        if (docs == null) {
            docs = new HashSet<>();
        }

        if (docs.isEmpty()) {
            CharArraySet stopWordArraySet = new CharArraySet(ConfigHelper.loadStopWords(args[3]), true);
            cleanStopWord = new StopWord(stopWordArraySet);
            File docsDir = new File(args[2]);
            for (File f : docsDir.listFiles()) {
                if (FilenameUtils.getExtension(f.getName()).endsWith("txt")) {
                    ReaderFile rf = new ReaderFile(f.getAbsolutePath());
                    cleanStopWord.setDescription(rf.readFile());
                    docs.add(cleanStopWord.execute());
                }
            }
        }

        Configuration conf = new Configuration();
        Job job = new Job(conf, "IDF");

        job.setJarByClass(TFIDF.class);
        job.setJobName("IDF");

        Path inPath = new Path(args[0]);
        Path outPath = new Path(args[1]);

        FileInputFormat.setInputPaths(job, inPath);
        FileOutputFormat.setOutputPath(job, outPath);
        outPath.getFileSystem(conf).delete(outPath, true);

        job.setMapperClass(IDFMapper.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setReducerClass(IDFReducer.class);
        return (job.waitForCompletion(true) ? 0 : 1);
    }

}
