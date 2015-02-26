/*
 * Copyright 2015 Martin Stockhammer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mst.cdtool.graphdb;

import java.util.Set;

import prefux.data.Schema;

public class NeoSchema {
	
	public static final String ID = "id";
	public static final String TYPE = "type";
	public static final String SOURCE = "source";
	public static final String TARGET = "target";
	public static final String LABELS = "labels_list";
	
	private static Schema NODE_SCHEMA;
	private static Schema EDGE_SCHEMA;
	
	public static Schema getNodeSchema() {
		if (NODE_SCHEMA==null) {
			Schema schema = new Schema();
			for(NeoRestData val : NeoRestData.values()) {
				schema.addColumn(val.getLocal(), String.class);
			}
			schema.addColumn(ID, long.class);
			schema.addColumn(SOURCE, long.class);
			schema.addColumn(TARGET, long.class);
			schema.addColumn(LABELS, Set.class);
			NODE_SCHEMA = schema;
		}
		return NODE_SCHEMA;
	}

	public static Schema getEdgeSchema() {
		if (EDGE_SCHEMA==null) {
			Schema schema = new Schema();
			schema.addColumn(ID, long.class);
			schema.addColumn(SOURCE, long.class);
			schema.addColumn(TARGET, long.class);
			schema.addColumn(TYPE, String.class);
			EDGE_SCHEMA = schema;
		}
		return EDGE_SCHEMA;
	}

}
