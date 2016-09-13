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
package eu.edisonproject.training.execute;

import com.google.common.io.Files;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import eu.edisonproject.training.context.corpus.DataPrepare;
import eu.edisonproject.training.term.extraction.TermExtractor;
import eu.edisonproject.training.tfidf.mapreduce.ITFIDFDriver;
import eu.edisonproject.training.tfidf.mapreduce.TFIDFDriverImpl;
import eu.edisonproject.training.tfidf.mapreduce.TFIDFTermsDriver;
import eu.edisonproject.training.wsd.DisambiguatorImpl;
import eu.edisonproject.training.wsd.MetaDisambiguator;
import eu.edisonproject.utility.commons.Term;
import eu.edisonproject.utility.commons.TermAvroSerializer;
import eu.edisonproject.utility.file.ConfigHelper;
import eu.edisonproject.utility.text.processing.StanfordLemmatizer;
import eu.edisonproject.utility.text.processing.StopWord;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 * Example usage for training : -op t -i
 * $HOME/Downloads/Databases/dictionary.csv \ -o
 * $HOME/Downloads/D2.1_Table5_Skills_and_knowledge_Big_Data_platforms_and_tools/Databases/Databases.avro
 *
 * for term extraction:
 *
 *
 * @author S. Koulouzis
 */
public class Main {

    private static Properties prop;
//    private static final String[] rejectPOS = new String[]{"JJ", "JJR", "JJS",
//        "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "RB", "RBR", "RBS"};

