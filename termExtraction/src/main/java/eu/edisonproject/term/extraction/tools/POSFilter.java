/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.term.extraction.tools;

import eu.edisonproject.common.mappers.POSMapper;
import eu.edisonproject.term.extraction.reducers.TermReducer;
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
public class POSFilter extends Configured implements Tool {

  @Override
  public int run(String[] args) throws Exception {
    Configuration jobconf = getConf();

    jobconf.set("reject.pos", args[2]);

    Job job = new Job(jobconf);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    job.setJarByClass(POSFilter.class);
    job.setMapperClass(POSMapper.class);

    job.setInputFormatClass(TextInputFormat.class);
    job.setOutputFormatClass(TextOutputFormat.class);
    Path inPath = new Path(args[0]);
    FileInputFormat.setInputPaths(job, inPath);

    job.setReducerClass(TermReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);

    Path outPath = new Path(args[1]);
    FileOutputFormat.setOutputPath(job, outPath);

    Path modelPath = new Path(args[3]);
    job.addCacheFile(modelPath.toUri());

    return (job.waitForCompletion(true) ? 0 : 1);

  }

}
