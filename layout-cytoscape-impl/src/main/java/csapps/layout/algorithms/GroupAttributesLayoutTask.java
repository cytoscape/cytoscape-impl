package csapps.layout.algorithms;

/*
 * #%L
 * Cytoscape Layout Algorithms Impl (layout-cytoscape-impl)
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


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.layout.AbstractLayoutTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupAttributesLayoutTask extends AbstractLayoutTask {
	
	private static Logger logger = LoggerFactory.getLogger(GroupAttributesLayoutTask.class);

	private TaskMonitor taskMonitor;	
	private CyNetwork network;

	private GroupAttributesLayoutContext context;
	
	public GroupAttributesLayoutTask(final String displayName, CyNetworkView networkView, Set<View<CyNode>> nodesToLayOut, GroupAttributesLayoutContext context, String attrName, UndoSupport undo) {
		super(displayName, networkView, nodesToLayOut, attrName, undo);
		
		this.context = context;
	}

	@Override
	final protected void doLayout(final TaskMonitor taskMonitor) {
		this.taskMonitor = taskMonitor;
		this.network = networkView.getModel();
		construct(); 
	}


	/**
	  Pseudo-procedure:
	  1. Call makeDiscrete(). This will create a map for each value of the
	     node attribute to the list of nodes with that attribute value.
	     Each of these lists will become a partition in the graph.
	     makeDiscrete() will also add nodes to the invalidNodes list
	     that do not have a value associated with the attribute.
	  2. Call sort(). This will return a list of partitions that is
	     sorted based on the value of the attribute. Add the invalid
	     nodes to the end of the sorted list. All the invalid nodes
	     will be grouped together in the last partition of the layout.
	  3. Begin plotting each partition.
	     a. Call encircle(). This will plot the partition in a circle.
	     b. Store the diameter of the last circle plotted.
	     c. Update maxheight. This stores the height of the largest circle
	        in a row.
	     d. Update offsetx. If we've reached the end of the row,
	        reset offsetx and maxheight; update offsety so that
	    it will store the y-axis location of the next row.
	*/
	private void construct() {		
		taskMonitor.setStatusMessage("Initializing");

		CyTable dataTable = network.getDefaultNodeTable();
		
		Class<?> klass;
		if(layoutAttribute == null || layoutAttribute.isEmpty()) {
			klass = null;
		} else {
			klass = dataTable.getColumn(layoutAttribute).getType();
		}
		
		if (klass == null || Comparable.class.isAssignableFrom(klass)){
			Class<Comparable>kasted = (Class<Comparable>) klass;
			doConstruct(kasted);
		} else {
			/* FIXME Error. */
		}
	}
	/** Needed to allow usage of parametric types */
	private <T extends Comparable<T>> void doConstruct(Class<T> klass){
		final Map<T, List<CyNode>> partitionMap = new TreeMap<T, List<CyNode>>();
		final List<CyNode> invalidNodes = new ArrayList<CyNode>();
		makeDiscrete(partitionMap, invalidNodes, klass);

		final List<List<CyNode>> partitionList = sort(partitionMap);
		partitionList.add(invalidNodes);

		double offsetx = 0.0;
		double offsety = 0.0;
		double maxheight = 0.0;

		for (List<CyNode> partition : partitionList) {
			if (cancelled)
				return;

			double radius = encircle(partition, offsetx, offsety);

			double diameter = 2.0 * radius;

			if (diameter > maxheight)
				maxheight = diameter;

			offsetx += diameter;

			if (offsetx > context.maxwidth) {
				offsety += (maxheight + context.spacingy);
				offsetx = 0.0;
				maxheight = 0.0;
			} else
				offsetx += context.spacingx;
		}
	}
	
	private <T extends Comparable<T>> void makeDiscrete(Map<T, List<CyNode>> map, List<CyNode> invalidNodes, Class<T> klass) {
		if (klass == null) {
			for (View<CyNode> nv: nodesToLayOut) {
				CyNode node = nv.getModel();
				invalidNodes.add(node);
			}
		} else {
			for (View<CyNode> nv: nodesToLayOut) {
				CyNode node = nv.getModel();
				// TODO: support namespace
				T key = network.getRow(node).get(layoutAttribute, klass);
	
				if (key == null) {
					if (invalidNodes != null)
						invalidNodes.add(node);
				} else {
					List<CyNode> list = map.get(key);
					if (list == null) {
						list = new ArrayList<CyNode>();
						map.put(key, list);
					}
					list.add(node);
				}
			}
		}
	}

	private <T extends Comparable<T>> List<List<CyNode>> sort(final Map<T, List<CyNode>> map) {
		List<T> keys = new ArrayList<T>(map.keySet());
		Collections.sort(keys);

		Comparator<CyNode> comparator = new Comparator<CyNode>() {
			public int compare(CyNode node1, CyNode node2) {
				// FIXME: this code was originally comparing node1.getIdentifier() to node2.getIdentifier()
				// I'm not sure that comparing the indices of the nodes gets the same effect
				// on the other hand, nodes don't have a human-readable uid in 3.0
				Long a = node1.getSUID();
				Long b = node2.getSUID();

				return a.compareTo(b);
			}
		};

		List<List<CyNode>> sortedlist = new ArrayList<List<CyNode>>(map.keySet().size());

		for (T key : keys) {
			List<CyNode> partition = map.get(key);
			Collections.sort(partition, comparator);
			sortedlist.add(partition);
		}

		return sortedlist;
	}

	private double encircle(List<CyNode> partition, double offsetx, double offsety) {
		if (partition == null)
			return 0.0;

		if (partition.size() == 1) {
			CyNode node = partition.get(0);
			networkView.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, offsetx);
			networkView.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, offsety);

			return 0.0;
		}

		double radius = context.radmult * Math.sqrt(partition.size());

		if (radius < context.minrad)
			radius = context.minrad;

		double phidelta = (2.0 * Math.PI) / partition.size();
		double phi = 0.0;

		for (CyNode node : partition) {
			double x = offsetx + radius + (radius * Math.cos(phi));
			double y = offsety + radius + (radius * Math.sin(phi));
			networkView.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, x);
			networkView.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, y);
			phi += phidelta;
		}

		return radius;
	}
}
