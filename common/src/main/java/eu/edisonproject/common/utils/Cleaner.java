/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.common.utils;

/*
 * @author Michele Sparamonti
 */
public abstract class Cleaner {

  private String description;

  public abstract String execute();

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
