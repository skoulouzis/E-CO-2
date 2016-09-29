/*
 * Copyright 2016 Michele Sparamonti, Spiros Koulouzis
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
package eu.edisonproject.classification.distance;

import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author Michele Sparamonti (Michele.Sparamonti@eng.it)
 */
public class CosineSimilarityMatrix {

    public double computeDistance(Collection<Double> c1, Collection<Double> c2) throws Exception {
        if(c1.size() != c2.size()){
            throw new Exception("vectors must be of same length");
        }
        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;
        double cosineSimilarity;
        
        Iterator<Double> s1 = c1.iterator();
        Iterator<Double> s2 = c2.iterator();

        while(s1.hasNext()){ //docVector1 and docVector2 must be of same length
            Double d1 = s1.next();
            Double d2 = s2.next();
            dotProduct += d1 * d2;//a.b
            magnitude1 += Math.pow(d1, 2);  //(a^2)
            magnitude2 += Math.pow(d2, 2); //(b^2)
        }
        
        magnitude1 = Math.sqrt(magnitude1);//sqrt(a^2)
        magnitude2 = Math.sqrt(magnitude2);//sqrt(b^2)
        if (magnitude1 != 0.0 | magnitude2 != 0.0) {
            cosineSimilarity = dotProduct / (magnitude1 * magnitude2);
        } else {
            return 0.0;
        }
        return cosineSimilarity;
    }

}
