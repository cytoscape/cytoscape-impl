package org.cytoscape.view.layout;

/*
 * #%L
 * Cytoscape Layout Impl (layout-impl)
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

import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Before;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

public class LayoutTaskTest extends AbstractLayoutTaskTest {

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		name = "test";
		nodesToLayOut.add(networkView.getNodeView(source));
		nodesToLayOut.add(networkView.getNodeView(target));
		task = new DummyLayoutTask(name, networkView, nodesToLayOut, "");
	}

	@Override
	public void testAbstractLayoutTaskConstructor() {
		name = "test";
		nodesToLayOut.add(networkView.getNodeView(source));
		nodesToLayOut.add(networkView.getNodeView(target));
		task = new DummyLayoutTask(name, networkView, nodesToLayOut,"");
		assertNotNull(task);

		AbstractLayoutTask task2 = new DummyLayoutTask(name, networkView, nodesToLayOut);
		assertNotNull(task2);
	}

	@Override
	public void testDoLayout() {
		// Should be implemented child classes.
	}

	private static final class DummyLayoutTask extends AbstractLayoutTask {

		public DummyLayoutTask(String displayName, CyNetworkView networkView, Set<View<CyNode>> nodesToLayOut) {
			super(displayName, networkView, nodesToLayOut,"", mock(UndoSupport.class));
		}

		public DummyLayoutTask(String displayName, CyNetworkView networkView, Set<View<CyNode>> nodesToLayOut, String attr) {
			super(displayName, networkView, nodesToLayOut,"", mock(UndoSupport.class));

		}

		@Override
		protected void doLayout(TaskMonitor taskMonitor) {
		}

	}
}
