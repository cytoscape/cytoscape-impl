package org.cytoscape.io.internal.read.xgmml.handler;

import java.util.Map;
import java.util.Set;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * handleGraphDone is called when we finish parsing the entire XGMML file. This
 * allows us to do deal with some cleanup line creating all of our groups, etc.
 */
public class HandleGraphDone extends AbstractHandler {

	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
		if (++manager.graphDoneCount != manager.graphCount)
			return current;
		
		// Resolve any unresolved node and edge references
		Map<CyNetwork, Set<String>> nodeMap = manager.getNodeLinks();
		
		for (Map.Entry<CyNetwork, Set<String>> entry : nodeMap.entrySet()) {
			CyNetwork net = entry.getKey();
			Set<String> ids = entry.getValue();
			
			if (net != null && ids != null && !ids.isEmpty()) {
				if (net instanceof CySubNetwork) {
					CySubNetwork sn = (CySubNetwork) net;
					
					for (String id : ids) {
						CyNode n = manager.getNode(id);
						
						if (n != null)
							sn.addNode(n);
						else
							logger.error("Cannot find XLink node with id \"" + id + "\".");
					}
				} else {
					logger.error("Cannot add existing nodes \"" + ids.toArray()
							+ "\" to a network which is not a CySubNetwork");
				}
			}
		}
		
		// TODO: refactor
		Map<CyNetwork, Set<String>> edgeMap = manager.getEdgeLinks();
		
		for (Map.Entry<CyNetwork, Set<String>> entry : edgeMap.entrySet()) {
			CyNetwork net = entry.getKey();
			Set<String> ids = entry.getValue();
			
			if (net != null && ids != null && !ids.isEmpty()) {
				if (net instanceof CySubNetwork) {
					CySubNetwork sn = (CySubNetwork) net;
					
					for (String id : ids) {
						CyEdge e = manager.getEdge(id);
						
						if (e != null)
							sn.addEdge(e);
						else
							logger.error("Cannot find XLink edge with id \"" + id + "\".");
					}
				} else {
					logger.error("Cannot add existing edges \"" + ids.toArray()
							+ "\" to a network which is not a CySubNetwork");
				}
			}
		}
		
		return current;
	}
}
