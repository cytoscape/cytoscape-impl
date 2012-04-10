package org.cytoscape.model.internal;

import java.util.List;


import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.subnetwork.CyRootNetwork;

public class InteractionSetListener extends ColumnSetListener {


	InteractionSetListener() {
		super(CyEdge.INTERACTION, CyRootNetwork.SHARED_INTERACTION);
	}

}
