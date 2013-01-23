package org.cytoscape.view.manual.internal.common;

/*
 * #%L
 * Cytoscape Manual Layout Impl (manual-layout-impl)
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


import java.util.HashSet;
import java.util.Set;

import javax.swing.JCheckBox;

import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.model.CyNetwork;


public class CheckBoxTracker implements /*SelectEventListener,*/ SetCurrentNetworkListener {
	private JCheckBox jCheckBox;
	private Set<CyNetwork> listeningNetworks;

	public CheckBoxTracker(JCheckBox j) {
		jCheckBox = j;
		listeningNetworks = new HashSet<CyNetwork>();
	}

/*	public void onSelectEvent(SelectEvent event) {
		jCheckBox.setSelected(Cytoscape.getCurrentNetworkView().getSelectedNodeIndices().length > 0);
	} */

	public void handleEvent(SetCurrentNetworkEvent e) {
			CyNetwork curr = e.getNetwork();

//			// only add this as a listener if it hasn't been done already
//			if ( !listeningNetworks.contains(curr) )
//				curr.addSelectEventListener(this);	
//		
//		 	// to make sure we're set intially	
//			onSelectEvent(null);
	}
}
