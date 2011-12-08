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
		final String label = getLabel(atts); // This is the network ID in 2.x
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
		
		addCurrentNetwork(label, currentNet, atts);
		
		return current;
	}
	
	private ParseState handleCy3Model(String tag, Attributes atts, ParseState current) throws SAXException {
		final String id = getId(atts);
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
		
		addCurrentNetwork(id, currentNet, atts);
		
		return current;
	}
	
	private ParseState handleGenericXGMMLGraph(String tag, Attributes atts, ParseState current) throws SAXException {
		final CyNetwork currentNet;
		String id = getId(atts);

		if (manager.graphCount == 1) {
			// Root (graph) element...
			final CyRootNetwork rootNet = manager.createRootNetwork();
			currentNet = rootNet.getBaseNetwork();
		} else {
			// Nested graph tag...
			final CyRootNetwork rootNet = manager.getRootNetwork();
			currentNet = rootNet.addSubNetwork();
		}

		addCurrentNetwork(id, currentNet, atts);

		return current;
	}
	
	/**
	 * @param oldId The original Id of the graph element. If null, one will be created.
	 * @param net Can be null if just adding an XLink to an existing network
	 * @param atts The attributes of the graph tag
	 * @return The string identifier of the network
	 */
	protected String addCurrentNetwork(String oldId, CyNetwork net, Attributes atts) {
		if (oldId == null)
			oldId = String.format("_graph%s_%s", manager.graphCount, net.getSUID());
		
		manager.setCurrentNetwork(net);
		manager.getNetworkStack().push(oldId);
		
		if (net != null) {
			manager.getCache().cache(net, oldId);
			
			if (!(net instanceof CyRootNetwork))
				manager.addNetwork(net);
			
			if (!manager.isSessionFormat() || manager.getDocumentVersion() < 3.0)
				setNetworkNameFromLabel(net, atts);
		}
		
		return oldId;
	}
	
	/**
	 * Should be used when handling 2.x format only or importing the network from a standalone XGMML file.
	 * @param atts
	 */
	protected void setNetworkNameFromLabel(CyNetwork net, Attributes atts) {
		final String name = getLabel(atts);
		
		if (net != null && name != null) {
			CyRow netRow = net.getCyRow(net);
			netRow.set(CyNetwork.NAME, name);
		}
	}
	
	protected String getLabel(Attributes atts) {
		String label = atts.getValue("label");
		
		if (label == null || label.isEmpty())
			label = atts.getValue("id");

		return label;
	}
	
	protected String getId(Attributes atts) {
		String id = atts.getValue("id");
		
		if (id == null || id.isEmpty())
			id = atts.getValue("label");
		
		return id;
	}
}
