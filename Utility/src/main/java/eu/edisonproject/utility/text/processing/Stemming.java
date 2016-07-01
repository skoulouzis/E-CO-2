package eu.edisonproject.utility.text.processing;

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
public class Stemming extends Cleaner{

	public String execute() {
		Tokenizer tokenizer = new StandardTokenizer();
		try {
			tokenizer.setReader(new InputStreamReader(new ByteArrayInputStream(this.getDescription().getBytes(StandardCharsets.UTF_8))));;
			StandardFilter standardFilter = new StandardFilter(tokenizer);
		    PorterStemFilter porterStemmingFilter = new PorterStemFilter(standardFilter);
		    
		    
		    
		    CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);
		    porterStemmingFilter.reset();
		    String description="";
		    while(porterStemmingFilter.incrementToken()) {
			    final String token = charTermAttribute.toString().toString();
			    description += token + " ";
			}
			porterStemmingFilter.close();

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
