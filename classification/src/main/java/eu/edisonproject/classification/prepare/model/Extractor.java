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

import eu.edisonproject.utility.file.ReaderFile;

/**
 * 
 * @author Michele Sparamonti (michele.sparamonti@eng.it)
 */
public abstract class Extractor {

	private String filePath;
	private String text;
	private DocumentObject jp;
	
	public abstract void extract();
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public void readFromFile(){
		ReaderFile rf = new ReaderFile(this.getFilePath());
		this.getJp().setDescription(rf.readFileWithN());
	}
	

	public DocumentObject getJp() {
		return jp;
	}
	public void setJp(DocumentObject jp) {
		this.jp = jp;
	}
	
}
