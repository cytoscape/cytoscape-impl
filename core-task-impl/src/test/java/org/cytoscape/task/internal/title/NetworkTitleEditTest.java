package org.cytoscape.task.internal.title;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;

import org.junit.Test;


public class NetworkTitleEditTest {
	@Test
	public void runTest() {
		final CyNetwork network = mock(CyNetwork.class);
		final CyRow row =  mock(CyRow.class);
		when(network.getCyRow()).thenReturn(row);
		when(row.get("name", String.class)).thenReturn("newTitle");

		final NetworkTitleEdit titleEdit = new NetworkTitleEdit(network, "oldTitle");
		titleEdit.undo();
		verify(row, times(1)).set("name", "oldTitle");
		titleEdit.redo();
		verify(row, times(1)).set("name", "newTitle");
	}
}
