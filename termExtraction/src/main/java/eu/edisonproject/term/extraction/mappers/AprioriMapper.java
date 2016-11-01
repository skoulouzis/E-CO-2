/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.term.extraction.mappers;

import java.io.IOException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 *
 * @author S. Koulouzis
 */
public class AprioriMapper extends Mapper<LongWritable, Text, Text, Text> {

  @Override
  protected void setup(Mapper.Context context) throws IOException {
    String[] args = new String[2];
//            args[0] = fileContents.toString();
//            args[1] = minSup;
//            apriori = new Apriori(args);

  }

  @Override
  public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

  }

}
