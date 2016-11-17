/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.rest;

import static eu.edisonproject.classification.tfidf.mapreduce.TFIDFDriverImpl.INPUT_ITEMSET;
import static eu.edisonproject.classification.tfidf.mapreduce.TFIDFDriverImpl.NUM_OF_LINES;
import static eu.edisonproject.classification.tfidf.mapreduce.TFIDFDriverImpl.OUTPUT_PATH1;
import static eu.edisonproject.classification.tfidf.mapreduce.TFIDFDriverImpl.STOPWORDS_PATH;
import static eu.edisonproject.classification.tfidf.mapreduce.TFIDFDriverImpl.text2Avro;
import static eu.edisonproject.rest.ECO2Controller.baseCategoryFolder;
import static eu.edisonproject.rest.ECO2Controller.itemSetFile;
import static eu.edisonproject.rest.ECO2Controller.propertiesFile;
import static eu.edisonproject.rest.ECO2Controller.stopwordsFile;

import document.avro.Document;
import eu.edisonproject.classification.main.BatchMain;
import eu.edisonproject.classification.tfidf.mapreduce.TFIDFDriverImpl;
import eu.edisonproject.classification.tfidf.mapreduce.WordCountsForDocsDriver;
import eu.edisonproject.classification.tfidf.mapreduce.WordFrequencyInDocDriver;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.avro.mapreduce.AvroJob;
import org.apache.avro.mapreduce.AvroKeyInputFormat;
import org.json.simple.JSONObject;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.ToolRunner;

/**
 *
 * @author S. Koulouzis
 */
class FolderWatcherRunnable implements Runnable {

  private final String dir;

  public FolderWatcherRunnable(String dir) {
    this.dir = dir;
  }

  @Override
  public void run() {
    final Path path = FileSystems.getDefault().getPath(dir);
    System.out.println(path);

    try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
      final WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
      while (true) {
        final WatchKey wk = watchService.take();
        for (WatchEvent<?> event : wk.pollEvents()) {

          final Path changed = (Path) event.context();
          executeClassification(new File(dir + File.separator + changed));
        }
        // reset the key
        boolean valid = wk.reset();
        if (!valid) {
          System.out.println("Key has been unregisterede");
        }
      }
    } catch (IOException ex) {
      Logger.getLogger(FolderWatcherRunnable.class.getName()).log(Level.SEVERE, null, ex);
    } catch (InterruptedException ex) {
      Logger.getLogger(FolderWatcherRunnable.class.getName()).log(Level.SEVERE, null, ex);
    } catch (Exception ex) {
      Logger.getLogger(FolderWatcherRunnable.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

//  private File executeClassification(File classificationFolder) throws Exception {
//    String[] args = new String[]{"-op", "c", "-i", classificationFolder.getAbsolutePath(),
//      "-o", classificationFolder.getAbsolutePath(), "-c", baseCategoryFolder.getAbsolutePath(),
//      "-p", propertiesFile.getAbsolutePath()};
//
//    System.setProperty("itemset.file", itemSetFile.getAbsolutePath());
//    System.setProperty("stop.words.file", stopwordsFile.getAbsolutePath());
//    
//    BatchMain.main(args);
//
////      convertMRResultToCSV(classificationFolder.getAbsolutePath() + File.separator + "part-r-00000");
//    return convertMRResultToJsonFile(classificationFolder.getAbsolutePath() + File.separator + "part-r-00000");
//  }
  private File convertMRResultToJsonFile(String mrPartPath) throws IOException {
    File parent = new File(mrPartPath).getParentFile();
    Map<String, Map<String, Double>> map = new HashMap<>();
    Map<String, Double> catSimMap;
    try (BufferedReader br = new BufferedReader(new FileReader(mrPartPath))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] kv = line.split("\t");
        String fileName = kv[0];
        String cat = kv[1];
        String sim = kv[2];
        catSimMap = map.get(fileName);
        if (catSimMap == null) {
          catSimMap = new HashMap<>();
        }
        catSimMap.put(cat, Double.valueOf(sim));
        map.put(fileName, catSimMap);
      }
    }

//    JSONArray ja = new JSONArray();
//    for (String fname : map.keySet()) {
//      Map<String, Double> catMap = map.get(fname);
//      JSONObject jo = new JSONObject(catMap);
//      ja.add(jo);
//    }
    File jsonFile = new File(parent.getAbsoluteFile() + File.separator + "result.json");
    JSONObject jo = new JSONObject(map);
    try (PrintWriter out = new PrintWriter(jsonFile)) {
      out.print(jo.toJSONString());
    }
    return jsonFile;
  }

