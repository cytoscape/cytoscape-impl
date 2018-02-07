package org.cytoscape.task.internal.export.graphics;

import java.io.File;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.PresentationWriterFactory;
import org.cytoscape.io.write.PresentationWriterManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.task.internal.export.ViewWriter;
import org.cytoscape.task.write.ExportNetworkImageTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import org.cytoscape.task.internal.export.TunableAbstractCyWriter;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2017 The Cytoscape Consortium
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

public class ExportNetworkImageCommandTask extends TunableAbstractCyWriter<PresentationWriterFactory, PresentationWriterManager> {

	public final CyServiceRegistrar serviceRegistrar;

	@Tunable(description="Network View to export", context="nogui")
	public CyNetworkView view;

	public ExportNetworkImageCommandTask(CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar.getService(PresentationWriterManager.class),
				serviceRegistrar.getService(CyApplicationManager.class));
		this.serviceRegistrar = serviceRegistrar;

		// Pick PNG as a default file format
		for (String fileTypeDesc : this.getFileFilterDescriptions()) {
			if (fileTypeDesc.contains("PNG")) {
				options.setSelectedValue(fileTypeDesc);
				break;
			}
		}
	}

	@Override
	protected CyWriter getWriter(CyFileFilter filter) throws Exception {
		// Get the rendering engine
		RenderingEngine<?> engine = serviceRegistrar.getService(CyApplicationManager.class).getCurrentRenderingEngine();

		if (view == null) return null;

		// Now get the rendering engine for this view and use this one if we can
		String engineId = view.getRendererId();
		RenderingEngineManager engineManager = serviceRegistrar.getService(RenderingEngineManager.class);

		for (RenderingEngine<?> e : engineManager.getRenderingEngines(view)) {
			if (engineId.equals(e.getRendererId())) {
				engine = e;
				break;
			}
		}
		return writerManager.getWriter(view, engine, filter, outputStream);
	}

	@Tunable(
			description = "Save Image as:",
			longDescription = "The path name of the file where the view must be saved to. "
					+ "By default, the view's title is used as the file name.",
			exampleStringValue = "/Users/johndoe/Downloads/View1.png",
			params = "fileCategory=image;input=false",
			dependsOn = "options!=",
			gravity = 1.1
	)
	@Override
	public File getOutputFile() {
		return outputFile;
	}

	@ProvidesTitle
	public String getTitle() {
		return "Export Network as Image";
	}

	@Override
	protected String getExportName() {
		if (view == null) view = cyApplicationManager.getCurrentNetworkView();
		if (view == null) return null;
		String name = view.getVisualProperty(BasicVisualLexicon.NETWORK_TITLE);

		if (name == null || name.trim().isEmpty())
			name = view.getModel().getRow(view.getModel()).get(CyNetwork.NAME, String.class);

		return name;
	}

}
