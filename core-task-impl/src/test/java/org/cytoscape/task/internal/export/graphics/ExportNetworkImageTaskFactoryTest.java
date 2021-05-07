package org.cytoscape.task.internal.export.graphics;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.PresentationWriterManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.export.ViewWriter;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

public class ExportNetworkImageTaskFactoryTest {
	
	private final PresentationWriterManager viewWriterMgr = mock(PresentationWriterManager.class);
	private final CyApplicationManager applicationManager = mock(CyApplicationManager.class);
	private final RenderingEngineManager engineManager = mock(RenderingEngineManager.class);
	private final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);

	@Before
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setUp() throws Exception {
		final CyFileFilter dummyFilter = mock(CyFileFilter.class);
		final RenderingEngine engine = mock(RenderingEngine.class);
		
		final List<CyFileFilter> filters = new ArrayList<>();
		filters.add(dummyFilter);
		
		when(dummyFilter.getDescription()).thenReturn("dummy description");
		when(dummyFilter.getExtensions()).thenReturn(Collections.singleton("dummy"));
		when(viewWriterMgr.getAvailableWriterFilters()).thenReturn(filters);
		when(applicationManager.getCurrentRenderingEngine()).thenReturn(engine);
		when(serviceRegistrar.getService(CyApplicationManager.class)).thenReturn(applicationManager);
		when(serviceRegistrar.getService(RenderingEngineManager.class)).thenReturn(engineManager);
		when(serviceRegistrar.getService(PresentationWriterManager.class)).thenReturn(viewWriterMgr);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testExportNetworkImageTaskFactory() throws Exception {
		final ExportNetworkImageTaskFactoryImpl factory = new ExportNetworkImageTaskFactoryImpl(serviceRegistrar);
		final CyNetworkView view = mock(CyNetworkView.class);
		final CyNetwork network = mock(CyNetwork.class);
		final CyRow row = mock(CyRow.class);
		when(view.getModel()).thenReturn(network);
		when(network.getRow(network)).thenReturn(row);
		
		final TaskIterator itr = factory.createTaskIterator(view);
		
		assertNotNull(itr);
		assertTrue(itr.hasNext());
		final Task task = itr.next();
		assertTrue(task instanceof ViewWriter);
	}
}
