/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.disambiguation.mappers;

import eu.edisonproject.common.Term;
import eu.edisonproject.common.TermFactory;
import eu.edisonproject.disambiguation.DisambiguatorImpl;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 *
 * @author S. Koulouzis
 */
public class CandidatesMapper extends Mapper<LongWritable, Text, Text, Text> {

  private DisambiguatorImpl disImpl;

  @Override
  protected void setup(Mapper.Context context) throws IOException, InterruptedException {
    try {
      Configuration conf = context.getConfiguration();
      String className = conf.get("disambiguator.class");
      Class c = Class.forName(className);
      Object obj = c.newInstance();
      disImpl = (DisambiguatorImpl) obj;

      Properties properties = new Properties();

      properties.put("num.of.terms", conf.get("num.of.terms"));
      properties.put("offset.terms", conf.get("offset.terms"));
      properties.put("minimum.similarity", conf.get("minimum.similarity"));

      if (context.getCacheFiles() != null && context.getCacheFiles().length > 0) {
        URI[] uris = context.getCacheFiles();
        URI stopwords = uris[0];
        URI itemsetfile = uris[1];
        properties.put("itemset.file", itemsetfile.toString());
        properties.put("stop.words.file", stopwords.toString());
      }

      disImpl.configure(properties);
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      Logger.getLogger(CandidatesMapper.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException, MalformedURLException, UnsupportedEncodingException {
    try {
      String[] parts = value.toString().split("\t");
      String term = parts[0];
      Set<Term> candidates = disImpl.getCandidates(term);

//      List<Term> dis = disImpl.disambiguateTerms(candidates);
      if (candidates != null) {
        for (Term t : candidates) {
          JSONObject json = TermFactory.term2Json(t);
          context.write(new Text(term), new Text(json.toJSONString()));
        }
      }
    } catch (ParseException | ExecutionException ex) {
      Logger.getLogger(CandidatesMapper.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

  @Override
  protected void cleanup(Mapper.Context context) throws IOException, InterruptedException {
    super.cleanup(context);
  }

}
