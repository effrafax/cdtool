package mst.cdtool.util;

public class HashGenerator {

	public HashGenerator() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Hash method taken from 
	 * http://www.partow.net/programming/hashfunctions/index.html#top
 	 *
 	 * The hash function seems to have a good over-all distribution for many different data sets
 	 *
	 * @param str
	 * @return
	 */
	public static long SDBMHash(String str) {
		long hash = 0;

		for (int i = 0; i < str.length(); i++) {
			hash = str.charAt(i) + (hash << 6) + (hash << 16) - hash;
		}

		return hash;
	}
}
