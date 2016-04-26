package org.cytoscape.prefuse.layouts.internal;

/*
 * #%L
 * Cytoscape Prefuse Layout Impl (layout-prefuse-impl)
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
		final Set<Class<?>> ret = new HashSet<>();

		ret.add(Integer.class);
		ret.add(Double.class);

		return ret;
	}

	@Override
	public boolean getSupportsSelectedOnly() {
		return true;
	}
}
