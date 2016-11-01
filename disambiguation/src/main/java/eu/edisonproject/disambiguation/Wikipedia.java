/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.disambiguation;

import eu.edisonproject.common.Term;
import eu.edisonproject.common.utils.file.CSVFileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

/**
 *
 * @author S. Koulouzis
 */
public class Wikipedia extends DisambiguatorImpl {

  protected final String PAGE = "https://en.wikipedia.org/w/api.php";

  public static final String[] EXCLUDED_CAT = new String[]{
    "articles needing",
    "articles lacking",
    "articles with",
    "articles containing",
    "articles to",
    "article disambiguation",
    "articles incorporating",
    "articles_including",
    "articles covered",
    "articles created",
    "articles that",
    "cs1 ",
    "disambiguation pages",
    "set index articles",
    "copied and pasted articles",
    "cleanup tagged articles",
    "pages needing",
    "pages lacking",
    "pages with",
    "pages using",
    "disambiguation pages",
    "use dmy dates",
    "use mdy dates",
    "all stub articles",
    "orphaned articles",
    "wikipedia introduction",
    "wikipedia articles",
    "wikipedia external",
    "wikipedia indefinitely",
    "wikipedia spam",
    "on wikidata",
    "vague or ambiguous time from",
    "stubs"
  };

  @Override
  public Term getTerm(String term) throws IOException, ParseException, MalformedURLException, UnsupportedEncodingException {
    Set<Term> possibleTerms = null;
    try {
      possibleTerms = getCandidates(term);
    } catch (InterruptedException | ExecutionException ex) {
      Logger.getLogger(Wikipedia.class.getName()).log(Level.SEVERE, null, ex);
    }
    String delimeter = ",";
    String wordSeperator = " ";
    Set<String> ngarms = CSVFileReader.getNGramsForTerm(term, getItemsFilePath(), delimeter, wordSeperator);
    Term dis = super.disambiguate(term, possibleTerms, ngarms, getMinimumSimilarity());
    if (dis == null) {
      Logger.getLogger(Wikipedia.class.getName()).log(Level.INFO, "Couldn''''t figure out what ''{0}'' means", term);
    } else {
      Logger.getLogger(Wikipedia.class.getName()).log(Level.INFO, "Term: {0}. Confidence: {1} URL: {2}", new Object[]{dis, dis.getConfidence(), dis.getUrl()});
    }
    return dis;
  }

  @Override
  public Set<Term> getCandidates(String lemma) throws MalformedURLException, IOException, ParseException, UnsupportedEncodingException, InterruptedException, ExecutionException {
    return null;
  }

  protected Set<String> getTitles(String lemma) throws ParseException, UnsupportedEncodingException, IOException {
    String URLquery = lemma.replaceAll("_", " ");
    URLquery = URLEncoder.encode(URLquery, "UTF-8");
    //sroffset=10
    URL url = new URL(PAGE + "?action=query&format=json&redirects&list=search&srlimit=500&srsearch=" + URLquery);
    Logger.getLogger(Wikipedia.class.getName()).log(Level.FINE, url.toString());
    String jsonString = IOUtils.toString(url);

    Set<String> titles = new TreeSet<>();
    JSONObject jsonObj = (JSONObject) JSONValue.parseWithException(jsonString);
    JSONObject query = (JSONObject) jsonObj.get("query");
    JSONArray search = (JSONArray) query.get("search");
    if (search != null) {
      for (Object o : search) {
        JSONObject res = (JSONObject) o;
        String title = (String) res.get("title");
//                System.err.println(title);
        if (title != null && !title.toLowerCase().contains("(disambiguation)")) {
//                if (title != null) {
          title = title.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
          title = title.replaceAll("\\+", "%2B");
          title = java.net.URLDecoder.decode(title, "UTF-8");
          title = title.replaceAll("_", " ").toLowerCase();
          lemma = java.net.URLDecoder.decode(lemma, "UTF-8");
          lemma = lemma.replaceAll("_", " ");
          titles.add(title);
        }
      }
    }
    titles.add(lemma);
    return titles;
  }
}
