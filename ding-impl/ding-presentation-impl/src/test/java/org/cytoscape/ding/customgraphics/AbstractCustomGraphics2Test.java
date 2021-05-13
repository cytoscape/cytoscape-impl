package org.cytoscape.ding.customgraphics;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.ding.internal.charts.DummyCyColumnIdentifierFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;
import org.junit.Test;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

public class AbstractCustomGraphics2Test {

	protected final Map<String, Object> props1 = new HashMap<>();
	
	protected CyServiceRegistrar serviceRegistrar;
	
	protected CyColumnIdentifierFactory colIdFactory = new DummyCyColumnIdentifierFactory();
	
	protected void setUp() throws Exception {
		props1.clear();
		serviceRegistrar = mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(CyColumnIdentifierFactory.class)).thenReturn(colIdFactory);
	}
	
	@Test
	public void testPropertyNamesPrefix() {
		for (String name : props1.keySet())
			assertTrue(name.startsWith("cy_"));
	}
}
