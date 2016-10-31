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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 *
 * @author Michele Sparamonti, S. Koulouzis
 */
public class StemmerMapper extends Mapper<LongWritable, Text, Text, Text> {

  private StandardTokenizer tokenizer;
  private CharTermAttribute charTermAttribute;
  private StandardFilter standardFilter;

  @Override
  protected void setup(Mapper.Context context) throws IOException, InterruptedException {
    tokenizer = new StandardTokenizer();
    charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);
    standardFilter = new StandardFilter(tokenizer);
  }

  @Override
  public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
    tokenizer.setReader(new InputStreamReader(new ByteArrayInputStream(value.toString().getBytes(StandardCharsets.UTF_8))));;

    StringBuilder sb = new StringBuilder();
    try (PorterStemFilter porterStemmingFilter = new PorterStemFilter(standardFilter)) {
      porterStemmingFilter.reset();

      while (porterStemmingFilter.incrementToken()) {
        final String token = charTermAttribute.toString();
        sb.append(token).append(" ");
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
