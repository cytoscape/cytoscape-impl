package org.cytoscape.task.internal.export.graphics;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2013 The Cytoscape Consortium
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


import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.write.PresentationWriterManager;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.task.internal.export.ViewWriter;
import org.cytoscape.task.write.ExportNetworkImageTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.work.TaskIterator;


public class ExportNetworkImageTaskFactoryImpl extends AbstractNetworkViewTaskFactory implements ExportNetworkImageTaskFactory{
	
	private final PresentationWriterManager presentationWriterMgr; 
	private final CyApplicationManager applicationManager;
	private final RenderingEngineManager engineManager;

	public ExportNetworkImageTaskFactoryImpl(final PresentationWriterManager presentationWriterMgr,
			final CyApplicationManager applicationManager,
			final RenderingEngineManager engineManager) {
		this.presentationWriterMgr = presentationWriterMgr;
		this.applicationManager = applicationManager;
		this.engineManager = engineManager;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView view) {
		// Get the rendering engine
		RenderingEngine<?> engine = applicationManager.getCurrentRenderingEngine();

		// Now get the rendering engine for this view and use this one
		// if we can
		String engineId = view.getRendererId();
		for (RenderingEngine<?> e: engineManager.getRenderingEngines(view)) {
			if (engineId.equals(e.getRendererId())) {
				engine = e;
				break;
			}
		}

		return new TaskIterator(2, new ViewWriter( presentationWriterMgr, applicationManager,
				view, engine ) );
	}
}
