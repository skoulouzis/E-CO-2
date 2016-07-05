/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.edisonproject.traning.wsd;

import eu.edisonproject.traning.utility.term.avro.Term;
import eu.edisonproject.utility.commons.ValueComparator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.json.simple.parser.ParseException;

/**
 *
 * @author S. Koulouzis
 */
public class DisambiguatorImpl implements Disambiguator, Callable {

    private static Collection<? extends String> tokenize(CharSequence s, boolean b) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Integer limit;
    private Double minimumSimilarity;
    private Integer lineOffset;
    private String termToProcess;
    private String stopWordsPath;
    private String itemsFilePath;

    /**
     *
     * @param filterredDictionary
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     * @throws ParseException
     */
    @Override
    public List<Term> disambiguateTerms(String filterredDictionary) throws IOException, FileNotFoundException, ParseException {
//        Logger.getLogger(DisambiguatorImpl.class.getName()).log(Level.INFO, "filterredDictionary: " + filterredDictionary);
        List<Term> terms = new ArrayList<>();
        String delimeter = ",";
        File dictionary = new File(filterredDictionary);
        int count = 0;
        int lineCount = 1;
        try (BufferedReader br = new BufferedReader(new FileReader(dictionary))) {
            for (String line; (line = br.readLine()) != null;) {
//                Logger.getLogger(DisambiguatorImpl.class.getName()).log(Level.INFO, "line: " + line);
                if (lineCount >= getLineOffset()) {
//                    Logger.getLogger(DisambiguatorImpl.class.getName()).log(Level.INFO, "Processing: " + line);
                    String[] parts = line.split(delimeter);
                    String term = parts[0];
//                Integer score = Integer.valueOf(parts[1]);
                    if (term.length() >= 1) {
                        count++;
                        if (count > getLimit()) {
                            break;
                        }
                        Term tt = getTerm(term);
                        if (tt != null) {
                            terms.add(tt);
                        }
                    }
                }
                lineCount++;
            }
        } catch (Exception ex) {
            Logger.getLogger(DisambiguatorImpl.class.getName()).log(Level.WARNING, "Failed while processing line " + lineCount, ex);
        } finally {
            return terms;
        }
    }

    @Override
    public void configure(Properties properties) {
        String numOfTerms = System.getProperty("num.of.terms");

        if (numOfTerms == null) {
            limit = Integer.valueOf(properties.getProperty("num.of.terms", "5"));
        } else {
            limit = Integer.valueOf(numOfTerms);
        }

        String offset = System.getProperty("offset.terms");

        if (offset == null) {
            lineOffset = Integer.valueOf(properties.getProperty("offset.terms", "1"));
        } else {
            lineOffset = Integer.valueOf(offset);
        }
        String minimumSimilarityStr = System.getProperty("minimum.similarity");
        if (minimumSimilarityStr == null) {
            minimumSimilarityStr = properties.getProperty("minimum.similarity", "0,3");
        }
        minimumSimilarity = Double.valueOf(minimumSimilarityStr);

        stopWordsPath = System.getProperty("stop.words.file");

        if (stopWordsPath == null) {
            stopWordsPath = properties.getProperty("stop.words.file", ".." + File.separator + "etc" + File.separator + "stopwords.csv");
        }

        itemsFilePath = System.getProperty("itemset.file");
        if (itemsFilePath == null) {
            itemsFilePath = properties.getProperty("itemset.file", ".." + File.separator + "etc" + File.separator + "itemset.csv");
        }

    }

    @Override
    public Term getTerm(String term) throws IOException, ParseException {
        Set<String> termsStr = getPossibleTermsFromDB(term);
        if (termsStr != null) {
            Set<Term> possibaleTerms = new HashSet<>();
            for (String jsonTerm : termsStr) {
                possibaleTerms.add(TermFactory.create(jsonTerm));
            }
            return disambiguate(term, possibaleTerms, termsStr, 0);
        } else {
            return null;
        }
    }

    /**
     * @return the limit
     */
    public Integer getLimit() {
        return limit;
    }

    /**
     * @return the minimumSimilarity
     */
    public Double getMinimumSimilarity() {
        return minimumSimilarity;
    }

    /**
     * @return the lineOffset
     */
    public Integer getLineOffset() {
        return lineOffset;
    }

    /**
     * @return the termToProcess
     */
    public String getTermToProcess() {
        return termToProcess;
    }

    /**
     * @param termToProcess the termToProcess to set
     */
    public void setTermToProcess(String termToProcess) {
        this.termToProcess = termToProcess;
    }

    @Override
    public Term call() throws Exception {
        return getTerm(getTermToProcess());
    }

    /**
     * @return the stopWordsPath
     */
    public String getStopWordsPath() {
        return stopWordsPath;
    }

    protected Term disambiguate(String term, Set<Term> possibleTerms, Set<String> ngarms, double minimumSimilarity) {

        return null;
    }

