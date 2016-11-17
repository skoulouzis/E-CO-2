/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.rest;

import static eu.edisonproject.rest.ECO2Controller.baseCategoryFolder;
import static eu.edisonproject.rest.ECO2Controller.itemSetFile;
import static eu.edisonproject.rest.ECO2Controller.propertiesFile;
import static eu.edisonproject.rest.ECO2Controller.stopwordsFile;

import eu.edisonproject.classification.main.BatchMain;
import eu.edisonproject.utility.file.ConfigHelper;
import eu.edisonproject.utility.file.MyProperties;
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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;

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

  private File executeClassification(File classificationFolder) throws Exception {
//    String[] args = new String[]{"-op", "c", "-i", classificationFolder.getAbsolutePath(),
//      "-o", classificationFolder.getAbsolutePath(), "-c", baseCategoryFolder.getAbsolutePath(),
//      "-p", propertiesFile.getAbsolutePath()};

    MyProperties prop = ConfigHelper.getProperties(propertiesFile.getAbsolutePath());
    System.setProperty("itemset.file", itemSetFile.getAbsolutePath());
    System.setProperty("stop.words.file", stopwordsFile.getAbsolutePath());

    BatchMain.calculateTFIDF(classificationFolder.getAbsolutePath(), classificationFolder.getAbsolutePath(), baseCategoryFolder.getAbsolutePath(), prop);
//    BatchMain.main(args);

//      convertMRResultToCSV(classificationFolder.getAbsolutePath() + File.separator + "part-r-00000");
    return convertMRResultToJsonFile(classificationFolder.getAbsolutePath() + File.separator + "part-r-00000");

  }

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

}
