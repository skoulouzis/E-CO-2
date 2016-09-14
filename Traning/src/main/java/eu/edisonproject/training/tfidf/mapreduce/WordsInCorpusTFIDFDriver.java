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
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;

public class WordsInCorpusTFIDFDriver extends Configured implements Tool{

    // where to put the data in hdfs when we're done
  //  private static final String OUTPUT_PATH = ".."+File.separator+"etc"+File.separator+"3-tf-idf";

    // where to read the data from.
//    private static final String INPUT_PATH = ".."+File.separator+"etc"+File.separator+"2-word-counts";

    public static class WordsInCorpusTFIDFMapper extends Mapper<LongWritable, Text, Text, Text> {

        public WordsInCorpusTFIDFMapper() {
        }

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            /*
			 * keyValues[0] --> word
			 * keyValues[1] --> title/document
			 * 
			 * value --> n/N
             */
            String[] line = value.toString().split("\t");
            String[] keyValues = line[0].split("@");
            String valueString = line[1];

            context.write(new Text(keyValues[0]), new Text(keyValues[1] + "=" + valueString));

        }
    } // end of mapper class

//	public static class WordsInCorpusTFIDFReducer extends Reducer<Text, Text, AvroKey<Text>, AvroValue<Tfidf>> {
    public static class WordsInCorpusTFIDFReducer extends Reducer<Text, Text, Text, Text> {

        private static final DecimalFormat DF = new DecimalFormat("###.########");

        public WordsInCorpusTFIDFReducer() {
        }

        /*
		 * Reducer Input
		 * key --> word
		 * values --> document = n/N
         */
        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            // get the number of documents indirectly from the file-system (stored in the job name on purpose)
            int count = 0;

            int numberOfDocumentsInCorpus = Integer.parseInt(context.getJobName());
            // total frequency of this word
            int numberOfDocumentsInCorpusWhereKeyAppears = 0;
            Map<String, String> tempFrequencies = new HashMap<>();
            for (Text val : values) {
                String[] documentAndFrequencies = val.toString().split("=");
                numberOfDocumentsInCorpusWhereKeyAppears++;
                tempFrequencies.put(documentAndFrequencies[0], documentAndFrequencies[1]);
            }

            String lineValue = "";

            for (String document : tempFrequencies.keySet()) {
                String[] wordFrequenceAndTotalWords = tempFrequencies.get(document).split("/");

                //Term frequency is the quocient of the number of terms in document and the total number of terms in doc
                double tf = Double.valueOf(wordFrequenceAndTotalWords[0])
                        / Double.valueOf(wordFrequenceAndTotalWords[1]);
              //  System.out.println("TF "+tf);
                //interse document frequency quocient between the number of docs in corpus and number of docs the term appears
                double idf = (double) numberOfDocumentsInCorpus / (double) numberOfDocumentsInCorpusWhereKeyAppears;
              //  System.out.println("IDF"+idf);
                //given that log(10) = 0, just consider the term frequency in documents
               double tfIdf = numberOfDocumentsInCorpus == numberOfDocumentsInCorpusWhereKeyAppears
                        ? tf : tf * Math.log10(idf);
               String[] documentFields = document.split("@");
              //  System.out.println(tfIdf);
                lineValue += documentFields[0] + ";" + key.toString() + ";" + DF.format(tfIdf) + "\n";
//
//                Tfidf tfidfJson = new Tfidf();
//                tfidfJson.setDocumentId(documentFields[0]);
//                tfidfJson.setWord(key.toString());
//                tfidfJson.setTfidf(DF.format(tfIdf));

                String newKey = documentFields[0]+"/"+key.toString();
                String newValue = DF.format(tfIdf);
//                context.write(new AvroKey<Text>(new Text(String.valueOf(count++))), new AvroValue<Tfidf>(tfidfJson));
                context.write(new Text(newKey),new Text(newValue));
            }
        }
    } // end of reducer class
    //changed run(String[]) in runWordsInCorpusTFIDFDriver(String[])
    @Override
    public int run(String[] rawArgs) throws Exception {
        Configuration conf = new Configuration();
        Job job = new Job(conf,"WordsInCorpusTFIDFDriver");

        job.setJarByClass(WordsInCorpusTFIDFDriver.class);
        //This row must be changed
        job.setJobName(rawArgs[2]);

        Path inPath = new Path(rawArgs[0]);
        Path outPath = new Path(rawArgs[1]);

        FileInputFormat.setInputPaths(job, inPath);
        FileOutputFormat.setOutputPath(job, outPath);
        outPath.getFileSystem(conf).delete(outPath, true);

        job.setMapperClass(WordsInCorpusTFIDFMapper.class);
        job.setInputFormatClass(NLineInputFormat.class);
        NLineInputFormat.addInputPath(job, inPath);
        NLineInputFormat.setNumLinesPerSplit(job, Integer.valueOf(rawArgs[3]));
         NLineInputFormat.setMaxInputSplitSize(job, 2000);
        
//        job.setInputFormatClass(AvroKeyValueInputFormat.class);
//        job.setMapperClass(WordsInCorpusTFIDFMapper.class);
//        AvroJob.setInputKeySchema(job, Schema.create(Schema.Type.STRING));
//        AvroJob.setInputValueSchema(job, Schema.create(Schema.Type.STRING));       
//        
//        job.setOutputFormatClass(AvroKeyValueOutputFormat.class);
//        job.setReducerClass(WordsInCorpusTFIDFReducer.class);
//        AvroJob.setOutputKeySchema(job, Schema.create(Schema.Type.STRING));
//        AvroJob.setOutputValueSchema(job, Tfidf.getClassSchema());

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setReducerClass(WordsInCorpusTFIDFReducer.class);

        return (job.waitForCompletion(true) ? 0 : 1);
    }

}
