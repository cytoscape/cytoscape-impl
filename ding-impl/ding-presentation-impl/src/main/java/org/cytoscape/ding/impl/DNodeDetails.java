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
import java.awt.TexturePaint;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

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
	Map<CyNode, Object> m_colorsLowDetail = new WeakHashMap<CyNode, Object>();
	Map<CyNode, Object> m_selectedColorsLowDetail = new WeakHashMap<CyNode, Object>();
	Map<CyNode, NodeShape> m_shapes = new WeakHashMap<CyNode, NodeShape>();
	Map<CyNode, Paint> m_unselectedPaints = new WeakHashMap<CyNode, Paint>();
	Map<CyNode, Paint> m_selectedPaints = new WeakHashMap<CyNode, Paint>();
	Map<CyNode, Float> m_borderWidths = new WeakHashMap<CyNode, Float>();
	Map<CyNode, Stroke> m_borderStrokes = new WeakHashMap<CyNode, Stroke>();
	Map<CyNode, Paint> m_borderPaints = new WeakHashMap<CyNode, Paint>();
	Map<CyNode, Integer> m_labelCounts = new WeakHashMap<CyNode, Integer>();
	Map<CyNode, String> m_labelTexts = new WeakHashMap<CyNode, String>();
	Map<CyNode, String> m_tooltipTexts = new WeakHashMap<CyNode, String>();
	Map<CyNode, Font> m_labelFonts = new WeakHashMap<CyNode, Font>();
	Map<CyNode, Paint> m_labelPaints = new WeakHashMap<CyNode, Paint>();
	Map<CyNode, Double> m_labelWidths = new WeakHashMap<CyNode, Double>();
	Map<CyNode, Integer> m_labelTextAnchors = new WeakHashMap<CyNode, Integer>();
	Map<CyNode, Integer> m_labelNodeAnchors = new WeakHashMap<CyNode, Integer>();
	Map<CyNode, Integer> m_labelJustifys = new WeakHashMap<CyNode, Integer>();
	Map<CyNode, Double> m_labelOffsetXs = new WeakHashMap<CyNode, Double>();
	Map<CyNode, Double> m_labelOffsetYs = new WeakHashMap<CyNode, Double>();
	Map<CyNode, Double> m_width = new WeakHashMap<CyNode, Double>();
	Map<CyNode, List<CustomGraphicLayer>> m_customGraphics = new WeakHashMap<CyNode, List<CustomGraphicLayer>>();
	Map<CyNode, Integer> m_nodeTansparencies = new WeakHashMap<CyNode, Integer>();
	Map<CyNode, Integer> m_nodeBorderTansparencies = new WeakHashMap<CyNode, Integer>();
	Map<CyNode, Integer> m_nodeLabelTansparencies = new WeakHashMap<CyNode, Integer>();

	private final Set<CyNode> selected = new HashSet<CyNode>();

	// Default values
	Color m_colorLowDetailDefault = (Color) DVisualLexicon.NODE_FILL_COLOR.getDefault();
	Color m_selectedColorLowDetailDefault = (Color) DVisualLexicon.NODE_SELECTED_PAINT.getDefault();
	Paint m_unselectedPaintDefault = DVisualLexicon.NODE_FILL_COLOR.getDefault();
	Paint m_selectedPaintDefault = DVisualLexicon.NODE_SELECTED_PAINT.getDefault();
	NodeShape m_shapeDefault = DVisualLexicon.NODE_SHAPE.getDefault();
	Double m_borderWidthDefault = DVisualLexicon.NODE_BORDER_WIDTH.getDefault();
	LineType m_borderStrokeDefault = DVisualLexicon.NODE_BORDER_LINE_TYPE.getDefault();
	Paint m_borderPaintDefault = DVisualLexicon.NODE_BORDER_PAINT.getDefault();
	Integer m_labelCountDefault;  // TODO: remove?
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

		m_colorsLowDetail = new WeakHashMap<CyNode, Object>();
		m_selectedColorsLowDetail = new WeakHashMap<CyNode, Object>();
		m_shapes = new WeakHashMap<CyNode, NodeShape>();
		m_unselectedPaints = new WeakHashMap<CyNode, Paint>();
		m_borderWidths = new WeakHashMap<CyNode, Float>();
		m_borderStrokes = new WeakHashMap<CyNode, Stroke>();
		m_borderPaints = new WeakHashMap<CyNode, Paint>();
		m_labelCounts = new WeakHashMap<CyNode, Integer>();
		m_labelTexts = new WeakHashMap<CyNode, String>();
		m_tooltipTexts = new WeakHashMap<CyNode, String>();
		m_labelFonts = new WeakHashMap<CyNode, Font>();
		m_labelPaints = new WeakHashMap<CyNode, Paint>();
		m_labelWidths = new WeakHashMap<CyNode, Double>();
		m_labelTextAnchors = new WeakHashMap<CyNode, Integer>();
		m_labelNodeAnchors = new WeakHashMap<CyNode, Integer>();
		m_labelJustifys = new WeakHashMap<CyNode, Integer>();
		m_labelOffsetXs = new WeakHashMap<CyNode, Double>();
		m_labelOffsetYs = new WeakHashMap<CyNode, Double>();
		m_width = new WeakHashMap<CyNode, Double>();
		m_selectedPaints = new WeakHashMap<CyNode, Paint>();
		m_customGraphics = new WeakHashMap<CyNode, List<CustomGraphicLayer>>();
		this.m_nodeTansparencies = new WeakHashMap<CyNode, Integer>();
		this.m_nodeBorderTansparencies = new WeakHashMap<CyNode, Integer>();
		this.m_nodeLabelTansparencies = new WeakHashMap<CyNode, Integer>();

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
		selected.remove(nodeIdx);
		m_labelCounts.remove(nodeIdx);
		m_labelTexts.remove(nodeIdx);
		m_labelFonts.remove(nodeIdx);
		m_labelPaints.remove(nodeIdx);
		m_nodeTansparencies.remove(nodeIdx);
		m_nodeBorderTansparencies.remove(nodeIdx);
		m_nodeLabelTansparencies.remove(nodeIdx);
	}

	@Override
	public Color colorLowDetail(final CyNode node) {
		boolean isSelected = selected.contains(node);

		if (isSelected)
			return selectedColorLowDetail(node);
		else
			return unselectedColorLowDetail(node);

	}

	private Color unselectedColorLowDetail(CyNode node) {
		// Check bypass
		final DNodeView dnv = dGraphView.getDNodeView(node);
		if (dnv.isValueLocked(DVisualLexicon.NODE_FILL_COLOR))
			return (Color) dnv.getVisualProperty(DVisualLexicon.NODE_FILL_COLOR);

		final Object o = m_colorsLowDetail.get(node);

		if ((o == null) || (o == m_deletedEntry))
			if (m_colorLowDetailDefault == null)
				return super.colorLowDetail(node);
			else
				return m_colorLowDetailDefault;

		return (Color) o;
	}

	void setColorLowDetailDefault(Color c) {
		m_colorLowDetailDefault = c;
		defaultValues.put(DVisualLexicon.NODE_FILL_COLOR, m_colorLowDetailDefault);
		defaultValues.put(DVisualLexicon.NODE_PAINT, m_colorLowDetailDefault);
	}

	private Color selectedColorLowDetail(CyNode node) {
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

	Paint selectedPaint(CyNode node) {
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
	public Paint unselectedPaint(final CyNode node) {
		// Check bypass
		final DNodeView dnv = dGraphView.getDNodeView(node);
		if (dnv.isValueLocked(DVisualLexicon.NODE_FILL_COLOR))
			return (Color) dnv.getVisualProperty(DVisualLexicon.NODE_FILL_COLOR);
		
		final Paint unselectedNodeFillPaint = m_unselectedPaints.get(node);

		if (unselectedNodeFillPaint == null) {
			// Mapped Value does not exist. Use default
			if (m_unselectedPaintDefault == null)
				return DVisualLexicon.NODE_FILL_COLOR.getDefault();
			else {
				return m_unselectedPaintDefault;
			}
		} else {
			return unselectedNodeFillPaint;
		}
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
		if (selected.contains(node))
			return selectedPaint(node);
		else
			return unselectedPaint(node);
	}

	void select(final CyNode node) {
		selected.add(node);
	}

	void unselect(final CyNode node) {
		selected.remove(node);
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

	/*
	 * A negative width value has the special meaning to remove overridden
	 * width.
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
		// Check bypass
		final DNodeView dnv = dGraphView.getDNodeView(node);
		if (dnv == null)
			return DVisualLexicon.NODE_BORDER_PAINT.getDefault();

		if (dnv.isValueLocked(DVisualLexicon.NODE_BORDER_PAINT))
			return dnv.getVisualProperty(DVisualLexicon.NODE_BORDER_PAINT);

		final Paint o = m_borderPaints.get(node);

		if (o == null)
			if (m_borderPaintDefault == null)
				return super.getBorderPaint(node);
			else
				return m_borderPaintDefault;

		return o;
	}

	void setBorderPaintDefault(Paint p) {
		m_borderPaintDefault = p;
		defaultValues.put(DVisualLexicon.NODE_BORDER_PAINT, p);
	}

	/*
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
	public int labelCount(final CyNode node) {
		final Integer o = m_labelCounts.get(node);

		if (o == null)
			if (m_labelCountDefault == null)
				return super.labelCount(node);
			else
				return m_labelCountDefault.intValue();

		return o;
	}

	void setLabelCountDefault(int lc) {
		m_labelCountDefault = Integer.valueOf(lc);
	}

	/*
	 * A negative labelCount has the special meaning to remove overridden count.
	 */
	void overrideLabelCount(final CyNode node, final int labelCount) {
		if ((labelCount < 0) || (labelCount == super.labelCount(node)))
			m_labelCounts.remove(node);
		else {
			m_labelCounts.put(node, labelCount);
			isCleared = false;
		}
	}

	@Override
	public String labelText(final CyNode node, final int labelInx) {
		// Check bypass
		final DNodeView dnv = dGraphView.getDNodeView(node);
		if (dnv.isValueLocked(DVisualLexicon.NODE_LABEL))
			return dnv.getVisualProperty(DVisualLexicon.NODE_LABEL);

		// final long key = (((long) node) << 32) | ((long) labelInx);
		final String o = m_labelTexts.get(node);

		if (o == null)
			if (m_labelTextDefault == null)
				return super.labelText(node, labelInx);
			else
				return m_labelTextDefault;

		return o;
	}

	void setLabelTextDefault(String text) {
		m_labelTextDefault = text;
		defaultValues.put(DVisualLexicon.NODE_LABEL, m_labelTextDefault);
	}

	/*
	 * A null text has the special meaning to remove overridden text.
	 */
	void overrideLabelText(final CyNode node, final int labelInx, final String text) {
		// final long key = (((long) node) << 32) | ((long) labelInx);

		if ((text == null) || text.equals(super.labelText(node, labelInx)))
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

	/*
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
		// Check bypass
		final DNodeView dnv = dGraphView.getDNodeView(node);

		Number size = null;
		Font fontFace = null;
		if (dnv.isValueLocked(DVisualLexicon.NODE_LABEL_FONT_SIZE))
			size = dnv.getVisualProperty(DVisualLexicon.NODE_LABEL_FONT_SIZE);
		if (dnv.isValueLocked(DVisualLexicon.NODE_LABEL_FONT_FACE))
			fontFace = dnv.getVisualProperty(DVisualLexicon.NODE_LABEL_FONT_FACE);

		if (size != null && fontFace != null)
			return fontFace.deriveFont(size.floatValue());

		final Font font = m_labelFonts.get(node);
		
		//System.out.println(dGraphView.getModel().getRow(node).get(CyNetwork.NAME, String.class)  + ": Font is = " + font);

		if (font == null) {
			if (m_labelFontDefault == null)
				return DVisualLexicon.NODE_LABEL_FONT_FACE.getDefault();
			else {
				return m_labelFontDefault;
			}
		} else {
			return font;
		}
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
	void overrideLabelFont(CyNode node, final Font font) {
		if (font == null) {
			m_labelFonts.remove(node);
		} else {
			m_labelFonts.put(node, font);
			isCleared = false;
		}
	}

	@Override
	public Paint labelPaint(CyNode node, int labelInx) {
		// Check bypass
		final DNodeView dnv = dGraphView.getDNodeView(node);
		if (dnv.isValueLocked(DVisualLexicon.NODE_LABEL_COLOR))
			return dnv.getVisualProperty(DVisualLexicon.NODE_LABEL_COLOR);

		final Object o = m_labelPaints.get(node);

		if (o == null)
			if (m_labelPaintDefault == null)
				return DVisualLexicon.NODE_LABEL_COLOR.getDefault();
			else
				return m_labelPaintDefault;

		return (Paint) o;
	}

	void setLabelPaintDefault(Paint p) {
		m_labelPaintDefault = p;
		defaultValues.put(DVisualLexicon.NODE_LABEL_COLOR, m_labelPaintDefault);
	}

	/*
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
	public int customGraphicCount(final CyNode node) {
		final DNodeView dnv = (DNodeView) dGraphView.getDNodeView(node);
		return dnv.getNumCustomGraphics();
	}

	@Override
	public Iterator<CustomGraphicLayer> customGraphics(final CyNode node) {
		final DNodeView dnv = (DNodeView) dGraphView.getDNodeView(node);
		return dnv.customGraphicIterator();
	}

	@Override
	public byte labelTextAnchor(final CyNode node, final int labelInx) {
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
				return super.labelTextAnchor(node, labelInx);
			else
				return m_labelTextAnchorDefault.byteValue();

		return convertG2ND(p);
	}

	void setLabelTextAnchorDefault(int anchor) {
		m_labelTextAnchorDefault = Byte.valueOf(convertG2ND(anchor));
	}

	void overrideLabelTextAnchor(final CyNode node, final int inx, final int anchor) {
		if (convertG2ND(anchor) == super.labelTextAnchor(node, inx))
			m_labelTextAnchors.remove(node);
		else {
			m_labelTextAnchors.put(node, Integer.valueOf(anchor));
			isCleared = false;
		}
	}

	@Override
	public byte labelNodeAnchor(final CyNode node, final int labelInx) {
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
				return super.labelNodeAnchor(node, labelInx);
			else
				return m_labelNodeAnchorDefault.byteValue();

		return convertG2ND(o);
	}

	void setLabelNodeAnchorDefault(int anchor) {
		m_labelNodeAnchorDefault = Byte.valueOf(convertG2ND(anchor));
	}

	void overrideLabelNodeAnchor(final CyNode node, final int inx, final int anchor) {
		if (convertG2ND(anchor) == super.labelNodeAnchor(node, inx))
			m_labelNodeAnchors.remove(node);
		else {
			m_labelNodeAnchors.put(node, Integer.valueOf(anchor));
			isCleared = false;
		}
	}

	@Override
	public float labelOffsetVectorX(final CyNode node, final int labelInx) {
		// Check bypass
		final DNodeView dnv = dGraphView.getDNodeView(node);
		if (dnv.isValueLocked(DVisualLexicon.NODE_LABEL_POSITION)) {
			final ObjectPosition lp = dnv.getVisualProperty(DVisualLexicon.NODE_LABEL_POSITION);
			return (float) lp.getOffsetX();
		}

		final Object o = m_labelOffsetXs.get(node);

		if (o == null)
			if (m_labelOffsetVectorXDefault == null)
				return super.labelOffsetVectorX(node, labelInx);
			else
				return m_labelOffsetVectorXDefault.floatValue();

		return ((Double) o).floatValue();
	}

	void setLabelOffsetVectorXDefault(double x) {
		m_labelOffsetVectorXDefault = Double.valueOf(x);
	}

	void overrideLabelOffsetVectorX(final CyNode node, final int inx, final double x) {
		if (((float) x) == super.labelOffsetVectorX(node, inx))
			m_labelOffsetXs.remove(node);
		else {
			m_labelOffsetXs.put(node, new Double(x));
			isCleared = false;
		}
	}

	@Override
	public float labelOffsetVectorY(final CyNode node, final int labelInx) {
		// Check bypass
		final DNodeView dnv = dGraphView.getDNodeView(node);
		if (dnv.isValueLocked(DVisualLexicon.NODE_LABEL_POSITION)) {
			final ObjectPosition lp = dnv.getVisualProperty(DVisualLexicon.NODE_LABEL_POSITION);
			return (float) lp.getOffsetY();
		}

		final Object o = m_labelOffsetYs.get(node);

		if (o == null)
			if (m_labelOffsetVectorYDefault == null)
				return super.labelOffsetVectorY(node, labelInx);
			else
				return m_labelOffsetVectorYDefault.floatValue();

		return ((Double) o).floatValue();
	}

	void setLabelOffsetVectorYDefault(double y) {
		m_labelOffsetVectorYDefault = Double.valueOf(y);
	}

	void overrideLabelOffsetVectorY(final CyNode node, final int inx, final double y) {
		if (((float) y) == super.labelOffsetVectorY(node, inx))
			m_labelOffsetYs.remove(node);
		else {
			m_labelOffsetYs.put(node, new Double(y));
			isCleared = false;
		}
	}

	@Override
	public byte labelJustify(final CyNode node, final int labelInx) {
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
				return super.labelJustify(node, labelInx);
			else
				return m_labelJustifyDefault.byteValue();

		return convertG2ND(o);
	}

	void setLabelJustifyDefault(int justify) {
		m_labelJustifyDefault = Byte.valueOf(convertG2ND(justify));		
	}

	void overrideLabelJustify(final CyNode node, final int inx, final int justify) {
		if (convertG2ND(justify) == super.labelJustify(node, inx))
			m_labelJustifys.remove(node);
		else {
			m_labelJustifys.put(node, Integer.valueOf(justify));
			isCleared = false;
		}
	}

	@Override
	public double labelWidth(CyNode node) {
		final Double o = m_labelWidths.get(node);

		if (o == null)
			if (m_labelWidthDefault == null)
				return super.labelWidth(node);
			else
				return m_labelWidthDefault.doubleValue();

		return o;
	}

	void setLabelWidthDefault(double width) {
		m_labelWidthDefault = width;
		defaultValues.put(DVisualLexicon.NODE_LABEL_WIDTH, m_labelWidthDefault);
	}

	/*
	 * A negative width value has the special meaning to remove overridden
	 * width.
	 */
	void overrideLabelWidth(final CyNode node, final double width) {
		if ((width < 0.0) || (width == super.labelWidth(node)))
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

		final Integer trans = m_nodeTansparencies.get(node);
		if (trans == null) {
			if (transparencyDefault == null)
				return DVisualLexicon.NODE_TRANSPARENCY.getDefault();
			else
				return transparencyDefault;
		} else
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

		final Integer trans = m_nodeLabelTansparencies.get(node);
		if (trans == null) {
			if (transparencyLabelDefault == null)
				return DVisualLexicon.NODE_TRANSPARENCY.getDefault();
			else
				return transparencyLabelDefault;
		} else
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

		final Integer trans = m_nodeBorderTansparencies.get(node);
		if (trans == null) {
			if (transparencyBorderDefault == null)
				return DVisualLexicon.NODE_BORDER_TRANSPARENCY.getDefault();
			else
				return transparencyBorderDefault;
		} else
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
