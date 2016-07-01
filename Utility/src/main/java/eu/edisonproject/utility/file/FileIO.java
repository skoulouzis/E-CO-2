package eu.edisonproject.utility.file;
/*
 * @author Michele Sparamonti
 */
public abstract class FileIO {

	private String filePath;
	
	public FileIO(String filePath){
		this.setFilePath(filePath);
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
}
