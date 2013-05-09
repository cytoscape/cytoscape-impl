package org.cytoscape.task.internal.loadnetwork;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
import java.util.Collection;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.internal.NullCyNetworkViewFactory;
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
		LoadNetworkFileTaskFactoryImpl factory = new LoadNetworkFileTaskFactoryImpl(mgr, netmgr, networkViewManager, props, namingUtil, tunableSetter, vmm, nullNetworkViewFactory);

		TaskMonitor taskMonitor = mock(TaskMonitor.class);
		TaskObserver<Collection<CyNetworkView>> observer = mock(TaskObserver.class);
		TaskIterator iterator = factory.createTaskIterator(new File(""), observer);
		while (iterator.hasNext()) {
			iterator.next().run(taskMonitor);
		}
		
		verify(observer, times(1)).taskFinished(any(Collection.class));
	}
}
