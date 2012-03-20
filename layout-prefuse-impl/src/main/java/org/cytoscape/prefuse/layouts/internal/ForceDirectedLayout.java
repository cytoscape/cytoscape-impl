package org.cytoscape.prefuse.layouts.internal;


import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.work.TaskIterator;

import prefuse.util.force.EulerIntegrator;
import prefuse.util.force.Integrator;
import prefuse.util.force.RungeKuttaIntegrator;


public class ForceDirectedLayout extends AbstractLayoutAlgorithm<ForceDirectedLayoutContext> {
	private Integrators integrator = Integrators.RUNGEKUTTA;
	
	public enum Integrators {
		RUNGEKUTTA ("Runge-Kutta"),
		EULER ("Euler");

		private String name;
		private Integrators(String str) { name=str; }
		public String toString() { return name; }
		public Integrator getNewIntegrator() {
			if (this == EULER)
				return new EulerIntegrator();
			else
				return new RungeKuttaIntegrator();
		}
	}

	
	/**
	 * Creates a new GridNodeLayout object.
	 */
	public ForceDirectedLayout() {
		super("force-directed", "Force Directed Layout", true);
	}

	public TaskIterator createTaskIterator(ForceDirectedLayoutContext context) {
		return new TaskIterator(
			new ForceDirectedLayoutTask(getName(), context, integrator));
	}
	
	@Override
	public ForceDirectedLayoutContext createLayoutContext() {
		return new ForceDirectedLayoutContext(supportsSelectedOnly(), supportsNodeAttributes(), supportsEdgeAttributes());
	}
}
