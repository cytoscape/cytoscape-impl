package org.cytoscape.task.internal.zoom;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_CENTER_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_SCALE_FACTOR;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

public class FitSelectedTaskTest {
	
	@Mock private CyNetworkView view;
	@Mock private CyApplicationManager appManager;
	@Mock private UndoSupport undoSupport;
	@Mock private CyServiceRegistrar serviceRegistrar;
	@Mock private TaskMonitor tm;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
        when(appManager.getCurrentNetworkView()).thenReturn(view);
		
		when(serviceRegistrar.getService(CyApplicationManager.class)).thenReturn(appManager);
		when(serviceRegistrar.getService(UndoSupport.class)).thenReturn(undoSupport);
	}
	
	@Test
	public void testRun() throws Exception {
		when(view.getVisualProperty(NETWORK_CENTER_X_LOCATION)).thenReturn(0.0);
		when(view.getVisualProperty(NETWORK_CENTER_Y_LOCATION)).thenReturn(0.0);
		when(view.getVisualProperty(NETWORK_SCALE_FACTOR)).thenReturn(0.0);

		FitSelectedTask t = new FitSelectedTask(view, serviceRegistrar);
		t.run(tm);

		verify(view, times(1)).fitSelected();
	}

	@Test(expected = Exception.class)
	public void testNullView() throws Exception {
		FitSelectedTask t = new FitSelectedTask(null, serviceRegistrar);
		t.run(tm);
	}
}
