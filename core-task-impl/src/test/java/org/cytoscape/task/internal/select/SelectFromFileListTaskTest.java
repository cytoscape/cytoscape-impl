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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;


import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Before;
import org.junit.Test;


public class SelectFromFileListTaskTest extends AbstractSelectTaskTester {
	CyEventHelper eventHelper;
	UndoSupport undoSupport;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		this.eventHelper = mock(CyEventHelper.class);
		final CyTable nodeTable = mock(CyTable.class);
		when(net.getDefaultNodeTable()).thenReturn(nodeTable);
		this.undoSupport = mock(UndoSupport.class);
	}

	@Test
	public void testRun() throws Exception {
		when(r3.get("name",String.class)).thenReturn("homer");
		when(r4.get("name",String.class)).thenReturn("marge");

		// run the task
		SelectFromFileListTask t =
			new SelectFromFileListTask(undoSupport, net, networkViewManager,
			                           eventHelper);
		t.file = new File("./src/test/resources/node_names.txt");
		t.run(tm);

		// check that the expected rows were set
		verify(r3, times(1)).set("selected",true);
		verify(r4, times(1)).set("selected",true);
	}

	@Test(expected=NullPointerException.class)
	public void testRunWithoutFileSpecified() throws Exception {
		// run the task
		SelectFromFileListTask t =
			new SelectFromFileListTask(undoSupport, net, networkViewManager, eventHelper);
		t.run(tm);
	}

	@Test(expected=Exception.class)
	public void testNonExistentFile() throws Exception {
		// run the task
		SelectFromFileListTask t =
			new SelectFromFileListTask(undoSupport, net, networkViewManager, eventHelper);
		t.file = new File("./src/test/resources/does-not-exist.txt");
		t.run(tm);
	}

	@Test
	public void testRunEmptyFile() throws Exception {
		// run the task
		SelectFromFileListTask t =
			new SelectFromFileListTask(undoSupport, net, networkViewManager, eventHelper);
		t.file = new File("./src/test/resources/empty.txt");
		t.run(tm);

		// check that the expected rows were set
		verify(r3, never()).set("selected",true);
		verify(r4, never()).set("selected",true);
	}

	@Test
	public void testRunNamesDontMatch() throws Exception {
		// run the task
		SelectFromFileListTask t =
			new SelectFromFileListTask(undoSupport, net, networkViewManager, eventHelper);
		t.file = new File("./src/test/resources/bad_names.txt");
		t.run(tm);

		// check that the expected rows were set
		verify(r3, never()).set("selected",true);
		verify(r4, never()).set("selected",true);
	}
}
