package org.cytoscape.ding.impl;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

import static org.cytoscape.ding.DVisualLexicon.EDGE_SOURCE_ARROW_UNSELECTED_PAINT;
import static org.cytoscape.ding.DVisualLexicon.EDGE_TARGET_ARROW_UNSELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_FONT_FACE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_SELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_UNSELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_WIDTH;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.cytoscape.ding.DArrowShape;
import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.EdgeView;
import org.cytoscape.graph.render.immed.EdgeAnchors;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.graph.render.stateful.EdgeDetails;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.util.intr.LongEnumerator;
import org.cytoscape.util.intr.MinLongHeap;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.EdgeBendVisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.LineType;

/**
 * Values stored in this object will be used renderer. Be careful to keep these
 * values consistent!
 */
final class DEdgeDetails extends EdgeDetails {

	private static final float DEFAULT_ARROW_SIZE = 6.0f;

	private final DGraphView dGraphView;
	private final Object m_deletedEntry = new Object();
	
	private final Map<VisualProperty<?>, Object> defaultValues; 

	// Mapped Values
	// If value found in these map objects, the value will be used by the renderer.
	// Otherwise, default value will be used.
	Map<CyEdge, Object> m_colorsLowDetail = new WeakHashMap<CyEdge, Object>();
	Map<CyEdge, Object> m_selectedColorsLowDetail = new WeakHashMap<CyEdge, Object>();
	Map<CyEdge, Float> m_segmentThicknesses = new WeakHashMap<CyEdge, Float>();
	Map<CyEdge, Stroke> m_segmentStrokes = new WeakHashMap<CyEdge, Stroke>();
	Map<CyEdge, Byte> m_sourceArrows = new WeakHashMap<CyEdge, Byte>();
	Map<CyEdge, Paint> m_sourceArrowPaints = new WeakHashMap<CyEdge, Paint>();
	Map<CyEdge, Paint> m_sourceArrowSelectedPaints = new WeakHashMap<CyEdge, Paint>();
	Map<CyEdge, Byte> m_targetArrows = new WeakHashMap<CyEdge, Byte>();
	Map<CyEdge, Paint> m_targetArrowPaints = new WeakHashMap<CyEdge, Paint>();
	Map<CyEdge, Paint> m_targetArrowSelectedPaints = new WeakHashMap<CyEdge, Paint>();
	Map<CyEdge, Integer> m_labelCounts = new WeakHashMap<CyEdge, Integer>();
	Map<CyEdge, String> m_labelTexts = new WeakHashMap<CyEdge, String>();
	Map<CyEdge, Font> m_labelFonts = new WeakHashMap<CyEdge, Font>();
	Map<CyEdge, Paint> m_labelPaints = new WeakHashMap<CyEdge, Paint>();
	Map<CyEdge, Double> m_labelWidths = new WeakHashMap<CyEdge, Double>();
	Map<CyEdge, Paint> m_unselectedPaints = new WeakHashMap<CyEdge, Paint>();
	Map<CyEdge, Paint> m_selectedPaints = new WeakHashMap<CyEdge, Paint>();
	Map<CyEdge, Integer> m_lineCurved = new WeakHashMap<CyEdge, Integer>();
	Map<CyEdge, Bend> m_edgeBends = new WeakHashMap<CyEdge, Bend>();
	Map<CyEdge, String> m_edgeTooltips = new WeakHashMap<CyEdge, String>();
	Map<CyEdge, Integer> m_edgeTansparencies = new WeakHashMap<CyEdge, Integer>();
	Map<CyEdge, Integer> m_edgeLabelTansparencies = new WeakHashMap<CyEdge, Integer>();

	// Default Values
	Byte m_sourceArrowDefault;
	Paint m_sourceArrowPaintDefault = EDGE_SOURCE_ARROW_UNSELECTED_PAINT.getDefault();
	Byte m_targetArrowDefault;
	Paint m_targetArrowPaintDefault = EDGE_TARGET_ARROW_UNSELECTED_PAINT.getDefault();
	Double m_segmentThicknessDefault = EDGE_WIDTH.getDefault();
	Stroke m_segmentStrokeDefault = new BasicStroke(m_segmentThicknessDefault.floatValue());
	String m_labelTextDefault;
	Font m_labelFontDefault = EDGE_LABEL_FONT_FACE.getDefault();
	Paint m_labelPaintDefault = EDGE_LABEL_COLOR.getDefault();
	Double m_labelWidthDefault;
	Paint m_selectedPaintDefault = EDGE_SELECTED_PAINT.getDefault();
	Paint m_unselectedPaintDefault = EDGE_UNSELECTED_PAINT.getDefault();
	Paint m_colorLowDetailDefault = EDGE_UNSELECTED_PAINT.getDefault();
	Paint m_selectedColorLowDetailDefault = EDGE_SELECTED_PAINT.getDefault();
	Integer m_lineCurvedDefault = EdgeView.STRAIGHT_LINES;
	Bend m_edgeBendDefault;
	String m_edgeTooltipDefault;
	Integer transparencyDefault = EDGE_TRANSPARENCY.getDefault();
	Integer labelTransparencyDefault = EDGE_LABEL_TRANSPARENCY.getDefault();

