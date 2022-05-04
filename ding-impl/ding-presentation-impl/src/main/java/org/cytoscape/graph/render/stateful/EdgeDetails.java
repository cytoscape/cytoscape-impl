package org.cytoscape.graph.render.stateful;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.Collection;

import org.cytoscape.graph.render.immed.EdgeAnchors;
import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.EdgeStacking;
import org.cytoscape.view.presentation.property.values.Justification;
import org.cytoscape.view.presentation.property.values.Position;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

/**
 * Defines the visual properties of an edge.  Even though this class is not
 * declared abstract, in most situations it makes sense to override at least
 * some of its methods (especially segmentThickness()) in order to gain
 * control over edge visual properties.<p>
 * To understand the significance of each method's return value, it makes
 * sense to become familiar with the API cytoscape.render.immed.GraphGraphics.
 */
public interface EdgeDetails {
	/**
	 * ******* This is not used anymore *************
	 * Specifies that an anchor point lies at the midpoint of an edge.
	 *
	public static final byte EDGE_ANCHOR_MIDPOINT = 16;

	/**
	 * Specifies that an anchor point lies at an edge's endpoint at source
	 * node.
	 *
	public static final byte EDGE_ANCHOR_SOURCE = 17;

	/**
	 * Specifies that an anchor point lies at an edge's endpoint at target
	 * node.
	 *
	public static final byte EDGE_ANCHOR_TARGET = 18;
	*/
	
	
	public static final int CURVED_LINES = 1;
	public static final int STRAIGHT_LINES = 2;
	


	/**
	 * Returns the color of edge in low detail rendering mode.
	 * It is an error to return null
	 * in this method.<p>
	 * In low detail rendering mode, this is the only method from this class
	 * that is looked at.  The rest of the methods in this class define visual
	 * properties that are used in full detail rendering mode.  In low detail
	 * rendering mode translucent colors are not supported whereas in full
	 * detail rendering mode they are.
	 */
	public Color getColorLowDetail(CyNetworkViewSnapshot netView, View<CyEdge> edgeView) ;

	/**
	 * Returns a GraphGraphics.ARROW_* constant; this defines the arrow
	 * to use when rendering the edge endpoint touching source node.
	 * Take note of certain constraints specified in
	 * GraphGraphics.drawEdgeFull().
	 */
	public ArrowShape getSourceArrowShape(View<CyEdge> edgeView);

	/**
	 * Returns the size of the arrow at edge endpoint touching source node.
	 * This return value is ignored
	 * if sourceArrow(edge) returns GraphGraphics.ARROW_NONE.
	 * Take note of certain constraints specified in
	 * GraphGraphics.drawEdgeFull().
	 */
	public float getSourceArrowSize(View<CyEdge> edgeView);

	/**
	 * Returns the paint of the arrow at edge endpoint touching source node.
	 * This return value is ignored if
	 * sourceArrow(edge) returns GraphGraphics.ARROW_NONE 
	 * it is an error to return null.
	 */
	public Paint getSourceArrowPaint(View<CyEdge> edgeView);

	/**
	 * Returns a GraphGraphics.ARROW_* constant; this defines the arrow
	 * to use when rendering the edge endpoint at the target node.
	 * Take note of certain constraints specified in
	 * GraphGraphics.drawEdgeFull().
	 */
	public ArrowShape getTargetArrowShape(View<CyEdge> edgeView);

	/**
	 * Returns the size of the arrow at edge endpoint touching target node.
	 * Take note of certain constraints specified
	 * in GraphGraphics.drawEdgeFull().
	 */
	public float getTargetArrowSize(View<CyEdge> edgeView);

	/**
	 * Returns the paint of the arrow at edge endpoint touching target node.
	 * This return value is ignored if
	 * targetArrow(edge) returns GraphGraphics.ARROW_NONE,
	 * it is an error to return null.
	 */
	public Paint getTargetArrowPaint(View<CyEdge> edgeView);

	/**
	 * Returns edge anchors to use when rendering this edge.
	 * Returning null is the optimal
	 * way to specify that this edge has no anchors.  Take note of certain
	 * constraints, specified in GraphGraphics.drawEdgeFull(), pertaining to
	 * edge anchors.<p>
	 * The anchors returned are interpreted such that the anchor at index zero
	 * (the "first" anchor) is the anchor next to the source node of this edge;
	 * the last anchor is the anchor next to the target node of this edge.  The
	 * rendering engine works such that if the first anchor lies inside
	 * the source node shape or if the last anchor lies inside the target
	 * node shape, the edge is not rendered.
	 */
//	default public EdgeAnchors getAnchors(View<CyEdge> edgeView) {
//		return null;
//	}

