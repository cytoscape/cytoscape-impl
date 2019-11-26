package csapps.layout.algorithms.hierarchicalLayout;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.AbstractLayoutTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.presentation.property.values.Handle;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

/*
 * #%L
 * Cytoscape Layout Algorithms Impl (layout-cytoscape-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

public class HierarchicalLayoutAlgorithmTask extends AbstractLayoutTask {

	private HashMap<Integer, HierarchyFlowLayoutOrderNode> nodes2HFLON = new HashMap<>();
	private final HierarchicalLayoutContext context;
	private final CyServiceRegistrar serviceRegistrar;
	
	/**
	 * Creates a new GridNodeLayout object.
	 */
	public HierarchicalLayoutAlgorithmTask(
			String displayName,
			CyNetworkView networkView,
			Set<View<CyNode>> nodesToLayOut,
			HierarchicalLayoutContext context,
			String attrName,
			UndoSupport undo,
			CyServiceRegistrar serviceRegistrar
	) {
		super(displayName, networkView, nodesToLayOut, attrName, undo);
		this.context = context;
		this.serviceRegistrar = serviceRegistrar;
	}


	/**
	 * Perform actual layout task. This creates the default square layout.<br>
	 * See this class' description for an outline of the method used.<br>
	 * For the last step, assembly of the layed out components, the method
	 * implemented is similar to the FlowLayout layout manager from the AWT.
	 * Space is allocated in horizontal bands, with a new band begun beneath
	 * the higher bands. This happens on two scales: on the component scale,
	 * and on the intra-component scale. Within each component, the layers
	 * are placed horizontally, each within its own band. Globally, each
	 * component appears in a band which is filled until a right margin is
	 * hit. After that a new band is started beneath the higher band of
	 * layed out components. Components are never split between these global
	 * bands. Each component is finished, regardless of its horizontal extent.<br>
	 * Also, a post placement pass is done on each component to move each
	 * layer horizontally in order to line up the centers of the layers with the center of the component.
	 */
	@Override
	protected void doLayout(TaskMonitor tm) {
		tm.setProgress(0.0);
		tm.setStatusMessage("Capturing snapshot of network and selected nodes...");

		if (cancelled)
			return;

		/* construct node list with selected nodes first */
		int numLayoutNodes = nodesToLayOut.size();

		if (numLayoutNodes == 1) // We were asked to do a hierchical layout of a single node -- done!
			return;

		HashMap<Long, Integer> suid2Index = new HashMap<>(numLayoutNodes);
		List<View<CyNode>> nodeViews = new ArrayList<>(nodesToLayOut);
		int index = 0;
		
		for (View<CyNode> view : nodeViews) {
			if (cancelled)
				return;
			
			CyNode node = view.getModel();
			Long suid = node.getSUID();
			suid2Index.put(suid, index);
			index++;
		}

		if (cancelled)
			return;

		/* create edge list from edges between selected nodes */
		LinkedList<Edge> edges = new LinkedList<>();
		
		for (var ev : networkView.getEdgeViewsIterable()) {
			if (cancelled)
				return;
			
		    // FIXME: much better would be to query adjacent edges of selected nodes...
		    
			Integer edgeFrom = suid2Index.get(ev.getModel().getSource().getSUID());
			Integer edgeTo = suid2Index.get(ev.getModel().getTarget().getSUID());

			if ((edgeFrom == null) || (edgeTo == null)) // Must be from an unselected node
				continue;

			if ((numLayoutNodes <= 1)
			    || ((edgeFrom < numLayoutNodes)
			       && (edgeTo < numLayoutNodes))) {
				/* add edge to graph */
				Edge theEdge = new Edge(edgeFrom, edgeTo);
				edges.add(theEdge);
			}
		}

		/* find horizontal and vertical coordinates of each node */
		Edge[] edge = new Edge[edges.size()];
		edges.toArray(edge);
		
		Graph graph = new Graph(numLayoutNodes, edge);
		
		/*
		int edgeIndex;
		for (edgeIndex = 0; edgeIndex<edge.length; edgeIndex++) {
		     System.out.println("Edge: " + edge[edgeIndex].getFrom() + " - " + edge[edgeIndex].getTo());
		}
		*/
		int[] cI = graph.componentIndex();
		int x;
		/*
		System.out.println("Node index:\n");
		for (x=0; x<graph.getNodecount(); x++) {
		    System.out.println(cI[x]);
		}
		System.out.println("Partitioning into components:\n");
		*/
		tm.setProgress(0.05);
		tm.setStatusMessage("Finding connected components...");

		if (cancelled)
			return;

		int[] renumber = new int[cI.length];
		Graph[] component = graph.partition(cI, renumber);
		final int numComponents = component.length;
		int[][] layer = new int[numComponents][];
		int[][] horizontalPosition = new int[numComponents][];
		Graph[] reduced = new Graph[component.length];
		Graph[] reducedTmp = new Graph[component.length];
		HashMap<Integer, Edge>[] dummy2Edge = new HashMap[component.length];
		int[] dummyStartForComp = new int[component.length];
		HashMap<Edge, View<CyEdge>>[] myEdges2EdgeViews = new HashMap[component.length];

		for (x = 0; x < component.length; x++) {
			/*
			System.out.println("plain component:\n");
			System.out.println(component[x]);
			System.out.println("filtered component:\n");
			System.out.println(component[x].getGraphWithoutOneOrTwoCycles());
			System.out.println("nonmulti component:\n");
			System.out.println(component[x].getGraphWithoutMultipleEdges());
			int cycleEliminationPriority[] = component[x].getCycleEliminationVertexPriority();
			System.out.println("acyclic component:\n");
			System.out.println(component[x].getGraphWithoutCycles(cycleEliminationPriority));
			System.out.println("reduced component:\n");
			System.out.println(component[x].getReducedGraph());
			System.out.println("layer assignment:\n");
			*/
			tm.setStatusMessage("Making acyclic transitive reduction...");
			Thread.yield();

			if (cancelled)
				return;

			reducedTmp[x] = component[x].getReducedGraph();
			tm.setStatusMessage("Layering nodes vertically...");
			Thread.yield();

			if (cancelled)
				return;

			layer[x] = reducedTmp[x].getVertexLayers();

			LinkedList<Integer> layerWithDummy = new LinkedList<>();

			for (int i = 0; i < layer[x].length; i++)
				layerWithDummy.add(Integer.valueOf(layer[x][i]));

			/*
			int y;
			for (y=0;y<layer[x].length;y++) {
			    System.out.println("" + y + " : " + layer[x][y]);
			}
			System.out.println("horizontal position:\n");
			*/

			/* Insertion of the dummy nodes in the graph */
			Edge[] allEdges = component[x].GetEdges();
			LinkedList<Edge> edgesWithAdd = new LinkedList<>();
			int dummyStart = component[x].getNodecount();
			dummyStartForComp[x] = dummyStart;
			dummy2Edge[x] = new HashMap<>();

			//System.out.println(allEdges.length);

			for (int i = 0; i < allEdges.length; i++) {
				if (cancelled)
					return;
				
				int from = allEdges[i].getFrom();
				int to = allEdges[i].getTo();

				if (layer[x][from] == (layer[x][to] + 1)) {
					edgesWithAdd.add(allEdges[i]);
				} else {
					if (layer[x][from] < layer[x][to]) {
						int tmp = from;
						from = to;
						to = tmp;
					}

					layerWithDummy.add(Integer.valueOf(layer[x][to] + 1));
					dummy2Edge[x].put(Integer.valueOf(layerWithDummy.size() - 1), allEdges[i]);
					edgesWithAdd.add(new Edge(layerWithDummy.size() - 1, to));

					for (int j = layer[x][to] + 2; j < layer[x][from]; j++) {
						layerWithDummy.add(Integer.valueOf(j));
						dummy2Edge[x].put(Integer.valueOf(layerWithDummy.size() - 1), allEdges[i]);
						edgesWithAdd.add(new Edge(layerWithDummy.size() - 1,
						                          layerWithDummy.size() - 2));
					}

					edgesWithAdd.add(new Edge(from, layerWithDummy.size() - 1));
				}
			}

			allEdges = new Edge[edgesWithAdd.size()];
			edgesWithAdd.toArray(allEdges);

			reduced[x] = new Graph(layerWithDummy.size(), allEdges);
			reduced[x].setDummyNodesStart(dummyStart);
			reduced[x].setReduced(true);

			int[] layerNew = new int[layerWithDummy.size()];
			Iterator<Integer> iter = layerWithDummy.iterator();

			for (int i = 0; i < layerNew.length; i++)
				layerNew[i] = iter.next();

			layer[x] = layerNew;

			tm.setStatusMessage("Positioning nodes within layer...");
			Thread.yield();

			if (cancelled)
				return;

			horizontalPosition[x] = reduced[x].getHorizontalPositionReverse(layer[x]);

			/*
			for (y=0;y<horizontalPosition[x].length;y++) {
			    System.out.println("" + y + " : " + horizontalPosition[x][y]);
			}
			*/
			setProgress(tm, .05f, x / (float) component.length, .15f);
		}
		
		tm.setProgress(0.15);
		
		int resize = renumber.length;

		for (int i = 0; i < component.length; i++)
			resize += (layer[i].length - dummyStartForComp[i]);

		int[] newRenumber = new int[resize];
		int[] newcI = new int[resize];

		for (int i = 0; i < renumber.length; i++) {
			newRenumber[i] = renumber[i];
			newcI[i] = cI[i];
		}

		int t = renumber.length;

		for (int i = 0; i < reduced.length; i++) {
			for (int j = reduced[i].getDummyNodesStart(); j < reduced[i].getNodecount(); j++) {
				newRenumber[t] = j;
				newcI[t] = i;
				t++;
			}
		}
		
		tm.setProgress(0.2); // TODO

		renumber = newRenumber;
		cI = newcI;

		edges = new LinkedList<>();

		for (int i = 0; i < reduced.length; i++) {
			edge = reduced[i].GetEdges();

			for (int j = 0; j < edge.length; j++) { // uzasna budzevina!!!!!! // FIXME: what does this mean?
				int from = -1;
				int to = -1;

				for (int k = 0; k < cI.length; k++) {
					if (cancelled)
						return;
					
					if ((cI[k] == i) && (renumber[k] == edge[j].getFrom()))
						from = k;

					if ((cI[k] == i) && (renumber[k] == edge[j].getTo()))
						to = k;

					if ((from != -1) && (to != -1))
						break;
				}

				edges.add(new Edge(from, to)); //edges.add(new Edge(to, from));
			}
			
			setProgress(tm, .2f, i / (float) reduced.length, .3f);
		}
		
		edge = new Edge[edges.size()];
		edges.toArray(edge);
		graph = new Graph(resize, edge);
		
		tm.setProgress(0.3);
		tm.setStatusMessage("Repositioning nodes in view...");
		Thread.yield();

		if (cancelled)
			return;

		/* order nodeviews by layout order */
		HierarchyFlowLayoutOrderNode[] flowLayoutOrder = new HierarchyFlowLayoutOrderNode[resize];

		for (x = 0; x < resize; x++) {
			if (cancelled)
				return;
			
			if (x < numLayoutNodes)
				flowLayoutOrder[x] = new HierarchyFlowLayoutOrderNode(nodeViews.get(x), cI[x],
				                                                      reduced[cI[x]].getNodecount(),
				                                                      layer[cI[x]][renumber[x]],
				                                                      horizontalPosition[cI[x]][renumber[x]],
				                                                      x);
			else
				flowLayoutOrder[x] = new HierarchyFlowLayoutOrderNode(null, cI[x],
				                                                      reduced[cI[x]].getNodecount(),
				                                                      layer[cI[x]][renumber[x]],
				                                                      horizontalPosition[cI[x]][renumber[x]],
				                                                      x);

			nodes2HFLON.put(x, flowLayoutOrder[x]);
			
			setProgress(tm, .3f, x / (float) resize, .4f);
		}
		
		tm.setProgress(0.4);
		Arrays.sort(flowLayoutOrder);

		int lastComponent = -1;
		int lastLayer = -1;
		int startBandY = context.topEdge;
		int cleanBandY = context.topEdge;
		int startComponentX = context.leftEdge;
		int cleanComponentX = context.leftEdge;
		int startLayerY = context.topEdge;
		int cleanLayerY = context.topEdge;
		int cleanLayerX = context.leftEdge;
		int[] layerStart = new int[numLayoutNodes + 1];

		/* layout nodes which are selected */
		int nodeIndex;

		/* layout nodes which are selected */
		int lastComponentEnd = -1;

		for (nodeIndex = 0; nodeIndex < resize; nodeIndex++) {
			if (cancelled)
				return;
			
			HierarchyFlowLayoutOrderNode node = flowLayoutOrder[nodeIndex];
			int currentComponent = node.componentNumber;
			int currentLayer = node.layer;
			View<CyNode> currentView = node.nodeView;

			tm.setStatusMessage("Layering nodes vertically...");
			Thread.yield();

			if (lastComponent == -1) {
				/* this is the first component */
				lastComponent = currentComponent;
				lastLayer = currentLayer;
				layerStart[currentLayer] = -1;
			}

			if (lastComponent != currentComponent) {
				/* new component */
				// first call function for Horizontal Positioning of nodes in lastComponent
				int[] minXArray = new int[1];
				int maxX = horizontalNodePositioning(nodeIndex
				                                     - flowLayoutOrder[nodeIndex - 1].componentSize,
				                                     nodeIndex - 1, flowLayoutOrder, graph,
				                                     renumber, cI, dummyStartForComp, minXArray);
				int minX = minXArray[0];
				lastComponentEnd = nodeIndex - 1;

				for (int i = nodeIndex - flowLayoutOrder[nodeIndex - 1].componentSize;
				     i <= (nodeIndex - 1); i++)
					flowLayoutOrder[i].xPos -= (minX - startComponentX);

				maxX -= (minX - startComponentX);

				layerStart[lastLayer] = startComponentX;

				/* initialize for new component */
				startComponentX = cleanComponentX + context.componentSpacing;

				if (maxX > startComponentX)
					startComponentX = maxX + context.componentSpacing;

				if (startComponentX > context.rightMargin) {
					/* new band */
					startBandY = cleanBandY + context.bandGap;
					cleanBandY = startBandY;
					startComponentX = context.leftEdge;
					cleanComponentX = context.leftEdge;
				}

				startLayerY = startBandY;
				cleanLayerY = startLayerY;
				cleanLayerX = startComponentX;
				layerStart[currentLayer] = -1;
			} else if (lastLayer != currentLayer) {
				/* new layer */
				layerStart[lastLayer] = startComponentX;

				startLayerY = cleanLayerY + context.nodeVerticalSpacing;
				cleanLayerY = startLayerY;
				cleanLayerX = startComponentX;
				layerStart[currentLayer] = -1;
			}

			node.setXPos(cleanLayerX);
			node.setYPos(startLayerY);
			cleanLayerX += context.nodeHorizontalSpacing;

			int currentBottom;
			int currentRight;

			if (currentView != null) {
				currentBottom = startLayerY + currentView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT).intValue();
				currentRight = cleanLayerX + currentView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH).intValue(); 
			} else {
				currentBottom = startLayerY;
				currentRight = cleanLayerX;
			}

			if (currentBottom > cleanBandY)
				cleanBandY = currentBottom;

			if (currentRight > cleanComponentX)
				cleanComponentX = currentRight;

			if (currentBottom > cleanLayerY)
				cleanLayerY = currentBottom;

			if (currentRight > cleanLayerX)
				cleanLayerX = currentRight;

			lastComponent = currentComponent;
			lastLayer = currentLayer;
			
			setProgress(tm, .4f, nodeIndex / (float) resize, .8f);
		}
		
		tm.setProgress(0.8);
		
		if (cancelled)
			return;

		/* Set horizontal positions of last component */
		int[] minXArray = new int[1];
		horizontalNodePositioning(lastComponentEnd + 1, resize - 1, flowLayoutOrder, graph,
		                          renumber, cI, dummyStartForComp, minXArray);

		int minX = minXArray[0];

		for (int i = lastComponentEnd + 1; i < resize; i++)
			flowLayoutOrder[i].xPos -= (minX - startComponentX);

		/* Map edges to edge views in order to map dummy nodes to edge bends properly */
		int edgeCount = networkView.getModel().getEdgeCount();
		int count = 0;
		
		for (var ev : networkView.getEdgeViewsIterable()) {
			if (cancelled)
				return;
			
			Integer edgeFrom = suid2Index.get(ev.getModel().getSource().getSUID());
			Integer edgeTo = suid2Index.get(ev.getModel().getTarget().getSUID());

			if ((edgeFrom == null) || (edgeTo == null)) // Must be from an unselected node
				continue;

			if ((numLayoutNodes <= 1)
			    || ((edgeFrom < numLayoutNodes)
			       && (edgeTo < numLayoutNodes))) {
				/* add edge to graph */
				Edge theEdge = component[cI[edgeFrom]].GetTheEdge(renumber[edgeFrom],
				                                                             renumber[edgeTo]);

				if (myEdges2EdgeViews[cI[edgeFrom]] == null)
					myEdges2EdgeViews[cI[edgeFrom]] = new HashMap<>();

				myEdges2EdgeViews[cI[edgeFrom]].put(theEdge, ev);
			}
			
			setProgress(tm, .8f, count / (float) edgeCount, .9f);
			count++;
		}
		
		tm.setProgress(0.9);
		
		for (nodeIndex = 0; nodeIndex < resize; nodeIndex++) {
			if (cancelled)
				return;
			
			HierarchyFlowLayoutOrderNode node = flowLayoutOrder[nodeIndex];

			if (node.nodeView != null) {
				View<CyNode> currentView = node.nodeView;
				currentView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, Double.valueOf((double)node.getXPos()));
				currentView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, Double.valueOf((double)node.getYPos()));
			}
		}
		
		tm.setProgress(0.925);
		
		final HandleFactory handleFactory = serviceRegistrar.getService(HandleFactory.class);
		final BendFactory bendFactory = serviceRegistrar.getService(BendFactory.class);
		
		for (nodeIndex = 0; nodeIndex < resize; nodeIndex++) {
			if (cancelled)
				return;
			
			HierarchyFlowLayoutOrderNode node = flowLayoutOrder[nodeIndex];

			if (node.nodeView == null) {
				Edge theEdge = (Edge) dummy2Edge[cI[node.graphIndex]].get(Integer.valueOf(renumber[node.graphIndex]));
				View<CyEdge> ev = myEdges2EdgeViews[cI[node.graphIndex]].get(theEdge);

				if (ev != null) {
					View<CyNode> source = networkView.getNodeView(ev.getModel().getSource());
					View<CyNode> target = networkView.getNodeView(ev.getModel().getTarget());
					
					double k = (getYPositionOf(target) - getYPositionOf(source)) / (
							getXPositionOf(target) - getXPositionOf(source));

					double xPos = getXPositionOf(source);

					if (k != 0)
						xPos += ((node.yPos - getYPositionOf(source)) / k);

					Bend b = bendFactory.createBend();

					Handle h = handleFactory.createHandle(networkView,ev,xPos,node.yPos);
					b.insertHandleAt(0,h);

					ev.setVisualProperty(BasicVisualLexicon.EDGE_BEND, b); 
				}
			}
		}
		
		tm.setProgress(0.95);
		
		for (nodeIndex = 0; nodeIndex < resize; nodeIndex++) {
			if (cancelled)
				return;
			
			HierarchyFlowLayoutOrderNode node = flowLayoutOrder[nodeIndex];

			if (node.nodeView == null) {
				Edge theEdge = dummy2Edge[cI[node.graphIndex]].get(Integer.valueOf(renumber[node.graphIndex]));
				View<CyEdge> ev = myEdges2EdgeViews[cI[node.graphIndex]].get(theEdge);
				
				if (ev != null) {
					List<Handle> handles = ev.getVisualProperty(BasicVisualLexicon.EDGE_BEND).getAllHandles();
					for ( Handle h : handles ) {
						Point2D handelPt = h.calculateHandleLocation(networkView,ev);
						if ( handelPt.getY() == node.yPos ) {
							h.defineHandle(networkView,ev,(double)(node.xPos), (double)(node.yPos));
							break;
						}
					}
				}
			}
		}

		tm.setProgress(1.0);
	}
	
	private double getXPositionOf(View<CyNode> nodeView){
		return nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
	}
	
	private double getYPositionOf(View<CyNode> nodeView){
		return nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
	}

	/**
	 * Sum length of edges between 2 consecutive layers. This is used for getting as compact
	 * layout as possible, we want to minimize this sum by horizontal coordinate assignment
	 */
	private double edgeLength2Layers(
			HierarchyFlowLayoutOrderNode[] nodes,
			LinkedList<Integer>[] edgesFrom,
			LinkedList<Integer>[] edgesTo,
			int x,
			int direct,
			int startInd,
			int endInd
	) {
		double layerMin = 0;

		HashMap<Integer, HierarchyFlowLayoutOrderNode> nodesBak2HFLON = new HashMap<>();

		for (int i = startInd; i <= endInd; i++)
			nodesBak2HFLON.put(Integer.valueOf(nodes[i].graphIndex), nodes[i]);

		if (direct == -1) {
			int xHlp = x;

			while ((xHlp < nodes.length) && (nodes[xHlp].layer == nodes[x].layer)) {
				if (cancelled)
					return layerMin;
				
				var iterToHlp = edgesTo[nodes[xHlp].graphIndex].iterator();
				double curPos = nodes[xHlp].xPos;

				while (iterToHlp.hasNext()) {
					Integer neigh = (Integer) iterToHlp.next();
					layerMin += (Math.abs(nodesBak2HFLON.get(neigh).xPos - curPos) / ((double) context.nodeHorizontalSpacing));

					// mozda ako je ivica izmedju 2 dummy cvora da duplira daljinu // FIXME: translate to english
				}

				xHlp++;
			}

			xHlp = x - 1;

			while ((xHlp >= 0) && (nodes[xHlp].layer == nodes[x].layer)) {
				if (cancelled)
					return layerMin;
				
				var iterToHlp = edgesTo[nodes[xHlp].graphIndex].iterator();
				double curPos = nodes[xHlp].xPos;

				while (iterToHlp.hasNext()) {
					Integer neigh = (Integer) iterToHlp.next();
					layerMin += (Math.abs(nodesBak2HFLON.get(neigh).xPos - curPos) / ((double) context.nodeHorizontalSpacing));
				}

				xHlp--;
			}
		} else {
			int xHlp = x;

			while ((xHlp < nodes.length) && (nodes[xHlp].layer == nodes[x].layer)) {
				if (cancelled)
					return layerMin;
				
				var iterFromHlp = edgesFrom[nodes[xHlp].graphIndex].iterator();
				double curPos = nodes[xHlp].xPos;

				while (iterFromHlp.hasNext()) {
					Integer neigh = (Integer) iterFromHlp.next();
					layerMin += (Math.abs(nodesBak2HFLON.get(neigh).xPos - curPos) / ((double) context.nodeHorizontalSpacing));
				}

				xHlp++;
			}

			xHlp = x - 1;

			while ((xHlp >= 0) && (nodes[xHlp].layer == nodes[x].layer)) {
				if (cancelled)
					return layerMin;
				
				var iterFromHlp = edgesFrom[nodes[xHlp].graphIndex].iterator();
				double curPos = nodes[xHlp].xPos;

				while (iterFromHlp.hasNext()) {
					Integer neigh = (Integer) iterFromHlp.next();
					layerMin += (Math.abs(nodesBak2HFLON.get(neigh).xPos - curPos) / ((double) context.nodeHorizontalSpacing));
				}

				xHlp--;
			}
		}
		
		return layerMin;
	}

	/**
	 * Function which does actual horizontal coordinate assignment of nodes
	 * @param startInd - in nodes array
	 * @param endInd - in nodes array
	 * @param nodes
	 * @param theGraph
	 * @param renumber
	 * @param cI
	 * @param dummyStarts - dummy nodes are always in the end, so we always remember just
	 *                         the index of the first dummy in a graph component
	 * @param minX2Return
	 * @return
	 */
	private int horizontalNodePositioning(
			int startInd,
			int endInd,
			HierarchyFlowLayoutOrderNode[] nodes,
			Graph theGraph,
			int[] renumber,
			int[] cI,
			int[] dummyStarts,
			int[] minX2Return
	) {
		int maxX = Integer.MIN_VALUE;
		
		/* sort nodes in layer in order of coordinate assignment - first dummy nodes, then sorted by fan-in + fan-out */
		LinkedList<Integer>[] edgesFrom = theGraph.GetEdgesFrom();
		LinkedList<Integer>[] edgesTo = theGraph.GetEdgesTo();

		LayerOrderNode[] lon = new LayerOrderNode[endInd - startInd + 1];
		HashMap<Integer, LayerOrderNode> ind2Lon = new HashMap<>();

		for (int i = 0; i <= (endInd - startInd); i++) {
			boolean dum = false;

			if (renumber[nodes[startInd + i].graphIndex] >= dummyStarts[cI[nodes[startInd + i].graphIndex]])
				dum = true;

			lon[i] = new LayerOrderNode(startInd + i, dum,
			                            edgesFrom[nodes[startInd + i].graphIndex].size()
			                            + edgesTo[nodes[startInd + i].graphIndex].size(),
			                            nodes[startInd + i].layer);
			ind2Lon.put(Integer.valueOf(startInd + i), lon[i]);
		}

		Arrays.sort(lon);

		int cur = 0; //, curLayer = nodes[x].layer;
		int x = lon[0].GetIndex(); //, curLayer = nodes[x].layer;
		int direct = 1; //, curLayer = nodes[x].layer;
		int noOfSteps = (10 * (endInd - startInd + 1)) - ((10 - 1) / 2); //, curLayer = nodes[x].layer;
		double layerMin = Integer.MAX_VALUE;
		boolean newLayer = true;
		boolean dirFirst = true;

		for (int dx = 0; dx < noOfSteps; dx++) {
			if (cancelled)
				return maxX;
			
			if (newLayer) {
				layerMin = edgeLength2Layers(nodes, edgesFrom, edgesTo, x, direct, startInd, endInd);
				newLayer = false;
			}

			int idealPosXUp = 0;
			int idealPosXDown = 0;
			int neighsCountUp = 0;
			int neighsCountDown = 0;
			var iterFrom = edgesFrom[nodes[x].graphIndex].iterator();
			var iterTo = edgesTo[nodes[x].graphIndex].iterator();

			while (iterFrom.hasNext()
			       && ((direct == 1) || (edgesTo[nodes[x].graphIndex].isEmpty() && (direct == -1)))) {
				if (cancelled)
					return maxX;
				
				Integer neigh = (Integer) iterFrom.next();

				if (nodes2HFLON.get(neigh).layer == (nodes[x].layer - 1)) {
					idealPosXUp += nodes2HFLON.get(neigh).xPos;
					neighsCountUp++;

					// enforcing the impact of dummy nodes, it straightens the lines connected to dummy nodes
					if ((renumber[nodes2HFLON.get(neigh).graphIndex] >= dummyStarts[cI[nodes2HFLON.get(neigh).graphIndex]])
					    && ind2Lon.get(Integer.valueOf(x)).GetIsDummy()) {
						idealPosXUp += (4 * nodes2HFLON.get(neigh).xPos);
						neighsCountUp += 4;
					}
				}
			}

			while (iterTo.hasNext()
			       && ((direct == -1)
			          || (edgesFrom[nodes[x].graphIndex].isEmpty() && (direct == 1)))) {
				if (cancelled)
					return maxX;
				
				Integer neigh = (Integer) iterTo.next();

				if (nodes2HFLON.get(neigh).layer == (nodes[x].layer + 1)) {
					idealPosXDown += nodes2HFLON.get(neigh).xPos;
					neighsCountDown++;

					if ((renumber[nodes2HFLON.get(neigh).graphIndex] >= dummyStarts[cI[nodes2HFLON.get(neigh).graphIndex]])
					    && ind2Lon.get(Integer.valueOf(x)).GetIsDummy()) {
						idealPosXDown += (4 * nodes2HFLON.get(neigh).xPos);
						neighsCountDown += 4;
					}
				}
			}

			if ((neighsCountUp > 0) || (neighsCountDown > 0)) {
				int idealPosX;

				if (neighsCountUp == 0)
					idealPosX = idealPosXDown / neighsCountDown;
				else if (neighsCountDown == 0)
					idealPosX = idealPosXUp / neighsCountUp;
				else
					idealPosX = ((idealPosXUp / neighsCountUp) + (idealPosXDown / neighsCountDown)) / 2;

				if ((idealPosX % context.nodeHorizontalSpacing) != 0)
					if ((idealPosX - nodes[x].xPos) < context.nodeHorizontalSpacing)
						idealPosX = ((int) (idealPosX / context.nodeHorizontalSpacing)) * context.nodeHorizontalSpacing;
					else if ((nodes[x].xPos - idealPosX) < context.nodeHorizontalSpacing)
						idealPosX = ((int) (idealPosX / context.nodeHorizontalSpacing) + 1) * context.nodeHorizontalSpacing;
					else
						idealPosX = ((int) ((idealPosX / context.nodeHorizontalSpacing) + 0.5)) * context.nodeHorizontalSpacing;

				int oldXPos = nodes[x].xPos;
				nodes[x].xPos = idealPosX;

				HierarchyFlowLayoutOrderNode[] nodesBak = new HierarchyFlowLayoutOrderNode[nodes.length];

				for (int i = 0; i < nodes.length; i++)
					nodesBak[i] = new HierarchyFlowLayoutOrderNode(nodes[i]);

				if ((idealPosX > oldXPos) && (x < endInd) && (nodes[x + 1].layer == nodes[x].layer)) {
					boolean q = false;

					for (int i = x + 1; (i <= endInd) && !q; i++) {
						//	System.out.print(nodesBak[i].xPos + " " + nodesBak[i+1].xPos + " " + x + " " + i + " ");
						if ((nodesBak[i].layer == nodesBak[x].layer)
						    && (nodesBak[i].xPos < (nodesBak[i - 1].xPos + context.nodeHorizontalSpacing))) {
							nodesBak[i].xPos = nodesBak[i - 1].xPos + context.nodeHorizontalSpacing;

							/*if (((LayerOrderNode)ind2Lon.get(Integer.valueOf(x))).GetPriority() < ((LayerOrderNode)ind2Lon.get(Integer.valueOf(i))).GetPriority())
							    q = true;*/
						} else

							break;
					}

					if (cancelled)
						return maxX;
					
					double w = edgeLength2Layers(nodesBak, edgesFrom, edgesTo, x, direct, startInd, endInd);

					if (!q && (w <= layerMin)) {
						layerMin = w;

						for (int i = x + 1; i <= endInd; i++)
							if ((nodes[i].layer == nodes[x].layer)
							    && (nodes[i].xPos < (nodes[i - 1].xPos + context.nodeHorizontalSpacing))) {
								nodes[i].xPos = nodes[i - 1].xPos + context.nodeHorizontalSpacing;
							} else {
								break;
							}
					} else {
						if (nodes[x + 1].layer == nodes[x].layer)
							nodes[x].xPos = nodes[x + 1].xPos - context.nodeHorizontalSpacing;
						else
							nodes[x].xPos = oldXPos;
					}
				} else if ((idealPosX < oldXPos) && (x > 0)
				           && (nodes[x - 1].layer == nodes[x].layer)) {
					boolean q = false;

					for (int i = x - 1; (i >= 0) && !q; i--) {
						//System.out.print(nodesBak[i].xPos + " " + nodesBak[i+1].xPos + " " + x + " " + i + " ");
						if ((nodesBak[i].layer == nodesBak[x].layer)
						    && (nodesBak[i].xPos > (nodesBak[i + 1].xPos - context.nodeHorizontalSpacing))) {
							nodesBak[i].xPos = nodesBak[i + 1].xPos - context.nodeHorizontalSpacing;

							/*if (((LayerOrderNode)ind2Lon.get(Integer.valueOf(x))).GetPriority() < ((LayerOrderNode)ind2Lon.get(Integer.valueOf(i))).GetPriority())
							{
							    q = true;
							}*/
						} else

							break;
					}

					double w = edgeLength2Layers(nodesBak, edgesFrom, edgesTo, x, direct, startInd, endInd);

					if (!q && (w <= layerMin)) {
						layerMin = w;

						for (int i = x - 1; i >= 0; i--)
							if ((nodes[i].layer == nodes[x].layer)
							    && (nodes[i].xPos > (nodes[i + 1].xPos - context.nodeHorizontalSpacing))) {
								nodes[i].xPos = nodes[i + 1].xPos - context.nodeHorizontalSpacing;
							} else {
								break;
							}
					} else {
						if (nodes[x - 1].layer == nodes[x].layer)
							nodes[x].xPos = nodes[x - 1].xPos + context.nodeHorizontalSpacing;
						else
							nodes[x].xPos = oldXPos;
					}
				}
			}

			if (startInd - endInd != 0 && dx % (startInd - endInd) == 0 && (dx != 0))
				if (!dirFirst) {
					direct *= -1;
					dirFirst = true;
				} else
					dirFirst = false;

			cur = (cur + direct + lon.length) % lon.length; //(cur + 1) % lon.length;

			if (nodes[x].layer != nodes[lon[cur].GetIndex()].layer) {
				newLayer = true;
			}

			x = lon[cur].GetIndex();
		}

		int minX = Integer.MAX_VALUE;

		for (int i = startInd; i <= endInd; i++) {
			if (nodes[i].getXPos() > maxX)
				maxX = nodes[i].getXPos();

			if (nodes[i].getXPos() < minX)
				minX = nodes[i].getXPos();
		}

		minX2Return[0] = minX;

		return maxX;
	}

	@Override
	public String toString() {
		return "Hierarchical Layout";
	}
	
	/**
	* Gets the Task Title.
	*
	* @return human readable task title.
	*/
	public String getTitle() {
		return new String("Hierarchical Layout");
	}
	
	/**
	 * @param tm the TaskMonitor
	 * @param pb the progress value of the previous block of code
	 * @param v  the new progress value of the current block of code, usually a loop
	 * @param nb the progress value of the next block of code
	 */
	private void setProgress(TaskMonitor tm, float pb, float v, float nb) {
		float n = pb + (v * (nb - pb));
		n = Math.round(n * 1000) / 1000.0f; // Reduce the precision in order to avoid unnecessary progress bar updates
		tm.setProgress(n);
	}
}

