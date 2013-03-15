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



import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Test;


public class ZoomOutTaskFactoryTest {
	@Test
	public void testGetTaskIterator() {
		UndoSupport undoSupport = mock(UndoSupport.class);
                CyApplicationManager cyApplicationManagerServiceRef = mock(CyApplicationManager.class);
                CyNetworkView cnv = mock(CyNetworkView.class);
                when(cyApplicationManagerServiceRef.getCurrentNetworkView()).thenReturn(cnv);
		ZoomOutTaskFactory factory = new ZoomOutTaskFactory(undoSupport, cyApplicationManagerServiceRef);
		
                CyNetwork network = mock(CyNetwork.class);
		TaskIterator ti = factory.createTaskIterator(network);
		assertNotNull(ti);
		
		assertTrue( ti.hasNext() );
		Task t = ti.next();
		assertNotNull( t );		
	}
}
