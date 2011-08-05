/*
 File: GMLWriter.java

 Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.io.internal.write.gml;

import org.cytoscape.io.internal.read.gml.KeyValue;
import org.cytoscape.io.internal.read.gml.GMLReader;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.CyNetworkView;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;


/**
 * The purpse of this class is to translate cytoscape data structures into a gml
 * object tree, we can then use the gml parser to write this tree out into a
 * file
 */
public class GMLWriter {
	/**
	 * It is possible that nodes have been added to the graph since it was
	 * loaded. This set will keep track of nodes and edges that are currently in
	 * the perspective that have no corresponding entry in the object tree that
	 * was loaded with the netowrk
	 */
	private Set<Integer> newNodes;

	/**
	 * It is possible that nodes have been added to the graph since it was
	 * loaded. This set will keep track of nodes and edges that are currently in
	 * the perspective that have no corresponding entry in the object tree that
	 * was loaded with the netowrk
	 */
	private Set<Integer> newEdges;

	/**
	 * Given an object tree given in oldList, update it with the information
	 * provided in network and optionall view (if view is not null). The GML
	 * spec requires that we remember all information provided in the original
	 * gml file. Therefore, we pass in the old object tree as oldList, as
	 * execute functions which will update that data structure. We would also
	 * like to save files that may not have been loaded from a gml file. This
	 * list is empty in that case. Those same update functions must be able to
	 * create all relevant key-value pairs as well then.
	 */
	@SuppressWarnings("unchecked")  // for the casts of KeyValue.value
	public void writeGML(final CyNetwork network, final CyNetworkView view, final List<KeyValue> oldList) {
		/*
		 * Initially all the nodes and edges have not been seen
		 */
		newNodes = new HashSet<Integer>(network.getNodeCount());
		newEdges = new HashSet<Integer>(network.getEdgeCount());

		for ( CyNode node : network.getNodeList() ) {
			newNodes.add(Integer.valueOf(node.getIndex()));
		}

		for ( CyEdge edge : network.getEdgeList() ) {
			newEdges.add(Integer.valueOf(edge.getIndex()));
		}

		/*
		 * We are going to make sure the keys graph,creator,and version are
		 * present and update them fi they are already present
		 */
		KeyValue graph = null;

		/*
		 * We are going to make sure the keys graph,creator,and version are
		 * present and update them fi they are already present
		 */
		KeyValue creator = null;

		/*
		 * We are going to make sure the keys graph,creator,and version are
		 * present and update them fi they are already present
		 */
		KeyValue version = null;
		KeyValue keyVal = null;

		for (Iterator<KeyValue> it = oldList.iterator(); it.hasNext();) {
			keyVal = it.next();

			if (keyVal.key.equals(GMLReader.GRAPH)) {
				graph = keyVal;
			} else if (keyVal.key.equals(GMLReader.CREATOR)) {
				creator = keyVal;
			} else if (keyVal.key.equals(GMLReader.VERSION)) {
				version = keyVal;
			}
		}

		if (creator == null) {
			creator = new KeyValue(GMLReader.CREATOR, null);
			oldList.add(creator);
		}

		if (version == null) {
			version = new KeyValue(GMLReader.VERSION, null);
			oldList.add(version);
		}

		if (graph == null) {
			graph = new KeyValue(GMLReader.GRAPH, new Vector());
			oldList.add(graph);
		}

		/*
		 * Update the list associated with the graph pair
		 */
		writeGraph(network, view, (List<KeyValue>) graph.value);
		creator.value = "Cytoscape";
		version.value = new Double(1.0);

		/*
		 * After update all of the graph objects that were already present in
		 * the object tree check and see if there are any objects in the current
		 * perspective that were not updated For these objects, create an empty
		 * key-value mapping and then update it
		 */
		List<KeyValue> graph_list = (List<KeyValue>) graph.value;

		while (!newNodes.isEmpty()) {
			KeyValue nodePair = new KeyValue(GMLReader.NODE, new Vector());
			graph_list.add(nodePair);
			((List) nodePair.value).add(new KeyValue(GMLReader.ROOT_INDEX,
			                                         newNodes.iterator().next()));
			writeGraphNode(network, view, (List) nodePair.value);
		}

		while (!newEdges.isEmpty()) {
			KeyValue edgePair = new KeyValue(GMLReader.EDGE, new Vector());
			graph_list.add(edgePair);
			((List<KeyValue>) edgePair.value).add(new KeyValue(GMLReader.ROOT_INDEX,
			                                         newEdges.iterator().next()));
			writeGraphEdge(network, view, (List) edgePair.value);
		}
	}