  private File executeClassification(File classificationFolder) throws IOException,
          InterruptedException, ClassNotFoundException {
    String inputPath = classificationFolder.getAbsolutePath();
    String AVRO_FILE = System.currentTimeMillis() + "-TFIDFDriverImpl-avro";
    TFIDFDriverImpl.text2Avro(inputPath, AVRO_FILE);

    String[] args = {AVRO_FILE, OUTPUT_PATH1, INPUT_ITEMSET, NUM_OF_LINES, STOPWORDS_PATH};
    System.out.println(Arrays.toString(args));

    Configuration conf = new Configuration();
    Job job = new Job(conf, "");
    job.setJarByClass(WordFrequencyInDocDriver.class);
    job.setJobName("Word Frequency In Doc Driver");

    FileSystem fs = FileSystem.get(conf);
    fs.delete(new org.apache.hadoop.fs.Path(args[1]), true);
    org.apache.hadoop.fs.Path in = new org.apache.hadoop.fs.Path(args[0]);
    org.apache.hadoop.fs.Path inHdfs = in;

    org.apache.hadoop.fs.Path dictionaryLocal = new org.apache.hadoop.fs.Path(args[2]);
    org.apache.hadoop.fs.Path dictionaryHDFS = dictionaryLocal;

    org.apache.hadoop.fs.Path stopwordsLocal = new org.apache.hadoop.fs.Path(args[4]);
    org.apache.hadoop.fs.Path stopwordsHDFS = stopwordsLocal;

    if (!conf.get(FileSystem.FS_DEFAULT_NAME_KEY).startsWith("file")) {
      inHdfs = new org.apache.hadoop.fs.Path(in.getName());
      fs.delete(inHdfs, true);
      fs.copyFromLocalFile(in, inHdfs);
      fs.deleteOnExit(inHdfs);

      dictionaryHDFS = new org.apache.hadoop.fs.Path(dictionaryLocal.getName());
      if (!fs.exists(dictionaryHDFS)) {
        fs.copyFromLocalFile(dictionaryLocal, dictionaryHDFS);
      }
      stopwordsHDFS = new org.apache.hadoop.fs.Path(stopwordsLocal.getName());
      if (!fs.exists(stopwordsHDFS)) {
        fs.copyFromLocalFile(stopwordsLocal, stopwordsHDFS);
      }
    }

    FileStatus dictionaryStatus = fs.getFileStatus(dictionaryHDFS);
    dictionaryHDFS = dictionaryStatus.getPath();
    job.addCacheFile(dictionaryHDFS.toUri());

    FileStatus stopwordsStatus = fs.getFileStatus(stopwordsHDFS);
    stopwordsHDFS = stopwordsStatus.getPath();
    job.addCacheFile(stopwordsHDFS.toUri());

    org.apache.hadoop.mapreduce.lib.input.FileInputFormat.setInputPaths(job, inHdfs);
    org.apache.hadoop.mapreduce.lib.output.FileOutputFormat.setOutputPath(job, new org.apache.hadoop.fs.Path(args[1]));

    job.setInputFormatClass(AvroKeyInputFormat.class);
    job.setMapperClass(WordFrequencyInDocDriver.WordFrequencyInDocMapper.class);
    AvroJob.setInputKeySchema(job, Document.getClassSchema());
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(IntWritable.class);

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Integer.class);
    job.setReducerClass(WordFrequencyInDocDriver.WordFrequencyInDocReducer.class);

    job.waitForCompletion(true);

    return convertMRResultToJsonFile(classificationFolder.getAbsolutePath() + File.separator + "part-r-00000");
  }

}
