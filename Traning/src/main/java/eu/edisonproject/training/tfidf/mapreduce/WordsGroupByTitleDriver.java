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
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;

public class WordsGroupByTitleDriver extends Configured implements Tool {

    public static class WordsGroupByTitleMapper extends Mapper<LongWritable, Text, Text, Text> {

        public WordsGroupByTitleMapper() {
        }

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            /*
			 * keyValues[0] --> word
			 * keyValues[1] --> date
			 * keyValues[2] --> title/document
			 * 
			 * value --> n/N
             */
            String[] lineKeyValue = value.toString().split("\t");
            String documentID = lineKeyValue[0].split("/")[0];
            String word = lineKeyValue[0].split("/")[1];
            String tfidf = lineKeyValue[1];

            context.write(new Text(documentID), new Text(word + "@" + tfidf));

        }
    } // end of mapper class

    public static class WordsGroupByTitleReducer extends Reducer<Text, Text, Text, Text> {

        public WordsGroupByTitleReducer() {
        }

        @Override
        protected void reduce(Text text, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            //The object are grouped for them documentId    
//            List<CharSequence> wordToWrite = new LinkedList<>();
//            List<CharSequence> valuesToWrite = new LinkedList<>();
            String pairWordValue = "";
            for (Text value : values) {
                String[] line = value.toString().split("@");
                pairWordValue += line[0] + ":" + line[1] + "/";
//                wordToWrite.add(line[0]);
//                valuesToWrite.add(line[1]);
            }
//
//            TfidfDocument tfidfDocument = new TfidfDocument();
//            tfidfDocument.setDocumentId(text.toString());
//            tfidfDocument.setWords(wordToWrite);
//            tfidfDocument.setValues(valuesToWrite);
//          
            context.write(new Text(text.toString()), new Text(pairWordValue));
            //context.write(new AvroKey<TfidfDocument>(tfidfDocument), new AvroValue<Text>(new Text()));

        }
    } // end of reducer class

    @Override
    public int run(String[] args) throws Exception {
        //Configuration config = HBaseConfiguration.create();
        Configuration conf = new Configuration();
        Job job = new Job(conf, "WordsGroupByTitleDriver");
        //TableMapReduceUtil.addDependencyJars(job); 
        job.setJarByClass(WordsGroupByTitleDriver.class);
        //This row must be changed
        job.setJobName("Words Group By Title Driver");

        Path inPath = new Path(args[0]);
        Path outPath = new Path(args[1]);
        FileSystem fs = FileSystem.get(conf);
        fs.delete(outPath, true);

        FileInputFormat.setInputPaths(job, inPath);
        FileOutputFormat.setOutputPath(job, outPath);
        outPath.getFileSystem(conf).delete(outPath, true);

        job.setMapperClass(WordsGroupByTitleMapper.class);
        job.setMapperClass(WordsInCorpusTFIDFDriver.WordsInCorpusTFIDFMapper.class);
//        job.setInputFormatClass(NLineInputFormat.class);
//        NLineInputFormat.addInputPath(job, inPath);
//        NLineInputFormat.setNumLinesPerSplit(job, Integer.valueOf(args[2]));
//        NLineInputFormat.setMaxInputSplitSize(job, 2000);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setReducerClass(WordsGroupByTitleReducer.class);
//        job.setOutputFormatClass(AvroKeyValueOutputFormat.class);
//        job.setReducerClass(WordsGroupByTitleReducer.class);
//        AvroJob.setOutputKeySchema(job, TfidfDocument.SCHEMA$);
//        AvroJob.setOutputValueSchema(job, Schema.create(Schema.Type.STRING));

        return (job.waitForCompletion(true) ? 0 : 1);
    }

}
