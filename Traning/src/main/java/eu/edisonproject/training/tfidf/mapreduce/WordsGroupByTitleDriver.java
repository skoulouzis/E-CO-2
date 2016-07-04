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
import eu.edisonproject.training.tfidf.avro.Tfidf;
import eu.edisonproject.training.tfidf.avro.TfidfDocument;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.mapred.AvroKey;
import org.apache.avro.mapred.AvroValue;
import org.apache.avro.mapreduce.AvroJob;
import org.apache.avro.mapreduce.AvroKeyValueInputFormat;
import org.apache.avro.mapreduce.AvroKeyValueOutputFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class WordsGroupByTitleDriver {

    
    public static class WordsGroupByTitleMapper extends Mapper<AvroKey<Text>, AvroValue<Tfidf>, Text, Text> {

        public WordsGroupByTitleMapper() {
        }

        protected void map(AvroKey<Text> key, AvroValue<Tfidf> value, Context context) throws IOException, InterruptedException {
            /*
			 * keyValues[0] --> word
			 * keyValues[1] --> date
			 * keyValues[2] --> title/document
			 * 
			 * value --> n/N
             */
            Tfidf tfidfJson = value.datum();
            String documentID = tfidfJson.getDocumentId().toString();
            String word = tfidfJson.getWord().toString();
            String tfidf = tfidfJson.getTfidf().toString();

            context.write(new Text(documentID), new Text(word + "@" + tfidf));

        }
    } // end of mapper class

    public static class WordsGroupByTitleReducer extends Reducer<Text, Text, AvroKey<TfidfDocument>, AvroValue<Text>> {

        public WordsGroupByTitleReducer() {}

        protected void reduce(Text text, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            //The object are grouped for them documentId    
            List<CharSequence> wordToWrite = new LinkedList<>();
            List<CharSequence> valuesToWrite = new LinkedList<>();

            for (Text value : values) {
                String[] line = value.toString().split("@");
                wordToWrite.add(line[0]);
                valuesToWrite.add(line[1]);
            }

            TfidfDocument tfidfDocument = new TfidfDocument();
            tfidfDocument.setDocumentId(text.toString());
            tfidfDocument.setWords(wordToWrite);
            tfidfDocument.setValues(valuesToWrite);
            
            context.write(new AvroKey<TfidfDocument>(tfidfDocument), new AvroValue<Text>(new Text()));

        }
    } // end of reducer class

    public int runWordsGroupByTitleDriver(String[] args) throws Exception {
        //Configuration config = HBaseConfiguration.create();
        Configuration conf = new Configuration();
        Job job = new Job(conf,"WordsGroupByTitleDriver");
        //TableMapReduceUtil.addDependencyJars(job); 
        job.setJarByClass(WordsGroupByTitleDriver.class);
        //This row must be changed
        job.setJobName("Words Group By Title Driver");

        Path inPath = new Path(args[0]);
        Path outPath = new Path(args[1]);

        FileInputFormat.setInputPaths(job, inPath);
        FileOutputFormat.setOutputPath(job, outPath);
        outPath.getFileSystem(conf).delete(outPath, true);

        job.setInputFormatClass(AvroKeyValueInputFormat.class);
        job.setMapperClass(WordsGroupByTitleMapper.class);
        AvroJob.setInputKeySchema(job, Schema.create(Schema.Type.STRING));
        AvroJob.setInputValueSchema(job, Tfidf.getClassSchema());

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        
        job.setOutputFormatClass(AvroKeyValueOutputFormat.class);
        job.setReducerClass(WordsGroupByTitleReducer.class);
        AvroJob.setOutputKeySchema(job, TfidfDocument.SCHEMA$);
        AvroJob.setOutputValueSchema(job, Schema.create(Schema.Type.STRING));

        //TableMapReduceUtil.initTableReducerJob("Distance", CompetencesDistanceReducer.class, job);
        return (job.waitForCompletion(true) ? 0 : 1);
    }

}
