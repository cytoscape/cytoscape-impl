package csapps.layout.algorithms.hierarchicalLayout;

/*
 * #%L
 * Cytoscape Layout Algorithms Impl (layout-cytoscape-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2004 - 2013
 *   Institute for Systems Biology
 *   University of California at San Diego
 *   Memorial Sloan-Kettering Cancer Center
 *   The Cytoscape Consortium
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

/*
 * Code written by: Robert Sheridan
 * Authors: Gary Bader, Ethan Cerami, Chris Sander
 * Date: January 19.2004
 * Description: Hierarcical layout app, based on techniques by Sugiyama
 * et al. described in chapter 9 of "graph drawing", Di Battista et al,1999
 *
 * Based on the csapps.tutorial written by Ethan Cerami and GINY app
 * written by Andrew Markiel
 */


import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;


/**
 * Lays out graph in tree-like pattern.
 * The layout will approximate the optimal orientation
 * for nodes which have a tree-like relationship. <strong> This
 * assumed relationship is based on directed edges. This class does
 * not currently distinguish or gracefully treat undirected edges.
 * Also, duplicate edges are ignored for the purpose of positioning
 * nodes in the layout.
 * </strong>
 * <br>The major steps in this algorithm are:
 * <ol>
 * <li>Choose the set of nodes to be layed out based on which are selected</li>
 * <li>Partition this set into connected components</li>
 * <li>Detect and eliminate (temporarily) graph cycles</li>
 * <li>Eliminate (temporarily) transitive edges</li>
 * <li>Assign nodes to layers (parents always in layer above any child's layer)</li>
 * <li>Choose a within-layer ordering which reduces edge crossings between layers</li>
 * <li>Select horizontal positions for nodes within a layer to minimize edge length</li>
 * <li>Assemble layed out components and any unselected nodes into a composite layout</li>
 * </ol>
 * Steps 2 through 6 are performed by calls to methods in the class
 * {@link csapps.hierarchicallayout.Graph}
*/
public class HierarchicalLayoutAlgorithm extends AbstractLayoutAlgorithm {
	
	private final CyServiceRegistrar serviceRegistrar;

	public HierarchicalLayoutAlgorithm(final CyServiceRegistrar serviceRegistrar, final UndoSupport undoSupport) {
		super("hierarchical", "Hierarchical Layout", undoSupport);
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView, Object context, Set<View<CyNode>> nodesToLayOut,
			String attrName) {
		return new TaskIterator(new HierarchicalLayoutAlgorithmTask(toString(), networkView, nodesToLayOut,
				(HierarchicalLayoutContext) context, attrName, undoSupport, serviceRegistrar));
	}
	
	@Override
	public Object createLayoutContext() {
		return new HierarchicalLayoutContext();
	}
	
	@Override
	public boolean getSupportsSelectedOnly() {
		return true;
	}
}
