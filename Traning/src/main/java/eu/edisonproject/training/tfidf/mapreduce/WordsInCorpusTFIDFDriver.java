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
import eu.edisonproject.training.tfidf.avro.TfidfDocument;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
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
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;

public class WordsInCorpusTFIDFDriver{

    // where to put the data in hdfs when we're done
    private static final String OUTPUT_PATH = ".."+File.separator+"etc"+File.separator+"3-tf-idf";

    // where to read the data from.
    private static final String INPUT_PATH = ".."+File.separator+"etc"+File.separator+"2-word-counts";

    public static class WordsInCorpusTFIDFMapper extends Mapper<AvroKey<Text>, AvroValue<Text>, Text, Text> {

        public WordsInCorpusTFIDFMapper() {
        }

        protected void map(AvroKey<Text> key, AvroValue<Text> value, Context context) throws IOException, InterruptedException {
            /*
			 * keyValues[0] --> word
			 * keyValues[1] --> date
			 * keyValues[2] --> title/document
			 * 
			 * value --> n/N
             */
            String[] keyValues = key.toString().split("@");
            String valueString = value.toString();

            context.write(new Text(keyValues[0]), new Text(keyValues[2] + "=" + valueString + "=" + keyValues[1]));

        }
    } // end of mapper class

//	public static class WordsInCorpusTFIDFReducer extends Reducer<Text, Text, AvroKey<Text>, AvroValue<Tfidf>> {
    public static class WordsInCorpusTFIDFReducer extends Reducer<Text, Text, AvroKey<Text>, AvroValue<TfidfDocument>> {

        private static final DecimalFormat DF = new DecimalFormat("###.########");

        public WordsInCorpusTFIDFReducer() {
        }

        /*
		 * Reducer Input
		 * key --> word
		 * values --> document = n/N = date
         */
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            // get the number of documents indirectly from the file-system (stored in the job name on purpose)
            int count = 0;

            int numberOfDocumentsInCorpus = Integer.parseInt(context.getJobName());
            // total frequency of this word
            int numberOfDocumentsInCorpusWhereKeyAppears = 0;
            Map<String, String> tempFrequencies = new HashMap<String, String>();
            for (Text val : values) {
                String[] documentAndFrequencies = val.toString().split("=");
                numberOfDocumentsInCorpusWhereKeyAppears++;
                tempFrequencies.put(documentAndFrequencies[0] + "@" + documentAndFrequencies[2], documentAndFrequencies[1]);
            }

            String lineValue = "";

            for (String document : tempFrequencies.keySet()) {
                String[] wordFrequenceAndTotalWords = tempFrequencies.get(document).split("/");

                //Term frequency is the quocient of the number of terms in document and the total number of terms in doc
                double tf = Double.valueOf(Double.valueOf(wordFrequenceAndTotalWords[0])
                        / Double.valueOf(wordFrequenceAndTotalWords[1]));

                //interse document frequency quocient between the number of docs in corpus and number of docs the term appears
                double idf = (double) numberOfDocumentsInCorpus / (double) numberOfDocumentsInCorpusWhereKeyAppears;

                //given that log(10) = 0, just consider the term frequency in documents
                double tfIdf = numberOfDocumentsInCorpus == numberOfDocumentsInCorpusWhereKeyAppears
                        ? tf : tf * Math.log10(idf);

                String[] documentFields = document.split("@");

                lineValue += documentFields[0] + ";" + key.toString() + ";" + DF.format(tfIdf) + "\n";

                TfidfDocument tfidfDocument = new TfidfDocument();
                tfidfDocument.setTitle(documentFields[0]);
                tfidfDocument.setPostDate(documentFields[1]);
                tfidfDocument.setWord(key.toString());
                tfidfDocument.setTfidfValue(DF.format(tfIdf));

                System.out.println(tfidfDocument);
                context.write(new AvroKey<Text>(new Text(String.valueOf(count++))), new AvroValue<TfidfDocument>(tfidfDocument));

            }
        }
    } // end of reducer class
    //changed run(String[]) in runWordsInCorpusTFIDFDriver(String[])
    public int runWordsInCorpusTFIDFDriver(String rawArgs) throws Exception {
        Configuration conf = new Configuration();
        Job job = new Job(conf,"WordsInCorpusTFIDFDriver");

        job.setJarByClass(WordsInCorpusTFIDFDriver.class);
        //This row must be changed
        job.setJobName(rawArgs);

        Path inPath = new Path(INPUT_PATH);
        Path outPath = new Path(OUTPUT_PATH);

        FileInputFormat.setInputPaths(job, inPath);
        FileOutputFormat.setOutputPath(job, outPath);
        outPath.getFileSystem(conf).delete(outPath, true);

        job.setInputFormatClass(AvroKeyValueInputFormat.class);
        job.setMapperClass(WordsInCorpusTFIDFMapper.class);
        AvroJob.setInputKeySchema(job, Schema.create(Schema.Type.STRING));
        AvroJob.setInputValueSchema(job, Schema.create(Schema.Type.STRING));

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
       
        job.setOutputFormatClass(AvroKeyValueOutputFormat.class);
        job.setReducerClass(WordsInCorpusTFIDFReducer.class);
        AvroJob.setOutputKeySchema(job, Schema.create(Schema.Type.STRING));
        AvroJob.setOutputValueSchema(job, TfidfDocument.getClassSchema());

        return (job.waitForCompletion(true) ? 0 : 1);
    }

}
