package org.cytoscape.task.internal.io;


import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.PresentationWriterManager;
import org.cytoscape.task.internal.export.AbstractCyWriterTest;
import org.cytoscape.task.internal.export.ViewWriter;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.RenderingEngine;
import org.junit.Before;
import org.mockito.ArgumentMatcher;


public class ViewWriterTest extends AbstractCyWriterTest {
	@Before
	public void setUp() throws Exception {
		final PresentationWriterManager pwm = mock(PresentationWriterManager.class);
		final View<?> view = mock(View.class);
		final RenderingEngine re = mock(RenderingEngine.class);
		fileFilter = mock(CyFileFilter.class);
		when(fileFilter.getDescription()).thenReturn("A dummy filter");
		final List<CyFileFilter> filters = new ArrayList<CyFileFilter>();
		filters.add(fileFilter);
		when(pwm.getAvailableWriterFilters()).thenReturn(filters);
		cyWriter = new ViewWriter(pwm, view, re);
		final CyWriter aWriter = mock(CyWriter.class);
		when(pwm.getWriter(eq(view),eq(re),eq(fileFilter),argThat(new AnyFileMatcher()))).thenReturn(aWriter);
	}

	static class AnyFileMatcher extends ArgumentMatcher<File> {
		public boolean matches(final Object o) {
			return true;
		}
	}
}