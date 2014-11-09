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
	private static final String PATH_COMMIT = "/commit"

	def baseUrl=""
	HTTPBuilder restClient
	JsonSlurper parser
	def serviceData
	boolean initialized=false
	boolean cypherInitialized=false


	def restRequest(def url, transactional=false,requestBody=null,Closure failure) {
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
		// Check only for errors in result if using transactional API
		if (transactional && result instanceof Map) {
			log.debug("Query result: "+result)
			log.debug("Errors: "+result.containsKey(ERROR_KEY)+result[ERROR_KEY])
			if (result[ERROR_KEY] != null ) log.debug("Errors: "+result[ERROR_KEY][0])
			if (result[ERROR_KEY]!=null && result[ERROR_KEY].size()>0) {
				log.error "Error during rest calls ${result}"
				throw new CypherResultException(result[ERROR_KEY][0][ERROR_CODE]+": "+result[ERROR_KEY][0][ERROR_MSG])
			}
		}
		return result
	}

	private void initialize() throws CypherQueryException {
		parser = new JsonSlurper()
		restClient = new HTTPBuilder(baseUrl)
		restClient.parser.'application/json' = restClient.parser.'text/plain'
		initialized=true
	}

	private void initializeCypher() throws CypherQueryException {
		if (!initialized) {
			initialize()
		}
		def requestUrl = baseUrl
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
		cypherInitialized=true
	}

	def query(String cypherQuery, Map cypherParams, resultFormat=["REST"]) throws CypherQueryException {
		if (!cypherInitialized) {
			initializeCypher()
		}
		def builder = new groovy.json.JsonBuilder()
		def root = builder {
			"statements"([
				[
					"statement":cypherQuery,
					"parameters":cypherParams,
					"resultDataContents" : resultFormat
				]
			])
		}
		log.debug "Cypher query: ${builder.toString()}"
		def result
		JsonSlurper parser = new JsonSlurper()
		boolean fail=false
		// Due to bug in groovy the HTTPBuilder parser must be set to text/plain
		// https://jira.codehaus.org/browse/GROOVY-7132
		def postUrl = serviceData[TRANSACTION_KEY]+PATH_COMMIT
		result = restRequest(postUrl, true, builder) { resp ->
			log.error "Error : ${resp}"
			throw new CypherQueryException("Cypher query REST request failed: "+resp.code)
		}
		log.debug "Response: ${result}"
		return result
	}

	def restCall(restUrl) throws CypherQueryException {
		if (!initialized) {
			initialize()
		}
		def result = restRequest(restUrl) { resp ->
			log.error "Error : ${resp}"
			throw new CypherQueryException("Neo REST request failed: "+resp.code)
		}
		log.debug "Response: ${result}"
		return result

	}
}