	/**
	 * For edges with anchors, the anchors can be rendered as squares.  To render
	 * an anchor, return a positive value in this method.  If zero is returned
	 * no edge anchor is rendered.  By default this method returns zero.
	 */
	public float getAnchorSize(View<CyEdge> edgeView, int anchorInx);

	/**
	 * Returns the paint to use when rendering an edge anchor.  The output of
	 * this method is ignored if anchorSize(edge, anchorInx) returned zero;
	 * otherwise, a non-null value must be returned.  By default this method
	 * returns null.
	 */
	public Paint getAnchorPaint(View<CyEdge> edgeView, int anchorInx);

	/**
	 * Returns the thickness of the edge segment.
	 * <font color="red">By default this method returns zero.</font>
	 * Take note of certain constraints specified in
	 * GraphGraphics.drawEdgeFull().
	 */
	public float getWidth(View<CyEdge> edgeView);

	
	public Stroke getStroke(View<CyEdge> edgeView);

	/**
	 * Returns the paint of the edge segment.
	 * It is an error to return null in this method.
	 */
	public Paint getPaint(View<CyEdge> edgeView);

	/**
	 * Returns the number of labels that this edge has.
	 */
	public int getLabelCount(View<CyEdge> edgeView);

	/**
	 * Returns a label's text.
	 * This method is only called by the rendering engine if labelCount(edge)
	 * returns a value greater than zero.  It is an error to return null if this
	 * method is called by the rendering engine.<p>
	 * To specify multiple lines of text in an edge label, simply insert the
	 * '\n' character between lines of text.
	 */
	public String getLabelText(View<CyEdge> edgeView);

	/**
	 * Returns the font to use when rendering this label.  
	 * This method is only called by the rendering
	 * engine if labelCount(edge) returns a value greater than zero.  It is an
	 * error to return null if this method is called by the rendering engine.
	 */
	public Font getLabelFont(View<CyEdge> edgeView, boolean forPdf);
	
	default Font getLabelFont(View<CyEdge> edgeView) {
		return getLabelFont(edgeView, false);
	}

  /**
   * Returns the angle (in degrees) to rotate the label.
	 * This method is only called by the rendering engine
	 * if labelCount(edge) returns a value greater than zero. It is an error to
	 * return null if this method is called by the rendering engine.
	 */
	default Double getLabelRotation(View<CyEdge> edge) { return 0d; }

  /**
   * Returns the angle (in degrees) to rotate the label, taking into
   * account autorotateion, if it's set.
	 * This method is only called by the rendering engine
	 * if labelCount(edge) returns a value greater than zero. It is an error to
	 * return null if this method is called by the rendering engine.
	 */
	default Double getLabelRotation(View<CyEdge> edge, double rise, double run) { return 0d; }

  /**
   * Returns 'true' if we want to autorotate edge labels.
   */
  default boolean getLabelAutorotate(View<CyEdge> edge) { return false; }

	/**
	 * Returns the paint of a text label. 
	 * This method is only called by the rendering engine if
	 * labelCount(edge) returns a value greater than zero.  It is an error to
	 * return null if this method is called by the rendering engine.
	 */
	public Paint getLabelPaint(View<CyEdge> edge);
	
	Paint getLabelBackgroundPaint(View<CyEdge> edge);
	
	byte getLabelBackgroundShape(View<CyEdge> edge);

	/**
	 * By returning one of the NodeDetails.ANCHOR_* constants, specifies where
	 * on a text label's logical bounds box an anchor point lies.  This
	 * <i>text anchor point</i> together with the edge anchor point and label
	 * offset vector determines where, relative to the edge, the text's logical
	 * bounds box is to be placed.  The text's logical bounds box is placed
	 * such that the label offset vector plus the edge anchor point equals the
	 * text anchor point.<p>
	 * This method is only called by the rendering engine if labelCount(edge)
	 * returns a value greater than zero.
	 * @see NodeDetails#ANCHOR_CENTER
	 * @see #getLabelEdgeAnchor(int, int)
	 * @see #getLabelOffsetVectorX(int, int)
	 * @see #getLabelOffsetVectorY(int, int)
	 */
	default public Position getLabelTextAnchor(View<CyEdge> edgeView) {
		return Position.CENTER;
	}

