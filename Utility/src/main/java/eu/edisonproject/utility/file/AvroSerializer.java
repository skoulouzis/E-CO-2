package eu.edisonproject.utility.file;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;

import jobpostdate.avro.Jobpost;

public class AvroSerializer {
	
	private DataFileWriter<Jobpost> dataFileWriter;

	public AvroSerializer(String file, Schema jpSchema){
		DatumWriter<Jobpost> jobpostDatumWriter = new SpecificDatumWriter<Jobpost>(Jobpost.class);
		dataFileWriter = new DataFileWriter<Jobpost>(jobpostDatumWriter);
		try{
			dataFileWriter.create(jpSchema, new File(file));
	
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void serialize(Jobpost jp){
		try {
			dataFileWriter.append(jp);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close(){
		try {
			dataFileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
