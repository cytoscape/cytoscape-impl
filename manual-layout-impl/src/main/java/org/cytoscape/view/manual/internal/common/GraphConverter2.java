package org.cytoscape.view.manual.internal.common;

/*
 * #%L
 * Cytoscape Manual Layout Impl (manual-layout-impl)
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


import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.manual.internal.layout.algorithm.MutablePolyEdgeGraphLayout;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;


/**
 *
 */
public final class GraphConverter2 {
	private GraphConverter2() {
	}

	/**
	 * Returns a representation of Cytoscape's current network view.
	 * Returns a MutablePolyEdgeGraphLayout, which, when mutated,
	 * has a direct effect on the underlying Cytoscape network view.  You'd
	 * sure as heck better be using the returned object from the AWT event
	 * dispatch thread.  Better yet, lock the Cytoscape desktop somehow
	 * (with a modal dialog for example) while using this returned object.
	 * Movable nodes are defined to be selected nodes in Cytoscape - if no
	 * nodes are selected then all nodes are movable.  If selected node
	 * information changes while we have a reference to this return object,
	 * then the movability of corresponding node also changes.  This is one
	 * reason why it's important to lock the Cytoscape desktop while operating
	 * on this return object.
	 **/
	public static MutablePolyEdgeGraphLayout getGraphReference(double percentBorder,
	                                                           boolean preserveEdgeAnchors,
	                                                           boolean onlySelectedNodesMovable,
															   final CyNetworkView graphView) {
		if (percentBorder < 0.0d)
			throw new IllegalArgumentException("percentBorder < 0.0");

		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;

		for ( View<CyNode> currentNodeView : graphView.getNodeViews() ) {
			minX = Math.min(minX, currentNodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION));
			maxX = Math.max(maxX, currentNodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION));
			minY = Math.min(minY, currentNodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION));
			maxY = Math.max(maxY, currentNodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION));
		}

		final List<CyNode> selectedNodes = CyTableUtil.getNodesInState(graphView.getModel(),CyNetwork.SELECTED,true);
		final boolean noNodesSelected = (!onlySelectedNodesMovable) || (selectedNodes.size() == 0);

/* TODO HANDLE anchors
		for ( View<CyEdge> currentEdgeView : graphView.getEdgeViews() ) {

			if ((!preserveEdgeAnchors)
			    && (noNodesSelected
			       || currentEdgeView.getModel().getSource().getCyRow().get(CyNetwork.SELECTED, Boolean.class)
			       || currentEdgeView.getModel().getTarget().getCyRow().get(CyNetwork.SELECTED, Boolean.class))) {
				currentEdgeView.getBend().removeAllHandles();
			} else {
				List handles = currentEdgeView.getBend().getHandles();

				for (int h = 0; h < handles.size(); h++) {
					Point2D point = (Point2D) handles.get(h);
					minX = Math.min(minX, point.getX());
					maxX = Math.max(maxX, point.getX());
					minY = Math.min(minY, point.getY());
					maxY = Math.max(maxY, point.getY());
				}
			}
		}
	*/

		double border = Math.max(maxX - minX, maxY - minY) * percentBorder * 0.5d;
		final double width = maxX - minX + border + border;
		final double height = maxY - minY + border + border;
		final double xOff = minX - border;
		final double yOff = minY - border;

		final CyNetwork fixedGraph = graphView.getModel();

		return new MutablePolyEdgeGraphLayout() {
				// FixedGraph methods.
				public List<CyNode> nodes() {
					return fixedGraph.getNodeList();
				}

				public List<CyEdge> edges() {
					return fixedGraph.getEdgeList();
				}


				// GraphLayout methods.
				public double getMaxWidth() {
					return width;
				}

				public double getMaxHeight() {
					return height;
				}

				public double getNodePosition(CyNode node, boolean xPosition) {
					View<CyNode> nodeView = graphView.getNodeView(node);

					if (xPosition)
						return (nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION) - xOff);

					return (nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION) - yOff);
				}

				// MutableGraphLayout methods.
				public boolean isMovableNode(CyNode node) {
					if (noNodesSelected)
						return true;

					return fixedGraph.getRow(node).get(CyNetwork.SELECTED,Boolean.class); 
				}

				public void setNodePosition(CyNode node, double xPos, double yPos) {
					View<CyNode> nodeView = graphView.getNodeView(node);
					checkPosition(xPos, yPos);

					if (!isMovableNode(node))
						throw new UnsupportedOperationException("node " + node + " is not movable");

					nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, xPos + xOff);
					nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, yPos + yOff);
				}

				private void checkPosition(double xPos, double yPos) {
					if (Double.isNaN(xPos) || (xPos < 0.0d) || (xPos > getMaxWidth()))
						throw new IllegalArgumentException("X position out of bounds");

					if (Double.isNaN(yPos) || (yPos < 0.0d) || (yPos > getMaxHeight()))
						throw new IllegalArgumentException("Y position out of bounds");
				}
			};

				// PolyEdgeGraphLayout methods.
