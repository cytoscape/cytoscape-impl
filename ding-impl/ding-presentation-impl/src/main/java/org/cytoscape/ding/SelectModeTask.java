package org.cytoscape.ding;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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


import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class SelectModeTask extends AbstractTask {

	private final CyApplicationManager applicationManagerServiceRef;
	private final String actionName;

	public SelectModeTask(final String actionName, final CyApplicationManager applicationManagerServiceRef){
		this.applicationManagerServiceRef = applicationManagerServiceRef;
		this.actionName = actionName;
	}
	
	@Override
	public void run(final TaskMonitor taskMonitor) {
		final CyNetworkView view = this.applicationManagerServiceRef.getCurrentNetworkView();

		if (view != null) {
			if (actionName.equalsIgnoreCase("Nodes only")) {
				view.clearValueLock(DVisualLexicon.NETWORK_NODE_SELECTION);
				view.setLockedValue(DVisualLexicon.NETWORK_EDGE_SELECTION, Boolean.FALSE);
			} else if (actionName.equalsIgnoreCase("Edges only")) {
				view.clearValueLock(DVisualLexicon.NETWORK_EDGE_SELECTION);
				view.setLockedValue(DVisualLexicon.NETWORK_NODE_SELECTION, Boolean.FALSE);
			} else if (actionName.equalsIgnoreCase("Nodes and Edges")) {
				view.clearValueLock(DVisualLexicon.NETWORK_NODE_SELECTION);
				view.clearValueLock(DVisualLexicon.NETWORK_EDGE_SELECTION);
			}
		}
	}
}
