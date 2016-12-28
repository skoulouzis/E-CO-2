
package eu.edisonproject.rest;

import eu.edisonproject.utility.file.FolderSearch;
import eu.edisonproject.utility.file.ReaderFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

/**
 *
 * @author S. Koulouzis
 */
@Path("/e-co2/")
public class ECO2Controller {

  public static File baseCategoryFolder;
  public static File baseFolder;
  public static File propertiesFile;
  public static File itemSetFile;
  public static File stopwordsFile;
  public static final String JSON_FILE_NAME = "result.json";
  public static final String CSV_FILE_NAME = "result.csv";
  public static File cvClassisifcationFolder;
  public static File jobClassisifcationFolder;
  public static File courseClassisifcationFolder;
  public static File jobAverageFolder;
  public static File jobProfileFolder;

  public ECO2Controller() throws IOException {

  }

  public static void initPaths() {
    Properties props = new Properties();

    String baseCategoryFolderPath = props.getProperty("categories.folder",
            System.getProperty("user.home") + File.separator + "workspace"
            + File.separator + "E-CO-2" + File.separator + "Competences" + File.separator);
    baseCategoryFolder = new File(baseCategoryFolderPath);

    String baseClassificationFolderPath = props.getProperty("categories.folder",
            System.getProperty("user.home")
            + File.separator + "Downloads" + File.separator + "classificationFiles"
            + File.separator);
    baseFolder = new File(baseClassificationFolderPath);
    cvClassisifcationFolder = new File(baseFolder.getAbsolutePath() + File.separator + "cv");

    jobClassisifcationFolder = new File(baseFolder.getAbsolutePath() + File.separator + "job");
    jobAverageFolder = new File(baseFolder.getAbsolutePath() + File.separator + "jobAvg");
    jobProfileFolder = new File(baseFolder.getAbsolutePath() + File.separator + "jobProfile");
    courseClassisifcationFolder = new File(baseFolder.getAbsolutePath() + File.separator + "course");

    String propertiesPath = props.getProperty("properties.file", System.getProperty("user.home")
            + File.separator + "workspace" + File.separator + "E-CO-2" + File.separator
            + "etc" + File.separator + "configure.properties");
    propertiesFile = new File(propertiesPath);

    String itemSetPath = props.getProperty("items.set", System.getProperty("user.home")
            + File.separator + "workspace" + File.separator + "E-CO-2" + File.separator
            + "etc" + File.separator + "allTerms.csv");
    itemSetFile = new File(itemSetPath);

    String stopwordsPath = props.getProperty("stop.words", System.getProperty("user.home")
            + File.separator + "workspace" + File.separator + "E-CO-2" + File.separator
            + "etc" + File.separator + "stopwords.csv");
    stopwordsFile = new File(stopwordsPath);
  }

