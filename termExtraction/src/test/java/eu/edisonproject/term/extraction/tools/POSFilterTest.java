/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.term.extraction.tools;

import static eu.edisonproject.common.test.TestProperties.MODEL_PATH;
import static eu.edisonproject.common.test.TestProperties.STRING_CONTENTS_1;
import static eu.edisonproject.common.test.TestProperties.STRING_CONTENTS_2;
import static eu.edisonproject.common.test.TestProperties.TAGGER_PATH;
import static eu.edisonproject.common.test.TestProperties.TEST_TEXT_FILE_1;
import static eu.edisonproject.common.test.TestProperties.TEST_TEXT_FILE_2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.hadoop.util.ToolRunner;

import static eu.edisonproject.common.test.TestProperties.TMP_OUT_PATH_1;
import static eu.edisonproject.common.test.TestProperties.TMP_IN_PATH_1;
import static eu.edisonproject.common.test.TestProperties.TMP_IN_PATH_2;
import static eu.edisonproject.common.test.TestProperties.TMP_OUT_PATH_2;

/**
 *
 * @author S. Koulouzis
 */
public class POSFilterTest {

  public POSFilterTest() {
  }

  @BeforeClass
  public static void setUpClass() throws IOException {

  }

  @AfterClass
  public static void tearDownClass() throws IOException {

  }

  @Before
  public void setUp() throws IOException {
    new File(TMP_IN_PATH_1).mkdirs();
    try (PrintWriter out = new PrintWriter(new FileWriter(TEST_TEXT_FILE_1))) {
      out.println(STRING_CONTENTS_1);
    }
    try (PrintWriter out = new PrintWriter(new FileWriter(TEST_TEXT_FILE_2))) {
      out.println(STRING_CONTENTS_2);
    }
  }

  @After
  public void tearDown() throws IOException {
    TEST_TEXT_FILE_1.delete();
    TEST_TEXT_FILE_2.delete();
    FileUtils.deleteDirectory(new File(TMP_IN_PATH_1));
    FileUtils.deleteDirectory(new File(TMP_OUT_PATH_1));

    FileUtils.deleteDirectory(new File(TMP_IN_PATH_2));
    FileUtils.deleteDirectory(new File(TMP_OUT_PATH_2));

  }

  /**
   * Test of run method, of class JTopiaTermExtraction.
   */
  @Test
  public void testRun() throws Exception {
    File modelDir = new File(MODEL_PATH);
    String[] args = new String[]{TMP_IN_PATH_1, TMP_OUT_PATH_1, "stanford", "3", "2", modelDir.getAbsolutePath()};
    ToolRunner.run(new JTopiaTermExtraction(), args);

    File taggerFile = new File(TAGGER_PATH);
    args = new String[]{TMP_OUT_PATH_1, TMP_OUT_PATH_2, "JJ,JJR,JJS,VB,VBD,VBG,VBN,VBP,VBZ,RB,RBR,RBS", taggerFile.getAbsolutePath()};
    ToolRunner.run(new POSFilter(), args);
  }

}
