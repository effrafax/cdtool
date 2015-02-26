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
