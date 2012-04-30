package org.cytoscape.prefuse.layouts.internal;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
	public ForceDirectedLayout(UndoSupport undo) {
		super("force-directed", "Force Directed Layout",undo);
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView, Object context, Set<View<CyNode>> nodesToLayOut, String attrName) {
		return new TaskIterator(new ForceDirectedLayoutTask(getName(), networkView, nodesToLayOut, (ForceDirectedLayoutContext)context, integrator, attrName,undoSupport));
	}
	
	@Override
	public Object createLayoutContext() {
		return new ForceDirectedLayoutContext();
	}
	
	public Set<Class<?>> getSupportedEdgeAttributeTypes() {
		Set<Class<?>> ret = new HashSet<Class<?>>();

		ret.add( Integer.class );
		ret.add( Double.class );

		return ret;
	}
	
	@Override
	public boolean getSupportsSelectedOnly() {
		return true;
	}

}
