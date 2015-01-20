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

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.cytoscape.ding.DNodeShape;
import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.Justification;
import org.cytoscape.ding.Label;
import org.cytoscape.ding.ObjectPosition;
import org.cytoscape.ding.Position;
import org.cytoscape.graph.render.stateful.NodeDetails;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;

/*
 * Access to the methods of this class should be synchronized externally if
 * there is a threat of multiple threads.
 */
class DNodeDetails extends NodeDetails {

	// Parent Network View
	private final DGraphView dGraphView;
	private final Object m_deletedEntry = new Object();
	
	private final Map<VisualProperty<?>, Object> defaultValues;

	// Mapped Values
	Map<CyNode, Object> m_colorsLowDetail = new ConcurrentHashMap<CyNode, Object>(16, 0.75f, 2);
	Map<CyNode, Object> m_selectedColorsLowDetail = new ConcurrentHashMap<CyNode, Object>(16, 0.75f, 2);
	Map<CyNode, NodeShape> m_shapes = new ConcurrentHashMap<CyNode, NodeShape>(16, 0.75f, 2);
	Map<CyNode, Paint> m_unselectedPaints = new ConcurrentHashMap<CyNode, Paint>(16, 0.75f, 2);
	Map<CyNode, Paint> m_selectedPaints = new ConcurrentHashMap<CyNode, Paint>(16, 0.75f, 2);
	Map<CyNode, Float> m_borderWidths = new ConcurrentHashMap<CyNode, Float>(16, 0.75f, 2);
	Map<CyNode, Stroke> m_borderStrokes = new ConcurrentHashMap<CyNode, Stroke>(16, 0.75f, 2);
	Map<CyNode, Paint> m_borderPaints = new ConcurrentHashMap<CyNode, Paint>(16, 0.75f, 2);
	Map<CyNode, Integer> m_labelCounts = new ConcurrentHashMap<CyNode, Integer>(16, 0.75f, 2);
	Map<CyNode, String> m_labelTexts = new ConcurrentHashMap<CyNode, String>(16, 0.75f, 2);
	Map<CyNode, String> m_tooltipTexts = new ConcurrentHashMap<CyNode, String>(16, 0.75f, 2);
	Map<CyNode, Font> m_labelFonts = new ConcurrentHashMap<CyNode, Font>(16, 0.75f, 2);
	Map<CyNode, Paint> m_labelPaints = new ConcurrentHashMap<CyNode, Paint>(16, 0.75f, 2);
	Map<CyNode, Double> m_labelWidths = new ConcurrentHashMap<CyNode, Double>(16, 0.75f, 2);
	Map<CyNode, Integer> m_labelTextAnchors = new ConcurrentHashMap<CyNode, Integer>(16, 0.75f, 2);
	Map<CyNode, Integer> m_labelNodeAnchors = new ConcurrentHashMap<CyNode, Integer>(16, 0.75f, 2);
	Map<CyNode, Integer> m_labelJustifys = new ConcurrentHashMap<CyNode, Integer>(16, 0.75f, 2);
	Map<CyNode, Double> m_labelOffsetXs = new ConcurrentHashMap<CyNode, Double>(16, 0.75f, 2);
	Map<CyNode, Double> m_labelOffsetYs = new ConcurrentHashMap<CyNode, Double>(16, 0.75f, 2);
	Map<CyNode, Double> m_width = new ConcurrentHashMap<CyNode, Double>(16, 0.75f, 2);
	Map<CyNode, List<CustomGraphicLayer>> m_customGraphics = new ConcurrentHashMap<CyNode, List<CustomGraphicLayer>>(16, 0.75f, 2);
	Map<CyNode, Integer> m_nodeTansparencies = new ConcurrentHashMap<CyNode, Integer>(16, 0.75f, 2);
	Map<CyNode, Integer> m_nodeBorderTansparencies = new ConcurrentHashMap<CyNode, Integer>(16, 0.75f, 2);
	Map<CyNode, Integer> m_nodeLabelTansparencies = new ConcurrentHashMap<CyNode, Integer>(16, 0.75f, 2);
	Map<CyNode, Double> m_nodeZ = new ConcurrentHashMap<CyNode, Double>(16, 0.75f, 2);
	Map<CyNode, Boolean> m_nestedNetworkImgVisible = new ConcurrentHashMap<CyNode, Boolean>(16, 0.75f, 2);

	private final Set<CyNode> selected = new HashSet<CyNode>();
	
	private final Object lock = new Object();

	// Default values
	Color m_colorLowDetailDefault = (Color) DVisualLexicon.NODE_FILL_COLOR.getDefault();
	Color m_selectedColorLowDetailDefault = (Color) DVisualLexicon.NODE_SELECTED_PAINT.getDefault();
	Paint m_unselectedPaintDefault = DVisualLexicon.NODE_FILL_COLOR.getDefault();
	Paint m_selectedPaintDefault = DVisualLexicon.NODE_SELECTED_PAINT.getDefault();
	NodeShape m_shapeDefault = DVisualLexicon.NODE_SHAPE.getDefault();
	Double m_borderWidthDefault = DVisualLexicon.NODE_BORDER_WIDTH.getDefault();
	LineType m_borderStrokeDefault = DVisualLexicon.NODE_BORDER_LINE_TYPE.getDefault();
	Paint m_borderPaintDefault = DVisualLexicon.NODE_BORDER_PAINT.getDefault();
	String m_labelTextDefault = DVisualLexicon.NODE_LABEL.getDefault();;
	String m_tooltipTextDefault = DVisualLexicon.NODE_TOOLTIP.getDefault();
	Font m_labelFontDefault = DVisualLexicon.NODE_LABEL_FONT_FACE.getDefault();
	Paint m_labelPaintDefault = DVisualLexicon.NODE_LABEL_COLOR.getDefault();
	Byte m_labelTextAnchorDefault;
	Byte m_labelNodeAnchorDefault;
	Double m_labelOffsetVectorXDefault;
	Double m_labelOffsetVectorYDefault;
	Byte m_labelJustifyDefault;
	Double m_labelWidthDefault = DVisualLexicon.NODE_LABEL_WIDTH.getDefault();
	Integer transparencyDefault = DVisualLexicon.NODE_TRANSPARENCY.getDefault();
	Integer transparencyBorderDefault = DVisualLexicon.NODE_BORDER_TRANSPARENCY.getDefault();
	Integer transparencyLabelDefault = DVisualLexicon.NODE_LABEL_TRANSPARENCY.getDefault();

