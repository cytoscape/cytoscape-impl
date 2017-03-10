package org.cytoscape.view.manual.internal;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.slf4j.Logger;

/*
 * #%L
 * Cytoscape Manual Layout Impl (manual-layout-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public final class Util {
	
	public static List<View<CyNode>> findSelectedNodes(final CyNetworkView view) {
		final List<View<CyNode>> snv = new ArrayList<>();

		for (CyNode n : CyTableUtil.getNodesInState(view.getModel(), CyNetwork.SELECTED, true))
			snv.add(view.getNodeView(n));
		
		return snv; 
	}

	/**
	 * Utility method that invokes the code in Runnable.run on the AWT Event Dispatch Thread.
	 * @param runnable
	 */
	public static void invokeOnEDT(final Runnable runnable) {
		if (SwingUtilities.isEventDispatchThread())
			runnable.run();
		else
			SwingUtilities.invokeLater(runnable);
	}
	
	public static void invokeOnEDTAndWait(final Runnable runnable) {
		invokeOnEDTAndWait(runnable, null);
	}
	
	public static void invokeOnEDTAndWait(final Runnable runnable, final Logger logger) {
		if (SwingUtilities.isEventDispatchThread()) {
			runnable.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(runnable);
			} catch (Exception e) {
				if (logger != null)
					logger.error("Unexpected error", e);
				else
					e.printStackTrace();
			}
		}
	}
	
	private Util() {
	}
}
