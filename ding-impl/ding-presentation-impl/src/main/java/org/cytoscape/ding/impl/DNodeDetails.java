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
import java.awt.TexturePaint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.ding.DNodeShape;
import org.cytoscape.ding.Label;
import org.cytoscape.ding.ObjectPosition;
import org.cytoscape.ding.customgraphics.CyCustomGraphics;
import org.cytoscape.ding.impl.visualproperty.CustomGraphicsVisualProperty;
import org.cytoscape.ding.impl.visualproperty.ObjectPositionVisualProperty;
import org.cytoscape.graph.render.stateful.CustomGraphic;
import org.cytoscape.graph.render.stateful.NodeDetails;
import org.cytoscape.model.CyNode;
import org.cytoscape.util.intr.IntObjHash;
import org.cytoscape.view.model.View;


/*
 * Access to the methods of this class should be synchronized externally if
 * there is a threat of multiple threads.
 */
class DNodeDetails extends IntermediateNodeDetails {
	
	
	final DGraphView m_view;
	
	final Object m_deletedEntry = new Object();

	IntObjHash m_colorsLowDetail = new IntObjHash();
	IntObjHash m_selectedColorsLowDetail = new IntObjHash();
	
	Map<Integer, Byte> m_shapes = new HashMap<Integer, Byte>();
	Map<Integer, Paint> m_unselectedPaints = new HashMap<Integer, Paint>();
	Map<Integer, Paint> m_selectedPaints = new HashMap<Integer, Paint>();
	
	Map<Integer, Float> m_borderWidths = new HashMap<Integer, Float>();
	Map<Integer, Paint> m_borderPaints = new HashMap<Integer, Paint>();
	Map<Integer, Integer> m_labelCounts = new HashMap<Integer, Integer>();
	
	Map<Long, String> m_labelTexts = new HashMap<Long, String>();
	Map<Integer, String> m_tooltipTexts = new HashMap<Integer, String>();

	Map<Long, Font> m_labelFonts = new HashMap<Long, Font>();
	Map<Long, Paint> m_labelPaints = new HashMap<Long, Paint>();
	Map<Integer, Double> m_labelWidths = new HashMap<Integer, Double>();
	
	Map<Integer, Integer> m_labelTextAnchors = new HashMap<Integer, Integer>();
	Map<Integer, Integer> m_labelNodeAnchors = new HashMap<Integer, Integer>();
	Map<Integer, Integer> m_labelJustifys = new HashMap<Integer, Integer>();
	Map<Integer, Double> m_labelOffsetXs = new HashMap<Integer, Double>();
	Map<Integer, Double> m_labelOffsetYs = new HashMap<Integer, Double>();
	
	Map<Integer, Double> m_width = new HashMap<Integer, Double>();
	
	Map<Integer, List<CustomGraphic>> m_customGraphics = new HashMap<Integer, List<CustomGraphic>>();
	
	private Set<Integer> selected = new HashSet<Integer>();

	// Default values
	private Color m_colorLowDetailDefault;
	private Color m_selectedColorLowDetailDefault;
	private Paint m_unselectedPaintDefault; 
	private Paint m_selectedPaintDefault;

	
	private DNodeShape m_shapeDefault; 
	private Float m_borderWidthDefault; 
	private Paint m_borderPaintDefault; 
	private Integer m_labelCountDefault; 
	
	private String m_labelTextDefault;
	private String m_tooltipTextDefault;
	
	private Font m_labelFontDefault; 
	private Paint m_labelPaintDefault; 
	private Byte m_labelTextAnchorDefault; 
	private Byte m_labelNodeAnchorDefault; 
	private Double m_labelOffsetVectorXDefault; 
	private Double m_labelOffsetVectorYDefault;
	private Byte m_labelJustifyDefault; 
	private Double m_labelWidthDefault;
	
	private Map<CustomGraphicsVisualProperty, CyCustomGraphics<CustomGraphic>> defaultCustomGraphicsMap;
	private Map<ObjectPositionVisualProperty, ObjectPosition> defaultCustomGraphicsPositionMap;
	
	private List<CustomGraphic> defaultCGList;

	private boolean isCleared = false;
	
	DNodeDetails(final DGraphView view) {
		m_view = view;
		
		defaultCustomGraphicsMap = new HashMap<CustomGraphicsVisualProperty, CyCustomGraphics<CustomGraphic>>();
		defaultCustomGraphicsPositionMap = new HashMap<ObjectPositionVisualProperty, ObjectPosition>();
	}
	