	private boolean isCleared;

	DNodeDetails(final DGraphView view) {
		this.dGraphView = view;
		defaultValues = new HashMap<VisualProperty<?>, Object>();
	}

	void clear() {
		if (isCleared)
			return;

		m_colorsLowDetail = new ConcurrentHashMap<CyNode, Object>(16, 0.75f, 2);
		m_selectedColorsLowDetail = new ConcurrentHashMap<CyNode, Object>(16, 0.75f, 2);
		m_shapes = new ConcurrentHashMap<CyNode, NodeShape>(16, 0.75f, 2);
		m_unselectedPaints = new ConcurrentHashMap<CyNode, Paint>(16, 0.75f, 2);
		m_borderWidths = new ConcurrentHashMap<CyNode, Float>(16, 0.75f, 2);
		m_borderStrokes = new ConcurrentHashMap<CyNode, Stroke>(16, 0.75f, 2);
		m_borderPaints = new ConcurrentHashMap<CyNode, Paint>(16, 0.75f, 2);
		m_labelCounts = new ConcurrentHashMap<CyNode, Integer>(16, 0.75f, 2);
		m_labelTexts = new ConcurrentHashMap<CyNode, String>(16, 0.75f, 2);
		m_tooltipTexts = new ConcurrentHashMap<CyNode, String>(16, 0.75f, 2);
		m_labelFonts = new ConcurrentHashMap<CyNode, Font>(16, 0.75f, 2);
		m_labelPaints = new ConcurrentHashMap<CyNode, Paint>(16, 0.75f, 2);
		m_labelWidths = new ConcurrentHashMap<CyNode, Double>(16, 0.75f, 2);
		m_labelTextAnchors = new ConcurrentHashMap<CyNode, Integer>(16, 0.75f, 2);
		m_labelNodeAnchors = new ConcurrentHashMap<CyNode, Integer>(16, 0.75f, 2);
		m_labelJustifys = new ConcurrentHashMap<CyNode, Integer>(16, 0.75f, 2);
		m_labelOffsetXs = new ConcurrentHashMap<CyNode, Double>(16, 0.75f, 2);
		m_labelOffsetYs = new ConcurrentHashMap<CyNode, Double>(16, 0.75f, 2);
		m_width = new ConcurrentHashMap<CyNode, Double>(16, 0.75f, 2);
		m_selectedPaints = new ConcurrentHashMap<CyNode, Paint>(16, 0.75f, 2);
		m_customGraphics = new ConcurrentHashMap<CyNode, List<CustomGraphicLayer>>(16, 0.75f, 2);
		this.m_nodeTansparencies = new ConcurrentHashMap<CyNode, Integer>(16, 0.75f, 2);
		this.m_nodeBorderTansparencies = new ConcurrentHashMap<CyNode, Integer>(16, 0.75f, 2);
		this.m_nodeLabelTansparencies = new ConcurrentHashMap<CyNode, Integer>(16, 0.75f, 2);
		m_nodeZ = new ConcurrentHashMap<CyNode, Double>(16, 0.75f, 2);
		m_nestedNetworkImgVisible = new ConcurrentHashMap<CyNode, Boolean>(16, 0.75f, 2);

		// Clear all Custom Graphics
		for (final View<CyNode> nv : dGraphView.getNodeViews())
			((DNodeView) nv).removeAllCustomGraphics();

		isCleared = true;
	}

	void unregisterNode(final CyNode nodeIdx) {
		final Object o = m_colorsLowDetail.get(nodeIdx);
		if ((o != null) && (o != m_deletedEntry))
			m_colorsLowDetail.put(nodeIdx, m_deletedEntry);

		final Object os = m_selectedColorsLowDetail.get(nodeIdx);
		if ((os != null) && (os != m_deletedEntry))
			m_selectedColorsLowDetail.put(nodeIdx, m_deletedEntry);

		m_shapes.remove(nodeIdx);
		m_unselectedPaints.remove(nodeIdx);
		m_borderWidths.remove(nodeIdx);
		m_borderStrokes.remove(nodeIdx);
		m_borderPaints.remove(nodeIdx);
		m_labelWidths.remove(nodeIdx);
		m_labelTextAnchors.remove(nodeIdx);
		m_labelNodeAnchors.remove(nodeIdx);
		m_labelJustifys.remove(nodeIdx);
		m_labelOffsetXs.remove(nodeIdx);
		m_labelOffsetYs.remove(nodeIdx);
		m_selectedPaints.remove(nodeIdx);
		m_tooltipTexts.remove(nodeIdx);
		
		synchronized (lock) {
			selected.remove(nodeIdx);
		}
		
		m_labelCounts.remove(nodeIdx);
		m_labelTexts.remove(nodeIdx);
		m_labelFonts.remove(nodeIdx);
		m_labelPaints.remove(nodeIdx);
		m_nodeTansparencies.remove(nodeIdx);
		m_nodeBorderTansparencies.remove(nodeIdx);
		m_nodeLabelTansparencies.remove(nodeIdx);
		m_nodeZ.remove(nodeIdx);
		m_nestedNetworkImgVisible.remove(nodeIdx);
	}

