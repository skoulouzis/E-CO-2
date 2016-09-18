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
package eu.edisonproject.utility.file;

import eu.edisonproject.utility.text.processing.Cleaner;
import eu.edisonproject.utility.text.processing.Stemming;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author S. Koulouzis
 */
public class CSVFileReader {

    private static Map<String, Set<String>> nGramsMap;
    private static final Cleaner STEMER = new Stemming();

    public static Map<String, Collection<Double>> csvCompetanceToMap(String csvFilePath, String delimeter, Boolean hasHeader) throws IOException {
        Map<String, Collection<Double>> map = new HashMap<>();
        boolean passed = false;
        try (BufferedReader br = new BufferedReader(new FileReader(new File(csvFilePath)))) {
            for (String text; (text = br.readLine()) != null;) {
                if (hasHeader && !passed) {
                    passed = true;
                    continue;
                }
                String[] parts = text.split(delimeter);
                String id = parts[0];
                List<Double> vals = new ArrayList<>();
                for (int i = 1; i < parts.length; i++) {
                    vals.add(Double.parseDouble(parts[i]));
                }
                map.put(id, vals);
            }
        }
        return map;
    }

    public static Map<String, String> csvFileToMap(String csvFilePath, String delimeter) throws IOException {
        Map<String, String> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(csvFilePath)))) {
            for (String text; (text = br.readLine()) != null;) {
                if (text.contains(delimeter)) {
                    String[] parts = text.split(delimeter);
                    map.put(parts[0], parts[1]);
                } else {
                    map.put(text, "0");
                }

            }
        }
        return map;
    }

    public static Map<String, Double> csvFileToMapDoubleValue(String csvFilePath, String delimeter) throws IOException {
        Map<String, Double> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(csvFilePath)))) {
            for (String text; (text = br.readLine()) != null;) {
                if (text.contains(delimeter)) {
                    String[] parts = text.split(delimeter);
                    map.put(parts[0], Double.valueOf(parts[1]));
                } else {
                    map.put(text, 0.0);
                }

            }
        }
        return map;
    }

    public static Map<CharSequence, Map<String, Double>> tfidfResult2Map(String path) throws IOException {
        Map<CharSequence, Map<String, Double>> featureVectors = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(path)))) {
            for (String text; (text = br.readLine()) != null;) {
                String[] parts = text.split("\t");
                String id = parts[0];
                String[] vector = parts[1].split("/");
                Map<String, Double> featureVector = new TreeMap<>();
                for (int i = 0; i < vector.length / 2; i++) {
                    String[] termValuePair = vector[i].split(":");
                    featureVector.put(termValuePair[0], Double.valueOf(termValuePair[1]));
                }
                featureVectors.put(id, featureVector);
            }
        }
        return featureVectors;
    }

    public static Set<String> getNGramsForTerm(String term, String itemsFilePath, String delimeter, String wordSeperator) throws IOException {
        if (nGramsMap == null) {
            nGramsMap = new HashMap<>();
        }
        Set<String> nGrams = nGramsMap.get(term);
        if (nGrams != null) {
            return nGrams;
        }
        nGrams = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(itemsFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String keyword;
                if (line.contains(delimeter)) {
                    keyword = line.split(delimeter)[0];
                } else {
                    keyword = line;
                }
//                boolean found = false;
                STEMER.setDescription(keyword.replaceAll("_", " "));
                String stemKeyword = STEMER.execute();

                STEMER.setDescription(term.replaceAll("_", " "));
                String stemTerm = STEMER.execute().trim();

                if (keyword.contains(wordSeperator) && stemKeyword.contains(stemTerm)) {
//                    String[] parts = stemKeyword.split(" ");
//                    StringBuilder sb = new StringBuilder();
//                    int i = 0;
//                    for (String p : parts) {
//                        if (p.equals(stemTerm)) {
//                            found = true;
//                            break;
//                        }
//                        if (i > 0 && i < parts.length - 1) {
//                            sb.append(" ");
//                        }
//                        sb.append(p);
//                        if (sb.toString().contains(stemTerm)) {
//                            found = true;
//                            break;
//                        }
//                        i++;
//                    }
//                    if (found) {
                    String[] parts = keyword.replaceAll(term, "").split(wordSeperator);
                    for (String p : parts) {
                        if (p.length() > 0) {
                            nGrams.add(p);
                        }
                    }
//                    }
                }
            }
        }
        nGramsMap.put(term, nGrams);
        return nGrams;
    }
}
