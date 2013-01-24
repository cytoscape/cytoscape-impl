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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.event.VizMapEventHandler;
import org.cytoscape.view.vizmap.gui.internal.VizMapPropertySheetBuilder;
import org.cytoscape.view.vizmap.gui.internal.VizMapperMainPanel;

import com.l2fprod.common.propertysheet.PropertySheetPanel;


public abstract class AbstractVizMapEventHandler implements VizMapEventHandler {
	protected VisualMappingManager vmm;
	protected VizMapperMainPanel vizMapperMainPanel;
	protected VizMapPropertySheetBuilder vizMapPropertySheetBuilder;
	protected PropertySheetPanel propertySheetPanel;
	protected CyApplicationManager applicationManager;

	/**
	 * Creates a new AbstractVizMapEventHandler object.
	 */
	public AbstractVizMapEventHandler() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.cytoscape.application.swing.vizmap.gui.event.VizMapEventHandler#processEvent(java.beans
	 * .PropertyChangeEvent)
	 */
	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public abstract void processEvent(PropertyChangeEvent e);
}
