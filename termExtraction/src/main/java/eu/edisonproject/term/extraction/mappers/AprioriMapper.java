/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.term.extraction.mappers;

import eu.edisonproject.term.extraction.Apriori;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/**
 *
 * @author S. Koulouzis
 */
public class AprioriMapper extends Mapper<LongWritable, Text, Text, Text> {

  private Apriori apriori;

  @Override
  protected void setup(Mapper.Context context) throws IOException {
    try {
      apriori = new Apriori();
      Configuration conf = context.getConfiguration();
//    wordDelimeter = conf.get("word.delimeter");
      String minSup = conf.get("minimum.support");
      apriori.setMinimumSupport(Double.valueOf(minSup));
      String ruleLen = conf.get("rule.len");
      apriori.setMaxRuleLen(Integer.valueOf(ruleLen));
    } catch (Exception ex) {
      Logger.getLogger(AprioriMapper.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

  @Override
  public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
    try {
      StringTokenizer tokenizer = new StringTokenizer(value.toString(), "\t\n\r\f,.:;?![]'");
      StringBuilder sentences = new StringBuilder();
      while (tokenizer.hasMoreElements()) {
        Object nextElement = tokenizer.nextElement();
        sentences.append(nextElement).append("\n");
      }
      if (sentences.length() > 0) {
        sentences.deleteCharAt(sentences.length() - 1);
        sentences.setLength(sentences.length());
      } else {
        tokenizer = new StringTokenizer(value.toString(), " ");
        while (tokenizer.hasMoreElements()) {
          Object nextElement = tokenizer.nextElement();
          sentences.append(nextElement).append("\n");
        }
        sentences.deleteCharAt(sentences.length() - 1);
        sentences.setLength(sentences.length());
      }

      apriori.setText(sentences.toString());
      List<String> terms = apriori.go();
      for (String t : terms) {
        String text = t.split("/")[0].replaceAll(" ", "_");
        Path filePath = ((FileSplit) context.getInputSplit()).getPath();
        context.write(new Text(text), new Text(filePath.toString()));
      }
    } catch (Exception ex) {
      Logger.getLogger(AprioriMapper.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

}
