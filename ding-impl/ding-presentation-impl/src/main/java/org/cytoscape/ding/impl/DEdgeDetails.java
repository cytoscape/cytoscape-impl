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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.ding.EdgeView;
import org.cytoscape.graph.render.immed.EdgeAnchors;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.util.intr.IntEnumerator;
import org.cytoscape.util.intr.IntIterator;
import org.cytoscape.util.intr.IntObjHash;
import org.cytoscape.util.intr.MinIntHeap;

class DEdgeDetails extends IntermediateEdgeDetails {

	final DGraphView m_view;
	final Object m_deletedEntry = new Object();

	IntObjHash m_colorsLowDetail = new IntObjHash();
	IntObjHash m_selectedColorsLowDetail = new IntObjHash();

	Map<Integer, Float> m_segmentThicknesses = new HashMap<Integer, Float>();
	Map<Integer, Stroke> m_segmentStrokes = new HashMap<Integer, Stroke>();

	Map<Integer, Byte> m_sourceArrows = new HashMap<Integer, Byte>();
	Map<Integer, Paint> m_sourceArrowPaints = new HashMap<Integer, Paint>();
	Map<Integer, Byte> m_targetArrows = new HashMap<Integer, Byte>();
	Map<Integer, Paint> m_targetArrowPaints = new HashMap<Integer, Paint>();

	Map<Integer, Paint> m_targetArrowSelectedPaints = new HashMap<Integer, Paint>();

	Map<Integer, Integer> m_labelCounts = new HashMap<Integer, Integer>();
	Map<Long, String> m_labelTexts = new HashMap<Long, String>();
	Map<Long, Font> m_labelFonts = new HashMap<Long, Font>();
	Map<Long, Paint> m_labelPaints = new HashMap<Long, Paint>();
	Map<Integer, Double> m_labelWidths = new HashMap<Integer, Double>();

	Map<Integer, Paint> m_unselectedPaints = new HashMap<Integer, Paint>();
	Map<Integer, Paint> m_selectedPaints = new HashMap<Integer, Paint>();
	
	// Curved or not
	Map<Integer, Integer> m_lineType = new HashMap<Integer, Integer>();

	private Byte m_sourceArrowDefault;
	private Paint m_sourceArrowPaintDefault;
	private Byte m_targetArrowDefault;
	private Paint m_targetArrowPaintDefault;

	private Float m_segmentThicknessDefault;
	private Stroke m_segmentStrokeDefault;
	private Integer m_labelCountDefault;
	private String m_labelTextDefault;
	private Font m_labelFontDefault;
	private Paint m_labelPaintDefault;
	private Double m_labelWidthDefault;

	private Paint m_selectedPaintDefault;
	private Paint m_unselectedPaintDefault;

	private Paint m_colorLowDetailDefault;
	private Color m_selectedColorLowDetailDefault;
	
	private Integer m_lineTypeDefault;

	private boolean isCleared = false;

	private Set<Integer> selected = new HashSet<Integer>();

	DEdgeDetails(final DGraphView view) {
		m_view = view;
	}

	void clear() {
		if (isCleared)
			return;

		m_segmentThicknesses = new HashMap<Integer, Float>();
		m_segmentStrokes = new HashMap<Integer, Stroke>();

		m_sourceArrows = new HashMap<Integer, Byte>();
		m_sourceArrowPaints = new HashMap<Integer, Paint>();
		m_targetArrows = new HashMap<Integer, Byte>();
		m_targetArrowPaints = new HashMap<Integer, Paint>();
		m_targetArrowSelectedPaints = new HashMap<Integer, Paint>();
		m_labelCounts = new HashMap<Integer, Integer>();
		m_labelTexts = new HashMap<Long, String>();
		m_labelFonts = new HashMap<Long, Font>();
		m_labelPaints = new HashMap<Long, Paint>();
		m_labelWidths = new HashMap<Integer, Double>();

		m_unselectedPaints = new HashMap<Integer, Paint>();
		m_selectedPaints = new HashMap<Integer, Paint>();

		m_colorsLowDetail = new IntObjHash();
		m_selectedColorsLowDetail = new IntObjHash();

		selected = new HashSet<Integer>();
		
		m_lineType = new HashMap<Integer, Integer>();

		isCleared = true;
	}

