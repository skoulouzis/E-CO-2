package eu.edisonproject.training.term.extraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

//   import LBJ2.nlp.SentenceSplitter;
//    import LBJ2.nlp.WordSplitter;
//    import LBJ2.nlp.seg.PlainToTokenParser;
//    import LBJ2.parse.Parser;
//    import edu.illinois.cs.cogcomp.lbj.chunk.Chunker;
//    import edu.illinois.cs.cogcomp.lbj.pos.POSTagger;
//    import edu.ehu.galan.cvalue.model.Token;
//import edu.illinois.cs.cogcomp.lbjava.frontend.parser;

/**
 *
 * @author S. Koulouzis
 *
 */
public class RAKEExtractor implements TermExtractor {

    private String stopWordsPath;
    private String puctuationFilePath;

    @Override
    public void configure(Properties prop) {
        stopWordsPath = System.getProperty("stop.words.file");

        if (stopWordsPath == null) {
            stopWordsPath = prop.getProperty("stop.words.file", ".." + File.separator + "etc" + File.separator + "stopwords.csv");
        }

        puctuationFilePath = System.getProperty("punctuation.file");

        if (puctuationFilePath == null) {
            puctuationFilePath = prop.getProperty("punctuation.file", ".." + File.separator + "etc" + File.separator + "punctuation");
        }

    }

    @Override
    public Map<String, Double> termXtraction(String inDir) throws IOException {
        File dir = new File(inDir);

        HashMap<String, Double> keywordsDictionaray = new HashMap();
        int count = 0;
        if (dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                count++;
                Logger.getLogger(JtopiaExtractor.class.getName()).log(Level.INFO, "{0}: {1} of {2}", new Object[]{f.getName(), count, dir.list().length});
                keywordsDictionaray.putAll(extractFromFile(f));
            }
        } else if (dir.isFile()) {
            keywordsDictionaray.putAll(extractFromFile(dir));
        }
        return keywordsDictionaray;
    }

    private Map<? extends String, ? extends Double> extractFromFile(File f) throws IOException {
//            Document doc=new Document(full_path,name);
//    doc.setSentenceList(sentences);
//    doc.setTokenList(tokenized_sentences); 
//        edu.ehu.galan.rake.RakeAlgorithm ex = new edu.ehu.galan.rake.RakeAlgorithm();
        List<String> words = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(stopWordsPath)))) {
            for (String text; (text = br.readLine()) != null;) {
                words.add(text);
            }
        }
//        ex.loadStopWordsList(words);
        words = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(puctuationFilePath)))) {
            for (String text; (text = br.readLine()) != null;) {
                words.add(text);
            }
        }
//        ex.loadPunctStopWord(words);
//        ex.loadPunctStopWord(words);

//    parser.readSource("testCorpus/textAstronomy");
inti();
//        edu.ehu.galan.rake.model.Document doc = new edu.ehu.galan.rake.model.Document(f.getAbsolutePath(), f.getName());
        String pPropsDir = "";

//        ex.init(doc, pPropsDir);
//        ex.runAlgorithm();
//        doc.getTermList();
        return null;
    }

    @Override
    public Map<String, Double> rank(String inDir) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Pattern buildPunctStopWord(List<String> pPunctStop) {
        StringBuilder sb = new StringBuilder();
        for (String string : pPunctStop) {
            sb.append("\\").append(string.trim()).append("|");
        }
        String pattern = sb.substring(0, sb.length() - 1);
        Pattern pat = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        return pat;
    }

    private void inti() {
       
//        List<LinkedList<Token>> tokenizedSentenceList;
//     List<String> sentenceList;
//     POSTagger tagger = new POSTagger();
//     Chunker chunker = new Chunker();
//     boolean first = true;
//     parser = new PlainToTokenParser(new WordSplitter(new SentenceSplitter(pFile)));
//     String sentence = "";
//     LinkedList<Token> tokenList = null;
//     for (LBJ2.nlp.seg.Token word = (LBJ2.nlp.seg.Token) parser.next(); word != null;
//            word = (LBJ2.nlp.seg.Token) parser.next()) {
//            String chunked = chunker.discreteValue(word);
//            tagger.discreteValue(word);
//            if (first) {
//                tokenList = new LinkedList<>();
//                tokenizedSentenceList.add(tokenList);
//                first = false;
//            }
//            tokenList.add(new Token(word.form, word.partOfSpeech, null, chunked));
//            sentence = sentence + " " + (word.form);
//            if (word.next == null) {
//                sentenceList.add(sentence);
//                first = true;
//                sentence = "";
//            }
//     }
//     parser.reset();
     
    }

}
