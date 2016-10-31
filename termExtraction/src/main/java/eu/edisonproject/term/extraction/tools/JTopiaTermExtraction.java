/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.term.extraction.tools;


import eu.edisonproject.term.extraction.mappers.JtopiaMapper;
import eu.edisonproject.term.extraction.reducers.TermReducer;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

/**
 *
 * @author S. Koulouzis
 */
public class JTopiaTermExtraction extends Configured implements Tool {

  @Override
  public int run(String[] args) throws Exception {
    Configuration jobconf = getConf();
    
    jobconf.set("tagger.type", args[3]);
    jobconf.set("single.strength", args[4]);
    jobconf.set("no.limit.strength", args[5]);
    

    Job job = new Job(jobconf);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    job.setJarByClass(JTopiaTermExtraction.class);
    job.setMapperClass(JtopiaMapper.class);

    job.setInputFormatClass(TextInputFormat.class);
    job.setOutputFormatClass(TextOutputFormat.class);
    Path inPath = new Path(args[0]);
    FileInputFormat.setInputPaths(job, inPath);

    job.setReducerClass(TermReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    
    Path outPath = new Path(args[1]);
    FileOutputFormat.setOutputPath(job, outPath);

    Path stopwords = new Path(args[2]);
    job.addCacheFile(stopwords.toUri());
    
    
    

    return (job.waitForCompletion(true) ? 0 : 1);

  }

}
