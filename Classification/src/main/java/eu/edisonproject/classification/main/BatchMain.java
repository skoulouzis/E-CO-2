/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
package eu.edisonproject.classification.main;

import com.google.common.io.Files;
import document.avro.Document;
import eu.edisonproject.classification.test.TestDataFlow;
import eu.edisonproject.classification.test.TestTFIDF;
import eu.edisonproject.classification.tfidf.mapreduce.TFIDFDriverImpl;
import eu.edisonproject.utility.file.ConfigHelper;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.io.DatumReader;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Michele Sparamonti (Michele.Sparamonti@eng.it), S. Koulouzis
 */
public class BatchMain {

    private static Properties prop;

    public static void main(String[] args) {
        try {
            //        args = new String[1];
//        args[0] = "..";
//        TestDataFlow.execute(args);
//            TestTFIDF.execute(args);
            Options options = new Options();
            Option input = new Option("i", "input", true, "input path");
            input.setRequired(true);
            options.addOption(input);

            Option output = new Option("o", "output", true, "output file");
            output.setRequired(true);
            options.addOption(output);

            Option competencesVector = new Option("c", "competences-vector", true, "competences vectors");
            competencesVector.setRequired(true);
            options.addOption(competencesVector);

            Option popertiesFile = new Option("p", "properties", true, "path for a properties file");
            popertiesFile.setRequired(false);
            options.addOption(popertiesFile);

            CommandLineParser parser = new BasicParser();
            CommandLine cmd = parser.parse(options, args);
            String propPath = cmd.getOptionValue("properties");
            if (propPath == null) {
                prop = ConfigHelper.getProperties(".." + File.separator + "etc" + File.separator + "configure.properties");
            } else {
                prop = ConfigHelper.getProperties(propPath);
            }

            calculateTFIDF(cmd.getOptionValue("input"), cmd.getOptionValue("output"), cmd.getOptionValue("competences-vector"));
        } catch (ParseException | IOException ex) {
            Logger.getLogger(BatchMain.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static void calculateTFIDF(String in, String out, String competencesVectorPath) throws IOException {
        File tmpFolder = null;
        try {
            if (new File(out).isFile()) {
                throw new IOException(out + " is a file. Should specify directory");
            }

            String workingFolder = System.getProperty("working.folder");
            if (workingFolder == null) {
                workingFolder = prop.getProperty("working.folder", System.getProperty("java.io.tmpdir"));
            }

            tmpFolder = new File(workingFolder + File.separator + System.currentTimeMillis());
            tmpFolder.mkdir();
            tmpFolder.deleteOnExit();

            TFIDFDriverImpl tfidfDriver = new TFIDFDriverImpl("post", "");
            setPaths(new File(in), tmpFolder, competencesVectorPath, tfidfDriver);

            tfidfDriver.executeTFIDF(tmpFolder.getAbsolutePath());

        } finally {
//            if (tmpFolder != null && tmpFolder.exists()) {
//                tmpFolder.delete();
//                FileUtils.forceDelete(tmpFolder);
//            }

        }
    }

    private static void setPaths(File inFile, File tmpFolder, String competencesVectorPath, TFIDFDriverImpl tfidfDriver) throws IOException {
        tfidfDriver.INPUT_ITEMSET = System.getProperty("itemset.file");
        if (tfidfDriver.INPUT_ITEMSET == null) {
            tfidfDriver.INPUT_ITEMSET = prop.getProperty("itemset.file", ".." + File.separator + "etc" + File.separator + "dictionaryAll.csv");
        }

        File outPath1 = new File(tfidfDriver.OUTPUT_PATH1);
        tfidfDriver.OUTPUT_PATH1 = tmpFolder.getAbsolutePath() + File.separator + outPath1.getName();

        File inPath2 = new File(tfidfDriver.INPUT_PATH2);
        tfidfDriver.INPUT_PATH2 = tmpFolder.getAbsolutePath() + File.separator + inPath2.getName();

        File outPath2 = new File(tfidfDriver.OUTPUT_PATH2);
        tfidfDriver.OUTPUT_PATH2 = tmpFolder.getAbsolutePath() + File.separator + outPath2.getName();

        File inPath3 = new File(tfidfDriver.INPUT_PATH3);
        tfidfDriver.INPUT_PATH3 = tmpFolder.getAbsolutePath() + File.separator + inPath3.getName();

        File outPath3 = new File(tfidfDriver.OUTPUT_PATH3);
        tfidfDriver.OUTPUT_PATH3 = tmpFolder.getAbsolutePath() + File.separator + outPath3.getName();

        File inPath4 = new File(tfidfDriver.INPUT_PATH4);
        tfidfDriver.INPUT_PATH4 = tmpFolder.getAbsolutePath() + File.separator + inPath4.getName();

        File outPath4 = new File(tfidfDriver.OUTPUT_PATH4);
        tfidfDriver.OUTPUT_PATH4 = tmpFolder.getAbsolutePath() + File.separator + outPath4.getName();

        File dist_vec_path = new File(tfidfDriver.DISTANCES_VECTOR_PATH);
        tfidfDriver.DISTANCES_VECTOR_PATH = tmpFolder.getAbsolutePath() + File.separator + dist_vec_path.getName();

        File comp_path = new File(tfidfDriver.COMPETENCES_PATH);
        File tmpFolder2 = new File(tmpFolder.getAbsolutePath() + "-2");
        tmpFolder2.mkdir();
        tmpFolder2.deleteOnExit();

        tfidfDriver.COMPETENCES_PATH = tmpFolder2.getAbsolutePath() + File.separator + comp_path.getName();
        copyFileOrFolder(new File(competencesVectorPath), new File(tfidfDriver.COMPETENCES_PATH));

        if (inFile.isFile() && FilenameUtils.getExtension(inFile.getName()).endsWith("avro")) {
//            File converted = convertToDocumet(inFile);
            FileUtils.copyFile(inFile, new File(tmpFolder + File.separator + inFile.getName()));

        } else {
            for (File f : inFile.listFiles()) {
                if (FilenameUtils.getExtension(f.getName()).endsWith("avro")) {
//                    File converted = convertToDocumet(f);

                    FileUtils.copyFile(f, new File(tmpFolder + File.separator + f.getName()));
                }
            }
        }

    }

    public static void copyFileOrFolder(File source, File dest) throws IOException {
        if (source.isDirectory()) {
            copyFolder(source, dest);
        } else {
            ensureParentFolder(dest);
            copyFile(source, dest);
        }
    }

    private static void copyFolder(File source, File dest) throws IOException {
        if (!dest.exists()) {
            dest.mkdirs();
        }
        File[] contents = source.listFiles();
        if (contents != null) {
            for (File f : contents) {
                File newFile = new File(dest.getAbsolutePath() + File.separator + f.getName());
                if (f.isFile() && FilenameUtils.getExtension(f.getName()).endsWith("csv") && !f.getName().equals("terms.csv")) {
                    copyFile(f, newFile);
                } else {
                    copyFolder(f, dest);
                }
            }
        }
    }

    private static void copyFile(File source, File dest) throws IOException {
        Files.copy(source, dest);
    }

    private static void ensureParentFolder(File file) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }
}
