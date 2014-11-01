

application {
	title = 'cdtool'
	startupGroups = ['cdtool']
	autoShutdown = true
}
mvcGroups {
	// MVC Group for "sample"
	'cdtool' {
		model      = 'mst.cdtool.CdtoolModel'
		view       = 'mst.cdtool.CdtoolView'
		controller = 'mst.cdtool.CdtoolController'
	}
}