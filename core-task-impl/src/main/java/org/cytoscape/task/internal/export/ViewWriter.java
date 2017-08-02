package org.cytoscape.task.internal.export;

import java.io.File;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.PresentationWriterFactory;
import org.cytoscape.io.write.PresentationWriterManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Tunable;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

/**
 * A utility Task implementation that will write the specified View to the
 * the specified image file using the specified RenderingEngine.
 */
public final class ViewWriter extends TunableAbstractCyWriter<PresentationWriterFactory, PresentationWriterManager> {
	
	private final CyNetworkView view;
	private final RenderingEngine<?> re;

	/**
	 * @param writerManager The {@link org.cytoscape.io.write.PresentationWriterManager} used to determine which type of
	 * file should be written.
	 * @param view The View object to be written to the specified file.
	 * @param re The RenderingEngine used to generate the image to be written to the file.
	 */
	public ViewWriter(CyNetworkView view, RenderingEngine<?> re, CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar.getService(PresentationWriterManager.class),
				serviceRegistrar.getService(CyApplicationManager.class));

		if (view == null)
			throw new NullPointerException("CyNetworkView is null");
		if (re == null)
			throw new NullPointerException("RenderingEngine is null");
		
		this.view = view;
		this.re = re;

		// Pick PNG as a default file format
		for (String fileTypeDesc : this.getFileFilterDescriptions()) {
			if (fileTypeDesc.contains("PNG")) {
				options.setSelectedValue(fileTypeDesc);
				break;
			}
		}

		outputFile = getSuggestedFile();
	}

	@Override
	protected CyWriter getWriter(CyFileFilter filter) throws Exception {
		return writerManager.getWriter(view, re, filter, outputStream);
	}

	@Tunable(description = "Save Image as:", params = "fileCategory=image;input=false", dependsOn = "options!=", gravity = 1.1)
	@Override
	public File getOutputFile() {
		return outputFile;
	}
	
	@ProvidesTitle
	public String getTitle() {
		return "Export as Image";
	}

	@Override
	protected String getExportName() {
		String name = view.getVisualProperty(BasicVisualLexicon.NETWORK_TITLE);
		
		if (name == null || name.trim().isEmpty())
			name = view.getModel().getRow(view.getModel()).get(CyNetwork.NAME, String.class);
		
		return name;
	}
}
