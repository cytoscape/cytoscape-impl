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

import java.io.File;
import java.io.FileOutputStream;
import java.util.Set;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.VizmapWriterFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.model.VizMapperProxy;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class MakeVisualStylesDefaultTask extends AbstractTask {

	public static final String TITLE = "Make Current Styles Default";
	
	@Tunable(description="<html>Do you want the current styles to be the default list<br>in all future Cytoscape sessions (this cannot be undone)?</html>",
			params="ForceSetDirectly=true;ForceSetTitle=Make Current Styles Default")
	public boolean confirm;
	
	private final ServicesUtil servicesUtil;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public MakeVisualStylesDefaultTask(final ServicesUtil servicesUtil) {
		this.servicesUtil = servicesUtil;
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@ProvidesTitle
	public String getTitle() {
		return TITLE;
	}
	
	@Override
	public void run(final TaskMonitor monitor) throws Exception {
		if (!confirm)
			return;
		
		final VisualMappingManager vmMgr = servicesUtil.get(VisualMappingManager.class);
		final Set<VisualStyle> currentStyles = vmMgr.getAllVisualStyles();
		
		if (!currentStyles.isEmpty()) {
			final VizmapWriterFactory vizmapWriterFactory = servicesUtil.get(VizmapWriterFactory.class);
			final CyApplicationConfiguration config = servicesUtil.get(CyApplicationConfiguration.class);
			final FileOutputStream os = new FileOutputStream(new File(config.getConfigurationDirectoryLocation(),
					VizMapperProxy.PRESET_VIZMAP_FILE));
			final CyWriter vizmapWriter = vizmapWriterFactory.createWriter(os, currentStyles);
			
			if (!cancelled)
				vizmapWriter.run(monitor);
		}
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
}
