package org.cytoscape.tableimport.internal.reader;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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


import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_BOOLEAN;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_BOOLEAN_LIST;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_FLOATING;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_FLOATING_LIST;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_INTEGER;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_INTEGER_LIST;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_STRING;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_STRING_LIST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.tableimport.internal.util.AttributeDataType;

/**
 * Take a line of data, analyze it, and map to CyAttributes.
 */
public class AttributeLineParser {
	
	private AttributeMappingParameters mapping;
	private Map<String, Object> invalid = new HashMap<String, Object>();

	/**
	 * Creates a new AttributeLineParser object.
	 */
	public AttributeLineParser(AttributeMappingParameters mapping) {
		this.mapping = mapping;
	}

	/**
	 * Import everything regardless associated nodes/edges exist or not.
	 * @param parts entries in a line.
	 */
	public void parseAll(CyTable table, String[] parts) {
		// Get key
		final Object primaryKey ;
		final int partsLen = parts.length;
		final AttributeDataType typeKey = mapping.getAttributeTypes()[mapping.getKeyIndex()];
		
		switch (typeKey) {
			case TYPE_BOOLEAN:
				primaryKey = Boolean.valueOf(parts[mapping.getKeyIndex()].trim());
				break;
			case TYPE_INTEGER:
				primaryKey = Integer.valueOf(parts[mapping.getKeyIndex()].trim());
				break;
			case TYPE_FLOATING:
				primaryKey = Double.valueOf(parts[mapping.getKeyIndex()].trim());
				break;
			case TYPE_STRING:
				primaryKey = parts[mapping.getKeyIndex()].trim();
				break;
			default:
				primaryKey = parts[mapping.getKeyIndex()].trim();
		}

		if (partsLen == 1) {
			table.getRow(parts[0]);
		} else {
			for (int i = 0; i < partsLen; i++) {
				if ((i != mapping.getKeyIndex()) && mapping.getImportFlag()[i]) {
					if (parts[i] == null) {
						continue;
					} else {
						mapAttribute(table, primaryKey, parts[i].trim(), i);
					}
				}
			}
		}
	}

	/**
	 * Based on the attribute types, map the entry to CyAttributes.<br>
	 */
	private void mapAttribute(CyTable table, final Object key, final String entry, final int index) {
		final AttributeDataType type = mapping.getAttributeTypes()[index];

		switch (type) {
			case TYPE_BOOLEAN:
				try {
					setAttributeForType(table, TYPE_BOOLEAN, key, mapping.getAttributeNames()[index], entry);
				} catch (Exception e) {
					invalid.put(key.toString(), entry);
				}

				break;
			case TYPE_INTEGER:
				try {
					setAttributeForType(table, TYPE_INTEGER, key, mapping.getAttributeNames()[index], entry);
				} catch (Exception e) {
					invalid.put(key.toString(), entry);
				}

				break;
			case TYPE_FLOATING:
				try {
					setAttributeForType(table, TYPE_FLOATING, key, mapping.getAttributeNames()[index], entry);
				} catch (Exception e) {
					invalid.put(key.toString(), entry);
				}

				break;
			case TYPE_STRING:
				try {
					setAttributeForType(table, TYPE_STRING, key, mapping.getAttributeNames()[index], entry);
				} catch (Exception e) {
					invalid.put(key.toString(), entry);
				}

				break;
			case TYPE_BOOLEAN_LIST:
			case TYPE_INTEGER_LIST:
			case TYPE_FLOATING_LIST:
			case TYPE_STRING_LIST:
				/*
				 * In case of list, do not overwrite the attribute. Get the existing list, and add it to the list.
				 */
				final ArrayList<Object> curList = new ArrayList<>();
				curList.addAll(buildList(entry, type));
				
				try {
					setListAttribute(table, type, key, mapping.getAttributeNames()[index], curList);
				} catch (Exception e) {
					invalid.put(key.toString(), entry);
				}

				break;
			default:
				break;
		}
	}

	public static void setAttributeForType(final CyTable tbl, final AttributeDataType type, final Object key,
			final String attributeName, final String val) {
		if (tbl.getColumn(attributeName) == null) {
			if (type == TYPE_INTEGER)
				tbl.createColumn(attributeName, Integer.class, false);
			else if (type == TYPE_BOOLEAN)
				tbl.createColumn(attributeName, Boolean.class, false);
			else if (type == TYPE_FLOATING)
				tbl.createColumn(attributeName, Double.class, false);
			else // type is String
				tbl.createColumn(attributeName, String.class, false);
		}

		final CyRow row = tbl.getRow(key);

		if (type == TYPE_INTEGER)
			row.set(attributeName, new Integer(val));
		else if (type == TYPE_BOOLEAN)
			row.set(attributeName, new Boolean(val));
		else if (type == TYPE_FLOATING)
			row.set(attributeName, (new Double(val)));
		else // type is String
			row.set(attributeName, new String(val));
	}

	public static void setListAttribute(final CyTable tbl, final AttributeDataType type, final Object key,
			final String attributeName, final ArrayList<?> elmsBuff) {
		if (tbl.getColumn(attributeName) == null) {
			if (type == TYPE_INTEGER_LIST)
				tbl.createListColumn(attributeName, Integer.class, false);
			else if (type == TYPE_BOOLEAN_LIST)
				tbl.createListColumn(attributeName, Boolean.class, false);
			else if (type == TYPE_FLOATING_LIST)
				tbl.createListColumn(attributeName, Double.class, false);
			else if (type == TYPE_STRING_LIST)
				tbl.createListColumn(attributeName, String.class, false);
		}
		
		final CyRow row = tbl.getRow(key);
		row.set(attributeName, elmsBuff);
	}

	protected Map<String, Object> getInvalidMap() {
		return invalid;
	}

	/**
	 * If an entry is a list, split the string and create new List Attribute.
	 */
	private List<?> buildList(final String entry, final AttributeDataType dataType) {
		if (entry == null)
			return null;
		
		String delimiter = mapping.getListDelimiter();
		
		if (delimiter.isEmpty())
			delimiter = " ";
		
		final String[] parts = (entry.replace("\"", "")).split(delimiter);
		final List<Object> listAttr = new ArrayList<>();

		for (String listItem : parts) {
			switch (dataType) {
				case TYPE_BOOLEAN_LIST:
					listAttr.add(Boolean.parseBoolean(listItem.trim()));
					break;
				case TYPE_INTEGER_LIST:
					listAttr.add(Integer.parseInt(listItem.trim()));
					break;
				case TYPE_FLOATING_LIST:
					listAttr.add(Double.parseDouble(listItem.trim()));
					break;
				case TYPE_STRING_LIST:
					listAttr.add(listItem.trim());
					break;
				default:
					break;
			}
		}

		return listAttr;
	}
}
