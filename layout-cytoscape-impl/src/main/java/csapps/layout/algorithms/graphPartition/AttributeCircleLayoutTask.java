package csapps.layout.algorithms.graphPartition;


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
import org.cytoscape.work.Tunable;


public class AttributeCircleLayoutTask extends AbstractPartitionLayoutTask {
	private final String attribute;
	private final double spacing;
	private final boolean supportNodeAttributes;

	/**
	 * Creates a new ForceDirectedLayout object.
	 */
	public AttributeCircleLayoutTask(
		final CyNetworkView networkView, final String name, final boolean selectedOnly,
		final Set<View<CyNode>> staticNodes, final String attribute, final double spacing,
		final boolean supportNodeAttributes, final boolean singlePartition)
	{
		super(networkView, name, singlePartition, selectedOnly, staticNodes);

		this.attribute = attribute;
		this.spacing = spacing;
		this.supportNodeAttributes = supportNodeAttributes;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param partition DOCUMENT ME!
	 */
	public void layoutPartion(LayoutPartition partition) {
		// just add the unlocked nodes
		List<LayoutNode> nodes = new ArrayList<LayoutNode>();
		for ( LayoutNode ln : partition.getNodeList() ) {
			if ( !ln.isLocked() ) {
				nodes.add(ln);
			}
		}

		int count = nodes.size();
		int r = (int) Math.sqrt(count);
		r *= spacing;

		if (this.attribute != null && count > 0) {
			final CyColumn column = nodes.get(0).getRow().getTable().getColumn(attribute);
			Class<?> klass = (column == null) ? null : column.getType();
			if (klass != null && Comparable.class.isAssignableFrom(klass)){
				// FIXME: I assume this would be better, but get type errors if I try:
				//Class<Comparable<?>> kasted = (Class<Comparable<?>>) klass;
				//Collections.sort(nodes, new AttributeComparator<Comparable<?>>(kasted));
				Collections.sort(nodes, new AttributeComparator(klass));
			} else {
				/* FIXME Error! */
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
			T v1 = o1.getRow().get(attribute, klass);
			T v2 = o2.getRow().get(attribute, klass);
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
