package org.cytoscape.task.internal.zoom;


import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_SCALE_FACTOR;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.cytoscape.view.model.CyNetworkView;
import org.junit.Test;


public class ZoomEditTest {
	@Test
	public void runTest() {
		final CyNetworkView view = mock(CyNetworkView.class);
		when(view.getVisualProperty(NETWORK_SCALE_FACTOR)).thenReturn(2.0);

		final ZoomEdit zoomEdit = new ZoomEdit(view, 0.5);
		zoomEdit.undo();
		verify(view, times(1)).setVisualProperty(NETWORK_SCALE_FACTOR, Double.valueOf(4.0));
		when(view.getVisualProperty(NETWORK_SCALE_FACTOR)).thenReturn(4.0);
		zoomEdit.redo();
		verify(view, times(1)).setVisualProperty(NETWORK_SCALE_FACTOR, Double.valueOf(2.0));
	}
}
