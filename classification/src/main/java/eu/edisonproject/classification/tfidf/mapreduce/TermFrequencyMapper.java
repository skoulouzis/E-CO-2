/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.classification.tfidf.mapreduce;

import document.avro.Document;
import eu.edisonproject.utility.file.ConfigHelper;
import eu.edisonproject.utility.text.processing.StanfordLemmatizer;
import eu.edisonproject.utility.text.processing.StopWord;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.avro.mapred.AvroKey;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.lucene.analysis.util.CharArraySet;

public class TermFrequencyMapper extends Mapper<AvroKey<Document>, NullWritable, Text, IntWritable> {

  private static final List<String> TERMS = new ArrayList<>();
  private static StopWord cleanStopWord;
  private StanfordLemmatizer cleanLemmatisation;

  @Override
  protected void setup(Context context) throws IOException, InterruptedException {
    if (context.getCacheFiles() != null && context.getCacheFiles().length > 0) {
      URI[] uris = context.getCacheFiles();

      FileSystem fs = FileSystem.get(context.getConfiguration());
      String s;
      if (TERMS == null || TERMS.size() < 1) {
        Path dictionaryFilePath = new Path(uris[0]);
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(fs.open(dictionaryFilePath)))) {
          while ((s = br.readLine()) != null) {
            s = s.replaceAll("_", " ").trim();
            TERMS.add(s);
          }
        }
      }
      URI stopwordFile = uris[1];
      if (cleanStopWord == null) {
        CharArraySet stopWordArraySet = new CharArraySet(ConfigHelper.loadStopWords(fs.open(new Path(stopwordFile)).getWrappedStream()), true);
        cleanStopWord = new StopWord(stopWordArraySet);
      }
      if (cleanLemmatisation == null) {
        cleanLemmatisation = new StanfordLemmatizer();
      }
    }

    Logger.getLogger(WordFrequencyInDocDriver.class.getName()).log(Level.INFO, "terms array has :{0} elemnts", TERMS.size());
  }

  @Override
  public void map(AvroKey<Document> key, NullWritable value, Context context) throws IOException, InterruptedException {
    String documentId = key.datum().getDocumentId().toString();
    String description = key.datum().getDescription().toString().toLowerCase();
    
    for (String s : TERMS) {
      s = trim(s.replaceAll("_", " "));
      cleanStopWord.setDescription(s);

      cleanLemmatisation.setDescription(cleanStopWord.execute());
      s = trim(cleanLemmatisation.execute());
      while (description.contains(" " + s + " ")) {
//                while (description.contains(s)) {
        StringBuilder valueBuilder = new StringBuilder();
        valueBuilder.append(s);
        valueBuilder.append("@");
        valueBuilder.append(documentId);
//                        valueBuilder.append("@");
//                        valueBuilder.append(title);
//                        valueBuilder.append("@");
//                        valueBuilder.append(date);
        context.write(new Text(valueBuilder.toString()), new IntWritable(1));
        description = description.replaceFirst(" " + s + " ", " ");
//                    description = description.replaceFirst(s, "");
      }
    }

//             Compile all the words using regex
    Pattern p = Pattern.compile("\\w+");
    Matcher m = p.matcher(description);

    // build the values and write <k,v> pairs through the context
    while (m.find()) {
      String matchedKey = m.group().toLowerCase();
      StringBuilder valueBuilder = new StringBuilder();
      valueBuilder.append(matchedKey);
      valueBuilder.append("@");
      valueBuilder.append(documentId);
      valueBuilder.append("@");
//                valueBuilder.append(title);
//                valueBuilder.append("@");
//                valueBuilder.append(date);
      // emit the partial <k,v>
      context.write(new Text(valueBuilder.toString()), new IntWritable(1));
    }
  }

  private String trim(String s) {
    while (s.endsWith(" ")) {
      s = s.substring(0, s.lastIndexOf(" "));
    }
    while (s.startsWith(" ")) {
      s = s.substring(s.indexOf(" ") + 1, s.length());
    }
    return s;
  }
}
