package org.cytoscape.tableimport.internal.util;

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

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyNetwork;

/**
 * Simple manager for loaded ontology dags.
 *
 */
public class OntologyDAGManager {
	
	private static final Map<String, CyNetwork> dagMap = new HashMap<String, CyNetwork>();
	
	public static CyNetwork getOntologyDAG(final String ontologyID) {
		return dagMap.get(ontologyID);
	}
	
	public static void addOntologyDAG(final String ontologyID, CyNetwork dag) {
		dagMap.put(ontologyID, dag);
	}

}
