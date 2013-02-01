package org.cytoscape.cpath2.internal.task;

/*
 * #%L
 * Cytoscape CPath2 Impl (cpath2-impl)
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

import org.cytoscape.cpath2.internal.CPath2Factory;
import org.cytoscape.cpath2.internal.web_service.CPathResponseFormat;
import org.cytoscape.cpath2.internal.web_service.CPathWebService;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ExecuteGetRecordByCPathIdTaskFactory extends AbstractTaskFactory {

	private final CPathWebService webApi;
	private final long[] ids;
	private final CPathResponseFormat format;
	private final String networkTitle;
	private final CyNetwork networkToMerge;
	private final CPath2Factory cPathFactory;
	private final VisualMappingManager mappingManager;

	public ExecuteGetRecordByCPathIdTaskFactory(CPathWebService webApi,
			long[] ids, CPathResponseFormat format, String networkTitle,
			CyNetwork networkToMerge, CPath2Factory cPathFactory, VisualMappingManager mappingManager) {
		this.webApi = webApi;
		this.ids = ids;
		this.format = format;
		this.networkTitle = networkTitle;
		this.networkToMerge = networkToMerge;
		this.cPathFactory = cPathFactory;
		this.mappingManager = mappingManager;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ExecuteGetRecordByCPathId(webApi, ids, format, networkTitle, networkToMerge, cPathFactory, mappingManager));
	}

}