class HierarchyFlowLayoutOrderNode implements Comparable<HierarchyFlowLayoutOrderNode> {
	
	public View<CyNode> nodeView;

	public int componentNumber;
	public int componentSize;
	public int layer;
	public int horizontalPosition;
	public int xPos;
	public int yPos;
	public int graphIndex;

	public HierarchyFlowLayoutOrderNode(
			View<CyNode> a_nodeView,
			int a_componentNumber,
			int a_componentSize,
			int a_layer,
			int a_horizontalPosition,
			int a_graphIndex
	) {
		nodeView = a_nodeView;
		componentNumber = a_componentNumber;
		componentSize = a_componentSize;
		layer = a_layer;
		horizontalPosition = a_horizontalPosition;
		graphIndex = a_graphIndex;
	}

	public HierarchyFlowLayoutOrderNode(HierarchyFlowLayoutOrderNode a) {
		nodeView = a.nodeView;
		componentNumber = a.componentNumber;
		componentSize = a.componentSize;
		layer = a.layer;
		horizontalPosition = a.horizontalPosition;
		graphIndex = a.graphIndex;
		yPos = a.yPos;
		xPos = a.xPos;
	}
	
	public int getXPos() {
		return xPos;
	}

	public int getYPos() {
		return yPos;
	}

