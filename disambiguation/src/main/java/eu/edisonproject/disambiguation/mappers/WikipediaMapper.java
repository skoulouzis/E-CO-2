/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.disambiguation.mappers;

import eu.edisonproject.common.Term;
import eu.edisonproject.disambiguation.Wikipedia;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.json.simple.parser.ParseException;

/**
 *
 * @author S. Koulouzis
 */
public class WikipediaMapper extends Mapper<LongWritable, Text, Text, Text> {

  private Wikipedia wiki;

  @Override
  protected void setup(Mapper.Context context) throws IOException, InterruptedException {
    wiki = new Wikipedia();
    wiki.configure(properties);
  }

  @Override
  public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException, MalformedURLException, UnsupportedEncodingException {
    try {
      Set<Term> candidates = wiki.getCandidates(value.toString());
    } catch (ParseException | ExecutionException ex) {
      Logger.getLogger(WikipediaMapper.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

  @Override
  protected void cleanup(Mapper.Context context) throws IOException, InterruptedException {
    super.cleanup(context);
  }

}
