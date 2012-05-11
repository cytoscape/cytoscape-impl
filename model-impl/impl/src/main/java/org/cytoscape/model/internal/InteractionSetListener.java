package org.cytoscape.model.internal;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.subnetwork.CyRootNetwork;

public class InteractionSetListener extends ColumnSetListener {


	InteractionSetListener() {
		super(CyEdge.INTERACTION, CyRootNetwork.SHARED_INTERACTION);
	}

}
