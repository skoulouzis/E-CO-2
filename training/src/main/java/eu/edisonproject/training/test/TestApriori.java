/*
 * Copyright 2016 Michele Sparamonti & Spiros Koulouzis
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

package eu.edisonproject.training.test;
/**
 *
 * @author Michele Sparamonti (michele.sparamonti@eng.it)
 */ 


import java.io.File;

import eu.edisonproject.training.context.corpus.DataPrepare;

public class TestApriori {

	public static void main(String[] args) {
		String absolutePath = ".."+File.separator+"documentation";
		String inputPath =  ".."+File.separator+"documentation"+File.separator+"Apriori documents"+File.separator;
		String outputPath = ".."+File.separator+"etc"+File.separator+"Training"+File.separator;
		String stopwordPath = ".."+File.separator+"etc"+File.separator+"stopwords.csv";
		DataPrepare dataPrepare = new DataPrepare(inputPath,outputPath,stopwordPath);
		dataPrepare.execute();
	}

}
