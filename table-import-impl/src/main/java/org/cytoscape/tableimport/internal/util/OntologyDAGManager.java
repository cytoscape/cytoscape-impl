package org.cytoscape.tableimport.internal.util;

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
