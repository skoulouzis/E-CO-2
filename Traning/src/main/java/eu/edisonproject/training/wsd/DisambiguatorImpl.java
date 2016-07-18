/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.edisonproject.training.wsd;

import eu.edisonproject.training.tfidf.mapreduce.ITFIDFDriver;
import eu.edisonproject.training.tfidf.mapreduce.TFIDFDriverImpl;
import eu.edisonproject.utility.commons.Term;
import eu.edisonproject.utility.commons.TermAvroSerializer;
import eu.edisonproject.utility.commons.TermFactory;
import eu.edisonproject.utility.commons.ValueComparator;
import eu.edisonproject.utility.file.CSVFileReader;
import eu.edisonproject.utility.text.processing.Cleaner;
import eu.edisonproject.utility.text.processing.Stemming;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.avro.file.DataFileReader;
import org.apache.avro.io.DatumReader;
import org.apache.avro.specific.SpecificDatumReader;

import org.json.simple.parser.ParseException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.ColumnPrefixFilter;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.ValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

/**
 *
 * @author S. Koulouzis
 */
public class DisambiguatorImpl implements Disambiguator, Callable {

    private Integer limit;
    private Double minimumSimilarity;
    private Integer lineOffset;
    private String termToProcess;
    private String stopWordsPath;
    private String itemsFilePath;
    private Connection conn;
    public static final TableName TERMS_TBL_NAME = TableName.valueOf("terms");

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

