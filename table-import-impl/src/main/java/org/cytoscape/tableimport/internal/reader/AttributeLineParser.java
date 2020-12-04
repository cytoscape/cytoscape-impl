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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.SUIDFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.tableimport.internal.util.AttributeDataType;
import org.cytoscape.tableimport.internal.util.SourceColumnSemantic;

/**
 * Take a line of data, analyze it, and map to CyAttributes.
 */
public class AttributeLineParser extends AbstractLineParser {
	
	private final AttributeMappingParameters mapping;
	private final Map<String, Object> invalid = new HashMap<>();

	public AttributeLineParser(final AttributeMappingParameters mapping, final CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
		this.mapping = mapping;
	}

	/**
	 * Import everything regardless associated nodes/edges exist or not.
	 * @param parts entries in a line.
	 */
	public void parseAll(final CyTable table, final String[] parts) {
		// Get key
		final Object primaryKey;
		final int partsLen = parts.length;
		final int keyIndex = mapping.getKeyIndex();
		
		if (keyIndex >= 0) {
			if(keyIndex < partsLen) {
				final AttributeDataType typeKey = mapping.getDataTypes()[keyIndex];
				
				String part = "";
				if(parts[keyIndex] != null) {
					part = parts[keyIndex].trim();
				}

				switch (typeKey) {
				case TYPE_BOOLEAN:
					primaryKey = Boolean.valueOf(part);
					break;
				case TYPE_INTEGER:
					primaryKey = Integer.valueOf(part);
					break;
				case TYPE_LONG:
					primaryKey = Long.valueOf(part);
					break;
				case TYPE_FLOATING:
					primaryKey = Double.valueOf(part);
					break;
				default:
					primaryKey = part;
				}

			} else {
				primaryKey = null;
			}
		} else { // Not importing a key column, so the table must have the default SUID one
			if (!table.getPrimaryKey().getName().equals(CyTable.SUID))
				throw new RuntimeException("When not importing a primary key columnm, the primary key must be the default 'SUID'");
			if (table.getPrimaryKey().getType() != Long.class)
				throw new RuntimeException("The type of the primary key column 'SUID' must be Long");
			
			primaryKey = SUIDFactory.getNextSUID();
		}

		if(primaryKey != null) {
			if (keyIndex >= 0 && partsLen == 1) {
				table.getRow(parts[0]);
			} else {
				final SourceColumnSemantic[] types = mapping.getTypes();

				for (int i = 0; i < partsLen; i++) {
					if (i != keyIndex && types[i] != SourceColumnSemantic.NONE) {
						if (parts[i] == null)
							continue;
						else
							mapAttribute(table, primaryKey, parts[i].trim(), i);
					}
				}
			}
		}
	}

	/**
	 * Based on the attribute types, map the entry to CyAttributes.<br>
	 */
	private void mapAttribute(final CyTable table, final Object key, final String entry, final int index) {
		final AttributeDataType type = mapping.getDataTypes()[index];

		try {
			if (type.isList()) {
				final String[] delimiters = mapping.getListDelimiters();
				String delimiter = delimiters != null && delimiters.length > index ?
						delimiters[index] : AbstractMappingParameters.DEF_LIST_DELIMITER;
						
				if (delimiter == null || delimiter.isEmpty())
					delimiter = AbstractMappingParameters.DEF_LIST_DELIMITER;
				
				Object value = parse(entry, type, delimiter, mapping.getDecimalSeparator());
				setListAttribute(table, type, key, mapping.getAttributeNames()[index], value);
			} else {
				setAttribute(table, type, key, mapping.getAttributeNames()[index], entry);
			}
		} catch (Exception e) {
			invalid.put(key.toString(), entry);
		}
	}

	private void setAttribute(final CyTable tbl, final AttributeDataType type, final Object key,
			final String attrName, final String attrValue) {
		if (tbl.getColumn(attrName) == null)
			tbl.createColumn(attrName, type.getType(), false);

		final Object value = parse(attrValue, type, null, mapping.getDecimalSeparator());
		final CyRow row = tbl.getRow(key);
		row.set(attrName, value);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setListAttribute(final CyTable tbl, final AttributeDataType type, final Object key,
			final String attributeName, Object value) {
		if (tbl.getColumn(attributeName) == null)
			tbl.createListColumn(attributeName, type.getListType(), false);
		
		final CyRow row = tbl.getRow(key);
		
		if (value instanceof List) {
			// In case of list, do not overwrite the attribute. Get the existing list, and add it to the list.
			List<?> curList = row.getList(attributeName, type.getListType());

			if (curList == null)
				curList = new ArrayList<>();
			
			curList.addAll((List)value);
			value = curList;
		}
		
		row.set(attributeName, value);
	}

	protected Map<String, Object> getInvalidMap() {
		return invalid;
	}
}
