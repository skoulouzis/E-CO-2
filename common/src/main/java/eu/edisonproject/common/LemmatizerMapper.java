/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.common;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/**
 *
 * @author Michele Sparamonti, S. Koulouzis
 */
public class LemmatizerMapper extends Mapper<LongWritable, Text, Text, Text> {

  protected StanfordCoreNLP pipeline;

  @Override
  protected void setup(Mapper.Context context) throws IOException, InterruptedException {
    Properties props;
    props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos, lemma");
    this.pipeline = new StanfordCoreNLP(props);
  }

  @Override
  public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
    Annotation document = new Annotation(value.toString());
    // run all Annotators on this text
    this.pipeline.annotate(document);
    StringBuilder sb = new StringBuilder();
    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
    for (CoreMap sentence : sentences) {
      // Iterate over all tokens in a sentence
      for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
        // Retrieve and add the lemma for each word into the
        // list of lemmas
        sb.append(token.get(LemmaAnnotation.class)).append(" ");
      }
    }
    sb.deleteCharAt(sb.length() - 1);
    sb.setLength(sb.length());
    Path filePath = ((FileSplit) context.getInputSplit()).getPath();
    context.write(new Text(filePath.toString()), new Text(sb.toString()));
  }

  @Override
  protected void cleanup(Mapper.Context context) throws IOException, InterruptedException {
    super.cleanup(context);
  }
}
