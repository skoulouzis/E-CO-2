/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.edisonproject.utility.text.processing;

import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 *
 * @author S. Koulouzis
 */
public class NGramGenerator extends Cleaner {

    private final int maxNGrams;
    private final CharArraySet stopwords;

    public NGramGenerator(CharArraySet stopwords, int maxNGrams) {
        this.stopwords = stopwords;
        this.maxNGrams = maxNGrams;
    }

    @Override
    public String execute() {
        try {
            return getNGrams();
        } catch (IOException ex) {
            Logger.getLogger(NGramGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private String getNGrams() throws IOException {
//        List<String> words = new ArrayList<>();

        Analyzer analyzer = new StandardAnalyzer(stopwords);
        TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(getDescription()));
        StopFilter stopFilter = new StopFilter(tokenStream, stopwords);
        StringBuilder words = new StringBuilder();
        try (ShingleFilter sf = new ShingleFilter(stopFilter, 2, maxNGrams)) {
            sf.setOutputUnigrams(false);
            CharTermAttribute charTermAttribute = sf.addAttribute(CharTermAttribute.class);
            sf.reset();
            while (sf.incrementToken()) {
                String word = charTermAttribute.toString();
                word = word.replaceAll("_", " ");
                word = word.replaceAll("\\s{2,}", " ");
                word = word.replaceAll(" ", "_");
                words.append(word).append(" ");
            }
            sf.end();
        }
        words.deleteCharAt(words.length() - 1);
        words.setLength(words.length());

        return words.toString();
    }
}
