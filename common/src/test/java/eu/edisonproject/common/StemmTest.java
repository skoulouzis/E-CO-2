/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.common;

import static eu.edisonproject.common.test.TestProperties.TEST_TEXT_FILE_1;
import static eu.edisonproject.common.test.TestProperties.TEST_TEXT_FILE_2;
import static eu.edisonproject.common.test.TestProperties.STRING_CONTENTS_1;
import static eu.edisonproject.common.test.TestProperties.STRING_CONTENTS_2;
import static eu.edisonproject.common.test.TestProperties.STOP_WORDS_FILE_PATH;

import eu.edisonproject.common.tools.Stemm;
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

import static eu.edisonproject.common.test.TestProperties.TMP_OUT_PATH_1;
import static eu.edisonproject.common.test.TestProperties.TMP_IN_PATH_1;

/**
 *
 * @author S. Koulouzis
 */
public class StemmTest {

  @BeforeClass
  public static void setUpClass() throws IOException {
    new File(TMP_IN_PATH_1).mkdirs();
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
    FileUtils.deleteDirectory(new File(TMP_IN_PATH_1));
    FileUtils.deleteDirectory(new File(TMP_OUT_PATH_1));
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
    String[] args = new String[]{TMP_IN_PATH_1, TMP_OUT_PATH_1};
    ToolRunner.run(new Stemm(), args);

//    File outPath = new File(TMP_OUT_PATH_1);
//    for (File f : outPath.listFiles()) {
//      if (f.getName().contains("part") && !f.getName().endsWith(".crc")) {
//      }
//    }
  }

}
