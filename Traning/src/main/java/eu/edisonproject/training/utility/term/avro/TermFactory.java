/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.edisonproject.training.utility.term.avro;

import eu.edisonproject.utility.text.processing.Cleaner;
import eu.edisonproject.utility.text.processing.Stemming;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author S. Koulouzis
 */
public class TermFactory {

    private static final Charset CHARSET = Charset.forName("ISO-8859-15");
    private static final CharsetDecoder DECODER = CHARSET.newDecoder();
    private static final Pattern LINE_PATTERN
            = Pattern.compile(".*\r?\n");
    private static Map<String, Set<String>> nGramsMap;
    private static JSONParser parser;

    public static Term create(String synet, String language, String lemma, String theID, String url) throws ParseException, UnsupportedEncodingException, IOException {
        Term node;
        language = language.toLowerCase();
        JSONObject jSynet = (JSONObject) JSONValue.parseWithException(synet);
        Cleaner stemer = new Stemming();

        JSONArray categoriesArray = (JSONArray) jSynet.get("categories");
        List<CharSequence> categories = null;
        if (categoriesArray != null) {

            categories = new ArrayList<>();
            for (Object o : categoriesArray) {
                JSONObject cat = (JSONObject) o;
                String lang = (String) cat.get("language");
                if (lang.toLowerCase().equals(language)) {
                    String category = ((String) cat.get("category")).toLowerCase();
                    categories.add(category);
                }
            }
        }

        JSONArray glossesArray = (JSONArray) jSynet.get("glosses");

        List<CharSequence> glosses = null;
        if (glossesArray != null) {
            glosses = new ArrayList<>();

            for (Object o : glossesArray) {
                JSONObject gloss = (JSONObject) o;
                String lang = (String) gloss.get("language");
                if (lang.toLowerCase().equals(language)) {
                    String g = ((String) gloss.get("gloss")).toLowerCase();
                    glosses.add(g);
                }
            }
        }

        JSONArray senses = (JSONArray) jSynet.get("senses");
        if (senses != null) {
            for (Object o2 : senses) {
                JSONObject jo2 = (JSONObject) o2;
                JSONObject synsetID = (JSONObject) jo2.get("synsetID");
                String babelNetID = (String) synsetID.get("id");

                String lang = (String) jo2.get("language");

//                String lemma1, lemma2;
                if (lang.toLowerCase().equals(language)) {
                    List<CharSequence> altLables = new ArrayList<>();
                    String jlemma = (String) jo2.get("lemma");
                    jlemma = jlemma.toLowerCase().replaceAll("(\\d+,\\d+)|\\d+", "");
                    altLables.add(jlemma);
                    if (theID != null && babelNetID.equals(theID)) {
                        node = new Term();
                        node.setLemma(lemma);
                        node.setUrl(url);
                        node.setUid(babelNetID);
                        node.setCategories(categories);
                        node.setAltLables(altLables);
                        node.setGlosses(glosses);
                        node.setOriginalTerm(lemma);
                        return node;
                    }
                    lemma = java.net.URLDecoder.decode(lemma, "UTF-8");
                    lemma = lemma.replaceAll(" ", "_");

                    stemer.setDescription(jlemma);
                    String stemjlemma = stemer.execute();

                    stemer.setDescription(lemma);
                    String stemLema = stemer.execute();

                    int dist = edu.stanford.nlp.util.StringUtils.editDistance(stemLema, stemjlemma);

                    if (dist <= 0) {
                        node = new Term();
                        node.setLemma(jlemma);
                        node.setUrl(url);
                        node.setUid(babelNetID);
                        node.setCategories(categories);
                        node.setAltLables(altLables);
                        node.setGlosses(glosses);
                        node.setOriginalTerm(lemma);
                        return node;
                    }
                    dist = edu.stanford.nlp.util.StringUtils.editDistance(lemma, jlemma);
                    String shorter, longer;
                    if (stemjlemma.length() > stemLema.length()) {
                        shorter = stemLema;
                        longer = stemjlemma;
                    } else {
                        shorter = stemjlemma;
                        longer = stemLema;
                    }

                    if (dist <= 4 && longer.contains(shorter)) {
                        node = new Term();
                        node.setLemma(jlemma);
                        node.setUrl(url);
                        node.setUid(babelNetID);
                        node.setCategories(categories);
                        node.setAltLables(altLables);
                        node.setGlosses(glosses);
                        node.setOriginalTerm(lemma);
                        return node;
                    }
                }
            }
        }

        return null;
    }

    public static Term create(FileReader fr) throws IOException, ParseException {
//        String jsonStr = FileUtils.readFile(fr);
//        Term term = new Term(FileUtils.getLemma(jsonStr), FileUtils.getURL(jsonStr));
//        term.setUid(FileUtils.getUID(jsonStr));
//        term.setAltLables(FileUtils.getAltLables(jsonStr));
//        term.setBroaderUIDS(FileUtils.getBroaderUIDS(jsonStr));
//        term.setCategories(FileUtils.getCategories(jsonStr));
//        term.setForeignKey(FileUtils.getForeignKey(jsonStr));
//        term.setGlosses(FileUtils.getGlosses(jsonStr));
//        term.setIsFromDictionary(FileUtils.IsFromDictionary(jsonStr));
//        term.setOriginalTerm(FileUtils.getOriginalTerm(jsonStr));
//        term.setConfidence(FileUtils.getConfidence(jsonStr));
//        return term;
        return null;
    }

