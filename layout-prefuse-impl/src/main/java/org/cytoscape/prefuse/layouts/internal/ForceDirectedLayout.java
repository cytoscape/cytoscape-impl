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

import prefuse.util.force.EulerIntegrator;
import prefuse.util.force.Integrator;
import prefuse.util.force.RungeKuttaIntegrator;


public class ForceDirectedLayout extends AbstractLayoutAlgorithm<ForceDirectedLayoutContext> {
	/**
	 * Value to set for doing unweighted layouts
	 */
	public static final String UNWEIGHTEDATTRIBUTE = "(unweighted)";

	private Integrators integrator = Integrators.RUNGEKUTTA;

	private boolean supportWeights;
	
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
		super("force-directed", "Force Directed Layout");
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView, ForceDirectedLayoutContext context, Set<View<CyNode>> nodesToLayOut) {
		return new TaskIterator(new ForceDirectedLayoutTask(getName(), networkView, nodesToLayOut, getSupportedNodeAttributeTypes(), getSupportedEdgeAttributeTypes(), getInitialAttributeList(), context, integrator));
	}
	
	@Override
	public ForceDirectedLayoutContext createLayoutContext() {
		return new ForceDirectedLayoutContext();
	}
	
	public Set<Class<?>> getSupportedEdgeAttributeTypes() {
		Set<Class<?>> ret = new HashSet<Class<?>>();
		if (!supportWeights)
			return ret;

		ret.add( Integer.class );
		ret.add( Double.class );

		return ret;
	}

	public List<String> getInitialAttributeList() {
		List<String> list = new ArrayList<String>();
		if (!supportWeights)
			return list;
		
		list.add(UNWEIGHTEDATTRIBUTE);

		return list;
	}
}
