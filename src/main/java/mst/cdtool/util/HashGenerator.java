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
