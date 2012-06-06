/*
  Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package org.cytoscape.task.internal.loadnetwork;

import static org.mockito.Mockito.*;

import java.io.File;

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
		TaskFactory factory = new LoadNetworkFileTaskFactoryImpl(mgr, netmgr, networkViewManager, props, namingUtil, ts, vmm);
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
		TaskFactory factory = new LoadNetworkFileTaskFactoryImpl(mgr, netmgr, networkViewManager, props, namingUtil, ts, vmm);
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
