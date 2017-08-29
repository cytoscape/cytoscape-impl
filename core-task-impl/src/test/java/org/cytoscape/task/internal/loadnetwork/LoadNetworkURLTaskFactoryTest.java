package org.cytoscape.task.internal.loadnetwork;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.net.URLConnection;

import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.task.internal.export.network.LoadNetworkURLTaskFactoryImpl;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.junit.Before;
import org.junit.Test;

public class LoadNetworkURLTaskFactoryTest extends AbstractLoadNetworkTaskTester {
	
	private URL url;

	@Before
	public void setUp() throws Exception {
		url = new URL("http://example.com");
		uri = url.toURI();
		super.setUp();
	}
	
	@Test
	public void testObserver() throws Exception {
		URLConnection con = mock(URLConnection.class);
		StreamUtil streamUtil = mock(StreamUtil.class);
		when(streamUtil.getURLConnection(url)).thenReturn(con);

		LoadNetworkURLTaskFactoryImpl factory = new LoadNetworkURLTaskFactoryImpl(serviceRegistrar);

		TaskMonitor taskMonitor = mock(TaskMonitor.class);
		TaskObserver observer = mock(TaskObserver.class);
		TaskIterator iterator = factory.createTaskIterator(url, observer);
		
		while (iterator.hasNext()) {
			Task t = iterator.next();
			t.run(taskMonitor);
			if (t instanceof ObservableTask)
				observer.taskFinished((ObservableTask)t);
		}
		
		// This is sort of a stupid verification.  We should actually be testing the results, here....
		verify(observer, times(1)).taskFinished(any(ObservableTask.class));
	}
}