	public <V> void setDefaultValue(final VisualProperty<V> vp, V value) {
		defaultValues.put(vp, value);
	}
	
	@Override
	public Color getColorLowDetail(final CyNode node) {
		boolean isSelected;
		synchronized (lock) {
			isSelected = selected.contains(node);
		}

		if (isSelected)
			return getSelectedColorLowDetail(node);
		else
			return getUnselectedColorLowDetail(node);

	}

	private Color getUnselectedColorLowDetail(CyNode node) {
		// Check bypass
		final DNodeView dnv = dGraphView.getDNodeView(node);
		if (dnv.isValueLocked(DVisualLexicon.NODE_FILL_COLOR))
			return (Color) dnv.getVisualProperty(DVisualLexicon.NODE_FILL_COLOR);

		final Object o = m_colorsLowDetail.get(node);

		if ((o == null) || (o == m_deletedEntry))
			if (m_colorLowDetailDefault == null)
				return super.getColorLowDetail(node);
			else
				return m_colorLowDetailDefault;

		return (Color) o;
	}

	void setColorLowDetailDefault(Color c) {
		m_colorLowDetailDefault = c;
		defaultValues.put(DVisualLexicon.NODE_FILL_COLOR, m_colorLowDetailDefault);
		defaultValues.put(DVisualLexicon.NODE_PAINT, m_colorLowDetailDefault);
	}

	private Color getSelectedColorLowDetail(CyNode node) {
		// Check bypass
		final DNodeView dnv = dGraphView.getDNodeView(node);
		if (dnv.isValueLocked(DVisualLexicon.NODE_SELECTED_PAINT))
			return (Color) dnv.getVisualProperty(DVisualLexicon.NODE_SELECTED_PAINT);

		final Object o = m_selectedColorsLowDetail.get(node);

		if ((o == null) || (o == m_deletedEntry))
			if (m_selectedColorLowDetailDefault == null)
				return (Color) DNodeView.DEFAULT_NODE_SELECTED_PAINT;
			else
				return m_selectedColorLowDetailDefault;

		return (Color) o;
	}

	void setSelectedColorLowDetailDefault(Color c) {
		m_selectedColorLowDetailDefault = c;
		defaultValues.put(DVisualLexicon.NODE_SELECTED_PAINT, m_selectedColorLowDetailDefault);
	}

	Paint getSelectedPaint(final CyNode node) {
		// Check bypass
		final DNodeView dnv = dGraphView.getDNodeView(node);
		if (dnv.isValueLocked(DVisualLexicon.NODE_SELECTED_PAINT))
			return dnv.getVisualProperty(DVisualLexicon.NODE_SELECTED_PAINT);

		final Paint o = m_selectedPaints.get(node);

		if (o == null)
			if (m_selectedPaintDefault == null)
				return DNodeView.DEFAULT_NODE_SELECTED_PAINT;
			else
				return m_selectedPaintDefault;

		return o;
	}

	void setSelectedPaintDefault(Paint c) {
		m_selectedPaintDefault = c;
		defaultValues.put(DVisualLexicon.NODE_SELECTED_PAINT, m_selectedPaintDefault);
	}

	@Override
	public byte getShape(final CyNode node) {
		// Check bypass
		final DNodeView dnv = dGraphView.getDNodeView(node);
		if (dnv.isValueLocked(DVisualLexicon.NODE_SHAPE))
			return DNodeShape.getDShape(dnv.getVisualProperty(DVisualLexicon.NODE_SHAPE)).getNativeShape();

		final NodeShape originaShape = m_shapes.get(node);

		if (originaShape == null) {
			if (m_shapeDefault == null)
				return super.getShape(node);
			else
				return DNodeShape.getDShape(m_shapeDefault).getNativeShape();
		} else {
			return DNodeShape.getDShape(originaShape).getNativeShape();
		}
	}

	void setShapeDefault(NodeShape shape) {
		m_shapeDefault = shape;
		defaultValues.put(DVisualLexicon.NODE_SHAPE, m_shapeDefault);
	}

	/*
	 * The shape argument must be pre-checked for correctness. A negative shape
	 * value has the special meaning to remove overridden shape.
	 */
	void overrideShape(CyNode node, NodeShape shape) {
		if (shape == null)
			m_shapes.remove(node);
		else {
			m_shapes.put(node, shape);
			isCleared = false;
		}
	}

	/**
	 * Note: this will be used for BOTH unselected and selected.
	 */
	public Paint getUnselectedPaint(final CyNode node) {
		Paint paint = null;
		Integer trans = null;
		final DNodeView dev = dGraphView.getDNodeView(node);
		
		// First check if transparency is locked, because the stored colors may not contain the correct alpha value
		if (dev.isValueLocked(DVisualLexicon.NODE_TRANSPARENCY))
			trans = getTransparency(node);
		
		if (dev.isValueLocked(DVisualLexicon.NODE_FILL_COLOR)) {
			paint = dev.getVisualProperty(DVisualLexicon.NODE_FILL_COLOR);
		} else {
			paint = m_unselectedPaints.get(node);

			if (paint == null) {
				// Mapped Value does not exist; use default
				if (m_unselectedPaintDefault == null)
					paint = DVisualLexicon.NODE_FILL_COLOR.getDefault();
				else
					paint = m_unselectedPaintDefault;
			}
		}
		
		if (trans != null)
			paint = dGraphView.getTransparentColor(paint, trans);
		
		return paint;
	}

