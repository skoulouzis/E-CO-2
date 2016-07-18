/*
 * Copyright 2016 S. Koulouzis.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.edisonproject.utility.test;

import eu.edisonproject.utility.file.DBTools;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.parser.ParseException;

/**
 *
 * @author S. Koulouzis
 */
public class TestDBTools {

    public static void main(String args[]) {
        try {
            testPortTerms();
        } catch (IOException | InterruptedException | ParseException ex) {
            Logger.getLogger(TestDBTools.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void testPortTerms() throws IOException, InterruptedException, ParseException {
        String path = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "scripts" + File.separator + "crawl_linkedin" + File.separator + "cache" + File.separator + "en.wikipedia.org.cacheDB";
        DBTools.portTermCache2Hbase(path);
    }

}
