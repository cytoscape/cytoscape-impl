package org.cytoscape.task.internal.loadnetwork;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;

import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.internal.NullCyNetworkViewFactory;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.internal.sync.SyncTunableHandlerFactory;
import org.cytoscape.work.internal.sync.SyncTunableMutatorFactory;
import org.cytoscape.work.internal.sync.TunableRecorderManager;
import org.cytoscape.work.internal.sync.TunableSetterImpl;
import org.junit.Before;
import org.junit.Test;

public class LoadNetworkFileTaskFactoryTest extends AbstractLoadNetworkTaskTester {
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
	}
	
	@Test
	public void testObserver() throws Exception {
		when(mgr.getReader(any(URI.class), anyString())).thenReturn(reader);

		TunableSetter tunableSetter = new TunableSetterImpl(new SyncTunableMutatorFactory(new SyncTunableHandlerFactory()), new TunableRecorderManager());
		CyNetworkViewFactory nullNetworkViewFactory = new NullCyNetworkViewFactory();
		LoadNetworkFileTaskFactoryImpl factory = new LoadNetworkFileTaskFactoryImpl(mgr, netmgr, networkViewManager, 
				props, namingUtil, vmm, nullNetworkViewFactory, serviceRegistrar);

		TaskMonitor taskMonitor = mock(TaskMonitor.class);
		TaskObserver observer = mock(TaskObserver.class);
		TaskIterator iterator = factory.createTaskIterator(new File(""), observer);
		while (iterator.hasNext()) {
			Task t = iterator.next();
			t.run(taskMonitor);
			if (t instanceof ObservableTask)
				observer.taskFinished((ObservableTask)t);
		}
		
		verify(observer, times(1)).taskFinished(any(ObservableTask.class));
	}
}
