package org.cytoscape.task.internal.export.vizmap;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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
import java.util.Set;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.VizmapWriterManager;
import org.cytoscape.io.write.VizmapWriterFactory;
import org.cytoscape.task.internal.export.TunableAbstractCyWriter;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Tunable;

public class VizmapWriter extends TunableAbstractCyWriter<VizmapWriterFactory,VizmapWriterManager> {

	private final VisualMappingManager vmMgr;

	public VizmapWriter(VizmapWriterManager writerManager, VisualMappingManager vmMgr) {
		super(writerManager);
		if (vmMgr == null) throw new NullPointerException("VisualMappingManager is null");
		this.vmMgr = vmMgr;
	}

	@Override
	protected CyWriter getWriter(CyFileFilter filter, File file) throws Exception {
		if (!fileExtensionIsOk(file))
			file = addOrReplaceExtension(outputFile);

		Set<VisualStyle> styles = vmMgr.getAllVisualStyles();

		return writerManager.getWriter(styles, filter, file);
	}
	
	@Tunable(description="Save Vizmap As:", params="fileCategory=vizmap;input=false")
	@Override
	public File getOutputFile() {
		return outputFile;
	}
	
	@ProvidesTitle
	public String getTitle() {
		return "Export Vizmap";
	}
}