    public static void main(String args[]) {
        Options options = new Options();
        Option operation = new Option("op", "operation", true, "type of operation to perform. "
                + "For term extraction use 'x'.\n"
                + "Example: -op x -i E-COCO/documentation/sampleTextFiles/databases.txt "
                + "-o E-COCO/documentation/sampleTextFiles/databaseTerms.csv"
                + "For word sense disambiguation use 'w'.\n"
                + "Example: -op w -i E-COCO/documentation/sampleTextFiles/databaseTerms.csv "
                + "-o E-COCO/documentation/sampleTextFiles/databse.avro\n"
                + "For tf-idf vector extraction use 't'.\n"
                + "For running the apriori algorithm use 'a'");
        operation.setRequired(true);
        options.addOption(operation);

        Option input = new Option("i", "input", true, "input file path");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option("o", "output", true, "output file");
        output.setRequired(true);
        options.addOption(output);

        Option popertiesFile = new Option("p", "properties", true, "path for a properties file");
        popertiesFile.setRequired(false);
        options.addOption(popertiesFile);

        Option termsFile = new Option("t", "terms", true, "terms file");
        termsFile.setRequired(false);
        options.addOption(termsFile);

        String helpmasg = "Usage: \n";
        for (Object obj : options.getOptions()) {
            Option op = (Option) obj;
            helpmasg += op.getOpt() + ", " + op.getLongOpt() + "\t Required: " + op.isRequired() + "\t\t" + op.getDescription() + "\n";
        }

        try {
            CommandLineParser parser = new BasicParser();
            CommandLine cmd = parser.parse(options, args);

            String propPath = cmd.getOptionValue("properties");
            if (propPath == null) {
                prop = ConfigHelper.getProperties(".." + File.separator + "etc" + File.separator + "configure.properties");
            } else {
                prop = ConfigHelper.getProperties(propPath);
            }
            switch (cmd.getOptionValue("operation")) {
                case "x":
                    termExtraction(cmd.getOptionValue("input"), cmd.getOptionValue("output"));
                    break;
                case "w":
                    wsd(cmd.getOptionValue("input"), cmd.getOptionValue("output"));
                    break;
                case "t":
                    calculateTFIDF(cmd.getOptionValue("input"), cmd.getOptionValue("output"));
                    break;
//                case "tt":
//                    calculateTermTFIDF(cmd.getOptionValue("input"), cmd.getOptionValue("terms"), cmd.getOptionValue("output"));
//                    break;
                case "a":
                    apriori(cmd.getOptionValue("input"), cmd.getOptionValue("output"));
                    break;
                default:
                    System.out.println(helpmasg);
            }

        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, helpmasg, ex);
        }
    }

    private static void termExtraction(String docs, String out) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, InterruptedException {
        if (!new File(docs).exists()) {
            throw new IOException(new File(docs).getAbsolutePath() + " don't exist");
        }
//
//        String[] extractors = prop.getProperty("term.extractors",
//                "eu.edisonproject.training.term.extraction.LuceneExtractor,"
//                + "eu.edisonproject.training.term.extraction.JtopiaExtractor,"
//                + "eu.edisonproject.training.term.extraction.AprioriExtraction").split(",");

//        String[] extractors = prop.getProperty("term.extractors",
//                "eu.edisonproject.training.term.extraction.JtopiaExtractor,"
//                + "eu.edisonproject.training.term.extraction.AprioriExtraction").split(",");
        String[] extractors = "eu.edisonproject.training.term.extraction.JtopiaExtractor".split(",");

        Map<String, Double> termDictionaray = new HashMap();
        for (String className : extractors) {
            Class c = Class.forName(className);
            Object obj = c.newInstance();
            TermExtractor termExtractor = (TermExtractor) obj;
            termExtractor.configure(prop);
//            termExtractor.setDocPath(docPath);
            termDictionaray.putAll(termExtractor.termXtraction(docs));
        }
        String stopWordsPath = System.getProperty("stop.words.file");

        if (stopWordsPath == null) {
            stopWordsPath = prop.getProperty("stop.words.file", ".." + File.separator + "etc" + File.separator + "stopwords.csv");
        }

//        SortTerms sorter = new TFIDF(stopWordsPath);
//        termDictionaray = sorter.sort(termDictionaray, in);
        writeDictionary2File(termDictionaray, out);

        calculateTermTFIDF(docs, out, out);
    }

    public static void writeDictionary2File(Map<String, Double> keywordsDictionaray, String outkeywordsDictionarayFile) throws FileNotFoundException {
//        ValueComparator bvc = new ValueComparator(keywordsDictionaray);
//        Map<String, Double> sorted_map = new TreeMap(bvc);
//        sorted_map.putAll(keywordsDictionaray);
        try (PrintWriter out = new PrintWriter(outkeywordsDictionarayFile)) {
            for (String key : keywordsDictionaray.keySet()) {
                Double value = keywordsDictionaray.get(key);
                key = key.toLowerCase().trim().replaceAll(" ", "_");
                if (key.endsWith("_")) {
                    key = key.substring(0, key.lastIndexOf("_"));
                }

                out.print(key + "," + value + "\n");
            }
        }
    }

    private static void wsd(String in, String out) throws IOException, FileNotFoundException, org.json.simple.parser.ParseException {
        DisambiguatorImpl d = new MetaDisambiguator();
        d.configure(prop);
        List<Term> terms = d.disambiguateTerms(in);
        saveTerms2Avro(terms, out);

    }

    private static void saveTerms2Avro(List<Term> terms, String out) {
        TermAvroSerializer ts = new TermAvroSerializer(out, Term.getClassSchema());
        List<CharSequence> empty = new ArrayList<>();
        empty.add("");
//        Stemming stemer = new Stemming();
        String stopWordsPath = System.getProperty("stop.words.file");

        if (stopWordsPath == null) {
            stopWordsPath = prop.getProperty("stop.words.file", ".." + File.separator + "etc" + File.separator + "stopwords.csv");
        }

        CharArraySet stopwordsCharArray = new CharArraySet(ConfigHelper.loadStopWords(stopWordsPath), true);
        StopWord tokenizer = new StopWord(stopwordsCharArray);
        StanfordLemmatizer lematizer = new StanfordLemmatizer();

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
                ArrayList<CharSequence> lem = new ArrayList<>();
                lem.add(t.lemma);
                t.setGlosses(lem);
            } else {
                StringBuilder glosses = new StringBuilder();
                for (CharSequence n : gl) {
                    glosses.append(n).append(" ");
                }
                glosses.append(t.lemma.toString().replaceAll("_", " "));
                if (alt != null && !alt.isEmpty() && !alt.contains(null)) {
                    for (CharSequence c : alt) {
                        glosses.append(c.toString().replaceAll("_", " ")).append(" ");
                    }
                }
                gl = new ArrayList<>();
                tokenizer.setDescription(glosses.toString());
                String cleanText = tokenizer.execute();
                lematizer.setDescription(cleanText);
                String lematizedText = lematizer.execute();

                gl.add(lematizedText);
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

    private static void calculateTFIDF(String in, String out) throws IOException {
        File tmpFolder = null;
        try {
            String contextName = FilenameUtils.removeExtension(in.substring(in.lastIndexOf(File.separator) + 1));
            ITFIDFDriver tfidfDriver = new TFIDFDriverImpl(contextName);
            File inFile = new File(in);

            String workingFolder = System.getProperty("working.folder");
            if (workingFolder == null) {
                workingFolder = prop.getProperty("working.folder", System.getProperty("java.io.tmpdir"));
            }

            tmpFolder = new File(workingFolder + File.separator + System.currentTimeMillis());

            tmpFolder.mkdir();
            tmpFolder.deleteOnExit();

            setTFIDFDriverImplPaths(inFile, tmpFolder);

            tfidfDriver.executeTFIDF(tmpFolder.getAbsolutePath());
            tfidfDriver.driveProcessResizeVector();
            File ctxPath = new File(TFIDFDriverImpl.CONTEXT_PATH);
            for (File f : ctxPath.listFiles()) {
                if (FilenameUtils.getExtension(f.getName()).endsWith("csv")) {
                    FileUtils.moveFile(f, new File(out + File.separator + f.getName()));
                }
            }
        } finally {
            if (tmpFolder != null && tmpFolder.exists()) {
                tmpFolder.delete();
                FileUtils.forceDelete(tmpFolder);
            }
        }
    }

    private static void setTFIDFDriverImplPaths(File inFile, File tmpFolder) throws IOException {

        TFIDFDriverImpl.INPUT_ITEMSET = System.getProperty("itemset.file");
        if (TFIDFDriverImpl.INPUT_ITEMSET == null) {
            TFIDFDriverImpl.INPUT_ITEMSET = prop.getProperty("itemset.file", ".." + File.separator + "etc" + File.separator + "itemset.csv");
        }

        File outPath1 = new File(TFIDFDriverImpl.OUTPUT_PATH1);
        TFIDFDriverImpl.OUTPUT_PATH1 = tmpFolder.getAbsolutePath() + File.separator + outPath1.getName();

        File inPath2 = new File(TFIDFDriverImpl.INPUT_PATH2);
        TFIDFDriverImpl.INPUT_PATH2 = tmpFolder.getAbsolutePath() + File.separator + inPath2.getName();

        File outPath2 = new File(TFIDFDriverImpl.OUTPUT_PATH2);
        TFIDFDriverImpl.OUTPUT_PATH2 = tmpFolder.getAbsolutePath() + File.separator + outPath2.getName();

        File inPath3 = new File(TFIDFDriverImpl.INPUT_PATH3);
        TFIDFDriverImpl.INPUT_PATH3 = tmpFolder.getAbsolutePath() + File.separator + inPath3.getName();

        File outPath3 = new File(TFIDFDriverImpl.OUTPUT_PATH3);
        TFIDFDriverImpl.OUTPUT_PATH3 = tmpFolder.getAbsolutePath() + File.separator + outPath3.getName();

        File inPath4 = new File(TFIDFDriverImpl.INPUT_PATH4);
        TFIDFDriverImpl.INPUT_PATH4 = tmpFolder.getAbsolutePath() + File.separator + inPath4.getName();

        File outPath4 = new File(TFIDFDriverImpl.OUTPUT_PATH4);
        TFIDFDriverImpl.OUTPUT_PATH4 = tmpFolder.getAbsolutePath() + File.separator + outPath4.getName();

        File tiidfCSV = new File(TFIDFDriverImpl.TFIDFCSV_PATH);
        TFIDFDriverImpl.TFIDFCSV_PATH = tmpFolder.getAbsolutePath() + File.separator + tiidfCSV.getName();

        File context = new File(TFIDFDriverImpl.CONTEXT_PATH);
        TFIDFDriverImpl.CONTEXT_PATH = tmpFolder.getAbsolutePath() + File.separator + context.getName();

        if (inFile.isFile() && FilenameUtils.getExtension(inFile.getName()).endsWith("avro")) {

            FileUtils.copyFile(inFile, new File(tmpFolder + File.separator + inFile.getName()));
//            tfidfDriver.executeTFIDF(tmpFolder.getAbsolutePath());
        } else {
            for (File f : inFile.listFiles()) {
                if (FilenameUtils.getExtension(f.getName()).endsWith("avro")) {
                    FileUtils.copyFile(f, new File(tmpFolder + File.separator + f.getName()));
                }
            }
        }

    }

    private static void calculateTermTFIDF(String docPath, String termsFile, String out) throws IOException {
        try {
            ITFIDFDriver tfidfDriver = new TFIDFTermsDriver();
            setTFIDFTermDriverPaths(docPath);
            tfidfDriver.executeTFIDF(termsFile);
//            File terms = new File(TFIDFTermsDriver.TERMS);
//            if (FilenameUtils.getExtension(terms.getName()).endsWith("csv")) {
//                FileUtils.moveFile(terms, new File(out));
//            }
        } finally {
//            if (tmpFolder != null && tmpFolder.exists()) {
//                tmpFolder.delete();
//                inFile.delete();
////                FileUtils.forceDelete(tmpFolder);
//            }
        }
    }

    private static void apriori(String in, String out) throws IOException {
        String stopWordsPath = System.getProperty("stop.words.file");

        if (stopWordsPath == null) {
            stopWordsPath = prop.getProperty("stop.words.file", ".." + File.separator + "etc" + File.separator + "stopwords.csv");
        }
        DataPrepare dataPrepare = new DataPrepare(in, out, stopWordsPath);
        dataPrepare.execute();
        String taggerPath = System.getProperty("tagger.file");

        if (taggerPath == null) {
            taggerPath = prop.getProperty("tagger.file", ".." + File.separator + "etc" + File.separator + "model" + File.separator + "stanford" + File.separator + "english-left3words-distsim.tagger");
        }

        File fin = new File(out + File.separator + "itemset.csv");
        File fout = new File(out + File.separator + "tmp.csv");
        MaxentTagger tagger = new MaxentTagger(taggerPath);
        try (PrintWriter pw = new PrintWriter(fout)) {
            try (BufferedReader br = new BufferedReader(new FileReader(fin))) {
                for (String text; (text = br.readLine()) != null;) {
                    String term = text.split("/")[0];
                    String tagged = tagger.tagString(term);
                    boolean add = true;
                    if (!tagged.contains("NN") || tagged.contains("RB")) {
                        add = false;
                    }
                    if (add) {
                        pw.print(text + "\n");
                    }
                }
            }
        }
        Files.move(fout, fin);
    }

    private static void setTFIDFTermDriverPaths(String textDocsPath) throws IOException {

        TFIDFTermsDriver.STOPWORDS_PATH = System.getProperty("stop.words.file");

        if (TFIDFTermsDriver.STOPWORDS_PATH == null) {
            TFIDFTermsDriver.STOPWORDS_PATH = prop.getProperty("stop.words.file", ".." + File.separator + "etc" + File.separator + "stopwords.csv");
        }
        TFIDFTermsDriver.TEXT_FILES_DIR_PATH = textDocsPath;

    }

}
