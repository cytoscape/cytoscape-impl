package org.cytoscape.io.internal.read.xgmml.handler;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleViewGraph extends AbstractHandler {

	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
		manager.graphCount++;
		String netId = atts.getValue("cy:networkId");
		
		// There should be only no nested graph tags!
		if (manager.graphCount > 1) {
			logger.warn("CyNetworkView XGMML does not support nested graphs. The nested graph will be ignored.");
			return current;
		}
		
		final String docVersion = atts.getValue("cy:documentVersion");

		if (docVersion != null)
			manager.setDocumentVersion(docVersion); // version 3.0+

//		final String view = atts.getValue("cy:view");
//		manager.setViewFormat(view != null && AttributeValueUtil.fromXGMMLBoolean(view));

		// The CyNetwork should be set already!
		// if (manager.getCurrentNetwork() == null)
		// throw new SAXException("Cannot parse CyNetworkView XGMML: The required CyNetwork is not set.");

		manager.setNetworkViewId(atts.getValue("id"));
		manager.setNetworkId(netId);
		manager.setCurrentElementId(netId);
		manager.setVisualStyleName(atts.getValue("cy:visualStyle"));
		manager.setRendererName(atts.getValue("cy:renderingEngine"));
		
		return current;
	}
}