package org.cytoscape.task.internal.loaddatatable;

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

import java.io.File;

import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableSetter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LoadAttributesFileTaskFactoryImplTest {

	@Mock
	CyTableReaderManager rmgr;
	
	@Mock
	TaskMonitor tm;
	@Mock
	TunableSetter ts;

	@Mock
	CyNetworkManager netMgr;
	
	@Mock
	CyTableManager tabMgr;
	
	@Mock
	CyRootNetworkManager rootNetMgr;
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		
	}

	@Test(expected = NullPointerException.class)
	public void testLoadAttributesFileTaskFactory() throws Exception {

		final LoadTableFileTaskFactoryImpl factory = new LoadTableFileTaskFactoryImpl(rmgr, ts, netMgr, tabMgr, rootNetMgr);
		TaskIterator ti = factory.createTaskIterator();
		assertNotNull(ti);

		assertTrue(ti.hasNext());
		Task t = ti.next();
		assertNotNull(t);

		((LoadTableFileTask) t).file = new File("./src/test/resources/empty.txt");
		
		// This throws NPE, but it;s normal.
		t.run(tm);
	}
}
