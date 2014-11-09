package mst.cdtool.graphdb;

import prefux.data.Schema;

public class NeoSchema {
	
	public static final String ID = "id";
	
	private static Schema NEO_SCHEMA;
	
	public static Schema getNeoSchema() {
		if (NEO_SCHEMA==null) {
			Schema schema = new Schema();
			for(NeoData val : NeoData.values()) {
				schema.addColumn(val.getLocal(), String.class);
			}
			schema.addColumn("id", long.class);
			schema.addColumn("source", long.class);
			schema.addColumn("target", long.class);
			NEO_SCHEMA = schema;
		}
		return NEO_SCHEMA;
	}

}
