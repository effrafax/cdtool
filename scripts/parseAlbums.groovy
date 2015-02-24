import groovy.io.FileType

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import mst.cdtool.graphdb.NeoClient;

import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.AudioHeader
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag
import org.jaudiotagger.tag.TagField

def fileExtensions = [".flac", ".ogg", ".mp3", ".wav"]


def cli = new CliBuilder(usage: 'parseAlbums baseDirectory')
// Create the list of options.
cli.with {
	h longOpt: 'help', 'Show usage information'
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

if (options.arguments().size() < 1) {
	cli.usage()
	return 1
}

def createArtist(NeoClient cl, String name) {
	println "Creating artist"
	def rt = cl.query("CREATE (a:Artist {name:{artist}}) RETURN a",["artist":name],["graph"])
	return rt["results"][0]["data"][0]["graph"]["nodes"][0]
}

def createAlbum(NeoClient cl, String album, def artists, String year, def properties) {
	def performerIds = []
	def composerIds = []
	artists["composer"].each { artist ->
		def rt = cl.query("MATCH (a:Artist) WHERE a.name={artist} RETURN a",["artist":artist],["graph"])["results"][0]["data"]
		def artistRt
		if (rt.size()==0) {
			artistRt = createArtist(cl, artist)
		} else {
			artistRt = rt[0]["graph"]["nodes"][0]
		}
		composerIds << (artistRt["id"] as int)
	}
	artists["performer"].each {artist ->
		def rt = cl.query("MATCH (a:Artist) WHERE a.name={artist} RETURN a",["artist":artist],["graph"])["results"][0]["data"]
		def artistRt
		if (rt.size()==0) {
			artistRt = createArtist(cl, artist)
		} else {
			artistRt = rt[0]["graph"]["nodes"][0]
		}
		performerIds << (artistRt["id"] as int)
	}
	def params = ["id":performerIds,"title":album,"year":year]
	def crtStmt = new StringBuilder()
	crtStmt << "(n:Album { title:{title}, year:{year}"
	properties.each { key, value ->
		crtStmt << ", ${key}:{${key}}"
		params[key]=value
	}
	crtStmt << "}) "
	def rt = cl.query("MATCH (a:Artist) WHERE id(a) IN {id} CREATE (a)-[:RELEASED]->${crtStmt} RETURN n",
			params,["graph"])["results"][0]["data"][0]["graph"]["nodes"][0]
	int albumId = rt["id"] as int
	cl.query("MATCH (a:Artist),(m:Album) WHERE id(a) IN {id} AND id(m)={albumId} CREATE (a)-[:COMPOSED]->(m)",
		["id":composerIds,"albumId":albumId])
	println "Album ${rt}"
	return rt

}

def createMedium(NeoClient cl, int number, int albumId, def properties) {
	println "Creating medium ${number}, ${albumId}, ${properties}"
	def params = ["index":number,"albumId":albumId]
	def crtStmt = new StringBuilder()
	crtStmt << "(m:Medium {index:{index}"
	properties.each { key, value ->
		crtStmt << ", ${key}:{${key}}"
		params[key]=value
	}
	crtStmt << "}) "
	println "Creation: ${params}"
	def result = cl.query("""MATCH (a:Album) WHERE id(a)={albumId}
		CREATE (a)-[:HAS_MEDIUM { index: {index} }]->${crtStmt}
		RETURN m
	""",params,["graph"])["results"][0]["data"][0]["graph"]["nodes"][0]
	println "Finished: ${result}"
	return result
}

def createTrack(NeoClient cl, String title, int trackNum, def trackLen, int mediumId, def properties) {
	println "Creating track ${title}, ${trackNum}, ${trackLen}, ${mediumId}, ${properties}"
	def precLen = new StringBuilder()
	precLen.withFormatter(Locale.US) { Formatter fmt ->
		fmt.format("%8.3f",trackLen["precise"])
	}
	def params = ["mediumId":mediumId,"title":title,"trackNum":trackNum,"lengthStr":trackLen["string"],"lengthSeconds":precLen.toString()]
	def crtStmt = new StringBuilder()
	crtStmt << "(r:Recording {title:{title}, track:{trackNum}, length:{lengthStr}, seconds:{lengthSeconds}"
	properties.each { key, value ->
		crtStmt << ", ${key}:{${key}}"
		params[key]=value
	}
	crtStmt << "}) "
	println "Creation: ${params}"
	def result = cl.query("""MATCH (m:Medium) WHERE id(m)={mediumId} 
		CREATE (m)-[:HAS_TRACK { index: {trackNum} }]->${crtStmt}
		RETURN r
	""",params,["graph"])["results"][0]["data"][0]["graph"]["nodes"][0]
	println "Finished: ${result}"
	return result
}

def getTrackLength(AudioHeader head) {
	def prec = -1.0
	try {
		prec = head.getPreciseLength()
	} catch (Throwable ex) {
	}
	if (prec <0) {
		try {
			prec = head.getPreciseTrackLength()
		} catch (Throwable ex) {
		}
	}
	if (prec > 0) {
		println "Precise length found ${prec}"
	} else {
		prec = head.getTrackLength()
	}
	double s = prec % 60
	int m = ((int) (prec / 60)) % 60
	def mStr = new StringBuilder()
	mStr.withFormatter(Locale.US) { Formatter formatter ->
		formatter.format("%02d:%05.2f",m,s)
	}
	return ["string":mStr.toString(),"precise":prec,"minutes":m,"seconds":s]

}

def parseArtists(String artistStr)  {
	def result = ["composer":[],"performer":[]]
	if (artistStr.contains(';')) {
		String[] el = artistStr.split(';')
		el[0].split(',').each {
			result["composer"]<<it.trim()
		}
		el[1].split(',').each {
			result["performer"]<<it.trim()
		}
	} else {
		artistStr.split(',').each {
			result["performer"]<<it.trim()
		}
	}
	return result
}

Path basePath = Paths.get(options.arguments()[0])
if (Files.exists(basePath) && Files.isDirectory(basePath)) {
	NeoClient cl = new NeoClient(baseUrl:"http://localhost:7474/")
	cl.initialize()
	basePath.eachFileRecurse(FileType.FILES) { Path file ->
		boolean match = false
		for (ext in fileExtensions) {
			if (file.fileName.toString().endsWith(ext)) {
				match=true
				break
			}
		}
		if (match) {
			AudioFile f = AudioFileIO.read(file.toFile());
			Tag tag = f.getTag();
			AudioHeader header = f.getAudioHeader();
			println "${file}"
			print "Len: ${header.getTrackLength()}"

			tag.getFields().each { TagField field -> print ", ${field.id}=${field.toString()}" }
			def album = tag.getFirst(FieldKey.ALBUM)
			def trackArtist = tag.getFirst(FieldKey.ARTIST)
			def albumArtist = tag.getFirst(FieldKey.ALBUM_ARTIST)
			if (!albumArtist) {
				albumArtist = trackArtist
			}

			def mbArtistId = tag.getFirst(FieldKey.MUSICBRAINZ_ARTISTID)
			def mbReleaseGroupId = tag.getFirst(FieldKey.MUSICBRAINZ_RELEASE_GROUP_ID)
			def mbTrackId = tag.getFirst(FieldKey.MUSICBRAINZ_TRACK_ID)
			def genre = tag.getFirst(FieldKey.GENRE)
			def year = tag.getFirst(FieldKey.YEAR)
			def country = tag.getFirst("RELEASECOUNTRY")
			def trackTitle = tag.getFirst(FieldKey.TITLE)
			def label = tag.getFirst(FieldKey.RECORD_LABEL)
			def tracks = tag.getFirst(FieldKey.TRACK_TOTAL)
			int trackNum = Integer.parseInt(tag.getFirst(FieldKey.TRACK))
			def len = getTrackLength(header)
			println tag.getFirst(FieldKey.DISC_NO)
			int discNum = tag.getFirst(FieldKey.DISC_NO) ? (tag.getFirst(FieldKey.DISC_NO) as int): 1
			if (discNum>3) {
				println "BAD DISC NO: ${discNum}"
				throw new IllegalArgumentException("Bad disc")
			}
			def discTitle = tag.getFirst("DISCSUBTITLE")
			if (!discTitle) {
				discTitle = "${album} ${discNum}"
			}
			def discType = tag.getFirst(FieldKey.MEDIA)


			def artists = parseArtists(albumArtist)
			def mainArtist = artists["performer"][0]
			print "\n"
			println "Searching for '${album}' of '${mainArtist}'"
			def myMatch = cl.query("MATCH (n:Album)<-[:RELEASED]-(a:Artist) WHERE n.title={album} AND a.name={artist} RETURN n",
					["album":album,"artist":mainArtist],["graph"])["results"][0]["data"]
			def albumRt
			if (myMatch.size()>0) {
				println "Found"
				println myMatch
				albumRt = myMatch[0]["graph"]["nodes"][0]
			} else {
				println "Not Found"
				println myMatch
				def properties = ["artist":albumArtist]
				if (country) {
					properties["releasecountry"]=country
				}
				if (label) {
					properties["recordlabel"]=label
				}
				if (tracks) {
					properties["tracks_total"]=tracks
				}
				if (mbArtistId) {
					properties["musicbrainz_artistid"]=mbArtistId
				}
				if (mbReleaseGroupId) {
					properties["musicbrainz_releasegroupid"]=mbReleaseGroupId
				}
				if (genre) {
					properties["genre"]=genre
				}
				properties["path"]=file.getParent().toString()
				albumRt = createAlbum(cl,album,artists,year,properties)
			}
			int albumId = Integer.parseInt(albumRt["id"])
			println "Searching for medium ${discNum}"
			myMatch = cl.query("MATCH (m:Medium { index:{discNum} })<-[:HAS_MEDIUM]-(a:Album) WHERE id(a)={albumId} RETURN m",
					["discNum":discNum,"albumId":albumId],["graph"])["results"][0]["data"]
			def mediumRt
			if (myMatch.size()>0) {
				println "Medium found"
				mediumRt=myMatch[0]["graph"]["nodes"][0]
			} else {
				def properties = [:]
				if (discTitle) {
					properties["title"]=discTitle
				}
				if (discType) {
					properties["type"]=discType
				}
				mediumRt = createMedium(cl, discNum, albumId, properties)
			}
			int mediumId = Integer.parseInt(mediumRt["id"])
			println "Medium ID: ${mediumId}"
			println "Searching for track '${trackTitle}'"
			myMatch = cl.query("MATCH (r:Recording)<-[:HAS_TRACK]-(m:Medium) WHERE id(m)={mediumId} AND r.title={trackTitle} RETURN r",
					["mediumId":mediumId,"trackTitle":trackTitle],["graph"])["results"][0]["data"]
			if (myMatch.size()>0) {
				println "Track found already"
			} else {
				println "Creating new track"
				def properties = ["album":album,"artist":trackArtist]
				if (mbTrackId) {
					properties["musicbrainz_trackid"]=mbTrackId
				}
				properties["path"]=file.toString()
				properties["file"]=file.fileName.toString()
				createTrack(cl, trackTitle, trackNum, len,mediumId, properties)
			}
		}

	}
} else {
	println "Path ${basePath} is not accessible"
	return 1
}