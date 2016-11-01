/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.common.test;

import java.io.File;

/**
 *
 * @author S. Koulouzis
 */
public class TestProperties {

  public static final String TMP_IN_PATH = System.getProperty("java.io.tmpdir") + File.separator + "testInFolder";
  public static final String TMP_OUT_PATH = System.getProperty("java.io.tmpdir") + File.separator + "testOutFolder";
  public static final String STRING_CONTENTS_1 = "Tell me, O muse, of that "
          + "ingenious hero who travelled far and wide after he had sacked the "
          + "famous town of Troy. Many cities did he visit, and many were the "
          + "nations with whose manners and customs he was acquainted; moreover "
          + "he suffered much by sea while trying to save his own life and bring "
          + "his men safely home; but do what he might he could not save his men, "
          + "for they perished through their own sheer folly in eating the "
          + "cattle of the Sun-god Hyperion; so the god prevented them from ever "
          + "reaching home.";

  public static final String TEST_TEXT_FILE_NAME_1 = "tmp1.txt";

  public static final File TEST_TEXT_FILE_1 = new File(TMP_IN_PATH + File.separator + TEST_TEXT_FILE_NAME_1);

  public static final String STRING_CONTENTS_2 = "See now, how men lay blame "
          + "upon us gods for what is after all nothing but their own folly. "
          + "Look at Aegisthus; he must needs make love to Agamemnon's wife "
          + "unrighteously and then kill Agamemnon, though he knew it would be "
          + "the death of him; for I sent Mercury to warn him not to do either "
          + "of these things, inasmuch as Orestes would be sure to take his "
          + "revenge when he grew up and wanted to return home. Mercury told "
          + "him this in all good will but he would not listen, and now he has "
          + "paid for everything in full.";

  public static final String TEST_TEXT_FILE_NAME_2 = "tmp2.txt";
  public static final File TEST_TEXT_FILE_2 = new File(TMP_IN_PATH + File.separator + TEST_TEXT_FILE_NAME_2);
  public static final String STOP_WORDS_FILE_PATH = "../etc" + File.separator + "stopwords";
  public static final String MODEL_PATH = "../etc" + File.separator + "";
  
}
