/*
  Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;
import org.junit.Test;

public class LoadNetworkURLTaskTest extends AbstractLoadNetworkTaskTester {

	URL url;

	@Before
	public void setUp() throws Exception {
		url = new URL("http://example.com");
		uri = url.toURI();
		super.setUp();
	}

	@Test
	public void testRun() throws Exception {
		URLConnection con = mock(URLConnection.class);
		StreamUtil streamUtil = mock(StreamUtil.class);
		when(streamUtil.getURLConnection(url)).thenReturn(con);

		TaskFactory factory = new LoadNetworkURLTaskFactoryImpl(mgr, netmgr, networkViewManager, props, namingUtil,
				streamUtil, synchronousTaskManager);
		assertNotNull(networkViewManager);
		TaskIterator ti = factory.createTaskIterator();
		TaskMonitor tm = mock(TaskMonitor.class);
		boolean first = true;
		while (ti.hasNext()) {
			Task t = ti.next();
			if (first) {
				((LoadNetworkURLTask) t).url = url;
				first = false;
			}
			t.run(tm);
		}
		verify(netmgr).addNetwork(net);
		// verify(networkViewManager).addNetworkView(view);
		verify(tm).setProgress(1.0);
	}

	@Test(expected = Exception.class)
	public void testBadConnection() throws Exception {
		URLConnection con = mock(URLConnection.class);
		doThrow(new IOException("bad connection")).when(con).connect();

		StreamUtil streamUtil = mock(StreamUtil.class);
		when(streamUtil.getURLConnection(url)).thenReturn(con);

		TaskFactory factory = new LoadNetworkURLTaskFactoryImpl(mgr, netmgr, networkViewManager, props, namingUtil,
				streamUtil, synchronousTaskManager);
		TaskIterator ti = factory.createTaskIterator();
		TaskMonitor tm = mock(TaskMonitor.class);
		boolean first = true;
		while (ti.hasNext()) {
			Task t = ti.next();
			if (first) {
				((LoadNetworkURLTask) t).url = url;
				first = false;
			}
			t.run(tm);
		}
	}

	@Test(expected = NullPointerException.class)
	public void testNullURL() throws Exception {
		URLConnection con = mock(URLConnection.class);
		StreamUtil streamUtil = mock(StreamUtil.class);
		when(streamUtil.getURLConnection(url)).thenReturn(con);

		TaskFactory factory = new LoadNetworkURLTaskFactoryImpl(mgr, netmgr, networkViewManager, props, namingUtil,
				streamUtil, synchronousTaskManager);
		TaskIterator ti = factory.createTaskIterator();
		TaskMonitor tm = mock(TaskMonitor.class);
		boolean first = true;
		while (ti.hasNext()) {
			Task t = ti.next();
			if (first) {
				((LoadNetworkURLTask) t).url = null;
				first = false;
			}
			t.run(tm);
		}
	}
}
