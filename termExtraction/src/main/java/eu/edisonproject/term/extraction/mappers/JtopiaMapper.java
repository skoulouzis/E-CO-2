/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.term.extraction.mappers;

import com.sree.textbytes.jtopia.TermDocument;
import com.sree.textbytes.jtopia.TermsExtractor;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Set;
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
public class JtopiaMapper extends Mapper<LongWritable, Text, Text, Text> {

  private TermsExtractor termExtractor;
  private TermDocument topiaDoc;

  @Override
  protected void setup(Mapper.Context context) throws IOException {
    Configuration conf = context.getConfiguration();
    String taggerType = conf.get("tagger.type");
    String singleStrength = conf.get("single.strength");
    String noLimitStrength = conf.get("no.limit.strength");

    if (context.getCacheFiles() != null && context.getCacheFiles().length > 0) {
      URI[] uris = context.getCacheFiles();
      URI modelPath = uris[0];
//      FileSystem fs = FileSystem.get(context.getConfiguration());

      switch (taggerType) {
        case "stanford":
          com.sree.textbytes.jtopia.Configuration.setModelFileLocation(modelPath + File.separator
                  + "stanford" + File.separator + "english-left3words-distsim.tagger");
          com.sree.textbytes.jtopia.Configuration.setTaggerType("stanford");
          break;
        case "openNLP":
          com.sree.textbytes.jtopia.Configuration.setModelFileLocation(modelPath + File.separator
                  + "openNLP" + File.separator + "en-pos-maxent.bin");
          com.sree.textbytes.jtopia.Configuration.setTaggerType("openNLP");
          break;
        case "default":
          com.sree.textbytes.jtopia.Configuration.setModelFileLocation(modelPath + File.separator
                  + "default" + File.separator + "english-lexicon.txt");
          com.sree.textbytes.jtopia.Configuration.setTaggerType("default");
          break;
      }

    }
    com.sree.textbytes.jtopia.Configuration.setSingleStrength(Integer.valueOf(singleStrength));
    com.sree.textbytes.jtopia.Configuration.setNoLimitStrength(Integer.valueOf(noLimitStrength));
    termExtractor = new TermsExtractor();
    topiaDoc = new TermDocument();
  }

  @Override
  public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
    topiaDoc = termExtractor.extractTerms(value.toString());
    Set<String> terms = topiaDoc.getFinalFilteredTerms().keySet();
    for (String t : terms) {
      String text = t.replaceAll(" ", "_").toLowerCase();
      Path filePath = ((FileSplit) context.getInputSplit()).getPath();
      context.write(new Text(text), new Text(filePath.toString()));
    }
  }

}
