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


import org.cytoscape.tableimport.internal.reader.TextTableReader.ObjectType;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.cytoscape.tableimport.internal.util.AttributeTypes;


/**
 * Take a line of data, analyze it, and map to CyAttributes.
 *
 * @since Cytoscape 2.4
 * @version 0.8
 * @author Keiichiro Ono
 *
 */
public class AttributeLineParser {
	private AttributeMappingParameters mapping;
	private Map<String, Object> invalid = new HashMap<String, Object>();

	/**
	 * Creates a new AttributeLineParser object.
	 *
	 * @param mapping  DOCUMENT ME!
	 */
	public AttributeLineParser(AttributeMappingParameters mapping) {
		this.mapping = mapping;
	}

	/**
	 *  Import everything regardless associated nodes/edges exist or not.
	 *
	 * @param parts entries in a line.
	 */
	public void parseAll(CyTable table, String[] parts) {

		//System.out.println("Entering AttributeLineParser.parseAll()....");

		// Get key
		final Object primaryKey ;
		final int partsLen = parts.length;
		final Byte typeKey = mapping.getAttributeTypes()[mapping.getKeyIndex()];
		
		switch (typeKey) {
			case AttributeTypes.TYPE_BOOLEAN:
				primaryKey = Boolean.valueOf(parts[mapping.getKeyIndex()].trim());
				break;
			case AttributeTypes.TYPE_INTEGER:
				primaryKey = Integer.valueOf(parts[mapping.getKeyIndex()].trim());
				break;
			case AttributeTypes.TYPE_FLOATING:
				primaryKey = Double.valueOf(parts[mapping.getKeyIndex()].trim());
				break;
			case AttributeTypes.TYPE_STRING:
				primaryKey = parts[mapping.getKeyIndex()].trim();
				break;
			default:
				primaryKey = parts[mapping.getKeyIndex()].trim();
		}

		if (partsLen==1)
			table.getRow(parts[0]);
		else{
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
	 *
	 * @param key
	 * @param entry
	 * @param index
	 */
	private void mapAttribute(CyTable table, final Object key, final String entry, final int index) {

		final Byte type = mapping.getAttributeTypes()[index];

		switch (type) {
		case AttributeTypes.TYPE_BOOLEAN:

				Boolean newBool;

				try {
					//newBool = new Boolean(entry);

					setAttributeForType(table,AttributeTypes.TYPE_BOOLEAN,key, mapping.getAttributeNames()[index], entry);
					//mapping.setAttribute(key, mapping.getAttributeNames()[index], newBool);
					//mapping.getAttributes()
					 //      .setAttribute(key, mapping.getAttributeNames()[index], newBool);
				} catch (Exception e) {
					invalid.put(key.toString(), entry);
				}

				break;

			case AttributeTypes.TYPE_INTEGER:

				//Integer newInt;

				try {
					//newInt = new Integer(entry);
					setAttributeForType(table,AttributeTypes.TYPE_INTEGER,key, mapping.getAttributeNames()[index], entry);
					//mapping.setAttribute(key, mapping.getAttributeNames()[index], newInt);
					//mapping.getAttributes()
					 //      .setAttribute(key, mapping.getAttributeNames()[index], newInt);
				} catch (Exception e) {
					invalid.put(key.toString(), entry);
				}

				break;

			case AttributeTypes.TYPE_FLOATING:

				//Double newDouble;

				try {
					//newDouble = new Double(entry);
					setAttributeForType(table,AttributeTypes.TYPE_FLOATING,key, mapping.getAttributeNames()[index], entry);
					//mapping.getAttributes()
					 //      .setAttribute(key, mapping.getAttributeNames()[index], newDouble);
				} catch (Exception e) {
					invalid.put(key.toString(), entry);
				}

				break;

			case AttributeTypes.TYPE_STRING:
				try {
					setAttributeForType(table,AttributeTypes.TYPE_STRING,key, mapping.getAttributeNames()[index], entry);
					//mapping.setAttribute(key, mapping.getAttributeNames()[index], entry);
					//mapping.getAttributes().setAttribute(key, mapping.getAttributeNames()[index], entry);
				} catch (Exception e) {
					invalid.put(key.toString(), entry);
				}

				break;

			case AttributeTypes.TYPE_SIMPLE_LIST:

				/*
				 * In case of list, not overwrite the attribute. Get the existing
				 * list, and add it to the list.
				 *
				 * Since list has data types for their data types, so we need to
				 * extract it first.
				 *
				 */
				final Byte[] listTypes = mapping.getListAttributeTypes();
				final Byte listType;

				if (listTypes != null) {
					listType = listTypes[index];
				} else {
					listType = AttributeTypes.TYPE_STRING;
				}

				ArrayList curList = new ArrayList();//mapping.getAttributes()
				                     // .getListAttribute(key, mapping.getAttributeNames()[index]);

				curList.addAll(buildList(entry, listType));
				try {
					setListAttribute(table,mapping.getListAttributeTypes()[index],key, mapping.getAttributeNames()[index], curList);

					//mapping.setAttribute(key, mapping.getAttributeNames()[index], curList);

					//mapping.getAttributes()
					//       .setListAttribute(key, mapping.getAttributeNames()[index], curList);
				} catch (Exception e) {
					invalid.put(key.toString(), entry);
				}

				break;

			default:
				try {
					//mapping.getAttributes().setAttribute(key, mapping.getAttributeNames()[index], entry);
				} catch (Exception e) {
					invalid.put(key.toString(), entry);
				}
		}
	}

	public static void setAttributeForType(CyTable tbl, byte type, Object key, String attributeName, String val){
		if (tbl.getColumn(attributeName) == null) {
			if (type == AttributeTypes.TYPE_INTEGER)
				tbl.createColumn(attributeName, Integer.class, false);
			else if (type == AttributeTypes.TYPE_BOOLEAN)
				tbl.createColumn(attributeName, Boolean.class, false);
			else if (type == AttributeTypes.TYPE_FLOATING)
				tbl.createColumn(attributeName, Double.class, false);
			else // type is String
				tbl.createColumn(attributeName, String.class, false);
		}

		CyRow row = tbl.getRow(key);

		if (type == AttributeTypes.TYPE_INTEGER)
			row.set(attributeName, new Integer(val));
		else if (type == AttributeTypes.TYPE_BOOLEAN)
			row.set(attributeName, new Boolean(val));
		else if (type == AttributeTypes.TYPE_FLOATING)
			row.set(attributeName, (new Double(val)));
		else // type is String
			row.set(attributeName, new String(val));
	}

	public static void setListAttribute(CyTable tbl, byte type, Object key, String attributeName, final ArrayList elmsBuff) {
		if (tbl.getColumn(attributeName) == null) {
			if (type == AttributeTypes.TYPE_INTEGER)
				tbl.createListColumn(attributeName, Integer.class, false);
			else if (type == AttributeTypes.TYPE_BOOLEAN)
				tbl.createListColumn(attributeName, Boolean.class, false);
			else if (type == AttributeTypes.TYPE_FLOATING)
				tbl.createListColumn(attributeName, Double.class, false);
			else if (type == AttributeTypes.TYPE_STRING){
				tbl.createListColumn(attributeName, String.class, false);
			}
			else
				;//invalid type
		}
		CyRow row = tbl.getRow(key);
		row.set(attributeName, elmsBuff);
	}

	protected Map getInvalidMap() {
		return invalid;
	}

	/**
	 * If an entry is a list, split the string and create new List Attribute.
	 *
	 * @return
	 */
	private List buildList(final String entry, final Byte dataType) {
		if (entry == null) {
			return null;
		}
		String delimiter = mapping.getListDelimiter();
		
		if(delimiter.isEmpty())
			delimiter = " ";
		
		final String[] parts = (entry.replace("\"", "")).split(delimiter);

		final List listAttr = new ArrayList();

		for (String listItem : parts) {


			switch (dataType) {
				case AttributeTypes.TYPE_BOOLEAN:
					listAttr.add(Boolean.parseBoolean(listItem.trim()));

					break;

				case AttributeTypes.TYPE_INTEGER:
					listAttr.add(Integer.parseInt(listItem.trim()));

					break;

				case AttributeTypes.TYPE_FLOATING:
					listAttr.add(Double.parseDouble(listItem.trim()));

					break;

				case AttributeTypes.TYPE_STRING:
					listAttr.add(listItem.trim());

					break;

				default:
					break;
			}

		}

		return listAttr;
	}
}
