/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.common;

import eu.edisonproject.common.utils.ConfigHelper;
import eu.edisonproject.common.utils.StopWord;
import java.io.IOException;
import java.net.URI;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 *
 * @author S. Koulouzis
 */
public class StopwordsMapper extends Mapper<LongWritable, Text, Text, Text> {

  private static StopWord cleanStopWord;

  @Override
  protected void setup(Mapper.Context context) throws IOException, InterruptedException {
    if (context.getCacheFiles() != null && context.getCacheFiles().length > 0) {
      URI[] uris = context.getCacheFiles();
      URI stopwordFile = uris[0];
      FileSystem fs = FileSystem.get(context.getConfiguration());
      if (cleanStopWord == null) {
        CharArraySet stopWordArraySet = new CharArraySet(ConfigHelper.loadStopWords(fs.open(new Path(stopwordFile)).getWrappedStream()), true);
        cleanStopWord = new StopWord(stopWordArraySet);
      }
    }
  }

  @Override
  public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
    cleanStopWord.setDescription(value.toString().replaceAll("_", " ").trim());
    String cTerm = cleanStopWord.execute();
    while (cTerm.endsWith(" ")) {
      cTerm = cTerm.substring(0, cTerm.lastIndexOf(" "));
    }
    while (cTerm.startsWith(" ")) {
      cTerm = cTerm.substring(cTerm.indexOf(" ") + 1, cTerm.length());
    }
    Path filePath = ((FileSplit) context.getInputSplit()).getPath();
    context.write(new Text(filePath.toString()), new Text(cTerm));
  }

  @Override
  protected void cleanup(Mapper.Context context) throws IOException, InterruptedException {
    super.cleanup(context);
  }
}
