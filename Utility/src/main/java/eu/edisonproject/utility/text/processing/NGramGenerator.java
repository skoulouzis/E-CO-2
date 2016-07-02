/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.edisonproject.utility.text.processing;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

/**
 *
 * @author S. Koulouzis
 */
public class NGramGenerator extends Cleaner {

    @Override
    public String execute() {
        return null;
    }

//      public static List<String> getNGrams(String text, int maxNGrams) throws IOException {
//        List<String> words = new ArrayList<>();
//
//        Analyzer analyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);
//        TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(text));
//        StopFilter stopFilter = new StopFilter(Version.LUCENE_5_0_0, tokenStream, getStopWords());
////        stopFilter.setEnablePositionIncrements(false);
////        SnowballFilter snowballFilter = new SnowballFilter(stopFilter, "English");
//
//        try (ShingleFilter sf = new ShingleFilter(stopFilter, 2, maxNGrams)) {
//            sf.setOutputUnigrams(false);
//            CharTermAttribute charTermAttribute = sf.addAttribute(CharTermAttribute.class);
//            sf.reset();
//            while (sf.incrementToken()) {
//                String word = charTermAttribute.toString();
//                words.add(word.replaceAll(" ", "_"));
//            }
//            sf.end();
//        }
//        return words;
//    }
}
