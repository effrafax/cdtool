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

import groovy.util.logging.Log4j2;
import mst.cdtool.util.HashGenerator;
import prefux.data.Edge
import prefux.data.Graph
import prefux.data.Node
import static mst.cdtool.graphdb.NeoRestData.*

@Log4j2
public class NodeRetriever {

	private NeoClient rc;
	private Graph graph;


	public List<Node> updateRelations(Node startNode,boolean dropRelations=false) throws CypherQueryException {
		def startNodeUrl = startNode.getString(SELF.local)
		// get current edges
		Map edges = [:]
		for (Edge edge in startNode.edges()) {
			def source = edge.getSourceNode().getString(SELF.local)
			if (!edges.containsKey(source)) {
				edges[source]=[]
			}
			edges[source]<<edge
		}
		def allRelationsRestUrl = startNode.get(REL_ALL.local)
		def result = rc.restCall(allRelationsRestUrl);
		def dropList = []
		result.each { relation ->
			def startUrl = relation[START.api]
			def endUrl = relation[END.api]
			Edge edge
			if (edges.containsKey(startUrl)) {
				edge = edges[startUrl].find { Edge edg ->
					edg.getString(TYPE.local)==relation[TYPE.api]
				}
				edges[startUrl].remove(edge)
			}
			if (!edge) {
				edge = createEdge(startNode, relation)
			}
		}
		if (dropRelations) {
			edges.each { String url, List values ->
				if (values.size()>0) {
					values.each { Edge edge ->
						log.debug "Removing edge ${edge}"
						removeEdge(edge)
					}
				}
			}
		}


	}

	/**
	 * Parses the REST node info. Properties of the "data" element are 
	 * put directly to the node. All other properties (API URLs) are set with prefix
	 * "rest."  
	 * 
	 * @param info The REST response data specific for the node.
	 * @param pNode The prefux node that is filled with the REST data.
	 * @return
	 */
	public void parseRestNodeInfo(Map info, Node pNode) {
		String selfUrl=""
		info.each { key, value ->
			if (key == DATA.api) {
				value.each { propName, propValue ->
					if (pNode.getColumnIndex(propName)==-1) {
						graph.getSet(Graph.NODES).addColumn(propName,String.class)
					}
					pNode.setString(propName, propValue)
				}
			} else {
				NeoRestData data = NeoRestData.apivalue(key)
				if (data==NeoRestData.SELF) {
					pNode.setLong(NeoSchema.ID, HashGenerator.SDBMHash(value.toString().trim()))
				}
				pNode.setString(data.local, value.toString())
			}
		}
	}
	
	/**
	 * Parses the REST result of a label query and returns a set of labels
	 * 
	 * @param result
	 * @param node
	 */
	void parseLabelInfo(def result, Node node) {
		log.debug "Label Result: ${result.class} // ${result}"
		Set labelSet = result as Set
		node.set(NeoSchema.LABELS, labelSet)
	}
	
	void removeEdge(Edge edge) {
		edge.graph.removeEdge(edge)
	}


	/**
	 * Creates a edge starting from the given node and using the
	 * edge information from the relation parameter.
	 * 
	 * @param startNode The start node
	 * @param relation The REST data for the relation
	 * @return The edge 
	 */
	def createEdge(Node startNode, def relation) {
		String startUrl = relation[START.api]
		String endUrl = relation[END.api]
		String type = relation[TYPE.api]
		def source, target
		def remoteUrl, remoteId
		if (startNode.getString(SELF.local) == startUrl) {
			source = startNode
			remoteUrl = endUrl
		} else if (startNode.getString(SELF.local) == endUrl) {
			target = startNode
			remoteUrl = startUrl
		} else {
			throw new NodeMismatchException("The given node "+startNode+" does not match the relation")
		}
		remoteId = HashGenerator.SDBMHash(remoteUrl)
		Graph graph = startNode.graph
		Node node = graph.getNodeFromKey(remoteId)
		if (node==null) {
			node = graph.addNode()
			updateNode(node,remoteUrl)
		}
		if (source==null) {
			source = node
		} else {
			target = node
		}
		Edge edge = graph.addEdge(source, target)
		edge.setString(NeoSchema.TYPE, type)
		if (relation[DATA.api]) {
			relation[DATA.api].each { propName, propValue ->
				if (edge.getColumnIndex(propName)==-1) {
					graph.getSet(Graph.EDGES).addColumn(propName,String.class)
				}
				edge.setString(propName, propValue)
			}
		}
		return edge
	}
	


	/**
	 * Updates the node by querying the REST api and storing the
	 * data to the node.
	 * 
	 * @param node
	 * @param selfUrl
	 * @return The Id of the node. 
	 */
	long updateNode(Node node, String selfUrl) {
		long orgId = node.getLong(NeoSchema.ID)
		def result = rc.restCall(selfUrl)
		parseRestNodeInfo(result,node)
		def labelUrl = node.getString(LABELS.local)
		result = rc.restCall(labelUrl)
		parseLabelInfo(result,node)
		return node.getLong(NeoSchema.ID)
	}
	
	/**
	 * This method should not be used directly. Better use the schema
	 * specific queries.
	 * The cypher query should return exactly one node result.
	 * 
	 * @param cypherQuery
	 * @param params
	 * @return The center node
	 */
	Node getInitNode(String cypherQuery, Map params) {
		graph.clear()
		Node nd = graph.addNode()
		def result = rc.query(cypherQuery, params)
		parseRestNodeInfo(result, nd)
		def labelUrl = nd.getString(LABELS.local)
		result = rc.restCall(labelUrl)
		parseLabelInfo(result,nd)
		updateRelations(nd)
		return nd
	}

}
