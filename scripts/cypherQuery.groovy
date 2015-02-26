#!/usr/bin/groovy

import mst.cdtool.graphdb.NeoClient
import groovy.json.JsonOutput

NeoClient cl = new NeoClient(baseUrl:"http://localhost:7474/")

def cli = new CliBuilder(usage: 'cypherQuery QUERY [ PARAM1=VALUE1 PARAM2=VALUE2 ... ]')
// Create the list of options.
cli.with {
	h longOpt: 'help', 'Show usage information'
	t longOpt: 'type', args:1, argName: 'typeName', 'Give the result type'
	//	c longOpt: 'format-custom', args: 1, argName: 'format', 'Format date with custom format defined by "format"'
	//	f longOpt: 'format-full',   'Use DateFormat#FULL format'
	//	l longOpt: 'format-long',   'Use DateFormat#LONG format'
	//	m longOpt: 'format-medium', 'Use DateFormat#MEDIUM format (default)'
	//	s longOpt: 'format-short',  'Use DateFormat#SHORT format'
}

def options = cli.parse(args)
// Show usage text when -h or --help option is used.
if (options?.h) {
	cli.usage()
	return
}

def type =  ["graph"]
if (options?.t) {
	type = options.t.split(',')
}

def query
def params = [:]
def arguments = options.arguments()
if (arguments) {
	query = arguments[0]
	for (int i=1; i<arguments.size(); i++) {
		if (!arguments[i].contains("=")) {
			cli.usage()
			return
		}
		param = arguments[i].split("=")
		params[param[0]]=param[1]
	}
} else {
	cli.usage()
	return
}

if (!query) {
	println "No query defined"
	return 1
}

println "Query: ${query}"
println "Params: ${params}"


def result = cl.query(query, params, type)
println JsonOutput.prettyPrint(JsonOutput.toJson(result))


