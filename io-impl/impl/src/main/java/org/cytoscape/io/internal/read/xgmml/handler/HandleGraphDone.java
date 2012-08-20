/*
 Copyright (c) 2006, 2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.io.internal.read.xgmml.handler;

import java.util.Map;
import java.util.Set;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.cytoscape.io.internal.util.SUIDUpdater;
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
		graphDone();
		
		// End of document?
		if (manager.graphDoneCount != manager.graphCount)
			return current;
		
		// Resolve any unresolved node and edge references
		Map<CyNetwork, Set<Long>> nodeMap = manager.getCache().getNodeLinks();
		
		for (Map.Entry<CyNetwork, Set<Long>> entry : nodeMap.entrySet()) {
			CyNetwork net = entry.getKey();
			Set<Long> ids = entry.getValue();
			
			if (net != null && ids != null && !ids.isEmpty()) {
				if (net instanceof CySubNetwork) {
					CySubNetwork sn = (CySubNetwork) net;
					
					for (Long id : ids) {
						CyNode n = manager.getCache().getNode(id);
						
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
		Map<CyNetwork, Set<Long>> edgeMap = manager.getCache().getEdgeLinks();
		
		for (Map.Entry<CyNetwork, Set<Long>> entry : edgeMap.entrySet()) {
			CyNetwork net = entry.getKey();
			Set<Long> ids = entry.getValue();
			
			if (net != null && ids != null && !ids.isEmpty()) {
				if (net instanceof CySubNetwork) {
					CySubNetwork sn = (CySubNetwork) net;
					
					for (Long id : ids) {
						CyEdge e = manager.getCache().getEdge(id);
						
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
		
		resolveEquations();
		resolveNetworkPointers();
		updateSUIDAttributes();
		createGroups(); // TODO: we should not create (and specially register) groups here!
		
		return ParseState.NONE;
	}
	
	protected void graphDone() {
		++manager.graphDoneCount;
		
		// In order to handle sub-graphs correctly
		if (!manager.getNetworkIDStack().isEmpty())
			manager.getNetworkIDStack().pop();
		
		CyNetwork currentNet = null;
		final Object netId = manager.getNetworkIDStack().isEmpty() ? null : manager.getNetworkIDStack().peek();
		
		if (netId != null)
			currentNet = manager.getCache().getNetwork(netId);
		
		manager.setCurrentElement(currentNet);
	}
	
	private void resolveEquations() {
		if (!manager.isSessionFormat() || manager.getDocumentVersion() < 3.0) {
			// 3.0+ session files should not contain raw attributes or equations!
			manager.parseAllEquations();
		}
	}
	
	private void resolveNetworkPointers() {
		if (!manager.isSessionFormat()) {
			// If reading a session file, network pointers should be processed after all XGMML files are parsed,
			// because some network references might not have been created yet.
			manager.getCache().createNetworkPointers();
		}
	}
	
	private void updateSUIDAttributes() {
		if (!manager.isSessionFormat()) {
			final SUIDUpdater updater = manager.getSUIDUpdater();
			updater.addTables(manager.getCache().getNetworkTables());
			updater.updateSUIDColumns();
		}
	}
	
	private void createGroups() {
		if (!manager.isSessionFormat())
			manager.createGroups();
	}
}