	/**
	 * Update the list associated with a graph key
	 */
	 @SuppressWarnings("unchecked") // for the casts of KeyValue.value
	private void writeGraph(final CyNetwork network, final CyNetworkView view, final List<KeyValue> oldList) {

		 
		 // To enhance compatibility with non-cytoscape GML-conformant
		 // programs, add directedness flag to graph: allways use 'directed',
		 // to match pre-3.0 cytoscape's 'edges are directed' behaviour
		 // TODO: could use undirected here if network is undirected i.e. all edges are undirected.
		 oldList.add(new KeyValue("directed", Integer.valueOf(0)));
		 
		 for (Iterator<KeyValue> it = oldList.iterator(); it.hasNext();) {
			KeyValue keyVal = it.next();

			/*
			 * For all nodes in the object tree, update the list that is
			 * associated with that key. If this node is no longer present in
			 * the perpsective, then we must remove it from the ojbect tree.
			 * Also do the same thing for the edges.
			 */
			if (keyVal.key.equals(GMLReader.NODE)) {
				if (!writeGraphNode(network, view, (List<KeyValue>) keyVal.value)) {
					it.remove();
				}
			} else if (keyVal.key.equals(GMLReader.EDGE)) {
				if (!writeGraphEdge(network, view, (List<KeyValue>) keyVal.value)) {
					it.remove();
				}
			}
		}
	}

	/**
	 * Update the list associated with a node key
	 */
	@SuppressWarnings("unchecked") // for the cast of KeyValue.value
	private boolean writeGraphNode(final CyNetwork network, final CyNetworkView view,
	                               final List<KeyValue> oldList) {
		/*
		 * We expect a list associated with node key to potentially have a
		 * graphic key, id key, and root_index key
		 */
		Integer root_index = null;
		KeyValue graphicsPair = null;
		KeyValue labelPair = null;
		KeyValue idPair = null;

		for (Iterator it = oldList.iterator(); it.hasNext();) {
			KeyValue keyVal = (KeyValue) it.next();

			if (keyVal.key.equals(GMLReader.ROOT_INDEX)) {
				root_index = (Integer) keyVal.value;
			} else if (keyVal.key.equals(GMLReader.GRAPHICS)) {
				graphicsPair = keyVal;
			} else if (keyVal.key.equals(GMLReader.LABEL)) {
				labelPair = keyVal;
			} else if (keyVal.key.equals(GMLReader.ID)) {
				idPair = keyVal;
			}
		}

		/*
		 * Check to see if this nodes is still in the perspective
		 */
		if (root_index == null) {
			return false;
		}

		CyNode node = network.getNode(root_index.intValue());

		if (node == null || !network.containsNode(node)) {
			return false;
		}

		/*
		 * Mark this node as seen
		 */
		newNodes.remove(root_index);

		/*
		 * Update or create the id key-value pair for this list
		 */
		if (idPair == null) {
			idPair = new KeyValue(GMLReader.ID, null);
			oldList.add(idPair);
		}

		idPair.value = root_index;

		/*
		 * Optionall update/create the graphics key-value pair for this list if
		 * there is currently defined. NOte that if no view is defined, the
		 * previously loaded view information will remain intact
		 */
		if (view != null) {
			if (graphicsPair == null) {
				graphicsPair = new KeyValue(GMLReader.GRAPHICS, new Vector<KeyValue>());
				oldList.add(graphicsPair);
			}

			writeGraphNodeGraphics(network, view.getNodeView(node), (List<KeyValue>) graphicsPair.value);
		}

		/*
		 * Update/create the label key-value pair. We have co-opted this field
		 * to mean the canoncial name
		 */
		if (labelPair == null) {
			labelPair = new KeyValue(GMLReader.LABEL, null);
			oldList.add(labelPair);
		}

		labelPair.value = node.attrs().get("name",String.class);

		return true;
	}

