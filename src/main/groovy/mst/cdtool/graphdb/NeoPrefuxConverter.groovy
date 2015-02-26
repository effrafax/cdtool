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

import groovy.json.JsonSlurper
import prefux.data.Graph
import prefux.data.Table

public class NeoPrefuxConverter {

	Graph graph
	NodeRetriever rt

	public void  initialize() {
		Table table = new Table();
		table.addColumns(NeoSchema.getNodeSchema())
		Table edges = new Table();
		edges.addColumns(NeoSchema.getEdgeSchema())

		graph = new Graph(table,edges,true,NeoSchema.ID,NeoSchema.SOURCE,NeoSchema.TARGET)
		
		rt = new NodeRetriever()
		rt.graph = graph
	}
	
	
	public void initializeGraph(String cypherQuery, Map params) {
		rt.rc.query(cypherQuery, params, )
	}
	
	
}
