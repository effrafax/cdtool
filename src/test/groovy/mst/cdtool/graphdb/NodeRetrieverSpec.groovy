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

import static org.junit.Assert.*
import griffon.core.test.TestFor
import groovy.json.JsonSlurper
import prefux.data.Edge
import prefux.data.Graph
import prefux.data.Node
import prefux.data.Table
import spock.lang.Specification

@TestFor(NodeRetriever)
class NodeRetrieverSpec extends Specification {

	private static String NODE_INFO
	private static String RELATION
	private static String TARGET_NODE
	private static String B_QUERY
	private static String B_REL
	private static String B_REL_ARTIST
	private static String B_REL_MEDIUM1
	private static String B_REL_MEDIUM2
	
	private static final String LABEL_ALBUM_DATA="""[
    "Album"
]
"""

	private static final String LABEL_ARTIST_DATA="""[
    "Artist"
]
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
		
		ClassLoader cl = Thread.currentThread().contextClassLoader
		NODE_INFO = cl.getResourceAsStream("rest/alin_coen_album/node.json").text
		RELATION = cl.getResourceAsStream("rest/alin_coen_album/relation.json").text
		TARGET_NODE = cl.getResourceAsStream("rest/alin_coen_album/target.json").text
		
		B_QUERY = cl.getResourceAsStream("rest/beatles_album/query_album.json").text
		B_REL = cl.getResourceAsStream("rest/beatles_album/all_relations_album.json").text
		B_REL_ARTIST = cl.getResourceAsStream("rest/beatles_album/artist_beatles.json").text
		B_REL_MEDIUM1 = cl.getResourceAsStream("rest/beatles_album/medium_1.json").text
		B_REL_MEDIUM2 = cl.getResourceAsStream("rest/beatles_album/medium_2.json").text
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
		def labelData = parser.parseText(LABEL_ARTIST_DATA)

		when:
		nt.updateNode(node, "http://self")

		then:
		1 * restclient.restCall("http://self") >>  jsonData
		node.getString(NeoRestData.SELF.local)=="http://localhost:7474/db/data/node/5"
		node.getString("name")=="Alin Coen Band"
		node.getLong("id")!=0
		node.getString(NeoRestData.LABELS.local)=="http://localhost:7474/db/data/node/5/labels"
		node.get(NeoSchema.LABELS)?.contains("Artist")
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
		def labelData = parser.parseText(LABEL_ALBUM_DATA)

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
		result.getTargetNode().get(NeoSchema.LABELS)?.contains("Album")
	}
	
	void "test query node"() {
		given:
		NodeRetriever nt = new NodeRetriever()
		nt.graph = graph
		Node node = graph.addNode()
		node.setString(NeoRestData.SELF.local, "http://localhost:7474/db/data/node/2473")
		NeoClient restclient = Mock()
		nt.rc=restclient
		
		when:
		def result = nt.getInitNode("QUERY",[:])
		
		then:
		1 * restclient.query("QUERY",[:]) >> parser.parseText(B_QUERY)
		result instanceof Node
		Node nd = result
		nd.get(NeoSchema.ID) == 2473
		nd.get("tracks_total")=="14"
		nd.get(NeoSchema.LABELS) == (["Album"] as Set)
		
	}

	void "test update relations"() {
		given:
		NodeRetriever nt = new NodeRetriever()
		nt.graph = graph
		Node node = graph.addNode()
		node.setString(NeoRestData.SELF.local, "http://localhost:7474/db/data/node/2473")
		NeoClient restclient = Mock()
		nt.rc=restclient
		
		when:
		node = nt.getInitNode("QUERY",[:])
		nt.updateRelations(node)
		
		then:
		1 * restclient.query("QUERY",[:]) >> parser.parseText(B_QUERY)
		1 * restclient.restCall("http://localhost:7474/db/data/node/2473/relationships/all") >> parser.parseText(B_REL)
		1 * restclient.restCall("http://localhost:7474/db/data/node/2472") >> parser.parseText(B_REL_ARTIST)
		1 * restclient.restCall("http://localhost:7474/db/data/node/2532") >> parser.parseText(B_REL_MEDIUM1)
		1 * restclient.restCall("http://localhost:7474/db/data/node/2474") >> parser.parseText(B_REL_MEDIUM2)
		node.edges().toList().size()==3
	}

	
}
