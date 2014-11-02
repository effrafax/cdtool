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
	private static final String ERROR_KEY = "errors"
	private static final String ERROR_CODE = "code"
	private static final String ERROR_MSG = "message"

	def url
	HTTPBuilder restClient
	JsonSlurper parser
	def serviceData


	def restRequest(def url, requestBody=null,Closure failure) {
		def result
		if (failure==null) {
			failure = { resp ->
				throw new CypherQueryException("GET Retrieval was not succesful "+url)
			}
		}
		def method = requestBody==null ? Method.GET :  Method.POST
		try {
			restClient.request(url, method, ContentType.JSON) {
				if (requestBody!=null) {
					body=requestBody.toString()
					requestContentType="${ContentType.JSON}; charset=UTF-8"
				}
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
		log.debug("Query result: "+result)
		log.debug("Errors: "+result[ERROR_KEY])
		if (result[ERROR_KEY] != null ) log.debug("Errors: "+result[ERROR_KEY][0])
		if (result[ERROR_KEY]!=null && result[ERROR_KEY].size()>0) {
			throw new CypherResultException(result[ERROR_KEY][0][ERROR_CODE]+": "+result[ERROR_KEY][0][ERROR_MSG])
		}
		return result
	}

	def initialize() throws CypherQueryException {
		parser = new JsonSlurper()
		restClient = new HTTPBuilder(url)
		restClient.parser.'application/json' = restClient.parser.'text/plain'
		def requestUrl = url
		serviceData = restRequest(requestUrl) { resp ->
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
				serviceData = restRequest(requestUrl) { resp ->
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
		result = restRequest(postUrl, builder) { resp ->
			log.error "Error : ${resp}"
			throw new CypherQueryException("Cypher query REST request failed: "+resp.code)
		}
		log.debug "Response: ${result}"
		return result
	}
}
