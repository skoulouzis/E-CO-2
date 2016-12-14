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
package eu.edisonproject.utility.execute;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 *
 * @author S. Koulouzis
 */
public class Main {

  public static void main(String args[]) {
    Options options = new Options();
    Option operation = new Option("op", "operation", true, "type of operation to perform. "
            + "To move cached terms from org.mapdb.DB 'm'");
    operation.setRequired(true);
    options.addOption(operation);

    Option input = new Option("i", "input", true, "input file path");
    input.setRequired(true);
    options.addOption(input);

    String helpmasg = "Usage: \n";
    for (Object obj : options.getOptions()) {
      Option op = (Option) obj;
      helpmasg += op.getOpt() + ", " + op.getLongOpt() + "\t Required: " + op.isRequired() + "\t\t" + op.getDescription() + "\n";
    }

    try {
      CommandLineParser parser = new BasicParser();
      CommandLine cmd = parser.parse(options, args);

      switch (cmd.getOptionValue("operation")) {
        case "m":
//                    DBTools.portTermCache2Hbase(cmd.getOptionValue("input"));
//                    DBTools.portBabelNetCache2Hbase(cmd.getOptionValue("input"));
          break;
        default:
          System.out.println(helpmasg);
      }

    } catch (Exception ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, helpmasg, ex);
    }
  }
}