	void unregisterEdge(final int edgeIdx) {
		final Object colorDetail = m_colorsLowDetail.get(edgeIdx);
		if ((colorDetail != null) && (colorDetail != m_deletedEntry))
			m_colorsLowDetail.put(edgeIdx, m_deletedEntry);

		final Object selectedColorDetail = m_selectedColorsLowDetail.get(edgeIdx);
		if ((selectedColorDetail != null) && (selectedColorDetail != m_deletedEntry))
			m_selectedColorsLowDetail.put(edgeIdx, m_deletedEntry);

		m_segmentThicknesses.remove(edgeIdx);
		m_segmentStrokes.remove(edgeIdx);
		m_sourceArrows.remove(edgeIdx);
		m_sourceArrowPaints.remove(edgeIdx);
		m_targetArrows.remove(edgeIdx);
		m_targetArrowPaints.remove(edgeIdx);
		m_targetArrowSelectedPaints.remove(edgeIdx);

		m_labelCounts.remove(edgeIdx);
		m_labelTexts.remove(edgeIdx);
		m_labelFonts.remove(edgeIdx);
		m_labelPaints.remove(edgeIdx);
		m_labelWidths.remove(edgeIdx);

		m_selectedPaints.remove(edgeIdx);
		m_unselectedPaints.remove(edgeIdx);
		
		m_lineType.remove(edgeIdx);

		selected.remove(edgeIdx);
	}

	@Override
	public Color colorLowDetail(final int edge) {
		boolean isSelected = selected.contains(edge);

		if (isSelected)
			return selectedColorLowDetail(edge);
		else
			return unselectedColorLowDetail(edge);
	}

	public Color unselectedColorLowDetail(final int edge) {
		final Object o = m_colorsLowDetail.get(edge);

		if ((o == null) || (o == m_deletedEntry))
			if (m_colorLowDetailDefault == null)
				return super.colorLowDetail(edge);
			else
				return (Color) m_colorLowDetailDefault;

		return (Color) o;
	}

	public Color selectedColorLowDetail(final int edge) {
		final Object o = m_selectedColorsLowDetail.get(edge);

		if ((o == null) || (o == m_deletedEntry))
			if (m_selectedColorLowDetailDefault == null)
				return super.colorLowDetail(edge);
			else
				return (Color) m_selectedColorLowDetailDefault;

		return (Color) o;
	}

	void setSelectedColorLowDetailDefault(Color c) {
		m_selectedColorLowDetailDefault = c;
	}

	void setColorLowDetailDefault(Color c) {
		m_colorLowDetailDefault = c;
	}

	@Override
	public byte sourceArrow(final int edge) {
		final Byte arrow = m_sourceArrows.get(edge);
		if (arrow == null)
			if (m_sourceArrowDefault == null)
				return super.sourceArrow(edge);
			else
				return m_sourceArrowDefault.byteValue();

		return arrow;
	}

	void setSourceArrowDefault(byte arrow) {
		m_sourceArrowDefault = arrow;
	}