	void setUnselectedPaintDefault(Paint p) {
		m_unselectedPaintDefault = p;
		defaultValues.put(DVisualLexicon.NODE_FILL_COLOR, m_unselectedPaintDefault);
		defaultValues.put(DVisualLexicon.NODE_PAINT, m_unselectedPaintDefault);
	}

	void setUnselectedPaint(final CyNode node, final Paint paint) {
		m_unselectedPaints.put(node, paint);
		if (paint instanceof Color)
			m_colorsLowDetail.put(node, paint);
		isCleared = false;
	}

	void setSelectedPaint(final CyNode node, final Paint paint) {
		m_selectedPaints.put(node, paint);
		if (paint instanceof Color)
			m_selectedColorsLowDetail.put(node, paint);

		isCleared = false;
	}

	/**
	 * This returns actual Color object to the low-level renderer.
	 */
	@Override
	public Paint getFillPaint(final CyNode node) {
		boolean isSelected;
		synchronized (lock) {
			isSelected = selected.contains(node);
		}
		
		if (isSelected)
			return getSelectedPaint(node);
		else
			return getUnselectedPaint(node);
	}

	void select(final CyNode node) {
		synchronized (lock) {
			selected.add(node);
		}
	}

	void unselect(final CyNode node) {
		synchronized (lock) {
			selected.remove(node);
		}
	}

	@Override
	public float getBorderWidth(final CyNode node) {
		// Check bypass
		final DNodeView dnv = dGraphView.getDNodeView(node);
		if (dnv.isValueLocked(DVisualLexicon.NODE_BORDER_WIDTH))
			return dnv.getVisualProperty(DVisualLexicon.NODE_BORDER_WIDTH).floatValue();

		final Float o = m_borderWidths.get(node);
		
		if (o == null)
			if (m_borderWidthDefault == null)
				return DVisualLexicon.NODE_BORDER_WIDTH.getDefault().floatValue();
			else
				return m_borderWidthDefault.floatValue();

		return o;
	}

	void setBorderWidthDefault(float width) {
		m_borderWidthDefault = Double.valueOf(width);
		defaultValues.put(DVisualLexicon.NODE_BORDER_WIDTH, m_borderWidthDefault);
	}

	/**
	 * A negative width value has the special meaning to remove overridden width.
	 */
	void overrideBorderWidth(final CyNode node, final float width) {
		if ((width < 0.0f) || (width == super.getBorderWidth(node)))
			m_borderWidths.remove(node);
		else {
			m_borderWidths.put(node, width);
			isCleared = false;
		}
	}
	
	@Override
	public Stroke getBorderStroke(final CyNode node) {
		final float borderWidth = getBorderWidth(node);
		// Check bypass
		final DNodeView dnv = dGraphView.getDNodeView(node);
		if (dnv.isValueLocked(DVisualLexicon.NODE_BORDER_LINE_TYPE)) {
			final LineType lockedLineType = dnv.getVisualProperty(DVisualLexicon.NODE_BORDER_LINE_TYPE);
			return DLineType.getDLineType(lockedLineType).getStroke(borderWidth);
		}

		final Stroke stroke = m_borderStrokes.get(node);
		
		if (stroke == null) {
			if (m_borderStrokeDefault == null) {
				final LineType vpDefaultLineType = dnv.getVisualProperty(DVisualLexicon.NODE_BORDER_LINE_TYPE);
				return DLineType.getDLineType(vpDefaultLineType).getStroke(borderWidth);
			} else {
				
				return DLineType.getDLineType(m_borderStrokeDefault).getStroke(borderWidth);
			}
		} else {
			return stroke;
		}
	}

	void setBorderLineTypeDefault(final LineType lineType) {
		m_borderStrokeDefault = lineType;
		defaultValues.put(DVisualLexicon.NODE_BORDER_LINE_TYPE, m_borderStrokeDefault);
	}
	
	void overrideBorderStroke(final CyNode node, final Stroke stroke) {
		if (stroke == null)
			m_borderStrokes.remove(node);
		else {
			m_borderStrokes.put(node, stroke);
			isCleared = false;
		}
	}
	
	@Override
	public Paint getBorderPaint(final CyNode node) {
		final DNodeView dnv = dGraphView.getDNodeView(node);
		
		if (dnv == null)
			return DVisualLexicon.NODE_BORDER_PAINT.getDefault();
		
		Paint paint = null;
		Integer trans = null;
		
		// First check if transparency is locked, because the stored colors may not contain the correct alpha value
		if (dnv.isValueLocked(DVisualLexicon.NODE_BORDER_TRANSPARENCY))
			trans = getBorderTransparency(node);
		
		if (dnv.isValueLocked(DVisualLexicon.NODE_BORDER_PAINT)) {
			paint = dnv.getVisualProperty(DVisualLexicon.NODE_BORDER_PAINT);
		} else {
			paint = m_borderPaints.get(node);

			if (paint == null)
				paint = m_borderPaintDefault != null ? m_borderPaintDefault : super.getBorderPaint(node);
		}

		if (trans != null) // New transparency? Recreate the paint object
			paint = dGraphView.getTransparentColor(paint, trans);
		
		if (paint != null)
			return paint;

		return DVisualLexicon.NODE_BORDER_PAINT.getDefault();
	}

