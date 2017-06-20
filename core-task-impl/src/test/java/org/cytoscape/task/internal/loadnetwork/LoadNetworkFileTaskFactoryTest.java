package org.cytoscape.task.internal.loadnetwork;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;

import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.junit.Before;
import org.junit.Test;

public class LoadNetworkFileTaskFactoryTest extends AbstractLoadNetworkTaskTester {
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
	}
	
	@Test
	public void testObserver() throws Exception {
		when(netReaderManager.getReader(any(URI.class), anyString())).thenReturn(reader);

		LoadNetworkFileTaskFactoryImpl factory = new LoadNetworkFileTaskFactoryImpl(serviceRegistrar);

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
