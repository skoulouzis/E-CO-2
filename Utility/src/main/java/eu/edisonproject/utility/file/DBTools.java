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
package eu.edisonproject.utility.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import eu.edisonproject.utility.commons.Term;
import eu.edisonproject.utility.commons.TermFactory;
import org.json.simple.parser.ParseException;

/**
 *
 * @author S. Koulouzis
 */
public class DBTools {

    private static DB db;

    public static final TableName EDGES_TBL_NAME = TableName.valueOf("edges");
    public static final TableName SYNSET_TBL_NAME = TableName.valueOf("synset");
    public static final TableName WORDS_TBL_NAME = TableName.valueOf("words");
    public static final TableName DISAMBIGUATE_TBL_NAME = TableName.valueOf("disambiguate");
    public static final TableName TERMS_TBL_NAME = TableName.valueOf("terms");
    private static Connection conn;
    private static boolean wordsTableCreated;
    private static boolean edgesTableCreated;

    public static void portTermCache2Hbase(String path) throws IOException, InterruptedException, ParseException {
        File cacheDBFile = new File(path);
        Map<String, Set<String>> termCache = getTermCache(cacheDBFile);
        for (String id : termCache.keySet()) {
            Set<String> val = termCache.get(id);
            Logger.getLogger(DBTools.class.getName()).log(Level.INFO, "Adding: " + id);
            addPossibleTermsToDB(id, val);
        }
    }

    public static void portBabelNetCache2Hbase(String path) throws IOException, InterruptedException {
        File cacheDBFile = new File(path);
        Map<String, List<String>> wordIDCache = getFromWordIDCache(cacheDBFile);
        List<String> families = new ArrayList<>();
        families.add("csvIds");
        createTable(WORDS_TBL_NAME, families);
        if (wordIDCache != null) {
            try (Admin admin = getConn().getAdmin()) {
                try (Table tbl = getConn().getTable(WORDS_TBL_NAME)) {
                    for (String w : wordIDCache.keySet()) {
                        List<String> ids = wordIDCache.get(w);
                        Logger.getLogger(DBTools.class.getName()).log(Level.INFO, "Adding: {0}", w);
                        putInWordINDB(w, ids, tbl);
                    }
                }
                admin.flush(EDGES_TBL_NAME);
            }
        }

        Map<String, String> synsetCache = getFromSynsetCache(cacheDBFile);
        families = new ArrayList<>();
        families.add("jsonString");
        createTable(SYNSET_TBL_NAME, families);
        if (synsetCache != null) {
            try (Admin admin = getConn().getAdmin()) {
                try (Table tbl = getConn().getTable(SYNSET_TBL_NAME)) {
                    for (String id : synsetCache.keySet()) {
                        String val = synsetCache.get(id);
                        Logger.getLogger(DBTools.class.getName()).log(Level.INFO, "Adding: {0}", id);
                        addToSynsetDB(id, val, tbl);
                    }
                }
                admin.flush(EDGES_TBL_NAME);
            }
        }

        Map<String, String> disambiguateCache = getDisambiguateCache(cacheDBFile);
        families = new ArrayList<>();
        families.add("jsonString");
        createTable(DISAMBIGUATE_TBL_NAME, families);
        if (disambiguateCache != null) {
            try (Admin admin = getConn().getAdmin()) {
                try (Table tbl = getConn().getTable(DISAMBIGUATE_TBL_NAME)) {
                    for (String sentence : disambiguateCache.keySet()) {
                        String val = disambiguateCache.get(sentence);
                        Logger.getLogger(DBTools.class.getName()).log(Level.INFO, "Adding: {0}", sentence);
                        putInDisambiguateDB(sentence, val, tbl);
                    }
                }
                admin.flush(EDGES_TBL_NAME);
            }
        }

        Map<String, String> edgesCache = getEdgesCache(cacheDBFile);
        families = new ArrayList<>();
        families.add("jsonString");
        createTable(EDGES_TBL_NAME, families);

        if (edgesCache != null) {
            try (Admin admin = getConn().getAdmin()) {
                try (Table tbl = getConn().getTable(EDGES_TBL_NAME)) {
                    for (String id : edgesCache.keySet()) {
                        String val = edgesCache.get(id);
                        Logger.getLogger(DBTools.class.getName()).log(Level.INFO, "Adding: {0}", id);
                        addToEdgesDB(id, val, tbl);
                    }
                }
                admin.flush(EDGES_TBL_NAME);
            }
        }
    }

    private static void putInDisambiguateDB(String sentence, String jsonString, Table tbl) throws IOException {

        Put put = new Put(Bytes.toBytes(sentence));
        put.addColumn(Bytes.toBytes("jsonString"), Bytes.toBytes("jsonString"), Bytes.toBytes(jsonString));
        tbl.put(put);

    }

    private static void addToSynsetDB(String id, String json, Table tbl) throws IOException {
        Put put = new Put(Bytes.toBytes(id));
        put.addColumn(Bytes.toBytes("jsonString"), Bytes.toBytes("jsonString"), Bytes.toBytes(json));
        tbl.put(put);

    }

