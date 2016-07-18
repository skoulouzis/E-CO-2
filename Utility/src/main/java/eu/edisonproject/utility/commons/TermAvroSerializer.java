package eu.edisonproject.utility.commons;

import java.io.File;
import java.io.IOException;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;



public class TermAvroSerializer {

    private DataFileWriter<Term> dataFileWriter;

    public TermAvroSerializer(String file, Schema termSchema) {
        DatumWriter<Term> jobpostDatumWriter = new SpecificDatumWriter<>(Term.class);
        dataFileWriter = new DataFileWriter<>(jobpostDatumWriter);
        try {
            dataFileWriter.create(termSchema, new File(file));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void serialize(Term term) {
        try {
            dataFileWriter.append(term);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            dataFileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
