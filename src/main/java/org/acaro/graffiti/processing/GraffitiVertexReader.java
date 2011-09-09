package org.acaro.graffiti.processing;

import java.io.IOException;

import org.apache.giraph.graph.Edge;
import org.apache.giraph.graph.MutableVertex;
import org.apache.giraph.lib.TextVertexInputFormat.TextVertexReader;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordReader;
import org.json.JSONArray;
import org.json.JSONException;

public class GraffitiVertexReader extends
	TextVertexReader<Text, IntWritable, Text> {

	public GraffitiVertexReader(RecordReader<LongWritable, Text> arg) {
		super(arg);
	}

	@Override
	public boolean next(MutableVertex<Text, IntWritable, Text, ?> vertex)
			throws IOException, InterruptedException {
        
		if (!getRecordReader().nextKeyValue())
            return false;
		
		Text line = getRecordReader().getCurrentValue();
		
		try {
            JSONArray jsonVertex = new JSONArray(line.toString());
            vertex.setVertexId(new Text(jsonVertex.getString(0)));
            vertex.setVertexValue(new IntWritable(0));
            
            JSONArray jsonEdgeArray = jsonVertex.getJSONArray(2);
            for (int i = 0; i < jsonEdgeArray.length(); ++i) {
                JSONArray jsonEdge = jsonEdgeArray.getJSONArray(i);
                Edge<Text, Text> edge =
                    new Edge<Text, Text>(
                        new Text(jsonEdge.getString(0)),
                        new Text(jsonEdge.getString(1)));
                vertex.addEdge(edge);
            }
        } catch (JSONException e) {
            throw new IllegalArgumentException(
                "next: Couldn't get vertex from line " + line, e);
        }
		
		return true;
	}
}