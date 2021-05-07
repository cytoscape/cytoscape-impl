package org.cytoscape.view.manual.internal.scale;

/*
 * #%L
 * Cytoscape Manual Layout Impl (manual-layout-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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


import org.cytoscape.math.xform.AffineTransform3D;
import org.cytoscape.math.xform.Scale3D;
import org.cytoscape.math.xform.Translation3D;

import org.cytoscape.view.manual.internal.layout.algorithm.MutablePolyEdgeGraphLayout;

import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;



public final class ScaleLayouter {
	public enum Direction {
		X_AXIS_ONLY,
		Y_AXIS_ONLY,
		BOTH_AXES
	};

	private final MutablePolyEdgeGraphLayout m_graph;
	private final Translation3D m_translationToOrig;
	private final Translation3D m_translationFromOrig;

	/**
	 * This operation does not affect edge anchor points which belong to edges
	 * containing at least one non-movable node.
	 **/
	public ScaleLayouter(MutablePolyEdgeGraphLayout graph) {
		m_graph = graph;

		double xMin = Double.MAX_VALUE;
		double xMax = Double.MIN_VALUE;
		double yMin = Double.MAX_VALUE;
		double yMax = Double.MIN_VALUE;

		for ( CyEdge edge : m_graph.edges() ) {

			if (!(m_graph.isMovableNode(edge.getSource()))
			    && m_graph.isMovableNode(edge.getTarget()))
				continue;
/* TODO handle anchors
			final int numAnchors = m_graph.getNumAnchors(edge);

			for (int j = 0; j < numAnchors; j++) {
				double anchXPosition = m_graph.getAnchorPosition(edge, j, true);
				double anchYPosition = m_graph.getAnchorPosition(edge, j, false);
				xMin = Math.min(xMin, anchXPosition);
				xMax = Math.max(xMax, anchXPosition);
				yMin = Math.min(yMin, anchYPosition);
				yMax = Math.max(yMax, anchYPosition);
			}
		*/
		}

		for ( CyNode node : m_graph.nodes() ) {

			if (!m_graph.isMovableNode(node))
				continue;

			double nodeXPosition = m_graph.getNodePosition(node, true);
			double nodeYPosition = m_graph.getNodePosition(node, false);
			xMin = Math.min(xMin, nodeXPosition);
			xMax = Math.max(xMax, nodeXPosition);
			yMin = Math.min(yMin, nodeYPosition);
			yMax = Math.max(yMax, nodeYPosition);
		}

		if (xMax < 0) { // Nothing is movable.
			m_translationToOrig = null;
			m_translationFromOrig = null;
		} else {
			final double xRectCenter = (xMin + xMax) / 2.0d;
			final double yRectCenter = (yMin + yMax) / 2.0d;
			m_translationToOrig = new Translation3D(-xRectCenter, -yRectCenter, 0.0d);
			m_translationFromOrig = new Translation3D(xRectCenter, yRectCenter, 0.0d);
		}
	}

	private final double[] m_pointBuff = new double[3];

	/**
	 * A scaleFactor of 1.0 does not move anything.
	 *
	 * @exception IllegalArgumentException if
	 *   scaleFactor < 0.001 or if scaleFactor > 1000.0.
	 **/
	public void scaleGraph(double scaleFactor, final Direction direction) {
		if ((scaleFactor < 0.001d) || (scaleFactor > 1000.0d))
			throw new IllegalArgumentException("scaleFactor is outside allowable range [0.001, 1000.0]");

		if (m_translationToOrig == null)
			return;

		double xFactor = scaleFactor, yFactor = scaleFactor;
		switch (direction) {
		case X_AXIS_ONLY:
			yFactor = 1.0;
			break;
		case Y_AXIS_ONLY:
			xFactor = 1.0;
			break;
		case BOTH_AXES:
			/* Intentionally empty. */
			break;
		}

		final AffineTransform3D xform = m_translationToOrig.concatenatePost(
			(new Scale3D(xFactor, yFactor, 1.0d)).concatenatePost(m_translationFromOrig));


		for ( CyNode node : m_graph.nodes() ) {
			if (!m_graph.isMovableNode(node))
				continue;

			m_pointBuff[0] = m_graph.getNodePosition(node, true);
			m_pointBuff[1] = m_graph.getNodePosition(node, false);
			m_pointBuff[2] = 0.0d;
			xform.transformArr(m_pointBuff);
			m_graph.setNodePosition(node, m_pointBuff[0], m_pointBuff[1]);
		}

		for ( CyEdge edge : m_graph.edges() ) {

			if (!(m_graph.isMovableNode(edge.getSource()))
			    && m_graph.isMovableNode(edge.getTarget()))
				continue;
/* TODO support anchors
			final int numAnchors = m_graph.getNumAnchors(edge);

			for (int j = 0; j < numAnchors; j++) {
				m_pointBuff[0] = m_graph.getAnchorPosition(edge, j, true);
				m_pointBuff[1] = m_graph.getAnchorPosition(edge, j, false);
				m_pointBuff[2] = 0.0d;
				xform.transformArr(m_pointBuff);
				m_graph.setAnchorPosition(edge, j, m_pointBuff[0], m_pointBuff[1]);
			}
			*/
		}
	}
}
