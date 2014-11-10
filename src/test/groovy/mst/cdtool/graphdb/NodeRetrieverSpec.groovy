package mst.cdtool.graphdb;

import static org.junit.Assert.*
import griffon.core.test.TestFor
import groovy.json.JsonSlurper
import prefux.data.Edge
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

	private static final String RELATION = """{
        "data": {
            "index":"1"
        },
        "end": "http://localhost:7474/db/data/node/7",
        "extensions": {
            
        },
        "properties": "http://localhost:7474/db/data/relationship/3/properties",
        "property": "http://localhost:7474/db/data/relationship/3/properties/{key}",
        "self": "http://localhost:7474/db/data/relationship/3",
        "start": "http://localhost:7474/db/data/node/5",
        "type": "RELEASED"
    }
"""
	
	private static final String TARGET_NODE = """{
    "all_relationships": "http://localhost:7474/db/data/node/7/relationships/all",
    "all_typed_relationships": "http://localhost:7474/db/data/node/7/relationships/all/{-list|&|types}",
    "create_relationship": "http://localhost:7474/db/data/node/7/relationships",
    "data": {
        "title": "Einer Will Immer Mehr"
    },
    "extensions": {
        
    },
    "incoming_relationships": "http://localhost:7474/db/data/node/7/relationships/in",
    "incoming_typed_relationships": "http://localhost:7474/db/data/node/7/relationships/in/{-list|&|types}",
    "labels": "http://localhost:7474/db/data/node/7/labels",
    "outgoing_relationships": "http://localhost:7474/db/data/node/7/relationships/out",
    "outgoing_typed_relationships": "http://localhost:7474/db/data/node/7/relationships/out/{-list|&|types}",
    "paged_traverse": "http://localhost:7474/db/data/node/7/paged/traverse/{returnType}{?pageSize,leaseTime}",
    "properties": "http://localhost:7474/db/data/node/7/properties",
    "property": "http://localhost:7474/db/data/node/7/properties/{key}",
    "self": "http://localhost:7474/db/data/node/7",
    "traverse": "http://localhost:7474/db/data/node/7/traverse/{returnType}"
}
"""

	Graph graph
	JsonSlurper parser

	def setup() {
		Table table = new Table();
		table.addColumns(NeoSchema.getNodeSchema())
		Table edges = new Table();
		edges.addColumns(NeoSchema.getEdgeSchema())
		
		graph = new Graph(table,edges,true,NeoSchema.ID,NeoSchema.SOURCE,NeoSchema.TARGET)
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
		nt.updateNode(node, "http://self")

		then:
		1 * restclient.restCall("http://self") >>  jsonData
		node.getString(NeoRestData.SELF.local)=="http://localhost:7474/db/data/node/5"
		node.getString("name")=="Alin Coen Band"
		node.getLong("id")!=0
		node.getString(NeoRestData.LABELS.local)=="http://localhost:7474/db/data/node/5/labels"
	}


	void "test create edge"() {
		given:
		NodeRetriever nt = new NodeRetriever()
		nt.graph = graph
		Node node = graph.addNode()
		node.setString(NeoRestData.SELF.local, "http://localhost:7474/db/data/node/5")
		NeoClient restclient = Mock()
		nt.rc=restclient
		def relationData = parser.parseText(RELATION)
		def targetData = parser.parseText(TARGET_NODE)
		
		when:
		def result = nt.createEdge(node, relationData)
		
		then:
		1 * restclient.restCall("http://localhost:7474/db/data/node/7") >>  targetData
		result!=null
		result instanceof Edge
		result.getString(NeoSchema.TYPE)=="RELEASED"
		result.getSourceNode()==node
		result.getTargetNode().getString("title")=="Einer Will Immer Mehr"
		result.getString("index")=="1"
		
	}
}
