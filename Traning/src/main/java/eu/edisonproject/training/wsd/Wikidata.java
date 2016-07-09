/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.edisonproject.training.wsd;

import eu.edisonproject.training.utility.term.avro.Term;
import eu.edisonproject.training.utility.term.avro.TermFactory;
import eu.edisonproject.utility.file.CSVFileReader;
import eu.edisonproject.utility.text.processing.Cleaner;
import eu.edisonproject.utility.text.processing.Stemming;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
public class Wikidata extends DisambiguatorImpl {

//    private static Map<String, Set<String>> termCache;
//    private static Map<String, Set<String>> titlesCache;
//    private static final String[] EXCLUDED_CAT = new String[]{
//        "articles needing",
//        "articles lacking",
//        "articles with",
//        "articles containing",
//        "articles to",
//        "article disambiguation",
//        "articles incorporating",
//        "articles covered",
//        "articles created",
//        "articles that",
//        "cs1 ",
//        "disambiguation pages",
//        "set index articles",
//        "copied and pasted articles",
//        "cleanup tagged articles",
//        "pages needing",
//        "pages lacking",
//        "pages with",
//        "pages using",
//        "disambiguation pages",
//        "use dmy dates",
//        "use mdy dates",
//        "all stub articles",
//        "orphaned articles",
//        "wikipedia introduction",
//        "wikipedia articles",
//        "wikipedia external",
//        "wikipedia indefinitely",
//        "wikipedia spam",
//        "on wikidata"
//    };
    private final String page = "https://www.wikidata.org/w/api.php";

    @Override
    public void configure(Properties properties) {

        super.configure(properties);

    }

    @Override
    public Term getTerm(String term) throws IOException, ParseException, MalformedURLException {
        Set<Term> possibleTerms = null;
        try {
            possibleTerms = getTermNodeByLemma(term);
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Wikipedia.class.getName()).log(Level.SEVERE, null, ex);
        }
        String delimeter = ",";
        String wordSeperator = "_";
        Set<String> ngarms = CSVFileReader.getNGramsForTerm(term, getItemsFilePath(), delimeter, wordSeperator);
        Term dis = super.disambiguate(term, possibleTerms, ngarms, getMinimumSimilarity());
        if (dis == null) {
            Logger.getLogger(Wikidata.class.getName()).log(Level.INFO, "Couldn''''t figure out what ''{0}'' means", term);
        } else {
            Logger.getLogger(Wikidata.class.getName()).log(Level.INFO, "Term: {0}. Confidence: {1} URL: {2}", new Object[]{dis, dis.getConfidence(), dis.getUrl()});
        }
        return dis;
    }

    private Set<Term> getTermNodeByLemma(String lemma) throws MalformedURLException, IOException, ParseException, InterruptedException, ExecutionException {

        Set<String> termsStr = getPossibleTermsFromDB(lemma);
        if (termsStr != null && !termsStr.isEmpty()) {
            Set<Term> terms = TermFactory.create(termsStr);
            Set<Term> wikiTerms = new HashSet<>();

            for (Term t : terms) {
                if (t.getUrl().toString().contains(new URL(page).getHost())) {
                    wikiTerms.add(t);
                }
            }
            if (!wikiTerms.isEmpty()) {
                return wikiTerms;
            }
        }
//        if (termsStr != null) {
//            Set<Term> terms = TermFactory.create(termsStr);
//            Set<Term> possibleTerms = new HashSet<>();
//            for (Term t : terms) {
//                boolean add = true;
//                for (CharSequence g : t.getGlosses()) {
//                    if (g != null && g.toString().contains("Wikimedia disambiguation page")) {
//                        add = false;
//                        break;
//                    }
//                }
//                if (add) {
//                    possibleTerms.add(t);
//                }
//            }
//            return possibleTerms;
//        }

        String query = lemma.replaceAll("_", " ");
        query = URLEncoder.encode(query, "UTF-8");
        int i = 0;
        URL url = new URL(page + "?action=wbsearchentities&format=json&language=en&continue=" + i + "&limit=50&search=" + query);
        System.err.println(url);
        String jsonString = IOUtils.toString(url);
        Set<Term> terms = getCandidateTerms(jsonString, lemma);

        addPossibleTermsToDB(lemma, terms);

        return terms;
    }

    private Set<Term> getCandidateTerms(String jsonString, String originalTerm) throws ParseException, IOException, MalformedURLException, InterruptedException, ExecutionException {
        Set<Term> terms = new HashSet<>();

        JSONObject jsonObj = (JSONObject) JSONValue.parseWithException(jsonString);
        JSONArray search = (JSONArray) jsonObj.get("search");
        Cleaner stemer = new Stemming();

        for (Object obj : search) {
            JSONObject jObj = (JSONObject) obj;
            String label = (String) jObj.get("label");
            if (label != null && !label.toLowerCase().contains("(disambiguation)")) {

                label = label.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
                label = label.replaceAll("\\+", "%2B");
                label = java.net.URLDecoder.decode(label, "UTF-8");
                label = label.replaceAll("_", " ").toLowerCase();
                originalTerm = java.net.URLDecoder.decode(originalTerm, "UTF-8");
                originalTerm = originalTerm.replaceAll("_", " ");

                stemer.setDescription(label);
                String stemTitle = stemer.execute();

                stemer.setDescription(originalTerm);
                String stemLema = stemer.execute();

                int dist = edu.stanford.nlp.util.StringUtils.editDistance(stemLema, stemTitle);
                if (stemTitle.contains(stemLema) && dist <= 10) {
                    String url = null;
                    Term t = new Term();
                    t.setLemma(label);
                    t.setUrl(url);
                    t.setOriginalTerm(originalTerm);
                    JSONArray aliases = (JSONArray) jObj.get("aliases");
                    if (aliases != null) {
                        List<CharSequence> altLables = new ArrayList<>();
                        for (Object aObj : aliases) {
                            String alt = (String) aObj;
                            altLables.add(alt);
                        }
                        t.setAltLables(altLables);
                    }
                    String description = (String) jObj.get("description");
                    if (description == null || !description.toLowerCase().contains("wikipedia disambiguation page")) {
                        String id = (String) jObj.get("id");
                        url = "https://www.wikidata.org/wiki/" + id;
                        t.setUrl(url);
                        List<CharSequence> glosses = new ArrayList<>();
                        glosses.add(description);
                        t.setGlosses(glosses);
                        t.setUid(id);

//                        List<String> broaderID = getBroaderID(id);
//                        t.setBroaderUIDS(broaderID);
//                        List<String> cat = getCategories(id);
//                        t.setCategories(cat);
                        terms.add(t);
                    }
                }
            }
        }
        Set<Term> catTerms = new HashSet<>();

        Map<CharSequence, List<CharSequence>> cats = getCategories(terms);
        for (Term t : terms) {
            List<CharSequence> cat = cats.get(t.getUid());
            t.setCategories(cat);
            catTerms.add(t);
        }

        Map<CharSequence, List<CharSequence>> broaderIDs = getbroaderIDS(terms);
        Set<Term> returnTerms = new HashSet<>();
        for (Term t : catTerms) {
            List<CharSequence> broaderID = broaderIDs.get(t.getUid());
            t.setBuids(broaderID);
            returnTerms.add(t);
        }

        return returnTerms;
    }

