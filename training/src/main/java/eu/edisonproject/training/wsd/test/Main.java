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
package eu.edisonproject.training.wsd.test;

import eu.edisonproject.training.wsd.DisambiguatorImpl;
import eu.edisonproject.training.wsd.MetaDisambiguator;
import eu.edisonproject.utility.commons.Term;
import eu.edisonproject.utility.commons.TermAvroSerializer;
import eu.edisonproject.utility.file.ConfigHelper;
import eu.edisonproject.utility.file.MyProperties;
import eu.edisonproject.utility.text.processing.Stemming;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.parser.ParseException;

/**
 *
 * @author S. Koulouzis
 */
public class Main {

    public static void main(String args[]) {
        testDisambiguators();
    }

    private static void testDisambiguators() {
        try {

//            Properties prop = new Properties();
//            prop.setProperty("bablenet.key", "f4212b8f-161e-42cc-88a9-3d06b515c4a1");
//            prop.setProperty("minimum.similarity", "-10");
//            prop.setProperty("stop.words.file", ".." + File.separator + "etc" + File.separator + "stopwords.csv");
//            prop.setProperty("max.ngrams", "4");
//            String itemsFilePath = ".." + File.separator + "etc" + File.separator + "allTerms.csv";
////            String itemsFilePath = ".." + File.separator + "documentation" + File.separator + "results" + File.separator + "itemset.csv";
//            prop.setProperty("itemset.file", itemsFilePath);
//            prop.setProperty("disambiguators", "eu.edisonproject.training.wsd.BabelNet,eu.edisonproject.training.wsd.WikipediaOnline,eu.edisonproject.training.wsd.Wikidata");
//            prop.setProperty("execute.sequentially", "true");
//            prop.setProperty("num.of.terms", "9");
            MyProperties prop = ConfigHelper.getProperties(".." + File.separator + "etc" + File.separator + "configure.properties");
            DisambiguatorImpl d = new MetaDisambiguator();
            d.configure(prop);
            String dictinaryPath = ".." + File.separator + "etc" + File.separator + "databases.csv";
//            List<Term> terms = d.disambiguateTerms(dictinaryPath);
            d.getTerm("bachelor");

//            String context = FilenameUtils.removeExtension(dictinaryPath.substring(dictinaryPath.lastIndexOf(File.separator) + 1));
//            saveTerms2Avro(terms, context);
        } catch (IOException | ParseException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void saveTerms2Avro(List<Term> terms, String context) {

        String filePath = ".." + File.separator + "etc" + File.separator + "Avro Document" + File.separator + "Databases" + File.separator + context + ".avro";
        TermAvroSerializer ts = new TermAvroSerializer(filePath, Term.getClassSchema());
        List<CharSequence> empty = new ArrayList<>();
        empty.add("");
        Stemming stemer = new Stemming();
        for (Term t : terms) {
            List<CharSequence> nuid = t.getNuids();
            if (nuid == null || nuid.isEmpty() || nuid.contains(null)) {
                t.setNuids(empty);
            }

            List<CharSequence> buids = t.getBuids();
            if (buids == null || buids.isEmpty() || buids.contains(null)) {
                t.setBuids(empty);
            }
            List<CharSequence> alt = t.getAltLables();
            if (alt == null || alt.isEmpty() || alt.contains(null)) {
                t.setAltLables(empty);
            }
            List<CharSequence> gl = t.getGlosses();
            if (gl == null || gl.isEmpty() || gl.contains(null)) {
                t.setGlosses(empty);
            } else {
                StringBuilder glosses = new StringBuilder();
                for (CharSequence n : gl) {
                    glosses.append(n).append(" ");
                }
                gl = new ArrayList<>();
                stemer.setDescription(glosses.toString());
                gl.add(stemer.execute());
                t.setGlosses(gl);

            }
            List<CharSequence> cat = t.getCategories();
            if (cat == null || cat.contains(null)) {
                t.setCategories(empty);
            }
            ts.serialize(t);
        }
        ts.close();

    }
}
