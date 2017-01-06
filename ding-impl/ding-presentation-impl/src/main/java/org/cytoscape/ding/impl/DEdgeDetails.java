package org.cytoscape.ding.impl;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

import static org.cytoscape.ding.DVisualLexicon.EDGE_CURVED;
import static org.cytoscape.ding.DVisualLexicon.EDGE_SOURCE_ARROW_UNSELECTED_PAINT;
import static org.cytoscape.ding.DVisualLexicon.EDGE_TARGET_ARROW_UNSELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_BEND;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_FONT_FACE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_FONT_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LINE_TYPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_SELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_SOURCE_ARROW_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_TARGET_ARROW_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_TOOLTIP;
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
import java.util.concurrent.ConcurrentHashMap;

import org.cytoscape.ding.DArrowShape;
import org.cytoscape.ding.EdgeView;
import org.cytoscape.ding.impl.strokes.AnimatedStroke;
import org.cytoscape.ding.impl.strokes.WidthStroke;
import org.cytoscape.graph.render.immed.EdgeAnchors;
import org.cytoscape.graph.render.stateful.EdgeDetails;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.util.intr.LongEnumerator;
import org.cytoscape.util.intr.MinLongHeap;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.EdgeBendVisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.LineType;

/**
 * Values stored in this object will be used renderer. Be careful to keep these
 * values consistent!
 */
final class DEdgeDetails extends EdgeDetails {

	private final DGraphView dGraphView;
	private final Map<VisualProperty<?>, Object> defaultValues; 

	// Mapped Values
	// If value found in these map objects, the value will be used by the renderer.
	// Otherwise, default value will be used.
	Map<CyEdge, Object> m_colorsLowDetail = new ConcurrentHashMap<>(16, 0.75f, 2);
	Map<CyEdge, Object> m_selectedColorsLowDetail = new ConcurrentHashMap<>(16, 0.75f, 2);
	Map<CyEdge, Float> m_widths = new ConcurrentHashMap<>(16, 0.75f, 2);
	Map<CyEdge, Stroke> m_strokes = new ConcurrentHashMap<>(16, 0.75f, 2);
	Map<CyEdge, ArrowShape> m_sourceArrows = new ConcurrentHashMap<>(16, 0.75f, 2);
	Map<CyEdge, Paint> m_sourceArrowPaints = new ConcurrentHashMap<>(16, 0.75f, 2);
	Map<CyEdge, Paint> m_sourceArrowSelectedPaints = new ConcurrentHashMap<>(16, 0.75f, 2);
	Map<CyEdge, Double> m_sourceArrowSizes = new ConcurrentHashMap<>(16, 0.75f, 2);
	Map<CyEdge, ArrowShape> m_targetArrows = new ConcurrentHashMap<>(16, 0.75f, 2);
	Map<CyEdge, Paint> m_targetArrowPaints = new ConcurrentHashMap<>(16, 0.75f, 2);
	Map<CyEdge, Paint> m_targetArrowSelectedPaints = new ConcurrentHashMap<>(16, 0.75f, 2);
	Map<CyEdge, Double> m_targetArrowSizes = new ConcurrentHashMap<>(16, 0.75f, 2);
	Map<CyEdge, Integer> m_labelCounts = new ConcurrentHashMap<>(16, 0.75f, 2);
	Map<CyEdge, String> m_labelTexts = new ConcurrentHashMap<>(16, 0.75f, 2);
	Map<CyEdge, Font> m_labelFonts = new ConcurrentHashMap<>(16, 0.75f, 2);
	Map<CyEdge, Paint> m_labelPaints = new ConcurrentHashMap<>(16, 0.75f, 2);
	Map<CyEdge, Double> m_labelWidths = new ConcurrentHashMap<>(16, 0.75f, 2);
	Map<CyEdge, Paint> m_unselectedPaints = new ConcurrentHashMap<>(16, 0.75f, 2);
	Map<CyEdge, Paint> m_selectedPaints = new ConcurrentHashMap<>(16, 0.75f, 2);
	Map<CyEdge, Integer> m_lineCurved = new ConcurrentHashMap<>(16, 0.75f, 2);
	Map<CyEdge, Bend> m_edgeBends = new ConcurrentHashMap<>(16, 0.75f, 2);
	Map<CyEdge, String> m_edgeTooltips = new ConcurrentHashMap<>(16, 0.75f, 2);
	Map<CyEdge, Integer> m_edgeTansparencies = new ConcurrentHashMap<>(16, 0.75f, 2);
	Map<CyEdge, Integer> m_edgeLabelTansparencies = new ConcurrentHashMap<>(16, 0.75f, 2);

