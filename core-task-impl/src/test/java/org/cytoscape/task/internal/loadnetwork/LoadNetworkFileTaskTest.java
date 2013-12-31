package org.cytoscape.task.internal.loadnetwork;

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

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableSetter;
import org.junit.Before;
import org.junit.Test;

public class LoadNetworkFileTaskTest extends AbstractLoadNetworkTaskTester {

	File file;
	TunableSetter ts = mock(TunableSetter.class);


	@Before
	public void setUp() throws Exception {
		file = new File("/tmp/asdfasdf");
		uri = file.toURI();
		super.setUp();
	}

	@Test
	public void testRun() throws Exception {
		CyNetworkViewFactory nullNetworkViewFactory = mock(CyNetworkViewFactory.class);
		TaskFactory factory = new LoadNetworkFileTaskFactoryImpl(mgr, netmgr, networkViewManager, props, namingUtil, ts,
				vmm, nullNetworkViewFactory);
		TaskIterator ti = factory.createTaskIterator();
		TaskMonitor tm = mock(TaskMonitor.class);
		boolean first = true;
		while (ti.hasNext()) {
			Task t = ti.next();
			if (first) {
				((LoadNetworkFileTask) t).file = file;
				first = false;
			}
			t.run(tm);
		}
		verify(netmgr).addNetwork(net);
		// verify(networkViewManager).addNetworkView(view);
		verify(tm,atLeast(1)).setProgress(1.0);
	}

	@Test(expected = NullPointerException.class)
	public void testNullFile() throws Exception {
		CyNetworkViewFactory nullNetworkViewFactory = mock(CyNetworkViewFactory.class);
		TaskFactory factory = new LoadNetworkFileTaskFactoryImpl(mgr, netmgr, networkViewManager, props, namingUtil, ts,
				vmm, nullNetworkViewFactory);
		TaskIterator ti = factory.createTaskIterator();
		TaskMonitor tm = mock(TaskMonitor.class);
		boolean first = true;
		while (ti.hasNext()) {
			Task t = ti.next();
			if (first) {
				((LoadNetworkFileTask) t).file = null;
				first = false;
			}
			t.run(tm);
		}
	}
}
