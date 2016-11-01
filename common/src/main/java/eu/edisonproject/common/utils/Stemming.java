/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.common.utils;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/*
 * @author Michele Sparamonti
 */
public class Stemming extends Cleaner {

  @Override
  public String execute() {
    Tokenizer tokenizer = new StandardTokenizer();
    try {
      tokenizer.setReader(new InputStreamReader(new ByteArrayInputStream(this.getDescription().getBytes(StandardCharsets.UTF_8))));;
      StandardFilter standardFilter = new StandardFilter(tokenizer);
      String description;
      try (PorterStemFilter porterStemmingFilter = new PorterStemFilter(standardFilter)) {
        CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);
        porterStemmingFilter.reset();
        description = "";
        while (porterStemmingFilter.incrementToken()) {
          final String token = charTermAttribute.toString();
          description += token + " ";
        }
      }

      return description;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

}