	void setBorderPaintDefault(Paint p) {
		m_borderPaintDefault = p;
		defaultValues.put(DVisualLexicon.NODE_BORDER_PAINT, p);
	}

	/**
	 * A null paint has the special meaning to remove overridden paint.
	 */
	void overrideBorderPaint(final CyNode node, final Paint paint) {
		if ((paint == null) || paint.equals(super.getBorderPaint(node)))
			m_borderPaints.remove(node);
		else {
			m_borderPaints.put(node, paint);
			isCleared = false;
		}
	}

	@Override
	public int getLabelCount(final CyNode node) {
		// Check related bypass
		final DNodeView dnv = dGraphView.getDNodeView(node);
		
		if (dnv.isValueLocked(DVisualLexicon.NODE_LABEL) && !dnv.getVisualProperty(DVisualLexicon.NODE_LABEL).isEmpty())
			return 1;
		
		Integer count = m_labelCounts.get(node);
		
		if (count == null) {
			try {
				String defLabel = (String) defaultValues.get(DVisualLexicon.NODE_LABEL);
				count = (defLabel == null || defLabel.isEmpty()) ? super.getLabelCount(node) : 1;
			} catch (ClassCastException e) {
				count = 0;
			}
		}

		return count;
	}

	/**
	 * A negative labelCount has the special meaning to remove overridden count.
	 */
	void overrideLabelCount(final CyNode node, final int labelCount) {
		if ((labelCount < 0) || (labelCount == super.getLabelCount(node)))
			m_labelCounts.remove(node);
		else {
			m_labelCounts.put(node, labelCount);
			isCleared = false;
		}
	}

	@Override
	public String getLabelText(final CyNode node, final int labelInx) {
		// Check bypass
		final DNodeView dnv = dGraphView.getDNodeView(node);
		if (dnv.isValueLocked(DVisualLexicon.NODE_LABEL))
			return dnv.getVisualProperty(DVisualLexicon.NODE_LABEL);

		// final long key = (((long) node) << 32) | ((long) labelInx);
		final String o = m_labelTexts.get(node);

		if (o == null)
			if (m_labelTextDefault == null)
				return super.getLabelText(node, labelInx);
			else
				return m_labelTextDefault;

		return o;
	}

	void setLabelTextDefault(String text) {
		m_labelTextDefault = text;
		defaultValues.put(DVisualLexicon.NODE_LABEL, m_labelTextDefault);
	}

	/**
	 * A null text has the special meaning to remove overridden text.
	 */
	void overrideLabelText(final CyNode node, final int labelInx, final String text) {
		// final long key = (((long) node) << 32) | ((long) labelInx);

		if ((text == null) || text.equals(super.getLabelText(node, labelInx)))
			m_labelTexts.remove(node);
		else {
			m_labelTexts.put(node, text);
			isCleared = false;
		}
	}

	public String getTooltipText(final CyNode node) {
		// Check bypass
		final DNodeView dnv = dGraphView.getDNodeView(node);
		if (dnv.isValueLocked(DVisualLexicon.NODE_TOOLTIP))
			return dnv.getVisualProperty(DVisualLexicon.NODE_TOOLTIP);

		final String o = m_tooltipTexts.get(node);

		if (o == null)
			if (m_tooltipTextDefault == null)
				return "";
			else
				return m_tooltipTextDefault;

		return o;
	}

	void setTooltipTextDefault(String tooltip) {
		m_tooltipTextDefault = tooltip;
		defaultValues.put(DVisualLexicon.NODE_TOOLTIP, m_tooltipTextDefault);
	}

	/**
	 * A null text has the special meaning to remove overridden text.
	 */
	void overrideTooltipText(final CyNode node, final String text) {

		if ((text == null) || text.equals(""))
			m_tooltipTexts.remove(node);
		else {
			m_tooltipTexts.put(node, text);
			isCleared = false;
		}
	}

	@Override
	public Font getLabelFont(CyNode node, int labelInx) {
		Number size = null;
		Font font = null;
		final DNodeView dnv = dGraphView.getDNodeView(node);
		
		// Check bypass
		if (dnv.isValueLocked(DVisualLexicon.NODE_LABEL_FONT_SIZE))
			size = dnv.getVisualProperty(DVisualLexicon.NODE_LABEL_FONT_SIZE);
		
		if (dnv.isValueLocked(DVisualLexicon.NODE_LABEL_FONT_FACE)) {
			font = dnv.getVisualProperty(DVisualLexicon.NODE_LABEL_FONT_FACE);
		} else {
			font = m_labelFonts.get(node);
	
			if (font == null) {
				font = m_labelFontDefault != null ? 
						m_labelFontDefault : DVisualLexicon.NODE_LABEL_FONT_FACE.getDefault();
			}
		}
		
		if (size != null && font != null)
			font = font.deriveFont(size.floatValue());
		
		return font;
	}

	void setLabelFontDefault(Font f) {
		m_labelFontDefault = f;
		defaultValues.put(DVisualLexicon.NODE_LABEL_FONT_FACE, m_labelFontDefault);
		
		if (f != null)
			defaultValues.put(DVisualLexicon.NODE_LABEL_FONT_SIZE, f.getSize());
	}

	/*
	 * A null font has the special meaning to remove overridden font.
	 */
	void overrideLabelFont(final CyNode node, final Font font) {
		if (font == null) {
			m_labelFonts.remove(node);
		} else {
			m_labelFonts.put(node, font);
			isCleared = false;
		}
	}

