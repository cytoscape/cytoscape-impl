package org.cytoscape.task.internal.select;

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


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.Task;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Before;
import org.junit.Test;


public class InvertSelectedEdgesTaskTest extends AbstractSelectTaskTester {
	CyEventHelper eventHelper;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		this.eventHelper = mock(CyEventHelper.class);
	}

	@Test
	public void testRun() throws Exception {
		final CyTable edgeTable = mock(CyTable.class);
		when(net.getDefaultEdgeTable()).thenReturn(edgeTable);
		UndoSupport undoSupport = mock(UndoSupport.class);

		// more setup
		when(r1.get("selected",Boolean.class)).thenReturn(false);	
		when(r2.get("selected",Boolean.class)).thenReturn(true);	

		// run the task
		Task t = new InvertSelectedEdgesTask(undoSupport, net, networkViewManager, eventHelper);
		t.run(tm);

		// check that the expected rows were set
		verify(r1, times(1)).set("selected",true);
		verify(r2, times(1)).set("selected",false);
	}
}
