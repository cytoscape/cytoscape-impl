package csapps.layout.algorithms.graphPartition;

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
import java.util.Set;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractPartitionLayoutTask;
import org.cytoscape.view.layout.LayoutNode;
import org.cytoscape.view.layout.LayoutPartition;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.undo.UndoSupport;


public class AttributeGridLayoutTask extends AbstractPartitionLayoutTask {
	private final AttributeGridLayoutContext context;

	/**
	 * Creates a new ForceDirectedLayout object.
	 */
	public AttributeGridLayoutTask(final String displayName, CyNetworkView networkView, Set<View<CyNode>> nodesToLayOut,  final AttributeGridLayoutContext context, String attrName, UndoSupport undo) {
		super(displayName, context.singlePartition, networkView, nodesToLayOut,attrName, undo);
		this.context = context;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param partition DOCUMENT ME!
	 */
	public void layoutPartition(LayoutPartition partition) {
		// just add the unlocked nodes
		List<LayoutNode> nodes = new ArrayList<LayoutNode>();
		for ( LayoutNode ln : partition.getNodeList() ) {
			if ( !ln.isLocked() ) {
				nodes.add(ln);
			}
		}

		int nodeCount = nodes.size();

		if (layoutAttribute!= null && nodeCount > 0) {
			final CyColumn column = nodes.get(0).getRow().getTable().getColumn(layoutAttribute);
			Class<?> klass = (column == null) ? null : column.getType();
			if (klass != null && Comparable.class.isAssignableFrom(klass)){
				// FIXME: I assume this would be better, but get type errors if I try:
				//Class<Comparable<?>> kasted = (Class<Comparable<?>>) klass;
				//Collections.sort(nodes, new AttributeComparator<Comparable<?>>(kasted));
				Collections.sort(nodes, new AttributeComparator(klass));
			} else {
				/* FIXME Error. */
			}
		}


		partition.resetNodes(); // We want to figure out our mins & maxes anew
		                        // Arrange vertices in a circle

		double initialX = 0.0;
		double initialY = 0.0;
		double nodeVerticalSpacing = context.nodeVerticalSpacing;
    double nodeHorizontalSpacing = context.nodeHorizontalSpacing;

		int columns = context.nColumns;
		if (columns <= 0)
			columns = (int) Math.sqrt(nodeCount);

		for (LayoutNode node: nodes) {
      initialX += node.getX()/nodeCount;
      initialY += node.getY()/nodeCount;
    }

    // initialX and initialY reflect the center of our grid, so we
    // need to offset by distance*columns/2 in each direction
    initialX = initialX - ((nodeHorizontalSpacing * (columns - 1)) / 2);
    initialY = initialY - ((nodeVerticalSpacing * (columns - 1)) / 2);
    double currX = initialX;
    double currY = initialY;

		int count = 0;
		for (LayoutNode node: nodes) {
			node.setX(currX);
			node.setY(currY);
			partition.moveNodeToLocation(node);
      count++;
      if (count == columns) {
        count = 0;
        currX = initialX;
        currY += nodeVerticalSpacing;
      } else {
        currX += nodeHorizontalSpacing;
      }
		}
	}

	private class AttributeComparator<T extends Comparable<T>> implements Comparator<LayoutNode> {
		Class<T> klass;
		private AttributeComparator(Class<T> klass) {
			this.klass = klass;
		}

		public int compare(LayoutNode o1, LayoutNode o2) {
			T v1 = o1.getRow().get(layoutAttribute, klass);
			T v2 = o2.getRow().get(layoutAttribute, klass);
			if (String.class.isAssignableFrom(klass)){ // i.e. if klass _is_ String.class
				String s1 = String.class.cast(v1);
				String s2 = String.class.cast(v2);
				if ((s1 != null) && (s2 != null))
					return s1.compareToIgnoreCase(s2);
				else if ((s1 == null) && (s2 != null))
					return -1;
				else if ((s1 == null) && (s2 == null))
					return 0;
				else if ((s1 != null) && (s2 == null))
					return 1;

			} else {
				return compareEvenIfNull(v1, v2);
			}

			return 0; // can't happen anyway
		}

		public int compareEvenIfNull(T v1, T v2){
			if ((v1 != null) && (v2 != null))
				return v1.compareTo(v2);
			else if ((v1 == null) && (v2 != null))
				return -1;
			else if ((v1 == null) && (v2 == null))
				return 0;
			else // if ((v1 != null) && (v2 == null)) // this is the only possibility
				return 1;
		}
	}

}
