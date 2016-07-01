package eu.edisonproject.utility.file;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
/*
 * @author Michele Sparamonti
 */
public class ReaderFile  extends FileIO{

	public ReaderFile(String filePath) {
		super(filePath);
	}
	
	public String readFile(){
		String text="";
		String line=null;
		try{
			BufferedReader br = new BufferedReader(
					new InputStreamReader(new FileInputStream(this.getFilePath())));
			while((line=br.readLine())!=null)
				text+=line+" ";
			br.close();
		}catch(IOException e){
			System.out.println("Can't read the following file: "+this.getFilePath());
		}
		return text.toLowerCase();
	}
	
	public String readFileWithN(){
		String text="";
		String line=null;
		try{
			BufferedReader br = new BufferedReader(
					new InputStreamReader(new FileInputStream(this.getFilePath())));
			while((line=br.readLine())!=null)
				text+=line+"\n";
			br.close();
		}catch(IOException e){
			System.out.println("Can't read the following file: "+this.getFilePath());
		}
		return text;
	}
	
	

}