/* TODO support anchors
				public int getNumAnchors(CyEdge edge) {
					// TODO support anchors
					//return getEdgeView(edge).getBend().getHandles().size();
					return 0;
				}
				public double getAnchorPosition(CyEdge edge, int anchorIndex, boolean xPosition) {
					Point2D point = (Point2D) getEdgeView(edge).getBend().getHandles()
					                              .get(anchorIndex);

					return (xPosition ? (point.getX() - xOff) : (point.getY() - yOff));
				}

				// MutablePolyEdgeGraphLayout methods.
				public void deleteAnchor(int edge, int anchorIndex) {
					checkAnchorIndexBounds(edge, anchorIndex, false);
					checkMutableAnchor(edge);
					getEdgeView(edge).getBend().removeHandle(anchorIndex);
				}

				public void createAnchor(int edge, int anchorIndex) {
					checkAnchorIndexBounds(edge, anchorIndex, true);
					checkMutableAnchor(edge);
					getEdgeView(edge).getBend().addHandle(anchorIndex, new Point2D.Double());

					Point2D src = ((anchorIndex == 0)
					               ? (new Point2D.Double(getNodePosition(edgeSource(edge), true),
					                                     getNodePosition(edgeSource(edge), false)))
					               : (new Point2D.Double(getAnchorPosition(edge, anchorIndex - 1,
					                                                       true),
					                                     getAnchorPosition(edge, anchorIndex - 1,
					                                                       false))));
					Point2D trg = ((anchorIndex == (getNumAnchors(edge) - 1))
					               ? (new Point2D.Double(getNodePosition(edgeTarget(edge), true),
					                                     getNodePosition(edgeTarget(edge), false)))
					               : (new Point2D.Double(getAnchorPosition(edge, anchorIndex + 1,
					                                                       true),
					                                     getAnchorPosition(edge, anchorIndex + 1,
					                                                       false))));
					setAnchorPosition(edge, anchorIndex, (src.getX() + trg.getX()) / 2.0d,
					                  (src.getY() + trg.getY()) / 2.0d);
				}

				public void setAnchorPosition(int edge, int anchorIndex, double xPos, double yPos) {
					checkAnchorIndexBounds(edge, anchorIndex, false);
					checkMutableAnchor(edge);
					checkPosition(xPos, yPos);
					getEdgeView(edge).getBend()
					    .moveHandle(anchorIndex, new Point2D.Double(xPos + xOff, yPos + yOff));
				}
				private void checkAnchorIndexBounds(int edge, int anchorIndex, boolean create) {
					int numAnchors = getNumAnchors(edge) + (create ? 0 : (-1));

					if ((anchorIndex < 0) || (anchorIndex > numAnchors))
						throw new IndexOutOfBoundsException("anchor index out of bounds");
				}

				private void checkMutableAnchor(int edge) {
					int srcNode = edgeSource(edge);
					int trgNode = edgeTarget(edge);

					if ((!isMovableNode(srcNode)) && (!isMovableNode(trgNode)))
						throw new UnsupportedOperationException("anchors at specified edge cannot be changed");
				}
				*/
	}
}