	void clear() {
		if(isCleared)
			return;
		
		m_colorsLowDetail = new IntObjHash();
		m_selectedColorsLowDetail = new IntObjHash();
		
		m_shapes = new HashMap<Integer, Byte>();
		m_unselectedPaints = new HashMap<Integer, Paint>();
		m_borderWidths = new HashMap<Integer, Float>();
		m_borderPaints = new HashMap<Integer, Paint>();
		m_labelCounts = new HashMap<Integer, Integer>();
		m_labelTexts = new HashMap<Long, String>();
		m_tooltipTexts = new HashMap<Integer, String>();
		m_labelFonts = new HashMap<Long, Font>();
		m_labelPaints = new HashMap<Long, Paint>();
		m_labelWidths = new HashMap<Integer, Double>();
		
		m_labelTextAnchors = new HashMap<Integer, Integer>();
		m_labelNodeAnchors = new HashMap<Integer, Integer>();
		m_labelJustifys = new HashMap<Integer, Integer>();
		m_labelOffsetXs = new HashMap<Integer, Double>();
		m_labelOffsetYs = new HashMap<Integer, Double>();
		m_width = new HashMap<Integer, Double>();
		
		m_selectedPaints = new HashMap<Integer, Paint>();
		
		m_customGraphics = new HashMap<Integer, List<CustomGraphic>>();
		
		defaultCustomGraphicsMap = new HashMap<CustomGraphicsVisualProperty, CyCustomGraphics<CustomGraphic>>();
		defaultCustomGraphicsPositionMap = new HashMap<ObjectPositionVisualProperty, ObjectPosition>();
		defaultCGList = new ArrayList<CustomGraphic>();
		
		// Clear all Custom Graphics
		for(final View<CyNode> nv: m_view.getNodeViews())
			((DNodeView) nv).removeAllCustomGraphics();
	
		isCleared = true;
	}

	void unregisterNode(final int nodeIdx) {
		final Object o = m_colorsLowDetail.get(nodeIdx);
		if ((o != null) && (o != m_deletedEntry))
			m_colorsLowDetail.put(nodeIdx, m_deletedEntry);
		
		final Object os = m_selectedColorsLowDetail.get(nodeIdx);
		if ((os != null) && (os != m_deletedEntry))
			m_selectedColorsLowDetail.put(nodeIdx, m_deletedEntry);

		m_shapes.remove(nodeIdx);
		m_unselectedPaints.remove(nodeIdx);
		m_borderWidths.remove(nodeIdx);
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

		final Integer intr = m_labelCounts.remove(nodeIdx);
		final int labelCount = ((intr == null) ? 0 : intr);

		for (int i = 0; i < labelCount; i++) {
			final Long lKey = (((long) nodeIdx) << 32) | ((long) i);
			m_labelTexts.remove(lKey);
			m_labelFonts.remove(lKey);
			m_labelPaints.remove(lKey);
		}
	}
	
	
	@Override
	public Color colorLowDetail(int node) {
		boolean isSelected = selected.contains(node);
		
		if(isSelected)
			return selectedColorLowDetail(node);
		else
			return unselectedColorLowDetail(node);
		
	}
	
	public Color unselectedColorLowDetail(int node) {
		final Object o = m_colorsLowDetail.get(node);

		if ((o == null) || (o == m_deletedEntry))
			if ( m_colorLowDetailDefault == null )
				return super.colorLowDetail(node);
			else
				return m_colorLowDetailDefault;

		return (Color) o;
	}

	void setColorLowDetailDefault(Color c) {
		m_colorLowDetailDefault = c;
	}
	
	public Color selectedColorLowDetail(int node) {
		final Object o = m_selectedColorsLowDetail.get(node);

		if ((o == null) || (o == m_deletedEntry))
			if ( m_selectedColorLowDetailDefault == null )
				return (Color) DNodeView.DEFAULT_NODE_SELECTED_PAINT;
			else
				return m_selectedColorLowDetailDefault;

		return (Color) o;
	}

