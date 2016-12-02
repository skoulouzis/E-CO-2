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
package eu.edisonproject.classification.tfidf.mapreduce;

import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Michele Sparamonti (michele.sparamonti@eng.it)
 */
public interface ITFIDFDriver {

  public void executeTFIDF(String inputPath, String outputPath, String competencesVectorPath, String propFilePath, Boolean useToolRunner) throws IOException;
}
