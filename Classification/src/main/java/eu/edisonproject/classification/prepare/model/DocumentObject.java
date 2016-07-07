package eu.edisonproject.classification.prepare.model;

import java.time.LocalDate;
/*
 * @author Michele Sparamonti
 */
public class DocumentObject {

	private String title;
	private LocalDate date;
	private String description;
	
	public DocumentObject(){
		//do nothing
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	
	
	
}
