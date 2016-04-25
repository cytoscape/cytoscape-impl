package org.cytoscape.filter.internal.quickfind.util;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;


/**
 * A set of Misc. Utility Methods for Accesssing CyAttribute data.
 *
 * @author Ethan Cerami
 */
public class CyAttributesUtil {
	private CyAttributesUtil() {
	}

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
	public static String[] getAttributeValues(CyNetwork network, CyIdentifiable graphObject, String attributeKey) {
		String[] terms = null;

		if (attributeKey.equals(QuickFind.UNIQUE_IDENTIFIER)) {
			terms = new String[] {String.valueOf(graphObject.getSUID())};
		} else {
			CyRow row = network.getRow(graphObject);
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
					Object value = row.get(attributeKey, type);
					
					if (value != null) {
						terms = new String[] { value.toString() };
					}
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
	public static String[] getDistinctAttributeValues(CyNetwork network, Iterator<? extends CyIdentifiable> iterator,
	                                                  String attributeKey, int numDistinctValues) {
		Set<String> set = new HashSet<String>();
		int counter = 0;

		while (iterator.hasNext() && (counter < numDistinctValues)) {
			CyIdentifiable graphObject = iterator.next();
			String[] values = CyAttributesUtil.getAttributeValues(network,graphObject, attributeKey);

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
		CyTable table; 
		if (indexType.equals("node")) {
			table = cyNetwork.getDefaultNodeTable();
		} else if (indexType.equals("edge")) {
			table = cyNetwork.getDefaultEdgeTable();
		} else {
			return true;
		}
		if (table.getColumn(attributeName) == null) {
			return true;
		}
		
		for (CyRow row :table.getAllRows()) {
			Class<?> type = row.getTable().getColumn(attributeName).getType();
			if (row.get(attributeName, type) != null) {
				return false;
			}
		}
		return true;
	}
}
