package eu.edisonproject.classification.prepare.model;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/*
 * @author Michele Sparamonti
 */
public class Date extends Extractor{

	LocalDate date;
	
	public Date(){
		date = LocalDate.now();
	}
	
	public void extract() {
		/*Document doc = Jsoup.parse(this.getJp().getDescription());
		Element link = doc.select("date").first();
		String text = doc.body().text(); 
		System.out.println(text);
		 */
		if(this.getJp().getDescription().contains(" date")==true){
			int start = this.getJp().getDescription().indexOf(" date");
			try{
				date = LocalDate.parse(this.getJp().getDescription().substring(start, start+50));
				//System.out.println("F"+ date);
			}catch(DateTimeParseException e){
				//System.out.println("Exception date can't be parsed in LocalDate");
			}
		}
		
		this.getJp().setDate(date);
	}

	
}
