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


import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterManager;
import org.cytoscape.task.internal.export.AbstractCyWriterTest;
import org.cytoscape.view.model.CyNetworkView;
import org.junit.Before;
import org.junit.Test;

public class CyNetworkViewWriterTest extends AbstractCyWriterTest {
	
	private final NetworkViewTestSupport viewSupport = new NetworkViewTestSupport();
	
	private CyNetworkViewWriterManager writerManager;
	private CyNetworkView view;

	@Before
	public void setUp() throws Exception {
		writerManager = mock(CyNetworkViewWriterManager.class);
		view = viewSupport.getNetworkView();
		
		final List<CyFileFilter> filters = new ArrayList<>();
		final CyFileFilter dummyFilter = mock(CyFileFilter.class);
		filters.add(dummyFilter);
		when(dummyFilter.getDescription()).thenReturn("dummy description");
		when(writerManager.getAvailableWriterFilters()).thenReturn(filters);

		final CyNetworkViewWriter writer = new CyNetworkViewWriter(writerManager, view);
		this.cyWriter = writer;
		
		when(writerManager.getWriter(eq(view), eq(dummyFilter), any(File.class))).thenReturn(writer, writer);
	}
	
	@Test
	@Override
	public void testGetWriter() throws Exception {
		// TODO: What should be tested?
	}
}