	// Default Values
	ArrowShape m_sourceArrowDefault;
	Paint m_sourceArrowPaintDefault = EDGE_SOURCE_ARROW_UNSELECTED_PAINT.getDefault();
	Double m_sourceArrowSizeDefault = EDGE_SOURCE_ARROW_SIZE.getDefault();
	ArrowShape m_targetArrowDefault;
	Paint m_targetArrowPaintDefault = EDGE_TARGET_ARROW_UNSELECTED_PAINT.getDefault();
	Double m_targetArrowSizeDefault = EDGE_TARGET_ARROW_SIZE.getDefault();
	Double m_widthDefault = EDGE_WIDTH.getDefault();
	Stroke m_strokeDefault = new BasicStroke(m_widthDefault.floatValue());
	String m_labelTextDefault;
	Font m_labelFontDefault = EDGE_LABEL_FONT_FACE.getDefault();
	Paint m_labelPaintDefault = EDGE_LABEL_COLOR.getDefault();
	Double m_labelWidthDefault = EDGE_LABEL_WIDTH.getDefault();
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

	private final Set<CyEdge> selected = new HashSet<>();

	DEdgeDetails(final DGraphView view) {
		dGraphView = view;
		defaultValues = new HashMap<>();
	}

	void clear() {
		if (isCleared)
			return;

		m_widths = new ConcurrentHashMap<>();
		m_strokes = new ConcurrentHashMap<>();
		m_sourceArrows = new ConcurrentHashMap<>();
		m_sourceArrowPaints = new ConcurrentHashMap<>();
		m_sourceArrowSelectedPaints = new ConcurrentHashMap<>();
		m_sourceArrowSizes = new ConcurrentHashMap<>();
		m_targetArrows = new ConcurrentHashMap<>();
		m_targetArrowPaints = new ConcurrentHashMap<>();
		m_targetArrowSelectedPaints = new ConcurrentHashMap<>();
		m_targetArrowSizes = new ConcurrentHashMap<>();
		m_labelCounts = new ConcurrentHashMap<>();
		m_labelTexts = new ConcurrentHashMap<>();
		m_labelFonts = new ConcurrentHashMap<>();
		m_labelPaints = new ConcurrentHashMap<>();
		m_labelWidths = new ConcurrentHashMap<>();
		m_unselectedPaints = new ConcurrentHashMap<>();
		m_selectedPaints = new ConcurrentHashMap<>();
		m_colorsLowDetail = new ConcurrentHashMap<>();
		m_selectedColorsLowDetail = new ConcurrentHashMap<>();
		m_lineCurved = new ConcurrentHashMap<>();
		m_edgeBends = new ConcurrentHashMap<>();
		m_edgeTooltips = new ConcurrentHashMap<>();
		m_edgeTansparencies = new ConcurrentHashMap<>();
		m_edgeLabelTansparencies = new ConcurrentHashMap<>();

		isCleared = true;
	}

	void unregisterEdge(final CyEdge edge) {
		// To avoid a memory leak its important to permanently remove the node from all the maps.
		m_colorsLowDetail.remove(edge);
		m_selectedColorsLowDetail.remove(edge);
		m_widths.remove(edge);
		m_strokes.remove(edge);
		m_sourceArrows.remove(edge);
		m_sourceArrowPaints.remove(edge);
		m_sourceArrowSelectedPaints.remove(edge);
		m_sourceArrowSizes.remove(edge);
		m_targetArrows.remove(edge);
		m_targetArrowPaints.remove(edge);
		m_targetArrowSelectedPaints.remove(edge);
		m_targetArrowSizes.remove(edge);
		m_labelCounts.remove(edge);
		m_labelTexts.remove(edge);
		m_labelFonts.remove(edge);
		m_labelPaints.remove(edge);
		m_labelWidths.remove(edge);
		m_selectedPaints.remove(edge);
		m_unselectedPaints.remove(edge);
		m_lineCurved.remove(edge);
		m_edgeBends.remove(edge);
		selected.remove(edge);
		m_edgeTooltips.remove(edge);
		m_edgeTansparencies.remove(edge);
		m_edgeLabelTansparencies.remove(edge);
	}
	