	/**
	 * Update the list associated with an edge key
	 */
	@SuppressWarnings("unchecked") // for the cast of KeyValue.value
	private boolean writeGraphEdge(final CyNetwork network, final CyNetworkView view,
	                               final List<KeyValue> oldList) {
		/*
		 * An edge key will definitely have a root_index, labelPair (we enforce
		 * this on loading), source key, and a target key
		 */
		Integer root_index = null;
		KeyValue graphicsPair = null;
		KeyValue labelPair = null;
		KeyValue sourcePair = null;
		KeyValue targetPair = null;
		KeyValue isDirected = null;
		
		for (Iterator it = oldList.iterator(); it.hasNext();) {
			KeyValue keyVal = (KeyValue) it.next();

			if (keyVal.key.equals(GMLReader.GRAPHICS)) {
				graphicsPair = keyVal;
			} else if (keyVal.key.equals(GMLReader.LABEL)) {
				labelPair = keyVal;
			} else if (keyVal.key.equals(GMLReader.ROOT_INDEX)) {
				root_index = (Integer) keyVal.value;
			} else if (keyVal.key.equals(GMLReader.SOURCE)) {
				sourcePair = keyVal;
			} else if (keyVal.key.equals(GMLReader.TARGET)) {
				targetPair = keyVal;
			} else if (keyVal.key.equals(GMLReader.IS_DIRECTED)) {
				isDirected = keyVal;
			}
		}

		/*
		 * Make sure the edge is still present in this perspective
		 */
		if (root_index == null) {
			return false;
		}

		CyEdge edge = network.getEdge(root_index.intValue());

		if (edge == null || !network.containsEdge(edge)) {
			return false;
		}

		newEdges.remove(root_index);

		if (targetPair == null) {
			targetPair = new KeyValue(GMLReader.TARGET, null);
			oldList.add(targetPair);
		}

		targetPair.value = Integer.valueOf(edge.getTarget().getIndex());

		if (sourcePair == null) {
			sourcePair = new KeyValue(GMLReader.SOURCE, null);
			oldList.add(sourcePair);
		}

		sourcePair.value = Integer.valueOf(edge.getSource().getIndex());

		if (view != null) {
			if (graphicsPair == null) {
				graphicsPair = new KeyValue(GMLReader.GRAPHICS, new Vector());
				oldList.add(graphicsPair);
			}

			writeGraphEdgeGraphics(network, view.getEdgeView(edge), (List<KeyValue>) graphicsPair.value);
		}

		if (labelPair == null) {
			labelPair = new KeyValue(GMLReader.LABEL, null);
			oldList.add(labelPair);
		}

		labelPair.value = edge.attrs().get("interaction",String.class); 

		if (isDirected == null) {
			isDirected = new KeyValue(GMLReader.IS_DIRECTED, null);
			oldList.add(isDirected);
		}
		if (edge.isDirected()){
			isDirected.value = Integer.valueOf(0); 
		} else {
			isDirected.value = Integer.valueOf(1);
		}

		return true;
	}

	/**
	 * This writes all the graphical information for a particular node into an
	 * object tree
	 */
	private void writeGraphNodeGraphics(final CyNetwork network, final View<CyNode> nodeView,
	                                    final List<KeyValue> oldList) {
	// TODO fix for new style view											
/*
		KeyValue x = null;
		KeyValue y = null;
		KeyValue w = null;
		KeyValue h = null;
		KeyValue type = null;
		KeyValue fill = null;
		KeyValue outline = null;
		KeyValue outline_width = null;

		for (Iterator it = oldList.iterator(); it.hasNext();) {
			KeyValue keyVal = (KeyValue) it.next();

			if (keyVal.key.equals(GMLReader.X)) {
				x = keyVal;
			} else if (keyVal.key.equals(GMLReader.Y)) {
				y = keyVal;
			} else if (keyVal.key.equals(GMLReader.W)) {
				w = keyVal;
			} else if (keyVal.key.equals(GMLReader.H)) {
				h = keyVal;
			} else if (keyVal.key.equals(GMLReader.TYPE)) {
				type = keyVal;
			} else if (keyVal.key.equals(GMLReader.FILL)) {
				fill = keyVal;
			} else if (keyVal.key.equals(GMLReader.OUTLINE)) {
				outline = keyVal;
			} else if (keyVal.key.equals(GMLReader.OUTLINE_WIDTH)) {
				outline_width = keyVal;
			}
		}

		if (x == null) {
			x = new KeyValue(GMLReader.X, null);
			oldList.add(x);
		}

		if (y == null) {
			y = new KeyValue(GMLReader.Y, null);
			oldList.add(y);
		}

		if (w == null) {
			w = new KeyValue(GMLReader.W, null);
			oldList.add(w);
		}

		if (h == null) {
			h = new KeyValue(GMLReader.H, null);
			oldList.add(h);
		}

		if (fill == null) {
			fill = new KeyValue(GMLReader.FILL, null);
			oldList.add(fill);
		}

		if (type == null) {
			type = new KeyValue(GMLReader.TYPE, null);
			oldList.add(type);
		}

		if (outline == null) {
			outline = new KeyValue(GMLReader.OUTLINE, null);
			oldList.add(outline);
		}

		if (outline_width == null) {
			outline_width = new KeyValue(GMLReader.OUTLINE_WIDTH, null);
			oldList.add(outline_width);
		}

		if (nodeView == null) return; // If no view data, simply don't save it (instead of crashing)
		
		x.value = new Double(nodeView.getXPosition());
		y.value = new Double(nodeView.getYPosition());
		w.value = new Double(nodeView.getWidth());
		h.value = new Double(nodeView.getHeight());
		fill.value = getColorHexString((Color) nodeView.getUnselectedPaint());
		outline.value = getColorHexString((Color) nodeView.getBorderPaint());
		outline_width.value = new Double(nodeView.getBorderWidth());

		switch (nodeView.getShape()) {
			case NodeView.RECTANGLE:
				type.value = GMLReader.RECTANGLE;

				break;

			case NodeView.ELLIPSE:
				type.value = GMLReader.ELLIPSE;

				break;

			case NodeView.DIAMOND:
				type.value = GMLReader.DIAMOND;

				break;

			case NodeView.HEXAGON:
				type.value = GMLReader.HEXAGON;

				break;

			case NodeView.OCTAGON:
				type.value = GMLReader.OCTAGON;

				break;

			case NodeView.PARALELLOGRAM:
				type.value = GMLReader.PARALELLOGRAM;

				break;

			case NodeView.TRIANGLE:
				type.value = GMLReader.TRIANGLE;

				break;
		}
		*/
	}