	void setSelectedColorLowDetailDefault(Color c) {
		m_selectedColorLowDetailDefault = c;
	}
	
	
	public Paint selectedPaint(int node) {
		final Paint o = m_selectedPaints.get(node);

		if (o == null)
			if ( m_selectedPaintDefault == null ) 
				return DNodeView.DEFAULT_NODE_SELECTED_PAINT;
			else
				return m_selectedPaintDefault;

		return o;
	}

	void setSelectedPaintDefault(Paint c) {
		m_selectedPaintDefault = c;
	}


	@Override
	public byte shape(final int node) {
		final Byte shape = m_shapes.get(node);

		if (shape == null)
			if ( m_shapeDefault == null )
				return super.shape(node);
			else 
				return m_shapeDefault.getNativeShape();

		return shape;
	}

	void setShapeDefault(DNodeShape shape) {
		m_shapeDefault = shape;
	}

	/*
	 * The shape argument must be pre-checked for correctness.
	 * A negative shape value has the special meaning to remove overridden shape.
	 */
	void overrideShape(int node, DNodeShape shape) {
		m_shapes.put(node, shape.getNativeShape());
		isCleared = false;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Note: this will be used for BOTH unselected and selected.
	 * 
	 */
	public Paint unselectedPaint(final int node) {
		final Paint o = m_unselectedPaints.get(node);

		if (o == null)
			if ( m_unselectedPaintDefault == null ) 
				return super.fillPaint(node);
			else
				return m_unselectedPaintDefault;

		return o;
	}

	void setUnselectedPaintDefault(Paint p) {
		m_unselectedPaintDefault = p;
	}

	
	void setUnselectedPaint(final int node, final Paint paint) {
		m_unselectedPaints.put(node, paint);
		if(paint instanceof Color)
			m_colorsLowDetail.put(node, paint);
		isCleared = false;
	}
	
	void setSelectedPaint(final int node, final Paint paint) {
		m_selectedPaints.put(node, paint);
		if(paint instanceof Color)
			m_selectedColorsLowDetail.put(node, paint);

		isCleared = false;
	}
	
	@Override
	public Paint fillPaint(final int node) {
		boolean isSelected = selected.contains(node);
		
		if(isSelected)
			return selectedPaint(node);
		else
			return unselectedPaint(node);
	}
	
	void select(final int node) {
		selected.add(node);
	}
	
	void unselect(final int node) {
		selected.remove(node);
	}

	
	@Override
	public float borderWidth(final int node) {
		final Float o = m_borderWidths.get(node);

		if (o == null)
			if ( m_borderWidthDefault == null )
				return super.borderWidth(node);
			else 
				return m_borderWidthDefault.floatValue();

		return o;
	}

	void setBorderWidthDefault(float width) {
		m_borderWidthDefault = Float.valueOf(width);
	}

	/*
	 * A negative width value has the special meaning to remove overridden width.
	 */
	void overrideBorderWidth(final int node, final float width) {
		if ((width < 0.0f) || (width == super.borderWidth(node)))
			m_borderWidths.remove(node);
		else {
			m_borderWidths.put(node, width);
			isCleared = false;
		}
	}

	@Override
	public Paint borderPaint(final int node) {
		final Paint o = m_borderPaints.get(node);

		if (o == null)
			if ( m_borderPaintDefault == null ) 
				return super.borderPaint(node);
			else
				return m_borderPaintDefault;

		return o;
	}

	void setBorderPaintDefault(Paint p) {
		m_borderPaintDefault = p;
	}

	/*
	 * A null paint has the special meaning to remove overridden paint.
	 */
	void overrideBorderPaint(final int node, final Paint paint) {
		if ((paint == null) || paint.equals(super.borderPaint(node)))
			m_borderPaints.remove(node);
		else {
			m_borderPaints.put(node, paint);
			isCleared = false;
		}
	}

	@Override
	public int labelCount(final int node) {
		final Integer o = m_labelCounts.get(node);

		if (o == null)
			if ( m_labelCountDefault == null ) 
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
	void overrideLabelCount(final int node, final int labelCount) {
		if ((labelCount < 0) || (labelCount == super.labelCount(node)))
			m_labelCounts.remove(node);
		else {
			m_labelCounts.put(node, labelCount);
			isCleared = false;
		}
	}

	@Override
	public String labelText(final int node, final int labelInx) {
		final long key = (((long) node) << 32) | ((long) labelInx);
		final String o = m_labelTexts.get(key);

		if (o == null)
			if ( m_labelTextDefault == null )
				return super.labelText(node, labelInx);
			else
				return m_labelTextDefault;

		return o;
	}

	void setLabelTextDefault(String text) {
		m_labelTextDefault = text;
	}

	/*
	 * A null text has the special meaning to remove overridden text.
	 */
	void overrideLabelText(final int node, final int labelInx, final String text) {
		final long key = (((long) node) << 32) | ((long) labelInx);

		if ((text == null) || text.equals(super.labelText(node, labelInx)))
			m_labelTexts.remove(key);
		else {
			m_labelTexts.put(key, text);
			isCleared = false;
		}
	}
	
	
	public String tooltipText(final int node) {
		final String o = m_tooltipTexts.get(node);

		if (o == null)
			if ( m_tooltipTextDefault == null )
				return "";
			else
				return m_tooltipTextDefault;

		return o;
	}

	void setTooltipTextDefault(String tooltip) {
		m_tooltipTextDefault = tooltip;
	}

	/*
	 * A null text has the special meaning to remove overridden text.
	 */
	void overrideTooltipText(final int node, final String text) {

		if ((text == null) || text.equals(""))
			m_tooltipTexts.remove(node);
		else {
			m_tooltipTexts.put(node, text);
			isCleared = false;
		}
	}
	
	

	@Override
	public Font labelFont(int node, int labelInx) {
		final long key = (((long) node) << 32) | ((long) labelInx);
		final Font o = m_labelFonts.get(key);

		if (o == null)
			if ( m_labelFontDefault == null )
				return super.labelFont(node, labelInx);
			else
				return m_labelFontDefault;

		return o;
	}

	void setLabelFontDefault(Font f) {
		m_labelFontDefault = f;
	}

	/*
	 * A null font has the special meaning to remove overridden font.
	 */
	void overrideLabelFont(int node, int labelInx, final Font font) {
		final long key = (((long) node) << 32) | ((long) labelInx);

		if ((font == null) || font.equals(super.labelFont(node, labelInx)))
			m_labelFonts.remove(key);
		else {
			m_labelFonts.put(key, font);
			isCleared = false;
		}
	}

	@Override
	public Paint labelPaint(int node, int labelInx) {
		final long key = (((long) node) << 32) | ((long) labelInx);
		final Object o = m_labelPaints.get(Long.valueOf(key));

		if (o == null)
			if ( m_labelPaintDefault == null )
				return super.labelPaint(node, labelInx);
			else 
				return m_labelPaintDefault;

		return (Paint) o;
	}

	void setLabelPaintDefault(Paint p) {
		m_labelPaintDefault = p;
	}

	/*
	 * A null paint has the special meaning to remove overridden paint.
	 */
	void overrideLabelPaint(int node, int labelInx, Paint paint) {
		final long key = (((long) node) << 32) | ((long) labelInx);

		if ((paint == null) || paint.equals(super.labelPaint(node, labelInx)))
			m_labelPaints.remove(Long.valueOf(key));
		else {
			m_labelPaints.put(Long.valueOf(key), paint);
			isCleared = false;
		}
	}


	@Override
	public int customGraphicCount(final int node) {
		final DNodeView dnv = (DNodeView) m_view.getDNodeView(node);	
		return dnv.getNumCustomGraphics();
	}


	/**
	 * Return value of this will be used actual rendering code.
	 * 
	 */
	@Override
	public Iterator<CustomGraphic> customGraphics (final int node) {
		final DNodeView dnv = (DNodeView) m_view.getDNodeView(node);
		return dnv.customGraphicIterator();
	}
	

//	void setCustomGraphicsDefault(final CustomGraphicsVisualProperty vp,
//			final CyCustomGraphics<CustomGraphic> customGraphics) {
//		
//		if(customGraphics == null || customGraphics == NullCustomGraphics.getNullObject()) {
//			// Setting NullCustomGraphics means remove current custom graphics.
//			defaultCustomGraphicsMap.remove(vp);
//		} else {
//			// Create new default values.  Need to apply positions, too.
//			defaultCustomGraphicsMap.put(vp, customGraphics);
//			
//			this.defaultCGList = new ArrayList<CustomGraphic>();
//			final Collection<CyCustomGraphics<CustomGraphic>> defCGList = defaultCustomGraphicsMap.values();
//			
//			for (final CyCustomGraphics<CustomGraphic> val:defCGList) {
//				List<Layer<CustomGraphic>> layers = val.getLayers();
//				for(Layer<CustomGraphic> layer: layers)
//					defaultCGList.add(layer.getLayerObject());
//			}
//		}
//	}
//	
//	private CustomGraphicsVisualProperty getParentVP(final ObjectPositionVisualProperty vp) {
//		final VisualLexiconNode lexNode = m_view.dingLexicon.getVisualLexiconNode(vp);
//		final Collection<VisualLexiconNode> leavs = lexNode.getParent().getChildren();
//
//		CustomGraphicsVisualProperty parent = null;
//		for (VisualLexiconNode vlNode : leavs) {
//			if (vlNode.getVisualProperty().getRange().getType().equals(CyCustomGraphics.class)) {
//				parent = (CustomGraphicsVisualProperty) vlNode.getVisualProperty();
//				break;
//			}
//		}
//
//		if (parent == null)
//			throw new NullPointerException("Associated Custom Graphics VP is missing for " + vp.getDisplayName());
//		
//		return parent;
//	}
//	
//	void setCustomGraphicsPositionDefault(final ObjectPositionVisualProperty vp, final ObjectPosition position) {
//		
//		System.out.println("#Setting called: " + vp.getDisplayName());
//		
//		if (position == null || position.equals(ObjectPositionImpl.DEFAULT_POSITION))
//			defaultCustomGraphicsPositionMap.remove(vp);
//		else {
//			defaultCustomGraphicsPositionMap.put(vp, position);
//			
//			final CustomGraphicsVisualProperty parent = getParentVP(vp);
//			final CyCustomGraphics<CustomGraphic> cg = defaultCustomGraphicsMap.get(parent);
//			if(cg == null)
//				return;
//			
//			System.out.println("#Setting new location for: " + parent.getDisplayName() + " -- " + vp.getDisplayName());
//
//			// This is the set of all Custom Graphics.
//			final Collection<CyCustomGraphics<CustomGraphic>> defCGList = defaultCustomGraphicsMap.values();
//			
//			for (final View<CyNode> dnv : m_view.getNodeViews()) {
//				final List<CustomGraphic> newCGList = new ArrayList<CustomGraphic>();
//				for (final Layer<CustomGraphic> layer : cg.getLayers()) {
//					final CustomGraphic newCG = moveCustomGraphicsToNewPosition(layer.getLayerObject(), position,
//							(DNodeView) dnv);
//					newCGList.add(newCG);
//				}
//
//				for (final CyCustomGraphics<CustomGraphic> val : defCGList) {
//					if (val != cg) {
//						List<Layer<CustomGraphic>> layers = val.getLayers();
//						for (Layer<CustomGraphic> layer : layers)
//							newCGList.add(layer.getLayerObject());
//					}
//				}
//				this.m_customGraphics.put(((DNodeView) dnv).m_inx, newCGList);
//			}
//		}
//	}
//	
//	private CustomGraphic moveCustomGraphicsToNewPosition(final CustomGraphic cg, final ObjectPosition newPosition, final DNodeView dnv) {
//		if (cg == null || newPosition == null)
//			throw new NullPointerException("CustomGraphic and Position cannot be null.");
//
//		// Create new graphics
//		final CustomGraphic newCg = CustomGraphicsPositionCalculator.transform(newPosition, dnv, cg);
//		return newCg;
//	}


	@Override
	public byte labelTextAnchor(final int node, final int labelInx) {
		final Integer p = m_labelTextAnchors.get(node);

		if (p == null)
			if ( m_labelTextAnchorDefault == null )
				return super.labelTextAnchor(node, labelInx);
			else 
				return m_labelTextAnchorDefault.byteValue();
		
		return convertG2ND(p);
	}

	void setLabelTextAnchorDefault(int anchor) {
		m_labelTextAnchorDefault = Byte.valueOf(convertG2ND(anchor));
	}

	void overrideLabelTextAnchor(final int node, final int inx, final int anchor) {
		if (convertG2ND(anchor) == super.labelTextAnchor(node, inx))
			m_labelTextAnchors.remove(Integer.valueOf(node));
		else {
			m_labelTextAnchors.put(Integer.valueOf(node), Integer.valueOf(anchor));
			isCleared = false;
		}
	}

	@Override
	public byte labelNodeAnchor(final int node, final int labelInx) {
		final Integer o = m_labelNodeAnchors.get(node);

		if (o == null)
			if ( m_labelNodeAnchorDefault == null )
				return super.labelNodeAnchor(node, labelInx);
			else 
				return m_labelNodeAnchorDefault.byteValue();

		return convertG2ND(o);
	}

	void setLabelNodeAnchorDefault(int anchor) {
		m_labelNodeAnchorDefault = Byte.valueOf(convertG2ND(anchor));
	}

	void overrideLabelNodeAnchor(final int node, final int inx, final int anchor) {
		if (convertG2ND(anchor) == super.labelNodeAnchor(node, inx))
			m_labelNodeAnchors.remove(Integer.valueOf(node));
		else {
			m_labelNodeAnchors.put(Integer.valueOf(node), Integer.valueOf(anchor));
			isCleared = false;
		}
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param node DOCUMENT ME!
	 * @param labelInx DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public float labelOffsetVectorX(final int node, final int labelInx) {
		final Object o = m_labelOffsetXs.get(Integer.valueOf(node));

		if (o == null)
			if ( m_labelOffsetVectorXDefault == null )
				return super.labelOffsetVectorX(node, labelInx);
			else
				return m_labelOffsetVectorXDefault.floatValue();

		return ((Double) o).floatValue();
	}

	void setLabelOffsetVectorXDefault(double x) {
		m_labelOffsetVectorXDefault = Double.valueOf(x);
	}

	void overrideLabelOffsetVectorX(final int node, final int inx, final double x) {
		if (((float) x) == super.labelOffsetVectorX(node, inx))
			m_labelOffsetXs.remove(Integer.valueOf(node));
		else {
			m_labelOffsetXs.put(Integer.valueOf(node), new Double(x));
			isCleared = false;
		}
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param node DOCUMENT ME!
	 * @param labelInx DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public float labelOffsetVectorY(final int node, final int labelInx) {
		final Object o = m_labelOffsetYs.get(Integer.valueOf(node));

		if (o == null)
			if ( m_labelOffsetVectorYDefault == null )
				return super.labelOffsetVectorY(node, labelInx);
			else
				return m_labelOffsetVectorYDefault.floatValue();

		return ((Double) o).floatValue();
	}

	void setLabelOffsetVectorYDefault(double y) {
		m_labelOffsetVectorYDefault = Double.valueOf(y);
	}

	void overrideLabelOffsetVectorY(final int node, final int inx, final double y) {
		if (((float) y) == super.labelOffsetVectorY(node, inx))
			m_labelOffsetYs.remove(Integer.valueOf(node));
		else {
			m_labelOffsetYs.put(Integer.valueOf(node), new Double(y));
			isCleared = false;
		}
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param node DOCUMENT ME!
	 * @param labelInx DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public byte labelJustify(final int node, final int labelInx) {
		Integer o = m_labelJustifys.get(node);

		if (o == null)
			if ( m_labelJustifyDefault == null )
				return super.labelJustify(node, labelInx);
			else
				return m_labelJustifyDefault.byteValue();

		return convertG2ND(o);
	}

	void setLabelJustifyDefault(int justify) {
		m_labelJustifyDefault = Byte.valueOf(convertG2ND(justify));
	}

	void overrideLabelJustify(final int node, final int inx, final int justify) {
		if (convertG2ND(justify) == super.labelJustify(node, inx))
			m_labelJustifys.remove(Integer.valueOf(node));
		else {
			m_labelJustifys.put(Integer.valueOf(node), Integer.valueOf(justify));
			isCleared = false;
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param node DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public double labelWidth(int node) {
		final Double o = m_labelWidths.get(node);

		if (o == null)
			if ( m_labelWidthDefault == null )
				return super.labelWidth(node);
			else
				return m_labelWidthDefault.doubleValue();

		return o;
	}

	void setLabelWidthDefault(double width) {
		m_labelWidthDefault = width;
	}
	
	/*
	 * A negative width value has the special meaning to remove overridden width.
	 */
	void overrideLabelWidth(final int node, final double width) {
		if ((width < 0.0) || (width == super.labelWidth(node)))
			m_labelWidths.remove(node);
		else {
			m_labelWidths.put(node, width);
			isCleared = false;
		}
	}

	@Override
	public TexturePaint getNestedNetworkTexturePaint(final int node) {
		final DNodeView dNodeView = (DNodeView) m_view.getDNodeView(node);
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
}
