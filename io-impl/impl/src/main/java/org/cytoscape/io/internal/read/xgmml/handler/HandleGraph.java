package org.cytoscape.io.internal.read.xgmml.handler;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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

import org.cytoscape.io.internal.read.xgmml.ObjectTypeMap;
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
	
	protected boolean isRegistered(Attributes atts) {
		String s = atts.getValue("cy:registered");

		return s == null || ObjectTypeMap.fromXGMMLBoolean(s);
	}
	
	/**
	 * Handles XGMML from Cytoscape 2.x session files only.
	 * @param tag
	 * @param atts
	 * @param current
	 * @return
	 * @throws SAXException
	 */
	private ParseState handleCy2ModelAndView(String tag, Attributes atts, ParseState current) throws SAXException {
		final CyRootNetwork parent = manager.getParentNetwork();
		final CyNetwork currentNet;
		
		if (manager.graphCount == 1) {
			// Root (graph) element...
			if (parent == null) {
				// This is a regular top-level network...
				final CyRootNetwork rootNet = manager.createRootNetwork();
				currentNet = rootNet.getBaseNetwork();
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
		addCurrentNetwork(id, currentNet, atts, true);
		
		return current;
	}
	
	/**
	 * Handles "CyNetwork-type" XGMML from Cytoscape 3 session files only.
	 * @param tag
	 * @param atts
	 * @param current
	 * @return
	 * @throws SAXException
	 */
	private ParseState handleCy3Model(String tag, Attributes atts, ParseState current) throws SAXException {
		final CyNetwork currentNet;
		boolean register = isRegistered(atts);
		
		if (manager.graphCount == 1) {
			// Root graph == CyRootNetwork 
			currentNet = manager.createRootNetwork();
			register = false;
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
		addCurrentNetwork(id, currentNet, atts, register);
		
		return current;
	}
	
	/**
	 * Handles standalone XGMML graphs, not associated with a session file.
	 * @param tag
	 * @param atts
	 * @param current
	 * @return
	 * @throws SAXException
	 */
	private ParseState handleGenericXGMMLGraph(String tag, Attributes atts, ParseState current) throws SAXException {
		final CyNetwork currentNet;

		if (manager.graphCount == 1) {
			// Root (graph) element...
			final CyRootNetwork parentNet = manager.getParentNetwork();
			
			if (parentNet == null) {
				final CyRootNetwork rootNet = manager.createRootNetwork();
				currentNet = rootNet.getBaseNetwork();
			} else {
				currentNet = parentNet.addSubNetwork();
			}
		} else {
			// Nested graph tag...
			final CyRootNetwork rootNet = manager.getRootNetwork();
			currentNet = rootNet.addSubNetwork();
		}

		final Object id = getId(atts);
		addCurrentNetwork(id, currentNet, atts, true);

		return current;
	}
	
	/**
	 * @param oldId The original Id of the graph element. If null, one will be created.
	 * @param net Can be null if just adding an XLink to an existing network
	 * @param atts The attributes of the graph tag
	 * @param register Should be true for networks that must be registered.
	 * @return The string identifier of the network
	 */
	protected Object addCurrentNetwork(Object oldId, CyNetwork net, Attributes atts, boolean register) {
		if (oldId == null)
			oldId = String.format("_graph%s_%s", manager.graphCount, net.getSUID());
		
		manager.setCurrentElement(net);
		manager.getNetworkIDStack().push(oldId);
		
		if (net != null) {
			manager.addNetwork(oldId, net, register);
			
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
