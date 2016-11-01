/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.disambiguation;

import eu.edisonproject.common.Term;
import eu.edisonproject.common.TermFactory;
import eu.edisonproject.common.utils.file.CSVFileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.hbase.TableName;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

/**
 *
 * @author S. Koulouzis
 */
public class WikipediaOnline extends Wikipedia {

  public static final TableName WIKIPEDIA_TERMS_TBL_NAME = TableName.valueOf("wikipedia_terms");
  private String prevTitles = "";
  private static final Logger LOGGER = Logger.getLogger(WikipediaOnline.class.getName());
  private static int numOfRecursion = 0;
  private static final int recursionLimit = 1;

  @Override
  public void configure(Properties properties) {
    super.configure(properties);

    Level level = Level.parse(properties.getProperty("log.level", "INFO"));
    Handler[] handlers
            = Logger.getLogger("").getHandlers();
    for (int index = 0; index < handlers.length; index++) {
      handlers[index].setLevel(level);
    }
    LOGGER.setLevel(level);
  }

  @Override
  public Term getTerm(String term) throws IOException, ParseException, UnsupportedEncodingException, FileNotFoundException {
    Term dis = null;
    if (dis == null) {
      Set<Term> possibleTerms = null;
      try {
        possibleTerms = getCandidates(term);
      } catch (InterruptedException | ExecutionException | IOException | ParseException ex) {
        LOGGER.log(Level.SEVERE, null, ex);
      }
      String delimeter = ",";
      String wordSeperator = " ";
      Set<String> ngarms = CSVFileReader.getNGramsForTerm(term, getItemsFilePath(), delimeter, wordSeperator);
      dis = super.disambiguate(term, possibleTerms, ngarms, getMinimumSimilarity());
      if (dis == null) {
        LOGGER.log(Level.INFO, "Couldn''''t figure out what ''{0}'' means", term);
      } else {
        LOGGER.log(Level.INFO, "Term: {0}. Confidence: {1} URL: {2}", new Object[]{dis, dis.getConfidence(), dis.getUrl()});
      }
      return dis;
    }
    return null;
  }

