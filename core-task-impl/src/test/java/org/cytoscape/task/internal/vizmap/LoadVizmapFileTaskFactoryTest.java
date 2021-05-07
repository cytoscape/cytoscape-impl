package org.cytoscape.task.internal.vizmap;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;

import org.cytoscape.io.read.VizmapReader;
import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.internal.sync.SyncTunableHandlerFactory;
import org.cytoscape.work.internal.sync.SyncTunableMutatorFactory;
import org.cytoscape.work.internal.sync.TunableRecorderManager;
import org.cytoscape.work.internal.sync.TunableSetterImpl;
import org.junit.Test;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class LoadVizmapFileTaskFactoryTest {
	
	@Test
	public void testObserver() throws Exception {
		VizmapReaderManager vizmapReaderMgr = mock(VizmapReaderManager.class);
		VizmapReader reader = mock(VizmapReader.class);
		when(vizmapReaderMgr.getReader(any(URI.class), any(String.class))).thenReturn(reader);
		
		VisualMappingManager vmMgr = mock(VisualMappingManager.class);
		SynchronousTaskManager<?> syncTaskManager = mock(SynchronousTaskManager.class);

		SyncTunableMutatorFactory mutatorFactory = new SyncTunableMutatorFactory(new SyncTunableHandlerFactory());
		TunableRecorderManager recorderManager = new TunableRecorderManager();
		TunableSetter tunableSetter = new TunableSetterImpl(mutatorFactory, recorderManager);
		
		CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(VizmapReaderManager.class)).thenReturn(vizmapReaderMgr);
		when(serviceRegistrar.getService(VisualMappingManager.class)).thenReturn(vmMgr);
		when(serviceRegistrar.getService(SynchronousTaskManager.class)).thenReturn(syncTaskManager);
		when(serviceRegistrar.getService(TunableSetter.class)).thenReturn(tunableSetter);
		
		LoadVizmapFileTaskFactory factory = new LoadVizmapFileTaskFactoryImpl(serviceRegistrar);
		File file = new File("");
		TaskObserver observer = mock(TaskObserver.class);
		TaskIterator iterator = factory.createTaskIterator(file, observer);
		TaskMonitor taskMonitor = mock(TaskMonitor.class);
		
		while (iterator.hasNext()) {
			Task t = iterator.next();
			t.run(taskMonitor);
			
			if (t instanceof ObservableTask)
				observer.taskFinished((ObservableTask)t);
		}
		
		verify(observer, times(1)).taskFinished(any(ObservableTask.class));
	}
}