    public static Term create(String jsonStr) throws IOException, ParseException {
        Term term = new Term();
        term.setLemma(getLemma(jsonStr));
        term.setUrl(getURL(jsonStr));
        term.setUid(getUID(jsonStr));
        term.setAltLables(getAltLables(jsonStr));
        term.setBuids(getBroaderUIDS(jsonStr));
        term.setNuids(getNarrowerUIDS(jsonStr));
        term.setCategories(getCategories(jsonStr));
//        term.setForeignKey(getForeignKey(jsonStr));
        term.setGlosses(getGlosses(jsonStr));
//        term.setIsFromDictionary(IsFromDictionary(jsonStr));
        term.setOriginalTerm(getOriginalTerm(jsonStr));
        term.setConfidence(getConfidence(jsonStr));
        return term;
    }

    public static Set<JSONObject> term2Json(List<Term> terms) {
        Set<JSONObject> objs = new HashSet<>();
        for (Term t : terms) {
            objs.add(term2Json(t));
        }
        return objs;
    }

    public static JSONObject term2Json(Term t) {
        JSONObject obj = new JSONObject();
        obj.put("uid", t.getUid());
        obj.put("lemma", t.getLemma());
        obj.put("altLables", t.getAltLables());
        obj.put("buids", t.getBuids());
        obj.put("categories", t.getCategories());
//        obj.put("foreignKey", t.getForeignKey());
        obj.put("glosses", t.getGlosses());
//        obj.put("isFromDictionary", t.getIsFromDictionary());
//                    obj.put("narrower", t.getNarrower());
        obj.put("narrowerUIDS", t.getNuids());
//  obj.put("narrowerUIDS", t.getSynonyms());
        obj.put("confidence", t.getConfidence());
        obj.put("originalTerm", t.getOriginalTerm());
        obj.put("url", t.getUrl());
        return obj;
    }

//    public static Term create(JSONObject page, String originalTerm) {
//        String title = (String) page.get("title");
//        Long pageid = (Long) page.get("pageid");
//        String extract = (String) page.get("extract");
//        String url = null;
//        Term t = null;
//        if (extract != null) {
//
//            t = new Term();
//            t.setLemma(title.toLowerCase());
//            t.setUrl(url);
//            List<CharSequence> glosses = new ArrayList<>();
//            glosses.add(extract.toLowerCase());
//            t.setGlosses(glosses);
//            t.setUid(String.valueOf(pageid));
//            url = "https://en.wikipedia.org/?curid=" + t.getUid();
//            t.setUrl(url);
//            t.setOriginalTerm(originalTerm);
//        }
//        return t;
//    }
//    public static Term create(File file) throws IOException, ParseException {
//        try (FileReader fr = new FileReader(file)) {
//            String jsonStr = FileUtils.readFile(fr);
//            return create(jsonStr);
//        }
//    }
    public static Set<Term> create(Set<String> terms) throws IOException, ParseException {
        Set<Term> te = new HashSet<>();
        for (String s : terms) {
            Term t = create(s);
            te.add(t);
        }
        return te;
    }

    public static Set<String> terms2Json(Set<Term> terms) throws IOException, ParseException {
        Set<String> te = new HashSet<>();
        for (Term t : terms) {
            te.add(term2Json(t).toJSONString());
//            te.add(create(s));
        }
        return te;
    }

    private static String getLemma(String jsonString) throws IOException, ParseException {
        return getString(jsonString, "lemma");
    }

    private static String getString(String jsonString, String field) throws IOException, org.json.simple.parser.ParseException {
        if (parser == null) {
            parser = new JSONParser();
        }
        Object obj = parser.parse(jsonString);
        JSONObject jsonObject = (JSONObject) obj;
        return (String) jsonObject.get(field);
    }

    static String getURL(String jsonStr) throws IOException, ParseException {
        return getString(jsonStr, "url");
    }

    private static String getUID(String jsonString) throws IOException, org.json.simple.parser.ParseException {
        return getString(jsonString, "uid");
    }

    private static List<CharSequence> getGlosses(String jsonString) throws IOException, org.json.simple.parser.ParseException {
        return getList(jsonString, "glosses");
    }

    private static List<CharSequence> getList(String jsonString, String field) throws IOException, ParseException {
        if (parser == null) {
            parser = new JSONParser();
        }
        Object obj;
        obj = parser.parse(jsonString);
        JSONObject jsonObject = (JSONObject) obj;
        List<CharSequence> list = new ArrayList<>();
        org.json.simple.JSONArray ja = (org.json.simple.JSONArray) jsonObject.get(field);
        if (ja == null) {
            list.add("EMPTY");
        } else {

            for (Object elem : ja) {
                String s = (String) elem;
                list.add(s);
            }
        }

        return list;
    }

    static List<CharSequence> getAltLables(String jsonString) throws IOException, ParseException {
        return getList(jsonString, "altLables");
    }

    static List<CharSequence> getBroaderUIDS(String jsonString) throws IOException, ParseException {
        return getList(jsonString, "buids");
    }

    private static List<CharSequence> getNarrowerUIDS(String jsonString) throws IOException, ParseException {
        return getList(jsonString, "nuids");
    }

    static List<CharSequence> getCategories(String jsonString) throws IOException, ParseException {
        return getList(jsonString, "categories");
    }

    static String getOriginalTerm(String jsonStr) throws IOException, ParseException {
        return getString(jsonStr, "originalTerm");
    }

    static double getConfidence(String jsonStr) throws ParseException {
        return getDouble(jsonStr, "confidence");
    }

    private static double getDouble(String jsonStr, String field) throws ParseException {
        if (parser == null) {
            parser = new JSONParser();
        }
        Object obj = parser.parse(jsonStr);
        JSONObject jsonObject = (JSONObject) obj;
        return (Double) jsonObject.get(field);
    }

}