	public void setXPos(int a_xPos) {
		xPos = a_xPos;
	}

	public void setYPos(int a_yPos) {
		yPos = a_yPos;
	}

	@Override
	public int compareTo(HierarchyFlowLayoutOrderNode y) {
		int diff = y.componentSize - componentSize;

		if (diff != 0)
			return diff;

		diff = componentNumber - y.componentNumber;

		if (diff != 0)
			return diff;

		diff = layer - y.layer; //y.layer - layer;

		if (diff != 0)
			return diff;

		return horizontalPosition - y.horizontalPosition;
	}
}

/**
 * Class which we use to determine the order by which we traverse nodes when
 * we calculate their horizontal coordinate. We traverse it layer by layer, inside
 * the layer we first place dummy nodes and then sorted by degree of the node. *
 * @author Aleksandar Nikolic
 */
class LayerOrderNode implements Comparable<LayerOrderNode> {
	
	private int index;
	private boolean isDummy;
	private int degree;
	private int priority;
	private int layer;

	public LayerOrderNode(int index, boolean isDummy, int degree, int layer) {
		this.index = index;
		this.isDummy = isDummy;
		this.degree = degree;
		this.layer = layer;

		if (isDummy)
			priority = 20;
		else if (degree < 5)
			priority = 5;
		else if (degree < 10)
			priority = 10;
		else
			priority = 15; //priority = degree;
	}

	@Override
	public int compareTo(LayerOrderNode second) {
		if (layer != second.layer)
			return (layer - second.layer); //(second.layer - layer); 

		if (isDummy && !second.isDummy)
			return -1;

		if (!isDummy && second.isDummy)
			return 1;

		return (second.degree - degree);
	}

	public int GetIndex() {
		return index;
	}

	public int GetPriority() {
		return priority;
	}

	public boolean GetIsDummy() {
		return isDummy;
	}
}
