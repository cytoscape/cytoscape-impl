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

import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class AbstractPartitionLayoutTaskTest extends LayoutTaskTest {

	@Before
	public void setUp() throws Exception {
		super.setUp();
		task = new DummyPartitionTask(name, true, networkView, nodesToLayOut, supportedNodeAttributeTypes,
				supportedEdgeAttributeTypes, initialAttributes);
	}

	
	@Test
	public void testAbstractPartitionLayoutTask() {
		assertNotNull(task);
		assertEquals(DummyPartitionTask.class, task.getClass());
	}
	
	@Test
	public void testDoLayout() {
		task.doLayout(null);
	}

	@Test
	public void testLayoutPartion() {
		final LayoutPartition partition = new LayoutPartition(10, 10);
		((AbstractPartitionLayoutTask)task).layoutPartition(partition);
	}

	@Test
	public void testSetTaskStatus() {
		((AbstractPartitionLayoutTask)task).setTaskStatus(20);
		((AbstractPartitionLayoutTask)task).setTaskStatus(-20);
	}

	private static final class DummyPartitionTask extends AbstractPartitionLayoutTask {

		public DummyPartitionTask(String name, boolean singlePartition, CyNetworkView networkView,
				Set<View<CyNode>> nodesToLayOut, Set<Class<?>> supportedNodeAttributeTypes,
				Set<Class<?>> supportedEdgeAttributeTypes, List<String> initialAttributes) {
			super(name, singlePartition, networkView, nodesToLayOut, "", mock(UndoSupport.class));
			// TODO Auto-generated constructor stub
		}

		@Override
		public void layoutPartition(LayoutPartition partition) {

		}

	}
}
