package org.cytoscape.task.internal.export.network;


import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.AbstractCyWriterTest;
import org.cytoscape.io.write.CyNetworkViewWriterManager;
import org.cytoscape.test.support.NetworkViewTestSupport;
import org.cytoscape.view.model.CyNetworkView;
import org.junit.Before;
import org.junit.Test;

public class CyNetworkViewWriterTest extends AbstractCyWriterTest {
	
	private final NetworkViewTestSupport viewSupport = new NetworkViewTestSupport();
	
	private CyNetworkViewWriterManager writerManager;
	private CyNetworkView view;

	@Before
	public void setUp() throws Exception {
		writerManager = mock(CyNetworkViewWriterManager.class);
		view = viewSupport.getNetworkView();
		
		final List<CyFileFilter> filters = new ArrayList<CyFileFilter>();
		final CyFileFilter dummyFilter = mock(CyFileFilter.class);
		filters.add(dummyFilter);
		when(writerManager.getAvailableWriterFilters()).thenReturn(filters);
		
		
		
		final CyNetworkViewWriter writer = new CyNetworkViewWriter(writerManager, view);
		this.cyWriter = writer;
		
		when(writerManager.getWriter(eq(view), eq(dummyFilter), any(File.class))).thenReturn(writer, writer);
	}
	
	@Test
	@Override
	public void testGetWriter() throws Exception {
		// TODO: What should be tested?
	}
}