	private boolean isCleared = false;

	private final Set<CyEdge> selected = new HashSet<CyEdge>();

	DEdgeDetails(final DGraphView view) {
		dGraphView = view;
		defaultValues = new HashMap<VisualProperty<?>, Object>();
	}

	void clear() {
		if (isCleared)
			return;

		m_segmentThicknesses = new WeakHashMap<CyEdge, Float>();
		m_segmentStrokes = new WeakHashMap<CyEdge, Stroke>();
		m_sourceArrows = new WeakHashMap<CyEdge, Byte>();
		m_sourceArrowPaints = new WeakHashMap<CyEdge, Paint>();
		m_targetArrows = new WeakHashMap<CyEdge, Byte>();
		m_targetArrowPaints = new WeakHashMap<CyEdge, Paint>();
		m_targetArrowSelectedPaints = new WeakHashMap<CyEdge, Paint>();
		m_labelCounts = new WeakHashMap<CyEdge, Integer>();
		m_labelTexts = new WeakHashMap<CyEdge, String>();
		m_labelFonts = new WeakHashMap<CyEdge, Font>();
		m_labelPaints = new WeakHashMap<CyEdge, Paint>();
		m_labelWidths = new WeakHashMap<CyEdge, Double>();
		m_unselectedPaints = new WeakHashMap<CyEdge, Paint>();
		m_selectedPaints = new WeakHashMap<CyEdge, Paint>();
		m_colorsLowDetail = new WeakHashMap<CyEdge, Object>();
		m_selectedColorsLowDetail = new WeakHashMap<CyEdge, Object>();
		m_lineCurved = new WeakHashMap<CyEdge, Integer>();
		m_edgeBends = new WeakHashMap<CyEdge, Bend>();
		m_edgeTooltips = new WeakHashMap<CyEdge, String>();
		m_edgeTansparencies = new WeakHashMap<CyEdge, Integer>();
		m_edgeLabelTansparencies = new WeakHashMap<CyEdge, Integer>();

		isCleared = true;
	}

	void unregisterEdge(final CyEdge edgeIdx) {
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
		m_lineCurved.remove(edgeIdx);
		m_edgeBends.remove(edgeIdx);
		selected.remove(edgeIdx);
		m_edgeTooltips.remove(edgeIdx);
		m_edgeTansparencies.remove(edgeIdx);
		m_edgeLabelTansparencies.remove(edgeIdx);
	}

	@Override
	public Color getColorLowDetail(final CyEdge edge) {
		boolean isSelected = selected.contains(edge);

		if (isSelected)
			return getSelectedColorLowDetail(edge);
		else
			return getUnselectedColorLowDetail(edge);
	}

	private final Color getUnselectedColorLowDetail(final CyEdge edge) {
		// Check bypass
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		if (dev.isValueLocked(DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT))
			return (Color) dev.getVisualProperty(DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);

		final Object o = m_colorsLowDetail.get(edge);

		if ((o == null) || (o == m_deletedEntry))
			if (m_colorLowDetailDefault == null)
				return super.getColorLowDetail(edge);
			else
				return (Color) m_colorLowDetailDefault;

		return (Color) o;
	}

	private Color getSelectedColorLowDetail(final CyEdge edge) {
		// Check bypass
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		if (dev.isValueLocked(DVisualLexicon.EDGE_STROKE_SELECTED_PAINT))
			return (Color) dev.getVisualProperty(DVisualLexicon.EDGE_STROKE_SELECTED_PAINT);

		final Object o = m_selectedColorsLowDetail.get(edge);

		if ((o == null) || (o == m_deletedEntry))
			if (m_selectedColorLowDetailDefault == null)
				return super.getColorLowDetail(edge);
			else
				return (Color) m_selectedColorLowDetailDefault;

		return (Color) o;
	}

	void setSelectedColorLowDetailDefault(Color c) {
		m_selectedColorLowDetailDefault = c;
		defaultValues.put(DVisualLexicon.EDGE_SELECTED_PAINT, m_selectedColorLowDetailDefault);
		defaultValues.put(DVisualLexicon.EDGE_STROKE_SELECTED_PAINT, m_selectedColorLowDetailDefault);
	}

	void setColorLowDetailDefault(Color c) {
		m_colorLowDetailDefault = c;
		defaultValues.put(DVisualLexicon.EDGE_UNSELECTED_PAINT, m_colorLowDetailDefault);
		defaultValues.put(DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, m_colorLowDetailDefault);
	}

	@Override
	public byte getSourceArrowShape(final CyEdge edge) {
		// Check bypass
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		if (dev.isValueLocked(DVisualLexicon.EDGE_SOURCE_ARROW_SHAPE)) {
			final ArrowShape tgtArrow = dev.getVisualProperty(DVisualLexicon.EDGE_SOURCE_ARROW_SHAPE);
			final String shapeID = tgtArrow.getSerializableString();
			return DArrowShape.parseArrowText(shapeID).getRendererTypeID();
		}

		final Byte arrow = m_sourceArrows.get(edge);
		if (arrow == null)
			if (m_sourceArrowDefault == null)
				return super.getSourceArrowShape(edge);
			else
				return m_sourceArrowDefault.byteValue();

		return arrow;
	}

