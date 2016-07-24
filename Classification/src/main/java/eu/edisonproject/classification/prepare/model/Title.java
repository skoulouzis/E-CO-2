/*
 * Copyright 2016 Michele Sparamonti & Spiros Koulouzis.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.edisonproject.classification.prepare.model;
/**
 * 
 * @author Michele Sparamonti (michele.sparamonti@eng.it)
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
