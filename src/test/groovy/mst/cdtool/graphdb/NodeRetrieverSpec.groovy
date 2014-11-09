package mst.cdtool.graphdb;

import static org.junit.Assert.*
import griffon.core.test.TestFor
import groovy.json.JsonSlurper
import prefux.data.Graph
import prefux.data.Table
import spock.lang.Specification
import prefux.data.Node

@TestFor(NodeRetriever)
class NodeRetrieverSpec extends Specification {
	
	private static final String NODE_INFO = """
{
    "all_relationships": "http://localhost:7474/db/data/node/5/relationships/all",
    "all_typed_relationships": "http://localhost:7474/db/data/node/5/relationships/all/{-list|&|types}",
    "create_relationship": "http://localhost:7474/db/data/node/5/relationships",
    "data": {
        "name": "Alin Coen Band",
        "type": "group"
    },
    "extensions": {
        
    },
    "incoming_relationships": "http://localhost:7474/db/data/node/5/relationships/in",
    "incoming_typed_relationships": "http://localhost:7474/db/data/node/5/relationships/in/{-list|&|types}",
    "labels": "http://localhost:7474/db/data/node/5/labels",
    "outgoing_relationships": "http://localhost:7474/db/data/node/5/relationships/out",
    "outgoing_typed_relationships": "http://localhost:7474/db/data/node/5/relationships/out/{-list|&|types}",
    "paged_traverse": "http://localhost:7474/db/data/node/5/paged/traverse/{returnType}{?pageSize,leaseTime}",
    "properties": "http://localhost:7474/db/data/node/5/properties",
    "property": "http://localhost:7474/db/data/node/5/properties/{key}",
    "self": "http://localhost:7474/db/data/node/5",
    "traverse": "http://localhost:7474/db/data/node/5/traverse/{returnType}"
}
"""
	
	Graph graph
	JsonSlurper parser

	def setup() {
		Table table = new Table();
		table.addColumns(NeoSchema.getNeoSchema())
		graph = new Graph(table,true)
		parser = new JsonSlurper()
	}

	void "test node parsing"() {
		given:
			NodeRetriever nt = new NodeRetriever()
			nt.graph = graph
			Node node = nt.graph.addNode()
			def jsonData = parser.parseText(NODE_INFO)
		when:
			nt.parseRestNodeInfo(jsonData, node)
			
		then:
			node.getString("rest.self")==jsonData.self
			node.getLong(NeoSchema.ID)!=0
	}
	
	void "test node update"() {
		given:
			NodeRetriever nt = new NodeRetriever()
			nt.graph = graph
			Node node = graph.addNode()
			NeoClient restclient = Mock()
			nt.rc=restclient
			def jsonData = parser.parseText(NODE_INFO)
		when:
			nt.updateNode(node, 0, "http://self")
		then:
			1 * restclient.restCall("http://self") >>  jsonData
			node.getString(NeoData.SELF.local)=="http://localhost:7474/db/data/node/5"
			node.getString("name")=="Alin Coen Band"
			node.getLong("id")!=0
			node.getString(NeoData.LABELS.local)=="http://localhost:7474/db/data/node/5/labels"
	}

}
