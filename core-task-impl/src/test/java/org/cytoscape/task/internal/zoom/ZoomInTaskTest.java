package org.cytoscape.task.internal.zoom;

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



import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_SCALE_FACTOR;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Test;


public class ZoomInTaskTest {
	@Test
	public void testRun() throws Exception {
		CyNetworkView view = mock(CyNetworkView.class);
		TaskMonitor tm = mock(TaskMonitor.class);

		double curScaleFactor = 2.0;
		
		when(view.getVisualProperty(NETWORK_SCALE_FACTOR)).thenReturn(curScaleFactor);
		
		UndoSupport undoSupport = mock(UndoSupport.class);
				
		ZoomInTask t = new ZoomInTask(undoSupport, view);
		
		t.run(tm);

		verify(view, times(1)).setVisualProperty(NETWORK_SCALE_FACTOR,curScaleFactor*1.1);
	}
}
