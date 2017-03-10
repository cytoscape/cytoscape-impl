package org.cytoscape.task.internal.export.network;

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
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.write.CyNetworkViewWriterManager;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.task.write.ExportNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;

public class ExportNetworkViewTaskFactoryImpl extends AbstractNetworkViewTaskFactory implements ExportNetworkViewTaskFactory {

	private CyNetworkViewWriterManager writerManager;
	private CyApplicationManager cyApplicationManager;
	private final TunableSetter tunableSetter;

	
	public ExportNetworkViewTaskFactoryImpl(CyNetworkViewWriterManager writerManager, CyApplicationManager cyApplicationManager,
			TunableSetter tunableSetter) {
		this.writerManager = writerManager;
		this.cyApplicationManager = cyApplicationManager;
		this.tunableSetter = tunableSetter;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView view) {
		return new TaskIterator(2,new CyNetworkViewWriter(writerManager, cyApplicationManager, view));
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView view, File file) {
		final Map<String, Object> m = new HashMap<String, Object>();
		m.put("OutputFile", file);

		CyNetworkViewWriter writer = new CyNetworkViewWriter(writerManager, cyApplicationManager, view);
		writer.setDefaultFileFormatUsingFileExt(file);
		return tunableSetter.createTaskIterator(new TaskIterator(2,writer), m); 
	}
}