    private static Map<String, String> getFromSynsetCache(File cacheDBFile) throws InterruptedException, IOException {
        if (db == null || db.isClosed()) {
            db = DBMaker.newFileDB(cacheDBFile).make();
        }
        Map<String, String> synsetCache = db.getHashMap("synsetCacheDB");
        return synsetCache;
    }

    private static Map<String, List<String>> getFromWordIDCache(File cacheDBFile) {
        if (db == null || db.isClosed()) {
            db = DBMaker.newFileDB(cacheDBFile).make();
        }
        Map<String, List<String>> wordIDCache = db.get("wordIDCacheDB");
        return wordIDCache;
    }

    public static void putInWordINDB(String word, List<String> ids, Table tbl) throws IOException {

        StringBuilder strIds = new StringBuilder();
        for (String id : ids) {
            strIds.append(id).append(",");
        }
        strIds.deleteCharAt(strIds.length() - 1);
        strIds.setLength(strIds.length());

        Put put = new Put(Bytes.toBytes(word));
        Logger.getLogger(DBTools.class.getName()).log(Level.INFO, "Adding: " + word + " , " + strIds);
        put.addColumn(Bytes.toBytes("csvIds"), Bytes.toBytes("csvIds"), Bytes.toBytes(strIds.toString()));
        tbl.put(put);

    }

    public static void createTable(TableName tblName, List<String> families) throws IOException {
        try (Admin admin = getConn().getAdmin()) {
            if (!admin.tableExists(tblName)) {
                HTableDescriptor tableDescriptor = new HTableDescriptor(tblName);
                for (String f : families) {
                    HColumnDescriptor desc = new HColumnDescriptor(f);
                    tableDescriptor.addFamily(desc);
                }
                admin.createTable(tableDescriptor);
            }
        }
    }

    /**
     * @return the conn
     */
    public static synchronized Connection getConn() {
        if (conn == null) {
            Configuration config = HBaseConfiguration.create();
            try {
                conn = ConnectionFactory.createConnection(config);
            } catch (IOException ex) {
                Logger.getLogger(DBTools.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return conn;
    }

    private static Map<String, String> getDisambiguateCache(File cacheDBFile) {
        if (db == null || db.isClosed()) {
            db = DBMaker.newFileDB(cacheDBFile).make();
        }
        Map<String, String> disambiguateCache = db.get("disambiguateCacheDB");
        return disambiguateCache;
    }

    private static Map<String, String> getEdgesCache(File cacheDBFile) {
        if (db == null || db.isClosed()) {
            db = DBMaker.newFileDB(cacheDBFile).make();
        }
        return db.getHashMap("edgesCacheDB");
    }

    private static void addToEdgesDB(CharSequence id, String jsonString, Table tbl) throws IOException {
        Put put = new Put(Bytes.toBytes(id.toString()));
        put.addColumn(Bytes.toBytes("jsonString"), Bytes.toBytes("jsonString"), Bytes.toBytes(jsonString));
        tbl.put(put);

    }

    private static Map<String, Set<String>> getTermCache(File cacheDBFile) {
        if (db == null || db.isClosed()) {
            db = DBMaker.newFileDB(cacheDBFile).make();
        }
        return db.getHashMap("termCacheDB");
    }

    protected static void addPossibleTermsToDB(String ambiguousTerm, Set<String> terms) throws IOException, ParseException {
        List<String> families = new ArrayList<>();
        families.add("jsonString");
        families.add("ambiguousTerm");
        createTable(TERMS_TBL_NAME, families);
        try (Admin admin = getConn().getAdmin()) {
            try (Table tbl = getConn().getTable(TERMS_TBL_NAME)) {
                for (String oldSchemaJsonStr : terms) {
                    String newSchemaJsonStr = convertSchema(oldSchemaJsonStr);
                    Term t = TermFactory.create(newSchemaJsonStr);
                    Put put = new Put(Bytes.toBytes(t.getUid().toString()));
//                    String oldSchemaJsonStr = TermFactory.term2Json(t).toJSONString();
                    put.addColumn(Bytes.toBytes("jsonString"), Bytes.toBytes("jsonString"), Bytes.toBytes(newSchemaJsonStr));
                    put.addColumn(Bytes.toBytes("ambiguousTerm"), Bytes.toBytes("ambiguousTerm"), Bytes.toBytes(ambiguousTerm));
                    tbl.put(put);
                }
            }
            admin.flush(TERMS_TBL_NAME);
        }
    }

    private static String convertSchema(String oldSchemaJsonStr) {
        String newSchema = oldSchemaJsonStr.replaceAll("narrowerUIDS", "nuids");
        newSchema = newSchema.replaceAll("broaderUIDS", "buids");
        newSchema = newSchema.replaceAll("alternativeLables", "altLables");
        return newSchema;
    }

}
