/*
 * Copyright 2016 S. Koulouzis.
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

package eu.edisonproject.common.utils;

import eu.edisonproject.common.utils.file.ReaderFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 *
 * @author S. Koulouzis
 */
public class ConfigHelper {

  public static CharArraySet loadStopWords(String stopWordsPath) {
    ReaderFile fileReader = new ReaderFile(stopWordsPath);
    String[] stopWord = fileReader.readFile().split(" ");
    final List<String> stopWords = Arrays.asList(stopWord);

    return new CharArraySet(stopWords, false);
  }

  public static CharArraySet loadStopWords(InputStream is) throws IOException {
    String line;
    List<String> stopWords = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(
            new InputStreamReader(is))) {
      while ((line = br.readLine()) != null) {
        stopWords.add(line);
      }
    }

    return new CharArraySet(stopWords, false);
  }

  public static MyProperties getProperties(String propertiesPath) throws IOException {
    Logger.getLogger(ConfigHelper.class.getName()).log(Level.INFO, "Reading properties from: {0}", propertiesPath);
    InputStream in = null;
    try {
      if (new File(propertiesPath).exists() && new File(propertiesPath).isFile()) {
        in = new FileInputStream(propertiesPath);
      } else {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        in = classLoader.getResourceAsStream(propertiesPath);
      }
      MyProperties properties = new MyProperties();
      properties.load(in);
      return properties;
    } catch (IOException ex) {
      Logger.getLogger(ConfigHelper.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      if (in != null) {
        in.close();
      }
    }
    return null;
  }
}
