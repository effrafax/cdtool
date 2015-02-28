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

public enum NeoRestData {
	SELF("self", "rest.self"),
	EXTENSIONS("extensions","rest.extensions"),
	REL_CREATE("create_relationship","rest.create_relationship"),
	REL_OUT("outgoing_relationships", "rest.outgoing_relationships"), 
	REL_IN("incoming_relationships","rest.incoming_relationships"), 
	REL_ALL("all_relationships", "rest.all_relationships"), 
	REL_TYPED_OUT("outgoing_typed_relationships", "rest.outgoing_typed_relationships"), 
	REL_TYPED_IN("incoming_typed_relationships", "rest.incoming_typed_relationships"), 
	REL_TYPED_ALL("all_typed_relationships", "rest.all_typed_relationships"), 
	LABELS("labels", "rest.labels_url"), 
	PROPERTIES("properties", "rest.properties"), 
	PROPERTY("property", "rest.property"), 
	START("start", "rest.start"), 
	END("end", "rest.end"), 
	TYPE("type", "rest.type"),
	DATA("data","-"),
	TRAVERSE("traverse","rest.traverse"),
	PAGED_TRAVERSE("paged_traverse","rest.paged_traverse"),
	ERRORS("errors","rest.errors"),
	METADATA("metadata","rest.metadata"),
	META_ID("metadata.id","rest.id"),
	META_LABELS("metadata.labels","rest.labels");

	
	
	
	private final String apiName;
	private final String localName;

	private NeoRestData(String apiName, String localName) {
		this.apiName = apiName;
		this.localName = localName;
	}

	public String getApi() {
		return apiName;
	}

	public String getLocal() {
		return localName;
	}

	public static NeoRestData apivalue(String apiName) {
		for (NeoRestData val : NeoRestData.values()) {
			if (val.getApi().equals(apiName)) {
				return val;
			}
		}
		throw new IllegalArgumentException("ApiName " + apiName + " does not exist");
	}

	public static NeoRestData localvalue(String localName) {
		for (NeoRestData val : NeoRestData.values()) {
			if (val.getLocal().equals(localName)) {
				return val;
			}
		}
		throw new IllegalArgumentException("LocalName " + localName + " does not exist");
	}

}
