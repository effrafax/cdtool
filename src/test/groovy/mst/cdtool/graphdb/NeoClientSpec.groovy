package mst.cdtool.graphdb

import griffon.core.test.TestFor;
import spock.lang.Specification



@TestFor(NeoClient)
class NeoClientSpec extends Specification {

	void 'testQuery with row result'() {
		setup:
		def client = new NeoClient()
		client.baseUrl="http://localhost:7474/"
		client.initialize()

		when:
		def result = client.query("MATCH (n:Artist)-->(a:Album) WHERE n.name={name} RETURN n, a",[name:"Alin Coen Band"],["ROW"])

		then:
		result.size()==2
		result["errors"]!=null
		result["results"] instanceof List
		result["results"][0]["data"].size()>0
		result["results"][0]["data"][0]["row"]!=null

		when:
		result = client.query("MATCH (n:Artist)->(a:Album) WHERE n.name={name} RETURN n, a",[name:"Alin Coen Band"],["ROW"])

		then:
		CypherResultException e = thrown(CypherResultException)
		e.message.startsWith("Neo.ClientError.Statement.InvalidSyntax")
	}

	void 'testQuery with rest result'() {
		setup:
		def client = new NeoClient()
		client.baseUrl="http://localhost:7474/"
		client.initialize()

		when:
		def result = client.query("MATCH (n:Artist)-->(a:Album) WHERE n.name={name} RETURN n, a",[name:"Alin Coen Band"],["REST"])

		then:
		result.size()==2
		result["errors"]!=null
		result["results"] instanceof List
		result["results"][0]["data"].size()>0
		result["results"][0]["data"][0]["rest"]!=null
	}

	void 'testInitalize'() {
		setup:
		def client = new NeoClient()
		client.baseUrl="http://localhost:7474/"

		when:
		client.initialize()

		then:
		notThrown(NullPointerException)

		when:
		client.baseUrl="xxx"
		client.initializeCypher()
		then:
		thrown(CypherQueryException)

		when:
		client.baseUrl="http://localhost:7474/xxx"
		client.initializeCypher()
		then:
		thrown(CypherQueryException)
	}
}