    private Set<Term> tf_idf_Disambiguation(Set<Term> possibleTerms, Set<String> nGrams, String lemma, double confidence, boolean matchTitle) throws IOException, ParseException {
        List<List<String>> allDocs = new ArrayList<>();
        Map<CharSequence, List<String>> docs = new HashMap<>();

        for (Term tv : possibleTerms) {
            Set<String> doc = getDocument(tv);
            allDocs.add(new ArrayList<>(doc));
            docs.put(tv.getUid(), new ArrayList<>(doc));
        }

        Set<String> contextDoc = new HashSet<>();
        for (String s : nGrams) {
            if (s.contains("_")) {
                String[] parts = s.split("_");
                for (String token : parts) {
                    if (token.length() >= 1 && !token.contains(lemma)) {
                        contextDoc.add(token);
                    }
                }
            } else if (s.length() >= 1 && !s.contains(lemma)) {
                contextDoc.add(s);
            }
        }
        docs.put("context", new ArrayList<>(contextDoc));

        Map<CharSequence, Map<String, Double>> featureVectors = new HashMap<>();
        for (CharSequence k : docs.keySet()) {
            List<String> doc = docs.get(k);
            Map<String, Double> featureVector = new TreeMap<>();
            for (String term : doc) {
                if (!featureVector.containsKey(term)) {
                    double tfidf = tfIdf(doc, allDocs, term);
                    featureVector.put(term, tfidf);
                }
            }
            featureVectors.put(k, featureVector);
        }

        Map<String, Double> contextVector = featureVectors.remove("context");

        Map<CharSequence, Double> scoreMap = new HashMap<>();
        for (CharSequence key : featureVectors.keySet()) {
            Double similarity = cosineSimilarity(contextVector, featureVectors.get(key));

            for (Term t : possibleTerms) {
                if (t.getUid().equals(key)) {

                    String stemTitle = stem(t.getLemma());
                    String stemLema = stem(lemma);
//                    List<String> subTokens = new ArrayList<>();
//                    if (!t.getLemma().toLowerCase().startsWith("(") && t.getLemma().toLowerCase().contains("(") && t.getLemma().toLowerCase().contains(")")) {
//                        int index1 = t.getLemma().toLowerCase().indexOf("(") + 1;
//                        int index2 = t.getLemma().toLowerCase().indexOf(")");
//                        String sub = t.getLemma().toLowerCase().substring(index1, index2);
//                        subTokens.addAll(tokenize(sub, true));
//                    }
//
//                    List<String> nTokens = new ArrayList<>();
//                    for (String s : nGrams) {
//                        if (s.contains("_")) {
//                            String[] parts = s.split("_");
//                            for (String token : parts) {
//                                nTokens.addAll(tokenize(token, true));
//                            }
//                        } else {
//                            nTokens.addAll(tokenize(s, true));
//                        }
//                    }
//                    if (t.getCategories() != null) {
//                        for (String s : t.getCategories()) {
//                            if (s != null && s.contains("_")) {
//                                String[] parts = s.split("_");
//                                for (String token : parts) {
//                                    subTokens.addAll(tokenize(token, true));
//                                }
//                            } else if (s != null) {
//                                subTokens.addAll(tokenize(s, true));
//                            }
//                        }
//                    }
////                    System.err.println(t.getGlosses());
//                    Set<String> intersection = new HashSet<>(nTokens);
//                    intersection.retainAll(subTokens);
//                    if (intersection.isEmpty()) {
//                        similarity -= 0.1;
//                    }
                    int dist = edu.stanford.nlp.util.StringUtils.editDistance(stemTitle, stemLema);
                    similarity = similarity - (dist * 0.05);
                    t.setConfidence(similarity);
                }
            }
            scoreMap.put(key, similarity);
        }

        if (scoreMap.isEmpty()) {
            return null;
        }

        ValueComparator bvc = new ValueComparator(scoreMap);
        TreeMap<CharSequence, Double> sorted_map = new TreeMap(bvc);
        sorted_map.putAll(scoreMap);
//        System.err.println(sorted_map);

        Iterator<CharSequence> it = sorted_map.keySet().iterator();
        CharSequence winner = it.next();

        Double s1 = scoreMap.get(winner);
        if (s1 < confidence) {
            return null;
        }

        Set<Term> terms = new HashSet<>();
        for (Term t : possibleTerms) {
            if (t.getUid().equals(winner)) {
                terms.add(t);
            }
        }
        if (!terms.isEmpty()) {
            return terms;
        } else {
            Logger.getLogger(DisambiguatorImpl.class.getName()).log(Level.INFO, "No winner");
            return null;
        }
    }

    private Set<String> getPossibleTermsFromDB(String term) {
        return null;
    }

    /**
     * @return the itemsFilePath
     */
    public String getItemsFilePath() {
        return itemsFilePath;
    }

    private static Set<String> getDocument(Term term) throws IOException, MalformedURLException, ParseException {

        Set<String> doc = new HashSet<>();

        List<CharSequence> g = term.getGlosses();
        if (g != null) {
            for (CharSequence s : g) {
                if (s != null) {
                    doc.addAll(tokenize(s, true));
                }
            }
        }
        List<CharSequence> al = term.getAltLables();
        if (al != null) {
            for (CharSequence s : al) {
                if (s != null) {
                    doc.addAll(tokenize(s, true));
                }
            }
        }
        List<CharSequence> cat = term.getCategories();
        if (cat != null) {
            for (CharSequence s : cat) {
                if (s != null) {
                    doc.addAll(tokenize(s, true));
                }
            }
        }
        return doc;
    }

    private double tfIdf(List<String> doc, List<List<String>> allDocs, String term) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Double cosineSimilarity(Map<String, Double> contextVector, Map<String, Double> get) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String stem(CharSequence lemma) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
