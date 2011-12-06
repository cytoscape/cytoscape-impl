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
		
		addCurrentNetwork(label, currentNet);
		setNetworkNameFromLabel(atts);
		
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
		
		addCurrentNetwork(id, currentNet);
		setNetworkNameFromLabel(atts); // TODO: not necessary--remove it?
		
		return current;
	}
	
	private ParseState handleGenericXGMMLGraph(String tag, Attributes atts, ParseState current) throws SAXException {
		final CyNetwork currentNet;
		final String id = getId(atts);

		if (manager.graphCount == 1) {
			// Root (graph) element...
			final CyRootNetwork rootNet = manager.createRootNetwork();
			currentNet = rootNet.getBaseNetwork();
		} else {
			// Nested graph tag...
			final CyRootNetwork rootNet = manager.getRootNetwork();
			currentNet = rootNet.addSubNetwork();
		}

		addCurrentNetwork(id, currentNet);
		setNetworkNameFromLabel(atts);

		return current;
	}
	
	protected void addCurrentNetwork(String oldId, CyNetwork net) {
		manager.setCurrentNetwork(net);
		manager.getNetworkStack().push(oldId);
		
		if (net != null) {
			manager.cache(net, oldId);
			
			if (!(net instanceof CyRootNetwork))
				manager.addNetwork(oldId, net);
		}
	}
	
	/**
	 * Should be used when handling 2.x format only or importing the network from a standalone XGMML file.
	 * @param atts
	 */
	private void setNetworkNameFromLabel(Attributes atts) {
		final String name = getLabel(atts);
		
		if (name != null) {
			CyRow netRow = manager.getCurrentNetwork().getCyRow();
			netRow.set(CyNetwork.NAME, name);
		}
	}
	
	private String getLabel(Attributes atts) {
		String label = atts.getValue("label");
		if (label != null) return label;

		return getId(atts);
	}
	
	private String getId(Attributes atts) {
		return atts.getValue("id");
	}
}