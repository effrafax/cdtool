package mst.cdtool.graphdb;

import mst.cdtool.util.HashGenerator;
import prefux.data.Edge
import prefux.data.Graph
import prefux.data.Node
import static mst.cdtool.graphdb.NeoData.*

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
						graph.addColumn(propName,String.class)
					}
					pNode.setString(propName, propValue)
				}
			} else {
				NeoData data = NeoData.apivalue(key)
				if (data==NeoData.SELF) {
					pNode.setLong(NeoSchema.ID, HashGenerator.SDBMHash(value.toString().trim()))
				}
				pNode.setString(data.local, value.toString())
			}
		}
	}


	def createEdge(Node startNode, def relation) {
		String startUrl = relation[START.api]
		String endUrl = relation[END.api]
		String type = relation[TYPE.api]
		long startId = HashGenerator.SDBMHash(startUrl)
		long endId = HashGenerator.SDBMHash(endUrl)
		Graph graph = startNode.graph
		def (source,target)=[[startUrl, startId], [endUrl, endId]].collect  { url, id ->
			Node node = graph.getNodeFromKey(id)
			if (node==null) {
				node = graph.addNode()
				updateNode(node,id,url)
			}
			node
		}
	}
	
	def updateNode(Node node, long id, String selfUrl) {
		def result = rc.restCall(selfUrl)
		parseRestNodeInfo(result,node)
	}

}
