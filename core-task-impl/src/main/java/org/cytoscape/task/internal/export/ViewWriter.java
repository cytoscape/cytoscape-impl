package org.cytoscape.task.internal.export;

import java.io.File;
import java.util.Objects;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.PresentationWriterFactory;
import org.cytoscape.io.write.PresentationWriterManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListChangeListener;
import org.cytoscape.work.util.ListSelection;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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
	
	private static String defaultFormat = "PNG";
	
	public CyNetworkView view = null;
	@Tunable(description="Network View to export", 
	         longDescription=StringToModel.CY_NETWORK_VIEW_LONG_DESCRIPTION,
	         exampleStringValue=StringToModel.CY_NETWORK_VIEW_EXAMPLE_STRING,
	         context="nogui")
	public CyNetworkView getView() {
		return view;
	}
	public void setView(CyNetworkView view) {
		this.view = view;
		if (view != null) {
			// Get the rendering engine
			RenderingEngine<?> engine = serviceRegistrar.getService(CyApplicationManager.class).getCurrentRenderingEngine();

			// Now get the rendering engine for this view and use this one if we can
			String engineId = view.getRendererId();
			RenderingEngineManager engineManager = serviceRegistrar.getService(RenderingEngineManager.class);
	
			for (RenderingEngine<?> e : engineManager.getRenderingEngines(view)) {
				if (engineId.equals(e.getRendererId())) {
					engine = e;
					break;
				}
			}
			this.re = engine;
		}
	}

	private RenderingEngine<?> re;
	private final CyServiceRegistrar serviceRegistrar;

	public ViewWriter(CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar.getService(PresentationWriterManager.class), serviceRegistrar.getService(CyApplicationManager.class));
		this.serviceRegistrar = serviceRegistrar;

		initDefaultFormat();
	}

	/**
	 * @param writerManager The {@link org.cytoscape.io.write.PresentationWriterManager} used to determine which type of
	 * file should be written.
	 * @param view The View object to be written to the specified file.
	 * @param re The RenderingEngine used to generate the image to be written to the file.
	 */
	public ViewWriter(CyNetworkView view, RenderingEngine<?> re, CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar.getService(PresentationWriterManager.class), serviceRegistrar.getService(CyApplicationManager.class));
		this.serviceRegistrar = serviceRegistrar;

		this.view = Objects.requireNonNull(view, "CyNetworkView is null");
		this.re   = Objects.requireNonNull(re, "RenderingEngine is null");

		initDefaultFormat();

		outputFile = getSuggestedFile();
	}

	
	private void initDefaultFormat() {
		for(String fileTypeDesc : getFileFilterDescriptions()) {
			if (fileTypeDesc.contains(defaultFormat)) {
				options.setSelectedValue(fileTypeDesc);
				break;
			}
		}
		
		// If options.setSelectedValue(...) is passed a value that is already selected 
		// then it won't fire an event to set the writer field. CYTOSCAPE-13049
		if(writer == null) {
			try {
				writer = getWriter(getFileFilter(getExportFileFormat()));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		options.addListener(new ListChangeListener<>() {
			@Override public void selectionChanged(ListSelection<String> source) {
				defaultFormat = options.getSelectedValue();
			}
		});
	}
	
	@Override
	protected CyWriter getWriter(CyFileFilter filter) throws Exception {
		if (view == null) 
			return null;
		return writerManager.getWriter(view, re, filter, outputStream);
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
		if (outputFile == null) outputFile = getSuggestedFile();
		return outputFile;
	}

	@ProvidesTitle
	public String getTitle() {
		return "Export Network as Image";
	}

	@Override
	protected String getExportName() {
		String name = view.getVisualProperty(BasicVisualLexicon.NETWORK_TITLE);

		if (name == null || name.trim().isEmpty())
			name = view.getModel().getRow(view.getModel()).get(CyNetwork.NAME, String.class);

		return name;
	}
}
