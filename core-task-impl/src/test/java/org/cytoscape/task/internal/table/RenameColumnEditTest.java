package org.cytoscape.task.internal.table;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.cytoscape.model.CyColumn;

import org.junit.Test;


public class RenameColumnEditTest {
	@Test
	public void runTest() {
		final CyColumn column = mock(CyColumn.class);
		when(column.getName()).thenReturn("orig name");

		final RenameColumnEdit renameEdit = new RenameColumnEdit(column);
		when(column.getName()).thenReturn("new name");
		renameEdit.undo();
		verify(column, times(1)).setName("orig name");
		renameEdit.redo();
		verify(column, times(1)).setName("new name");
	}
}