	void setSourceArrowDefault(byte arrow) {
		m_sourceArrowDefault = arrow;
		// FIXME
//		defaultValues.put(DVisualLexicon.EDGE_SOURCE_ARROW_SHAPE, m_selectedPaintDefault);
	}

	/*
	 * A non-negative arrowType has the special meaning to remove overridden
	 * arrow.
	 */
	void overrideSourceArrow(final CyEdge edge, final byte arrowType) {
		if ((arrowType >= 0) || (arrowType == super.getSourceArrowShape(edge)))
			m_sourceArrows.remove(edge);
		else {
			m_sourceArrows.put(edge, arrowType);
			isCleared = false;
		}
	}

	@Override
	public Paint getSourceArrowPaint(final CyEdge edge) {
		boolean isSelected = selected.contains(edge);

		if (isSelected)
			return getSelectedPaint(edge);
		else
			return getSourceArrowUnselectedPaint(edge);
	}

	private final Paint getSourceArrowUnselectedPaint(final CyEdge edge) {
		// Check bypass
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		if (dev.isValueLocked(DVisualLexicon.EDGE_SOURCE_ARROW_UNSELECTED_PAINT))
			return dev.getVisualProperty(DVisualLexicon.EDGE_SOURCE_ARROW_UNSELECTED_PAINT);

		final Paint paint = m_sourceArrowPaints.get(edge);
		if (paint == null) {
			if (m_sourceArrowPaintDefault == null)
				return DVisualLexicon.EDGE_SOURCE_ARROW_UNSELECTED_PAINT.getDefault();
			else
				return m_sourceArrowPaintDefault;
		} else
			return paint;
	}

	void setSourceArrowPaintDefault(final Paint p) {
		m_sourceArrowPaintDefault = p;
		defaultValues.put(DVisualLexicon.EDGE_SOURCE_ARROW_UNSELECTED_PAINT, m_sourceArrowPaintDefault);
	}

	/*
	 * A null paint has the special meaning to remove overridden paint.
	 */
	void overrideSourceArrowPaint(final CyEdge edge, final Paint paint) {
		if ((paint == null) || paint.equals(super.getSourceArrowPaint(edge)))
			m_sourceArrowPaints.remove(edge);
		else {
			m_sourceArrowPaints.put(edge, paint);
			isCleared = false;
		}
	}

	void overrideSourceArrowSelectedPaint(final CyEdge edge, final Paint paint) {
		if ((paint == null) || paint.equals(super.getSourceArrowPaint(edge)))
			this.m_sourceArrowSelectedPaints.remove(edge);
		else {
			m_sourceArrowSelectedPaints.put(edge, paint);
			isCleared = false;
		}
	}

