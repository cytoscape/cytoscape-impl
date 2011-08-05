
/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

package org.cytoscape.filter.internal.widgets.autocomplete.index;

import java.text.ParseException;
import java.text.RuleBasedCollator;

import java.util.*;


/**
 * Basic implementation of the Text Index Interface.
 *
 * @author Ethan Cerami.
 */
class TextIndexImpl extends GenericIndexImpl implements TextIndex {
	private Trie trie;
	private HashMap map;
	private static final boolean OUTPUT_PERFORMANCE_STATS = false;
	private HashMap cache = new HashMap();
	private static final String WILD_CARD = "*";
	private int maxKeyLength;

	/**
	 * Constructor.
	 * @param indexType QuickFind.INDEX_NODES or QuickFind.INDEX_EDGES.
	 */
	public TextIndexImpl(int indexType) {
		super(indexType);
		maxKeyLength = TextIndex.DEFAULT_MAX_KEY_LENGTH;
		init();
	}

	/**
	 * Resets the index, e.g. wipes everything clean.
	 */
	public void resetIndex() {
		init();
		super.resetIndex();
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param len DOCUMENT ME!
	 */
	public void setMaxKeyLength(int len) {
		maxKeyLength = len;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public int getMaxKeyLength() {
		return maxKeyLength;
	}

	/**
	 * Adds new object to index.
	 *
	 * @param key String Hit.
	 * @param o   Any Java Object.
	 */
	public void addToIndex(Object key, Object o) {
		// convert all keys to lowercase
		String keyString = (String) key;
		keyString = keyString.toLowerCase();

		// truncate key, if necessary
		if (keyString.length() > maxKeyLength) {
			keyString = keyString.substring(0, maxKeyLength);
		}

		//  Add to Trie and HashMap
		trie.add(keyString);

		ArrayList objectList = (ArrayList) map.get(keyString);

		if (objectList == null) {
			objectList = new ArrayList();
			map.put(keyString, objectList);
		}

		objectList.add(o);
		super.addToIndex(key, o);
	}

	/**
	 * Gets all hits which begin with the specified prefix.
	 *
	 * @param prefix  String prefix.
	 * @param maxHits Maximum number of hits
	 * @return Array of Strings, which begin with the specified prefix.
	 */
	public Hit[] getHits(String prefix, int maxHits) {
		Date start = new Date();
		Hit[] hits = null;

		//  Deal with wild card cases.
		if (prefix.equals("")) {
			hits = (Hit[]) cache.get(prefix);
		} else if (prefix.endsWith(WILD_CARD)) {
			hits = getWildCardHits(prefix);
		}

		if (hits == null) {
			String[] keys = trie.getWords(prefix.toLowerCase());
			int size = Math.min(keys.length, maxHits);
			hits = new Hit[size];

			//  By default, strings are ordered lexicographically -- meaning
			//  that the unicode value for each character is used for
			//  comparison.  Therefore, in the world of  QuickFind, strings
			//  beginning with non-alphanumeric and digits appear before those
			//  strings that begin with letters.  This is not really want we
			//  want.  Rather, we would like those strings beginning with
			//  letters to appear first. The collator rules below do the trick.
			//  This rule essentially says that a-z should appear before all
			//  other characters.
			String collatorRules = "<a<b<c<d<e<f<g<h<i<j<k<l<m<n<o<p<q<r<s<t" + "<u<v<w<x<y<z";

			try {
				RuleBasedCollator collator = new RuleBasedCollator(collatorRules);
				Arrays.sort(keys, collator);
			} catch (ParseException e) {
			}

			//  Create the Hits
			for (int i = 0; i < size; i++) {
				hits[i] = new Hit(keys[i], getObjectsByKey(keys[i]));
			}
		}

		if (prefix.equals("")) {
			cache.put(prefix, hits);
		}

		Date stop = new Date();

		if (OUTPUT_PERFORMANCE_STATS) {
			long interval = stop.getTime() - start.getTime();
			System.out.println("Time to look up:  " + interval + " ms");
		}

		return hits;
	}

	/**
	 * Gets total number of keys in index.
	 *
	 * @return number of keys in index.
	 */
	public int getNumKeys() {
		return map.size();
	}

	/**
	 * Gets a text description of the text index, primarily used for
	 * debugging purposes.
	 *
	 * @return text description of the text index.
	 */
	public String toString() {
		return "Text Index:  [Total number of keys:  " + map.size() + "]";
	}

	/**
	 * Executes basic wild card search.  Prefix must end with *.
	 * For example:  "YDR*".
	 *
	 * @param prefix prefix ending in *.
	 * @return An array containing 1 hit object or null.
	 */
	private Hit[] getWildCardHits(String prefix) {
		Hit[] hits = null;

		//  Remove wildcard.
		String regex = prefix.toLowerCase().substring(0, prefix.length() - 1);

		//  Find all matching words
		String[] keys = trie.getWords(regex);

		//  Find all associated graph objects;  avoid redundant objects.
		Set graphObjectSet = new HashSet();

		for (int i = 0; i < keys.length; i++) {
			Object[] graphObjects = getObjectsByKey(keys[i]);

			for (int j = 0; j < graphObjects.length; j++) {
				graphObjectSet.add(graphObjects[j]);
			}
		}

		//  Return result set
		if (graphObjectSet.size() > 0) {
			hits = new Hit[1];

			Object[] graphObjects = graphObjectSet.toArray(new Object[graphObjectSet.size()]);
			hits[0] = new Hit(prefix, graphObjects);
		}

		return hits;
	}

	/**
	 * Gets all objects associated with the specified key.
	 * <p/>
	 * Each key can be associated with multiple objects.  This method therefore
	 * returns an array of Objects.
	 *
	 * @param key String Hit.
	 * @return Array of Java Objects.
	 */
	private Object[] getObjectsByKey(String key) {
		if (map.containsKey(key)) {
			ArrayList list = (ArrayList) map.get(key);

			return list.toArray();
		} else {
			throw new IllegalArgumentException("No objects exist for key:  " + key);
		}
	}

	/**
	 * Initializes the Text Index.
	 */
	private void init() {
		trie = new Trie();
		map = new HashMap();
		cache = new HashMap();
	}
}