	@Override
	public Paint getLabelPaint(final CyNode node, final int labelInx) {
		Paint paint = null;
		Integer trans = null;
		final DNodeView dnv = dGraphView.getDNodeView(node);
		
		// First check if transparency is locked, because the stored colors may not contain the correct alpha value
		if (dnv.isValueLocked(DVisualLexicon.NODE_LABEL_TRANSPARENCY))
			trans = getLabelTransparency(node);
		
		if (dnv.isValueLocked(DVisualLexicon.NODE_LABEL_COLOR)) {
			// Check bypass
			paint = dnv.getVisualProperty(DVisualLexicon.NODE_LABEL_COLOR);
		} else {
			paint = m_labelPaints.get(node);

			if (paint == null)
				paint = m_labelPaintDefault != null ? m_labelPaintDefault : DVisualLexicon.NODE_LABEL_COLOR.getDefault();
		}
		
		if (trans != null)
			paint = dGraphView.getTransparentColor(paint, trans);

		return paint;
	}

	void setLabelPaintDefault(Paint p) {
		m_labelPaintDefault = p;
		defaultValues.put(DVisualLexicon.NODE_LABEL_COLOR, m_labelPaintDefault);
	}

	/**
	 * A null paint has the special meaning to remove overridden paint.
	 */
	void overrideLabelPaint(CyNode node, int labelInx, Paint paint) {
		if (paint == null)
			m_labelPaints.remove(node);
		else {
			m_labelPaints.put(node, paint);
			isCleared = false;
		}
	}

	@Override
	public int getCustomGraphicCount(final CyNode node) {
		final DNodeView dnv = (DNodeView) dGraphView.getDNodeView(node);
		return dnv.getNumCustomGraphics();
	}

	@Override
	public Iterator<CustomGraphicLayer> getCustomGraphics(final CyNode node) {
		final DNodeView dnv = (DNodeView) dGraphView.getDNodeView(node);
		return dnv.customGraphicIterator();
	}

	@Override
	public byte getLabelTextAnchor(final CyNode node, final int labelInx) {
		// Check bypass
		final DNodeView dnv = dGraphView.getDNodeView(node);
		if (dnv.isValueLocked(DVisualLexicon.NODE_LABEL_POSITION)) {
			final ObjectPosition lp = dnv.getVisualProperty(DVisualLexicon.NODE_LABEL_POSITION);
			final Position anchor = lp.getAnchor();
			return convertG2ND(anchor.getConversionConstant());
		}

		final Integer p = m_labelTextAnchors.get(node);

		if (p == null)
			if (m_labelTextAnchorDefault == null)
				return super.getLabelTextAnchor(node, labelInx);
			else
				return m_labelTextAnchorDefault.byteValue();

		return convertG2ND(p);
	}

	void setLabelTextAnchorDefault(int anchor) {
		m_labelTextAnchorDefault = Byte.valueOf(convertG2ND(anchor));
	}

	void overrideLabelTextAnchor(final CyNode node, final int inx, final int anchor) {
		if (convertG2ND(anchor) == super.getLabelTextAnchor(node, inx))
			m_labelTextAnchors.remove(node);
		else {
			m_labelTextAnchors.put(node, Integer.valueOf(anchor));
			isCleared = false;
		}
	}

	@Override
	public byte getLabelNodeAnchor(final CyNode node, final int labelInx) {
		// Check bypass
		final DNodeView dnv = dGraphView.getDNodeView(node);
		if (dnv.isValueLocked(DVisualLexicon.NODE_LABEL_POSITION)) {
			final ObjectPosition lp = dnv.getVisualProperty(DVisualLexicon.NODE_LABEL_POSITION);
			final Position anchor = lp.getTargetAnchor();
			return convertG2ND(anchor.getConversionConstant());
		}

		final Integer o = m_labelNodeAnchors.get(node);

		if (o == null)
			if (m_labelNodeAnchorDefault == null)
				return super.getLabelNodeAnchor(node, labelInx);
			else
				return m_labelNodeAnchorDefault.byteValue();

		return convertG2ND(o);
	}

	void setLabelNodeAnchorDefault(int anchor) {
		m_labelNodeAnchorDefault = Byte.valueOf(convertG2ND(anchor));
	}

	void overrideLabelNodeAnchor(final CyNode node, final int inx, final int anchor) {
		if (convertG2ND(anchor) == super.getLabelNodeAnchor(node, inx))
			m_labelNodeAnchors.remove(node);
		else {
			m_labelNodeAnchors.put(node, Integer.valueOf(anchor));
			isCleared = false;
		}
	}

	@Override
	public float getLabelOffsetVectorX(final CyNode node, final int labelInx) {
		// Check bypass
		final DNodeView dnv = dGraphView.getDNodeView(node);
		if (dnv.isValueLocked(DVisualLexicon.NODE_LABEL_POSITION)) {
			final ObjectPosition lp = dnv.getVisualProperty(DVisualLexicon.NODE_LABEL_POSITION);
			return (float) lp.getOffsetX();
		}

		final Object o = m_labelOffsetXs.get(node);

		if (o == null)
			if (m_labelOffsetVectorXDefault == null)
				return super.getLabelOffsetVectorX(node, labelInx);
			else
				return m_labelOffsetVectorXDefault.floatValue();

		return ((Double) o).floatValue();
	}

	void setLabelOffsetVectorXDefault(double x) {
		m_labelOffsetVectorXDefault = Double.valueOf(x);
	}

	void overrideLabelOffsetVectorX(final CyNode node, final int inx, final double x) {
		if (((float) x) == super.getLabelOffsetVectorX(node, inx))
			m_labelOffsetXs.remove(node);
		else {
			m_labelOffsetXs.put(node, new Double(x));
			isCleared = false;
		}
	}

