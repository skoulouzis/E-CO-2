package eu.edisonproject.classification.prepare.model;
/*
 * @author Michele Sparamonti
 */
public class Title extends Extractor{

	private String[] possibleTitleDS = {"data scientist","scientist data","ds", "data","scientist"};
	private String[] possibleTitleJDS = {"junior data scientist", "junior"};
	private String[] possibleTitleSDS = {"senior data scientis", "senior"};;
	
	public void extract() {
		/*if(this.getFilePath().charAt(0)=='_'){
			System.out.println("1");
			Document doc = Jsoup.parse(this.getJp().getDescription());
			Element link = doc.select("title").first();
			System.out.println(link.toString());
			String text = doc.body().text(); 
			System.out.println(text);
	
		}*/
		
		this.getJp().setTitle(normalizeTitle(this.getFilePath()));
                this.getJp().setDocumentId(normalizeId(this.getFilePath()));
	}
        
        public String normalizeId(String path){
            return path.substring(path.indexOf("_"));
        }
	
	public String normalizeTitle(String title){
		String normalized_title=title.toLowerCase();
		for(String s: possibleTitleSDS){
			if(normalized_title.contains(s)){
				return normalized_title = "Senior_Data_Scientist";
			}
		}
		
		for(String s: possibleTitleJDS){
			if(normalized_title.contains(s)){
				return normalized_title = "Junior_Data_Scientist";
			}
		}
		
		for(String s: possibleTitleDS){
			if(normalized_title.contains(s)){
				return normalized_title = "Data_Scientist";
			}
		}
		
		
		return normalized_title;
	}

}
