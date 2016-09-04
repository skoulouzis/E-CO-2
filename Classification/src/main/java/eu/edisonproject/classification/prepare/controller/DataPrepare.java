/*
 * Copyright 2016 Michele Sparamonti & Spiros Koulouzis.
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
package eu.edisonproject.classification.prepare.controller;

/**
 *
 * @author Michele Sparamonti (michele.sparamonti@eng.it)
 */
import document.avro.Document;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.util.CharArraySet;
import org.jsoup.Jsoup;

import eu.edisonproject.utility.text.processing.Cleaner;

import eu.edisonproject.classification.prepare.model.Date;
import eu.edisonproject.classification.prepare.model.Extractor;
import eu.edisonproject.classification.prepare.model.DocumentObject;
import eu.edisonproject.utility.text.processing.StopWord;

import eu.edisonproject.utility.text.processing.StanfordLemmatizer;
import eu.edisonproject.classification.prepare.model.Text;
import eu.edisonproject.classification.prepare.model.Title;
import eu.edisonproject.classification.avro.DocumentAvroSerializer;
import eu.edisonproject.utility.file.ReaderFile;


/*
 * @author Michele Sparamonti
 */
public class DataPrepare implements IDataPrepare {

    private String inputFolder;
    private String outputFolder;
    private LinkedList<DocumentObject> documentObjectList;
    private DocumentObject documentObject;
    private String charArraySetPath;
    private CharArraySet stopWordArraySet;
    private ReaderFile fileReader;
    private static final int maxNumberOfAvroPerFile = 10;
    private String relativePath;
    
    public DataPrepare(String inputFolder, String outputFolder, String relativePath) {
        
        this.inputFolder = inputFolder;
        this.outputFolder = outputFolder;
        this.relativePath = relativePath;
        documentObjectList = new LinkedList<DocumentObject>();
        stopWordArraySet = loadStopWords();
    }

    public CharArraySet loadStopWords() {
        fileReader = new ReaderFile(relativePath + File.separator + "etc" + File.separator + "stopwords.csv");

        String[] stopWord = fileReader.readFileWithN().split("\n");
        final List<String> stopWords = Arrays.asList(stopWord);
        return new CharArraySet(stopWords, false);
    }

    @Override
    public void execute() {
        File file = new File(inputFolder);
        Document davro;
        DocumentAvroSerializer dAvroSerializer = null;
        if (file.isDirectory()) {
            File[] filesInDir = file.listFiles();
            Arrays.sort(filesInDir);
            for (File subFolder : filesInDir) {

                String date = subFolder.getName().replace("Data Scientis ", "");
                System.out.println("retrived: " + date);
                File[] files = subFolder.listFiles();
                Arrays.sort(filesInDir);
                // String newOutputFolder = outputFolder + File.separator + subFolder.getName() + LocalDate.now().toString();
                //create a new Folder
                //new File(newOutputFolder).mkdir();
                for (File f : files) {
                    documentObject = new DocumentObject();
                    extract(this.getDocumentObject(), f.getPath());
                    documentObject.setDescription(documentObject.getDescription().toLowerCase());
                    clean(this.getDocumentObject().getDescription());
                    if (documentObject.getDescription().equals("")) {
                        continue;
                    }
                    documentObjectList.add(this.getDocumentObject());

                    davro = new Document();
                    davro.setDocumentId(documentObject.getDocumentId());
                    davro.setTitle(documentObject.getTitle());
                    davro.setDate(documentObject.getDate().toString());
                    davro.setDescription(documentObject.getDescription());

                    if (dAvroSerializer == null) {
                        dAvroSerializer = new DocumentAvroSerializer(outputFolder + File.separator + documentObject.getTitle() + date + ".avro", davro.getSchema());
                    }
                    dAvroSerializer.serialize(davro);

                }

                if (dAvroSerializer != null) {
                    dAvroSerializer.close();
                    dAvroSerializer = null;
                }
            }
        } else {
            System.out.println("NOT A DIRECTORY");
        }

    }

    @Override
    public void extract(DocumentObject jp, String filePath) {
        Extractor extractorTitle = new Title();
        extractorTitle.setJp(jp);
        extractorTitle.setFilePath(filePath);
        extractorTitle.readFromFile();
        extractorTitle.extract();

        Extractor extractorDate = new Date();
        extractorDate.setJp(extractorTitle.getJp());
        extractorDate.extract();

        Extractor extractorText = new Text();
        extractorText.setJp(extractorDate.getJp());
        extractorText.extract();
    }

    public void clean(String description) {
        //System.out.println("DESCRIZIONE"+description);
        Cleaner cleanStopWord = new StopWord(this.getStopWordArraySet());
        cleanStopWord.setDescription("DESCRIZIONE PULITA " + description);
        documentObject.setDescription(cleanStopWord.execute());
        //System.out.println(documentObject.getDescription());
        Cleaner cleanStanfordLemmatizer = new StanfordLemmatizer();
        cleanStanfordLemmatizer.setDescription(documentObject.getDescription());
        documentObject.setDescription(cleanStanfordLemmatizer.execute());

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

    public LinkedList<DocumentObject> getJobPostList() {
        return documentObjectList;
    }

    public void setDocumentObjectList(LinkedList<DocumentObject> jdocumentObjectList) {
        this.documentObjectList = documentObjectList;
    }

    public DocumentObject getDocumentObject() {
        return documentObject;
    }

    public void setJDocumentObject(DocumentObject jobPost) {
        this.documentObject = jobPost;
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
