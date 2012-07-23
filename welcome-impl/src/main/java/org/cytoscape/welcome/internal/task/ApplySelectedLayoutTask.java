package org.cytoscape.welcome.internal.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewCollectionTask;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.work.util.ListSingleSelection;

public class ApplySelectedLayoutTask extends AbstractNetworkViewCollectionTask {

	@Tunable(description = "Which Layout do you want to apply?")
	public ListSingleSelection<Object> layouts;

	private final CyApplicationManager applicationManager;

	private final CyServiceRegistrar registrar;

	public ApplySelectedLayoutTask(Collection<CyNetworkView> networkViews,
			final CyLayoutAlgorithmManager cyLayoutAlgorithmManager, final CyApplicationManager applicationManager,
			final CyServiceRegistrar registrar) {
		super(networkViews);

		this.applicationManager = applicationManager;
		this.registrar = registrar;

		getAllAlgorithms(cyLayoutAlgorithmManager);

	}

	/**
	 * Hack to mix CyLayots and irregular yFiles algorithms
	 * 
	 * @param cyLayoutAlgorithmManager
	 */
	private final void getAllAlgorithms(final CyLayoutAlgorithmManager cyLayoutAlgorithmManager) {
		// Hand-pick some yFiles algorithms
		final NetworkViewTaskFactory organic = registrar.getService(NetworkViewTaskFactory.class, "(title=Organic)");
		final NetworkViewTaskFactory orthogonal = registrar.getService(NetworkViewTaskFactory.class,
				"(title=Orthogonal)");
		final NetworkViewTaskFactory hierarchic = registrar.getService(NetworkViewTaskFactory.class,
				"(title=Hierarchic)");
		final NetworkViewTaskFactory circular = registrar.getService(NetworkViewTaskFactory.class, "(title=Circular)");

		final DummyLayoutWrapper wrapped1 = new DummyLayoutWrapper(organic, "organic", "yFiles Organic Layout",
				registrar.getService(UndoSupport.class));
		final DummyLayoutWrapper wrapped2 = new DummyLayoutWrapper(orthogonal, "orthogonal", "yFiles Orthogonal Layout",
				registrar.getService(UndoSupport.class));
		final DummyLayoutWrapper wrapped3 = new DummyLayoutWrapper(hierarchic, "hierarchic", "yFiles Hierarchic Layout",
				registrar.getService(UndoSupport.class));
		final DummyLayoutWrapper wrapped4 = new DummyLayoutWrapper(circular, "circular", "yFiles Circular Layout",
				registrar.getService(UndoSupport.class));

		final Collection<CyLayoutAlgorithm> availableLayouts = cyLayoutAlgorithmManager.getAllLayouts();
		final List<Object> mixed = new ArrayList<Object>();
		mixed.add(wrapped1);
		mixed.add(wrapped2);
		mixed.add(wrapped3);
		mixed.add(wrapped4);
		mixed.addAll(availableLayouts);

		layouts = new ListSingleSelection<Object>(mixed);
		layouts.setSelectedValue(wrapped1);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		networkViews.clear();
		networkViews.addAll(applicationManager.getSelectedNetworkViews());
		networkViews.add(applicationManager.getCurrentNetworkView());

		final Object selected = layouts.getSelectedValue();

		if (selected instanceof DummyLayoutWrapper) {
			final DummyLayoutWrapper layout = (DummyLayoutWrapper) selected;
			for (final CyNetworkView view : networkViews)
				insertTasksAfterCurrentTask(layout.createTaskIterator(view, null, null, null));

		} else if (selected instanceof CyLayoutAlgorithm) {
			final CyLayoutAlgorithm layout = (CyLayoutAlgorithm) selected;
			for (final CyNetworkView view : networkViews) {
				insertTasksAfterCurrentTask(layout.createTaskIterator(view, layout.getDefaultLayoutContext(),
						CyLayoutAlgorithm.ALL_NODE_VIEWS, ""));
			}
		}
	}

	private static final class DummyLayoutWrapper extends AbstractLayoutAlgorithm {

		private final NetworkViewTaskFactory tf;

		public DummyLayoutWrapper(final NetworkViewTaskFactory tf, String computerName, String humanName,
				UndoSupport undoSupport) {
			super(computerName, humanName, undoSupport);
			this.tf = tf;

		}

		@Override
		public TaskIterator createTaskIterator(CyNetworkView networkView, Object layoutContext,
				Set<View<CyNode>> nodesToLayOut, String layoutAttribute) {
			return tf.createTaskIterator(networkView);
		}

	}

}
