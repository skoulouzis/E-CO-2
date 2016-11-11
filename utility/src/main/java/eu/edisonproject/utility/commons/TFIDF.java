/*
 * Copyright 2016 alogo.
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
package eu.edisonproject.utility.commons;

import eu.edisonproject.utility.file.ConfigHelper;
import eu.edisonproject.utility.text.processing.StopWord;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 *
 * @author S. Koulouzis
 */
public class TFIDF implements SortTerms {

    private String stopWordsPath;

    public TFIDF(String stopWordsPath) {
        this.stopWordsPath = stopWordsPath;
    }

    @Override
    public Map<String, Double> sort(Map<String, Double> termDictionaray, String docs) throws IOException, InterruptedException {
        Map<String, Double> newTermDictionaray = new HashMap<>();
        TFRatio tfSort = new TFRatio();
        Map<String, Double> tfMap = tfSort.sort(termDictionaray, docs);
        IDFSort idfSort = new IDFSort(stopWordsPath);
        Map<String, Double> idfMap = idfSort.sort(termDictionaray, docs);
        for (String t : tfMap.keySet()) {
            Double tf = tfMap.get(t);
            Double idf = idfMap.get(t);
            newTermDictionaray.put(t, (tf * idf));
        }
        return newTermDictionaray;
    }

}
