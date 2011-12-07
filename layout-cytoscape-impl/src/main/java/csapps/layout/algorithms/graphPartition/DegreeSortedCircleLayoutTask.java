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


public class DegreeSortedCircleLayoutTask extends AbstractPartitionLayoutTask {
	private String DEGREE_ATTR_NAME = "degree";
	private CyNetwork network;

	/**
	 * Creates a new GridNodeLayout object.
	 */
	public DegreeSortedCircleLayoutTask(
		final CyNetworkView networkView, final String name, final boolean selectedOnly,
		final Set<View<CyNode>> staticNodes, final String DEGREE_ATTR_NAME,
		final boolean singlePartition)
	{
		super(networkView, name, singlePartition, selectedOnly, staticNodes);

		this.DEGREE_ATTR_NAME= DEGREE_ATTR_NAME;
		this.network = networkView.getModel();
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param partition DOCUMENT ME!
	 */
	public void layoutPartion(LayoutPartition partition) {
		// Create attribute
		CyTable table = network.getDefaultNodeTable();
		if (table.getColumn(DEGREE_ATTR_NAME) == null)
			table.createColumn(DEGREE_ATTR_NAME, Double.class, false);

		// just add the unlocked nodes
		List<LayoutNode> nodes = new ArrayList<LayoutNode>();
		for (final LayoutNode ln : partition.getNodeList()) {
			if (!ln.isLocked())
				nodes.add(ln);
		}
	
		if (cancelled)
			return;

		// sort the Nodes based on the degree
		Collections.sort(nodes,
		            new Comparator<LayoutNode>() {
				public int compare(LayoutNode o1, LayoutNode o2) {
					final CyNode node1 = o1.getNode();
					final CyNode node2 = o2.getNode();
					// FIXME: should allow parametrization of edge type? (expose as tunable)
					final int d1 = network.getAdjacentEdgeList(node1, CyEdge.Type.ANY).size();
					final int d2 = network.getAdjacentEdgeList(node2, CyEdge.Type.ANY).size();
					
					// Create Degree Attribute
					node1.getCyRow().set(DEGREE_ATTR_NAME, (double)d1);
					node2.getCyRow().set(DEGREE_ATTR_NAME, (double)d2);
					
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
	
	//public void construct() {
	//	super.construct();
	//}

}
