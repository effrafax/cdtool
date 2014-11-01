package mst.cdtool.graphdb

import griffon.core.test.TestFor;
import spock.lang.Specification



@TestFor(NeoClient)
class NeoClientSpec extends Specification {

	void 'testQuery'() {
		setup:
		def client = new NeoClient()
		client.url="http://localhost:7474/"
		client.initialize()

		when:
		def result = client.query("MATCH (n:Artist) WHERE n.name={name} RETURN n",[name:"Alin Coen Band"])

		then:
		result.size()==2
		result["errors"]!=null
		result["results"] instanceof List
	}


	void 'testInitalize'() {
		setup:
		def client = new NeoClient()
		client.url="http://localhost:7474/"

		when:
		client.initialize()
		
		then:
			 notThrown(NullPointerException)
			 
		when:
			client.url="xxx"
			client.initialize()
		then:
			thrown(CypherQueryException)

		when:
			client.url="http://localhost:7474/xxx"
			client.initialize()
		then:
			thrown(CypherQueryException)
	}
}
