/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.common.utils;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author S. Koulouzis
 */
public class MyProperties extends Properties {

  @Override
  public String getProperty(String key) {
    String v = super.getProperty(key);
    if (v != null && v.contains("$")) {
//            String variable = v.substring(v.indexOf("$\\{"), v.indexOf("$\\{"));

      String regexString = Pattern.quote("{") + "(.*?)" + Pattern.quote("}");
      Pattern p = Pattern.compile(regexString);
      Matcher m = p.matcher(v);
      while (m.find()) {
        String matched = m.group().replaceAll("\\{", "").replaceAll("}", "");
        String envVar = System.getProperty(matched);
        if (envVar == null) {
          envVar = super.getProperty(matched);
        }
        v = v.replaceFirst("\\$", "").replaceAll("\\{" + matched + "}", envVar);
      }
    }
    return v;
  }

}
