package org.cytoscape.task.internal.export.graphics;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.PresentationWriterManager;
import org.cytoscape.session.CyApplicationManager;
import org.cytoscape.task.internal.export.ViewWriter;
import org.cytoscape.task.internal.export.graphics.ExportNetworkImageTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ExportNetworkImageTaskFactoryTest {
	
	private final PresentationWriterManager viewWriterMgr = mock(PresentationWriterManager.class);
	private final CyApplicationManager applicationManager = mock(CyApplicationManager.class);

	@Before
	public void setUp() throws Exception {
		final List<CyFileFilter> filters = new ArrayList<CyFileFilter>();
		final CyFileFilter dummyFilter = mock(CyFileFilter.class);
		filters.add(dummyFilter);
		when(viewWriterMgr.getAvailableWriterFilters()).thenReturn(filters);
		final RenderingEngine engine = mock(RenderingEngine.class);
		when(applicationManager.getCurrentRenderingEngine()).thenReturn(engine);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testExportNetworkImageTaskFactory() throws Exception {
		final ExportNetworkImageTaskFactory factory = new ExportNetworkImageTaskFactory(viewWriterMgr, applicationManager);
		final CyNetworkView view = mock(CyNetworkView.class);
		factory.setNetworkView(view);
		
		final TaskIterator itr = factory.getTaskIterator();
		
		assertNotNull(itr);
		assertTrue(itr.hasNext());
		final Task task = itr.next();
		assertTrue(task instanceof ViewWriter);
	}

}
