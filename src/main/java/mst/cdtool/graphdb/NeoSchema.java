package mst.cdtool.graphdb;

import prefux.data.Schema;

public class NeoSchema {
	
	public static final String ID = "id";
	public static final String TYPE = "type";
	public static final String SOURCE = "source";
	public static final String TARGET = "target";
	
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
