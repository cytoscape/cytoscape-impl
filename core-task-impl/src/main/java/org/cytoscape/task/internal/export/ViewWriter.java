package org.cytoscape.task.internal.export;

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


import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Tunable;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.PresentationWriterManager;
import org.cytoscape.io.write.PresentationWriterFactory;

import java.io.File;


/**
 * A utility Task implementation that will write the specified View to the
 * the specified image file using the specified RenderingEngine.
 */
public final class ViewWriter extends TunableAbstractCyWriter<PresentationWriterFactory,PresentationWriterManager> {
	private final View<?> view;
	private final RenderingEngine<?> re;

	/**
	 * @param writerManager The {@link org.cytoscape.io.write.PresentationWriterManager} used to determine which type of
	 * file should be written.
	 * @param view The View object to be written to the specified file.
	 * @param re The RenderingEngine used to generate the image to be written to the file.
	 */
	public ViewWriter(final PresentationWriterManager writerManager, final View<?> view, final RenderingEngine<?> re ) {
		super(writerManager);

		if ( view == null )
			throw new NullPointerException("view is null");
		this.view = view;

		if ( re == null )
			throw new NullPointerException("rendering engine is null");
		this.re = re;
		
		// Pick PNG as a default file format
		for(String fileTypeDesc: this.getFileFilterDescriptions()) {
			if(fileTypeDesc.contains("PNG")) {
				this.options.setSelectedValue(fileTypeDesc);
				break;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected CyWriter getWriter(CyFileFilter filter, File file) throws Exception {
		if (!fileExtensionIsOk(file))
			file = addOrReplaceExtension(outputFile);
		return writerManager.getWriter(view,re,filter,file);
	}

	@Tunable(description="Save Image as:", params="fileCategory=image;input=false", dependsOn="options!=")
	public File getOutputFile() {
		return outputFile;
	}
	
	@ProvidesTitle
	public String getTitle() {
		return "Export Network as Graphics";
	}
}
