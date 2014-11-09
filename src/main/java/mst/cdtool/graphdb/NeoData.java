package mst.cdtool.graphdb;

public enum NeoData {
	SELF("self", "rest.self"),
	EXTENSIONS("extensions","rest.extensions"),
	REL_CREATE("create_relationship","rest.create_relationship"),
	REL_OUT("outgoing_relationships", "rest.outgoing_relationships"), 
	REL_IN("incoming_relationships","rest.incoming_relationships"), 
	REL_ALL("all_relationships", "rest.all_relationships"), 
	REL_TYPED_OUT("outgoing_typed_relationships", "rest.outgoing_typed_relationships"), 
	REL_TYPED_IN("incoming_typed_relationships", "rest.incoming_typed_relationships"), 
	REL_TYPED_ALL("all_typed_relationships", "rest.all_typed_relationships"), 
	LABELS("labels", "rest.labels"), 
	PROPERTIES("properties", "rest.properties"), 
	PROPERTY("property", "rest.property"), 
	START("start", "rest.start"), 
	END("end", "rest.end"), 
	TYPE("type", "rest.type"),
	DATA("data","-"),
	TRAVERSE("traverse","rest.traverse"),
	PAGED_TRAVERSE("paged_traverse","rest.paged_traverse");

	
	
	
	private final String apiName;
	private final String localName;

	private NeoData(String apiName, String localName) {
		this.apiName = apiName;
		this.localName = localName;
	}

	public String getApi() {
		return apiName;
	}

	public String getLocal() {
		return localName;
	}

	public static NeoData apivalue(String apiName) {
		for (NeoData val : NeoData.values()) {
			if (val.getApi().equals(apiName)) {
				return val;
			}
		}
		throw new IllegalArgumentException("ApiName " + apiName + " does not exist");
	}

	public static NeoData localvalue(String localName) {
		for (NeoData val : NeoData.values()) {
			if (val.getLocal().equals(localName)) {
				return val;
			}
		}
		throw new IllegalArgumentException("LocalName " + localName + " does not exist");
	}

}
