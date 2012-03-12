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

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleGraph extends AbstractHandler {
	
	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
		manager.graphCount++;
		
		if (manager.graphCount == 1) {
			// Root <graph>...
			final String docVersion = atts.getValue("cy:documentVersion");
			
			if (docVersion != null)
				manager.setDocumentVersion(docVersion); // version 3.0+
		}
		
		if (manager.isSessionFormat()) {	
			if (manager.getDocumentVersion() >= 3.0) {
				handleCy3Model(tag, atts, current);
			} else {
				handleCy2ModelAndView(tag, atts, current);
			}
		} else {
			handleGenericXGMMLGraph(tag, atts, current);
		}
		
		return current;
	}
	
	private ParseState handleCy2ModelAndView(String tag, Attributes atts, ParseState current) throws SAXException {
		final CyRootNetwork parent = manager.getParentNetwork();
		final CyNetwork currentNet;
		
		if (manager.graphCount == 1) {
			// Root (graph) element...
			if (parent == null) {
				// This is a regular top-level network...
				final CyRootNetwork rootNet = manager.createRootNetwork();
				currentNet = rootNet.getBaseNetwork(); // The root-network is not important here!
			} else {
				// This is a 2.x "child-network"...
				currentNet = parent.addSubNetwork();
			}
		} else {
			// Nested graph tag...
			final CyRootNetwork rootNet = manager.getRootNetwork();
			currentNet = rootNet.addSubNetwork();
		}
		
		final String id = getLabel(atts); // This is the network ID in 2.x
		addCurrentNetwork(id, currentNet, atts);
		
		return current;
	}
	
	private ParseState handleCy3Model(String tag, Attributes atts, ParseState current) throws SAXException {
		final CyNetwork currentNet;
		
		if (manager.graphCount == 1) {
			// Root graph == CyRootNetwork 
			currentNet = manager.createRootNetwork();
		} else if (manager.graphCount == 2) {
			// First nested graph == base-network
			final CyRootNetwork rootNet = manager.getRootNetwork();
			currentNet = rootNet.getBaseNetwork();
		} else {
			// Other nested graphs == regular sub-networks
			final CyRootNetwork rootNet = manager.getRootNetwork();
			currentNet = rootNet.addSubNetwork();
		}
		
		final Object id = getId(atts);
		addCurrentNetwork(id, currentNet, atts);
		
		return current;
	}
	
	private ParseState handleGenericXGMMLGraph(String tag, Attributes atts, ParseState current) throws SAXException {
		final CyNetwork currentNet;

		if (manager.graphCount == 1) {
			// Root (graph) element...
			final CyRootNetwork rootNet = manager.createRootNetwork();
			currentNet = rootNet.getBaseNetwork();
		} else {
			// Nested graph tag...
			final CyRootNetwork rootNet = manager.getRootNetwork();
			currentNet = rootNet.addSubNetwork();
		}

		final Object id = getId(atts);
		addCurrentNetwork(id, currentNet, atts);

		return current;
	}
	
	/**
	 * @param oldId The original Id of the graph element. If null, one will be created.
	 * @param net Can be null if just adding an XLink to an existing network
	 * @param atts The attributes of the graph tag
	 * @return The string identifier of the network
	 */
	protected Object addCurrentNetwork(Object oldId, CyNetwork net, Attributes atts) {
		if (oldId == null)
			oldId = String.format("_graph%s_%s", manager.graphCount, net.getSUID());
		
		manager.setCurrentElement(net);
		manager.getNetworkIDStack().push(oldId);
		
		if (net != null) {
			manager.getCache().cache(oldId, net);
			
			if (!(net instanceof CyRootNetwork))
				manager.addNetwork(net);
			
			if (!manager.isSessionFormat() || manager.getDocumentVersion() < 3.0)
				setNetworkName(net, atts);
		} else {
			manager.setCurrentNetwork(null);
		}
		
		return oldId;
	}
	
	/**
	 * Should be used when handling 2.x format only or importing the network from a standalone XGMML file.
	 */
	protected void setNetworkName(CyNetwork net, Attributes atts) {
		String name = getLabel(atts);
		
		if (name == null || name.trim().isEmpty()) {
			if (net instanceof CyRootNetwork) {
				name = "Root-Network " + net.getSUID();
			} else if (manager.graphCount == 1) {
				name = "Network " + net.getSUID();
			} else {
				CyRootNetwork root = manager.getRootNetwork();
				
				if (root != null) {
					name = root.getBaseNetwork().getRow(root.getBaseNetwork()).get(CyNetwork.NAME, String.class);
					
					if (name == null || name.trim().isEmpty())
						name = root.getRow(root).get(CyNetwork.NAME, String.class);
				}
			}
			
			if (name == null || name.trim().isEmpty())
				name = "Network " + net.getSUID();
			
			name += " - " + manager.graphCount;
		}
		
		if (net != null && name != null) {
			CyRow netRow = net.getRow(net);
			netRow.set(CyNetwork.NAME, name);
		}
	}
}
