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
package eu.edisonproject.training.term.extraction.test;

import eu.edisonproject.utility.commons.Term;
import eu.edisonproject.utility.commons.TermAvroSerializer;
import eu.edisonproject.utility.commons.TermFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.json.simple.parser.ParseException;

/**
 *
 * @author S. Koulouzis
 */
public class Main {

  public static void main(String args[]) {
    try {
//            hBaseExample();
//            testAvroSerializer();
    } catch (Exception ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private static void hBaseExample() throws ZooKeeperConnectionException, IOException, ParseException {
    Configuration config = HBaseConfiguration.create();
    Connection conn = ConnectionFactory.createConnection(config);

    try (Admin admin = conn.getAdmin()) {

      TableName tblName = TableName.valueOf("terms");

      if (!admin.tableExists(tblName)) {
        createTableDescriptor(tblName, admin);
      }
      try (Table tbl = conn.getTable(tblName)) {
        populateTable(tbl, admin, tblName);
      }

      try (Table tbl = conn.getTable(tblName)) {
        Set<String> terms = getPossibleTermsFromDB("somthing", tbl);
        for (String s : terms) {
          Term t = TermFactory.create(s);
          System.err.println(t);
        }
      }

      try (Table tbl = conn.getTable(tblName)) {
        String jsonString = getTermFromDB("43620318", tbl);
        System.err.println(jsonString);

      }

    }

  }

  private static void createTableDescriptor(TableName peopleTblName, Admin admin) throws IOException {

    // create the table...
    HTableDescriptor tableDescriptor = new HTableDescriptor(peopleTblName);
//        HColumnDescriptor uid = new HColumnDescriptor("uid");
//        tableDescriptor.addFamily(uid);

//        HColumnDescriptor lemma = new HColumnDescriptor("lemma");
//        tableDescriptor.addFamily(lemma);
//
//        HColumnDescriptor glosses = new HColumnDescriptor("glosses");
//        tableDescriptor.addFamily(glosses);
//
//        HColumnDescriptor originalTerm = new HColumnDescriptor("originalTerm");
//        tableDescriptor.addFamily(originalTerm);
//
//        HColumnDescriptor url = new HColumnDescriptor("url");
//        tableDescriptor.addFamily(url);
//
//        HColumnDescriptor categories = new HColumnDescriptor("categories");
//        tableDescriptor.addFamily(categories);
//
//        HColumnDescriptor altLables = new HColumnDescriptor("altLables");
//        tableDescriptor.addFamily(altLables);
//
//        HColumnDescriptor buids = new HColumnDescriptor("buids");
//        tableDescriptor.addFamily(buids);
//
//        HColumnDescriptor nuids = new HColumnDescriptor("nuids");
//        tableDescriptor.addFamily(nuids);
//
//        HColumnDescriptor confidence = new HColumnDescriptor("confidence");
//        tableDescriptor.addFamily(confidence);
//
    HColumnDescriptor ambiguousTerm = new HColumnDescriptor("ambiguousTerm");
    tableDescriptor.addFamily(ambiguousTerm);

    HColumnDescriptor jsonString = new HColumnDescriptor("jsonString");
    tableDescriptor.addFamily(jsonString);
    admin.createTable(tableDescriptor);
  }

  private static void populateTable(Table tbl, Admin admin, TableName peopleTblName) throws IOException, ParseException {
    List<Term> terms = createTerms();
    int count = 0;
    for (Term t : terms) {
      Put put = new Put(Bytes.toBytes(t.getUid().toString()));
      String jsonStr = TermFactory.term2Json(t).toJSONString();
      put.addColumn(Bytes.toBytes("jsonString"), Bytes.toBytes("jsonString"), Bytes.toBytes(jsonStr));

      if (count > 1) {
        put.addColumn(Bytes.toBytes("ambiguousTerm"), Bytes.toBytes("ambiguousTerm"), Bytes.toBytes("somthing"));
      } else {
        put.addColumn(Bytes.toBytes("ambiguousTerm"), Bytes.toBytes("ambiguousTerm"), Bytes.toBytes("somthingElse"));
      }

      tbl.put(put);
      count++;
    }
    admin.flush(peopleTblName);
  }

  private static void testAvroSerializer() throws IOException, ParseException {
    List<Term> terms = createTerms();
    TermAvroSerializer ts = new TermAvroSerializer(System.getProperty("java.io.tmpdir") + File.separator + "terms.avro", Term.SCHEMA$);
    for (Term t : terms) {
      ts.serialize(t);
    }
    ts.close();
  }

  private static List<Term> createTerms() throws IOException, ParseException {
    String[] jsonTerms = new String[]{"{\"uid\":\"43620318\","
      + "\"nuids\":null,"
      + "\"originalTerm\":\"platform\","
      + "\"buids\":null,"
      + "\"glosses\":[\"platformism is a tendency (or organized school of thought) within the anarchist movement. it stresses the need for tightly organized anarchist organizations that are able to influence working class and peasant movements.\\n\\\"platformist\\\" groups reject the model of leninist vanguardism. they aim, instead, to \\\"make anarchist ideas the leading ideas within the class struggle\\\". the four main principles by which an anarchist organisation should operate, according to platformists, are ideological unity, tactical unity, collective responsibility, and federalism.\\nin general, platformist groups aim to win the widest possible influence for anarchist ideas and methods in the working class and peasantry\\u2014like especifismo groups, platformists orient towards the working class, rather than to the extreme the left. this usually entails a willingness to work in single-issue campaigns, trade unionism and community groups, and to fight for immediate reforms while linking this to a project of building popular consciousness and organisation. they therefore reject approaches that they believe will prevent this, such as insurrectionist anarchism, as well as \\\"views that dismiss activity in the unions\\\" or that dismiss anti-imperialist movements.\\nthe name \\\"platformist\\\" derives from the 1926 organisational platform of the general union of anarchists (draft). this was published by the group of russian anarchists abroad, in their journal dielo truda (\\\"workers' cause\\\" in russian).\\nthe group, which consisted of exiled russian anarchist veterans of the 1917 october revolution (notably nestor makhno who played a leading role in the anarchist revolution in the ukraine of 1918\\u20131921), based the platform on their experiences of the revolution, and the eventual victory of the bolsheviks over the anarchists and other groups. the platform attempted to address and explain the anarchist movement's failures during the russian revolution outside of the ukraine.\\nthe document drew both praise and criticism from anarchists worldwide and sparked a major debate within the anarchist movement.\\ntoday \\\"platformism\\\" is an important current in international anarchism. around thirty platformist and especifista organisations are linked together in the anarkismo.net project, including groups from africa, latin america, north america and europe. at least in terms of the number of affiliated organisations (if not in actual membership in some countries), the anarkismo network is larger than other anarchist international bodies, like the synthesist international of anarchist federations and the anarcho-syndicalist international workers' association. it is not, however, a formal \\\"international\\\" and has no intention of competing with these other formations.\\n\\n\"],"
      + "\"confidence\":0.04476283112493827,"
      + "\"lemma\":\"platformism\","
      + "\"categories\":[\"anarchism_by_form\",\"anarchist_communism\",\"makhnovism\",\"platformism\",\"political_theories\"],"
      + "\"altLables\":null,"
      + "\"url\":\"https:\\/\\/en.wikipedia.org\\/?curid=43620318\"}",
      "{\"uid\":\"487132\","
      + "\"nuids\":null,"
      + "\"originalTerm\":\"analytics\","
      + "\"buids\":null,"
      + "\"glosses\":[\"analytics is the discovery, interpretation, and communication of meaningful patterns in data. especially valuable in areas rich with recorded information, analytics relies on the simultaneous application of statistics, computer programming and operations research to quantify performance. analytics often favors data visualization to communicate insight.\\nfirms may apply analytics to business data to describe, predict, and improve business performance. specifically, areas within analytics include predictive analytics, prescriptive analytics, enterprise decision management, retail analytics, store assortment and stock-keeping unit optimization, marketing optimization and marketing mix modeling, web analytics, sales force sizing and optimization, price and promotion modeling, predictive science, credit risk analysis, and fraud analytics. since analytics can require extensive computation (see big data), the algorithms and software used for analytics harness the most current methods in computer science, statistics, and mathematics.\"],"
      + "\"confidence\":0.0415106570049238,"
      + "\"lemma\":\"analytics\","
      + "\"isFromDictionary\":false,"
      + "\"categories\":[\"analytics\",\"big_data\",\"business_intelligence\",\"business_terms\"],"
      + "\"altLables\":null,"
      + "\"url\":\"https:\\/\\/en.wikipedia.org\\/?curid=487132\"}",
      "{\"uid\":\"bn:00046705n\","
      + "\"nuids\":null,"
      + "\"originalTerm\":\"data\","
      + "\"buids\":[\"bn:00020452n\",\"bn:00070574n\",\"bn:00045822n\",\"bn:00021547n\"],"
      + "\"glosses\":[\"knowledge acquired through study or experience or instruction\",\"sequence of symbols that can be interpreted as a message by a subject\",\"all facts, ideas or imaginative works of the mind which have been communicated, published or distributed formally or informally in any format, or the knowledge that is communicated or received.\",\"things that are or can be known about a given topic; communicable knowledge of something.\",\"information is the result of processing, manipulating and organizing data in a way that adds to the knowledge of the person receiving it.\"],\"confidence\":0.03582541404307744,\"lemma\":\"data\",\"isFromDictionary\":false,\"categories\":[\"concepts_in_metaphysics\",\"information\",\"information,_knowledge,_and_uncertainty\",\"information_science\"],"
      + "\"altLables\":[\"data\"],"
      + "\"url\":\"http:\\/\\/babelnet.org\\/synset?word=bn%3A00046705n\"}"};

    List<Term> terms = new ArrayList<>();
    for (String jt : jsonTerms) {
      Term t = TermFactory.create(jt);
      terms.add(t);
    }
    return terms;
  }

  private static Set<String> getPossibleTermsFromDB(String term, Table tbl) throws IOException, ParseException {

    Scan scan = new Scan();
    scan.addFamily(Bytes.toBytes("ambiguousTerm"));
    scan.addFamily(Bytes.toBytes("jsonString"));

    ResultScanner resultScanner = tbl.getScanner(scan);
    Iterator<Result> results = resultScanner.iterator();
    Set<String> jsonTerms = new HashSet<>();
    while (results.hasNext()) {
      Result r = results.next();

      String ambiguousTerm = Bytes.toString(r.getValue(Bytes.toBytes("ambiguousTerm"), Bytes.toBytes("ambiguousTerm")));
      if (ambiguousTerm.equals(term)) {
//                String uid = Bytes.toString(r.getRow());
        String jsonStr = Bytes.toString(r.getValue(Bytes.toBytes("jsonString"), Bytes.toBytes("jsonString")));
        jsonTerms.add(jsonStr);
      }

    }
    return jsonTerms;
  }

  private static String getTermFromDB(String id, Table tbl) throws IOException {

    Get get = new Get(Bytes.toBytes(id));
    get.addFamily(Bytes.toBytes("jsonString"));
    Result r = tbl.get(get);
    return Bytes.toString(r.getValue(Bytes.toBytes("jsonString"), Bytes.toBytes("jsonString")));
  }

}
