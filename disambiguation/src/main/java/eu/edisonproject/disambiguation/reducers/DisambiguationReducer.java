/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.disambiguation.reducers;

import eu.edisonproject.common.Term;
import eu.edisonproject.common.TermFactory;
import eu.edisonproject.common.utils.file.CSVFileReader;
import eu.edisonproject.disambiguation.DisambiguatorImpl;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.json.simple.parser.ParseException;

/**
 *
 * @author S. Koulouzis
 */
public class DisambiguationReducer extends Reducer<Text, Text, Text, Text> {

  private DisambiguatorImpl disImpl;
  private String minSim;
  private URI itemsetfile;

  @Override
  protected void setup(Context context) throws IOException, InterruptedException {
    try {
      Configuration conf = context.getConfiguration();
      String className = conf.get("disambiguator.class");
      Class c = Class.forName(className);
      Object obj = c.newInstance();
      disImpl = (DisambiguatorImpl) obj;

      Properties properties = new Properties();

      properties.put("num.of.terms", conf.get("num.of.terms"));
      properties.put("offset.terms", conf.get("offset.terms"));
      minSim = conf.get("minimum.similarity");
      properties.put("minimum.similarity", minSim);

      if (context.getCacheFiles() != null && context.getCacheFiles().length > 0) {
        URI[] uris = context.getCacheFiles();
        URI stopwords = uris[0];
        itemsetfile = uris[1];
        properties.put("itemset.file", itemsetfile.toString());
        properties.put("stop.words.file", stopwords.toString());
      }

      disImpl.configure(properties);
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      Logger.getLogger(DisambiguationReducer.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
    Iterator<Text> iter = values.iterator();
    Set<Term> candidateTerms = new HashSet<>();
    try {
      while (iter.hasNext()) {
        String jsonStr = iter.next().toString();
        candidateTerms.add(TermFactory.create(jsonStr));
      }
      String delimeter = ",";
      String wordSeperator = " ";
      Set<String> ngarms = CSVFileReader.getNGramsForTerm(key.toString(), itemsetfile.toASCIIString(), delimeter, wordSeperator);
      Term dis = disImpl.disambiguate(key.toString(), candidateTerms, ngarms, Double.valueOf(minSim));
      if (dis != null) {
        context.write(key, new Text(TermFactory.term2Json(dis).toJSONString()));
      }
    } catch (ParseException ex) {
      Logger.getLogger(DisambiguationReducer.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

}
