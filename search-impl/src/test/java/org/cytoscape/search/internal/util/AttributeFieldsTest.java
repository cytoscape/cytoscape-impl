package org.cytoscape.search.internal.util;

/*
 * #%L
 * Cytoscape Search Impl (search-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.*;

import java.util.HashSet;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.NetworkTestSupport;

public class AttributeFieldsTest {
	
	@Test
	public void testEmptyNetwork() {
		// a network isn't really empty cause it has 10 columns in it, but
		// no data
		final NetworkTestSupport nts = new NetworkTestSupport();
		CyNetwork network = nts.getNetwork();
		
		// so there is a bug here, if there exists a column with same
		// name on node and on edge it is only included once which is fine
		// if there types are the same, but if the types differ the type
		// from the edge table will be the one returned from getType() 
		// method
		HashSet<String> fieldNameSet = new HashSet<>();
		CyTable nodeCyDataTable = network.getDefaultNodeTable();
		for (final CyColumn column : nodeCyDataTable.getColumns()) {
			fieldNameSet.add(column.getName().toLowerCase());
		}
		CyTable edgeCyDataTable = network.getDefaultEdgeTable();
		for (final CyColumn column : edgeCyDataTable.getColumns()) {
			fieldNameSet.add(column.getName().toLowerCase());
		}
		network.getRow(network).set(CyNetwork.NAME, "My network");
		AttributeFields fields = new AttributeFields(network);
		assertEquals(fieldNameSet.size(), fields.getFields().length);
		
		// assuming suid column will be a type Long
		assertEquals("java.lang.Long", fields.getType("suid").getName());
	}
	

}
