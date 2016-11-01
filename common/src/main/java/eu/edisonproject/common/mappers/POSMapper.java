/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.common.mappers;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import java.io.IOException;
import java.net.URI;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/**
 *
 * @author S. Koulouzis
 */
public class POSMapper extends Mapper<LongWritable, Text, Text, Text> {

  private MaxentTagger tagger;
//  private String wordDelimeter;
  private String[] rejectPOS;

  @Override
  protected void setup(Mapper.Context context) throws IOException, InterruptedException {
    if (context.getCacheFiles() != null && context.getCacheFiles().length > 0) {
      URI[] uris = context.getCacheFiles();
      URI taggerPath = uris[0];

      tagger = new MaxentTagger(taggerPath.toString());
    }
    Configuration conf = context.getConfiguration();
//    wordDelimeter = conf.get("word.delimeter");
    rejectPOS = conf.get("reject.pos").split(",");

  }

  @Override
  public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
    String[] parts = value.toString().split("\t");
    String term = parts[0];
//    String path = parts[1];
    String tagged = tagger.tagString(term);
    boolean reject = false;
    if (tagged != null) {
      for (String pos : rejectPOS) {
        if (tagged.contains(pos)) {
          reject = true;
          break;
        }
      }
    }
    if (!reject) {
      Path filePath = ((FileSplit) context.getInputSplit()).getPath();
      context.write(new Text(term), new Text(filePath.toString()));
    }

  }

  @Override
  protected void cleanup(Mapper.Context context) throws IOException, InterruptedException {
    super.cleanup(context);
  }
}
