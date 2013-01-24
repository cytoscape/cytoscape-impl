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
import org.cytoscape.work.undo.UndoSupport;

import cern.colt.list.tlong.LongArrayList;
import cern.colt.map.tobject.OpenLongObjectHashMap;
import cern.colt.map.PrimeFinder;
import cern.colt.map.tlong.OpenLongIntHashMap;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;

public class ISOMLayoutTask  extends AbstractPartitionLayoutTask {

	private int epoch;
	private double adaption;
	private LayoutPartition partition;

	//Queue, First In First Out, use add() and get(0)/remove(0)
	private LongArrayList q;
	OpenLongObjectHashMap nodeIndexToDataMap;
	OpenLongIntHashMap nodeIndexToLayoutIndex;
	double globalX;
	double globalY;
	double squared_size;

	CyNetwork network;
	private ISOMLayoutContext context;
	
	public ISOMLayoutTask(final String displayName, CyNetworkView networkView, Set<View<CyNode>> nodesToLayOut, ISOMLayoutContext context, String attrName, UndoSupport undo) {
		super(displayName, context.singlePartition, networkView, nodesToLayOut, attrName, undo);
		this.context = context;
		network = networkView.getModel();
		q = new LongArrayList();
	}
	
	public void layoutPartition(LayoutPartition partition) {
		this.partition = partition;

		int nodeCount = partition.nodeCount();
		nodeIndexToDataMap = new OpenLongObjectHashMap(PrimeFinder.nextPrime(nodeCount));
		nodeIndexToLayoutIndex = new OpenLongIntHashMap(PrimeFinder.nextPrime(nodeCount));
		squared_size = network.getNodeCount() * context.sizeFactor;

		epoch = 1;

		adaption = context.initialAdaptation;

		System.out.println("Epoch: " + epoch + " maxEpoch: " + context.maxEpoch);

		while (epoch < context.maxEpoch) {
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
	public long getClosestPosition(double x, double y) {
		double minDistance = Double.MAX_VALUE;
		long closest = 0;
		Iterator nodeIter = partition.nodeIterator();

		while (nodeIter.hasNext()) {
			LayoutNode node = (LayoutNode) nodeIter.next();
			long rootGraphIndex = node.getNode().getSUID();

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
		long winner = getClosestPosition(globalX, globalY);

		Iterator nodeIter = partition.nodeIterator();

		while (nodeIter.hasNext()) {
			long nodeIndex = ((LayoutNode) nodeIter.next()).getNode().getSUID();
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

		double factor = Math.exp(-1 * context.coolingFactor * ((1.0 * epoch) / context.maxEpoch));
		adaption = Math.max(context.minAdaptation, factor * context.initialAdaptation);

		if ((context.radius > context.minRadius) && ((epoch % context.radiusConstantTime) == 0)) {
			context.radius--;
		}
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param v DOCUMENT ME!
	 */
	public void adjustVertex(long v) {
		q.clear();

		ISOMVertexData ivd = getISOMVertexData(v);
		ivd.distance = 0;
		ivd.visited = true;
		q.add(v);

		long current;
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

			if (currData.distance < context.radius) {
				long[] neighbors = neighborsArray(network, currentNode.getNode());

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
	public ISOMVertexData getISOMVertexData(long v) {
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
	public long[] neighborsArray(CyNetwork network, CyNode node) {
		// Get a list of edges
		List<CyNode> neighborList = network.getNeighborList(node, CyEdge.Type.ANY);
		long [] neighbors = new long[neighborList.size()];
		int offset = 0;
		for (CyNode n: neighborList){
			neighbors[offset++]=n.getSUID();
		}
		return neighbors;
	}

}
