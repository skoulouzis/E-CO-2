package eu.edisonproject.classification.test;

import eu.edisonproject.classification.flow.model.DataFlow;
import eu.edisonproject.classification.flow.model.IDataFlow;
import java.io.File;

public class TestDataFlow {

	public static void main(String[] args){
		IDataFlow idf = new DataFlow();
                String inputFolder = ".."+File.separator+"etc"+File.separator+"Job post"+File.separator;
		String outputFolder = ".."+File.separator+"etc"+File.separator+"Avro Document"+File.separator;
                //String stopWordPath = ".."+File.separator+"etc"+File.separator+"stopwords.csv";
                idf.dataPreProcessing(inputFolder,outputFolder);
		
	}
	
}
