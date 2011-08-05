
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

package org.cytoscape.filter.internal.quickfind.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableEntry;


/**
 * A set of Misc. Utility Methods for Accesssing CyAttribute data.
 *
 * @author Ethan Cerami
 */
public class CyAttributesUtil {
	/**
	 * Regardless of attribute type, this method will always return attribute
	 * values as an array of String objects.  For example, given an attribute of
	 * type:  CyAttributes.TYPE_INTEGER with a single value = 25, this method
	 * will return an array of size 1 = ["25"].  The method will return null if
	 * no attribute value is found, or if the attribute is of type
	 * {@link CyAttributes#TYPE_COMPLEX}.
	 *
	 * @param attributes    CyAttributes Object.
	 * @param graphObjectId Graph Object ID.
	 * @param attributeKey  Attribute Key.
	 * @return array of String Objects or null.
	 */
	public static String[] getAttributeValues(CyTableEntry graphObject, String attributeKey) {
		String[] terms = null;

		if (attributeKey.equals(QuickFind.UNIQUE_IDENTIFIER)) {
			terms = new String[] {String.valueOf(graphObject.getSUID())};
		} else {
			CyRow row = graphObject.getCyRow();
			Class<?> type = row.getTable().getColumn(attributeKey).getType();
			boolean hasAttribute = type != null;

			if (hasAttribute) {
				//  Convert all types to String array.
				if (type == List.class) {
					List<?> list = row.get(attributeKey, List.class);

					//  Iterate through all elements in the list
					if ((list != null) && (list.size() > 0)) {
						terms = new String[list.size()];

						for (int i = 0; i < list.size(); i++) {
							Object o = list.get(i);
							terms[i] = o.toString();
						}
					}
				} else if (type == Map.class) {
					Map<?, ?> map = row.get(attributeKey, Map.class);

					//  Iterate through all values in the map
					if ((map != null) && (map.size() > 0)) {
						terms = new String[map.size()];

						int index = 0;
						for (Object o : map.values()) {
							terms[index++] = o.toString();
						}
					}
				} else {
					terms = new String[] {row.get(attributeKey, type).toString()};
				}
			} else {
				return null;
			}
		}

		if (terms == null) {
			return null;
		}
		
		//  Remove all new line chars
		for (int i = 0; i < terms.length; i++) {
			terms[i] = terms[i].replaceAll("\n", " ");
		}

		return terms;
	}

	/**
	 * Method returns the first X distinct attribute values.
	 *
	 * @param iterator          Iterator of nodes or edges.
	 * @param attributes        Node or Edge Attributes.
	 * @param attributeKey      Attribute Key.
	 * @param numDistinctValues Number of Distinct Values.
	 * @return Array of Distinct Value Strings.
	 */
	public static String[] getDistinctAttributeValues(Iterator<? extends CyTableEntry> iterator,
	                                                  String attributeKey, int numDistinctValues) {
		Set<String> set = new HashSet<String>();
		int counter = 0;

		while (iterator.hasNext() && (counter < numDistinctValues)) {
			CyTableEntry graphObject = iterator.next();
			String[] values = CyAttributesUtil.getAttributeValues(graphObject, attributeKey);

			if ((values != null) && (values.length > 0)) {
				String singleStr = join(values);

				if (!set.contains(singleStr)) {
					set.add(singleStr);
					counter++;
				}
			}
		}

		if (set.size() > 0) {
			return set.toArray(new String[0]);
		} else {
			return null;
		}
	}

	/**
	 * Joins a list of Strings with ,
	 *
	 * @param values Array of String Objects.
	 * @return One string with each value separate by a comma.
	 */
	private static String join(String[] values) {
		StringBuffer buf = new StringBuffer();

		for (int i = 0; i < values.length; i++) {
			buf.append(values[i]);

			if (i < (values.length - 1)) {
				buf.append(", ");
			}
		}

		return buf.toString();
	}

	public static boolean isNullAttribute(CyNetwork cyNetwork, String indexType, String attributeName) {
		Collection<? extends CyTableEntry> entries; 
		if (indexType.equals("node")) {
			entries = cyNetwork.getNodeList();
		} else if (indexType.equals("edge")) {
			entries = cyNetwork.getEdgeList();
		} else {
			return true;
		}
		if (entries.size() == 0) {
			return true;
		}
		for (CyTableEntry entry : entries) {
			CyRow row = entry.getCyRow();
			Class<?> type = row.getTable().getColumn(attributeName).getType();
			if (row.get(attributeName, type) != null) {
				return false;
			}
		}
		return true;
	}
}
