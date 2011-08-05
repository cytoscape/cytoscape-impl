package org.cytoscape.task.internal.zoom;


import static org.cytoscape.view.presentation.property.MinimalVisualLexicon.NETWORK_CENTER_X_LOCATION;
import static org.cytoscape.view.presentation.property.MinimalVisualLexicon.NETWORK_CENTER_Y_LOCATION;
import static org.cytoscape.view.presentation.property.MinimalVisualLexicon.NETWORK_SCALE_FACTOR;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.cytoscape.view.model.CyNetworkView;

import org.junit.Test;


public class FitContentEditTest {
	@Test
	public void runTest() {
		final CyNetworkView view = mock(CyNetworkView.class);
		when(view.getVisualProperty(NETWORK_SCALE_FACTOR)).thenReturn(2.0);
		when(view.getVisualProperty(NETWORK_CENTER_X_LOCATION)).thenReturn(100.0);
		when(view.getVisualProperty(NETWORK_CENTER_Y_LOCATION)).thenReturn(400.0);

		final FitContentEdit zoomEdit = new FitContentEdit("XYZ", view);
		when(view.getVisualProperty(NETWORK_SCALE_FACTOR)).thenReturn(1.0);
		when(view.getVisualProperty(NETWORK_CENTER_X_LOCATION)).thenReturn(200.0);
		when(view.getVisualProperty(NETWORK_CENTER_Y_LOCATION)).thenReturn(300.0);
		zoomEdit.undo();
		verify(view, times(1)).setVisualProperty(NETWORK_SCALE_FACTOR, Double.valueOf(2.0));
		verify(view, times(1)).setVisualProperty(NETWORK_CENTER_X_LOCATION, Double.valueOf(100.0));
		verify(view, times(1)).setVisualProperty(NETWORK_CENTER_Y_LOCATION, Double.valueOf(400.0));
		zoomEdit.redo();
		verify(view, times(1)).fitContent();
	}
}
