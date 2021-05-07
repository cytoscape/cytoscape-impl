package org.cytoscape.task.internal.zoom;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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
