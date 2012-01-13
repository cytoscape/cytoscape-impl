
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

import java.util.*;


/**
 * Basic implementation of the Number Index Interface.
 *
 * @author Ethan Cerami.
 */
class NumberIndexImpl extends GenericIndexImpl implements NumberIndex {
	private TreeMap treeMap;

	/**
	 * Creates a new NumberIndexImpl object.
	 *
	 * @param indexType  DOCUMENT ME!
	 */
	public NumberIndexImpl(int indexType) {
		super(indexType);
		treeMap = new TreeMap();
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void resetIndex() {
		treeMap = new TreeMap();
		super.resetIndex();
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param key DOCUMENT ME!
	 * @param o DOCUMENT ME!
	 */
	public void addToIndex(Object key, Object o) {
		if (key instanceof Integer || key instanceof Double) {
			List list = (List) treeMap.get(key);

			if (list == null) {
				list = new ArrayList();
				treeMap.put(key, list);
			}

			list.add(o);
		} else {
			throw new IllegalArgumentException("key parameter must be of "
			                                   + "type Integer or Double.");
		}

		super.addToIndex(key, o);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param lower DOCUMENT ME!
	 * @param upper DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public List getRange(Number lower, Number upper) {
		ArrayList list = new ArrayList();

		//  Calculate successor of upper via IEEE 754 method.
		//  Used to create a closed range between lower (inclusive) and
		//  upper (inclusive).
		//  For background, see:
		//  http://www.cygnus-software.com/papers/comparingfloats/
		//  comparingfloats.htm
		if (upper instanceof Double) {
			long bits = Double.doubleToLongBits(upper.doubleValue());
			upper = new Double(Double.longBitsToDouble(bits + 1));
		} else if (upper instanceof Integer) {
			upper = new Integer(upper.intValue() + 1);
		}

		SortedMap map = treeMap.subMap(lower, upper);
		Iterator iterator = map.values().iterator();

		while (iterator.hasNext()) {
			List subList = (List) iterator.next();
			list.addAll(subList);
		}

		return list;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Number getMinimumValue() {
		return (Number) treeMap.firstKey();
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Number getMaximumValue() {
		return (Number) treeMap.lastKey();
	}
}
