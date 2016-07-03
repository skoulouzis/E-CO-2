/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.edisonproject.traning.wsd;

import eu.edisonproject.traning.utility.term.avro.Term;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.parser.ParseException;

/**
 *
 * @author S. Koulouzis
 */
public class DisambiguatorImpl implements Disambiguator, Callable {

    private Integer limit;
    private Double minimumSimilarity;
    private Integer lineOffset;
    private String cachePath;
    private String allTermsDictionaryPath;
    private String termToProcess;
    private String stopWordsPath;

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
        minimumSimilarity = Double.valueOf(properties.getProperty("minimum.similarity", "0,3"));
        this.cachePath = (properties.getProperty("cache.path"));
        allTermsDictionaryPath = properties.getProperty("all.terms.dictionary.path");

        stopWordsPath = System.getProperty("stop.words.file");

        if (getStopWordsPath() == null) {
            stopWordsPath = properties.getProperty("stop.words.file", ".." + File.separator + "etc" + File.separator + "stopwords.csv");
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
     * @return the cachePath
     */
    public String getCachePath() {
        return cachePath;
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
     * @return the allTermsDictionaryPath
     */
    public String getAllTermsDictionaryPath() {
        return allTermsDictionaryPath;
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

    private Set<String> getPossibleTermsFromDB(String term) {
        return null;
    }
}
