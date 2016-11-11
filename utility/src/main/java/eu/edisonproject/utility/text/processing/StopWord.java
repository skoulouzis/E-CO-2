package eu.edisonproject.utility.text.processing;

import java.io.IOException;
import java.io.StringReader;

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

public class StopWord extends Cleaner{

	private CharArraySet charArraySet;
	
	public StopWord(CharArraySet chs){
		this.setCharArraySet(chs);
	}
	
	public String execute() {
		
		String fullText = this.getDescription();
		
        // replace any punctuation char but apostrophes and dashes with a space
		fullText = fullText.replaceAll("((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)", " ");
		
		fullText = fullText.replaceAll("www.", "");
		fullText = fullText.replaceAll("http.", "");
		fullText = fullText.replaceAll("[^a-zA-Z ]", "");
    
		CharArraySet stopWords = this.getCharArraySet();
		StandardTokenizer stdToken = new StandardTokenizer();
	     try {
	    	 stdToken.setReader(new StringReader(fullText));
	
	    	 TokenStream tokenStream;

	    	 tokenStream = new StopFilter(new ASCIIFoldingFilter(new ClassicFilter(new LowerCaseFilter(stdToken))), stopWords);
	    	 tokenStream.reset();

	    	 CharTermAttribute token = tokenStream.getAttribute(CharTermAttribute.class);
	    	 String description ="";
	    	 while (tokenStream.incrementToken()) {
	    		 description += token.toString() + " ";
	    	 }
	    	 tokenStream.close();

	    	 return description;
	 	} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		

	}

	public CharArraySet getCharArraySet() {
		return charArraySet;
	}

	public void setCharArraySet(CharArraySet charArraySet) {
		this.charArraySet = charArraySet;
	}
	
	

	
	
	
}
