/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.common.reducers;

import java.io.IOException;
import java.util.Iterator;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;

/**
 *
 * @author S. Koulouzis
 */
public class CleanReducer extends Reducer<Text, Text, Text, Text> {

  private MultipleOutputs mos;

  @Override
  protected void setup(Context context) {
    mos = new MultipleOutputs(context);
  }

  @Override
  protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
    System.err.println(key);
    Iterator<Text> iter = values.iterator();
    while (iter.hasNext()) {
      Text text = iter.next();
      String name = FilenameUtils.removeExtension(FilenameUtils.getBaseName(key.toString()));
      mos.write(new Text(), text, name);
    }
  }

  @Override
  protected void cleanup(Context context) throws IOException, InterruptedException {
    mos.close();
  }

}
