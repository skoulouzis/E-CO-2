/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.term.extraction.tools;

import static eu.edisonproject.common.test.TestProperties.STRING_CONTENTS_1;
import static eu.edisonproject.common.test.TestProperties.STRING_CONTENTS_2;
import static eu.edisonproject.common.test.TestProperties.TEST_TEXT_FILE_1;
import static eu.edisonproject.common.test.TestProperties.TEST_TEXT_FILE_2;
import static eu.edisonproject.common.test.TestProperties.TMP_IN_PATH;
import static eu.edisonproject.common.test.TestProperties.TMP_OUT_PATH;

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

/**
 *
 * @author S. Koulouzis
 */
public class JTopiaTermExtractionTest {

  public JTopiaTermExtractionTest() {
  }

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
   * Test of run method, of class JTopiaTermExtraction.
   */
  @Test
  public void testRun() throws Exception {
    String[] args = new String[]{TMP_IN_PATH, TMP_OUT_PATH, "stanford", "3", "2",MODEL_PATH};
    ToolRunner.run(new JTopiaTermExtraction(), args);
  }

}
