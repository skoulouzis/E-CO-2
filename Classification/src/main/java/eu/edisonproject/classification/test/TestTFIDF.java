/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.edisonproject.classification.test;

import eu.edisonproject.classification.tfidf.mapreduce.TFIDFDriver;
import java.io.File;

/**
 *
 * @author Michele Sparamonti (michele.sparamonti@eng.it)
 */
public class TestTFIDF {

    public static void main(String[] args) {
        String inputPath = ".." + File.separator + "etc" + File.separator + 
                "Classification" + File.separator + "Avro Document" + File.separator;
        TFIDFDriver tfidfDriver = new TFIDFDriver("post");
        tfidfDriver.executeTFIDF(inputPath);
        //tfidfDriver.readDistancesOutputAndPrintCSV();
    }

}
