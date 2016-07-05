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
import eu.edisonproject.classification.avro.Distances;
import eu.edisonproject.classification.avro.Tfidf;
import eu.edisonproject.classification.avro.TfidfDocument;
import eu.edisonproject.classification.distance.CosineSimilarityMatrix;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.avro.Schema;
import org.apache.avro.mapred.AvroKey;
import org.apache.avro.mapred.AvroValue;
import org.apache.avro.mapreduce.AvroJob;
import org.apache.avro.mapreduce.AvroKeyValueInputFormat;
import org.apache.avro.mapreduce.AvroKeyValueOutputFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class CompetencesDistanceDriver {

    private static List<HashMap<String, Double>> listOfCompetencesVector;

    private void readCompetences(String arg) {
        File fileDir = new File(arg);
        File[] listOfCompetencesFile = fileDir.listFiles();
        listOfCompetencesVector = new LinkedList<>();
        for (File f : listOfCompetencesFile) {
            HashMap<String, Double> competenceFile = new HashMap<>();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f.getAbsolutePath())));
                String line = "";
                while ((line = br.readLine()) != null) {
                    String[] value = line.split(",");
                    competenceFile.put(value[0], Double.parseDouble(value[1]));
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CompetencesDistanceDriver.class.getName()).log(Level.SEVERE, "File Not Found", ex);
            } catch (IOException ex) {
                Logger.getLogger(CompetencesDistanceDriver.class.getName()).log(Level.SEVERE, "IO Exception", ex);
            }
            listOfCompetencesVector.add(competenceFile);

        }
    }

    public static class CompetencesDistanceMapper extends Mapper<AvroKey<Text>, AvroValue<Tfidf>, Text, Text> {

        public CompetencesDistanceMapper() {
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

    public static class CompetencesDistanceReducer extends Reducer<Text, Text, AvroKey<Distances>, AvroValue<Text>> {

        public CompetencesDistanceReducer() {
        }

        @Override
        protected void reduce(Text text, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            //The object are grouped for them documentId
            HashMap<String, Double> documentWords = new HashMap<>();
            List<CharSequence> wordToWrite = new LinkedList<>();
            List<CharSequence> valuesToWrite = new LinkedList<>();

            for (Text value : values) {
                String[] line = value.toString().split("@");
                documentWords.put(line[0], Double.parseDouble(line[1]));
            }

            List<Double> distances = new LinkedList<Double>();
            CosineSimilarityMatrix cosineFunction = new CosineSimilarityMatrix();
            for (HashMap<String, Double> competence : listOfCompetencesVector) {
                HashMap<String, Double> documentToCompetenceSpace = new HashMap<>();
                Set<String> words = competence.keySet();
                for (String word : words) {
                    if (documentWords.containsKey(word)) {
                        documentToCompetenceSpace.put(word, documentWords.get(word));
                    } else {
                        documentToCompetenceSpace.put(word, 0.0);
                    }
                }

                distances.add(cosineFunction.computeDistance(competence.values(), documentToCompetenceSpace.values()));

            }
            String[] docIdAndDate = text.toString().split("@");
            Distances distanceDocument = new Distances();
            distanceDocument.setDocumentId(docIdAndDate[0]);
            distanceDocument.setDate(docIdAndDate[1]);
            distanceDocument.setDistances(distances);

            context.write(new AvroKey<Distances>(distanceDocument), new AvroValue<Text>(new Text()));

        }
        
/*        private static Put resultToPut(ImmutableBytesWritable key, Result result) throws IOException {
  		Put put = new Put(key.get());
 		for (KeyValue kv : result.raw()) {
			put.add(kv);
		}
		return put;
   	}*/
    } // end of reducer class

    public int runWordsGroupByTitleDriver(String[] args) throws Exception {
        Configuration conf = HBaseConfiguration.create();
        //Configuration conf = new Configuration();
        Job job = new Job(conf, "WordsGroupByTitleDriver");
        //TableMapReduceUtil.addDependencyJars(job); 
        job.setJarByClass(CompetencesDistanceDriver.class);
        //This row must be changed
        job.setJobName("Words Group By Title Driver");

        Path inPath = new Path(args[0]);
        Path outPath = new Path(args[1]);

        readCompetences(args[2]);

        FileInputFormat.setInputPaths(job, inPath);
        FileOutputFormat.setOutputPath(job, outPath);
        outPath.getFileSystem(conf).delete(outPath, true);

        job.setInputFormatClass(AvroKeyValueInputFormat.class);
        job.setMapperClass(CompetencesDistanceMapper.class);
        AvroJob.setInputKeySchema(job, Schema.create(Schema.Type.STRING));
        AvroJob.setInputValueSchema(job, Tfidf.getClassSchema());

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setOutputFormatClass(AvroKeyValueOutputFormat.class);
        job.setReducerClass(CompetencesDistanceReducer.class);
        AvroJob.setOutputKeySchema(job, TfidfDocument.SCHEMA$);
        AvroJob.setOutputValueSchema(job, Schema.create(Schema.Type.STRING));

        //TableMapReduceUtil.initTableReducerJob("Distance", CompetencesDistanceReducer.class, job);
        return (job.waitForCompletion(true) ? 0 : 1);
    }

}
