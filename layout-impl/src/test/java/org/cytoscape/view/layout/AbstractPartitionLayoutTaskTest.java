package org.cytoscape.view.layout;

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
		((AbstractPartitionLayoutTask)task).layoutPartion(partition);
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
		public void layoutPartion(LayoutPartition partition) {

		}

	}
}