	public <V> void setDefaultValue(final VisualProperty<V> vp, V value) {
		defaultValues.put(vp, value);
	}

	@Override
	public Color getColorLowDetail(final CyEdge edge) {
		boolean isSelected = selected.contains(edge);

		return isSelected ? getSelectedColorLowDetail(edge) : getUnselectedColorLowDetail(edge);
	}

	private final Color getUnselectedColorLowDetail(final CyEdge edge) {
		// Check bypass
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		
		if (dev.isValueLocked(EDGE_STROKE_UNSELECTED_PAINT))
			return (Color) dev.getVisualProperty(EDGE_STROKE_UNSELECTED_PAINT);

		final Object o = m_colorsLowDetail.get(edge);

		if (o == null)
			if (m_colorLowDetailDefault == null)
				return super.getColorLowDetail(edge);
			else
				return (Color) m_colorLowDetailDefault;

		return (Color) o;
	}

	private Color getSelectedColorLowDetail(final CyEdge edge) {
		// Check bypass
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		if (dev.isValueLocked(EDGE_STROKE_SELECTED_PAINT))
			return (Color) dev.getVisualProperty(EDGE_STROKE_SELECTED_PAINT);

		final Object o = m_selectedColorsLowDetail.get(edge);

		if (o == null)
			if (m_selectedColorLowDetailDefault == null)
				return super.getColorLowDetail(edge);
			else
				return (Color) m_selectedColorLowDetailDefault;

		return (Color) o;
	}

	void setSelectedColorLowDetailDefault(Color c) {
		m_selectedColorLowDetailDefault = c;
		defaultValues.put(EDGE_SELECTED_PAINT, m_selectedColorLowDetailDefault);
		defaultValues.put(EDGE_STROKE_SELECTED_PAINT, m_selectedColorLowDetailDefault);
	}

	void setColorLowDetailDefault(Color c) {
		m_colorLowDetailDefault = c;
		defaultValues.put(EDGE_UNSELECTED_PAINT, m_colorLowDetailDefault);
		defaultValues.put(EDGE_STROKE_UNSELECTED_PAINT, m_colorLowDetailDefault);
	}

	@Override
	public ArrowShape getSourceArrowShape(final CyEdge edge) {
		// Check bypass
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		
		if (dev.isValueLocked(EDGE_SOURCE_ARROW_SHAPE)) {
			final ArrowShape tgtArrow = dev.getVisualProperty(EDGE_SOURCE_ARROW_SHAPE);
			final String shapeID = tgtArrow.getSerializableString();
			
			return DArrowShape.parseArrowText(shapeID).getPresentationShape();
		}

		final ArrowShape arrow = m_sourceArrows.get(edge);
		
		if (arrow == null)
			return m_sourceArrowDefault == null ? super.getSourceArrowShape(edge) : m_sourceArrowDefault;

		return arrow;
	}

	void setSourceArrowDefault(final ArrowShape arrow) {
		m_sourceArrowDefault = arrow;
		defaultValues.put(EDGE_SOURCE_ARROW_SHAPE, arrow);
	}

	void overrideSourceArrow(final CyEdge edge, final ArrowShape arrowType) {
		if (arrowType == null) {
			m_sourceArrows.remove(edge);
		} else {
			m_sourceArrows.put(edge, arrowType);
			isCleared = false;
		}
	}

	@Override
	public Paint getSourceArrowPaint(final CyEdge edge) {
		boolean isSelected = selected.contains(edge);

		return isSelected ? getSelectedPaint(edge) : getSourceArrowUnselectedPaint(edge);
	}

	private final Paint getSourceArrowUnselectedPaint(final CyEdge edge) {
		// Check bypass
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		
		if (dev.isValueLocked(EDGE_SOURCE_ARROW_UNSELECTED_PAINT))
			return dev.getVisualProperty(EDGE_SOURCE_ARROW_UNSELECTED_PAINT);

		final Paint paint = m_sourceArrowPaints.get(edge);
		
		if (paint == null)
			return m_sourceArrowPaintDefault == null ? EDGE_SOURCE_ARROW_UNSELECTED_PAINT.getDefault()
					: m_sourceArrowPaintDefault;

		return paint;
	}

