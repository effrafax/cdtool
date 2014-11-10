package mst.cdtool.graphdb;

import mst.cdtool.util.HashGenerator;
import prefux.data.Edge
import prefux.data.Graph
import prefux.data.Node
import static mst.cdtool.graphdb.NeoRestData.*

public class NodeRetriever {

	private NeoClient rc;
	private Graph graph;


	public List<Node> updateRelations(Node startNode) throws CypherQueryException {
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
		def allRestUrl = startNode.get(REL_ALL.local)
		def result = rc.restCall(allRestUrl);
		result.each { relation ->
			def startUrl = relation[START.api]
			def endUrl = relation[END.api]
			Edge edge
			if (edges.containsKey(startUrl)) {
				edge = edges[startUrl].find { Edge edg ->
					edg.getString(TYPE.local)==relation[TYPE.api]
				}
			}
			if (!edge) {
				edge = createEdge(startNode, relation)
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
		return node.getLong(NeoSchema.ID)
	}

}
