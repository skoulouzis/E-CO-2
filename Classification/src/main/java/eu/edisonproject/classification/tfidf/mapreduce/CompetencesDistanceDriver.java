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
import distances.avro.Distances;
import eu.edisonproject.classification.distance.CosineSimilarityMatrix;
import eu.edisonproject.utility.file.DBTools;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;

public class CompetencesDistanceDriver extends Configured implements Tool {

    private static List<HashMap<String, Double>> listOfCompetencesVector;

    private static String[] data_analytics = {"predictive analytics", "statistical techniques",
        "analytics for decision making", "data blending", "big data analytics platform"};
    private static String[] data_management_curation = {"data management plan", "develop data models",
        "data collection and integration", "data visualization", "repository of analysis history"};
    private static String[] data_science_engineering = {"engineering principles", "big data computational solutions",
        "analysis tools for decision making", "relational and non-relational databases", "security service management",
        "agile development"};
    private static String[] scientific_research_methods = {"systematic study", "devise new applications",
        "develop innovative ideas", "strategies into action plans", "contribute research objectives"};
    private static String[] domain_knowledge = {"business process", "improve existing services",
        "participate financial decisions", "analytic support to other organisation", "analyse data for marketing",
        "analyse customer data"};

    public static final TableName JOB_POST_COMETENCE_TBL_NAME = TableName.valueOf("jobpostcompetence");

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
                    String[] value = line.split(";");
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

    public static class CompetencesDistanceMapper extends Mapper<LongWritable, Text, Text, Text> {

        public CompetencesDistanceMapper() {
        }

        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            /*
			 * keyValues[0] --> word
			 * keyValues[1] --> date
			 * keyValues[2] --> title/document
			 * 
			 * value --> n/N
             */
            String[] keyValues = value.toString().split("\t");
            String documentID = keyValues[0];
            String word = keyValues[1].split("/")[0];
            String tfidf = keyValues[1].split("/")[1];

            context.write(new Text(documentID), new Text(word + "@" + tfidf));

        }
    } // end of mapper class

//    public static class CompetencesDistanceReducer extends TableReducer<Text, Text, ImmutableBytesWritable> {
    public static class CompetencesDistanceReducer extends Reducer<Text, Text, ImmutableBytesWritable, Put> {

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
                documentWords.put(line[0], Double.parseDouble(line[1].replace(",", ".")));
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
            System.out.println("Distance" + text);
            String[] docIdAndDate = text.toString().split("@");
            Distances distanceDocument = new Distances();
            distanceDocument.setTitle(docIdAndDate[1]);
            distanceDocument.setDocumentId(docIdAndDate[0]);
            distanceDocument.setDate(docIdAndDate[2]);
            distanceDocument.setDistanceArray(distances);

//            String distancesString = "";
//            for (Double d : distances) {
//                distancesString += d + ";";
//            }
//            Put put = new Put(key.get());
            List<String> families = new ArrayList<>();
            families.add("info");
            families.add("data_analytics");
            families.add("data_management_curation");
            families.add("data_science_engineering");
            families.add("scintific_research_methods");
            families.add("domain_knowledge");
            DBTools.createTable(JOB_POST_COMETENCE_TBL_NAME, families);
            try (Admin admin = DBTools.getConn().getAdmin()) {
                try (Table tbl = DBTools.getConn().getTable(JOB_POST_COMETENCE_TBL_NAME)) {
                    Put put = new Put(Bytes.toBytes(docIdAndDate[0]));
                    // put.addColumn(Bytes.toBytes("details"), Bytes.toBytes("total"), Bytes.toBytes(sum));
                    // column family info
                    put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("title"), Bytes.toBytes(docIdAndDate[1]));
                    put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("date"), Bytes.toBytes(docIdAndDate[2]));
                    // column family distance
                    int count = 0;
                    for (String s : data_analytics) {
                        put.addColumn(Bytes.toBytes("data_analytics"), Bytes.toBytes(s), Bytes.toBytes(distances.get(count)));
                        count++;
                    }
                    for (String s : data_management_curation) {
                        put.addColumn(Bytes.toBytes("data_management_curation"), Bytes.toBytes(s), Bytes.toBytes(distances.get(count)));
                        count++;
                    }
                    for (String s : data_science_engineering) {
                        put.addColumn(Bytes.toBytes("data_science_engineering"), Bytes.toBytes(s), Bytes.toBytes(distances.get(count)));
                        count++;
                    }
                    for (String s : scientific_research_methods) {
                        put.addColumn(Bytes.toBytes("scintific_research_methods"), Bytes.toBytes(s), Bytes.toBytes(distances.get(count)));
                        count++;
                    }
                    for (String s : domain_knowledge) {
                        put.addColumn(Bytes.toBytes("domain_knowledge"), Bytes.toBytes(s), Bytes.toBytes(distances.get(count)));
                        count++;
                    }
                    tbl.put(put);
                    System.out.println(docIdAndDate[0] + "...... " + docIdAndDate[1]);
                    context.write(new ImmutableBytesWritable(docIdAndDate[0].getBytes()), put);
                    //context.write(new Text(text), new Text(distancesString));
                }
                admin.flush(JOB_POST_COMETENCE_TBL_NAME);
            } catch (Exception ex) {
                Logger.getLogger(CompetencesDistanceDriver.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        /*        private static Put resultToPut(ImmutableBytesWritable key, Result result) throws IOException {
  		Put put = new Put(key.get());
 		for (KeyValue kv : result.raw()) {
			put.addColumn(kv);
		}
		return put;
   	}*/
    } // end of reducer class

    public int run(String[] args) throws Exception {
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

        job.setMapperClass(CompetencesDistanceMapper.class);
//        job.setInputFormatClass(AvroKeyValueInputFormat.class);
//        job.setMapperClass(CompetencesDistanceMapper.class);
//        AvroJob.setInputKeySchema(job, Schema.create(Schema.Type.STRING));
//        AvroJob.setInputValueSchema(job, Tfidf.getClassSchema());

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setReducerClass(CompetencesDistanceReducer.class);
        job.setOutputFormatClass(TableOutputFormat.class);
        job.getConfiguration().set(TableOutputFormat.OUTPUT_TABLE, "jobpostcompetence");
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        //additional output using TextOutputFormat.
        MultipleOutputs.addNamedOutput(job, "text", TextOutputFormat.class,
                Text.class, Text.class);

//        job.setOutputKeyClass(Text.class);
//        job.setOutputValueClass(Text.class);
//        job.setReducerClass(CompetencesDistanceReducer.class);
        //  TableMapReduceUtil.initTableReducerJob("summary_user", CompetencesDistanceReducer.class, job);
        //TableMapReduceUtil.initTableReducerJob("Distance", CompetencesDistanceReducer.class, job);
        return (job.waitForCompletion(true) ? 0 : 1);

//        HBaseConfiguration conf = HBaseConfiguration.create();
//        Job job = new Job(conf, "JOB_NAME");
//        job.setJarByClass(yourclass.class);
//        job.setMapperClass(yourMapper.class);
//        job.setMapOutputKeyClass(Text.class);
//        job.setMapOutputValueClass(Intwritable.class);
//        FileInputFormat.setInputPaths(job, new Path(inputPath));
//        TableMapReduceUtil.initTableReducerJob(TABLE,
//                yourReducer.class, job);
//        job.setReducerClass(yourReducer.class);
//        job.waitForCompletion(true);
//class yourReducer
//        extends
//        TableReducer<Text, IntWritable, 
//        ImmutableBytesWritable>
//{
////@override reduce()
//}
    }

}