	/*
	 * A non-negative arrowType has the special meaning to remove overridden
	 * arrow.
	 */
	void overrideSourceArrow(final int edge, final byte arrowType) {
		if ((arrowType >= 0) || (arrowType == super.sourceArrow(edge)))
			m_sourceArrows.remove(edge);
		else {
			m_sourceArrows.put(edge, arrowType);
			isCleared = false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Paint sourceArrowPaint(final int edge) {

		boolean isSelected = selected.contains(edge);

		if (isSelected)
			return selectedPaint(edge);
		else
			return sourceArrowUnselectedPaint(edge);
	}

	private Paint sourceArrowUnselectedPaint(final int edge) {
		final Paint paint = this.m_sourceArrowPaints.get(edge);

		if (paint == null) {
			if(m_sourceArrowPaintDefault == null)
				return DEdgeView.DEFAULT_ARROW_PAINT;
			else
				return m_sourceArrowPaintDefault;
		} else
			return paint;
	}

	void setSourceArrowPaintDefault(final Paint p) {
		m_sourceArrowPaintDefault = p;
	}

	/*
	 * A null paint has the special meaning to remove overridden paint.
	 */
	void overrideSourceArrowPaint(final int edge, final Paint paint) {
		if ((paint == null) || paint.equals(super.sourceArrowPaint(edge)))
			m_sourceArrowPaints.remove(edge);
		else {
			m_sourceArrowPaints.put(edge, paint);
			isCleared = false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte targetArrow(final int edge) {
		final Byte arrow = m_targetArrows.get(edge);

		if (arrow == null)
			if (m_targetArrowDefault == null)
				return super.targetArrow(edge);
			else
				return m_targetArrowDefault.byteValue();

		return arrow;
	}

	void setTargetArrowDefault(final byte arrow) {
		m_targetArrowDefault = arrow;
	}

	/*
	 * A non-negative arrowType has the special meaning to remove overridden
	 * arrow.
	 */
	void overrideTargetArrow(final int edge, final byte arrowType) {
		if ((arrowType >= 0) || (arrowType == super.targetArrow(edge)))
			m_targetArrows.remove(edge);
		else {
			m_targetArrows.put(edge, arrowType);
			isCleared = false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Paint targetArrowPaint(final int edge) {
		final boolean isSelected = selected.contains(edge);

		if (isSelected)
			return selectedPaint(edge);
		else
			return targetArrowUnselectedPaint(edge);
	}

	private Paint targetArrowUnselectedPaint(final int edge) {
		final Paint paint = this.m_targetArrowPaints.get(edge);

		if (paint == null) {
			if(m_targetArrowPaintDefault == null)
				return DEdgeView.DEFAULT_ARROW_PAINT;
			else
				return this.m_targetArrowPaintDefault;
		} else
			return paint;
	}

	void setTargetArrowPaintDefault(final Paint p) {
		m_targetArrowPaintDefault = p;
	}

	/*
	 * A null paint has the special meaning to remove overridden paint.
	 */
	void overrideTargetArrowPaint(final int edge, final Paint paint) {
		if ((paint == null) || paint.equals(super.targetArrowPaint(edge)))
			m_targetArrowPaints.remove(edge);
		else {
			m_targetArrowPaints.put(edge, paint);
			isCleared = false;
		}
	}

	/*
	 * A null paint has the special meaning to remove overridden paint.
	 */
	void overrideTargetArrowSelectedPaint(final int edge, final Paint paint) {
		if ((paint == null) || paint.equals(super.targetArrowPaint(edge)))
			this.m_targetArrowSelectedPaints.remove(edge);
		else {
			m_targetArrowSelectedPaints.put(edge, paint);
			isCleared = false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float segmentThickness(final int edge) {
		final Float thickness = m_segmentThicknesses.get(edge);
		if (thickness == null)
			if (m_segmentThicknessDefault == null)
				return super.segmentThickness(edge);
			else
				return m_segmentThicknessDefault;

		return thickness;
	}

	void setSegmentThicknessDefault(float thick) {
		m_segmentThicknessDefault = thick;
	}

	/*
	 * A negative thickness value has the special meaning to remove overridden
	 * thickness.
	 */
	void overrideSegmentThickness(final int edge, final float thickness) {
		if ((thickness < 0.0f) || (thickness == super.segmentThickness(edge)))
			m_segmentThicknesses.remove(edge);
		else {
			m_segmentThicknesses.put(edge, thickness);
			isCleared = false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Stroke segmentStroke(final int edge) {
		final Stroke stroke = m_segmentStrokes.get(edge);

		if (stroke == null)
			if (m_segmentStrokeDefault == null)
				return super.segmentStroke(edge);
			else
				return m_segmentStrokeDefault;

		return stroke;
	}

	void setSegmentStrokeDefault(Stroke s) {
		m_segmentStrokeDefault = s;
	}

	/*
	 * A null paint has the special meaning to remove overridden paint.
	 */
	void overrideSegmentStroke(int edge, Stroke stroke) {
		if ((stroke == null) || stroke.equals(super.segmentStroke(edge)))
			m_segmentStrokes.remove(edge);
		else {
			m_segmentStrokes.put(edge, stroke);
			isCleared = false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Paint segmentPaint(final int edge) {
		boolean isSelected = selected.contains(edge);

		if (isSelected)
			return selectedPaint(edge);
		else {
			return unselectedPaint(edge);
		}
	}

	void setSegmentPaintDefault(Paint p) {
		m_unselectedPaintDefault = p;
	}

	public Paint unselectedPaint(final int edge) {
		final Paint paint = m_unselectedPaints.get(edge);

		if (paint == null) {
			if (m_unselectedPaintDefault == null)
				return DEdgeView.DEFAULT_EDGE_PAINT;
			else
				return m_unselectedPaintDefault;
		}

		return paint;
	}

	public Paint selectedPaint(final int edge) {
		final Paint paint = m_selectedPaints.get(edge);
		if (paint == null)
			if (m_selectedPaintDefault == null)
				return Color.red;
			else
				return m_selectedPaintDefault;

		return paint;
	}

	void setSelectedPaintDefault(final Paint p) {
		m_selectedPaintDefault = p;
	}

	void select(final int edge) {
		selected.add(edge);
	}

	void unselect(final int edge) {
		selected.remove(edge);
	}

	void setUnselectedPaint(final int edge, final Paint paint) {
		m_unselectedPaints.put(edge, paint);
		if (paint instanceof Color)
			m_colorsLowDetail.put(edge, paint);
		isCleared = false;
	}

	void setSelectedPaint(final int edge, final Paint paint) {
		m_selectedPaints.put(edge, paint);
		if (paint instanceof Color)
			m_selectedColorsLowDetail.put(edge, paint);

		isCleared = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int labelCount(final int edge) {
		final Integer i = m_labelCounts.get(edge);
		if (i == null)
			if (m_labelCountDefault == null)
				return super.labelCount(edge);
			else
				m_labelCountDefault.intValue();

		return i;
	}

	void setLabelCountDefault(int count) {
		m_labelCountDefault = Integer.valueOf(count);
	}

	/*
	 * A negative labelCount has the special meaning to remove overridden count.
	 */
	void overrideLabelCount(final int edge, final int labelCount) {
		if ((labelCount < 0) || (labelCount == super.labelCount(edge)))
			m_labelCounts.remove(edge);
		else {
			m_labelCounts.put(edge, labelCount);
			isCleared = false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String labelText(final int edge, final int labelInx) {
		final long key = (((long) edge) << 32) | ((long) labelInx);
		final String text = m_labelTexts.get(key);
		if (text == null)
			if (m_labelTextDefault == null)
				return super.labelText(edge, labelInx);
			else
				return m_labelTextDefault;

		return text;
	}

	void setLabelTextDefault(String text) {
		m_labelTextDefault = text;
	}

	/*
	 * A null text has the special meaning to remove overridden text.
	 */
	void overrideLabelText(final int edge, final int labelInx, final String text) {
		final long key = (((long) edge) << 32) | ((long) labelInx);

		if ((text == null) || text.equals(super.labelText(edge, labelInx)))
			m_labelTexts.remove(key);
		else {
			m_labelTexts.put(key, text);
			isCleared = false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Font labelFont(final int edge, final int labelInx) {
		final long key = (((long) edge) << 32) | ((long) labelInx);
		final Font font = m_labelFonts.get(key);

		if (font == null)
			if (m_labelFontDefault == null)
				return super.labelFont(edge, labelInx);
			else
				return m_labelFontDefault;

		return font;
	}

	void setLabelFontDefault(Font f) {
		m_labelFontDefault = f;
	}

	/*
	 * A null font has the special meaning to remove overridden font.
	 */
	void overrideLabelFont(final int edge, final int labelInx, final Font font) {
		final long key = (((long) edge) << 32) | ((long) labelInx);

		if ((font == null) || font.equals(super.labelFont(edge, labelInx)))
			m_labelFonts.remove(key);
		else {
			m_labelFonts.put(key, font);
			isCleared = false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Paint labelPaint(final int edge, final int labelInx) {
		final long key = (((long) edge) << 32) | ((long) labelInx);
		final Paint paint = m_labelPaints.get(key);

		if (paint == null)
			if (m_labelPaintDefault == null)
				return super.labelPaint(edge, labelInx);
			else
				return m_labelPaintDefault;

		return paint;
	}

	void setLabelPaintDefault(Paint p) {
		m_labelPaintDefault = p;
	}

	/*
	 * A null paint has the special meaning to remove overridden paint.
	 */
	void overrideLabelPaint(final int edge, final int labelInx, final Paint paint) {
		final long key = (((long) edge) << 32) | ((long) labelInx);

		if ((paint == null) || paint.equals(super.labelPaint(edge, labelInx)))
			m_labelPaints.remove(key);
		else {
			m_labelPaints.put(key, paint);
			isCleared = false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double labelWidth(final int edge) {
		final Double width = m_labelWidths.get(edge);
		if (width == null)
			if (m_labelWidthDefault == null)
				return super.labelWidth(edge);
			else
				return m_labelWidthDefault.doubleValue();

		return width;
	}

	void setLabelWidthDefault(double width) {
		m_labelWidthDefault = width;
	}

	/*
	 * A negative width value has the special meaning to remove overridden
	 * width.
	 */
	void overrideLabelWidth(final int edge, final double width) {
		if ((width < 0.0) || (width == super.labelWidth(edge)))
			m_labelWidths.remove(edge);
		else {
			m_labelWidths.put(edge, width);
			isCleared = false;
		}
	}


	@Override
	public float sourceArrowSize(int edge) {
		// For the half arrows, we need to scale multiplicatively
		// so that the arrow matches the line.
		final int arrowType = sourceArrow(edge);
		if (arrowType == GraphGraphics.ARROW_HALF_TOP || arrowType == GraphGraphics.ARROW_HALF_BOTTOM)
			return (segmentThickness(edge) * DEdgeView.DEFAULT_ARROW_SIZE);

		// For all other arrows we can scale additively. This produces less
		// egregiously big arrows.
		else
			return (segmentThickness(edge) + DEdgeView.DEFAULT_ARROW_SIZE);
	}


	@Override
	public float targetArrowSize(int edge) {
		// For the half arrows, we need to scale multiplicatively
		// so that the arrow matches the line.
		final int arrowType = targetArrow(edge);
		if (arrowType == GraphGraphics.ARROW_HALF_TOP || arrowType == GraphGraphics.ARROW_HALF_BOTTOM)
			return (segmentThickness(edge) * DEdgeView.DEFAULT_ARROW_SIZE);
		// For all other arrows we can scale additively. This produces
		// less egregiously big arrows.
		else
			return (segmentThickness(edge) + DEdgeView.DEFAULT_ARROW_SIZE);
	}
	
	
	
	public Integer lineType(final int edge) {
		final Integer lineType = m_lineType.get(edge);
		if (lineType == null)
			if (m_lineTypeDefault == null)
				return EdgeView.STRAIGHT_LINES;
			else
				return m_lineTypeDefault;

		return lineType;
	}
	
	public void setLineTypeDefault(int lineType) {
		this.m_lineTypeDefault = lineType;
	}
	
	
	// Used by bends
	private final MinIntHeap m_heap = new MinIntHeap();
	private final float[] m_extentsBuff = new float[4];
	
	@Override
	public EdgeAnchors anchors(final int edge) {
		//TODO: Why NOT op is here?
//		final EdgeAnchors returnThis = (EdgeAnchors) (m_view.getDEdgeView(~edge));
		final DEdgeView edgeView = (DEdgeView) m_view.getDEdgeView(edge);
		final EdgeAnchors returnThis = edgeView;

		if (returnThis.numAnchors() > 0) 
			return returnThis;

		final CyNetwork graph = m_view.m_drawPersp;
		
		final int srcNodeIndex = edgeView.getModel().getSource().getIndex();
		final int trgNodeIndex = edgeView.getModel().getTarget().getIndex();

		// Calculate anchors necessary for self edges.
		if (srcNodeIndex == trgNodeIndex) { 
			m_view.m_spacial.exists(srcNodeIndex, m_extentsBuff, 0);

			final double w = ((double) m_extentsBuff[2]) - m_extentsBuff[0];
			final double h = ((double) m_extentsBuff[3]) - m_extentsBuff[1];
			final double x = (((double) m_extentsBuff[0]) + m_extentsBuff[2]) / 2.0d;
			final double y = (((double) m_extentsBuff[1]) + m_extentsBuff[3]) / 2.0d;
			final double nodeSize = Math.max(w, h);
			int i = 0;
			
			final List<CyEdge> selfEdgeList = graph.getConnectingEdgeList(edgeView.getModel().getSource(), edgeView.getModel().getSource(), CyEdge.Type.ANY);
			//final IntIterator selfEdges = graph.edgesConnecting(srcNodeIndex, srcNodeIndex, true, true, true);

			for(final CyEdge selfEdge: selfEdgeList) {
//			while (selfEdges.hasNext()) {
//				final int e2 = selfEdges.nextInt();
				final int e2 = selfEdge.getIndex();

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
			final int tmpSrcIndex = Math.min( srcNodeIndex, trgNodeIndex ); 
			final int tmpTrgIndex = Math.max( srcNodeIndex, trgNodeIndex ); 

			// Sort the connecting edges.
			final CyNode tmpSrc = graph.getNode(tmpSrcIndex);
			final CyNode tmpTrg = graph.getNode(tmpTrgIndex);
			final List<CyEdge> conEdgeList = graph.getConnectingEdgeList(tmpSrc, tmpTrg, CyEdge.Type.ANY);
//			final IntIterator conEdges = graph.edgesConnecting(tmpSrc, tmpTrg,
//			                                                   true, true, true);
			m_heap.empty();

			for(final CyEdge conEdge: conEdgeList) {
//			while (conEdges.hasNext()) {
//				m_heap.toss(conEdges.nextInt());
				m_heap.toss(conEdge.getIndex());
			}

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
			m_view.m_spacial.exists(tmpSrcIndex, m_extentsBuff, 0);
			final double srcW = ((double) m_extentsBuff[2]) - m_extentsBuff[0];
			final double srcH = ((double) m_extentsBuff[3]) - m_extentsBuff[1];
			final double srcX = (((double) m_extentsBuff[0]) + m_extentsBuff[2]) / 2.0d;
			final double srcY = (((double) m_extentsBuff[1]) + m_extentsBuff[3]) / 2.0d;

			// Get target node size and position.
			m_view.m_spacial.exists(tmpTrgIndex, m_extentsBuff, 0);
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

	@Override
	public float anchorSize(final int edge, final int anchorInx) {
		if (m_view.getDEdgeView(edge).isSelected() && (((DEdgeView) m_view.getDEdgeView(edge)).numAnchors() > 0))
			return m_view.getAnchorSize();
		else
			return 0.0f;
	}

	@Override
	public Paint anchorPaint(final int edge, int anchorInx) {
//		final DEdgeView edgeView = (DEdgeView) m_view.getDEdgeView(edge);

		if (lineType(edge) == DEdgeView.STRAIGHT_LINES)
			anchorInx = anchorInx / 2;

		if (m_view.m_selectedAnchors.count((edge << 6) | anchorInx) > 0)
			return m_view.getAnchorSelectedPaint();
		else
			return m_view.getAnchorUnselectedPaint();
	}
}