//    private boolean shouldAddCategory(String cat) {
//        for (String s : EXCLUDED_CAT) {
//            if (cat.toLowerCase().contains(s)) {
//                return false;
//            }
//        }
//        return true;
//    }
//    private List<String> getBroaderID(String id) throws MalformedURLException, IOException, ParseException {
//        return getNumProperty(id, "P31");
//    }
    private Map<CharSequence, List<CharSequence>> getbroaderIDS(Set<Term> terms) throws MalformedURLException, InterruptedException, ExecutionException {
        Map<CharSequence, List<CharSequence>> map = new HashMap<>();
        if (terms.size() > 0) {
            int maxT = 2;
            ExecutorService pool = new ThreadPoolExecutor(maxT, maxT,
                    5000L, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(maxT, true), new ThreadPoolExecutor.CallerRunsPolicy());

            Set<Future<Map<CharSequence, List<CharSequence>>>> set1 = new HashSet<>();
            String prop = "P31";
            for (Term t : terms) {
                URL url = new URL(page + "?action=wbgetclaims&format=json&props=&property=" + prop + "&entity=" + t.getUid());
                System.err.println(url);
                WikiRequestor req = new WikiRequestor(url, t.getUid().toString(), 1);
                Future<Map<CharSequence, List<CharSequence>>> future = pool.submit(req);
                set1.add(future);
            }
            pool.shutdown();

            for (Future<Map<CharSequence, List<CharSequence>>> future : set1) {
                while (!future.isDone()) {
//                Logger.getLogger(Wikipedia.class.getName()).log(Level.INFO, "Task is not completed yet....");
                    Thread.currentThread().sleep(10);
                }
                Map<CharSequence, List<CharSequence>> c = future.get();
                if (c != null) {
                    map.putAll(c);
                }
            }
        }

        return map;
    }

    private Map<CharSequence, List<CharSequence>> getCategories(Set<Term> terms) throws MalformedURLException, InterruptedException, ExecutionException {
        Map<CharSequence, List<CharSequence>> cats = new HashMap<>();

        if (terms.size() > 0) {
            int maxT = 2;
            ExecutorService pool = new ThreadPoolExecutor(maxT, maxT,
                    5000L, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(maxT, true), new ThreadPoolExecutor.CallerRunsPolicy());

            Set<Future<Map<CharSequence, List<CharSequence>>>> set1 = new HashSet<>();
            String prop = "P910";
            for (Term t : terms) {
                URL url = new URL(page + "?action=wbgetclaims&format=json&props=&property=" + prop + "&entity=" + t.getUid());
                System.err.println(url);
                WikiRequestor req = new WikiRequestor(url, t.getUid().toString(), 1);
                Future<Map<CharSequence, List<CharSequence>>> future = pool.submit(req);
                set1.add(future);
            }
            pool.shutdown();

            Map<CharSequence, List<CharSequence>> map = new HashMap<>();
            for (Future<Map<CharSequence, List<CharSequence>>> future : set1) {
                while (!future.isDone()) {
//                Logger.getLogger(Wikipedia.class.getName()).log(Level.INFO, "Task is not completed yet....");
                    Thread.currentThread().sleep(10);
                }
                Map<CharSequence, List<CharSequence>> c = future.get();
                if (c != null) {
                    map.putAll(c);
                }
            }

            pool = new ThreadPoolExecutor(maxT, maxT,
                    5000L, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(maxT, true), new ThreadPoolExecutor.CallerRunsPolicy());

            Set<Future<Map<CharSequence, List<CharSequence>>>> set2 = new HashSet<>();
            for (Term t : terms) {
                List<CharSequence> catIDs = map.get(t.getUid());
                for (CharSequence catID : catIDs) {
                    URL url = new URL(page + "?action=wbgetentities&format=json&props=labels&languages=en&ids=" + catID);
                    System.err.println(url);
                    WikiRequestor req = new WikiRequestor(url, t.getUid().toString(), 2);
                    Future<Map<CharSequence, List<CharSequence>>> future = pool.submit(req);
                    set2.add(future);
                }
            }
            pool.shutdown();

            for (Future<Map<CharSequence, List<CharSequence>>> future : set2) {
                while (!future.isDone()) {
//                Logger.getLogger(Wikipedia.class.getName()).log(Level.INFO, "Task is not completed yet....");
                    Thread.currentThread().sleep(10);
                }
                Map<CharSequence, List<CharSequence>> c = future.get();
                if (c != null) {
                    cats.putAll(c);
                }
            }
        }

        return cats;
    }

    private List<String> getNumProperty(String id, String prop) throws MalformedURLException, IOException, ParseException {
        URL url = new URL(page + "?action=wbgetclaims&format=json&props=&property=" + prop + "&entity=" + id);
        System.err.println(url);
        String jsonString = IOUtils.toString(url);
        JSONObject jsonObj = (JSONObject) JSONValue.parseWithException(jsonString);

        JSONObject claims = (JSONObject) jsonObj.get("claims");

        JSONArray Jprop = (JSONArray) claims.get(prop);
        List<String> ids = new ArrayList<>();
        if (Jprop != null) {
            for (Object obj : Jprop) {
                JSONObject jobj = (JSONObject) obj;

                JSONObject mainsnak = (JSONObject) jobj.get("mainsnak");
//                System.err.println(mainsnak);
                JSONObject datavalue = (JSONObject) mainsnak.get("datavalue");
//                System.err.println(datavalue);
                if (datavalue != null) {
                    JSONObject value = (JSONObject) datavalue.get("value");
//            System.err.println(value);
                    java.lang.Long numericID = (java.lang.Long) value.get("numeric-id");
//                System.err.println(id + " -> Q" + numericID);
                    ids.add("Q" + numericID);
                }

            }
        }

        return ids;
    }

//    private List<String> getCategories(String id) throws IOException, MalformedURLException, ParseException {
//        List<String> ids = getNumProperty(id, "P910");
//        List<String> lables = new ArrayList();
//        if (ids != null) {
//            for (String s : ids) {
//                String l = getLabel(s);
//                lables.add(l);
//            }
//        }
//
//        return lables;
//    }
//    private String getLabel(String id) throws MalformedURLException, IOException, ParseException {
//
//        URL url = new URL(page + "?action=wbgetentities&format=json&props=labels&languages=en&ids=" + id);
//        System.err.println(url);
//        String jsonString = IOUtils.toString(url);
//        JSONObject jsonObj = (JSONObject) JSONValue.parseWithException(jsonString);
//
//        JSONObject entities = (JSONObject) jsonObj.get("entities");
////        System.err.println(entities);
//        JSONObject jID = (JSONObject) entities.get(id);
//
//        JSONObject labels = (JSONObject) jID.get("labels");
////        System.err.println(labels);
//        JSONObject en = (JSONObject) labels.get("en");
////        System.err.println(en);
//        if (en != null) {
//            String value = (String) en.get("value");
//            return value.substring("Category:".length()).toLowerCase().replaceAll(" ", "_");
//        }
//        return null;
//    }
}
