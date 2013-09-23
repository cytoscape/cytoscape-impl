package org.cytoscape.task.internal.session;

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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.io.read.CySessionReader;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySession.Builder;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.task.internal.session.OpenSessionTask.OpenSessionWithoutWarningTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class OpenSessionTaskTest {
	
	@Mock private TaskMonitor tm;
	@Mock private CySessionManager mgr;
	@Mock private CySessionReaderManager readerMgr;
	@Mock private CyApplicationManager appMgr;
	@Mock private CyNetworkManager netMgr;
	@Mock private CyTableManager tableMgr;
	@Mock private CyNetworkTableManager netTableMgr;
	@Mock private CyGroupManager grMgr;
	@Mock private RecentlyOpenedTracker tracker;
	
	@Mock private CySessionReader reader;
	private CySession session;
	
	private File sampleFile;
	
	@Before
	@SuppressWarnings("unchecked")
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		
		when(netTableMgr.getNetworkSet()).thenReturn(Collections.EMPTY_SET);
		
		sampleFile = new File("./src/test/resources/test_session1.cys");
		when(readerMgr.getReader(sampleFile.toURI(),sampleFile.getName())).thenReturn(reader);
		
		session = new Builder().build();
		when(reader.getSession()).thenReturn(session);
	}
	
	@Test
	public void testRun() throws Exception {
		final OpenSessionTask t = new OpenSessionTask(mgr, readerMgr, appMgr, netMgr, tableMgr, netTableMgr, grMgr, tracker);
		OpenSessionWithoutWarningTask t2 = t.new OpenSessionWithoutWarningTask();
		t2.file = sampleFile;
		t2.setTaskIterator(new TaskIterator(t2));

		t2.run(tm);
		verify(reader, times(1)).run(tm);
	}
}