  @Override
  public Set<Term> getCandidates(String lemma) throws MalformedURLException, IOException, ParseException, UnsupportedEncodingException, InterruptedException, ExecutionException {

    Set<String> jsonTerms = getPossibleTermsFromDB(lemma, new URL(PAGE).getHost());

    if (jsonTerms != null && !jsonTerms.isEmpty()) {
      Set<Term> terms = TermFactory.create(jsonTerms);
      return terms;
//            Set<Term> wikiTerms = new HashSet<>();

//            for (Term t : terms) {
//                if (t.getUrl().toString().contains(new URL(PAGE).getHost())) {
//                    wikiTerms.add(t);
//                }
//            }
//            if (!wikiTerms.isEmpty()) {
//                return wikiTerms;
//            }
    }

    URL url;
    String jsonString;

    Set<String> titlesList = getTitles(lemma);

    StringBuilder titles = new StringBuilder();

    Iterator<String> it = titlesList.iterator();
    int i = 0;
    Set<Term> terms = new HashSet<>();
    while (it.hasNext()) {
      String t = it.next();
      t = URLEncoder.encode(t, "UTF-8");
      titles.append(t).append("|");
      if ((i % 20 == 0 && i > 0) || i >= titlesList.size() - 1) {
        try {
          titles.deleteCharAt(titles.length() - 1);
          titles.setLength(titles.length());
          jsonString = null;
          if (jsonString == null) {
            url = new URL(PAGE + "?format=json&redirects&action=query&prop=extracts&exlimit=max&explaintext&exintro&titles=" + titles.toString());
            LOGGER.log(Level.FINE, url.toString());
            jsonString = IOUtils.toString(url);
            titles = new StringBuilder();
          }
          Set<Term> qt = queryTerms(jsonString, lemma);
          if (qt != null) {
            terms.addAll(qt);
          }
        } catch (Exception ex) {
          Logger.getLogger(WikipediaOnline.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      i++;
    }
    addPossibleTermsToDB(lemma, terms);
    numOfRecursion = 0;
    return terms;
  }

  private Set<Term> queryTerms(String jsonString, String originalTerm) throws ParseException, IOException, MalformedURLException, InterruptedException, ExecutionException, Exception {
    if (numOfRecursion <= recursionLimit) {
      Set<Term> terms = new HashSet<>();
      Set<Term> termsToReturn = new HashSet<>();
      JSONObject jsonObj = (JSONObject) JSONValue.parseWithException(jsonString);
      JSONObject query = (JSONObject) jsonObj.get("query");
      JSONObject pages = (JSONObject) query.get("pages");
      Set<String> keys = pages.keySet();
      for (String key : keys) {
        JSONObject jsonpage = (JSONObject) pages.get(key);
        Term t = TermFactory.create(jsonpage, originalTerm);
        if (t != null) {
          terms.add(t);
        }
      }
      if (terms.size() > 0) {
        Map<CharSequence, List<CharSequence>> cats = getCategories(terms);
        for (Term t : terms) {
          boolean add = true;
          List<CharSequence> cat = cats.get(t.getUid());
          t.setCategories(cat);
          for (CharSequence g : t.getGlosses()) {
            if (g != null && g.toString().contains("may refer to:") && numOfRecursion <= recursionLimit) {
              Set<Term> referToTerms = getReferToTerms(g.toString(), originalTerm);
              if (referToTerms != null) {
                for (Term rt : referToTerms) {
                  String url = "https://en.wikipedia.org/?curid=" + rt.getUid();
                  rt.setUrl(url);
                  termsToReturn.add(rt);
                }
              }
              add = false;
              break;
            }
          }
          if (add) {
            String url = "https://en.wikipedia.org/?curid=" + t.getUid();
            t.setUrl(url);
            termsToReturn.add(t);
          }
        }
      }
      return termsToReturn;
    }
    return null;
  }

  private Map<CharSequence, List<CharSequence>> getCategories(Set<Term> terms) throws MalformedURLException, InterruptedException, ExecutionException, Exception {
    Map<CharSequence, List<CharSequence>> cats = new HashMap<>();
    for (Term t : terms) {
      URL url = new URL(PAGE + "?action=query&format=json&prop=categories&pageids=" + t.getUid());
      LOGGER.log(Level.FINE, url.toString());
      WikiRequestor req = new WikiRequestor(url, t.getUid().toString(), 0);
      cats.putAll(req.call());
    }
    return cats;
  }

  private Set<Term> getReferToTerms(String g, String lemma) throws IOException, ParseException, MalformedURLException, InterruptedException, ExecutionException {
    numOfRecursion++;
    if (numOfRecursion <= recursionLimit) {
      String titles = getReferToTitles(g);
      if (titles.length() > 0 && !titles.equals(prevTitles)) {
        try {
          URL url = new URL(PAGE + "?format=json&redirects&action=query&prop=extracts&exlimit=max&explaintext&exintro&titles=" + titles);
          LOGGER.log(Level.FINE, url.toString());
          String jsonString = IOUtils.toString(url);
          prevTitles = titles;
          return queryTerms(jsonString, lemma);
        } catch (Exception ex) {
          Logger.getLogger(WikipediaOnline.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }
    return null;
  }

  private String getReferToTitles(String g) throws UnsupportedEncodingException {
    String[] titlesArray = g.split("\n");
    StringBuilder titles = new StringBuilder();
    for (String t : titlesArray) {
      if (!t.toLowerCase().contains("may refer to:")) {
        t = URLEncoder.encode(t.split(",")[0], "UTF-8");
        if (t.length() > 0) {
          titles.append(t).append("|");
        }
      }
    }
    if (titles.length() > 1) {
      titles.deleteCharAt(titles.length() - 1);
      titles.setLength(titles.length());
    }
    return titles.toString();
  }

}