  //  @GET
//  @Path("/taxonomy")
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
//
//  @POST
//  @Consumes(MediaType.APPLICATION_JSON)
//  @Path("/classification/bulk")
//  public final String classifyBulk(final String jsonString) {
//    try {
//      JSONObject ja = (JSONObject) JSONValue.parseWithException(jsonString);
////      JSONArray cats = (JSONArray) ja.get("categories");
//      JSONArray docs = (JSONArray) ja.get("documents");
//      long now = System.currentTimeMillis();
//      UUID uid = UUID.randomUUID();
//      String classificationId = String.valueOf(now) + "_" + uid.toString();
//      File classificationFolder = new File(baseFolder.getAbsoluteFile() + File.separator + classificationId);
//      classificationFolder.mkdir();
//      for (Object obj : docs) {
//        JSONObject doc = (JSONObject) obj;
//        classify(doc.toJSONString());
//        String contents = (String) doc.get("contents");
//        String id = (String) doc.get("id");
//        String title = (String) doc.get("title");
//        String ext = id;
//        if (!ext.endsWith(".txt")) {
//          ext += ".txt";
//        }
//        try (PrintWriter out = new PrintWriter(classificationFolder.getAbsolutePath()
//                + File.separator + ext)) {
//          out.println(contents);
//        }
//      }
//
////      File result = doIt(classificationFolder);
//      return classificationId;
//    } catch (ParseException | FileNotFoundException ex) {
//      Logger.getLogger(ECO2Controller.class.getName()).log(Level.SEVERE, null, ex);
//    } catch (Exception ex) {
//      Logger.getLogger(ECO2Controller.class.getName()).log(Level.SEVERE, null, ex);
//    }
//    return null;
//  }
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/classification/cv")
  public final String classifyCV(final String jsonString) {
    String classificationId = classify(jsonString, "cv");
    return classificationId;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/classification/job")
  public final String classifyJob(final String jsonString) {
    String classificationId = classify(jsonString, "job");
    return classificationId;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/classification/course")
  public final String classifyCourse(final String jsonString) {
    String classificationId = classify(jsonString, "course");
    return classificationId;
  }

  @GET
  @Path("/classification/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public String getClassification(@PathParam("id") final String classificationId) {

    File resultFile = getFile(classificationId, "json");

    if (!resultFile.exists()) {
      return "202";
    }
    ReaderFile rf = new ReaderFile(resultFile.getAbsolutePath());
    return rf.readFile();

  }

  @GET
  @Path("/profile/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public String getProfile(@PathParam("id") final String classificationId) {

    File resultFile = getFile(classificationId, "json");

    if (!resultFile.exists()) {
      return "202";
    }
    ReaderFile rf = new ReaderFile(resultFile.getAbsolutePath());
    return rf.readFile();
  }

  private String profile(String id, String docType) throws IOException {
    File avgFolder = null;
    File trgFolder = null;
    switch (docType) {
      case "cv":
        break;
      case "job":
        avgFolder = jobAverageFolder;
        trgFolder = jobProfileFolder;
        break;
      case "course":
        break;
    }

    long now = System.currentTimeMillis();
    UUID uid = UUID.randomUUID();
    String prpfileId = String.valueOf(now) + "_" + uid.toString();

    File targetCsvFile = getFile(id, "csv");
    File listFile = new File(avgFolder + File.separator + CSV_FILE_NAME);
    if (!listFile.exists()) {
      throw new NotFoundException("Analisis not found");
    }

    FileUtils.copyFileToDirectory(listFile, new File(System.getProperty("java.io.tmpdir")));
    File renamedListFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "list.csv");
    FileUtils.moveFile(new File(System.getProperty("java.io.tmpdir") + File.separator + CSV_FILE_NAME), renamedListFile);
    File profileFolder = new File(trgFolder + File.separator + prpfileId);
    FileUtils.moveFileToDirectory(renamedListFile, profileFolder, true);
    FileUtils.copyFileToDirectory(targetCsvFile, profileFolder, true);

    return prpfileId;
  }

  private String classify(String jsonString, String docType) {
    try {
      File folder = null;
      switch (docType) {
        case "cv":
          folder = cvClassisifcationFolder;
          break;
        case "job":
          folder = jobClassisifcationFolder;
          break;
        case "course":
          folder = courseClassisifcationFolder;
          break;
      }

      JSONObject ja = (JSONObject) JSONValue.parseWithException(jsonString);
      long now = System.currentTimeMillis();
      UUID uid = UUID.randomUUID();
      String classificationId = String.valueOf(now) + "_" + uid.toString();

      File classificationFolder = new File(folder.getAbsoluteFile() + File.separator + classificationId);
      classificationFolder.mkdir();

      JSONObject doc = (JSONObject) ja;
      String id = (String) doc.get("id");
      String contents = (String) doc.get("contents");
      String title = (String) doc.get("title");
      if (!id.endsWith(".txt")) {
        id += ".txt";
      }
      try (PrintWriter out = new PrintWriter(classificationFolder.getAbsolutePath()
              + File.separator + id)) {
        out.println(contents);
      }

//      File result = doIt(classificationFolder);
      return classificationId;
    } catch (ParseException | FileNotFoundException ex) {
      Logger.getLogger(ECO2Controller.class.getName()).log(Level.SEVERE, null, ex);
      throw new javax.ws.rs.ServerErrorException("Malformed JSON message", Response.serverError().build());
    } catch (Exception ex) {
      Logger.getLogger(ECO2Controller.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  @GET
  @Path("/profile/jobs/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public String getJobsList(@PathParam("id") final String classificationId) throws ParseException, IOException {
    return profile(classificationId, "job");
  }

  private File getFile(String classificationId, String type) {
    FolderSearch fs = new FolderSearch(baseFolder, classificationId, true);
    File targetFolder;
    try {
      Set<String> res = fs.search();
      if (!res.isEmpty()) {
        targetFolder = new File(res.iterator().next());
      } else {
        throw new NotFoundException(String.format("Classification %s not found", classificationId));
      }
    } catch (IOException ex) {
      Logger.getLogger(ECO2Controller.class.getName()).log(Level.SEVERE, null, ex);
      throw new NotFoundException(String.format("Classification %s not found", classificationId));
    }

    if (!targetFolder.exists()) {
      throw new NotFoundException(String.format("Classification %s not found", classificationId));
    }
    switch (type) {
      case "json":
        return new File(targetFolder + File.separator + JSON_FILE_NAME);
      case "csv":
        return new File(targetFolder + File.separator + CSV_FILE_NAME);
    }

    return null;
  }

}
