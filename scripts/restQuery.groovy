#!/usr/bin/groovy

import mst.cdtool.graphdb.NeoClient
import groovy.json.JsonOutput

NeoClient cl = new NeoClient(baseUrl:"http://localhost:7474/")

def cli = new CliBuilder(usage: 'cypherQuery URL')
// Create the list of options.
cli.with {
	h longOpt: 'help', 'Show usage information'
}

def options = cli.parse(args)
// Show usage text when -h or --help option is used.
if (options?.h) {
	cli.usage()
	return
}

def url
def arguments = options.arguments()
if (arguments) {
	url = arguments[0]
} else {
	cli.usage()
	return
}

println "URL: ${url}"

def result = cl.restCall(url)
println JsonOutput.prettyPrint(JsonOutput.toJson(result))


