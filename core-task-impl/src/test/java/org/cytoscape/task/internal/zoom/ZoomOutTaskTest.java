package org.cytoscape.task.internal.zoom;


import javax.swing.undo.UndoableEditSupport;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Test;
import static org.cytoscape.view.presentation.property.MinimalVisualLexicon.NETWORK_SCALE_FACTOR;
import static org.mockito.Mockito.*;


public class ZoomOutTaskTest {
	@Test
	public void testRun() throws Exception {
		CyNetworkView view = mock(CyNetworkView.class);
		TaskMonitor tm = mock(TaskMonitor.class);

		double curScaleFactor = 2.0;

		when(view.getVisualProperty(NETWORK_SCALE_FACTOR)).thenReturn(curScaleFactor);

		UndoableEditSupport undoableEditSupport = mock(UndoableEditSupport.class);
		UndoSupport undoSupport = mock(UndoSupport.class);
		when(undoSupport.getUndoableEditSupport()).thenReturn(undoableEditSupport);
				
		ZoomOutTask t = new ZoomOutTask(undoSupport, view);

		t.run(tm);

		verify(view, times(1)).setVisualProperty(NETWORK_SCALE_FACTOR,curScaleFactor*0.9);
	}
}
