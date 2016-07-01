package eu.edisonproject.utility.text.processing;
/*
 * @author Michele Sparamonti
 */
public abstract class Cleaner {
	
	private String description ;
	
	public abstract String execute();

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
