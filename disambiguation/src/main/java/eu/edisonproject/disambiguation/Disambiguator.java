/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.disambiguation;

import eu.edisonproject.common.Term;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.json.simple.parser.ParseException;

/**
 *
 * @author S. Koulouzis
 */
public interface Disambiguator {

  public List<Term> disambiguateTerms(String filterredDictionary) throws IOException, ParseException;

  public void configure(Properties properties);

  public Term getTerm(String term) throws IOException, ParseException;

  public Set<Term> getCandidates(String lemma) throws MalformedURLException, IOException, ParseException, InterruptedException, ExecutionException;
}
