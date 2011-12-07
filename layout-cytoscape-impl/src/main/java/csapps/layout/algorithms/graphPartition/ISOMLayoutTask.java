package csapps.layout.algorithms.graphPartition;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractPartitionLayoutTask;
import org.cytoscape.view.layout.LayoutNode;
import org.cytoscape.view.layout.LayoutPartition;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.Tunable;

import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntIntHashMap;
import cern.colt.map.OpenIntObjectHashMap;
import cern.colt.map.PrimeFinder;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;

public class ISOMLayoutTask  extends AbstractPartitionLayoutTask {

	public int maxEpoch;
	private int epoch;
	public int radiusConstantTime;
	public int radius;
	public int minRadius;
	private double adaption;
	public double initialAdaptation;
	public double minAdaptation;
	public double sizeFactor;
	public double coolingFactor;
	private LayoutPartition partition;

	//Queue, First In First Out, use add() and get(0)/remove(0)
	private IntArrayList q;
	OpenIntObjectHashMap nodeIndexToDataMap;
	OpenIntIntHashMap nodeIndexToLayoutIndex;
	double globalX;
	double globalY;
	double squared_size;

	CyNetwork network;
	
	public ISOMLayoutTask(
		final CyNetworkView networkView, final String name, final boolean selectedOnly,
		final Set<View<CyNode>> staticNodes, final int maxEpoch,
		final int radiusConstantTime, final int radius, final int minRadius,
		final double initialAdaptation, final double minAdaptation,
		final double sizeFactor, final double coolingFactor, final boolean singlePartition)
	{
		super(networkView, name, singlePartition, selectedOnly, staticNodes);
	
		this.maxEpoch = maxEpoch;
		this.radiusConstantTime = radiusConstantTime;
		this.radius = radius;
		this.minRadius = minRadius;
		this.initialAdaptation = initialAdaptation;
		this.minAdaptation = minAdaptation;
		this.sizeFactor = sizeFactor;
		this.coolingFactor = coolingFactor;
		
		network = networkView.getModel();
		q = new IntArrayList();
	}
	
	public void layoutPartion(LayoutPartition partition) {
		this.partition = partition;

		int nodeCount = partition.nodeCount();
		nodeIndexToDataMap = new OpenIntObjectHashMap(PrimeFinder.nextPrime(nodeCount));
		nodeIndexToLayoutIndex = new OpenIntIntHashMap(PrimeFinder.nextPrime(nodeCount));
		squared_size = network.getNodeCount() * sizeFactor;

		epoch = 1;

		adaption = initialAdaptation;

		System.out.println("Epoch: " + epoch + " maxEpoch: " + maxEpoch);

		while (epoch < maxEpoch) {
			partition.resetNodes();
			adjust();
			updateParameters();

			if (cancelled)
				break;
		}
	}

