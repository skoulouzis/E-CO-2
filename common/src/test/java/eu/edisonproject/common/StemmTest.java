/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.common;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.util.ToolRunner;

/**
 *
 * @author S. Koulouzis
 */
public class StemmTest {

  private static final String TMP_IN_PATH = System.getProperty("java.io.tmpdir") + File.separator + "testInFolder";
  private static final String TMP_OUT_PATH = System.getProperty("java.io.tmpdir") + File.separator + "testOutFolder";
  private static final String STRING_CONTENTS_1 = "Tell me, O muse, of that "
          + "ingenious hero who travelled far and wide after he had sacked the "
          + "famous town of Troy. Many cities did he visit, and many were the "
          + "nations with whose manners and customs he was acquainted; moreover "
          + "he suffered much by sea while trying to save his own life and bring "
          + "his men safely home; but do what he might he could not save his men, "
          + "for they perished through their own sheer folly in eating the "
          + "cattle of the Sun-god Hyperion; so the god prevented them from ever "
          + "reaching home.";

  private static final String TEST_TEXT_FILE_NAME_1 = "tmp1.txt";

  private static final File TEST_TEXT_FILE_1 = new File(TMP_IN_PATH + File.separator + TEST_TEXT_FILE_NAME_1);

  private static final String STRING_CONTENTS_2 = "See now, how men lay blame "
          + "upon us gods for what is after all nothing but their own folly. "
          + "Look at Aegisthus; he must needs make love to Agamemnon's wife "
          + "unrighteously and then kill Agamemnon, though he knew it would be "
          + "the death of him; for I sent Mercury to warn him not to do either "
          + "of these things, inasmuch as Orestes would be sure to take his "
          + "revenge when he grew up and wanted to return home. Mercury told "
          + "him this in all good will but he would not listen, and now he has "
          + "paid for everything in full.";

  private static final String TEST_TEXT_FILE_NAME_2 = "tmp2.txt";
  private static final File TEST_TEXT_FILE_2 = new File(TMP_IN_PATH + File.separator + TEST_TEXT_FILE_NAME_2);
  private static final String STOP_WORDS_FILE_PATH = "../etc" + File.separator + "stopwords";

  @BeforeClass
  public static void setUpClass() throws IOException {
    new File(TMP_IN_PATH).mkdirs();
    try (PrintWriter out = new PrintWriter(new FileWriter(TEST_TEXT_FILE_1))) {
      out.println(STRING_CONTENTS_1);
    }
    try (PrintWriter out = new PrintWriter(new FileWriter(TEST_TEXT_FILE_2))) {
      out.println(STRING_CONTENTS_2);
    }
  }

  @AfterClass
  public static void tearDownClass() throws IOException {
    TEST_TEXT_FILE_1.delete();
    TEST_TEXT_FILE_2.delete();
    FileUtils.deleteDirectory(new File(TMP_IN_PATH));
    FileUtils.deleteDirectory(new File(TMP_OUT_PATH));
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of run method, of class Stopwords.
   *
   * @throws java.lang.Exception
   */
  @org.junit.Test
  public void testRun() throws Exception {
    File stopwordFile = new File(STOP_WORDS_FILE_PATH);
    String[] args = new String[]{TMP_IN_PATH, TMP_OUT_PATH, stopwordFile.getAbsolutePath()};
    ToolRunner.run(new Stemm(), args);

//    File outPath = new File(TMP_OUT_PATH);
//    for (File f : outPath.listFiles()) {
//      if (f.getName().contains("part") && !f.getName().endsWith(".crc")) {
//      }
//    }
  }

}
