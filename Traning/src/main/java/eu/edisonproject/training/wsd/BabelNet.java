/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.edisonproject.training.wsd;

import edu.stanford.nlp.util.Pair;
import eu.edisonproject.training.utility.term.avro.Term;
import eu.edisonproject.utility.commons.ValueComparator;
import eu.edisonproject.utility.file.CSVFileReader;
import java.util.Properties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
public class BabelNet extends DisambiguatorImpl {

    private String keysStr;
    private String key;
    private String[] keys;
    private int keyIndex = 0;

    @Override
    public Term getTerm(String term) throws IOException, ParseException, UnsupportedEncodingException, FileNotFoundException {
        Term dis = super.getTerm(term);
        if (dis == null) {
            String delimeter = ",";
            String wordSeperator = "_";
            Set<String> ngarms = CSVFileReader.getNGramsForTerm(term, getItemsFilePath(), delimeter, wordSeperator);
            Set<Term> possibleTerms = getTermNodeByLemma(term);
            dis = super.disambiguate(term, possibleTerms, ngarms, getMinimumSimilarity());
            if (dis == null) {
                possibleTerms = babelNetDisambiguation("EN", term, ngarms);
                if (possibleTerms != null && possibleTerms.size() == 1) {
                    dis = possibleTerms.iterator().next();
                }
            }

        } else {
            return dis;
        }

        return dis;
    }

