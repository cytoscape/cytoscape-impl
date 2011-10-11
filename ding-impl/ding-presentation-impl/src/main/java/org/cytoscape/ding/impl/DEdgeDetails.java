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
import java.util.Map;

import org.cytoscape.util.intr.IntObjHash;


class DEdgeDetails extends IntermediateEdgeDetails {
	
	final DGraphView m_view;
	final Object m_deletedEntry = new Object();
	
	IntObjHash m_colorsLowDetail = new IntObjHash();
	
	Map<Integer, Float> m_segmentThicknesses = new HashMap<Integer, Float>();
	Map<Integer, Stroke> m_segmentStrokes = new HashMap<Integer, Stroke>();
	
	Map<Integer, Byte> m_sourceArrows = new HashMap<Integer, Byte>();
	Map<Integer, Paint> m_sourceArrowPaints = new HashMap<Integer, Paint>();
	Map<Integer, Byte> m_targetArrows = new HashMap<Integer, Byte>();
	Map<Integer, Paint> m_targetArrowPaints = new HashMap<Integer, Paint>();
	
	Map<Integer, Integer> m_labelCounts = new HashMap<Integer, Integer>();
	Map<Long, String> m_labelTexts = new HashMap<Long, String>();
	Map<Long, Font> m_labelFonts = new HashMap<Long, Font>();
	Map<Long, Paint> m_labelPaints = new HashMap<Long, Paint>();
	Map<Integer, Double> m_labelWidths = new HashMap<Integer, Double>();
	
	Map<Integer, Paint> m_segmentPaints = new HashMap<Integer, Paint>();
	Map<Integer, Paint> m_selectedPaints = new HashMap<Integer, Paint>();
	
	private Paint m_colorLowDetailDefault ;
	private Byte m_sourceArrowDefault ;
	private Paint m_sourceArrowPaintDefault ;
	private Byte m_targetArrowDefault ;
	private Paint m_targetArrowPaintDefault ;
	private Float m_segmentThicknessDefault ;
	private Stroke m_segmentStrokeDefault ;
	private Paint m_segmentPaintDefault ;
	private Integer m_labelCountDefault ;
	private String m_labelTextDefault ;
	private Font m_labelFontDefault ;
	private Paint m_labelPaintDefault ;
	private Double m_labelWidthDefault ;
	
	private Paint m_selectedPaintDefault;
	
	private boolean isCleared = false; 


	DEdgeDetails(final DGraphView view) {
		m_view = view;
	}
	
	void clear() {
		if(isCleared)
			return;
		
		m_segmentThicknesses = new HashMap<Integer, Float>();
		m_segmentStrokes = new HashMap<Integer, Stroke>();
		
		m_sourceArrows = new HashMap<Integer, Byte>();
		m_sourceArrowPaints = new HashMap<Integer, Paint>();
		m_targetArrows = new HashMap<Integer, Byte>();
		m_targetArrowPaints = new HashMap<Integer, Paint>();
		
		m_labelCounts = new HashMap<Integer, Integer>();
		m_labelTexts = new HashMap<Long, String>();
		m_labelFonts = new HashMap<Long, Font>();
		m_labelPaints = new HashMap<Long, Paint>();
		m_labelWidths = new HashMap<Integer, Double>();
		
		m_segmentPaints = new HashMap<Integer, Paint>();
		m_selectedPaints = new HashMap<Integer, Paint>();
		
		m_colorsLowDetail = new IntObjHash();
		
		isCleared = true;
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
		
		m_selectedPaints.remove(edgeIdx);
	}


	public Color colorLowDetail(final int edge) {
		final Object o = m_colorsLowDetail.get(edge);

		if ((o == null) || (o == m_deletedEntry))
			if ( m_colorLowDetailDefault == null )
				return super.colorLowDetail(edge);
			else
				return (Color)m_colorLowDetailDefault;

		return (Color) o;
	}

	void setColorLowDetailDefault(Paint c) {
		m_colorLowDetailDefault = c;
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
			if ( m_sourceArrowDefault == null )
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


	public Paint sourceArrowPaint(final int edge) {
		final Paint arrowPaint = m_sourceArrowPaints.get(edge);
		if (arrowPaint == null)
			if ( m_sourceArrowPaintDefault == null )
				return super.sourceArrowPaint(edge);
			else
				return m_sourceArrowPaintDefault;

		return arrowPaint;
	}

	void setSourceArrowPaintDefault(Paint p) {
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
			if ( m_targetArrowDefault == null )
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
		final Paint arrowPaint = m_targetArrowPaints.get(edge);
		if (arrowPaint == null)
			if ( m_targetArrowPaintDefault == null )
				return super.targetArrowPaint(edge);
			else
				return m_targetArrowPaintDefault;

		return arrowPaint;
	}

	void setTargetArrowPaintDefault(Paint p) {
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


	/**
	 * {@inheritDoc}
	 */
	@Override
	public float segmentThickness(final int edge) {
		final Float thickness = m_segmentThicknesses.get(edge);
		if (thickness == null)
			if ( m_segmentThicknessDefault == null )
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
			if ( m_segmentStrokeDefault == null )
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
		final Paint paint = m_segmentPaints.get(edge);
		if (paint == null)
			if ( m_segmentPaintDefault == null )
				return super.segmentPaint(edge);
			else
				return m_segmentPaintDefault;

		return paint;
	}

	void setSegmentPaintDefault(Paint p) {
		m_segmentPaintDefault = p;
	}
	
	public Paint selectedPaint(final int edge) {
		final Paint paint = m_selectedPaints.get(edge);
		if (paint == null)
			if ( m_selectedPaintDefault == null )
				return super.segmentPaint(edge);
			else
				return m_selectedPaintDefault;

		return paint;
	}

	void setSelectedPaintDefault(final Paint p) {
		m_selectedPaintDefault = p;
	}


	/*
	 * A negative length value has the special meaning to remove overridden
	 * length.
	 */
	void overrideSegmentPaint(final int edge, final Paint paint) {
		if ((paint == null) || (paint == super.segmentPaint(edge)))
			m_segmentPaints.remove(edge);
		else {
			m_segmentPaints.put(edge, paint);
			isCleared = false;
		}
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int labelCount(final int edge) {
		final Integer i = m_labelCounts.get(edge);
		if (i == null)
			if ( m_labelCountDefault == null )
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
			if ( m_labelTextDefault == null )
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
			if ( m_labelFontDefault == null )
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
			if ( m_labelPaintDefault == null )
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
			if ( m_labelWidthDefault == null )
				return super.labelWidth(edge);
			else
				return m_labelWidthDefault.doubleValue();

		return width;
	}

	void setLabelWidthDefault(double width) {
		m_labelWidthDefault = width;
	}

	/*
	 * A negative width value has the special meaning to remove overridden width.
	 */
	void overrideLabelWidth(final int edge, final double width) {
		if ((width < 0.0) || (width == super.labelWidth(edge)))
			m_labelWidths.remove(edge);
		else {
			m_labelWidths.put(edge, width);
			isCleared = false;
		}
	}

}
