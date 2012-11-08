package org.cytoscape.prefuse.layouts.internal;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

import prefuse.util.force.EulerIntegrator;
import prefuse.util.force.Integrator;
import prefuse.util.force.RungeKuttaIntegrator;

public class ForceDirectedLayout extends AbstractLayoutAlgorithm {

	private static final String ALGORITHM_ID = "force-directed";
	static final String ALGORITHM_DISPLAY_NAME = "Prefuse Force Directed Layout";

	private Integrators integrator = Integrators.RUNGEKUTTA;

	public enum Integrators {
		RUNGEKUTTA("Runge-Kutta"), EULER("Euler");

		private String name;

		private Integrators(String str) {
			name = str;
		}

		@Override
		public String toString() {
			return name;
		}

		public Integrator getNewIntegrator() {
			if (this == EULER)
				return new EulerIntegrator();
			else
				return new RungeKuttaIntegrator();
		}
	}

	public ForceDirectedLayout(UndoSupport undo) {
		super(ALGORITHM_ID, ALGORITHM_DISPLAY_NAME, undo);
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView, Object context, Set<View<CyNode>> nodesToLayOut,
			String attrName) {
		return new TaskIterator(new ForceDirectedLayoutTask(toString(), networkView, nodesToLayOut,
				(ForceDirectedLayoutContext) context, integrator, attrName, undoSupport));
	}

	@Override
	public Object createLayoutContext() {
		return new ForceDirectedLayoutContext();
	}

	@Override
	public Set<Class<?>> getSupportedEdgeAttributeTypes() {
		final Set<Class<?>> ret = new HashSet<Class<?>>();

		ret.add(Integer.class);
		ret.add(Double.class);

		return ret;
	}

	@Override
	public boolean getSupportsSelectedOnly() {
		return true;
	}
}
