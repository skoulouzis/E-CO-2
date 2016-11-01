/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.disambiguation.tools;

import eu.edisonproject.common.reducers.GenericReducer;
import eu.edisonproject.disambiguation.mappers.CandidatesMapper;
import eu.edisonproject.disambiguation.reducers.DisambiguationReducer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

/**
 *
 * @author S. Koulouzis
 */
public class Disambiguation extends Configured implements Tool {

  @Override
  public int run(String[] args) throws Exception {
    Configuration jobconf = getConf();

    jobconf.set("num.of.terms", args[2]);
    jobconf.set("offset.terms", args[3]);
    jobconf.set("minimum.similarity", args[4]);
    jobconf.set("disambiguator.class", args[5]);

    Job job = new Job(jobconf);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    job.setJarByClass(Disambiguation.class);
    job.setMapperClass(CandidatesMapper.class);

    job.setInputFormatClass(TextInputFormat.class);
    job.setOutputFormatClass(TextOutputFormat.class);
    Path inPath = new Path(args[0]);
    FileInputFormat.setInputPaths(job, inPath);

    job.setReducerClass(DisambiguationReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);

    Path outPath = new Path(args[1]);
    FileOutputFormat.setOutputPath(job, outPath);

    Path stopwords = new Path(args[6]);
    job.addCacheFile(stopwords.toUri());

    Path itemsetFile = new Path(args[7]);
    job.addCacheFile(itemsetFile.toUri());

    return (job.waitForCompletion(true) ? 0 : 1);

  }

}
