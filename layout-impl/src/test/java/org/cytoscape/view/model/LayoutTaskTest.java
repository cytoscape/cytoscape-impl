package org.cytoscape.view.layout;

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

		public DummyLayoutTask(String name, CyNetworkView networkView, Set<View<CyNode>> nodesToLayOut) {
			super(name, networkView, nodesToLayOut,"", mock(UndoSupport.class));
		}

		public DummyLayoutTask(String name, CyNetworkView networkView, Set<View<CyNode>> nodesToLayOut, String attr) {
			super(name, networkView, nodesToLayOut,"", mock(UndoSupport.class));

		}

		@Override
		protected void doLayout(TaskMonitor taskMonitor) {
		}

	}
}
