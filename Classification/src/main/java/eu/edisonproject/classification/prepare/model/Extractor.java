package eu.edisonproject.classification.prepare.model;

import eu.edisonproject.utility.file.ReaderFile;

/*
 * @author Michele Sparamonti
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
