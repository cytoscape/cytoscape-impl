package org.cytoscape.task.internal.table;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.cytoscape.model.CyColumn;
import org.cytoscape.task.internal.table.RenameColumnEdit;
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