	void setSourceArrowPaintDefault(final Paint p) {
		m_sourceArrowPaintDefault = p;
		defaultValues.put(EDGE_SOURCE_ARROW_UNSELECTED_PAINT, m_sourceArrowPaintDefault);
	}

	/**
	 * A null paint has the special meaning to remove overridden paint.
	 */
	void overrideSourceArrowPaint(final CyEdge edge, final Paint paint) {
		if ((paint == null) || paint.equals(super.getSourceArrowPaint(edge))) {
			m_sourceArrowPaints.remove(edge);
		} else {
			m_sourceArrowPaints.put(edge, paint);
			isCleared = false;
		}
	}

	void overrideSourceArrowSelectedPaint(final CyEdge edge, final Paint paint) {
		if ((paint == null) || paint.equals(super.getSourceArrowPaint(edge))) {
			this.m_sourceArrowSelectedPaints.remove(edge);
		} else {
			m_sourceArrowSelectedPaints.put(edge, paint);
			isCleared = false;
		}
	}

	@Override
	public ArrowShape getTargetArrowShape(final CyEdge edge) {
		// Check bypass
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		
		if (dev.isValueLocked(EDGE_TARGET_ARROW_SHAPE)) {
			final ArrowShape tgtArrow = dev.getVisualProperty(EDGE_TARGET_ARROW_SHAPE);
			final String shapeID = tgtArrow.getSerializableString();
			
			return DArrowShape.parseArrowText(shapeID).getPresentationShape();
		}

		final ArrowShape arrow = m_targetArrows.get(edge);
		
		if (arrow == null)
			return m_targetArrowDefault == null ? super.getTargetArrowShape(edge) : m_targetArrowDefault;

		return arrow;
	}

	void setTargetArrowDefault(final ArrowShape arrow) {
		m_targetArrowDefault = arrow;
		defaultValues.put(EDGE_TARGET_ARROW_SHAPE, arrow);
	}

