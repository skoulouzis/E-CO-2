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
 * The EDISON COmpetencies ClassificatiOn (E-CO-2) service is an automated tool
 * designed to support gap analysis. It can identify the similarity of a
 * document against a set of predefined categories. It can therefore be used to
 * perform a gap analysis based on the EDISON DS-taxonomy to identify mismatches
 * between education and industry. Moreover, students, practitioners educators
 * and other stake holders can use these tools to identify the gaps in their
 * skills and competences.
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
    public static final String CSV_AVG_FILENAME = "result_avg.csv";
    public static final String JSON_AVG_FILENAME = "result_avg.json";
    public static File cvClassisifcationFolder;
    public static File jobClassisifcationFolder;
    public static File courseClassisifcationFolder;
    public static File jobAverageFolder;
    public static File jobProfileFolder;
    public static File cvAverageFolder;
    public static File cvProfileFolder;
    public static File courseAverageFolder;
    public static File courseProfileFolder;

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
        cvAverageFolder = new File(baseFolder.getAbsolutePath() + File.separator + "cvAvg");
        cvProfileFolder = new File(baseFolder.getAbsolutePath() + File.separator + "cvProfile");

        jobClassisifcationFolder = new File(baseFolder.getAbsolutePath() + File.separator + "job");
        jobAverageFolder = new File(baseFolder.getAbsolutePath() + File.separator + "jobAvg");
        jobProfileFolder = new File(baseFolder.getAbsolutePath() + File.separator + "jobProfile");

        courseClassisifcationFolder = new File(baseFolder.getAbsolutePath() + File.separator + "course");
        courseAverageFolder = new File(baseFolder.getAbsolutePath() + File.separator + "courseAvg");
        courseProfileFolder = new File(baseFolder.getAbsolutePath() + File.separator + "courseProfile");

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

    /**
     * Performs classification of a cv based on the EDISON DS-taxonomy. The
     * method is asynchronous. After the call is made an id is immediately
     * returned.
     *
     * @param jsonDoc The document to classify.
     * @return a unique id to retrieve the results
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/classification/cv")
    public final String classifyCV(final String jsonDoc) {
        String classificationId = classify(jsonDoc, "cv");
        return classificationId;
    }

    /**
     * Performs classification of a job posting based on the EDISON DS-taxonomy.
     * The method is asynchronous. After the call is made an id is immediately
     * returned.
     *
     * @param jsonDoc
     * @return a unique id to retrieve the results
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/classification/job")
    public final String classifyJob(final String jsonDoc) {
        String classificationId = classify(jsonDoc, "job");
        return classificationId;
    }

    /**
     * Performs classification of course description based on the EDISON
     * DS-taxonomy.
     *
     * The method is asynchronous. After the call is made an id is immediately
     * returned.
     *
     * @param jsonDoc
     * @return a unique id to retrieve the results
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/classification/course")
    public final String classifyCourse(final String jsonDoc) {
        String classificationId = classify(jsonDoc, "course");
        return classificationId;
    }

    /**
     * Providing the unique classification id this method returns the
     * classification results.
     *
     * @param classificationId unique classification id
     * @return the classification results
     */
    @GET
    @Path("/classification/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getClassification(@PathParam("id") final String classificationId) {

        File resultFile = getFile(classificationId, "json", "full");

        if (!resultFile.exists()) {
            return "202";
        }
        ReaderFile rf = new ReaderFile(resultFile.getAbsolutePath());
        return rf.readFile();
    }

    /**
     * Providing the unique classification id this method profiles the
     * classified document (represented by the unique classification id) against
     * the all jobs available to this service.
     *
     * The method is asynchronous. After the call is made an id is immediately
     * returned.
     *
     * @param classificationId the unique classification id (obtained from the
     * classification methods)
     * @return unique profile id
     * @throws ParseException
     * @throws IOException
     */
    @GET
    @Path("/profile/jobs/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getJobsList(@PathParam("id") final String classificationId) throws ParseException, IOException {
        return profile(classificationId, "job");
    }

    /**
     * Providing the unique classification id this method profiles the
     * classified document (represented by the unique classification id) against
     * the all courses available to this service.
     *
     * The method is asynchronous. After the call is made an id is immediately
     * returned.
     *
     * @param classificationId the unique classification id (obtained from the
     * classification methods)
     * @return unique profile id
     * @throws ParseException
     * @throws IOException
     */
    @GET
    @Path("/profile/courses/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getCourseList(@PathParam("id") final String classificationId) throws ParseException, IOException {
        return profile(classificationId, "course");
    }

    /**
     * Providing the unique classification id this method profiles the
     * classified document (represented by the unique classification id) against
     * the all CVs available to this service.
     *
     * The method is asynchronous. After the call is made an id is immediately
     * returned.
     *
     * @param classificationId the unique classification id (obtained from the
     * classification methods)
     * @return unique profile id
     * @throws ParseException
     * @throws IOException
     *
     */
    @GET
    @Path("/profile/cv/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getCVList(@PathParam("id") final String classificationId) throws ParseException, IOException {
        return profile(classificationId, "cv");
    }

    /**
     * Providing the unique profile id this method returns the profiling
     * results.
     *
     * @param profileId unique classification id
     * @return the profiling results
     */
    @GET
    @Path("/profile/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getProfile(@PathParam("id") final String profileId) {

        File resultFile = getFile(profileId, "json", "full");

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
                avgFolder = cvAverageFolder;
                trgFolder = cvProfileFolder;
                break;
            case "job":
                avgFolder = jobAverageFolder;
                trgFolder = jobProfileFolder;
                break;
            case "course":
                avgFolder = courseAverageFolder;
                trgFolder = courseProfileFolder;
                break;
        }

        long now = System.currentTimeMillis();
        UUID uid = UUID.randomUUID();
        String prpfileId = String.valueOf(now) + "_" + uid.toString();

        File targetCsvFile = getFile(id, "csv", "full");
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

    /**
     * Returns the average profile of all the classified job
     *
     * @return
     */
    @GET
    @Path("/average/job")
    @Produces(MediaType.APPLICATION_JSON)
    public String getAverageJob() {

        File resultFile = getFile(jobAverageFolder.getName(), "json", "avg");

        if (!resultFile.exists()) {
            return "202";
        }
        ReaderFile rf = new ReaderFile(resultFile.getAbsolutePath());
        return rf.readFile();
    }

    /**
     * Returns the average profile of all the classified course
     *
     * @return
     */
    @GET
    @Path("/average/course")
    @Produces(MediaType.APPLICATION_JSON)
    public String getAverageCourse() {

        File resultFile = getFile(courseAverageFolder.getName(), "json", "avg");

        if (!resultFile.exists()) {
            return "202";
        }
        ReaderFile rf = new ReaderFile(resultFile.getAbsolutePath());
        return rf.readFile();
    }

    /**
     * Returns the average profile of all the classified CV
     *
     * @return
     */
    @GET
    @Path("/average/cv")
    @Produces(MediaType.APPLICATION_JSON)
    public String getAverageCV() {

        File resultFile = getFile(cvAverageFolder.getName(), "json", "avg");

        if (!resultFile.exists()) {
            return "202";
        }
        ReaderFile rf = new ReaderFile(resultFile.getAbsolutePath());
        return rf.readFile();
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

    private File getFile(String classificationId, String format, String type) {
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
        switch (format) {
            case "json":
                switch (type) {
                    case "avg":
                        return new File(targetFolder + File.separator + JSON_AVG_FILENAME);
                    case "full":
                        return new File(targetFolder + File.separator + JSON_FILE_NAME);
                }
            case "csv":
                switch (type) {
                    case "avg":
                        return new File(targetFolder + File.separator + CSV_AVG_FILENAME);
                    case "full":
                        return new File(targetFolder + File.separator + CSV_FILE_NAME);
                }
        }

        return null;
    }

}