	/**
	 * @return the index of the closest NodeView to these coords.
	 */
	public int getClosestPosition(double x, double y) {
		double minDistance = Double.MAX_VALUE;
		int closest = 0;
		Iterator nodeIter = partition.nodeIterator();

		while (nodeIter.hasNext()) {
			LayoutNode node = (LayoutNode) nodeIter.next();
			int rootGraphIndex = node.getNode().getIndex();

			nodeIndexToLayoutIndex.put(rootGraphIndex, node.getIndex());

			double dx = node.getX();
			double dy = node.getY();
			double dist = (dx * dx) + (dy * dy);

			if (dist < minDistance) {
				minDistance = dist;
				closest = rootGraphIndex;
			}
		}

		return closest;
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void adjust() {
		//Generate random position in graph space
		ISOMVertexData tempISOM = new ISOMVertexData();

		// creates a new XY data location
		globalX = 10 + (Math.random() * squared_size);
		globalY = 10 + (Math.random() * squared_size);

		//Get closest vertex to random position
		int winner = getClosestPosition(globalX, globalY);

		Iterator nodeIter = partition.nodeIterator();

		while (nodeIter.hasNext()) {
			int nodeIndex = ((LayoutNode) nodeIter.next()).getNode().getIndex();
			ISOMVertexData ivd = getISOMVertexData(nodeIndex);
			ivd.distance = 0;
			ivd.visited = false;
		}

		adjustVertex(winner);
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void updateParameters() {
		epoch++;

		double factor = Math.exp(-1 * coolingFactor * ((1.0 * epoch) / maxEpoch));
		adaption = Math.max(minAdaptation, factor * initialAdaptation);

		if ((radius > minRadius) && ((epoch % radiusConstantTime) == 0)) {
			radius--;
		}
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param v DOCUMENT ME!
	 */
	public void adjustVertex(int v) {
		q.clear();

		ISOMVertexData ivd = getISOMVertexData(v);
		ivd.distance = 0;
		ivd.visited = true;
		q.add(v);

		int current;
		List<LayoutNode> nodeList = partition.getNodeList();

		while (!q.isEmpty()) {
			current = q.get(0);
			q.remove(0);

			int layoutIndex = nodeIndexToLayoutIndex.get(current);
			LayoutNode currentNode = (LayoutNode) nodeList.get(layoutIndex);

			ISOMVertexData currData = getISOMVertexData(current);

			double current_x = currentNode.getX();
			double current_y = currentNode.getY();

			double dx = globalX - current_x;
			double dy = globalY - current_y;

			// possible mod
			double factor = adaption / Math.pow(2, currData.distance);

			currentNode.setX(current_x + (factor * dx));
			currentNode.setY(current_y + (factor * dy));
			partition.moveNodeToLocation(currentNode);

			if (currData.distance < radius) {
				int[] neighbors = neighborsArray(network, currentNode.getNode());

				for (int neighbor_index = 0; neighbor_index < neighbors.length; ++neighbor_index) {
					ISOMVertexData childData = getISOMVertexData(neighbors[neighbor_index]);

					if (!childData.visited) {
						childData.visited = true;
						childData.distance = currData.distance + 1;
						q.add(neighbors[neighbor_index]);
					}
				}
			}
		}

		// Add check to make sure we don't put nodes on top of each other
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param v DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public ISOMVertexData getISOMVertexData(int v) {
		ISOMVertexData vd = (ISOMVertexData) nodeIndexToDataMap.get(v);

		if (vd == null) {
			vd = new ISOMVertexData();
			nodeIndexToDataMap.put(v, vd);
		}

		return vd;
	}

	public static class ISOMVertexData {
		public DoubleMatrix1D disp;
		int distance;
		boolean visited;

		public ISOMVertexData() {
			initialize();
		}

		public void initialize() {
			disp = new DenseDoubleMatrix1D(2);

			distance = 0;
			visited = false;
		}

		public double getXDisp() {
			return disp.get(0);
		}

		public double getYDisp() {
			return disp.get(1);
		}

		public void setDisp(double x, double y) {
			disp.set(0, x);
			disp.set(1, y);
		}

		public void incrementDisp(double x, double y) {
			disp.set(0, disp.get(0) + x);
			disp.set(1, disp.get(1) + y);
		}

		public void decrementDisp(double x, double y) {
			disp.set(0, disp.get(0) - x);
			disp.set(1, disp.get(1) - y);
		}
	}

	// This is here to replace the deprecated neighborsArray function
	/**
	 *  DOCUMENT ME!
	 *
	 * @param network DOCUMENT ME!
	 * @param nodeIndex DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public int[] neighborsArray(CyNetwork network, CyNode node) {
		// Get a list of edges
		List<CyNode> neighborList = network.getNeighborList(node, CyEdge.Type.ANY);
		int [] neighbors = new int[neighborList.size()];
		int offset = 0;
		for (CyNode n: neighborList){
			neighbors[offset++]=n.getIndex();
		}
		return neighbors;
	}

}