    private Set<Term> getTermNodeByLemma(String term) throws IOException, ParseException, UnsupportedEncodingException, FileNotFoundException {
        try {
            String language = "EN";

            List<String> ids = getcandidateWordIDs(language, term);
            Set<Term> nodes = new HashSet<>();
            if (ids != null) {
                for (String id : ids) {
                    String synet = getBabelnetSynset(id, language);
                    String url = null;
                    Term node = TermFactory.create(synet, language, term, null, url);
                    if (node != null) {
                        try {
                            url = "http://babelnet.org/synset?word=" + URLEncoder.encode(node.getUid().toString(), "UTF-8");
                            node.setUrl(url);
                            List<Term> h = getHypernyms(language, node);
                            if (h != null && !h.isEmpty()) {
                                List<CharSequence> broaderUIDS = new ArrayList<>();
                                for (Term t : h) {
                                    broaderUIDS.add(t.getUid());
                                }
                                node.setBuids(broaderUIDS);
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(BabelNet.class.getName()).log(Level.WARNING, null, ex);
                        }
                        nodes.add(node);
                    }
                }
            }
            return nodes;
        } catch (InterruptedException ex) {
            Logger.getLogger(BabelNet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private String getBabelnetSynset(String id, String lan) throws IOException, FileNotFoundException, InterruptedException {
//        if (db == null || db.isClosed()) {
//            loadCache();
//        }
        if (id == null || id.length() < 1) {
            return null;
        }
//        String json = getFromSynsetCache(id);
//        if (json != null && json.equals("NON-EXISTING")) {
//            return null;
//        }
        String json = null;
        if (json == null) {
            URL url = new URL("http://babelnet.io/v2/getSynset?id=" + id + "&filterLangs=" + lan + "&langs=" + lan + "&key=" + this.key);
            System.err.println(url);
            json = IOUtils.toString(url);
            handleKeyLimitException(json);
//            if (db.isClosed()) {
//                loadCache();
//            }

//            if (json != null) {
//                putInSynsetCache(id, json);
//            } else {
//                putInSynsetCache(id, "NON-EXISTING");
//            }
        }

        return json;
    }

    @Override
    public void configure(Properties properties) {
        super.configure(properties);
        keysStr = properties.getProperty("bablenet.key");
        keys = keysStr.split(",");
        key = keys[keyIndex];
    }

    private List<String> getcandidateWordIDs(String language, String word) throws IOException, ParseException, FileNotFoundException, InterruptedException {
//        if (db == null || db.isClosed()) {
//            loadCache();
//        }
//        List<String> ids = getFromWordIDCache(word);
//        if (ids != null && ids.size() == 1 && ids.get(0).equals("NON-EXISTING")) {
//            return null;
//        }
        List<String> ids = null;
        language = language.toUpperCase();
        if (ids == null || ids.isEmpty()) {
            ids = new ArrayList<>();
            URL url = new URL("http://babelnet.io/v2/getSynsetIds?word=" + word + "&langs=" + language + "&key=" + this.key);
            System.err.println(url);
            String genreJson = IOUtils.toString(url);
            int count = 0;
            try {
                handleKeyLimitException(genreJson);
            } catch (IOException ex) {
                if (ex.getMessage().contains("Your key is not valid or the daily requests limit has been reached") && count < keys.length - 1) {
                    count++;
                    return getcandidateWordIDs(language, word);
                } else {
                    throw ex;
                }
            }

            Object obj = JSONValue.parseWithException(genreJson);
            if (obj instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) obj;
                for (Object o : jsonArray) {
                    JSONObject jo = (JSONObject) o;
                    if (jo != null) {
                        String id = (String) jo.get("id");
                        if (id != null) {
                            ids.add(id);
                        }
                    }
                }
            } else if (obj instanceof JSONObject) {
                JSONObject jsonObj = (JSONObject) obj;
                String id = (String) jsonObj.get("id");
                if (id != null) {
                    ids.add(id);
                }
            }
//            if (db.isClosed()) {
//                loadCache();
//            }

            if (ids.isEmpty()) {
//                ids.add("NON-EXISTING");
//                putInWordIDCache(word, ids);
                return null;
            }
//            putInWordIDCache(word, ids);
        }
        return ids;
    }

    private void handleKeyLimitException(String genreJson) throws IOException, FileNotFoundException, InterruptedException {
        if (genreJson.contains("Your key is not valid or the daily requests limit has been reached")) {
            keyIndex++;
            if (keyIndex > keys.length - 1) {
                keyIndex = 0;
            }
            key = keys[keyIndex];
            Logger.getLogger(BabelNet.class.getName()).log(Level.FINE, "Switch to: {0}", keyIndex);
            throw new IOException(genreJson);
        }
    }

//    private void saveCache() throws FileNotFoundException, IOException, InterruptedException {
//        Logger.getLogger(BabelNet.class.getName()).log(Level.FINE, "Saving cache");
//        if (db != null) {
//            if (!db.isClosed()) {
//                commitDB();
//                db.close();
//            }
//        }
//    }
    private List<Term> getHypernyms(String language, Term t) throws MalformedURLException, IOException, ParseException, Exception {
        Map<String, Double> hypenymMap = getEdgeIDs(language, t.getUid().toString(), "HYPERNYM");
        List<Term> hypernyms = new ArrayList<>();

        ValueComparator bvc = new ValueComparator(hypenymMap);
        Map<String, Double> sorted_map = new TreeMap(bvc);
        sorted_map.putAll(hypenymMap);
        int maxNumOfHyper = 5;

        for (String uid : sorted_map.keySet()) {
            if (maxNumOfHyper <= 0) {
                break;
            }

            String synetHyper = getBabelnetSynset(uid, language);
            String url = "http://babelnet.org/synset?word=" + URLEncoder.encode(uid, "UTF-8");
            Term hypernym = TermFactory.create(synetHyper, language, null, uid, url);
            if (hypernym != null) {
                hypernyms.add(hypernym);
            }

            maxNumOfHyper--;
        }
        return hypernyms;
    }

    private Map<String, Double> getEdgeIDs(String language, CharSequence id, String relation) throws MalformedURLException, IOException, ParseException, Exception {
//        if (db == null || db.isClosed()) {
//            loadCache();
//        }
//        String genreJson = getFromEdgesCache(id);
        String genreJson = null;
        if (genreJson == null) {
            URL url = new URL("http://babelnet.io/v2/getEdges?id=" + id + "&key=" + this.key);
            System.err.println(url);
            genreJson = IOUtils.toString(url);
            handleKeyLimitException(genreJson);
            if (genreJson != null) {
//                putInEdgesCache(id, genreJson);
            }
            if (genreJson == null) {
//                putInEdgesCache(id, "NON-EXISTING");
            }
        }
        Object obj = JSONValue.parseWithException(genreJson);
        JSONArray edgeArray = (JSONArray) obj;
        Map<String, Double> map = new HashMap<>();
        for (Object o : edgeArray) {
            JSONObject pointer = (JSONObject) ((JSONObject) o).get("pointer");
            String relationGroup = (String) pointer.get("relationGroup");
            String target = (String) ((JSONObject) o).get("target");
            Double normalizedWeight = (Double) ((JSONObject) o).get("normalizedWeight");
            Double weight = (Double) ((JSONObject) o).get("weight");
            if (relationGroup.equals(relation)) {
                map.put(target, ((normalizedWeight + weight) / 2.0));
            }
        }
        return map;
    }

//    private Term disambiguate(String term, Set<Term> possibleTerms, String itemsFilePath, double minimumSimilarity) throws IOException, ParseException {
//        String delimeter = "/";
//        Set<String> ngarms = CSVFileReader.getNGramsForTerm(term, itemsFilePath, delimeter);
//
//        Term dis = disambiguate(term, possibleTerms, ngarms, minimumSimilarity);
//        if (dis != null) {
//            return dis;
//        } else {
//            possibleTerms = babelNetDisambiguation("EN", term, ngarms);
//            if (possibleTerms != null && possibleTerms.size() == 1) {
//                dis = possibleTerms.iterator().next();
//            }
//        }
//        return dis;
//    }
    private Set<Term> babelNetDisambiguation(String language, String lemma, Set<String> ngarms) {
        if (ngarms.isEmpty()) {
            return null;
        }
        if (ngarms.size() == 1 && ngarms.iterator().next().length() <= 1) {
            return null;
        }

        HashMap<CharSequence, Double> idsMap = new HashMap<>();
        Map<CharSequence, Term> termMap = new HashMap<>();
        Set<Term> terms = new HashSet<>();
        int count = 0;
        int breaklimit = 1000;
        int oneElementlimit = 65;
        int difflimit = 60;
        Double persent;
        for (String n : ngarms) {
            if (n.length() <= 1) {
                continue;
            }
            count++;
            if (idsMap.size() == 1 && count > oneElementlimit) {
//                Double score = idsMap.values().iterator().next();
//                if (score >= 10) {
                break;
//                }
            }

            if ((count % 2) == 0 && idsMap.size() >= 2 && count > difflimit) {
                ValueComparator bvc = new ValueComparator(idsMap);
                TreeMap<CharSequence, Double> sorted_map = new TreeMap(bvc);
                sorted_map.putAll(idsMap);
                Iterator<CharSequence> iter = sorted_map.keySet().iterator();
                Double first = idsMap.get(iter.next());
                Double second = idsMap.get(iter.next());

                persent = first / (first + second);
                if (persent > 0.65) {
                    break;
                }
            }
            if (count > breaklimit) {
                break;
            }

            String clearNg = n.replaceAll("_", " ");
            if (clearNg == null) {
                continue;
            }
            if (clearNg.startsWith(" ")) {
                clearNg = clearNg.replaceFirst(" ", "");
            }
            if (clearNg.endsWith(" ")) {
                clearNg = clearNg.substring(0, clearNg.length() - 1);
            }

            Pair<Term, Double> termPair = null;
            try {
                termPair = babelNetDisambiguation(language, lemma, clearNg);
            } catch (Exception ex) {
                if (ex.getMessage() != null && ex.getMessage().contains("Your key is not valid")) {
                    try {
                        termPair = babelNetDisambiguation(language, lemma, clearNg);
                    } catch (Exception ex1) {
//                        Logger.getLogger(BabelNet.class.getName()).log(Level.WARNING, ex1, null);
                    }
                } else {
                    Logger.getLogger(BabelNet.class.getName()).log(Level.WARNING, null, ex);
                }
            }
            if (termPair != null) {
                termMap.put(termPair.first.getUid(), termPair.first);
                Double score;
                if (idsMap.containsKey(termPair.first.getUid())) {
                    score = idsMap.get(termPair.first.getUid());
//                    score++;
                    score += termPair.second;
                } else {
//                    score = 1.0;
                    score = termPair.second;
                }
                idsMap.put(termPair.first.getUid(), score);
            }
        }
        if (!idsMap.isEmpty()) {
            ValueComparator bvc = new ValueComparator(idsMap);
            TreeMap<CharSequence, Double> sorted_map = new TreeMap(bvc);
            sorted_map.putAll(idsMap);
            count = 0;
            Double firstScore = idsMap.get(sorted_map.firstKey());
            terms.add(termMap.get(sorted_map.firstKey()));
            idsMap.remove(sorted_map.firstKey());
            for (CharSequence tvID : sorted_map.keySet()) {
                if (count >= 1) {
                    Double secondScore = idsMap.get(tvID);
                    persent = secondScore / (firstScore + secondScore);
                    if (persent > 0.2) {
                        terms.add(termMap.get(tvID));
                    }
                    if (count >= 2) {
                        break;
                    }
                }
                count++;
            }
            return terms;
        }
        return null;
    }

    private Pair<Term, Double> babelNetDisambiguation(String language, String lemma, String sentence) throws IOException, ParseException, Exception {
        if (lemma == null || lemma.length() < 1) {
            return null;
        }
//        if (db == null || db.isClosed()) {
//            loadCache();
//        }
        String query = lemma + " " + sentence.replaceAll("_", " ");

        query = URLEncoder.encode(query, "UTF-8");
        String genreJson = null;

//        genreJson = getFromDisambiguateCache(sentence);
        if (genreJson != null && genreJson.equals("NON-EXISTING")) {
            return null;
        }
        if (genreJson == null) {
            URL url = new URL("http://babelfy.io/v1/disambiguate?text=" + query + "&lang=" + language + "&key=" + key);
            System.err.println(url);
            genreJson = IOUtils.toString(url);
            handleKeyLimitException(genreJson);
//            if (db.isClosed()) {
//                loadCache();
//            }
//            if (!genreJson.isEmpty() || genreJson.length() < 1) {
//                putInDisambiguateCache(sentence, genreJson);
//            } else {
//                putInDisambiguateCache(sentence, "NON-EXISTING");
//            }
        }
        Object obj = JSONValue.parseWithException(genreJson);
//        Term term = null;
        if (obj instanceof JSONArray) {
            JSONArray ja = (JSONArray) obj;
            for (Object o : ja) {
                JSONObject jo = (JSONObject) o;
                String id = (String) jo.get("babelSynsetID");
                Double score = (Double) jo.get("score");
                Double globalScore = (Double) jo.get("globalScore");
                Double coherenceScore = (Double) jo.get("coherenceScore");
                double someScore = (score + globalScore + coherenceScore) / 3.0;
                String synet = getBabelnetSynset(id, language);
                String url = "http://babelnet.org/synset?word=" + URLEncoder.encode(id, "UTF-8");
                Term t = TermFactory.create(synet, language, lemma, null, url);
                if (t != null) {
                    List<Term> h = getHypernyms(language, t);
                    if (h != null) {
                        List<CharSequence> broader = new ArrayList<>();
                        for (Term hyper : h) {
                            broader.add(hyper.getUid());
                        }
                        t.setBuids(broader);
                    }

                    return new Pair<>(t, someScore);
                }
            }
        }
        return null;
    }
}
