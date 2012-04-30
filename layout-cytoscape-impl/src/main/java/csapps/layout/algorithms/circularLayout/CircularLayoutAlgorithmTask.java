package csapps.layout.algorithms.circularLayout;


import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractPartitionLayoutTask;
import org.cytoscape.view.layout.LayoutEdge;
import org.cytoscape.view.layout.LayoutNode;
import org.cytoscape.view.layout.LayoutPartition;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.undo.UndoSupport;

import csapps.layout.algorithms.hierarchicalLayout.Edge;
import csapps.layout.algorithms.hierarchicalLayout.Graph;


public class CircularLayoutAlgorithmTask extends AbstractPartitionLayoutTask {
	
	private int[][] bc;
	private boolean[] posSet;
	private boolean[] depthPosSet;
	private Map<Integer, Integer> nodeHeights;
	private List<Integer>[] edgesFrom;
	private Map<Integer, View<CyNode>> nodeViews;
	private Map<Integer, Integer> node2BiComp;
	private boolean[] drawnBiComps;


	public CircularLayoutAlgorithmTask(final String name, CyNetworkView networkView, Set<View<CyNode>> nodesToLayOut, CircularLayoutContext context, UndoSupport undo) {
		super(name, context.singlePartition, networkView, nodesToLayOut,"", undo);
	}


