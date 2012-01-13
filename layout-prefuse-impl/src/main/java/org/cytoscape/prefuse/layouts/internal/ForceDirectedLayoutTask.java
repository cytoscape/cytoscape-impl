/*
 Copyright (c) 2007, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.prefuse.layouts.internal; 


import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractPartitionLayoutTask;
import org.cytoscape.view.layout.EdgeWeighter;
import org.cytoscape.view.layout.LayoutEdge;
import org.cytoscape.view.layout.LayoutNode;
import org.cytoscape.view.layout.LayoutPartition;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.undo.UndoSupport;

import prefuse.util.force.DragForce;
import prefuse.util.force.EulerIntegrator;
import prefuse.util.force.ForceItem;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.Integrator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.RungeKuttaIntegrator;
import prefuse.util.force.SpringForce;


/**
 * This class wraps the Prefuse force-directed layout algorithm.
 * See {@link http://prefuse.org} for more detail.
 */
public class ForceDirectedLayoutTask extends AbstractPartitionLayoutTask {
	private ForceSimulator m_fsim;

	public int numIterations;
	public double defaultSpringCoefficient;
	public double defaultSpringLength;
	public double defaultNodeMass;
	public ForceDirectedLayout.Integrators integrator;
	
	/**
	 * Value to set for doing unweighted layouts
	 */
	public static final String UNWEIGHTEDATTRIBUTE = "(unweighted)";

	private boolean supportWeights = true;
	private Map<LayoutNode,ForceItem> forceItems;

	/**
	 * Creates a new ForceDirectedLayout object.
	 */
	public ForceDirectedLayoutTask(final CyNetworkView networkView, final String name,
				       final boolean selectedOnly,
				       final Set<View<CyNode>> staticNodes,
				       final int numIterations,
				       final double defaultSpringCoefficient,
				       final double defaultSpringLength,
				       final double defaultNodeMass,
				       final ForceDirectedLayout.Integrators integrator,
				       final boolean singlePartition)
	{
		super(networkView, name, singlePartition, selectedOnly, staticNodes);
		
		this.numIterations = numIterations;
		this.defaultSpringCoefficient = defaultSpringCoefficient;
		this.defaultSpringLength = defaultSpringLength;
		this.defaultNodeMass = defaultNodeMass;
		this.integrator = integrator;
		
		if (edgeWeighter == null)
			edgeWeighter = new EdgeWeighter();

		m_fsim = new ForceSimulator();
		m_fsim.addForce(new NBodyForce());
		m_fsim.addForce(new SpringForce());
		m_fsim.addForce(new DragForce());

		forceItems = new HashMap<LayoutNode,ForceItem>();

	}
	
	public String getName() {
		return "force-directed";
	}

	public String toString() {
		return "Force-Directed Layout";
	}

	protected void initialize_local() {
	}


	public void layoutPartion(LayoutPartition part) {
		Dimension initialLocation = null;
		// System.out.println("layoutPartion: "+part.getEdgeList().size()+" edges");
		// Calculate our edge weights
		part.calculateEdgeWeights();
		// System.out.println("layoutPartion: "+part.getEdgeList().size()+" edges after calculateEdgeWeights");

		m_fsim.setIntegrator(integrator.getNewIntegrator());
		m_fsim.clear();

		// initialize nodes
		for (LayoutNode ln: part.getNodeList()) {
			ForceItem fitem = forceItems.get(ln); 
			if ( fitem == null ) {
				fitem = new ForceItem();
				forceItems.put(ln, fitem);
			}
			fitem.mass = getMassValue(ln);
			fitem.location[0] = 0f; 
			fitem.location[1] = 0f; 
			m_fsim.addItem(fitem);
		}
		
		// initialize edges
		for (LayoutEdge e: part.getEdgeList()) {
			LayoutNode n1 = e.getSource();
			ForceItem f1 = forceItems.get(n1); 
			LayoutNode n2 = e.getTarget();
			ForceItem f2 = forceItems.get(n2); 
			if ( f1 == null || f2 == null )
				continue;
			// System.out.println("Adding edge "+e+" with spring coeffficient = "+getSpringCoefficient(e)+" and length "+getSpringLength(e));
			m_fsim.addSpring(f1, f2, getSpringCoefficient(e), getSpringLength(e)); 
		}

		// setTaskStatus(5); // This is a rough approximation, but probably good enough
		if (taskMonitor != null) {
			taskMonitor.setStatusMessage("Initializing partition "+part.getPartitionNumber());
		}

		// Figure out our starting point
		if (selectedOnly) {
			initialLocation = part.getAverageLocation();
		}

		// perform layout
		long timestep = 1000L;
		for ( int i = 0; i < numIterations && !cancelled; i++ ) {
			timestep *= (1.0 - i/(double)numIterations);
			long step = timestep+50;
			m_fsim.runSimulator(step);
			setTaskStatus((int)(((double)i/(double)numIterations)*90.+5));
		}
		
		// update positions
		for (LayoutNode ln: part.getNodeList()) {
			if (!ln.isLocked()) {
				ForceItem fitem = forceItems.get(ln); 
				ln.setX(fitem.location[0]);
				ln.setY(fitem.location[1]);
				part.moveNodeToLocation(ln);
			}
		}
		// Not quite done, yet.  If we're only laying out selected nodes, we need
		// to migrate the selected nodes back to their starting position
		if (selectedOnly) {
			double xDelta = 0.0;
			double yDelta = 0.0;
			Dimension finalLocation = part.getAverageLocation();
			xDelta = finalLocation.getWidth() - initialLocation.getWidth();
			yDelta = finalLocation.getHeight() - initialLocation.getHeight();
			for (LayoutNode v: part.getNodeList()) {
				if (!v.isLocked()) {
					v.decrement(xDelta, yDelta);
					part.moveNodeToLocation(v);
				}
			}
		}
	}

	/**
	 * Get the mass value associated with the given node. Subclasses should
	 * override this method to perform custom mass assignment.
	 * @param n the node for which to compute the mass value
	 * @return the mass value for the node. By default, all items are given
	 * a mass value of 1.0.
	 */
	protected float getMassValue(LayoutNode n) {
		return (float)defaultNodeMass;
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
		return (float)(defaultSpringLength/weight);
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
		return (float)defaultSpringCoefficient;
	}

	/**
	 * Return information about our algorithm
	 */
	public boolean supportsSelectedOnly() {
		return true;
	}

	public Set<Class<?>> supportsEdgeAttributes() {
		Set<Class<?>> ret = new HashSet<Class<?>>();
		if (!supportWeights)
			return ret;

		ret.add( Integer.class );
		ret.add( Double.class );

		return ret;
	}

	public List getInitialAttributeList() {
		ArrayList list = new ArrayList();
		list.add(UNWEIGHTEDATTRIBUTE);

		return list;
	}

}
