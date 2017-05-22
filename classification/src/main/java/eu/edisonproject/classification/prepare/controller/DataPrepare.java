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
//import java.time.LocalDate;
import java.util.LinkedList;

import org.apache.lucene.analysis.util.CharArraySet;

import eu.edisonproject.utility.text.processing.Cleaner;

import eu.edisonproject.classification.prepare.model.Date;
import eu.edisonproject.classification.prepare.model.Extractor;
import eu.edisonproject.classification.prepare.model.DocumentObject;
import eu.edisonproject.utility.text.processing.StopWord;

import eu.edisonproject.utility.text.processing.StanfordLemmatizer;
import eu.edisonproject.classification.prepare.model.Text;
import eu.edisonproject.classification.prepare.model.Title;
import eu.edisonproject.classification.avro.DocumentAvroSerializer;
import eu.edisonproject.utility.file.ConfigHelper;
import eu.edisonproject.utility.file.ReaderFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
//import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


/*
 * @author Michele Sparamonti
 */
public class DataPrepare implements IDataPrepare {

    private String inputFolder;
    private String outputFolder;
    private LinkedList<DocumentObject> documentObjectList;
    private DocumentObject documentObject;
    private String charArraySetPath;
//    private CharArraySet stopWordArraySet;
//    private ReaderFile fileReader;
//    private static final int maxNumberOfAvroPerFile = 10;
    private final StopWord cleanStopWord;
    private final StanfordLemmatizer cleanLemmatisation;

    public DataPrepare(String inputFolder, String outputFolder, String stopWordsPath) {
        this.inputFolder = inputFolder;
        this.outputFolder = outputFolder;
        documentObjectList = new LinkedList<>();
        CharArraySet stopWordArraySet = new CharArraySet(ConfigHelper.loadStopWords(stopWordsPath), true);
        cleanStopWord = new StopWord(stopWordArraySet);
        cleanLemmatisation = new StanfordLemmatizer();
    }

    @Override
    public void execute() {
        File file = new File(inputFolder);
        Document davro;
        DocumentAvroSerializer dAvroSerializer = null;
        if (file.isDirectory()) {
            File[] filesInDir = file.listFiles();
//            Arrays.sort(filesInDir);

//            LocalDate date = getCreationDate(file);
            for (File f : filesInDir) {
                if (f.isFile() && FilenameUtils.getExtension(f.getName()).endsWith("txt")) {
                    LocalDate date = getCreationDate(f);
                    documentObject = new DocumentObject();
                    documentObject.setDate(date);
                    ReaderFile rf = new ReaderFile(f.getAbsolutePath());
                    String contents = rf.readFile();
                    cleanStopWord.setDescription(contents);
                    String cleanCont = cleanStopWord.execute().toLowerCase();
                    cleanLemmatisation.setDescription(cleanCont);
                    cleanCont = cleanLemmatisation.execute();
                    documentObject.setDescription(cleanCont);
                    documentObject.setDocumentId(FilenameUtils.removeExtension(f.getName()));
                    documentObject.setTitle(f.getParentFile().getName());
//                extract(this.getDocumentObject(), f.getPath());
//                documentObject.setDescription(documentObject.getDescription().toLowerCase());
//                clean(this.getDocumentObject().getDescription());
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
                        dAvroSerializer = new DocumentAvroSerializer(outputFolder
                                + File.separator + documentObject.getTitle().replaceAll(" ", "_")
                                + date + ".avro", davro.getSchema());
                    }
                    Logger.getLogger(Text2Avro.class.getName()).log(Level.INFO, "Adding :{0} to: {1}{2}{3}{4}.avro", new Object[]{documentObject.getDocumentId(), outputFolder, File.separator, documentObject.getTitle().replaceAll(" ", "_"), date});
                    dAvroSerializer.serialize(davro);
                }

            }

            if (dAvroSerializer != null) {
                dAvroSerializer.close();
                dAvroSerializer = null;
            }
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

    @Override
    public void clean(String description) {
        //System.out.println("DESCRIZIONE"+description);
//        Cleaner cleanStopWord = new StopWord(this.getStopWordArraySet());
        cleanStopWord.setDescription(description);
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
        this.documentObjectList = jdocumentObjectList;
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

//    public CharArraySet getStopWordArraySet() {
//        return stopWordArraySet;
//    }
//
//    public void setStopWordArraySet(CharArraySet charArraySet) {
//        this.stopWordArraySet = charArraySet;
//    }
    private LocalDate getCreationDate(File file) {
        Path p = Paths.get(file.getAbsolutePath());
        BasicFileAttributes attr = null;
        try {
            attr = Files.readAttributes(p, BasicFileAttributes.class);
        } catch (IOException ex) {
            Logger.getLogger(Text2Avro.class.getName()).log(Level.SEVERE, null, ex);
        }
        FileTime ct = attr.creationTime();
        DateTimeFormatter formatter;

//        
//        LocalDate.parse("2016-09-18T11:40:03.750522Z", formatter);
        formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        LocalDate date = null;
        try {
            date = LocalDate.parse(ct.toString(), formatter);
        } catch (java.lang.IllegalArgumentException ex) {
            formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
            date = LocalDate.parse(ct.toString(), formatter);
        }
        Logger.getLogger(Text2Avro.class.getName()).log(Level.INFO, "CreationDate: {0}", date);
        return date;
    }

}
