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
import java.util.Iterator;
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

    // private static List<HashMap<String, Double>> listOfCompetencesVector;
    private static HashMap<String, HashMap<String, Double>> CATEGORIES_LIST;

//    private static String[] data_analytics = {"predictive analytics", "statistical techniques",
//        "analytics for decision making", "data blending", "big data analytics platform"};
//    private static String[] data_management_curation = {"data management plan", "develop data models",
//        "data collection and integration", "data visualization", "repository of analysis history"};
//    private static String[] data_science_engineering = {"engineering principles", "big data computational solutions",
//        "analysis tools for decision making", "relational and non-relational databases", "security service management",
//        "agile development"};
//    private static String[] scientific_research_methods = {"systematic study", "devise new applications",
//        "develop innovative ideas", "strategies into action plans", "contribute research objectives"};
//    private static String[] domain_knowledge = {"business process", "improve existing services",
//        "participate financial decisions", "analytic support to other organisation", "analyse data for marketing",
//        "analyse customer data"};
    public static final TableName JOB_POST_COMETENCE_TBL_NAME = TableName.valueOf("categories");
    private static final Logger LOGGER = Logger.getLogger(CompetencesDistanceDriver.class.getName());

    private void readCompetences(String arg) {
        File fileDir = new File(arg);
        File[] listOfCompetencesFile = fileDir.listFiles();
        //listOfCompetencesVector = new LinkedList<>();
        CATEGORIES_LIST = new HashMap<>();
        for (File f : listOfCompetencesFile) {
            HashMap<String, Double> categoriesFile = new HashMap<>();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f.getAbsolutePath())));
                String line = "";
                while ((line = br.readLine()) != null) {
                    String[] value = line.split(",");
                    categoriesFile.put(value[0], Double.parseDouble(value[1]));
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CompetencesDistanceDriver.class.getName()).log(Level.SEVERE, "File Not Found", ex);
            } catch (IOException ex) {
                Logger.getLogger(CompetencesDistanceDriver.class.getName()).log(Level.SEVERE, "IO Exception", ex);
            }
            //   listOfCompetencesVector.add(categoriesFile);
            CATEGORIES_LIST.put(f.getName().replace(".csv", ""), categoriesFile);

        }
    }

    public static class CompetencesDistanceMapper extends Mapper<LongWritable, Text, Text, Text> {

        public CompetencesDistanceMapper() {
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
            HashMap<String, Double> distancesNameAndValue = new HashMap<>();
            HashMap<String, Double> documentWords = new HashMap<>();
//            List<CharSequence> wordToWrite = new LinkedList<>();
//            List<CharSequence> valuesToWrite = new LinkedList<>();

            for (Text value : values) {
                String[] line = value.toString().split("@");
                documentWords.put(line[0], Double.parseDouble(line[1].replace(",", ".")));
            }

            //  List<Double> distances = new LinkedList<Double>();
            CosineSimilarityMatrix cosineFunction = new CosineSimilarityMatrix();

            //for (HashMap<String, Double> competence : listOfCompetencesVector) {
            Set<String> names = CATEGORIES_LIST.keySet();
            Iterator<String> iter = names.iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                HashMap<String, Double> competence = CATEGORIES_LIST.get(key);
                HashMap<String, Double> documentToCompetenceSpace = new HashMap<>();

                //Change to the common sub space
                Set<String> words = competence.keySet();
                List<Double> competenceValue = new LinkedList<Double>();
                List<Double> documentValue = new LinkedList<Double>();
                for (String word : words) {
                    //Align the term written in the csv with the term analysed by MR
                    //The terms comosed by two or more words in MR are separeted by whitespace
                    //Instead the terms into the csv file are separeteb by "_" char
                    String originalWord = word;
                    if (word.contains("_")) {
                        word = word.replaceAll("_", " ");
                    }
                    if (documentWords.containsKey(word)) {
                        competenceValue.add(competence.get(originalWord));
                        documentValue.add(documentWords.get(word));
                        //documentToCompetenceSpace.put(word, documentWords.get(word));
                    }// else {
                    // documentToCompetenceSpace.put(word, 0.0);
                    // }

                    if (competenceValue.size() != 0) {
                        distancesNameAndValue.put(key, cosineFunction.computeDistance(competence.values(), documentToCompetenceSpace.values()));
                        System.out.println(key+"--"+distancesNameAndValue.get(key));
                    } else {
                        distancesNameAndValue.put(key, 0.0);
                    }
                    //distances.add(cosineFunction.computeDistance(competence.values(), documentToCompetenceSpace.values()));

                }
            }
            LOGGER.log(Level.INFO, "Distance{0}", text);
            String[] docIdAndDate = text.toString().split("@");
            List<String> families = new ArrayList<>();
            families.add("info");

            for (String family : distancesNameAndValue.keySet()) {
                String columnFamily = family.split("-")[0];
                boolean isPresent = false;
                for (String fam : families) {
                    if (fam.equals(columnFamily)) {
                        isPresent = true;
                    }
                }
                if (!isPresent) {
                    families.add(columnFamily);
                }
            }

            DBTools.createOrUpdateTable(JOB_POST_COMETENCE_TBL_NAME, families, true);
            try (Admin admin = DBTools.getConn().getAdmin()) {
                try (Table tbl = DBTools.getConn().getTable(JOB_POST_COMETENCE_TBL_NAME)) {
                    Put put = new Put(Bytes.toBytes(docIdAndDate[0]));
                    // put.addColumn(Bytes.toBytes("details"), Bytes.toBytes("total"), Bytes.toBytes(sum));
                    // column family info
                    put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("title"), Bytes.toBytes(docIdAndDate[1]));
                    put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("date"), Bytes.toBytes(docIdAndDate[2]));

                    for (String family : distancesNameAndValue.keySet()) {
                        //String key = family; //iterColumn.next();
                        Double d = distancesNameAndValue.get(family);
                        String columnFamily = family.split("-")[0];
                        String columnQualifier = family.split("-")[1];
                        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(columnQualifier), Bytes.toBytes(d));
                    }
                    tbl.put(put);
                    LOGGER.log(Level.INFO, "{0}...... {1}", new Object[]{docIdAndDate[0], docIdAndDate[1]});
                    context.write(new ImmutableBytesWritable(docIdAndDate[0].getBytes()), put);
                    //context.write(new Text(text), new Text(distancesString));
                }
                admin.flush(JOB_POST_COMETENCE_TBL_NAME);
            } catch (Exception ex) {
                Logger.getLogger(CompetencesDistanceDriver.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

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

        return (job.waitForCompletion(true) ? 0 : 1);

    }

}
