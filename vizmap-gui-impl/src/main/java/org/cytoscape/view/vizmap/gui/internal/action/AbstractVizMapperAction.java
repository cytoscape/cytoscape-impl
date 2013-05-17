package org.cytoscape.view.vizmap.gui.internal.action;

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

import java.util.Properties;

import javax.swing.JMenuItem;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.DefaultViewEditor;
import org.cytoscape.view.vizmap.gui.internal.theme.IconManager;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperMainPanel;

import com.l2fprod.common.propertysheet.PropertySheetPanel;

/**
 * Action class to process commands.
 */
public abstract class AbstractVizMapperAction extends AbstractCyAction {

	private static final long serialVersionUID = 1499424630636172107L;

	protected DefaultViewEditor defViewEditor;
	protected VisualMappingManager vmm;
	protected VizMapperMainPanel vizMapperMainPanel;
	protected IconManager iconManager;

	protected final PropertySheetPanel propertySheetPanel;

	protected Properties vizmapUIResource;

	protected String menuLabel;
	protected String iconId;
	protected JMenuItem menuItem;

	protected final CyApplicationManager applicationManager;

	public AbstractVizMapperAction(final String name, final CyApplicationManager applicationManager,
			final PropertySheetPanel propertySheetPanel) {
		super(name);
		this.propertySheetPanel = propertySheetPanel;
		this.applicationManager = applicationManager;
	}

	public void setDefaultAppearenceBuilder(DefaultViewEditor defViewEditor) {
		this.defViewEditor = defViewEditor;
	}

	public void setVmm(VisualMappingManager vmm) {
		this.vmm = vmm;
	}
	
	public void setVizMapperMainPanel(VizMapperMainPanel vizMapperMainPanel) {
		this.vizMapperMainPanel = vizMapperMainPanel;
	}
	
	public void setMenuLabel(final String menuLabel) {
		this.menuLabel = menuLabel;
	}
	
	public void setIconId(final String iconId) {
		this.iconId = iconId;
	}
	
	public void setIconManager(IconManager iconManager) {
		this.iconManager = iconManager;
	}
}
