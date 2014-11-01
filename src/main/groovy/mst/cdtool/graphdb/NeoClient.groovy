package mst.cdtool.graphdb

import groovy.json.JsonSlurper
import groovy.util.logging.Log4j2
import groovyx.net.http.ContentType;
import groovyx.net.http.HTTPBuilder;
import groovyx.net.http.RESTClient
import groovyx.net.http.Method

@Log4j2
class NeoClient {

	private static final String TRANSACTION_KEY = "transaction"
	private static final String VERSION_KEY = "neo4j_version"
	private static final String DATA_KEY = "data"

	def url
	HTTPBuilder restClient
	JsonSlurper parser
	def serviceData


	def getRequest(def url, Closure failure) {
		def result
		if (failure==null) {
			failure = { resp ->
				throw new CypherQueryException("GET Retrieval was not succesful "+url)
			}
		}
		try {
			restClient.request(url, Method.GET, ContentType.JSON) {
				response.success = { resp, InputStreamReader reader ->
					def writer = new StringWriter()
					writer << reader
					result=parser.parseText(writer.toString())
				}

				response.failure = failure
			}
		} catch (Exception ex) {
			throw new CypherQueryException("Error while initializing GET request: "+ex.message, ex)
		}
		return result
	}

	def initialize() throws CypherQueryException {
		parser = new JsonSlurper()
		restClient = new HTTPBuilder(url)
		restClient.parser.'application/json' = restClient.parser.'text/plain'
		def requestUrl = url
		serviceData = getRequest(requestUrl) { resp ->
			throw new CypherQueryException("Error during initalization "+requestUrl)
		}
		log.debug "ServiceData: ${serviceData}"
		if (serviceData==null) {
			throw new CypherQueryException("No initial data found at "+requestUrl)
		}
		if (serviceData[VERSION_KEY]==null) {
			if (serviceData[DATA_KEY]==null) {
				throw new CypherQueryException("No base and service response at "+requestUrl)
			} else {
				requestUrl = serviceData[DATA_KEY]
				serviceData = getRequest(requestUrl) { resp ->
					throw new CypherQueryException("Error during service url initialization "+requestUrl)
				}
			}
		}
		if (serviceData[VERSION_KEY]==null) {
			log.error "Initialization Error: ${serviceData}"
			throw new CypherQueryException("No service data found at "+requestUrl)
		}
	}

	def query(String cypherQuery, Map cypherParams) throws CypherQueryException {
		def builder = new groovy.json.JsonBuilder()
		def root = builder {
			"statements"([
				[
					"statement":cypherQuery,
					"parameters":cypherParams
				]
			])
		}
		log.debug "Cypher query: ${builder.toString()}"
		def result
		JsonSlurper parser = new JsonSlurper()
		boolean fail=false
		// Due to bug in groovy the HTTPBuilder parser must be set to text/plain
		// https://jira.codehaus.org/browse/GROOVY-7132
		def postUrl = serviceData[TRANSACTION_KEY]+"/commit"
		try {
			restClient.request(postUrl, Method.POST, ContentType.JSON) { req ->
				body=builder.toString()
				requestContentType="${ContentType.JSON}; charset=UTF-8"
				response.success = { resp, InputStreamReader reader ->
					log.debug "Response status: ${resp.status}"
					log.debug "Reader: ${reader.class}"
					log.debug "Encoding: ${reader.encoding}"
					def writer = new StringWriter()
					writer << reader
					result=writer.toString()
				}

				response.failure = { resp ->
					log.error "Error : ${resp}"
					result=resp.data
					fail=true
				}
			}
		} catch (Exception ex) {
			log.error "Exception during cypher call: ${ex.message}"
			throw new CypherQueryException("Cypher request failed: ${cypherQuery}", ex)
		}
		if (fail) {
			throw new CypherQueryException("Cypher request failed: ${result}")
		}
		result = parser.parseText(result)
		log.debug "Response: ${result}"
		return result
	}
}
