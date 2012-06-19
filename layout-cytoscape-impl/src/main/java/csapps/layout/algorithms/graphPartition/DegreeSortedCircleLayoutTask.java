package csapps.layout.algorithms.graphPartition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.layout.AbstractPartitionLayoutTask;
import org.cytoscape.view.layout.LayoutNode;
import org.cytoscape.view.layout.LayoutPartition;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.undo.UndoSupport;

public class DegreeSortedCircleLayoutTask extends AbstractPartitionLayoutTask {
	
	private static final String DEGREE_ATTR_NAME = "degree.layout";
	
	private final CyNetwork network;

	/**
	 * Creates a new GridNodeLayout object.
	 */
	public DegreeSortedCircleLayoutTask(final String name, CyNetworkView networkView, Set<View<CyNode>> nodesToLayOut,
			DegreeSortedCircleContext context, String attrName, UndoSupport undo) {
		super(name, context.singlePartition, networkView, nodesToLayOut, attrName, undo);

		this.network = networkView.getModel();
	}

	@Override
	public void layoutPartion(LayoutPartition partition) {
		// Create attribute
		final CyTable table = network.getDefaultNodeTable();
		if (table.getColumn(DEGREE_ATTR_NAME) == null)
			table.createColumn(DEGREE_ATTR_NAME, Integer.class, false);

		// just add the unlocked nodes
		final List<LayoutNode> nodes = new ArrayList<LayoutNode>();
		for (final LayoutNode ln : partition.getNodeList()) {
			if (!ln.isLocked())
				nodes.add(ln);
		}

		if (cancelled)
			return;

		// sort the Nodes based on the degree
		Collections.sort(nodes, new Comparator<LayoutNode>() {
			public int compare(LayoutNode o1, LayoutNode o2) {
				final CyNode node1 = o1.getNode();
				final CyNode node2 = o2.getNode();
				// FIXME: should allow parametrization of edge type? (expose as
				// tunable)
				final int d1 = network.getAdjacentEdgeList(node1, CyEdge.Type.ANY).size();
				final int d2 = network.getAdjacentEdgeList(node2, CyEdge.Type.ANY).size();

				// Create Degree Attribute
				o1.getRow().set(DEGREE_ATTR_NAME, d1);
				o2.getRow().set(DEGREE_ATTR_NAME, d2);

				return (d2 - d1);
			}

			public boolean equals(Object o) {
				return false;
			}
		});

		if (cancelled)
			return;

		// place each Node in a circle
		int r = 100 * (int) Math.sqrt(nodes.size());
		double phi = (2 * Math.PI) / nodes.size();
		partition.resetNodes(); // We want to figure out our mins & maxes anew

		for (int i = 0; i < nodes.size(); i++) {
			LayoutNode node = nodes.get(i);
			node.setX(r + (r * Math.sin(i * phi)));
			node.setY(r + (r * Math.cos(i * phi)));
			partition.moveNodeToLocation(node);
		}
	}
}
