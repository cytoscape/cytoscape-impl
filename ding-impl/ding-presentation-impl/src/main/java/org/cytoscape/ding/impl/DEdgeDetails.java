/*
 Copyright (c) 2006, 2007, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.ding.impl;


import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.graph.render.immed.EdgeAnchors;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.util.intr.IntEnumerator;
import org.cytoscape.util.intr.IntObjHash;
import org.cytoscape.util.intr.MinIntHeap;


class DEdgeDetails extends IntermediateEdgeDetails {
	
	final DGraphView m_view;
	final IntObjHash m_colorsLowDetail = new IntObjHash();
	final Object m_deletedEntry = new Object();
	
	final Map<Integer, Float> m_segmentThicknesses = new HashMap<Integer, Float>();
	final Map<Integer, Stroke> m_segmentStrokes = new HashMap<Integer, Stroke>();
	final Map<Integer, Byte> m_sourceArrows = new HashMap<Integer, Byte>();
	final Map<Integer, Paint> m_sourceArrowPaints = new HashMap<Integer, Paint>();
	final Map<Integer, Byte> m_targetArrows = new HashMap<Integer, Byte>();
	final Map<Integer, Paint> m_targetArrowPaints = new HashMap<Integer, Paint>();
	final Map<Integer, Paint> m_segmentPaints = new HashMap<Integer, Paint>();
	final Map<Integer, Integer> m_labelCounts = new HashMap<Integer, Integer>();
	final Map<Integer, String> m_labelTexts = new HashMap<Integer, String>();
	final Map<Integer, Font> m_labelFonts = new HashMap<Integer, Font>();
	final Map<Integer, Paint> m_labelPaints = new HashMap<Integer, Paint>();
	final Map<Integer, Double> m_labelWidths = new HashMap<Integer, Double>();
	
	private final MinIntHeap m_heap = new MinIntHeap();
	private final float[] m_extentsBuff = new float[4];

	DEdgeDetails(final DGraphView view) {
		m_view = view;
	}

	void unregisterEdge(final int edgeIdx) {
		final Object colorDetail = m_colorsLowDetail.get(edgeIdx);
		if ((colorDetail != null) && (colorDetail != m_deletedEntry))
			m_colorsLowDetail.put(edgeIdx, m_deletedEntry);

		m_segmentThicknesses.remove(edgeIdx);
		m_segmentStrokes.remove(edgeIdx);
		m_sourceArrows.remove(edgeIdx);
		m_sourceArrowPaints.remove(edgeIdx);
		m_targetArrows.remove(edgeIdx);
		m_targetArrowPaints.remove(edgeIdx);
		m_segmentPaints.remove(edgeIdx);
		m_labelCounts.remove(edgeIdx);
		m_labelTexts.remove(edgeIdx);
		m_labelFonts.remove(edgeIdx);
		m_labelPaints.remove(edgeIdx);
		m_labelWidths.remove(edgeIdx);
	}


	public Color colorLowDetail(final int edge) {
		final Object o = m_colorsLowDetail.get(edge);

		if ((o == null) || (o == m_deletedEntry))
			return super.colorLowDetail(edge);

		return (Color) o;
	}

	/*
	 * A null color has the special meaning to remove overridden color.
	 */
	void overrideColorLowDetail(final int edge, final Color color) {
		if ((color == null) || color.equals(super.colorLowDetail(edge))) {
			final Object val = m_colorsLowDetail.get(edge);

			if ((val != null) && (val != m_deletedEntry))
				m_colorsLowDetail.put(edge, m_deletedEntry);
		} else
			m_colorsLowDetail.put(edge, color);
	}

	@Override
	public byte sourceArrow(final int edge) {
		final Byte arrow = m_sourceArrows.get(edge);
		if (arrow == null)
			return super.sourceArrow(edge);

		return arrow;
	}

	/*
	 * A non-negative arrowType has the special meaning to remove overridden
	 * arrow.
	 */
	void overrideSourceArrow(final int edge, final byte arrowType) {
		if ((arrowType >= 0) || (arrowType == super.sourceArrow(edge)))
			m_sourceArrows.remove(edge);
		else
			m_sourceArrows.put(edge, arrowType);
	}


	public Paint sourceArrowPaint(final int edge) {
		final Paint arrowPaint = m_sourceArrowPaints.get(edge);
		if (arrowPaint == null)
			return super.sourceArrowPaint(edge);

		return arrowPaint;
	}

	/*
	 * A null paint has the special meaning to remove overridden paint.
	 */
	void overrideSourceArrowPaint(final int edge, final Paint paint) {
		if ((paint == null) || paint.equals(super.sourceArrowPaint(edge)))
			m_sourceArrowPaints.remove(edge);
		else
			m_sourceArrowPaints.put(edge, paint);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte targetArrow(final int edge) {
		final Byte arrow = m_targetArrows.get(edge);

		if (arrow == null)
			return super.targetArrow(edge);

		return arrow;
	}

	/*
	 * A non-negative arrowType has the special meaning to remove overridden
	 * arrow.
	 */
	void overrideTargetArrow(final int edge, final byte arrowType) {
		if ((arrowType >= 0) || (arrowType == super.targetArrow(edge)))
			m_targetArrows.remove(edge);
		else
			m_targetArrows.put(edge, arrowType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Paint targetArrowPaint(final int edge) {
		final Paint arrowPaint = m_targetArrowPaints.get(edge);
		if (arrowPaint == null)
			return super.targetArrowPaint(edge);

		return arrowPaint;
	}

	/*
	 * A null paint has the special meaning to remove overridden paint.
	 */
	void overrideTargetArrowPaint(final int edge, final Paint paint) {
		if ((paint == null) || paint.equals(super.targetArrowPaint(edge)))
			m_targetArrowPaints.remove(edge);
		else
			m_targetArrowPaints.put(edge, paint);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public EdgeAnchors anchors(final int edge) {
		final EdgeAnchors returnThis = (EdgeAnchors) m_view.getDEdgeView(edge);

		if (returnThis.numAnchors() > 0) 
			return returnThis;

		final CyNetwork graph = m_view.model;
		final CyEdge edgeObj = graph.getEdge(edge);
		if (edgeObj == null) {
			System.err.println("in DEdgeDetails.anchors(): Warning: non-existent edge="+edge);
			return returnThis;
		}

		final CyNode source = edgeObj.getSource();
		if (source == null) {
			System.err.println("in DEdgeDetails.anchors(): Warning: non-existent source!");
			return returnThis;
		}
		final int srcNode = source.getIndex();

		final CyNode target = edgeObj.getTarget();
		if (target == null) {
			System.err.println("in DEdgeDetails.anchors(): Warning: non-existent target!");
			return returnThis;
		}
		final int trgNode = target.getIndex();

		// Calculate anchors necessary for self edges.
		if (srcNode == trgNode) { // Self-edge.
			final CyNode nodeObj = edgeObj.getSource();
			final int node = nodeObj.getIndex(); 
			m_view.m_spacial.exists(node, m_extentsBuff, 0);

			final double w = ((double) m_extentsBuff[2]) - m_extentsBuff[0];
			final double h = ((double) m_extentsBuff[3]) - m_extentsBuff[1];
			final double x = (((double) m_extentsBuff[0]) + m_extentsBuff[2]) / 2.0d;
			final double y = (((double) m_extentsBuff[1]) + m_extentsBuff[3]) / 2.0d;
			final double nodeSize = Math.max(w, h);
			int i = 0;
			
			final List<CyEdge> selfEdges = graph.getConnectingEdgeList(nodeObj, nodeObj, CyEdge.Type.ANY);

			for ( CyEdge e2obj : selfEdges ) {
				final int e2 = e2obj.getIndex();

				if (e2 == edge)
					break;

				if (((EdgeAnchors) m_view.getDEdgeView(e2)).numAnchors() == 0)
					i++;
			}

			final int inx = i;
			return new EdgeAnchors() {
					public int numAnchors() {
						return 2;
					}

					public void getAnchor(int anchorInx, float[] anchorArr, int offset) {
						if (anchorInx == 0) {
							anchorArr[offset] = (float) (x - (((inx + 3) * nodeSize) / 2.0d));
							anchorArr[offset + 1] = (float) y;
						} else if (anchorInx == 1) {
							anchorArr[offset] = (float) x;
							anchorArr[offset + 1] = (float) (y - (((inx + 3) * nodeSize) / 2.0d));
						}
					}
				};
		}

		// Now add "invisible" anchors to edges for the case where multiple edges
		// exist between two nodes. This has no effect if user specified anchors
		// exist on the edge.
		while (true) {
			// By consistently ordering the source and target nodes, dx and dy will always
			// be calculated according to the same orientation. This allows the offset
			// calculation to toggle the edges from side to side without any overlap.
			final int tmpSrc = Math.min( srcNode, trgNode ); 
			final int tmpTrg = Math.max( srcNode, trgNode ); 

			// Sort the connecting edges.
			final List<CyEdge> selfEdges = graph.getConnectingEdgeList(edgeObj.getSource(), edgeObj.getTarget(), CyEdge.Type.ANY);

			m_heap.empty();

			for ( CyEdge e : selfEdges ) 
				m_heap.toss(e.getIndex());

			final IntEnumerator otherEdges = m_heap.orderedElements(false);

			int otherEdge = otherEdges.nextInt();

			// If the first other edge is the same as this edge, 
			// (i.e. we're at the end of the list?).
			if (otherEdge == edge)
				break;

			// So we don't count the other edge twice?
			int i = (((EdgeAnchors) m_view.getDEdgeView(otherEdge)).numAnchors() == 0) ? 1 : 0;

			// Count the number of other edges.
			while (true) {
				if (edge == (otherEdge = otherEdges.nextInt()))
					break;

				if (((EdgeAnchors) m_view.getDEdgeView(otherEdge)).numAnchors() == 0)
					i++;
			}

			final int inx = i;

			// Get source node size and position.
			m_view.m_spacial.exists(tmpSrc, m_extentsBuff, 0);
			final double srcW = ((double) m_extentsBuff[2]) - m_extentsBuff[0];
			final double srcH = ((double) m_extentsBuff[3]) - m_extentsBuff[1];
			final double srcX = (((double) m_extentsBuff[0]) + m_extentsBuff[2]) / 2.0d;
			final double srcY = (((double) m_extentsBuff[1]) + m_extentsBuff[3]) / 2.0d;

			// Get target node size and position.
			m_view.m_spacial.exists(tmpTrg, m_extentsBuff, 0);
			final double trgW = ((double) m_extentsBuff[2]) - m_extentsBuff[0];
			final double trgH = ((double) m_extentsBuff[3]) - m_extentsBuff[1];
			final double trgX = (((double) m_extentsBuff[0]) + m_extentsBuff[2]) / 2.0d;
			final double trgY = (((double) m_extentsBuff[1]) + m_extentsBuff[3]) / 2.0d;

			// Used for determining the space between the edges.
			final double nodeSize = Math.max(Math.max(Math.max(srcW, srcH), trgW), trgH);

			// Midpoint between nodes.
			final double midX = (srcX + trgX) / 2;
			final double midY = (srcY + trgY) / 2;

			// Distance in X and Y dimensions.
			// Note that dx and dy may be negative.  This is OK, because this will ensure
			// that the handle is always correctly placed offset from the midpoint of, 
			// and perpendicular to, the original edge.
			final double dx = trgX - srcX;
			final double dy = trgY - srcY;

			// Distance or length between nodes.
			final double len = Math.sqrt((dx * dx) + (dy * dy));

			if (((float) len) == 0.0f) 
				break;

			// This determines which side of the first edge and how far from the first
			// edge the other edge should be placed.
			// -  Divide by 2 puts consecutive edges at the same distance from the center
			//    because of integer math.
			// -  Modulo puts consecutive edges on opposite sides.
			// -  Node size is for consistent scaling.
			final double offset = ((inx + 1) / 2) * (inx % 2 == 0 ? 1 : -1) * nodeSize;

			// Depending on orientation sine or cosine. This adjusts the length
			// of the offset according the appropriate X and Y dimensions.
			final double normX = dx / len;
			final double normY = dy / len;

			// Calculate the anchor points.
			final double anchorX = midX + (offset * normY);
			final double anchorY = midY - (offset * normX);

			return new EdgeAnchors() {
					public int numAnchors() {
						return 1;
					}

					public void getAnchor(int inx, float[] arr, int off) {
						arr[off] = (float) anchorX;
						arr[off + 1] = (float) anchorY;
					}
				};
		}

		return returnThis;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float anchorSize(final int edge, final int anchorInx) {
		if (m_view.getDEdgeView(edge).isSelected() && (((DEdgeView) m_view.getDEdgeView(edge)).numAnchors() > 0))
			return m_view.getAnchorSize();
		else
			return 0.0f;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Paint anchorPaint(final int edge, int anchorInx) {
		if (((DEdgeView) (m_view.getDEdgeView(edge))).m_lineType == DEdgeView.STRAIGHT_LINES)
			anchorInx = anchorInx / 2;

		if (m_view.m_selectedAnchors.count((edge << 6) | anchorInx) > 0)
			return m_view.getAnchorSelectedPaint();
		else
			return m_view.getAnchorUnselectedPaint();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float segmentThickness(final int edge) {
		final Float thickness = m_segmentThicknesses.get(edge);
		if (thickness == null)
			return super.segmentThickness(edge);

		return thickness;
	}

	/*
	 * A negative thickness value has the special meaning to remove overridden
	 * thickness.
	 */
	void overrideSegmentThickness(final int edge, final float thickness) {
		if ((thickness < 0.0f) || (thickness == super.segmentThickness(edge)))
			m_segmentThicknesses.remove(edge);
		else
			m_segmentThicknesses.put(edge, thickness);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Stroke segmentStroke(final int edge) {
		final Stroke stroke = m_segmentStrokes.get(edge);
		if (stroke == null)
			return super.segmentStroke(edge);

		return stroke;
	}


	/*
	 * A null paint has the special meaning to remove overridden paint.
	 */
	void overrideSegmentStroke(int edge, Stroke stroke) {
		if ((stroke == null) || stroke.equals(super.segmentStroke(edge)))
			m_segmentStrokes.remove(edge);
		else
			m_segmentStrokes.put(edge, stroke);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Paint segmentPaint(final int edge) {
		final Paint paint = m_segmentPaints.get(edge);
		if (paint == null)
			return super.segmentPaint(edge);

		return paint;
	}


	/*
	 * A negative length value has the special meaning to remove overridden
	 * length.
	 */
	void overrideSegmentPaint(final int edge, final Paint paint) {
		if ((paint == null) || (paint == super.segmentPaint(edge)))
			m_segmentPaints.remove(edge);
		else
			m_segmentPaints.put(edge, paint);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int labelCount(final int edge) {
		final Integer i = m_labelCounts.get(edge);
		if (i == null)
			return super.labelCount(edge);

		return i;
	}


	/*
	 * A negative labelCount has the special meaning to remove overridden count.
	 */
	void overrideLabelCount(final int edge, final int labelCount) {
		if ((labelCount < 0) || (labelCount == super.labelCount(edge)))
			m_labelCounts.remove(edge);
		else
			m_labelCounts.put(edge, labelCount);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String labelText(final int edge, final int labelInx) {
		final long key = (((long) edge) << 32) | ((long) labelInx);
		final String text = m_labelTexts.get(key);

		if (text == null)
			return super.labelText(edge, labelInx);

		return text;
	}

	
	/*
	 * A null text has the special meaning to remove overridden text.
	 */
	void overrideLabelText(final int edge, final int labelInx, final String text) {
		final long key = (((long) edge) << 32) | ((long) labelInx);

		if ((text == null) || text.equals(super.labelText(edge, labelInx)))
			m_labelTexts.remove(key);
		else
			m_labelTexts.put((int) key, text);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Font labelFont(final int edge, final int labelInx) {
		final long key = (((long) edge) << 32) | ((long) labelInx);
		final Font font = m_labelFonts.get(key);

		if (font == null)
			return super.labelFont(edge, labelInx);

		return font;
	}

	/*
	 * A null font has the special meaning to remove overridden font.
	 */
	void overrideLabelFont(final int edge, final int labelInx, final Font font) {
		final long key = (((long) edge) << 32) | ((long) labelInx);

		if ((font == null) || font.equals(super.labelFont(edge, labelInx)))
			m_labelFonts.remove(key);
		else
			m_labelFonts.put((int) key, font);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Paint labelPaint(final int edge, final int labelInx) {
		final long key = (((long) edge) << 32) | ((long) labelInx);
		final Paint paint = m_labelPaints.get(key);

		if (paint == null)
			return super.labelPaint(edge, labelInx);

		return paint;
	}

	/*
	 * A null paint has the special meaning to remove overridden paint.
	 */
	void overrideLabelPaint(final int edge, final int labelInx, final Paint paint) {
		final long key = (((long) edge) << 32) | ((long) labelInx);

		if ((paint == null) || paint.equals(super.labelPaint(edge, labelInx)))
			m_labelPaints.remove(key);
		else
			m_labelPaints.put((int) key, paint);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float sourceArrowSize(final int edge) {
		// For the half arrows, we need to scale multiplicatively
		// so that the arrow matches the line.
		final int arrowType = sourceArrow(edge);
		if (arrowType == GraphGraphics.ARROW_HALF_TOP ||
		     arrowType == GraphGraphics.ARROW_HALF_BOTTOM )
			 return (segmentThickness(edge) * DEdgeView.DEFAULT_ARROW_SIZE);

		// For all other arrows we can scale additively.  This produces
		// less egregiously big arrows.
		else
			return (segmentThickness(edge) + DEdgeView.DEFAULT_ARROW_SIZE);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The arrow size will scale with the edge width.
	 */
	@Override
	public float targetArrowSize(final int edge) {
		// For the half arrows, we need to scale multiplicatively
		// so that the arrow matches the line.
		final int arrowType = targetArrow(edge);
		if ( arrowType == GraphGraphics.ARROW_HALF_TOP ||
		     arrowType == GraphGraphics.ARROW_HALF_BOTTOM )
			 return (segmentThickness(edge) * DEdgeView.DEFAULT_ARROW_SIZE);
		// For all other arrows we can scale additively.  This produces
		// less egregiously big arrows.
		else
			return (segmentThickness(edge) + DEdgeView.DEFAULT_ARROW_SIZE);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public double labelWidth(final int edge) {
		final Double width = m_labelWidths.get(edge);
		if (width == null)
			return super.labelWidth(edge);

		return width;
	}

	/*
	 * A negative width value has the special meaning to remove overridden width.
	 */
	void overrideLabelWidth(final int edge, final double width) {
		if ((width < 0.0) || (width == super.labelWidth(edge)))
			m_labelWidths.remove(edge);
		else
			m_labelWidths.put(edge, width);
	}

}
