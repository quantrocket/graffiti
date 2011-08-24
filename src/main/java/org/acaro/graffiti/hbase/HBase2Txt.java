package org.acaro.graffiti.hbase;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.NavigableMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

public class HBase2Txt {
	private static final String USAGE = "usage: HBase2Txt <output filename>";
	private static final byte[] SPO = Bytes.toBytes("_SPO");;

	public static void main(String[] args) throws IOException {

		if (args.length != 1) {
			System.err.println(USAGE);
			System.exit(-1);
		}
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(args[0]));
		
		Configuration conf = HBaseConfiguration.create();
		HTable htable = new HTable(conf, "DBLP");
		htable.setScannerCaching(100);

		Scan scan = new Scan();
		scan.addFamily(Bytes.toBytes("_SPO"));
		
		ResultScanner rs = htable.getScanner(scan);
		
		Result res;
		while ((res = rs.next()) != null) {
			bw.write(Bytes.toString(res.getRow()));
			
			NavigableMap<byte[], byte[]> columns = res.getNoVersionMap().get(SPO);
			
			for (Entry<byte[], byte[]> column: columns.entrySet())
				bw.write("\t" + Bytes.toString(column.getKey()));
			
			bw.write("\n");
		}
		
		rs.close();
		bw.flush();
		bw.close();
	}
}