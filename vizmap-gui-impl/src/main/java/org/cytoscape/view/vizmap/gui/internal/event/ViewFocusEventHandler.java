package org.cytoscape.view.vizmap.gui.internal.event;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

import java.beans.PropertyChangeEvent;

//import cytoscape.internal.view.NetworkPanel;

public class ViewFocusEventHandler extends AbstractVizMapEventHandler {

	@Override
	public void processEvent(PropertyChangeEvent e) {

		//FIXME
//		if (e.getSource().getClass() != NetworkPanel.class)
//			return;
//
//		// Get visual style for the selected netwrok view.
//		final VisualStyle vs = vmm.getVisualStyle(cyNetworkManager
//				.getCurrentNetworkView());
//
//		if (vs != null) {
//
//			if (vs.equals(vizMapperMainPanel.getSelectedVisualStyle()) == false) {
//
//				vizMapperMainPanel.switchVS(vs);
//
//				vizMapperMainPanel.setDefaultViewImagePanel(vizMapperMainPanel
//						.getDefaultImageManager().get(vs));
//			}
//		}
	}

}
