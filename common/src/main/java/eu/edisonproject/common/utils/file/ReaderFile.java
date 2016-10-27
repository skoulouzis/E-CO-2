/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.common.utils.file;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/*
 * @author Michele Sparamonti
 */
public class ReaderFile extends FileIO {

  public ReaderFile(String filePath) {
    super(filePath);
  }

  public String readFile() {
    String text = "";
    String line = null;
    try {
      try (BufferedReader br = new BufferedReader(
              new InputStreamReader(new FileInputStream(this.getFilePath())))) {
        while ((line = br.readLine()) != null) {
          text += line + " ";
        }
      }
    } catch (IOException e) {
      System.out.println("Can't read the following file: " + this.getFilePath());
    }
    return text.toLowerCase();
  }

  public String readFileWithN() {
    String text = "";
    String line = null;
    try {
      try (BufferedReader br = new BufferedReader(
              new InputStreamReader(new FileInputStream(this.getFilePath())))) {
        while ((line = br.readLine()) != null) {
          text += line + "\n";
        }
      }
    } catch (IOException e) {
      System.out.println("Can't read the following file: " + this.getFilePath());
    }
    return text;
  }

}
