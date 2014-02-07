package org.cytoscape.view.vizmap.gui.internal.task;

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

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.util.NotificationNames;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;

public class RenameVisualStyleTask extends AbstractTask {

	public static final String TITLE = "Rename Style";

	@Tunable(description = "Enter new Style name:")
	public String vsName;

	private String previousName;
	
	private final VisualStyle style;
	private final ServicesUtil servicesUtil;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public RenameVisualStyleTask(final VisualStyle style, final ServicesUtil servicesUtil) {
		this.style = style;
		this.servicesUtil = servicesUtil;
		final VisualMappingManager vmm = servicesUtil.get(VisualMappingManager.class);
		vsName = vmm.getCurrentVisualStyle().getTitle();
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@ProvidesTitle
	public String getTitle() {
		return TITLE;
	}
	
	@Override
	public void run(final TaskMonitor monitor) throws Exception {
		previousName = style.getTitle();
		final boolean renamed = renameVisualStyle(vsName);
		
		if (renamed) {
			final UndoSupport undo = servicesUtil.get(UndoSupport.class);
			undo.postEdit(new RenameVisualStyleEdit());
		}
	}

	// ==[ PRIVATE METHODS ]============================================================================================
	
	private boolean renameVisualStyle(final String name) {
		boolean renamed = false;
		
		if (name != null && style != null) {
			final VisualMappingManager vmMgr = servicesUtil.get(VisualMappingManager.class);
	
			if (style.equals(vmMgr.getDefaultVisualStyle()))
				throw new IllegalArgumentException("You cannot rename the default style.");
	
			// Ignore if user does not enter new name.
			if (!style.getTitle().equals(name)) {
				style.setTitle(name);
				renamed = true;
				servicesUtil.sendNotification(NotificationNames.VISUAL_STYLE_NAME_CHANGED, style);
			}
		}
		
		return renamed;
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class RenameVisualStyleEdit extends AbstractCyEdit {

		public RenameVisualStyleEdit() {
			super(getTitle());
		}

		@Override
		public void undo() {
			final VisualMappingManager vmMgr = servicesUtil.get(VisualMappingManager.class);
			
			if (style != null && previousName != null && !style.equals(vmMgr.getDefaultVisualStyle()))
				renameVisualStyle(previousName);
		}

		@Override
		public void redo() {
			final VisualMappingManager vmMgr = servicesUtil.get(VisualMappingManager.class);
			
			if (style != null && vsName != null && !style.equals(vmMgr.getDefaultVisualStyle()))
				renameVisualStyle(vsName);
		}
	}
}
