/**
 *
 */
package eu.edisonproject.training.context.corpus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import eu.edisonproject.utility.file.ReaderFile;
import eu.edisonproject.utility.file.WriterFile;
import eu.edisonproject.utility.text.processing.Cleaner;
import eu.edisonproject.utility.text.processing.StanfordLemmatizer;
import eu.edisonproject.utility.text.processing.StopWord;

import org.apache.lucene.analysis.util.CharArraySet;

/**
 *
 * Copyright 2016 Michele Sparamonti & Spiros Koulouzis
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
public class DataPrepare {

    private String inputFolder;
    private String outputFolder;
    private String charArraySetPath;
    private CharArraySet stopWordArraySet;
    private ReaderFile fileReader;
    private List<List<String>> allRules;

    public DataPrepare(String inputFolder, String outputFolder, String stopwordDocument) {
        stopWordArraySet = loadStopWords(stopwordDocument);
        this.inputFolder = inputFolder;
        this.outputFolder = outputFolder;
        this.allRules = new LinkedList<List<String>>();
    }

    public CharArraySet loadStopWords(String stopwordDocument) {
        fileReader = new ReaderFile(stopwordDocument);
        String[] stopWord = fileReader.readFile().split(" ");
        final List<String> stopWords = Arrays.asList(stopWord);

        return new CharArraySet(stopWords, false);
    }

    public void execute() {

        /*
		 * Read, clean and compute the association rules for each categories
		 * organized in folder. It is required to following this pattern for the
		 * folder organization. Top folder-- - Category1 --Doc11 --Doc21 ...
		 * -Category2 --Doc12 and so on.
         */
        File file = new File(inputFolder);
        if (file.isDirectory()) {
            File[] filesInDir = file.listFiles();
            Arrays.sort(filesInDir);
            for (File folder : filesInDir) {
                if (file.isDirectory()) {
                    String transactions = "";
                    File[] documents = folder.listFiles();
                    for (File document : documents) {
                        ReaderFile rf = new ReaderFile(inputFolder + "/" + folder.getName() + "/" + document.getName());
                        String description = rf.readFile();
                        String cleanedDescription = clean(description);

                        transactions += cleanedDescription + "\n";
                    }
                    String supportThreshold = "0.6";
                    String[] arg = {transactions.toString(), supportThreshold};
                    Apriori aprioriDocuments;
                    try {
                        aprioriDocuments = new Apriori(arg);
                        allRules.add(aprioriDocuments.go());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFolder + "itemset.csv")));
            for (List<String> itemset : allRules) {
                for (String item : itemset) {
                    String[] words = (item.split(",")[0]).split(" ");
                    if (words.length > 1) {
                        bw.write(item + "\n");
                    }
                }
            }

            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String clean(String description) {
        Cleaner cleanStopWord = new StopWord(this.getStopWordArraySet());
        cleanStopWord.setDescription(description);
        String descriptionWithoutStopWord = cleanStopWord.execute();

        Cleaner cleanLemmatisation = new StanfordLemmatizer();
        cleanLemmatisation.setDescription(descriptionWithoutStopWord);
        String descriptionLemma = cleanLemmatisation.execute();

        return descriptionLemma;

    }

    public String getInputFolder() {
        return inputFolder;
    }

    public void setInputFolder(String inputFolder) {
        this.inputFolder = inputFolder;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    public String getCharArraySetPath() {
        return charArraySetPath;
    }

    public void setCharArraySetPath(String charArraySetPath) {
        this.charArraySetPath = charArraySetPath;
    }

    public CharArraySet getStopWordArraySet() {
        return stopWordArraySet;
    }

    public void setStopWordArraySet(CharArraySet charArraySet) {
        this.stopWordArraySet = charArraySet;
    }

}
