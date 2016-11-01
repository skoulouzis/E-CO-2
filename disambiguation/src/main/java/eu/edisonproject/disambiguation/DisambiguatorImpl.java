/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.disambiguation;

import eu.edisonproject.common.Term;
import eu.edisonproject.common.TermFactory;
import eu.edisonproject.common.ValueComparator;
import eu.edisonproject.common.utils.ConfigHelper;
import eu.edisonproject.common.utils.StanfordLemmatizer;
import eu.edisonproject.common.utils.Stemming;
import eu.edisonproject.common.utils.StopWord;
import eu.edisonproject.common.utils.file.CSVFileReader;
import eu.edisonproject.common.utils.file.DBTools;
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
import java.util.concurrent.ExecutionException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.parser.ParseException;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.filter.ValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 *
 * @author S. Koulouzis
 */
public class DisambiguatorImpl implements Disambiguator, Callable {

  private Integer limit;
  private Double minimumSimilarity;
  private Integer lineOffset;
  private String termToProcess;
  private static String stopWordsPath;
  private String itemsFilePath;
  public static final TableName TERMS_TBL_NAME = TableName.valueOf("terms");
  protected StopWord tokenizer;
  protected StanfordLemmatizer lematizer;
  protected Stemming stemer;
  private static final Logger LOGGER = Logger.getLogger(DisambiguatorImpl.class.getName());
  private String candidateTermsFile;

