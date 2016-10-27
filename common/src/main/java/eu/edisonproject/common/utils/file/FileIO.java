/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.common.utils.file;

/*
 * @author Michele Sparamonti
 */
public abstract class FileIO {

  private String filePath;

  public FileIO(String filePath) {
    this.setFilePath(filePath);
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

}
