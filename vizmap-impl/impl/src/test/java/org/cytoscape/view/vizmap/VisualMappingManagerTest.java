package org.cytoscape.view.vizmap;

/*
 * #%L
 * Cytoscape VizMap Impl (vizmap-impl)
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


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.vizmap.internal.VisualMappingManagerImpl;
import org.junit.After;
import org.junit.Before;

public class VisualMappingManagerTest extends AbstractVisualMappingManagerTest {
	
	@Before
	public void setUp() throws Exception {
		final VisualStyleFactory factory = mock(VisualStyleFactory.class);
		final VisualStyle dummyDefaultStyle = mock(VisualStyle.class);
		when(factory.createVisualStyle(VisualMappingManagerImpl.DEFAULT_STYLE_NAME)).thenReturn(dummyDefaultStyle);
		
		final CyEventHelper eventHelper = mock(CyEventHelper.class);
		final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
		
		vmm = new VisualMappingManagerImpl(factory, serviceRegistrar);
	}

	@After
	public void tearDown() throws Exception {
	}
}