	@Override
	public byte getTargetArrowShape(final CyEdge edge) {
		// Check bypass
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		if (dev.isValueLocked(DVisualLexicon.EDGE_TARGET_ARROW_SHAPE)) {
			final ArrowShape tgtArrow = dev.getVisualProperty(DVisualLexicon.EDGE_TARGET_ARROW_SHAPE);
			final String shapeID = tgtArrow.getSerializableString();
			return DArrowShape.parseArrowText(shapeID).getRendererTypeID();
		}

		final Byte arrow = m_targetArrows.get(edge);
		if (arrow == null)
			if (m_targetArrowDefault == null)
				return super.getTargetArrowShape(edge);
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
	void overrideTargetArrow(final CyEdge edge, final byte arrowType) {
		if ((arrowType >= 0) || (arrowType == super.getTargetArrowShape(edge)))
			m_targetArrows.remove(edge);
		else {
			m_targetArrows.put(edge, arrowType);
			isCleared = false;
		}
	}

	/**
	 * Renderer uses this value for arrow paint;
	 */
	@Override
	public Paint getTargetArrowPaint(final CyEdge edge) {
		final boolean isSelected = selected.contains(edge);

		if (isSelected)
			return getSelectedPaint(edge);
		else
			return getTargetArrowUnselectedPaint(edge);
	}

	private final Paint getTargetArrowUnselectedPaint(final CyEdge edge) {
		// Check bypass
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		if (dev.isValueLocked(DVisualLexicon.EDGE_TARGET_ARROW_UNSELECTED_PAINT))
			return dev.getVisualProperty(DVisualLexicon.EDGE_TARGET_ARROW_UNSELECTED_PAINT);

		final Paint paint = m_targetArrowPaints.get(edge);

		if (paint == null) {
			if (m_targetArrowPaintDefault == null)
				return DVisualLexicon.EDGE_TARGET_ARROW_UNSELECTED_PAINT.getDefault();
			else
				return this.m_targetArrowPaintDefault;
		} else
			return paint;
	}
	
	void setTargetArrowPaintDefault(final Paint p) {
		m_targetArrowPaintDefault = p;
		defaultValues.put(DVisualLexicon.EDGE_TARGET_ARROW_UNSELECTED_PAINT, m_targetArrowPaintDefault);
	}

	/*
	 * A null paint has the special meaning to remove overridden paint.
	 */
	void overrideTargetArrowPaint(final CyEdge edge, final Paint paint) {
		if (paint == null)
			m_targetArrowPaints.remove(edge);
		else {
			m_targetArrowPaints.put(edge, paint);
			isCleared = false;
		}
	}

	/*
	 * A null paint has the special meaning to remove overridden paint.
	 */
	void overrideTargetArrowSelectedPaint(final CyEdge edge, final Paint paint) {
		if ((paint == null) || paint.equals(super.getTargetArrowPaint(edge)))
			this.m_targetArrowSelectedPaints.remove(edge);
		else {
			m_targetArrowSelectedPaints.put(edge, paint);
			isCleared = false;
		}
	}

	@Override
	public float getWidth(final CyEdge edge) {
		Float w = null;
		// Bypass check
		final DEdgeView edv = dGraphView.getDEdgeView(edge);
		
		if (edv.isValueLocked(DVisualLexicon.EDGE_WIDTH)) {
			w = edv.getVisualProperty(DVisualLexicon.EDGE_WIDTH).floatValue();
		} else {
			w = m_segmentThicknesses.get(edge);
			if (w == null) {
				if (m_segmentThicknessDefault == null)
					w = super.getWidth(edge);
				else
					w = m_segmentThicknessDefault.floatValue();
			}
		}

		return w;
	}

	void setSegmentThicknessDefault(float thick) {
		m_segmentThicknessDefault = (double) thick;
		defaultValues.put(DVisualLexicon.EDGE_WIDTH, m_segmentThicknessDefault);
	}

	/*
	 * A negative thickness value has the special meaning to remove overridden
	 * thickness.
	 */
	void overrideSegmentThickness(final CyEdge edge, final float thickness) {
		if ((thickness < 0.0f) || (thickness == super.getWidth(edge)))
			m_segmentThicknesses.remove(edge);
		else {
			m_segmentThicknesses.put(edge, thickness);
			isCleared = false;
		}
	}

	@Override
	public Stroke getStroke(final CyEdge edge) {
		Stroke stroke = null;
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		
		if (dev.isValueLocked(DVisualLexicon.EDGE_LINE_TYPE) || dev.isValueLocked(DVisualLexicon.EDGE_WIDTH)) {
			// If one of these properties are locked, the stroke has to be recreated
			final LineType lineType = dev.getVisualProperty(DVisualLexicon.EDGE_LINE_TYPE);
			stroke = DLineType.getDLineType(lineType).getStroke(getWidth(edge));
		} else {
			stroke = m_segmentStrokes.get(edge);
			
			if (stroke == null) {
				if (m_segmentStrokeDefault == null)
					stroke = super.getStroke(edge);
				else
					stroke = m_segmentStrokeDefault;
			}
		}

		return stroke;
	}

	void setSegmentStrokeDefault(final Stroke s, final LineType t) {
		m_segmentStrokeDefault = s;
		defaultValues.put(DVisualLexicon.EDGE_LINE_TYPE, t);
	}

	/*
	 * A null paint has the special meaning to remove overridden paint.
	 */
	void overrideSegmentStroke(final CyEdge edge, final Stroke stroke) {
		if ((stroke == null) || stroke.equals(super.getStroke(edge)))
			m_segmentStrokes.remove(edge);
		else {
			m_segmentStrokes.put(edge, stroke);
			isCleared = false;
		}
	}

	/**
	 * High detail paint.
	 */
	@Override
	public Paint getPaint(final CyEdge edge) {
		final boolean isSelected = selected.contains(edge);

		if (isSelected)
			return getSelectedPaint(edge);
		else
			return getUnselectedPaint(edge);
	}

	void setSegmentPaintDefault(final Paint p) {
		m_unselectedPaintDefault = p;
		defaultValues.put(DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, p);
		defaultValues.put(DVisualLexicon.EDGE_UNSELECTED_PAINT, p);
	}

	Paint getUnselectedPaint(final CyEdge edge) {
		Paint paint = null;
		Integer trans = null;
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		
		// First check if transparency is locked, because the stored colors may not contain the correct alpha value
		if (dev.isValueLocked(DVisualLexicon.EDGE_TRANSPARENCY))
			trans = getTransparency(edge);
		
		if (dev.isValueLocked(DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT)) {
			paint = dev.getVisualProperty(DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
		} else if (dev.isValueLocked(DVisualLexicon.EDGE_UNSELECTED_PAINT)) {
			paint = dev.getVisualProperty(DVisualLexicon.EDGE_UNSELECTED_PAINT);
		} else {
			paint = m_unselectedPaints.get(edge);

			if (paint == null) {
				if (m_unselectedPaintDefault == null)
					paint = DVisualLexicon.EDGE_UNSELECTED_PAINT.getDefault();
				else
					paint = m_unselectedPaintDefault;
			}
		}
		
		if (trans != null)
			paint = dGraphView.getTransparentColor(paint, trans);

		return paint;
	}

	Paint getSelectedPaint(final CyEdge edge) {
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		if (dev.isValueLocked(DVisualLexicon.EDGE_STROKE_SELECTED_PAINT))
			return dev.getVisualProperty(DVisualLexicon.EDGE_STROKE_SELECTED_PAINT);
		if (dev.isValueLocked(DVisualLexicon.EDGE_SELECTED_PAINT))
			return dev.getVisualProperty(DVisualLexicon.EDGE_SELECTED_PAINT);

		final Paint paint = m_selectedPaints.get(edge);

		if (paint == null) {
			if (m_selectedPaintDefault == null)
				return Color.red;
			else
				return m_selectedPaintDefault;
		}

		return paint;
	}

	void setSelectedPaintDefault(final Paint p) {
		m_selectedPaintDefault = p;
		defaultValues.put(DVisualLexicon.EDGE_STROKE_SELECTED_PAINT, m_selectedPaintDefault);
		defaultValues.put(DVisualLexicon.EDGE_SELECTED_PAINT, m_selectedPaintDefault);
	}

	void select(final CyEdge edge) {
		selected.add(edge);
	}

	void unselect(final CyEdge edge) {
		selected.remove(edge);
	}

	void setUnselectedPaint(final CyEdge edge, final Paint paint) {
		m_unselectedPaints.put(edge, paint);
		if (paint instanceof Color)
			m_colorsLowDetail.put(edge, paint);
		isCleared = false;
	}

	void setSelectedPaint(final CyEdge edge, final Paint paint) {
		m_selectedPaints.put(edge, paint);
		if (paint instanceof Color)
			m_selectedColorsLowDetail.put(edge, paint);

		isCleared = false;
	}

	@Override
	public int getLabelCount(final CyEdge edge) {
		// Check related bypass
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		
		if (dev.isValueLocked(DVisualLexicon.EDGE_LABEL) && !dev.getVisualProperty(DVisualLexicon.EDGE_LABEL).isEmpty())
			return 1;
		
		Integer count = m_labelCounts.get(edge);
		
		if (count == null) {
			try {
				String defLabel = (String) defaultValues.get(DVisualLexicon.EDGE_LABEL);
				count = (defLabel == null || defLabel.isEmpty()) ? super.getLabelCount(edge) : 1;
			} catch (ClassCastException e) {
				count = 0;
			}
		}
		
		return count;
	}

	/*
	 * A negative labelCount has the special meaning to remove overridden count.
	 */
	void overrideLabelCount(final CyEdge edge, final int labelCount) {
		if ((labelCount < 0) || (labelCount == super.getLabelCount(edge))) {
			m_labelCounts.remove(edge);
		} else {
			m_labelCounts.put(edge, labelCount);
			isCleared = false;
		}
	}

	@Override
	public String getLabelText(final CyEdge edge, final int labelInx) {
		// Check bypass
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		if (dev.isValueLocked(DVisualLexicon.EDGE_LABEL))
			return dev.getVisualProperty(DVisualLexicon.EDGE_LABEL);

		final String text = m_labelTexts.get(edge);
		if (text == null)
			if (m_labelTextDefault == null)
				return super.getLabelText(edge, labelInx);
			else
				return m_labelTextDefault;

		return text;
	}

	void setLabelTextDefault(String text) {
		m_labelTextDefault = text;
		defaultValues.put(DVisualLexicon.EDGE_LABEL, m_labelTextDefault);
	}

	/*
	 * A null text has the special meaning to remove overridden text.
	 */
	void overrideLabelText(final CyEdge edge, final int labelInx, final String text) {
		// final long key = (((long) edge) << 32) | ((long) labelInx);

		if ((text == null) || text.equals(super.getLabelText(edge, labelInx)))
			m_labelTexts.remove(edge);
		else {
			m_labelTexts.put(edge, text);
			isCleared = false;
		}
	}

	public String getTooltipText(final CyEdge edge, final int labelInx) {
		// Check bypass
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		if (dev.isValueLocked(DVisualLexicon.EDGE_TOOLTIP))
			return dev.getVisualProperty(DVisualLexicon.EDGE_TOOLTIP);

		final String text = m_edgeTooltips.get(edge);
		if (text == null)
			if (m_edgeTooltipDefault == null)
				return DVisualLexicon.EDGE_TOOLTIP.getDefault();
			else
				return m_edgeTooltipDefault;

		return text;
	}

	void setTooltipTextDefault(String text) {
		m_edgeTooltipDefault = text;
		defaultValues.put(DVisualLexicon.EDGE_TOOLTIP, m_edgeTooltipDefault);
	}

	void overrideTooltipText(final CyEdge edge, final String text) {
		if ((text == null) || text.equals(""))
			m_edgeTooltips.remove(edge);
		else {
			m_edgeTooltips.put(edge, text);
			isCleared = false;
		}
	}

	public Integer getTransparency(final CyEdge edge) {
		// Check bypass
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		if (dev.isValueLocked(DVisualLexicon.EDGE_TRANSPARENCY))
			return dev.getVisualProperty(DVisualLexicon.EDGE_TRANSPARENCY);

		Integer trans = m_edgeTansparencies.get(edge);
		if (trans == null) {
			if (transparencyDefault == null)
				trans = DVisualLexicon.EDGE_TRANSPARENCY.getDefault();
			else
				trans = transparencyDefault;
		}
		
		return trans;
	}

	void setTransparencyDefault(Integer transparency) {
		transparencyDefault = transparency;
		defaultValues.put(DVisualLexicon.EDGE_TRANSPARENCY, transparencyDefault);
	}

	void overrideTransparency(final CyEdge edge, final Integer transparency) {
		if (transparency == null)
			m_edgeTansparencies.remove(edge);
		else {
			m_edgeTansparencies.put(edge, transparency);
			isCleared = false;
		}
	}

	public Integer getLabelTransparency(final CyEdge edge) {
		// Check bypass
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		if (dev.isValueLocked(DVisualLexicon.EDGE_LABEL_TRANSPARENCY))
			return dev.getVisualProperty(DVisualLexicon.EDGE_LABEL_TRANSPARENCY);

		Integer trans = m_edgeLabelTansparencies.get(edge);
		if (trans == null) {
			if (labelTransparencyDefault == null)
				trans = DVisualLexicon.EDGE_LABEL_TRANSPARENCY.getDefault();
			else
				trans = labelTransparencyDefault;
		}
		
		return trans;
	}

	void setLabelTransparencyDefault(Integer transparency) {
		labelTransparencyDefault = transparency;
		defaultValues.put(DVisualLexicon.EDGE_LABEL_TRANSPARENCY, labelTransparencyDefault);
	}

	void overrideLabelTransparency(final CyEdge edge, final Integer transparency) {
		if (transparency == null)
			m_edgeLabelTansparencies.remove(edge);
		else {
			m_edgeLabelTansparencies.put(edge, transparency);
			isCleared = false;
		}
	}
	
	@Override
	public Font getLabelFont(final CyEdge edge, final int labelInx) {
		Number size = null;
		Font font = null;
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		
		// Check bypass
		if (dev.isValueLocked(DVisualLexicon.EDGE_LABEL_FONT_SIZE))
			size = dev.getVisualProperty(DVisualLexicon.EDGE_LABEL_FONT_SIZE);
		
		if (dev.isValueLocked(DVisualLexicon.EDGE_LABEL_FONT_FACE)) {
			font = dev.getVisualProperty(DVisualLexicon.EDGE_LABEL_FONT_FACE);
		} else {
			font = m_labelFonts.get(edge);
	
			if (font == null)
				font = m_labelFontDefault != null ? m_labelFontDefault : super.getLabelFont(edge, labelInx);
		}
		
		if (size != null && font != null)
			font = font.deriveFont(size.floatValue());
		
		return font;
	}

	void setLabelFontDefault(Font f) {
		m_labelFontDefault = f;
		defaultValues.put(DVisualLexicon.EDGE_LABEL_FONT_FACE, m_labelFontDefault);
		
		if (f != null)
			defaultValues.put(DVisualLexicon.EDGE_LABEL_FONT_SIZE, f.getSize());
	}

	/*
	 * A null font has the special meaning to remove overridden font.
	 */
	void overrideLabelFont(final CyEdge edge, final int labelInx, final Font font) {
		// final long key = (((long) edge) << 32) | ((long) labelInx);

		if ((font == null) || font.equals(super.getLabelFont(edge, labelInx)))
			m_labelFonts.remove(edge);
		else {
			m_labelFonts.put(edge, font);
			isCleared = false;
		}
	}

	@Override
	public Paint getLabelPaint(final CyEdge edge, final int labelInx) {
		Paint paint = null;
		Integer trans = null;
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		
		// First check if transparency is locked, because the stored colors may not contain the correct alpha value
		if (dev.isValueLocked(DVisualLexicon.EDGE_LABEL_TRANSPARENCY))
			trans = getLabelTransparency(edge);
		
		if (dev.isValueLocked(DVisualLexicon.EDGE_LABEL_COLOR)) {
			// Check bypass
			paint = dev.getVisualProperty(DVisualLexicon.EDGE_LABEL_COLOR);
		} else {
			paint = m_labelPaints.get(edge);

			if (paint == null)
				paint = m_labelPaintDefault != null ? m_labelPaintDefault : super.getLabelPaint(edge, labelInx);
		}
		
		if (trans != null)
			paint = dGraphView.getTransparentColor(paint, trans);

		return paint;
	}

	void setLabelPaintDefault(Paint p) {
		m_labelPaintDefault = p;
		defaultValues.put(DVisualLexicon.EDGE_LABEL_COLOR, m_labelPaintDefault);
	}

	/*
	 * A null paint has the special meaning to remove overridden paint.
	 */
	void overrideLabelPaint(final CyEdge edge, final int labelInx, final Paint paint) {

		if ((paint == null) || paint.equals(super.getLabelPaint(edge, labelInx)))
			m_labelPaints.remove(edge);
		else {
			m_labelPaints.put(edge, paint);
			isCleared = false;
		}
	}

	@Override
	public double getLabelWidth(final CyEdge edge) {
		// Check bypass
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		// TODO: Edge Label width?

		final Double width = m_labelWidths.get(edge);
		if (width == null) {
			if (m_labelWidthDefault == null)
				return super.getLabelWidth(edge);
			else
				return m_labelWidthDefault.doubleValue();
		}

		return width;
	}

	void setLabelWidthDefault(double width) {
		m_labelWidthDefault = width;
	}

	/*
	 * A negative width value has the special meaning to remove overridden
	 * width.
	 */
	void overrideLabelWidth(final CyEdge edge, final double width) {
		if ((width < 0.0) || (width == super.getLabelWidth(edge)))
			m_labelWidths.remove(edge);
		else {
			m_labelWidths.put(edge, width);
			isCleared = false;
		}
	}

	@Override
	public float getSourceArrowSize(CyEdge edge) {
		// For the half arrows, we need to scale multiplicatively
		// so that the arrow matches the line.
		final int arrowType = getSourceArrowShape(edge);
		if (arrowType == GraphGraphics.ARROW_HALF_TOP || arrowType == GraphGraphics.ARROW_HALF_BOTTOM)
			return (getWidth(edge) * DEdgeDetails.DEFAULT_ARROW_SIZE);

		// For all other arrows we can scale additively. This produces less
		// egregiously big arrows.
		else
			return (getWidth(edge) + DEdgeDetails.DEFAULT_ARROW_SIZE);
	}

	@Override
	public float getTargetArrowSize(CyEdge edge) {
		// For the half arrows, we need to scale multiplicatively
		// so that the arrow matches the line.
		final int arrowType = getTargetArrowShape(edge);
		if (arrowType == GraphGraphics.ARROW_HALF_TOP || arrowType == GraphGraphics.ARROW_HALF_BOTTOM)
			return (getWidth(edge) * DEdgeDetails.DEFAULT_ARROW_SIZE);
		// For all other arrows we can scale additively. This produces
		// less egregiously big arrows.
		else
			return (getWidth(edge) + DEdgeDetails.DEFAULT_ARROW_SIZE);
	}

	void overrideLineCurved(final CyEdge edge, final int type) {
		if (EdgeView.STRAIGHT_LINES == type || EdgeView.CURVED_LINES == type) {
			m_lineCurved.put(edge, type);
			isCleared = false;
		} else {
			m_lineCurved.remove(edge);
		}
	}

	Integer getLineCurved(final CyEdge edge) {
		// Check bypass
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		if (dev.isValueLocked(DVisualLexicon.EDGE_CURVED)) {
			Boolean lockedVal = dev.getVisualProperty(DVisualLexicon.EDGE_CURVED);
			if (lockedVal)
				return EdgeView.CURVED_LINES;
			else {
				return EdgeView.STRAIGHT_LINES;
			}
		}

		final Integer lineType = m_lineCurved.get(edge);
		if (lineType == null)
			if (m_lineCurvedDefault == null)
				return EdgeView.STRAIGHT_LINES;
			else
				return m_lineCurvedDefault;

		return lineType;
	}

	void setLineCurvedDefault(int lineType) {
		this.m_lineCurvedDefault = lineType;
	}

	Bend getBend(final CyEdge edge) {
		return getBend(edge, false);
	}

	/**
	 * Returns current Edge Bend value.
	 * 
	 * @param edge
	 * @return edge Bend
	 */
	synchronized Bend getBend(final CyEdge edge, boolean forceCreate) {
		// Check bypass
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		if (dev.isValueLocked(DVisualLexicon.EDGE_BEND))
			return dev.getVisualProperty(DVisualLexicon.EDGE_BEND);

		Bend bend = m_edgeBends.get(edge);

		if (bend == null && forceCreate) {
			bend = new BendImpl();
			m_edgeBends.put(edge, bend);
		}

		if (bend == null) {
			if (m_edgeBendDefault == null)
				return EdgeBendVisualProperty.DEFAULT_EDGE_BEND;
			else
				return m_edgeBendDefault;
		}

		if( bend == EdgeBendVisualProperty.DEFAULT_EDGE_BEND && m_edgeBendDefault != null )
			return m_edgeBendDefault;

		return bend;
	}

	void setEdgeBendDefault(final Bend bend) {
		this.m_edgeBendDefault = bend;
		defaultValues.put(DVisualLexicon.EDGE_BEND, m_edgeBendDefault);
	}

	// Used by bends
	private final MinLongHeap m_heap = new MinLongHeap();
	private final float[] m_extentsBuff = new float[4];

	@Override
	public EdgeAnchors getAnchors(final CyEdge edge) {
		final DEdgeView edgeView = (DEdgeView) dGraphView.getDEdgeView(edge);
		final EdgeAnchors returnThis = edgeView;

		if (returnThis.numAnchors() > 0)
			return returnThis;

		final CyNetwork graph = dGraphView.m_drawPersp;

		final long srcNodeIndex = edgeView.getModel().getSource().getSUID();
		final long trgNodeIndex = edgeView.getModel().getTarget().getSUID();

		// Calculate anchors necessary for self edges.
		if (srcNodeIndex == trgNodeIndex) {
			dGraphView.m_spacial.exists(srcNodeIndex, m_extentsBuff, 0);

			final double w = ((double) m_extentsBuff[2]) - m_extentsBuff[0];
			final double h = ((double) m_extentsBuff[3]) - m_extentsBuff[1];
			final double x = (((double) m_extentsBuff[0]) + m_extentsBuff[2]) / 2.0d;
			final double y = (((double) m_extentsBuff[1]) + m_extentsBuff[3]) / 2.0d;
			final double nodeSize = Math.max(w, h);
			int i = 0;

			final List<CyEdge> selfEdgeList = graph.getConnectingEdgeList(edgeView.getModel().getSource(), edgeView
					.getModel().getSource(), CyEdge.Type.ANY);
			// final IntIterator selfEdges = graph.edgesConnecting(srcNodeIndex,
			// srcNodeIndex, true, true, true);

			for (final CyEdge selfEdge : selfEdgeList) {
				// while (selfEdges.hasNext()) 
				// final int e2 = selfEdges.nextInt();
				final long e2 = selfEdge.getSUID();

				if (e2 == edge.getSUID())
					break;

				if (((EdgeAnchors) dGraphView.getDEdgeView(e2)).numAnchors() == 0)
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

		// Now add "invisible" anchors to edges for the case where multiple
		// edges
		// exist between two nodes. This has no effect if user specified anchors
		// exist on the edge.
		while (true) {

			// By consistently ordering the source and target nodes, dx and dy
			// will always
			// be calculated according to the same orientation. This allows the
			// offset
			// calculation to toggle the edges from side to side without any
			// overlap.
			final long tmpSrcIndex = Math.min(srcNodeIndex, trgNodeIndex);
			final long tmpTrgIndex = Math.max(srcNodeIndex, trgNodeIndex);

			// Sort the connecting edges.
			final CyNode tmpSrc = graph.getNode(tmpSrcIndex);
			final CyNode tmpTrg = graph.getNode(tmpTrgIndex);
			final List<CyEdge> conEdgeList = graph.getConnectingEdgeList(tmpSrc, tmpTrg, CyEdge.Type.ANY);
			// final IntIterator conEdges = graph.edgesConnecting(tmpSrc,
			// tmpTrg,
			// true, true, true);
			m_heap.empty();

			for (final CyEdge conEdge : conEdgeList) {
				// while (conEdges.hasNext()) 
				// m_heap.toss(conEdges.nextInt());
				m_heap.toss(conEdge.getSUID());
			}

			final LongEnumerator otherEdges = m_heap.orderedElements(false);

			long otherEdge = otherEdges.nextLong();

			// If the first other edge is the same as this edge,
			// (i.e. we're at the end of the list?).
			if (otherEdge == edge.getSUID())
				break;

			// So we don't count the other edge twice?
			int i = (((EdgeAnchors) dGraphView.getDEdgeView(otherEdge)).numAnchors() == 0) ? 1 : 0;

			// Count the number of other edges.
			while (true) {
				if (edge.getSUID() == (otherEdge = otherEdges.nextLong()))
					break;

				if (((EdgeAnchors) dGraphView.getDEdgeView(otherEdge)).numAnchors() == 0)
					i++;
			}

			final int inx = i;

			// Get source node size and position.
			dGraphView.m_spacial.exists(tmpSrcIndex, m_extentsBuff, 0);
			final double srcW = ((double) m_extentsBuff[2]) - m_extentsBuff[0];
			final double srcH = ((double) m_extentsBuff[3]) - m_extentsBuff[1];
			final double srcX = (((double) m_extentsBuff[0]) + m_extentsBuff[2]) / 2.0d;
			final double srcY = (((double) m_extentsBuff[1]) + m_extentsBuff[3]) / 2.0d;

			// Get target node size and position.
			dGraphView.m_spacial.exists(tmpTrgIndex, m_extentsBuff, 0);
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
			// Note that dx and dy may be negative. This is OK, because this
			// will ensure
			// that the handle is always correctly placed offset from the
			// midpoint of,
			// and perpendicular to, the original edge.
			final double dx = trgX - srcX;
			final double dy = trgY - srcY;

			// Distance or length between nodes.
			final double len = Math.sqrt((dx * dx) + (dy * dy));

			if (((float) len) == 0.0f)
				break;

			// This determines which side of the first edge and how far from the
			// first
			// edge the other edge should be placed.
			// - Divide by 2 puts consecutive edges at the same distance from
			// the center
			// because of integer math.
			// - Modulo puts consecutive edges on opposite sides.
			// - Node size is for consistent scaling.
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
	 * Size of handle graphics (square)
	 */
	@Override
	public float getAnchorSize(final CyEdge edge, final int anchorInx) {
		final DEdgeView eView = (DEdgeView) dGraphView.getDEdgeView(edge);

		if (eView.isSelected() && (eView.numAnchors() > 0))
			return dGraphView.getAnchorSize();
		else
			return 0.0f;
	}

	/**
	 * Color of handles.
	 */
	@Override
	public Paint getAnchorPaint(final CyEdge edge, int anchorInx) {
		if (getLineCurved(edge) == DEdgeView.STRAIGHT_LINES)
			anchorInx = anchorInx / 2;

		if (dGraphView.m_selectedAnchors.count((edge.getSUID() << 6) | anchorInx) > 0)
			return dGraphView.getAnchorSelectedPaint();
		else
			return dGraphView.getAnchorUnselectedPaint();
	}
	
	public <T, V extends T> V getDefaultValue(VisualProperty<T> vp) {
		return (V) defaultValues.get(vp);
	}
}
