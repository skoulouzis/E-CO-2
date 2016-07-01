package eu.edisonproject.classification;

/**
 *
 * Copyright 2016 Michele Sparamonti & Spiros Koulouzis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
*/
import java.io.File;
import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;

public class JobPostAvroSerializer {

	public class AvroSerializer {

		private DataFileWriter<Jobpost> dataFileWriter;

		public AvroSerializer(String file, Schema jpSchema) {
			DatumWriter<Jobpost> jobpostDatumWriter = new SpecificDatumWriter<Jobpost>(Jobpost.class);
			dataFileWriter = new DataFileWriter<Jobpost>(jobpostDatumWriter);
			try {
				dataFileWriter.create(jpSchema, new File(file));

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void serialize(Jobpost jp) {
			try {
				dataFileWriter.append(jp);
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
}
