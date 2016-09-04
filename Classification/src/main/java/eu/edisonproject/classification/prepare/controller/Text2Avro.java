/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.edisonproject.classification.prepare.controller;

import document.avro.Document;
import eu.edisonproject.classification.avro.DocumentAvroSerializer;
import eu.edisonproject.classification.prepare.controller.IDataPrepare;
import eu.edisonproject.classification.prepare.model.Date;
import eu.edisonproject.classification.prepare.model.DocumentObject;
import eu.edisonproject.classification.prepare.model.Extractor;
import eu.edisonproject.classification.prepare.model.Text;
import eu.edisonproject.classification.prepare.model.Title;
import eu.edisonproject.utility.file.ConfigHelper;
import eu.edisonproject.utility.file.ReaderFile;
import eu.edisonproject.utility.text.processing.Cleaner;
import eu.edisonproject.utility.text.processing.StanfordLemmatizer;
import eu.edisonproject.utility.text.processing.StopWord;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 *
 * @author S. Koulouzis
 */
public class Text2Avro implements IDataPrepare {

    private final String inputFolder;
    private final String outputFolder;
    private static CharArraySet stopwordsCharArray;
    private DocumentObject documentObject;
    private LinkedList<DocumentObject> documentObjectList;

    public Text2Avro(String inputFolder, String outputFolder, String stopWordsPath) {
        this.inputFolder = inputFolder;
        this.outputFolder = outputFolder;
        stopwordsCharArray = new CharArraySet(ConfigHelper.loadStopWords(stopWordsPath), true);
        documentObjectList = new LinkedList<>();
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

    @Override
    public void clean(String description) {
        Cleaner cleanStopWord = new StopWord(stopwordsCharArray);
        cleanStopWord.setDescription(description);
        documentObject.setDescription(cleanStopWord.execute());
        Cleaner cleanStanfordLemmatizer = new StanfordLemmatizer();
        cleanStanfordLemmatizer.setDescription(documentObject.getDescription());
        documentObject.setDescription(cleanStanfordLemmatizer.execute());

    }

    private DocumentObject getDocumentObject() {
        return documentObject;
    }

}