	@Override
	public float getLabelOffsetVectorY(final CyNode node, final int labelInx) {
		// Check bypass
		final DNodeView dnv = dGraphView.getDNodeView(node);
		if (dnv.isValueLocked(DVisualLexicon.NODE_LABEL_POSITION)) {
			final ObjectPosition lp = dnv.getVisualProperty(DVisualLexicon.NODE_LABEL_POSITION);
			return (float) lp.getOffsetY();
		}

		final Object o = m_labelOffsetYs.get(node);

		if (o == null)
			if (m_labelOffsetVectorYDefault == null)
				return super.getLabelOffsetVectorY(node, labelInx);
			else
				return m_labelOffsetVectorYDefault.floatValue();

		return ((Double) o).floatValue();
	}

	void setLabelOffsetVectorYDefault(double y) {
		m_labelOffsetVectorYDefault = Double.valueOf(y);
	}

	void overrideLabelOffsetVectorY(final CyNode node, final int inx, final double y) {
		if (((float) y) == super.getLabelOffsetVectorY(node, inx))
			m_labelOffsetYs.remove(node);
		else {
			m_labelOffsetYs.put(node, new Double(y));
			isCleared = false;
		}
	}

	@Override
	public byte getLabelJustify(final CyNode node, final int labelInx) {
		// Check bypass
		final DNodeView dnv = dGraphView.getDNodeView(node);
		if (dnv.isValueLocked(DVisualLexicon.NODE_LABEL_POSITION)) {
			final ObjectPosition lp = dnv.getVisualProperty(DVisualLexicon.NODE_LABEL_POSITION);
			final Justification justify = lp.getJustify();
			return convertG2ND(justify.getConversionConstant());
		}

		Integer o = m_labelJustifys.get(node);

		if (o == null)
			if (m_labelJustifyDefault == null)
				return super.getLabelJustify(node, labelInx);
			else
				return m_labelJustifyDefault.byteValue();

		return convertG2ND(o);
	}

	void setLabelJustifyDefault(int justify) {
		m_labelJustifyDefault = Byte.valueOf(convertG2ND(justify));		
	}

	void overrideLabelJustify(final CyNode node, final int inx, final int justify) {
		if (convertG2ND(justify) == super.getLabelJustify(node, inx))
			m_labelJustifys.remove(node);
		else {
			m_labelJustifys.put(node, Integer.valueOf(justify));
			isCleared = false;
		}
	}

	@Override
	public double getLabelWidth(CyNode node) {
		final Double o = m_labelWidths.get(node);

		if (o == null)
			if (m_labelWidthDefault == null)
				return super.getLabelWidth(node);
			else
				return m_labelWidthDefault.doubleValue();

		return o;
	}

	void setLabelWidthDefault(double width) {
		m_labelWidthDefault = width;
		defaultValues.put(DVisualLexicon.NODE_LABEL_WIDTH, m_labelWidthDefault);
	}

	/**
	 * A negative width value has the special meaning to remove overridden width.
	 */
	void overrideLabelWidth(final CyNode node, final double width) {
		if ((width < 0.0) || (width == super.getLabelWidth(node)))
			m_labelWidths.remove(node);
		else {
			m_labelWidths.put(node, width);
			isCleared = false;
		}
	}
	
	////// Transparencies /////////////
	
	public Integer getTransparency(final CyNode node) {
		// Check bypass
		final DNodeView dnv = dGraphView.getDNodeView(node);
		if (dnv.isValueLocked(DVisualLexicon.NODE_TRANSPARENCY))
			return dnv.getVisualProperty(DVisualLexicon.NODE_TRANSPARENCY);

		Integer trans = m_nodeTansparencies.get(node);
		if (trans == null) {
			trans = transparencyDefault != null ? transparencyDefault : DVisualLexicon.NODE_TRANSPARENCY.getDefault();
		}
		
		return trans;
	}

	void setTransparencyDefault(Integer transparency) {
		transparencyDefault = transparency;
		defaultValues.put(DVisualLexicon.NODE_TRANSPARENCY, transparencyDefault);
	}

	void overrideTransparency(final CyNode node, final Integer transparency) {
		if (transparency == null)
			m_nodeTansparencies.remove(node);
		else {
			m_nodeTansparencies.put(node, transparency);
			isCleared = false;
		}
	}
	
	public Integer getLabelTransparency(final CyNode node) {
		// Check bypass
		final DNodeView dnv = dGraphView.getDNodeView(node);
		if (dnv.isValueLocked(DVisualLexicon.NODE_LABEL_TRANSPARENCY))
			return dnv.getVisualProperty(DVisualLexicon.NODE_LABEL_TRANSPARENCY);

		Integer trans = m_nodeLabelTansparencies.get(node);
		if (trans == null) {
			trans = transparencyLabelDefault != null ? 
					transparencyLabelDefault : DVisualLexicon.NODE_LABEL_TRANSPARENCY.getDefault();
		}
		
		return trans;
	}

	void setLabelTransparencyDefault(Integer transparency) {
		transparencyLabelDefault = transparency;
		defaultValues.put(DVisualLexicon.NODE_LABEL_TRANSPARENCY, transparencyLabelDefault);
	}

	void overrideLabelTransparency(final CyNode node, final Integer transparency) {
		if (transparency == null)
			m_nodeLabelTansparencies.remove(node);
		else {
			m_nodeLabelTansparencies.put(node, transparency);
			isCleared = false;
		}
	}
	
