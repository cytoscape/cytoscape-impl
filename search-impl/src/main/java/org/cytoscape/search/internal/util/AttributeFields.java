/*
 Copyright (c) 2006, 2007, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.search.internal.util;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyTable; 
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyTableManager;


/**
 * This object will serve as input to the CustomMultiFieldQueryParser.
 * It contains attribute fields names and their types.
 * This way CustomMultiFieldQueryParser can recognize numeric attribute fields.
 */
public class AttributeFields {
	private final CyTableManager tableMgr;
	private final Map<String, Class<?>> columnTypeMap;

	public AttributeFields(final CyNetwork network, final CyTableManager tableMgr) {
		this.tableMgr = tableMgr;
		this.columnTypeMap = new HashMap<String, Class<?>>();
		initFields(network);
	}

	/**
	 * Initialize this object with attribute fields names and their type.
	 * Eventually, fields[i] will hold attribute field name and types[i] will hold its type.
	 * fields[] and types[] contain both node and edge attributes.
	 * ID (INDEX_FIELD) is treated as another attribute of type string.
	 * There are probably better ways to do this, but there you go :)
	 */
	private void initFields(final CyNetwork network) {
		CyTable nodeCyDataTable = tableMgr.getTableMap(CyNode.class, network).get(CyNetwork.DEFAULT_ATTRS);
		for (final CyColumn column : nodeCyDataTable.getColumns())
			columnTypeMap.put(EnhancedSearchUtils.replaceWhitespace(column.getName()),
					  column.getType());

		CyTable edgeCyDataTable = (CyTable) tableMgr.getTableMap(CyEdge.class, network).get(CyNetwork.DEFAULT_ATTRS);
		for (final CyColumn column : edgeCyDataTable.getColumns())
			columnTypeMap.put(EnhancedSearchUtils.replaceWhitespace(column.getName()),
					  column.getType());
	}

	/**
	 * Get list of fields
	 */
	public String[] getFields() {
		final String[] keys = new String[columnTypeMap.size()];
		
		int i = 0;
		for (final String key : columnTypeMap.keySet())
			keys[i++] = key;

		return keys;
	}

	public Class<?> getType(final String attrName) {
		Class<?> valueType = columnTypeMap.get(attrName);
		return valueType;
	}
}
