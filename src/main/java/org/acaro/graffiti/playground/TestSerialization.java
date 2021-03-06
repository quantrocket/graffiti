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

package org.acaro.graffiti.playground;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.acaro.graffiti.query.Query;
import org.acaro.graffiti.query.QueryParser;

public class TestSerialization {
	
	public static void main(String[] args) throws Exception {
		Query q = new QueryParser("bla :: age [MAX(10)][MIN(3)] > loves (*3) > knows [ country = PREFIX('DE')]. DISTANCE('blabla')").parse();
		
		System.out.println(q);
		
		DataOutputStream dos = new DataOutputStream(new FileOutputStream("test-serialization.dat"));
		q.write(dos);
		dos.flush();
		dos.close();
		
		DataInputStream dis = new DataInputStream(new FileInputStream("test-serialization.dat"));
		q = new Query();
		q.readFields(dis);
		
		System.out.println(q);
	}
}
