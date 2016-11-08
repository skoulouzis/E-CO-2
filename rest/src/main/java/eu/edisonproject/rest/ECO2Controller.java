/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.rest;

import eu.edisonproject.classification.main.BatchMain;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

/**
 *
 * @author S. Koulouzis
 */
@Path("/e-co2/")
public class ECO2Controller {

  private final File baseCategoryFolder;
  private final File baseClassisifcationFolder;
  private final File propertiesFile;
  private final File itemSetFile;
  private final File stopwordsFile;

  public ECO2Controller(Properties props) {
    String baseCategoryFolderPath = props.getProperty("categories.folder",
            System.getProperty("user.home") + File.separator + "workspace"
            + File.separator + "E-CO-2" + File.separator + "Competences" + File.separator);
    baseCategoryFolder = new File(baseCategoryFolderPath);

    String baseClassificationFolderPath = props.getProperty("categories.folder",
            System.getProperty("user.home")
            + File.separator + "Downloads" + File.separator + "classificationFiles"
            + File.separator);
    baseClassisifcationFolder = new File(baseClassificationFolderPath);

    String propertiesPath = props.getProperty("properties.file", System.getProperty("user.home")
            + File.separator + "workspace" + File.separator + "E-CO-2" + File.separator
            + "etc" + File.separator + "configure.properties");
    propertiesFile = new File(propertiesPath);

    String itemSetPath = props.getProperty("items.set", System.getProperty("user.home")
            + File.separator + "workspace" + File.separator + "E-CO-2" + File.separator
            + "etc" + File.separator + "dictionaryAll.csv");
    itemSetFile = new File(itemSetPath);

    String stopwordsPath = props.getProperty("stop.words", System.getProperty("user.home")
            + File.separator + "workspace" + File.separator + "E-CO-2" + File.separator
            + "etc" + File.separator + "stopwords.csv");
    stopwordsFile = new File(stopwordsPath);
  }

//  @GET
//  @Path("/categories")
//  @Produces(MediaType.APPLICATION_JSON)
//  public String available() {
//    JSONArray ja = new JSONArray();
//    Iterator<File> iter = FileUtils.iterateFiles(baseCategoryFolder, new String[]{"csv", "desc"}, true);
//    while (iter.hasNext()) {
//      File f = iter.next();
//      Map<String, String> map = new HashMap();
//      if (f.getName().endsWith("csv")) {
//        map.put("name", FilenameUtils.removeExtension(f.getName()));
//      }
//      if (f.getName().endsWith("desc")) {
//        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
//          StringBuilder sb = new StringBuilder();
//          String line = br.readLine();
//
//          while (line != null) {
//            sb.append(line).append(" ");
//            line = br.readLine();
//          }
//          String everything = sb.toString();
//          map.put("description", everything);
//        } catch (FileNotFoundException ex) {
//          Logger.getLogger(ECO2Controller.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//          Logger.getLogger(ECO2Controller.class.getName()).log(Level.SEVERE, null, ex);
//        }
//      }
//      if (map.size() > 0) {
//        JSONObject jo = new JSONObject(map);
//        ja.add(jo);
//      }
//    }
//    return ja.toString();
//  }
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/classify/bulk")
  public final String classifyBulk(final String jsonString) {
    try {
      JSONObject ja = (JSONObject) JSONValue.parseWithException(jsonString);
//      JSONArray cats = (JSONArray) ja.get("categories");
      JSONArray docs = (JSONArray) ja.get("documents");
      long now = System.currentTimeMillis();
      File classificationFolder = new File(baseClassisifcationFolder.getAbsoluteFile() + File.separator + now);
      classificationFolder.mkdir();
      for (Object obj : docs) {
        JSONObject doc = (JSONObject) obj;
        classify(doc.toJSONString());
        String text = (String) doc.get("text");
        String title = (String) doc.get("id");
        if (!title.endsWith(".txt")) {
          title += ".txt";
        }
        try (PrintWriter out = new PrintWriter(classificationFolder.getAbsolutePath()
                + File.separator + title)) {
          out.println(text);
        }
      }

      File result = doIt(classificationFolder);

      return String.valueOf(now);
    } catch (ParseException | FileNotFoundException ex) {
      Logger.getLogger(ECO2Controller.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(ECO2Controller.class.getName()).log(Level.SEVERE, null, ex);
    } catch (Exception ex) {
      Logger.getLogger(ECO2Controller.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/classify/doc")
  public final String classify(final String jsonString) {
    try {
      JSONObject ja = (JSONObject) JSONValue.parseWithException(jsonString);
      long now = System.currentTimeMillis();
      File classificationFolder = new File(baseClassisifcationFolder.getAbsoluteFile() + File.separator + now);
      classificationFolder.mkdir();

      JSONObject doc = (JSONObject) ja;
      String text = (String) doc.get("text");
      String title = (String) doc.get("id");
      if (!title.endsWith(".txt")) {
        title += ".txt";
      }
      try (PrintWriter out = new PrintWriter(classificationFolder.getAbsolutePath()
              + File.separator + title)) {
        out.println(text);
      }

      File result = doIt(classificationFolder);

      return String.valueOf(now);
    } catch (ParseException | FileNotFoundException ex) {
      Logger.getLogger(ECO2Controller.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(ECO2Controller.class.getName()).log(Level.SEVERE, null, ex);
    } catch (Exception ex) {
      Logger.getLogger(ECO2Controller.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  @GET
  @Path("/classification/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public String available(@PathParam("id") final String id) {
    JSONArray ja = new JSONArray();
    File classificationFolder = new File(baseClassisifcationFolder.getAbsoluteFile() + File.separator + id);

    return ja.toString();
  }

  private void convertMRResultToCSV(String mrPartPath) throws IOException {
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

    Set<String> fileNames = map.keySet();
    StringBuilder header = new StringBuilder();
    header.append(" ").append(",");
    for (Map<String, Double> m : map.values()) {
      for (String c : m.keySet()) {
        header.append(c).append(",");
      }
      break;
    }
    header.deleteCharAt(header.length() - 1);
    header.setLength(header.length());

    for (String fName : fileNames) {
      StringBuilder csvLine = new StringBuilder();

      csvLine.append(fName).append(",");
      catSimMap = map.get(fName);
      for (String cat : catSimMap.keySet()) {
        Double sim = catSimMap.get(cat);
        csvLine.append(sim).append(",");
      }
      csvLine.deleteCharAt(csvLine.length() - 1);
      csvLine.setLength(csvLine.length());

//      System.err.println(csvLine.toString());
    }
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

  private File doIt(File classificationFolder) throws Exception {
    String[] args = new String[]{"-op", "c", "-i", classificationFolder.getAbsolutePath(),
      "-o", classificationFolder.getAbsolutePath(), "-c", baseCategoryFolder.getAbsolutePath(),
      "-p", propertiesFile.getAbsolutePath()};

    System.setProperty("itemset.file", itemSetFile.getAbsolutePath());
    System.setProperty("stop.words.file", stopwordsFile.getAbsolutePath());

    BatchMain.main(args);

//      convertMRResultToCSV(classificationFolder.getAbsolutePath() + File.separator + "part-r-00000");
    return convertMRResultToJsonFile(classificationFolder.getAbsolutePath() + File.separator + "part-r-00000");

  }
}