	private void writeGraphEdgeGraphics(final CyNetwork network, final View<CyEdge> edgeView,
	                                    final List<KeyValue> oldList) {
		// TODO fix for new style view
	/*
		KeyValue width = null;
		KeyValue fill = null;
		KeyValue line = null;
		KeyValue type = null;
		KeyValue source_arrow = null;
		KeyValue target_arrow = null;

		for (Iterator it = oldList.iterator(); it.hasNext();) {
			KeyValue keyVal = (KeyValue) it.next();

			if (keyVal.key.equals(GMLReader.WIDTH)) {
				width = keyVal;
			} else if (keyVal.key.equals(GMLReader.FILL)) {
				fill = keyVal;
			} else if (keyVal.key.equals(GMLReader.LINE)) {
				line = keyVal;
			} else if (keyVal.key.equals(GMLReader.TYPE)) {
				type = keyVal;
			} else if (keyVal.key.equals(GMLReader.SOURCE_ARROW)) {
				source_arrow = keyVal;
			} else if (keyVal.key.equals(GMLReader.TARGET_ARROW)) {
				target_arrow = keyVal;
			}
		}

		if (width == null) {
			width = new KeyValue(GMLReader.WIDTH, null);
			oldList.add(width);
		}
	
		if (edgeView == null) return; // If no view data, simply don't save it (instead of crashing)
		
		width.value = new Double(edgeView.getStrokeWidth());

		if (fill == null) {
			fill = new KeyValue(GMLReader.FILL, null);
			oldList.add(fill);
		}

		fill.value = getColorHexString((Color) edgeView.getUnselectedPaint());

		if (type == null) {
			type = new KeyValue(GMLReader.TYPE, null);
			oldList.add(type);
		}

		switch (edgeView.getLineType()) {
			case EdgeView.STRAIGHT_LINES:
				type.value = GMLReader.STRAIGHT_LINES;

				break;

			case EdgeView.CURVED_LINES:
				type.value = GMLReader.CURVED_LINES;

				break;
		}

		if (line == null) {
			line = new KeyValue(GMLReader.LINE, null);
			oldList.add(line);
		}

		Point2D[] pointsArray = edgeView.getBend().getDrawPoints();
		Vector<KeyValue> points = new Vector<KeyValue>(pointsArray.length);

		// CTW funny thing with anchor points, need to trim off the first and
		// last
		// and reverse the order x
		for (int idx = pointsArray.length - 2; idx > 0; idx--) {
			Vector<KeyValue> coords = new Vector<KeyValue>(2);
			coords.add(new KeyValue(GMLReader.X, new Double(pointsArray[idx].getX())));
			coords.add(new KeyValue(GMLReader.Y, new Double(pointsArray[idx].getY())));
			points.add(new KeyValue(GMLReader.POINT, coords));
		}

		line.value = points;

		if (source_arrow == null) {
			source_arrow = new KeyValue(GMLReader.SOURCE_ARROW, null);
			oldList.add(source_arrow);
		}

		source_arrow.value = Integer.valueOf(edgeView.getSourceEdgeEnd());

		if (target_arrow == null) {
			target_arrow = new KeyValue(GMLReader.TARGET_ARROW, null);
			oldList.add(target_arrow);
		}

		target_arrow.value = Integer.valueOf(edgeView.getTargetEdgeEnd());
		*/
	}

	/**
	 * Get the String representation of the 6 character hexidecimal RGB values
	 * i.e. #ff000a
	 *
	 * @param Color
	 *            The color to be converted
	 */
	private static String getColorHexString(final Color c) {
		return ("#" // +Integer.toHexString(c.getRGB());
		       + Integer.toHexString(256 + c.getRed()).substring(1)
		       + Integer.toHexString(256 + c.getGreen()).substring(1)
		       + Integer.toHexString(256 + c.getBlue()).substring(1));
	}
}