        File dictionary = new File(filterredDictionary);
        int count = 0;
        int lineCount = 1;
        try (BufferedReader br = new BufferedReader(new FileReader(dictionary))) {
            for (String line; (line = br.readLine()) != null;) {
//                Logger.getLogger(DisambiguatorImpl.class.getName()).log(Level.INFO, "line: " + line);
                if (lineCount >= getLineOffset()) {
//                    Logger.getLogger(DisambiguatorImpl.class.getName()).log(Level.INFO, "Processing: " + line);
                    String[] parts = line.split(",");
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

        Configuration config = HBaseConfiguration.create();
        try {
            conn = ConnectionFactory.createConnection(config);
        } catch (IOException ex) {
            Logger.getLogger(DisambiguatorImpl.class.getName()).log(Level.SEVERE, null, ex);
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
            String delimeter = ",";
            String wordSeperator = "_";
            Set<String> ngarms = CSVFileReader.getNGramsForTerm(term, getItemsFilePath(), delimeter, wordSeperator);
            return disambiguate(term, possibaleTerms, ngarms, getMinimumSimilarity());
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

    protected Term disambiguate(String term, Set<Term> possibleTerms, Set<String> ngarms, double minimumSimilarity) throws IOException, ParseException {
        String filePath = ".." + File.separator + "etc" + File.separator + "Avro Document" + File.separator + term + File.separator + term + ".avro";
        TermAvroSerializer ts = new TermAvroSerializer(filePath, Term.getClassSchema());
        List<CharSequence> empty = new ArrayList<>();
        empty.add("");
        Cleaner stemer = new Stemming();
        for (Term t : possibleTerms) {
            List<CharSequence> nuid = t.getNuids();
            if (nuid == null || nuid.isEmpty() || nuid.contains(null)) {
                t.setNuids(empty);
            }

            List<CharSequence> buids = t.getBuids();
            if (buids == null || buids.isEmpty() || buids.contains(null)) {
                t.setBuids(empty);
            }
            List<CharSequence> alt = t.getAltLables();
            if (alt == null || alt.isEmpty() || alt.contains(null)) {
                t.setAltLables(empty);
            }
            List<CharSequence> gl = t.getGlosses();
            if (gl == null || gl.isEmpty() || gl.contains(null)) {
                t.setGlosses(empty);
            } else {
                StringBuilder glosses = new StringBuilder();
                for (String n : ngarms) {
                    glosses.append(n).append(" ");
                }
                gl = new ArrayList<>();
                stemer.setDescription(glosses.toString());
                gl.add(stemer.execute());
                t.setGlosses(gl);

            }
            List<CharSequence> cat = t.getCategories();
            if (cat == null || cat.contains(null)) {
                t.setCategories(empty);
            }
            ts.serialize(t);
        }
        Term context = new Term();
        context.setUid("context");
        StringBuilder glosses = new StringBuilder();
        context.setLemma(term);
        context.setOriginalTerm(term);
        context.setUrl("empty");
        for (String n : ngarms) {
            glosses.append(n).append(" ");
        }
        List<CharSequence> contextGlosses = new ArrayList<>();
        stemer.setDescription(glosses.toString());

        contextGlosses.add(stemer.execute());
        context.setGlosses(contextGlosses);
        List<CharSequence> nuid = context.getNuids();
        if (nuid == null || nuid.isEmpty() || nuid.contains(null)) {
            context.setNuids(empty);
        }

        List<CharSequence> buids = context.getBuids();
        if (buids == null || buids.isEmpty() || buids.contains(null)) {
            context.setBuids(empty);
        }
        List<CharSequence> alt = context.getAltLables();
        if (alt == null || alt.isEmpty() || alt.contains(null)) {
            context.setAltLables(empty);
        }
        List<CharSequence> gl = context.getGlosses();
        if (gl == null || gl.isEmpty() || gl.contains(null)) {
            context.setGlosses(empty);
        }
        List<CharSequence> cat = context.getCategories();
        if (cat == null || cat.contains(null)) {
            context.setCategories(empty);
        }
        ts.serialize(context);
        ts.close();

        ITFIDFDriver tfidfDriver = new TFIDFDriverImpl(term);
        tfidfDriver.executeTFIDF(new File(filePath).getParent());

        Map<CharSequence, Map<String, Double>> featureVectors = CSVFileReader.tfidfResult2Map(TFIDFDriverImpl.OUTPUT_PATH4 + File.separator + "part-r-00000");
        Map<String, Double> contextVector = featureVectors.remove("context");

        Map<CharSequence, Double> scoreMap = new HashMap<>();
        for (CharSequence key : featureVectors.keySet()) {
            Double similarity = cosineSimilarity(contextVector, featureVectors.get(key));
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
        if (s1 < getMinimumSimilarity()) {
            return null;
        }

        Set<Term> terms = new HashSet<>();
        for (Term t : possibleTerms) {
            if (t.getUid().equals(winner)) {
                terms.add(t);
            }
        }
        if (!terms.isEmpty()) {
            return terms.iterator().next();
        } else {
            Logger.getLogger(DisambiguatorImpl.class.getName()).log(Level.INFO, "No winner");
            return null;
        }

//        possibleTerms = tf_idf_Disambiguation(possibleTerms, ngarms, term, getMinimumSimilarity(), true);
//        Term dis = null;
//        if (possibleTerms != null && possibleTerms.size() == 1) {
//            dis = possibleTerms.iterator().next();
//        }
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

//            Cleaner stemer = new Stemming();
//            for (Term t : possibleTerms) {
//                if (t.getUid().equals(key)) {
//                    stemer.setDescription(t.getLemma().toString());
//                    String stemTitle = stemer.execute();
//                    stemer.setDescription(lemma);
//                    String stemLema = stemer.execute();
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
//                    int dist = edu.stanford.nlp.util.StringUtils.editDistance(stemTitle, stemLema);
//                    similarity = similarity - (dist * 0.05);
//                    t.setConfidence(similarity);
//                }
//            }
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

    protected void addPossibleTermsToDB(String ambiguousTerm, Set<Term> terms) throws IOException {
        List<String> families = new ArrayList<>();
        families.add("jsonString");
        families.add("ambiguousTerm");
        createTable(TERMS_TBL_NAME, families);
        try (Admin admin = getConn().getAdmin()) {
            try (Table tbl = getConn().getTable(TERMS_TBL_NAME)) {
                for (Term t : terms) {
                    Put put = new Put(Bytes.toBytes(t.getUid().toString()));
                    String jsonStr = TermFactory.term2Json(t).toJSONString();
                    put.addColumn(Bytes.toBytes("jsonString"), Bytes.toBytes("jsonString"), Bytes.toBytes(jsonStr));
                    put.addColumn(Bytes.toBytes("ambiguousTerm"), Bytes.toBytes("ambiguousTerm"), Bytes.toBytes(ambiguousTerm));
                    tbl.put(put);
                }
            }
            admin.flush(TERMS_TBL_NAME);
        }
    }

    protected Set<String> getPossibleTermsFromDB(String term) throws IOException {
        try (Admin admin = getConn().getAdmin()) {
            if (admin.tableExists(TERMS_TBL_NAME)) {
                try (Table tbl = getConn().getTable(TERMS_TBL_NAME)) {
                    //shell query: 'scan 'terms', { COLUMNS => 'ambiguousTerm:ambiguousTerm', FILTER => "ValueFilter( =, 'binary:python' )" }'
                    Scan scan = new Scan();
                    scan.addFamily(Bytes.toBytes("ambiguousTerm"));
                    scan.addFamily(Bytes.toBytes("jsonString"));

//                    BinaryComparator bc = new BinaryComparator(Bytes.toBytes(term));
//                    Filter filter = new ValueFilter(CompareFilter.CompareOp.EQUAL, bc);
//                    scan.setFilter(filter);
                    ResultScanner resultScanner = tbl.getScanner(scan);
                    Iterator<Result> results = resultScanner.iterator();
                    Set<String> jsonTerms = new HashSet<>();
                    while (results.hasNext()) {
                        Result r = results.next();

                        String ambiguousTerm = Bytes.toString(r.getValue(Bytes.toBytes("ambiguousTerm"),
                                Bytes.toBytes("ambiguousTerm")));
                        if (ambiguousTerm.equals(term)) {
//                String uid = Bytes.toString(r.getRow());
                            String jsonStr = Bytes.toString(r.getValue(Bytes.toBytes("jsonString"),
                                    Bytes.toBytes("jsonString")));
                            jsonTerms.add(jsonStr);
                        }

                    }
                    return jsonTerms;
                }
            }
        }
        return null;
    }

    /**
     * @return the itemsFilePath
     */
    public String getItemsFilePath() {
        return itemsFilePath;
    }

    private static Set<String> getDocument(Term term) throws IOException, MalformedURLException, ParseException {
        Cleaner stemer = new Stemming();
        Set<String> doc = new HashSet<>();

        List<CharSequence> g = term.getGlosses();
        if (g != null) {
            for (CharSequence s : g) {
                if (s != null) {
                    stemer.setDescription(s.toString());
                    String stemed = stemer.execute();
                    doc.addAll(Arrays.asList(stemed.split(" ")));
                }
            }
        }
        List<CharSequence> al = term.getAltLables();
        if (al != null) {
            for (CharSequence s : al) {
                if (s != null) {
                    stemer.setDescription(s.toString());
                    String stemed = stemer.execute();
                    doc.addAll(Arrays.asList(stemed.split(" ")));
                }
            }
        }
        List<CharSequence> cat = term.getCategories();
        if (cat != null) {
            for (CharSequence s : cat) {
                if (s != null) {
                    stemer.setDescription(s.toString());
                    String stemed = stemer.execute();
                    doc.addAll(Arrays.asList(stemed.split(" ")));
                }
            }
        }
        return doc;
    }

    private double tfIdf(List<String> doc, List<List<String>> docs, String term) {
        return tf(doc, term) * idf(docs, term);
    }

    private static double idf(List<List<String>> docs, String term) {
        double n = 0;
        for (List<String> doc : docs) {
            for (String word : doc) {
                if (term.equalsIgnoreCase(word)) {
                    n++;
                    break;
                }
            }
        }
        if (n <= 0) {
            n = 1;
        }
        return Math.log(docs.size() / n);
    }

    private static double tf(List<String> doc, String term) {
        double result = 0;
        for (String word : doc) {
            if (term.equalsIgnoreCase(word)) {
                result++;
            }
        }
        return result / (double) doc.size();
    }

    //Code From org.apache.commons.text.similarity. 
    /**
     * Calculates the cosine similarity for two given vectors.
     *
     * @param leftVector left vector
     * @param rightVector right vector
     * @return cosine similarity between the two vectors
     */
    public static Double cosineSimilarity(Map<String, Double> leftVector, Map<String, Double> rightVector) {
        if (leftVector == null || rightVector == null) {
            throw new IllegalArgumentException("Vectors must not be null");
        }

        Set<String> intersection = getIntersection(leftVector, rightVector);

//        System.err.println(leftVector);
//        System.err.println(rightVector);
        double dotProduct = dot(leftVector, rightVector, intersection);
        double d1 = 0.0d;
        for (Double value : leftVector.values()) {
            d1 += Math.pow(value, 2);
        }
        double d2 = 0.0d;
        for (Double value : rightVector.values()) {
            d2 += Math.pow(value, 2);
        }
        double cosineSimilarity;
        if (d1 <= 0.0 || d2 <= 0.0) {
            cosineSimilarity = 0.0;
        } else {
            double a = Math.sqrt(d1) * Math.sqrt(d2);
            cosineSimilarity = (dotProduct / a);
        }
        return cosineSimilarity;

    }

    /**
     * Returns a set with strings common to the two given maps.
     *
     * @param leftVector left vector map
     * @param rightVector right vector map
     * @return common strings
     */
    private static Set<String> getIntersection(Map<String, Double> leftVector,
            Map<String, Double> rightVector) {

//        ValueComparator bvc = new ValueComparator(leftVector);
//        TreeMap<String, Double> Lsorted_map = new TreeMap(bvc);
//        Lsorted_map.putAll(leftVector);
//
//        bvc = new ValueComparator(rightVector);
//        TreeMap<String, Double> Rsorted_map = new TreeMap(bvc);
//        Rsorted_map.putAll(rightVector);
//
//        SortedSet<String> Lkeys = new TreeSet<>(leftVector.keySet());
//        SortedSet<String> Rkeys = new TreeSet<>(rightVector.keySet());
        Set<String> intersection = new HashSet<>(leftVector.keySet());
        intersection.retainAll(rightVector.keySet());
        return intersection;
    }

    /**
     * Computes the dot product of two vectors. It ignores remaining elements.
     * It means that if a vector is longer than other, then a smaller part of it
     * will be used to compute the dot product.
     *
     * @param leftVector left vector
     * @param rightVector right vector
     * @param intersection common elements
     * @return the dot product
     */
    private static double dot(Map<String, Double> leftVector, Map<String, Double> rightVector,
            Set<String> intersection) {
        Double dotProduct = 0.0;
        for (String key : intersection) {
            dotProduct += leftVector.get(key) * rightVector.get(key);
        }
        return dotProduct;
    }

    /**
     * @return the conn
     */
    public Connection getConn() {
        return conn;
    }

    protected void createTable(TableName tblName, List<String> families) throws IOException {
        try (Admin admin = getConn().getAdmin()) {
            if (!admin.tableExists(tblName)) {
                HTableDescriptor tableDescriptor = new HTableDescriptor(tblName);
                for (String f : families) {
                    HColumnDescriptor desc = new HColumnDescriptor(f);
                    tableDescriptor.addFamily(desc);
                }
                admin.createTable(tableDescriptor);
            }
        }
    }
}