	@Override
	public void layoutPartion(LayoutPartition partition) {
		if (cancelled)
			return;

		final int numNodes = partition.nodeCount();

		if (numNodes == 1) {
			// We were asked to do a circular layout of a single node -- done!
			return;
		}

		nodeViews = new HashMap<Integer, View<CyNode>>(numNodes);

		Map<CyNode, Integer> nodeIdexMap = new HashMap<CyNode, Integer>();
		int nodeIndex = 0;

		Iterator<LayoutNode> nodeIter = partition.getNodeList().iterator();
		while (nodeIter.hasNext() && !cancelled) {
			final View<CyNode> nv = nodeIter.next().getNodeView();
			nodeViews.put(nodeIndex, nv);
			nodeIdexMap.put(nv.getModel(), nodeIndex);
			nodeIndex++;
		}

		if (cancelled)
			return;

		/* create edge list from edges between selected nodes */
		final List<Edge> edges = new LinkedList<Edge>();
		final Iterator<LayoutEdge> edgeIter = partition.edgeIterator();
		while (edgeIter.hasNext() && !cancelled) {
			final LayoutEdge ev = edgeIter.next();
			final Integer edgeFrom = nodeIdexMap.get(ev.getEdge().getSource());
			final Integer edgeTo = nodeIdexMap.get(ev.getEdge().getTarget());

			if ((edgeFrom == null) || (edgeTo == null))
				continue;
			
			edges.add(new Edge(edgeFrom, edgeTo));
			edges.add(new Edge(edgeTo, edgeFrom));
		}
		nodeIdexMap.clear();
		nodeIdexMap = null;
		if (cancelled)
			return;

		/* find horizontal and vertical coordinates of each node */
		final Edge[] edge = new Edge[edges.size()];
		edges.toArray(edge);

		final Graph graph = new Graph(numNodes, edge);

		if (cancelled)
			return;

		posSet = new boolean[nodeViews.size()]; // all false
		depthPosSet = new boolean[nodeViews.size()]; // all false
		
		bc = graph.biconnectedComponents();

		int maxSize = -1;
		int maxIndex = -1;

		for (int i = 0; i < bc.length; i++)
			if (bc[i].length > maxSize) {
				maxSize = bc[i].length;
				maxIndex = i;
			}

		if (maxIndex == -1)
			return;

		if (cancelled)
			return;

		drawnBiComps = new boolean[bc.length];
		node2BiComp = new HashMap<Integer, Integer>();

		for (int i = 0; i < bc.length; i++)
			if (bc[i].length > 3) {
				for (int j = 0; j < bc[i].length; j++) {
					node2BiComp.put(bc[i][j], i);
				}
			}

		final double radius = (48 * maxSize) / (2 * Math.PI);
		final double deltaAngle = (2 * Math.PI) / maxSize;
		double angle = 0;

		int startX = (int) radius;
		int startY = (int) radius;

		edgesFrom = graph.GetEdgesFrom();

		// sorting nodes on inner circle
		bc[maxIndex] = SortInnerCircle(bc[maxIndex]);

		// setting nodes on inner circle
		for (int i = 0; i < bc[maxIndex].length; i++) {
			setOffset(nodeViews.get(bc[maxIndex][i]), 
					startX + (Math.cos(angle) * radius), 
					startY - (Math.sin(angle) * radius));
			posSet[bc[maxIndex][i]] = true;

			angle += deltaAngle;
		}

		drawnBiComps[maxIndex] = true;

		nodeHeights = new HashMap<Integer, Integer>();

		SetOuterCircle(maxIndex, radius, startX, startY, -1);

		if (cancelled)
			return;

		nodeIter = partition.nodeIterator();

		while (nodeIter.hasNext() && !cancelled) {
			final LayoutNode ln = nodeIter.next();
			final View<CyNode> nv = ln.getNodeView();
			ln.setX(nv.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION));
			ln.setY(nv.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION));
			partition.moveNodeToLocation(ln);
		}
	}


	/**
	 * Function which sets the first neighbours of nodes from circle (biconnected component)
	 * on the concentric circle (larger then the first circle).
	 * @param compIndex - index of that biconnected component in array bc
	 * @param innerCircleRadius - radius of the inner cicrle
	 * @param startX - start X position for drawing
	 * @param startY - start Y position for drawing
	 * @param firstTouched - node from that component which is found first
	 */
	private void SetOuterCircle(int compIndex, double innerCircleRadius, double startX,
	                            double startY, int firstTouched) {
		int outerNodesCount = 0;
		int rnc = 0;
		Iterator<Integer> iter;
		Map<Integer, Integer> outerCircle = new HashMap<Integer, Integer>();

		for (int i = 0; i < bc[compIndex].length; i++) {
			iter = edgesFrom[bc[compIndex][i]].iterator();

			while (iter.hasNext()) {
				int currNeighbour = iter.next();

				if (!posSet[currNeighbour]) {
					outerNodesCount += (NoOfChildren(currNeighbour, outerCircle) + 1);
					outerCircle.put(Integer.valueOf(currNeighbour), Integer.valueOf(0));
					rnc++;
				}
			}
		}

		double outerRadius = 1.5 * innerCircleRadius;

		// + 5 * nodeHorizontalSpacing;
		int tryCount = (int) ((2 * Math.PI * outerRadius) / 32);
		double outerDeltaAngle = (2 * Math.PI) / tryCount;

		if (tryCount < (1.2 * outerNodesCount)) {
			outerRadius = (1.2 * 32 * outerNodesCount) / (2 * Math.PI);
			outerDeltaAngle = (2 * Math.PI) / (1.2 * outerNodesCount);
			outerNodesCount *= 1.2;
		} else
			outerNodesCount = tryCount;

		if ((outerNodesCount > 10) && (firstTouched != -1))
			outerNodesCount += 5;

		// 5 places on outer circle for connection with other biconn. comp.
		//System.out.println("tryCount = " + tryCount);

		// setting nodes on outer circle
		int[] outerPositionsTaken = new int[outerNodesCount];
		int[] outerPositionsOwners = new int[outerNodesCount];

		for (int i = 0; i < outerPositionsTaken.length; i++) {
			outerPositionsTaken[i] = -1;
			outerPositionsOwners[i] = -1;
		}

		double pointX;
		double pointY;
		double theAngle;
		double theAngleHlp;
		double innerDeltaAngle;
		innerDeltaAngle = (2 * Math.PI) / bc[compIndex].length;

		if (firstTouched != -1) {
			View<CyNode> view = nodeViews.get(firstTouched);
			pointX = view.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
			pointY = view.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
			theAngle = Math.asin((startY - pointY) / Math.sqrt(((pointX - startX) * (pointX
			                                                                        - startX))
			                                                   + ((pointY - startY) * (pointY
			                                                                          - startY))));
			theAngleHlp = Math.acos((pointX - startX) / Math.sqrt(((pointX - startX) * (pointX
			                                                                           - startX))
			                                                      + ((pointY - startY) * (pointY
			                                                                             - startY))));

			if (theAngleHlp > (Math.PI / 2))
				theAngle = Math.PI - theAngle;

			if (theAngle < 0)
				theAngle += (2 * Math.PI);

			int idPos = ((int) (theAngle / outerDeltaAngle)) % outerPositionsTaken.length;
			outerPositionsTaken[idPos] = (int) (theAngle / innerDeltaAngle);
			outerPositionsOwners[idPos] = -2; // must not be even moved because that node is coming from another bicomp.

			if (outerPositionsTaken.length > 10) {
				outerPositionsTaken[(idPos + 1) % outerPositionsTaken.length] = (int) (theAngle / innerDeltaAngle);
				outerPositionsTaken[(idPos + 2) % outerPositionsTaken.length] = (int) (theAngle / innerDeltaAngle);
				outerPositionsTaken[(idPos - 1 + outerPositionsTaken.length) % outerPositionsTaken.length] = (int) (theAngle / innerDeltaAngle);
				outerPositionsTaken[(idPos - 2 + outerPositionsTaken.length) % outerPositionsTaken.length] = (int) (theAngle / innerDeltaAngle);

				outerPositionsOwners[(idPos + 1) % outerPositionsOwners.length] = -2;
				outerPositionsOwners[(idPos + 2) % outerPositionsOwners.length] = -2;
				outerPositionsOwners[(idPos - 1 + outerPositionsOwners.length) % outerPositionsOwners.length] = -2;
				outerPositionsOwners[(idPos - 2 + outerPositionsOwners.length) % outerPositionsOwners.length] = -2;
			}
		}

		HashMap<Integer, Integer> addedNeighbours = new HashMap<Integer, Integer>();

		for (int i = 0; i < bc[compIndex].length; i++) {
			iter = edgesFrom[bc[compIndex][i]].iterator();

			int currentNeighbour;
			int noOfNeighbours = 0;

			while (iter.hasNext()) {
				currentNeighbour = ((Integer) iter.next()).intValue();

				if (!posSet[currentNeighbour]) {
					noOfNeighbours += (NoOfChildren(currentNeighbour, addedNeighbours) + 1);
					addedNeighbours.put(Integer.valueOf(currentNeighbour), Integer.valueOf(0));
				}
			}

			if (noOfNeighbours == 0)
				continue;

			pointX = nodeViews.get(bc[compIndex][i]).getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
			pointY = nodeViews.get(bc[compIndex][i]).getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);

			theAngle = Math.asin((startY - pointY) / Math.sqrt(((pointX - startX) * (pointX
			                                                                        - startX))
			                                                   + ((pointY - startY) * (pointY
			                                                                          - startY))));
			theAngleHlp = Math.acos((pointX - startX) / Math.sqrt(((pointX - startX) * (pointX
			                                                                           - startX))
			                                                      + ((pointY - startY) * (pointY
			                                                                             - startY))));

			if (theAngleHlp > (Math.PI / 2))
				theAngle = Math.PI - theAngle;

			if (theAngle < 0)
				theAngle += (2 * Math.PI);

			iter = edgesFrom[bc[compIndex][i]].iterator();

			int startPos = BestFreePositionsForAll((int) ((theAngle / outerDeltaAngle)
			                                       - (noOfNeighbours / 2.0)), outerPositionsTaken,
			                                       outerPositionsOwners, noOfNeighbours,
			                                       (int) (theAngle / innerDeltaAngle), startX,
			                                       startY, outerDeltaAngle, outerRadius,
			                                       bc[compIndex].length);
			double startAngle = startPos * outerDeltaAngle;

			if (startAngle < 0)
				continue;

			iter = edgesFrom[bc[compIndex][i]].iterator();

			while (iter.hasNext()) {
				currentNeighbour = ((Integer) iter.next()).intValue();

				if (!posSet[currentNeighbour]) {
					posSet[currentNeighbour] = true;

					int holeDepth = NoOfChildren(currentNeighbour, addedNeighbours);

					for (int j = 0; j < (holeDepth / 2); j++) {
						outerPositionsOwners[(startPos) % outerPositionsOwners.length] = -3;
						// free but it must not be used (add. space for tree-like struct.)
						outerPositionsTaken[(startPos) % outerPositionsOwners.length] = (int) (theAngle / innerDeltaAngle);
						startPos++;
						startAngle += outerDeltaAngle;

						if (startAngle > (2 * Math.PI))
							startAngle -= (2 * Math.PI);
					}

					setOffset(nodeViews.get(currentNeighbour), startX + (Math.cos(startAngle) * outerRadius),
					                                     	   startY - (Math.sin(startAngle) * outerRadius));
					outerPositionsOwners[(startPos) % outerPositionsOwners.length] = currentNeighbour;
					outerPositionsTaken[(startPos) % outerPositionsOwners.length] = (int) (theAngle / innerDeltaAngle);
					startPos++;
					startAngle += outerDeltaAngle;

					if (startAngle > (2 * Math.PI))
						startAngle -= (2 * Math.PI);

					for (int j = 0; j < (holeDepth / 2); j++) {
						outerPositionsOwners[(startPos) % outerPositionsOwners.length] = -3;
						outerPositionsTaken[(startPos) % outerPositionsOwners.length] = (int) (theAngle / innerDeltaAngle);
						startPos++;
						startAngle += outerDeltaAngle;

						if (startAngle > (2 * Math.PI))
							startAngle -= (2 * Math.PI);
					}
				}
			}
		}

		// laying out the rest of nodes
		for (int i = 0; i < bc[compIndex].length; i++) {
			iter = edgesFrom[bc[compIndex][i]].iterator();

			int currentNeighbour;

			while (iter.hasNext()) {
				currentNeighbour = ((Integer) iter.next()).intValue();

				if (!addedNeighbours.containsKey(Integer.valueOf(currentNeighbour))) {
					continue;
				}

				View<CyNode> view = nodeViews.get(currentNeighbour);
				pointX = view.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
				pointY = view.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);

				theAngle = Math.asin((startY - pointY) / Math.sqrt(((pointX - startX) * (pointX
				                                                                        - startX))
				                                                   + ((pointY - startY) * (pointY
				                                                                          - startY))));
				theAngleHlp = Math.acos((pointX - startX) / Math.sqrt(((pointX - startX) * (pointX
				                                                                           - startX))
				                                                      + ((pointY - startY) * (pointY
				                                                                             - startY))));

				if (theAngleHlp > (Math.PI / 2))
					theAngle = Math.PI - theAngle;

				if (theAngle < 0)
					theAngle += (2 * Math.PI);

				for (int j = 0; j < posSet.length; j++)
					depthPosSet[j] = posSet[j];

				EachNodeHeight(currentNeighbour);

				DFSSetPos(currentNeighbour, theAngle, outerRadius - innerCircleRadius);
			}
		}
	}

	/**
	 * Returns number of children of the specified node from outer circle.
	 * If number of children larger than 7 return 7.
	 * @param nodeID
	 * @param outerCircle
	 * @return
	 */
	private int NoOfChildren(int nodeID, Map<Integer, Integer> outerCircle) {
		int toReturn = 0;
		Iterator iter = edgesFrom[nodeID].iterator();

		while (iter.hasNext()) {
			int currNeigh = ((Integer) iter.next()).intValue();

			if (!posSet[currNeigh] && !outerCircle.containsKey(currNeigh))
				toReturn++;
		}

		if (toReturn > 7)
			return 7;

		return toReturn;
	}

	/**
	 * Sort the nodes from biconnected component to get the best ordering in terms
	 * of tree-like neighbouring patterns
	 * @param icNodes - nodes from biconnected component
	 * @return
	 */
	private int[] SortInnerCircle(int[] icNodes) {
		LinkedList<Integer> greedyNodes = new LinkedList<Integer>();
		LinkedList<Integer> modestNodes = new LinkedList<Integer>();

		HashMap<Integer, Integer> forFunct = new HashMap<Integer, Integer>();

		for (int i = 0; i < icNodes.length; i++)
			forFunct.put(Integer.valueOf(icNodes[i]), Integer.valueOf(0));

		for (int i = 0; i < icNodes.length; i++) {
			int tmp = NoOfChildren(icNodes[i], forFunct);

			if (tmp > 4)
				greedyNodes.add(Integer.valueOf(icNodes[i]));
			else
				modestNodes.add(Integer.valueOf(icNodes[i]));
		}

		int[] toReturn = new int[icNodes.length];
		int gNo = greedyNodes.size();
		int mNo = modestNodes.size();
		int deltaM;
		int deltaG;

		if (gNo == 0) {
			deltaM = mNo;
			deltaG = 0;
		} else if (mNo == 0) {
			deltaG = gNo;
			deltaM = 0;
		} else if (gNo > mNo) {
			deltaM = 1;
			deltaG = gNo / mNo;
		} else {
			deltaG = 1;
			deltaM = mNo / gNo;
		}

		int x = 0;
		Iterator iterM = modestNodes.iterator();
		Iterator iterG = greedyNodes.iterator();

		while (iterM.hasNext() && iterG.hasNext()) {
			for (int i = 0; i < deltaG; i++)
				toReturn[x++] = ((Integer) iterG.next()).intValue();

			for (int i = 0; i < deltaM; i++)
				toReturn[x++] = ((Integer) iterM.next()).intValue();
		}

		while (iterG.hasNext())
			toReturn[x++] = ((Integer) iterG.next()).intValue();

		while (iterM.hasNext())
			toReturn[x++] = ((Integer) iterM.next()).intValue();

		return toReturn;
	}


	/**
	 * Function traverses graph starting from the node from outer circle until
	 * it traverse all the nodes. When it comes along another biconnected component
	 * it sets it out on circle and calls SetOuterCircle() again. The main purpose of
	 * the function is setting the node positions of tree-like parts of graph.
	 * @param nodeID - ID of the node from which we start DFS
	 * @param theAngle - the angle at which we "enter" the node, using it we can calculate
	 *                     at which position to set the node
	 * @param theRadius - this will represent the distance between the parent of the node and
	 *                     the child in tree-like parts
	 */
	private void DFSSetPos(int nodeID, double theAngle, double theRadius) {
		Integer component = node2BiComp.get(Integer.valueOf(nodeID));
		if (component != null && !drawnBiComps[component]) {
			int comp = node2BiComp.get(Integer.valueOf(nodeID)).intValue();
			View<CyNode> view = nodeViews.get(nodeID);
			double centerX = view.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
			double centerY = view.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
			double radius = (48 * bc[comp].length) / (2 * Math.PI);
			double deltaAngle = (2 * Math.PI) / bc[comp].length;
			double currAngle = theAngle - Math.PI - deltaAngle;

			if (currAngle < 0)
				currAngle += (2 * Math.PI);

			centerX += (Math.cos(theAngle) * radius * 4.0);
			centerY -= (Math.sin(theAngle) * radius * 4.0);

			drawnBiComps[comp] = true;

			// sorting nodes on inner circle
			bc[comp] = SortInnerCircle(bc[comp]);

			/*if (bc[comp].length > 20)
			    bc[comp] = ReduceInnerCircleCrossings(bc[comp]);*/
			boolean oneAtLeast = false;

			for (int i = 0; i < bc[comp].length; i++) {
				if (posSet[bc[comp][i]])
					continue;

				setOffset(nodeViews.get(bc[comp][i]), centerX + (Math.cos(currAngle) * radius),
				                                      centerY - (Math.sin(currAngle) * radius));
				posSet[bc[comp][i]] = true;

				oneAtLeast = true;
				currAngle -= deltaAngle;

				if (currAngle < 0)
					currAngle += (2 * Math.PI);
			}

			if (oneAtLeast) {
				setOffset(nodeViews.get(nodeID),
						  nodeViews.get(nodeID).getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION) + (Math.cos(theAngle) * 3 * radius),
				          nodeViews.get(nodeID).getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION) - (Math.sin(theAngle) * 3 * radius));

				SetOuterCircle(comp, radius, centerX, centerY, nodeID);
			}
		} else {
			Iterator iter = edgesFrom[nodeID].iterator();
			int currentNeighbour;
			double startAngle = theAngle + (Math.PI / 2);

			if (startAngle > (2 * Math.PI))
				startAngle -= (2 * Math.PI);

			int neighboursCount = 0;
			int min1 = 1000;
			int min2 = 1000;
			int max = -1;
			int min1Id = -1;
			int min2Id = -2;
			int maxId = -3;
			HashMap<Integer, Integer> tmp = new HashMap<Integer, Integer>();

			while (iter.hasNext()) {
				currentNeighbour = ((Integer) iter.next()).intValue();

				if (!posSet[currentNeighbour] && !tmp.containsKey(Integer.valueOf(currentNeighbour))) {
					neighboursCount++;
					tmp.put(Integer.valueOf(currentNeighbour), Integer.valueOf(0));

					if (nodeHeights.get(Integer.valueOf(currentNeighbour)).intValue() < min1) {
						min2 = min1;
						min2Id = min1Id;
						min1 = nodeHeights.get(Integer.valueOf(currentNeighbour)).intValue();
						min1Id = currentNeighbour;
					} else if (nodeHeights.get(Integer.valueOf(currentNeighbour)).intValue() < min2) {
						min2 = nodeHeights.get(Integer.valueOf(currentNeighbour)).intValue();
						min2Id = currentNeighbour;
					}

					if (nodeHeights.get(Integer.valueOf(currentNeighbour)).intValue() >= max)//&& currentNeighbour != min2Id && currentNeighbour != min1Id)
					 {
						max = nodeHeights.get(Integer.valueOf(currentNeighbour)).intValue();
						maxId = currentNeighbour;
					}
				}
			}

			if (neighboursCount == 0)
				return;

			double deltaAngle = Math.PI / (neighboursCount + 1);

			startAngle -= deltaAngle;

			if (startAngle < 0)
				startAngle += (2 * Math.PI);

			double remStartAngle = startAngle;

			if (neighboursCount > 2) {
				deltaAngle = (2 * Math.PI) / neighboursCount;
				startAngle = (theAngle + Math.PI) - ((3 * deltaAngle) / 2);

				if (startAngle > (2 * Math.PI))
					startAngle -= (2 * Math.PI);

				remStartAngle = (theAngle + Math.PI) - (deltaAngle / 2);

				if (remStartAngle > (2 * Math.PI))
					remStartAngle -= (2 * Math.PI);
			}

			iter = edgesFrom[nodeID].iterator();

			double r = 72;
			double rTry;

			if (((48 * neighboursCount) / (2 * Math.PI)) > r)
				r = (48 * neighboursCount) / (2 * Math.PI);

			rTry = r;

			double hlp = 100.0;
			double startX = nodeViews.get(nodeID).getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
			double startY = nodeViews.get(nodeID).getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);

			if (neighboursCount > 2) {
				setOffset(nodeViews.get(nodeID), startX + (Math.cos(theAngle) * r * ((min2 + 1) % 100)),
				                                 startY - (Math.sin(theAngle) * r * ((min2 + 1) % 100)));
				startX = nodeViews.get(nodeID).getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
				startY = nodeViews.get(nodeID).getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);

				//System.out.println("theAngle = " + theAngle + ", startAngle = " + startAngle + ", remStartAngle = " + remStartAngle + ", deltaAngle = " + deltaAngle);
				//System.out.println("min1Id = " + min1Id + ", min2Id" + min2Id + ", maxId" + maxId);
				setOffset(nodeViews.get(min1Id), startX + (Math.cos(remStartAngle) * r),
				                                 startY - (Math.sin(remStartAngle) * r));
				setOffset(nodeViews.get(min2Id), startX + (Math.cos(remStartAngle + deltaAngle) * r),
				                                 startY - (Math.sin(remStartAngle + deltaAngle) * r));

				if (nodeHeights.get(Integer.valueOf(maxId)).intValue() > 8)
					r = 256;

				setOffset(nodeViews.get(maxId),
						  startX + (Math.cos(remStartAngle - ((neighboursCount / 2) * deltaAngle)) * r),
				          startY - (Math.sin(remStartAngle - ((neighboursCount / 2) * deltaAngle)) * r));
				//System.out.println("Ugao za maxID "
				 //                  + (remStartAngle - ((neighboursCount / 2) * deltaAngle)));
			}

			tmp = new HashMap<Integer, Integer>();

			while (iter.hasNext()) {
				currentNeighbour = ((Integer) iter.next()).intValue();

				if (!posSet[currentNeighbour] && !tmp.containsKey(Integer.valueOf(currentNeighbour))) {
					if (nodeHeights.get(Integer.valueOf(currentNeighbour)).intValue() > 8)
						r = 256;
					else
						r = rTry;

					posSet[currentNeighbour] = true;
					tmp.put(Integer.valueOf(currentNeighbour), Integer.valueOf(0));

					if (((currentNeighbour != min1Id) && (currentNeighbour != min2Id)
					    && (currentNeighbour != maxId)) || (neighboursCount <= 2)) {
						setOffset(nodeViews.get(currentNeighbour), startX + (Math.cos(startAngle) * r),
						                                           startY - (Math.sin(startAngle) * r));

						startAngle -= deltaAngle;

						if (startAngle < 0)
							startAngle += (2 * Math.PI);

						if (((Math.abs(startAngle
						               - (remStartAngle - ((neighboursCount / 2) * deltaAngle))) < 0.0001)
						    || (Math.abs(startAngle
						                 - (remStartAngle - ((neighboursCount / 2) * deltaAngle)
						                   + (2 * Math.PI))) < 0.0001)) && (neighboursCount > 2)) {
							startAngle -= deltaAngle;

							if (startAngle < 0)
								startAngle += (2 * Math.PI);
						}
					}
				}
			}

			iter = edgesFrom[nodeID].iterator();

			if (neighboursCount > 2) {
				DFSSetPos(min1Id, remStartAngle, theRadius * Math.sin(deltaAngle / 2));
				DFSSetPos(min2Id, remStartAngle + deltaAngle, theRadius * Math.sin(deltaAngle / 2));
				DFSSetPos(maxId, remStartAngle - ((neighboursCount / 2) * deltaAngle),
				          theRadius * Math.sin(deltaAngle / 2));
				hlp = remStartAngle;
				remStartAngle -= deltaAngle;
			}

			while (iter.hasNext()) {
				currentNeighbour = ((Integer) iter.next()).intValue();

				if (tmp.containsKey(Integer.valueOf(currentNeighbour))) {
					if (((currentNeighbour != min1Id) && (currentNeighbour != min2Id)
					    && (currentNeighbour != maxId)) || (neighboursCount <= 2)) {
						DFSSetPos(currentNeighbour, remStartAngle,
						          theRadius * Math.sin(deltaAngle / 2));

						remStartAngle -= deltaAngle;

						if (((remStartAngle == (hlp - ((neighboursCount / 2) * deltaAngle)))
						    || (remStartAngle == (hlp - ((neighboursCount / 2) * deltaAngle)
						                         + (2 * Math.PI)))) && (neighboursCount > 2))
							startAngle -= deltaAngle;

						if (remStartAngle < 0)
							remStartAngle += (2 * Math.PI);
					}
				}
			}
		}
	}

	/**
	 * Heuristic function which estimates the number of nodes "after" the given node.
	 * Using it we can estimate the distance from this node to his children.
	 * @param nodeID - ID of given node
	 * @return
	 */
	private int EachNodeHeight(int nodeID) {
		Iterator iter = edgesFrom[nodeID].iterator();
		int currentNeighbour;
		int noOfChildren = 0;
		HashMap<Integer, Integer> tmp = new HashMap<Integer, Integer>();

		while (iter.hasNext()) {
			currentNeighbour = ((Integer) iter.next()).intValue();

			if (!depthPosSet[currentNeighbour] && !tmp.containsKey(Integer.valueOf(currentNeighbour))) {
				depthPosSet[currentNeighbour] = true;
				tmp.put(Integer.valueOf(currentNeighbour), Integer.valueOf(0));
			}
		}

		iter = edgesFrom[nodeID].iterator();

		while (iter.hasNext()) {
			currentNeighbour = ((Integer) iter.next()).intValue();

			if (tmp.containsKey(Integer.valueOf(currentNeighbour))) {
				noOfChildren += EachNodeHeight(currentNeighbour);
			}
		}

		if (nodeHeights.containsKey(Integer.valueOf(nodeID)))
			nodeHeights.remove(Integer.valueOf(nodeID));

		nodeHeights.put(Integer.valueOf(nodeID), Integer.valueOf(noOfChildren));

		return (noOfChildren + 1);
	}

	/**
	 * Founds best positions for nodes from outer cicrle, according to inner circle.
	 * We avoid crossings of edges between inner and outer circle, and we want to minimize
	 * the length of that edges.
	 * @param idealPosition - according to position of neighbour node from inner circle
	 * @param outerPositionsTaken - array of availability of positions on second circle
	 * @param outerPositionsOwners - array of owners (from inner cicrle) of positions on second circle
	 * @param noOfPos - number of positions that we need
	 * @param innerCirclePos - owner (parent, neighbour from inner cicrle) of given node
	 * @param startX
	 * @param startY
	 * @param outerDeltaAngle
	 * @param outerRadius
	 * @param innerCSize
	 * @return
	 */
	private int BestFreePositionsForAll(int idealPosition, int[] outerPositionsTaken,
	                                    int[] outerPositionsOwners, int noOfPos,
	                                    int innerCirclePos, double startX, double startY,
	                                    double outerDeltaAngle, double outerRadius, int innerCSize) {
//		for (int j = 0; j < outerPositionsTaken.length; j++)
//			System.out.print(outerPositionsTaken[j] + " ");

//		System.out.println("innerCircPos: " + innerCirclePos + ", noOfPos: " + noOfPos
//		                   + ", idealPos: " + idealPosition);

		int startPos = idealPosition;

		if (idealPosition < 0)
			startPos += outerPositionsTaken.length;

		int i = 0;
		int alreadyFound = 0;
		int startOfAlFound = -1;
		boolean found = false;
		boolean goDown = false;
		boolean goUp = false;

		while (!found && !(goUp && goDown)) {
			//System.out.print(startPos + " ");
			for (i = startPos;
			     (i < (startPos + noOfPos))
			     && (outerPositionsTaken[i % outerPositionsTaken.length] == -1); i++) {
			}

			if (i < (startPos + noOfPos)) {
				if (((outerPositionsTaken[i % outerPositionsTaken.length] > innerCirclePos)
				    && ((outerPositionsTaken[i % outerPositionsTaken.length] - innerCirclePos) < (0.7 * innerCSize)))
				    || ((innerCirclePos - outerPositionsTaken[i % outerPositionsTaken.length]) > (0.7 * innerCSize))) {
					alreadyFound = (i - startPos + outerPositionsTaken.length) % outerPositionsTaken.length;
					startOfAlFound = startPos;
					startPos -= (noOfPos - alreadyFound);

					if (startPos < 0)
						startPos += outerPositionsTaken.length;

					goDown = true;
				} else {
					startPos = (i + 1) % outerPositionsTaken.length;
					goUp = true;
				}
			} else
				found = true;
		}

		if (goUp && goDown) {
			i = startOfAlFound - 1;

			int j = i - 1;
			int count = 0;
			//System.out.print(j + " ");

			int index = (i % outerPositionsTaken.length + outerPositionsTaken.length) % outerPositionsTaken.length;
			if (((outerPositionsTaken[index] > innerCirclePos)
			    && ((outerPositionsTaken[index] - innerCirclePos) < (0.7 * innerCSize)))
			    || ((innerCirclePos - outerPositionsTaken[index]) > (0.7 * innerCSize))) {
				j--;
				i--;
			}

			while (count < (noOfPos - alreadyFound)) {
				//System.out.print(j + " ");

				if (outerPositionsTaken[(j + outerPositionsTaken.length) % outerPositionsTaken.length] == -1) {
					// move all for one place left
					//	System.out.print(" moving ");
					if (outerPositionsOwners[(j + outerPositionsTaken.length) % outerPositionsTaken.length] == -2) {
						//System.out.println("BUUUUUUUUUUUUUUUUUUU");

						return -1;
					}

					for (int k = j; k < (i - count); k++) {
						if (outerPositionsOwners[(k + 1 + outerPositionsTaken.length) % outerPositionsTaken.length] > 0)
							setOffset(nodeViews.get(outerPositionsOwners[(k + 1 + outerPositionsTaken.length) % outerPositionsTaken.length]),
									  startX + (Math.cos(outerDeltaAngle * k) * outerRadius),
							          startY - (Math.sin(outerDeltaAngle * k) * outerRadius));

						outerPositionsOwners[(k + outerPositionsTaken.length) % outerPositionsTaken.length] = outerPositionsOwners[(k + 1 + outerPositionsTaken.length) % outerPositionsTaken.length];
						outerPositionsTaken[(k + outerPositionsTaken.length) % outerPositionsTaken.length] = outerPositionsTaken[(k + 1 + outerPositionsTaken.length) % outerPositionsTaken.length];
					}

					count++;
				}

				j--;
			}

			startPos = (i - count + 1 + outerPositionsOwners.length) % outerPositionsOwners.length;
		}

		/*    for (i = startPos; i < startPos + noOfPos; i++)
		    {
		        outerPositionsTaken[i % outerPositionsTaken.length] = innerCirclePos;
		    }*/
		return startPos;
	}
	

	private void setOffset(View<CyNode> nv, double x, double y){
		nv.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, x);
		nv.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, y);
	}
}
