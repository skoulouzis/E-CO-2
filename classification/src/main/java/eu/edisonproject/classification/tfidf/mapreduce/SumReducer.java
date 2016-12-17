/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.classification.tfidf.mapreduce;

import java.io.IOException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class SumReducer extends Reducer<Text, IntWritable, Text, Integer> {

  @Override
  public void reduce(Text key, Iterable<IntWritable> values, Context context)
          throws IOException, InterruptedException {
      Integer sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      context.write(key, sum);
  }
}
