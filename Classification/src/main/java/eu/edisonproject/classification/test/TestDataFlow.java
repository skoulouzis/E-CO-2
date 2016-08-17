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
package eu.edisonproject.classification.test;

import eu.edisonproject.classification.flow.model.DataFlow;
import eu.edisonproject.classification.flow.model.IDataFlow;
import java.io.File;
/**
 * 
 * @author Michele Sparamonti (michele.sparamonti@eng.it)
 */
public class TestDataFlow {

	public static void execute(String[] args){
		IDataFlow idf = new DataFlow();
                String inputFolder = args[0]+File.separator+"etc"+File.separator+"Classification"+File.separator+"Job Post"+File.separator;
		String outputFolder = args[0]+File.separator+"etc"+File.separator+"Classification"+File.separator+"Avro Document"+File.separator;
                //String stopWordPath = ".."+File.separator+"etc"+File.separator+"stopwords.csv";
                System.out.println("TEST DATA FLOW");
                System.out.println(inputFolder);
                idf.dataPreProcessing(inputFolder,outputFolder);
		
	}
	
}
