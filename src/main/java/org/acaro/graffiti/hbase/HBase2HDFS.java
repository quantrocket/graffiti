/*  Copyright 2011 Claudio Martella
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
*/

package org.acaro.graffiti.hbase;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.NavigableMap;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

public class HBase2HDFS {

    private static final String USAGE = "./HBase2HDFS <hdfs filename>";
    private static final Logger LOG = Logger.getLogger(HBase2HDFS.class);
    private static final int SCANNERCACHING = 1000;
    private static final byte[] SPO = Bytes.toBytes("_SPO");
    private HTable htable;
    private JsonFactory f;
    private PrintWriter wout;

    public HBase2HDFS(String filename) 
    throws IOException {

        Configuration conf = HBaseConfiguration.create();
        htable = new HTable(conf, "DBLP");
        htable.setScannerCaching(SCANNERCACHING);

        Configuration hdfsConf = new Configuration();
        FileSystem fs = FileSystem.get(hdfsConf);
        Path outFile = new Path(filename);
        if (fs.exists(outFile)) {
            LOG.error("file " + filename + " already exists.");
            System.exit(-1);
        }

        f    = new JsonFactory();
        wout = new PrintWriter(fs.create(outFile));
    }

    private void convert() 
    throws IOException {

        Scan scan = new Scan();
        scan.addFamily(Bytes.toBytes("_SPO"));

        ResultScanner rs = htable.getScanner(scan);

        Result res;
        while ((res = rs.next()) != null) {
            write(res.getRow(), res.getNoVersionMap().get(SPO));
        }

        rs.close();
        wout.flush();
        wout.close();
    }

    private String cleanResource(String resource) {
        String cleaned = resource;

        // this is not an URI
        if (cleaned.startsWith("\"")) {
            int end = resource.indexOf('"', 1);
            cleaned = resource.substring(1, end);
        }

        return cleaned;
    }

    private void write(byte[] row, NavigableMap<byte[], byte[]> columns) 
    throws IOException {

        StringWriter w      = new StringWriter();
        JsonGenerator json  = f.createJsonGenerator(w);

        json.writeStartArray();
        json.writeString(Bytes.toString(row));
        json.writeStartArray();
        for (Entry<byte[], byte[]> column: columns.entrySet()) {
            json.writeStartArray();
            String splits[] = Bytes.toString(column.getKey()).split("\\|");
            json.writeString(splits[0]);
            json.writeString(cleanResource(splits[1]));
            json.writeEndArray();
        }
        json.writeEndArray();
        json.writeEndArray();
        json.close();

        wout.println(w.toString());
    }

    public static void main(String[] args) 
    throws IOException {

        if (args.length < 1) {
            System.out.println(USAGE);
            System.exit(-1);
        }

        HBase2HDFS converter = new HBase2HDFS(args[0]);
        converter.convert();
    }
}
