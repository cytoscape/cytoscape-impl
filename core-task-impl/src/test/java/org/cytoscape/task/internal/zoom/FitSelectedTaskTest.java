package org.cytoscape.task.internal.zoom;


import static org.cytoscape.view.presentation.property.MinimalVisualLexicon.NETWORK_CENTER_X_LOCATION;
import static org.cytoscape.view.presentation.property.MinimalVisualLexicon.NETWORK_CENTER_Y_LOCATION;
import static org.cytoscape.view.presentation.property.MinimalVisualLexicon.NETWORK_SCALE_FACTOR;

import javax.swing.undo.UndoableEditSupport;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

import org.junit.Test;

import static org.mockito.Mockito.*;


public class FitSelectedTaskTest {
	@Test
	public void testRun() throws Exception {
		CyNetworkView view = mock(CyNetworkView.class);
		when(view.getVisualProperty(NETWORK_CENTER_X_LOCATION)).thenReturn(0.0);
		when(view.getVisualProperty(NETWORK_CENTER_Y_LOCATION)).thenReturn(0.0);
		when(view.getVisualProperty(NETWORK_SCALE_FACTOR)).thenReturn(0.0);

		TaskMonitor tm = mock(TaskMonitor.class);

		UndoableEditSupport undoableEditSupport = mock(UndoableEditSupport.class);
		UndoSupport undoSupport = mock(UndoSupport.class);
		when(undoSupport.getUndoableEditSupport()).thenReturn(undoableEditSupport);

		FitSelectedTask t = new FitSelectedTask(undoSupport, view);

		t.run(tm);

		verify(view, times(1)).fitSelected();
	}

	@Test(expected=Exception.class)
	public void testNullView() throws Exception {
		UndoSupport undoSupport = mock(UndoSupport.class);
		CyNetworkView view = null;

		TaskMonitor tm = mock(TaskMonitor.class);

		FitSelectedTask t = new FitSelectedTask(undoSupport, view);

		t.run(tm);
	}

}
