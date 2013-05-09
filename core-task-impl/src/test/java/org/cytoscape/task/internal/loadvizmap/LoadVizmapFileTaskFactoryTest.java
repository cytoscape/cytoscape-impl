package org.cytoscape.task.internal.loadvizmap;

import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.Set;

import org.cytoscape.io.read.VizmapReader;
import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.internal.sync.SyncTunableHandlerFactory;
import org.cytoscape.work.internal.sync.SyncTunableMutatorFactory;
import org.cytoscape.work.internal.sync.TunableRecorderManager;
import org.cytoscape.work.internal.sync.TunableSetterImpl;
import org.junit.Test;

import static org.mockito.Mockito.*;

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
		
		LoadVizmapFileTaskFactory factory = new LoadVizmapFileTaskFactoryImpl(vizmapReaderMgr, vmMgr, syncTaskManager, tunableSetter);
		File file = new File("");
		TaskObserver<Set<VisualStyle>> observer = mock(TaskObserver.class);
		TaskIterator iterator = factory.createTaskIterator(file, observer);
		TaskMonitor taskMonitor = mock(TaskMonitor.class);
		while (iterator.hasNext()) {
			iterator.next().run(taskMonitor);
		}
		verify(observer, times(1)).taskFinished(any(Set.class));
	}
}
