/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

/**
 *
 * @author S. Koulouzis
 */
@Path("/e-coco/")
public class ECO2Controller {

  private final File baseCategoryFolder = new File(System.getProperty("user.home") + File.separator + "workspace" + File.separator + "E-CO-2" + File.separator + "Competences" + File.separator);

  @GET
  @Path("/categories")
  @Produces(MediaType.APPLICATION_JSON)
  public String available() {
    JSONArray ja = new JSONArray();

    Iterator<File> iter = FileUtils.iterateFiles(baseCategoryFolder, null, true);
    while (iter.hasNext()) {
      File f = iter.next();
      Map<String, String> map = new HashMap();
      if (f.getName().endsWith("csv")) {
        map.put("name", f.getName());
      }
      if (f.getName().endsWith("desc")) {
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
          StringBuilder sb = new StringBuilder();
          String line = br.readLine();

          while (line != null) {
            sb.append(line);
            sb.append(System.lineSeparator());
            line = br.readLine();
          }
          String everything = sb.toString();
          map.put("description", everything);
        } catch (FileNotFoundException ex) {
          Logger.getLogger(ECO2Controller.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
          Logger.getLogger(ECO2Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      JSONObject jo = new JSONObject(map);
      ja.add(jo);
    }
    return ja.toString();
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public final String classifyText() {
    return String.valueOf(this.hashCode());
  }

}