	public Integer getBorderTransparency(final CyNode node) {
		// Check bypass
		final DNodeView dnv = dGraphView.getDNodeView(node);
		if (dnv.isValueLocked(DVisualLexicon.NODE_BORDER_TRANSPARENCY))
			return dnv.getVisualProperty(DVisualLexicon.NODE_BORDER_TRANSPARENCY);

		Integer trans = m_nodeBorderTansparencies.get(node);
		if (trans == null) {
			trans = transparencyBorderDefault != null ? 
					transparencyBorderDefault : DVisualLexicon.NODE_BORDER_TRANSPARENCY.getDefault();
		}
		
		return trans;
	}

	void setBorderTransparencyDefault(Integer transparency) {
		transparencyBorderDefault = transparency;
		defaultValues.put(DVisualLexicon.NODE_BORDER_TRANSPARENCY, transparencyBorderDefault);
	}

	void overrideBorderTransparency(final CyNode node, final Integer transparency) {
		if (transparency == null)
			m_nodeBorderTansparencies.remove(node);
		else {
			m_nodeBorderTansparencies.put(node, transparency);
			isCleared = false;
		}
	}
	
	void setNestedNetworkImgVisibleDefault(boolean visible) {
		defaultValues.put(BasicVisualLexicon.NODE_NESTED_NETWORK_IMAGE_VISIBLE, visible);
	}
	
	void overrideNestedNetworkImgVisible(final CyNode node, final boolean visible) {
		m_nestedNetworkImgVisible.put(node, visible);
		isCleared = false;
	}
	
	Boolean getNestedNetworkImgVisible(final CyNode node) {
		final DNodeView dnv = (DNodeView) dGraphView.getDNodeView(node);
		
		if (dnv.isValueLocked(BasicVisualLexicon.NODE_NESTED_NETWORK_IMAGE_VISIBLE))
			return dnv.getVisualProperty(BasicVisualLexicon.NODE_NESTED_NETWORK_IMAGE_VISIBLE);
		
		Boolean visible = m_nestedNetworkImgVisible.get(node);
		
		if (visible != null)
			return visible;
		
		return getDefaultValue(BasicVisualLexicon.NODE_NESTED_NETWORK_IMAGE_VISIBLE);
	}

	public Double getNodeDepth(final CyNode node) {
		final DNodeView dnv = dGraphView.getDNodeView(node);
		if (dnv.isValueLocked(DVisualLexicon.NODE_DEPTH))
			return dnv.getVisualProperty(DVisualLexicon.NODE_DEPTH);

		Double depth = m_nodeZ.get(node);
		if (depth == null)
			return 0.0;
		return depth;
	}


	@Override
	public TexturePaint getNestedNetworkTexturePaint(final CyNode node) {
		final DNodeView dNodeView = (DNodeView) dGraphView.getDNodeView(node);
		return dNodeView.getNestedNetworkTexturePaint();
	}

	static byte convertG2ND(final int giny) {
		switch (giny) {
		case (Label.NORTH):
			return NodeDetails.ANCHOR_NORTH;

		case (Label.SOUTH):
			return NodeDetails.ANCHOR_SOUTH;

		case (Label.EAST):
			return NodeDetails.ANCHOR_EAST;

		case (Label.WEST):
			return NodeDetails.ANCHOR_WEST;

		case (Label.NORTHEAST):
			return NodeDetails.ANCHOR_NORTHEAST;

		case (Label.NORTHWEST):
			return NodeDetails.ANCHOR_NORTHWEST;

		case (Label.SOUTHEAST):
			return NodeDetails.ANCHOR_SOUTHEAST;

		case (Label.SOUTHWEST):
			return NodeDetails.ANCHOR_SOUTHWEST;

		case (Label.CENTER):
			return NodeDetails.ANCHOR_CENTER;

		case (Label.JUSTIFY_CENTER):
			return NodeDetails.LABEL_WRAP_JUSTIFY_CENTER;

		case (Label.JUSTIFY_RIGHT):
			return NodeDetails.LABEL_WRAP_JUSTIFY_RIGHT;

		case (Label.JUSTIFY_LEFT):
			return NodeDetails.LABEL_WRAP_JUSTIFY_LEFT;

		default:
			return -1;
		}
	}

	static int convertND2G(byte nd) {
		switch (nd) {
		case (NodeDetails.ANCHOR_NORTH):
			return Label.NORTH;

		case (NodeDetails.ANCHOR_SOUTH):
			return Label.SOUTH;

		case (NodeDetails.ANCHOR_EAST):
			return Label.EAST;

		case (NodeDetails.ANCHOR_WEST):
			return Label.WEST;

		case (NodeDetails.ANCHOR_NORTHEAST):
			return Label.NORTHEAST;

		case (NodeDetails.ANCHOR_NORTHWEST):
			return Label.NORTHWEST;

		case (NodeDetails.ANCHOR_SOUTHEAST):
			return Label.SOUTHEAST;

		case (NodeDetails.ANCHOR_SOUTHWEST):
			return Label.SOUTHWEST;

		case (NodeDetails.ANCHOR_CENTER):
			return Label.CENTER;

		case (NodeDetails.LABEL_WRAP_JUSTIFY_CENTER):
			return Label.JUSTIFY_CENTER;

		case (NodeDetails.LABEL_WRAP_JUSTIFY_RIGHT):
			return Label.JUSTIFY_RIGHT;

		case (NodeDetails.LABEL_WRAP_JUSTIFY_LEFT):
			return Label.JUSTIFY_LEFT;

		default:
			return -1;
		}
	}
	
	public <T, V extends T> V getDefaultValue(VisualProperty<T> vp) {
		return (V) defaultValues.get(vp);
	}
}