	void overrideTargetArrow(final CyEdge edge, final ArrowShape arrowType) {
		if (arrowType == null) {
			m_targetArrows.remove(edge);
		} else {
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

		return isSelected ? getSelectedPaint(edge) : getTargetArrowUnselectedPaint(edge);
	}

	private final Paint getTargetArrowUnselectedPaint(final CyEdge edge) {
		// Check bypass
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		
		if (dev.isValueLocked(EDGE_TARGET_ARROW_UNSELECTED_PAINT))
			return dev.getVisualProperty(EDGE_TARGET_ARROW_UNSELECTED_PAINT);

		final Paint paint = m_targetArrowPaints.get(edge);

		if (paint == null)
			return m_targetArrowPaintDefault == null ? EDGE_TARGET_ARROW_UNSELECTED_PAINT.getDefault()
					: this.m_targetArrowPaintDefault;

		return paint;
	}
	
	void setTargetArrowPaintDefault(final Paint p) {
		m_targetArrowPaintDefault = p;
		defaultValues.put(EDGE_TARGET_ARROW_UNSELECTED_PAINT, m_targetArrowPaintDefault);
	}

	/**
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

	/**
	 * A null paint has the special meaning to remove overridden paint.
	 */
	void overrideTargetArrowSelectedPaint(final CyEdge edge, final Paint paint) {
		if ((paint == null) || paint.equals(super.getTargetArrowPaint(edge))) {
			this.m_targetArrowSelectedPaints.remove(edge);
		} else {
			m_targetArrowSelectedPaints.put(edge, paint);
			isCleared = false;
		}
	}

	@Override
	public float getWidth(final CyEdge edge) {
		Float w = null;
		// Bypass check
		final DEdgeView edv = dGraphView.getDEdgeView(edge);
		
		if (edv.isValueLocked(EDGE_WIDTH)) {
			w = edv.getVisualProperty(EDGE_WIDTH).floatValue();
		} else {
			w = m_widths.get(edge);
			if (w == null) {
				if (m_widthDefault == null)
					w = super.getWidth(edge);
				else
					w = m_widthDefault.floatValue();
			}
		}

		return w;
	}

	void setWidthDefault(float width) {
		m_widthDefault = (double) width;
		defaultValues.put(EDGE_WIDTH, m_widthDefault);
	}

	/**
	 * A negative thickness value has the special meaning to remove overridden thickness.
	 */
	void overrideWidth(final CyEdge edge, final float width) {
		if ((width < 0.0f) || (width == super.getWidth(edge))) {
			m_widths.remove(edge);
		} else {
			m_widths.put(edge, width);
			isCleared = false;
		}
	}

	@Override
	public Stroke getStroke(final CyEdge edge) {
		Stroke stroke = null;
		final DEdgeView dev = dGraphView.getDEdgeView(edge);

		if (dev == null) return null;
		
		if (dev.isValueLocked(EDGE_LINE_TYPE) || dev.isValueLocked(EDGE_WIDTH)) {
			// If one of these properties are locked, the stroke has to be recreated
			final LineType lineType = dev.getVisualProperty(EDGE_LINE_TYPE);
			stroke = DLineType.getDLineType(lineType).getStroke(getWidth(edge));

			// We need to handle animated edges with some care...
			if (stroke instanceof AnimatedStroke) {
				Stroke oldStroke = m_strokes.get(edge);
				
				if (oldStroke != null && oldStroke.getClass().equals(stroke.getClass()))
					stroke = ((WidthStroke)oldStroke).newInstanceForWidth(getWidth(edge));
			}
		} else {
			stroke = m_strokes.get(edge);

			if (stroke == null) {
				if (m_strokeDefault == null)
					stroke = super.getStroke(edge);
				else
					stroke = m_strokeDefault;
			}
		}

		if (stroke instanceof AnimatedStroke)
			dGraphView.addAnimatedEdge(dev);
		else
			dGraphView.removeAnimatedEdge(dev);

		return stroke;
	}

	void setStrokeDefault(final Stroke s, final LineType t) {
		m_strokeDefault = s;
		defaultValues.put(EDGE_LINE_TYPE, t);
	}

	/*
	 * A null paint has the special meaning to remove overridden paint.
	 */
	void overrideStroke(final CyEdge edge, final Stroke stroke) {
		if ((stroke == null) || stroke.equals(super.getStroke(edge))) {
			m_strokes.remove(edge);
		} else {
			m_strokes.put(edge, stroke);
			isCleared = false;
		}
	}

	/**
	 * High detail paint.
	 */
	@Override
	public Paint getPaint(final CyEdge edge) {
		final boolean isSelected = selected.contains(edge);

		return isSelected ? getSelectedPaint(edge) : getUnselectedPaint(edge);
	}

	void setSegmentPaintDefault(final Paint p) {
		m_unselectedPaintDefault = p;
		defaultValues.put(EDGE_STROKE_UNSELECTED_PAINT, p);
		defaultValues.put(EDGE_UNSELECTED_PAINT, p);
	}

	Paint getUnselectedPaint(final CyEdge edge) {
		Paint paint = null;
		Integer trans = null;
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		
		// First check if transparency is locked, because the stored colors may not contain the correct alpha value
		if (dev.isValueLocked(EDGE_TRANSPARENCY))
			trans = getTransparency(edge);
		
		if (dev.isValueLocked(EDGE_STROKE_UNSELECTED_PAINT)) {
			paint = dev.getVisualProperty(EDGE_STROKE_UNSELECTED_PAINT);
		} else if (dev.isValueLocked(EDGE_UNSELECTED_PAINT)) {
			paint = dev.getVisualProperty(EDGE_UNSELECTED_PAINT);
		} else {
			paint = m_unselectedPaints.get(edge);

			if (paint == null)
				paint = m_unselectedPaintDefault == null ?
						EDGE_UNSELECTED_PAINT.getDefault() : m_unselectedPaintDefault;
		}
		
		if (trans != null)
			paint = dGraphView.getTransparentColor(paint, trans);

		return paint;
	}

	Paint getSelectedPaint(final CyEdge edge) {
		Paint paint = null;
		Integer trans = null;
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		
		if (dev.isValueLocked(EDGE_TRANSPARENCY))
			trans = getTransparency(edge);
		
		if (dev.isValueLocked(EDGE_STROKE_SELECTED_PAINT)) {
			paint = dev.getVisualProperty(EDGE_STROKE_SELECTED_PAINT);
		} else if (dev.isValueLocked(EDGE_SELECTED_PAINT)) {
			paint = dev.getVisualProperty(EDGE_SELECTED_PAINT);
		} else {
			paint = m_selectedPaints.get(edge);

			if (paint == null)
				paint = m_selectedPaintDefault == null ? EDGE_SELECTED_PAINT.getDefault() : m_selectedPaintDefault;
		}
		
		if (trans != null)
			paint = dGraphView.getTransparentColor(paint, trans);

		return paint;
	}

	void setSelectedPaintDefault(final Paint p) {
		m_selectedPaintDefault = p;
		defaultValues.put(EDGE_STROKE_SELECTED_PAINT, m_selectedPaintDefault);
		defaultValues.put(EDGE_SELECTED_PAINT, m_selectedPaintDefault);
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
		
		if (dev.isValueLocked(EDGE_LABEL) && !dev.getVisualProperty(EDGE_LABEL).isEmpty())
			return 1;
		
		Integer count = m_labelCounts.get(edge);
		
		if (count == null) {
			try {
				String defLabel = (String) defaultValues.get(EDGE_LABEL);
				count = (defLabel == null || defLabel.isEmpty()) ? super.getLabelCount(edge) : 1;
			} catch (ClassCastException e) {
				count = 0;
			}
		}
		
		return count;
	}

	/**
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
		
		if (dev.isValueLocked(EDGE_LABEL))
			return dev.getVisualProperty(EDGE_LABEL);

		final String text = m_labelTexts.get(edge);
		
		if (text == null)
			return m_labelTextDefault == null ? super.getLabelText(edge, labelInx) : m_labelTextDefault;

		return text;
	}

	void setLabelTextDefault(String text) {
		m_labelTextDefault = text;
		defaultValues.put(EDGE_LABEL, m_labelTextDefault);
	}

	/**
	 * A null text has the special meaning to remove overridden text.
	 */
	void overrideLabelText(final CyEdge edge, final int labelInx, final String text) {
		if ((text == null) || text.equals(super.getLabelText(edge, labelInx))) {
			m_labelTexts.remove(edge);
		} else {
			m_labelTexts.put(edge, text);
			isCleared = false;
		}
	}

	public String getTooltipText(final CyEdge edge, final int labelInx) {
		// Check bypass
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		
		if (dev.isValueLocked(EDGE_TOOLTIP))
			return dev.getVisualProperty(EDGE_TOOLTIP);

		final String text = m_edgeTooltips.get(edge);
		
		if (text == null)
			return m_edgeTooltipDefault == null ? EDGE_TOOLTIP.getDefault() : m_edgeTooltipDefault;

		return text;
	}

	void setTooltipTextDefault(String text) {
		m_edgeTooltipDefault = text;
		defaultValues.put(EDGE_TOOLTIP, m_edgeTooltipDefault);
	}

	void overrideTooltipText(final CyEdge edge, final String text) {
		if ((text == null) || text.equals("")) {
			m_edgeTooltips.remove(edge);
		} else {
			m_edgeTooltips.put(edge, text);
			isCleared = false;
		}
	}

	public Integer getTransparency(final CyEdge edge) {
		// Check bypass
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		
		if (dev.isValueLocked(EDGE_TRANSPARENCY))
			return dev.getVisualProperty(EDGE_TRANSPARENCY);

		Integer trans = m_edgeTansparencies.get(edge);
		
		if (trans == null)
			trans = transparencyDefault == null ? EDGE_TRANSPARENCY.getDefault() : transparencyDefault;
		
		return trans;
	}

	void setTransparencyDefault(Integer transparency) {
		transparencyDefault = transparency;
		defaultValues.put(EDGE_TRANSPARENCY, transparencyDefault);
	}

	void overrideTransparency(final CyEdge edge, final Integer transparency) {
		if (transparency == null) {
			m_edgeTansparencies.remove(edge);
		} else {
			m_edgeTansparencies.put(edge, transparency);
			isCleared = false;
		}
	}

	public Integer getLabelTransparency(final CyEdge edge) {
		// Check bypass
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		
		if (dev.isValueLocked(EDGE_LABEL_TRANSPARENCY))
			return dev.getVisualProperty(EDGE_LABEL_TRANSPARENCY);

		Integer trans = m_edgeLabelTansparencies.get(edge);
		
		if (trans == null)
			trans = labelTransparencyDefault == null ? EDGE_LABEL_TRANSPARENCY.getDefault() : labelTransparencyDefault;
		
		return trans;
	}

	void setLabelTransparencyDefault(Integer transparency) {
		labelTransparencyDefault = transparency;
		defaultValues.put(EDGE_LABEL_TRANSPARENCY, labelTransparencyDefault);
	}

	void overrideLabelTransparency(final CyEdge edge, final Integer transparency) {
		if (transparency == null) {
			m_edgeLabelTansparencies.remove(edge);
		} else {
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
		if (dev.isValueLocked(EDGE_LABEL_FONT_SIZE))
			size = dev.getVisualProperty(EDGE_LABEL_FONT_SIZE);
		
		if (dev.isValueLocked(EDGE_LABEL_FONT_FACE)) {
			font = dev.getVisualProperty(EDGE_LABEL_FONT_FACE);
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
		defaultValues.put(EDGE_LABEL_FONT_FACE, m_labelFontDefault);
		
		if (f != null)
			defaultValues.put(EDGE_LABEL_FONT_SIZE, f.getSize());
	}

	/**
	 * A null font has the special meaning to remove overridden font.
	 */
	void overrideLabelFont(final CyEdge edge, final int labelInx, final Font font) {
		// final long key = (((long) edge) << 32) | ((long) labelInx);

		if ((font == null) || font.equals(super.getLabelFont(edge, labelInx))) {
			m_labelFonts.remove(edge);
		} else {
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
		if (dev.isValueLocked(EDGE_LABEL_TRANSPARENCY))
			trans = getLabelTransparency(edge);
		
		if (dev.isValueLocked(EDGE_LABEL_COLOR)) {
			// Check bypass
			paint = dev.getVisualProperty(EDGE_LABEL_COLOR);
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
		defaultValues.put(EDGE_LABEL_COLOR, m_labelPaintDefault);
	}

	/**
	 * A null paint has the special meaning to remove overridden paint.
	 */
	void overrideLabelPaint(final CyEdge edge, final int labelInx, final Paint paint) {
		if ((paint == null) || paint.equals(super.getLabelPaint(edge, labelInx))) {
			m_labelPaints.remove(edge);
		} else {
			m_labelPaints.put(edge, paint);
			isCleared = false;
		}
	}

	@Override
	public double getLabelWidth(final CyEdge edge) {
		// Check bypass first
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		
		if (dev.isValueLocked(EDGE_LABEL_WIDTH))
			return dev.getVisualProperty(EDGE_LABEL_WIDTH);
		
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
		defaultValues.put(EDGE_LABEL_WIDTH, m_labelWidthDefault);
	}

	/**
	 * A negative width value has the special meaning to remove overridden width.
	 */
	void overrideLabelWidth(final CyEdge edge, final double width) {
		if ((width < 0.0) || (width == super.getLabelWidth(edge))) {
			m_labelWidths.remove(edge);
		} else {
			m_labelWidths.put(edge, width);
			isCleared = false;
		}
	}

	@Override
	public float getSourceArrowSize(final CyEdge edge) {
		Number size = null;
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		
		// Check bypass
		if (dev.isValueLocked(EDGE_SOURCE_ARROW_SIZE)) {
			size = dev.getVisualProperty(EDGE_SOURCE_ARROW_SIZE);
		} else {
			size = m_sourceArrowSizes.get(edge);
			
			if (size == null)
				size = m_sourceArrowSizeDefault != null ? m_sourceArrowSizeDefault : super.getSourceArrowSize(edge);
		}
		
		return adjustArrowSize(edge, getSourceArrowShape(edge), size);
	}
	
	void setSourceArrowSizeDefault(final double size) {
		m_sourceArrowSizeDefault = size;
		defaultValues.put(EDGE_SOURCE_ARROW_SIZE, size);
	}
	
	void overrideSourceArrowSize(final CyEdge edge, final double size) {
		if (size < 0.0 || size == super.getSourceArrowSize(edge)) {
			m_sourceArrowSizes.remove(edge);
		} else {
			m_sourceArrowSizes.put(edge, size);
			isCleared = false;
		}
	}

	@Override
	public float getTargetArrowSize(final CyEdge edge) {
		Number size = null;
		final DEdgeView dev = dGraphView.getDEdgeView(edge);
		
		// Check bypass
		if (dev.isValueLocked(EDGE_TARGET_ARROW_SIZE)) {
			size = dev.getVisualProperty(EDGE_TARGET_ARROW_SIZE);
		} else {
			size = m_targetArrowSizes.get(edge);
			
			if (size == null)
				size = m_targetArrowSizeDefault != null ? m_targetArrowSizeDefault : super.getTargetArrowSize(edge);
		}
		
		return adjustArrowSize(edge, getTargetArrowShape(edge), size);
	}
	
	void setTargetArrowSizeDefault(final double size) {
		m_targetArrowSizeDefault = size;
		defaultValues.put(EDGE_TARGET_ARROW_SIZE, size);
	}
	
	void overrideTargetArrowSize(final CyEdge edge, final double size) {
		if (size < 0.0 || size == super.getTargetArrowSize(edge)) {
			m_targetArrowSizes.remove(edge);
		} else {
			m_targetArrowSizes.put(edge, size);
			isCleared = false;
		}
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
		
		if (dev.isValueLocked(EDGE_CURVED)) {
			Boolean lockedVal = dev.getVisualProperty(EDGE_CURVED);
			
			return lockedVal ? EdgeView.CURVED_LINES : EdgeView.STRAIGHT_LINES;
		}

		final Integer lineType = m_lineCurved.get(edge);
		
		if (lineType == null)
			return m_lineCurvedDefault == null ? EdgeView.STRAIGHT_LINES : m_lineCurvedDefault;

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
		if (dev.isValueLocked(EDGE_BEND))
			return dev.getVisualProperty(EDGE_BEND);

		Bend bend = m_edgeBends.get(edge);

		if (bend == null && forceCreate) {
			bend = new BendImpl();
			m_edgeBends.put(edge, bend);
		}

		if (bend == null)
			return m_edgeBendDefault == null ? EdgeBendVisualProperty.DEFAULT_EDGE_BEND : m_edgeBendDefault;

		if (bend == EdgeBendVisualProperty.DEFAULT_EDGE_BEND && m_edgeBendDefault != null)
			return m_edgeBendDefault;

		return bend;
	}

	void setEdgeBendDefault(final Bend bend) {
		this.m_edgeBendDefault = bend;
		defaultValues.put(EDGE_BEND, m_edgeBendDefault);
	}

	// Used by bends
	private final MinLongHeap m_heap = new MinLongHeap();
	private final float[] m_extentsBuff = new float[4];

	public boolean isVisible(final CyEdge edge) {
		final DEdgeView edgeView = (DEdgeView) dGraphView.getDEdgeView(edge);
		return edgeView != null && edgeView.isVisible();
	}

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

		// Now add "invisible" anchors to edges for the case where multiple edges
		// exist between two nodes. This has no effect if user specified anchors exist on the edge.
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
				if (edge.getSUID() == (otherEdge = otherEdges.nextLong()) || otherEdge == -1)
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
			// Note that dx and dy may be negative. This is OK, because this will ensure
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
			// - Divide by 2 puts consecutive edges at the same distance from the center because of integer math.
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
			return getSelectedPaint(edge);
		else
			return getUnselectedPaint(edge);
	}
	
	@SuppressWarnings("unchecked")
	public <T, V extends T> V getDefaultValue(VisualProperty<T> vp) {
		return (V) defaultValues.get(vp);
	}
	
	private float adjustArrowSize(final CyEdge edge, final ArrowShape arrowType, final Number size) {
		// For the half arrows, we need to scale multiplicatively so that the arrow matches the line.
		if (arrowType == ArrowShapeVisualProperty.HALF_TOP || arrowType == ArrowShapeVisualProperty.HALF_BOTTOM)
			return getWidth(edge) * size.floatValue();
		else // For all other arrows, we can scale additively. This produces less egregious big arrows.
			return getWidth(edge) + size.floatValue();
	}
}