  /**
   *
   * @param candidateTermsFile
   * @return
   * @throws IOException
   * @throws FileNotFoundException
   * @throws ParseException
   */
  @Override
  public List<Term> disambiguateTerms(String candidateTermsFile) throws IOException, FileNotFoundException, ParseException {
    LOGGER.log(Level.FINE, "filterredDictionary: {0}", candidateTermsFile);
    this.candidateTermsFile = candidateTermsFile;
    List<Term> terms = new ArrayList<>();

    File dictionary = new File(candidateTermsFile);
    int count = 0;
    int lineCount = 1;
    try (BufferedReader br = new BufferedReader(new FileReader(dictionary))) {
      for (String line; (line = br.readLine()) != null;) {
//                LOGGER.log(Level.FINE, "line: {0}", line);
        if (lineCount >= getLineOffset()) {

          String[] parts = line.split(",");
          String term = parts[0];
//                Integer score = Integer.valueOf(parts[1]);
          if (term.length() >= 1) {
            count++;
            if (count > getLimit()) {
              break;
            }
            LOGGER.log(Level.INFO, "Processing: {0}  at line: {1} of " + getLimit(), new Object[]{line, lineCount});
            Term tt = getTerm(term);
            if (tt != null) {
              terms.add(tt);
            }
          }
        }
        lineCount++;
      }
    } catch (Exception ex) {
      LOGGER.log(Level.WARNING, "Failed while processing line: " + lineCount + " from: " + candidateTermsFile, ex);
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
    LOGGER.log(Level.FINE, "num.of.terms: {0}", limit);

    String offset = System.getProperty("offset.terms");

    if (offset == null) {
      lineOffset = Integer.valueOf(properties.getProperty("offset.terms", "1"));
    } else {
      lineOffset = Integer.valueOf(offset);
    }
    LOGGER.log(Level.FINE, "offset.terms: {0}", lineOffset);
    String minimumSimilarityStr = System.getProperty("minimum.similarity");
    if (minimumSimilarityStr == null) {
      minimumSimilarityStr = properties.getProperty("minimum.similarity", "0,3");
    }
    minimumSimilarity = Double.valueOf(minimumSimilarityStr);
    LOGGER.log(Level.FINE, "minimum.similarity: {0}", lineOffset);
    stopWordsPath = System.getProperty("stop.words.file");

    if (stopWordsPath == null) {
      stopWordsPath = properties.getProperty("stop.words.file", ".." + File.separator + "etc" + File.separator + "stopwords.csv");
    }
    LOGGER.log(Level.FINE, "stop.words.file: {0}", stopWordsPath);
    itemsFilePath = System.getProperty("itemset.file");
    if (itemsFilePath == null) {
      itemsFilePath = properties.getProperty("itemset.file", ".." + File.separator + "etc" + File.separator + "dictionaryAll.csv");
    }
    LOGGER.log(Level.FINE, "itemset.file: {0}", itemsFilePath);
//        Configuration config = HBaseConfiguration.create();
//        try {
//            conn = ConnectionFactory.createConnection(config);
//        } catch (IOException ex) {
//            LOGGER.log(Level.SEVERE, null, ex);
//        }
    CharArraySet stopwordsCharArray = new CharArraySet(ConfigHelper.loadStopWords(stopWordsPath), true);
    tokenizer = new StopWord(stopwordsCharArray);
    lematizer = new StanfordLemmatizer();
    stemer = new Stemming();

//        try {
//            String logPropFile = properties.getProperty("logging.properties.file", ".." + File.separator + "etc" + File.separator + "log.properties");
//            FileInputStream fis = new FileInputStream(logPropFile);
//            LogManager.getLogManager().readConfiguration(fis);
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(DisambiguatorImpl.class.getName()).log(Level.WARNING, null, ex);
//        } catch (IOException | SecurityException ex) {
//            Logger.getLogger(DisambiguatorImpl.class.getName()).log(Level.WARNING, null, ex);
//        }
    Level level = Level.parse(properties.getProperty("log.level", "INFO"));

    Handler[] handlers
            = Logger.getLogger("").getHandlers();
    for (int index = 0; index < handlers.length; index++) {
      handlers[index].setLevel(level);
    }
    LOGGER.setLevel(level);
  }

  @Override
  public Term getTerm(String term) throws IOException, ParseException {
    Set<String> termsStr = getPossibleTermsFromDB(term, null);
    if (termsStr != null) {
      Set<Term> possibaleTerms = new HashSet<>();
      for (String jsonTerm : termsStr) {
        possibaleTerms.add(TermFactory.create(jsonTerm));
      }
      String delimeter = ",";
      String wordSeperator = " ";
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
    Set<Term> dis = tf_idf_Disambiguation(possibleTerms, ngarms, term, getMinimumSimilarity(), true);
    if (dis != null) {
      return dis.iterator().next();
    }
    return null;
  }

  private Set<Term> tf_idf_Disambiguation(Set<Term> possibleTerms, Set<String> nGrams, String lemma, double confidence, boolean matchTitle) throws IOException, ParseException {
    LOGGER.log(Level.FINE, "Loaded {0} for {1}", new Object[]{nGrams.size(), lemma});
    if (nGrams.size() < 7) {
      LOGGER.log(Level.WARNING, "Found only {0} n-grams for {1}. Not enough for disambiguation.", new Object[]{nGrams.size(), lemma});
      return null;
    }

    List<List<String>> allDocs = new ArrayList<>();
    Map<CharSequence, List<String>> docs = new HashMap<>();

    for (Term tv : possibleTerms) {
      Set<String> doc = getDocument(tv);
      allDocs.add(new ArrayList<>(doc));
      docs.put(tv.getUid(), new ArrayList<>(doc));
    }

    Set<String> contextDoc = new HashSet<>();
    StringBuilder ngString = new StringBuilder();
    for (String s : nGrams) {
      if (s.contains("_")) {
        String[] parts = s.split("_");
        for (String token : parts) {
          if (token.length() >= 1 && !token.contains(lemma)) {
//                        contextDoc.add(token);
            ngString.append(token).append(" ");
          }
        }
      } else if (s.length() >= 1 && !s.contains(lemma)) {
        ngString.append(s).append(" ");
//                contextDoc.add(s);
      }
    }
    tokenizer.setDescription(ngString.toString());
    String cleanText = tokenizer.execute();
    lematizer.setDescription(cleanText);
    String lematizedText = lematizer.execute();
    List<String> ngList = Arrays.asList(lematizedText.split(" "));
    contextDoc.addAll(ngList);

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
        if (t.getUid().equals(key) && matchTitle) {
          stemer.setDescription(t.getLemma().toString());
          String stemTitle = stemer.execute();
          stemer.setDescription(lemma);
          String stemLema = stemer.execute();
//                    List<String> subTokens = new ArrayList<>();
//                    if (!t.getLemma().toString().toLowerCase().startsWith("(") && t.getLemma().toString().toLowerCase().contains("(") && t.getLemma().toLowerCase().contains(")")) {
//                        int index1 = t.getLemma().toString().toLowerCase().indexOf("(") + 1;
//                        int index2 = t.getLemma().toString().toLowerCase().indexOf(")");
//                        String sub = t.getLemma().toString().toLowerCase().substring(index1, index2);
//                        subTokens.addAll(tokenize(sub, true));
//                    }
          double factor = 0.15;
          if (stemTitle.length() > stemLema.length()) {
            if (stemTitle.contains(stemLema)) {
              factor = 0.075;
            }
          } else if (stemLema.length() > stemTitle.length()) {
            if (stemLema.contains(stemTitle)) {
              factor = 0.075;
            }
          }
          int dist = edu.stanford.nlp.util.StringUtils.editDistance(stemTitle, stemLema);
          similarity = similarity - (dist * factor);
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
      LOGGER.log(Level.INFO, "No winner");
      return null;
    }
  }

  protected void addPossibleTermsToDB(String ambiguousTerm, Set<Term> terms) throws IOException {
    List<String> families = new ArrayList<>();
    families.add("jsonString");
    families.add("ambiguousTerm");
    DBTools.createOrUpdateTable(TERMS_TBL_NAME, families, false);
    try (Admin admin = DBTools.getConn().getAdmin()) {
      try (Table tbl = DBTools.getConn().getTable(TERMS_TBL_NAME)) {
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

  protected Set<String> getPossibleTermsFromDB(String term, CharSequence url) throws IOException {
    try (Admin admin = DBTools.getConn().getAdmin()) {
      if (admin.tableExists(TERMS_TBL_NAME)) {
        try (Table tbl = DBTools.getConn().getTable(TERMS_TBL_NAME)) {
          //shell query: 'scan 'terms', { COLUMNS => 'ambiguousTerm:ambiguousTerm', FILTER => "ValueFilter( =, 'binary:python' )" }'
          Scan scan = new Scan();
//                    scan.addFamily(Bytes.toBytes("ambiguousTerm"));
//                    scan.addFamily(Bytes.toBytes("jsonString"));

          List<Filter> filterList = new ArrayList<>();
          ValueFilter valueFilter = new ValueFilter(CompareOp.EQUAL, new SubstringComparator(term));
          filterList.add(valueFilter);

          FilterList filter = new FilterList(FilterList.Operator.MUST_PASS_ALL, filterList);

          scan.setFilter(filter);

          ResultScanner resultScanner = tbl.getScanner(scan);
          Iterator<Result> results = resultScanner.iterator();
          Set<String> jsonTerms = new HashSet<>();
          while (results.hasNext()) {
            Result r = results.next();
            String ambiguousTerm = Bytes.toString(r.getValue(Bytes.toBytes("ambiguousTerm"),
                    Bytes.toBytes("ambiguousTerm")));
            String jsonStr = Bytes.toString(r.getValue(Bytes.toBytes("jsonString"),
                    Bytes.toBytes("jsonString")));
            if (jsonStr != null) {
              if (url != null) {
                if (ambiguousTerm != null && ambiguousTerm.equals(term) && jsonStr.contains(url)) {
                  jsonTerms.add(jsonStr);
                }
              } else if (ambiguousTerm != null && ambiguousTerm.equals(term)) {
                jsonTerms.add(jsonStr);
              }
            } else {
              deleteEntryFromTerms(r.getRow());
            }

          }
          return jsonTerms;
        }
      }
    }
    return null;
  }

  private void deleteEntryFromTerms(byte[] id) throws IOException {
    try (Table tbl = DBTools.getConn().getTable(TERMS_TBL_NAME)) {
      Delete d = new Delete(id);
      tbl.delete(d);
    }
  }

  /**
   * @return the itemsFilePath
   */
  public String getItemsFilePath() {
    return itemsFilePath;
  }

  private Set<String> getDocument(Term term) throws IOException, MalformedURLException, ParseException {
    Set<String> doc = new HashSet<>();

    List<CharSequence> g = term.getGlosses();
    if (g != null) {
      for (CharSequence s : g) {
        if (s != null && s.length() > 0) {
          s = s.toString().replaceAll("_", " ");
          tokenizer.setDescription(s.toString());
          String cleanText = tokenizer.execute();
          lematizer.setDescription(cleanText);
          String lematizedText = lematizer.execute();

//                    cleaner.setDescription(s.toString());
//                    String stemed = cleaner.execute();
          doc.addAll(Arrays.asList(lematizedText.split(" ")));
        }
      }
    }
    List<CharSequence> al = term.getAltLables();
    if (al != null) {
      for (CharSequence s : al) {
        if (s != null && s.length() > 0) {
//                    cleaner.setDescription(s.toString());
//                    String stemed = cleaner.execute();
          s = s.toString().replaceAll("_", " ");
          tokenizer.setDescription(s.toString());
          String cleanText = tokenizer.execute();
          lematizer.setDescription(cleanText);
          String lematizedText = lematizer.execute();

          doc.addAll(Arrays.asList(lematizedText.split(" ")));
        }
      }
    }
    List<CharSequence> cat = term.getCategories();
    if (cat != null) {
      for (CharSequence s : cat) {
        if (s != null && s.length() > 0) {
          s = s.toString().replaceAll("_", " ");
//                    cleaner.setDescription(s.toString());
//                    String stemed = cleaner.execute();
          tokenizer.setDescription(s.toString());
          String cleanText = tokenizer.execute();
          lematizer.setDescription(cleanText);
          String lematizedText = lematizer.execute();
          doc.addAll(Arrays.asList(lematizedText.split(" ")));
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
   * Computes the dot product of two vectors. It ignores remaining elements. It
   * means that if a vector is longer than other, then a smaller part of it will
   * be used to compute the dot product.
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

  @Override
  public Set<Term> getCandidates(String lemma) throws MalformedURLException, IOException, ParseException, InterruptedException, ExecutionException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  /**
   * @return the candidateTermsFile
   */
  public String getCandidateTermsFile() {
    return candidateTermsFile;
  }

}
