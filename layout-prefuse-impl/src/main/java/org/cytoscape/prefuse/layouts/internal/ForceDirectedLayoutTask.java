package org.cytoscape.prefuse.layouts.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractParallelPartitionLayoutTask;
import org.cytoscape.view.layout.LayoutEdge;
import org.cytoscape.view.layout.LayoutNode;
import org.cytoscape.view.layout.LayoutPartition;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.undo.UndoSupport;

import prefuse.util.force.DragForce;
import prefuse.util.force.ForceItem;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.SpringForce;
import prefuse.util.force.StateMonitor;

/*
 * #%L
 * Cytoscape Prefuse Layout Impl (layout-prefuse-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2007 - 2021 The Cytoscape Consortium
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

/**
 * This class wraps the Prefuse force-directed layout algorithm.
 * 
 * @see <a href="http://prefuse.org">Prefuse web site</a>
 */
public class ForceDirectedLayoutTask extends AbstractParallelPartitionLayoutTask {

	// private ForceSimulator m_fsim;
	private ForceDirectedLayout.Integrators integrator;
	private final ForceDirectedLayoutContext context;
	private final StateMonitor monitor;

	/**
	 * Creates a new ForceDirectedLayout object.
	 */
	public ForceDirectedLayoutTask(
			String displayName,
			CyNetworkView networkView,
			Set<View<CyNode>> nodesToLayOut,
			ForceDirectedLayoutContext context,
			ForceDirectedLayout.Integrators integrator,
			String attrName,
			UndoSupport undo
	) {
		super(displayName, context.singlePartition, networkView, nodesToLayOut, attrName, undo);

		this.context = context;
		this.integrator = integrator;
		
		edgeWeighter = context.edgeWeighter;
		edgeWeighter.setWeightAttribute(layoutAttribute);

		monitor = new StateMonitor();
		
		// m_fsim = new ForceSimulator(monitor);
		// m_fsim.addForce(new NBodyForce(monitor));
		// m_fsim.addForce(new SpringForce());
		// m_fsim.addForce(new DragForce());
	}
	
	@Override
	public String toString() {
		return ForceDirectedLayout.ALGORITHM_DISPLAY_NAME;
	}

	@Override
	public void layoutPartition(LayoutPartition part) {
		// if (taskMonitor != null)
		// 	taskMonitor.setStatusMessage("Partition " + part.getPartitionNumber() + ": Initializing...");
		
		ForceSimulator m_fsim = new ForceSimulator(monitor);
		m_fsim.addForce(new NBodyForce(monitor));
		m_fsim.addForce(new SpringForce());
		m_fsim.addForce(new DragForce());

		// Calculate our edge weights
		part.calculateEdgeWeights();
		
		m_fsim = new ForceSimulator(monitor);
		m_fsim.addForce(new NBodyForce(monitor));
		m_fsim.addForce(new SpringForce());
		m_fsim.addForce(new DragForce());

		List<LayoutNode> nodeList = part.getNodeList();
		List<LayoutEdge> edgeList = part.getEdgeList();

		if (context.isDeterministic) {
			Collections.sort(nodeList);
			Collections.sort(edgeList);
		}

		Map<LayoutNode,ForceItem> forceItems = new HashMap<>();
		
		// initialize nodes
		for (LayoutNode ln : nodeList) {
			if (cancelled)
				return;
			
			ForceItem fitem = forceItems.get(ln);
			
			if (fitem == null) {
				fitem = new ForceItem();
				forceItems.put(ln, fitem);
			}
			
			fitem.mass = getMassValue(ln);
			fitem.location[0] = 0f;
			fitem.location[1] = 0f;
			m_fsim.addItem(fitem);
		}

		// initialize edges
		for (LayoutEdge e : edgeList) {
			if (cancelled)
				return;
			
			LayoutNode n1 = e.getSource();
			ForceItem f1 = forceItems.get(n1);
			LayoutNode n2 = e.getTarget();
			ForceItem f2 = forceItems.get(n2);

			if (f1 == null || f2 == null)
				continue;

			m_fsim.addSpring(f1, f2, getSpringCoefficient(e), getSpringLength(e));
		}

		// perform layout
		long timestep = 1000L;

		for (int i = 0; i < context.numIterations; i++) {
			if (cancelled)
				return;

			// if (taskMonitor != null)
			// 	taskMonitor.setStatusMessage(
			// 			"Partition " + part.getPartitionNumber() + ": Iteration " + (i + 1)
			// 			+ " of " + context.numIterations + "...");

			timestep *= (1.0 - i / (double) context.numIterations);
			long step = timestep + 50;
			m_fsim.runSimulator(step);
			// setTaskStatus((int) (((double) i / (double) context.numIterations) * 90. + 5));
		}

		// update positions
		part.resetNodes(); // reset the nodes so we get the new average location

		for (LayoutNode ln : part.getNodeList()) {
			if (cancelled)
				return;

			if (!ln.isLocked()) {
				ForceItem fitem = forceItems.get(ln);
				ln.setX(fitem.location[0]);
				ln.setY(fitem.location[1]);
				part.moveNodeToLocation(ln);
			}
		}
	}
	
	@Override
	public void cancel() {
		super.cancel();
		monitor.cancel();
	}

	/**
	 * Get the mass value associated with the given node. Subclasses should
	 * override this method to perform custom mass assignment.
	 * @param n the node for which to compute the mass value
	 * @return the mass value for the node. By default, all items are given
	 * a mass value of 1.0.
	 */
	protected float getMassValue(LayoutNode n) {
		return (float)context.defaultNodeMass;
	}

	/**
	 * Get the spring length for the given edge. Subclasses should
	 * override this method to perform custom spring length assignment.
	 * @param e the edge for which to compute the spring length
	 * @return the spring length for the edge. A return value of
	 * -1 means to ignore this method and use the global default.
	*/
	protected float getSpringLength(LayoutEdge e) {
		double weight = e.getWeight();
		
		if (weight == 0.0)
			return (float)(context.defaultSpringLength);

		return (float)(context.defaultSpringLength/weight);
	}

	/**
	 * Get the spring coefficient for the given edge, which controls the
	 * tension or strength of the spring. Subclasses should
	 * override this method to perform custom spring tension assignment.
	 * @param e the edge for which to compute the spring coefficient.
	 * @return the spring coefficient for the edge. A return value of
	 * -1 means to ignore this method and use the global default.
	 */
	protected float getSpringCoefficient(LayoutEdge e) {
		return (float)context.defaultSpringCoefficient;
	}
}