	/**
	 * By returning one of the EDGE_ANCHOR_* constants, specifies where on
	 * an edge an anchor point lies.  This <i>edge anchor point</i> together
	 * with the text anchor point and label offset vector determines where,
	 * relative to the edge, the text's logical bounds box is to be placed.
	 * The text's logical bounds box is placed such that the label offset
	 * vector plus the edge anchor point equals the text anchor point.<p>
	 * This method is only called by the rendering engine if labelCount(edge) 
	 * returns a value greater than zero.
	 * @see #EDGE_ANCHOR_MIDPOINT
	 * @see #getLabelTextAnchor(int, int)
	 * @see #getLabelOffsetVectorX(int, int)
	 * @see #getLabelOffsetVectorY(int, int)
	 */
	default public Position getLabelEdgeAnchor(View<CyEdge> edgeView) {
		return Position.CENTER;
	}

	/**
	 * Specifies the X component of the vector that separates a text anchor
	 * point from an edge anchor point.  This <i>label offset vector</i>
	 * together with the text anchor point and edge anchor point determines
	 * where, relative to the edge, the text's logical bounds box is to be
	 * placed.  The text's logical bounds box is placed such that the label
	 * offset vector plus the edge anchor point equals the text anchor point.<p>
	 * By default this method always returns zero.  This method is only called
	 * by the rendering engine if labelCount(edge) returns a value greater than
	 * zero.
	 * @see #getLabelOffsetVectorY(int, int)
	 * @see #getLabelTextAnchor(int, int)
	 * @see #getLabelEdgeAnchor(int, int)
	 */
	default public float getLabelOffsetVectorX(View<CyEdge> edgeView) {
		return 0.0f;
	}

	/**
	 * Specifies the Y component of the vector that separates a text anchor
	 * point from an edge anchor point.  This <i>label offset vector</i>
	 * together with the text anchor point and edge anchor point determines
	 * where, relative to the edge, the text's logical bounds box is to be
	 * placed.  The text's logical bounds box is placed such that the label
	 * offset vector plus the edge anchor point equals the text anchor point.<p>
	 * By default this method always returns zero.  This method is only called
	 * by the rendering engine if labelCount(edge) returns a value greater than
	 * zero.
	 * @see #getLabelOffsetVectorX(int, int)
	 * @see #getLabelTextAnchor(int, int)
	 * @see #getLabelEdgeAnchor(int, int)
	 */
	default public float getLabelOffsetVectorY(View<CyEdge> edgeView) {
		return 0.0f;
	}

	/**
	 * By returning one of the NodeDetails.LABEL_WRAP_JUSTIFY_* constants,
	 * determines how to justify an edge label spanning multiple lines.  The
	 * choice made here does not affect the size of the logical bounding box
	 * of an edge label's text.  The lines of text are justified within that
	 * logical bounding box.<p>
	 * By default this method always returns
	 * NodeDetails.LABEL_WRAP_JUSTIFY_CENTER.  This return value is ignored
	 * if labelText(edge, labelInx) returns a text string that does not span
	 * multiple lines.
	 * @see NodeDetails#LABEL_WRAP_JUSTIFY_CENTER
	 */
	default public Justification getLabelJustify(View<CyEdge> edgeView) {
		return Justification.JUSTIFY_CENTER;
	}

	/**
	 * Returns the width of the label. 
	 * <font color="red">By default this method returns 100.</font>
	 * Take note of certain constraints specified in
	 * GraphGraphics.drawEdgeFull().
	 */
	default public double getLabelWidth(View<CyEdge> edgeView) {
		return 100.0;
	}

	default public boolean isVisible(View<CyEdge> edgeView) {
		return true;
	}
	
	EdgeStacking getStacking(View<CyEdge> edgeView);
	
	float getStackingDensity(View<CyEdge> edgeView);

	Integer getLineCurved(View<CyEdge> edgeView);

	Bend getBend(View<CyEdge> edgeView);

	boolean isSelected(View<CyEdge> edgeView);

	String getTooltipText(View<CyEdge> edgeView);

	EdgeAnchors getAnchors(CyNetworkViewSnapshot netView, View<CyEdge> edgeView);
	
	Paint getUnselectedPaint(View<CyEdge> edgeView);

	Paint getSelectedPaint(View<CyEdge> edgeView);
	
	public void advanceAnimatedEdges();
	
	public void updateAnimatedEdges(Collection<View<CyEdge>> animatedEdges);
	
}
