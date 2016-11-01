/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.edisonproject.disambiguation.tools;

import static eu.edisonproject.common.test.TestProperties.MODEL_PATH;
import static eu.edisonproject.common.test.TestProperties.N_GRAMS_FILE_PATH;
import static eu.edisonproject.common.test.TestProperties.STOP_WORDS_FILE_PATH;
import static eu.edisonproject.common.test.TestProperties.STRING_CONTENTS_1;
import static eu.edisonproject.common.test.TestProperties.STRING_CONTENTS_2;
import static eu.edisonproject.common.test.TestProperties.TEST_TEXT_FILE_1;
import static eu.edisonproject.common.test.TestProperties.TEST_TEXT_FILE_2;
import static eu.edisonproject.common.test.TestProperties.TMP_IN_PATH_1;
import static eu.edisonproject.common.test.TestProperties.TMP_IN_PATH_2;
import static eu.edisonproject.common.test.TestProperties.TMP_OUT_PATH_1;
import static eu.edisonproject.common.test.TestProperties.TMP_OUT_PATH_2;
import static eu.edisonproject.common.test.TestProperties.TMP_OUT_PATH_3;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import eu.edisonproject.common.tools.Stopwords;
import eu.edisonproject.term.extraction.tools.JTopiaTermExtraction;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.util.ToolRunner;

/**
 *
 * @author alogo
 */
public class WikipediaDisambiguationTest {

  public WikipediaDisambiguationTest() {
  }

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
    FileUtils.deleteDirectory(new File(TMP_IN_PATH_2));
    FileUtils.deleteDirectory(new File(TMP_OUT_PATH_1));
    FileUtils.deleteDirectory(new File(TMP_OUT_PATH_2));
    FileUtils.deleteDirectory(new File(TMP_OUT_PATH_3));
  }

  /**
   * Test of run method, of class Disambiguation.
   */
  @Test
  public void testRun() throws Exception {

    File stopwords = new File(STOP_WORDS_FILE_PATH);
    File ngramsFile = new File(N_GRAMS_FILE_PATH);

    String[] args = new String[]{TMP_IN_PATH_1, TMP_OUT_PATH_1, stopwords.getAbsolutePath()};
    ToolRunner.run(new Stopwords(), args);

    File modelDir = new File(MODEL_PATH);
    args = new String[]{TMP_OUT_PATH_1, TMP_OUT_PATH_2, "stanford", "3", "2", modelDir.getAbsolutePath()};
    ToolRunner.run(new JTopiaTermExtraction(), args);

    String numOfTerms = "2";
    String offsetTerms = "0";
    String minimumSimilarity = "-10";
    String className = eu.edisonproject.disambiguation.WikipediaOnline.class.getName();
    args = new String[]{TMP_OUT_PATH_2, TMP_OUT_PATH_3, numOfTerms, offsetTerms, minimumSimilarity, className, stopwords.getAbsolutePath(), ngramsFile.getAbsolutePath()};
    ToolRunner.run(new Disambiguation(), args);
  }

}
