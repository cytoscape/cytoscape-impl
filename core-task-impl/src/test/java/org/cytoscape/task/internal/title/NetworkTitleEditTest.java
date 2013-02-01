package org.cytoscape.task.internal.title;

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

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.junit.Test;


public class NetworkTitleEditTest {
	@Test
	public void runTest() {
		final CyNetwork network = mock(CyNetwork.class);
		final CyRow row =  mock(CyRow.class);
		when(network.getRow(network)).thenReturn(row);
		when(row.get("name", String.class)).thenReturn("newTitle");

		final NetworkTitleEdit titleEdit = new NetworkTitleEdit(network, "oldTitle");
		titleEdit.undo();
		verify(row, times(1)).set("name", "oldTitle");
		titleEdit.redo();
		verify(row, times(1)).set("name", "newTitle");
	}
}
