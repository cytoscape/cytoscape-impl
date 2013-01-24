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


public class AttributeCircleLayoutTask extends AbstractPartitionLayoutTask {
	private final AttributeCircleLayoutContext context;

	/**
	 * Creates a new ForceDirectedLayout object.
	 */
	public AttributeCircleLayoutTask(final String displayName, CyNetworkView networkView, Set<View<CyNode>> nodesToLayOut,  final AttributeCircleLayoutContext context, String attrName, UndoSupport undo) {
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

		int count = nodes.size();
		int r = (int) Math.sqrt(count);
		r *= context.spacing;

		if (layoutAttribute!= null && count > 0) {
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

		// Compute angle step
		double phi = (2 * Math.PI) / count;

		partition.resetNodes(); // We want to figure out our mins & maxes anew
		                        // Arrange vertices in a circle

		for (int i = 0; i < count; i++) {
			LayoutNode node = (LayoutNode) nodes.get(i);
			double x = r + (r * Math.sin(i * phi));
			double y = r + (r * Math.cos(i * phi));
			node.setX(x);
			node.setY(y);
			partition.moveNodeToLocation(node);
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
