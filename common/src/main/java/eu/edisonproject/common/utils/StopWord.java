/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.common.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.ClassicFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;


/*
 * @author Michele Sparamonti
 */
public final class StopWord extends Cleaner {

  private CharArraySet charArraySet;

  public StopWord(CharArraySet chs) {
    this.setCharArraySet(chs);
  }

  @Override
  public String execute() {

    try {
      String fullText = this.getDescription();

      // replace any punctuation char but apostrophes and dashes with a space
      fullText = fullText.replaceAll("((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)", " ");

      fullText = fullText.replaceAll("www.", "");
      fullText = fullText.replaceAll("http.", "");
      fullText = fullText.replaceAll("[^a-zA-Z ]", "");

      CharArraySet stopWords = this.getCharArraySet();
      StandardTokenizer stdToken = new StandardTokenizer();

      stdToken.setReader(new StringReader(fullText));

      TokenStream tokenStream;

      tokenStream = new StopFilter(new ASCIIFoldingFilter(new ClassicFilter(new LowerCaseFilter(stdToken))), stopWords);
      tokenStream.reset();

      CharTermAttribute token = tokenStream.getAttribute(CharTermAttribute.class);
      String description = "";
      while (tokenStream.incrementToken()) {
        description += token.toString() + " ";
      }
      tokenStream.close();

      return description;
    } catch (IOException ex) {
      Logger.getLogger(StopWord.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public CharArraySet getCharArraySet() {
    return charArraySet;
  }

  public void setCharArraySet(CharArraySet charArraySet) {
    this.charArraySet = charArraySet;
  }

}
