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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.GraphView;
import org.cytoscape.ding.Label;
import org.cytoscape.ding.NodeView;
import org.cytoscape.ding.impl.visualproperty.CustomGraphicsVisualProperty;
import org.cytoscape.ding.impl.visualproperty.ObjectPositionVisualProperty;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.graph.render.stateful.CustomGraphicsInfo;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.presentation.property.values.ObjectPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DING implementation of View Model and Presentation.
 */
public class DNodeView extends AbstractDViewModel<CyNode> implements NodeView, Label {
	
	private static Pattern CG_SIZE_PATTERN = Pattern.compile("NODE_CUSTOMGRAPHICS_SIZE_[1-9]");
	
	// Affects size of the nested network image relative to the node size:
	private static final float NESTED_IMAGE_SCALE_FACTOR = 0.6f;

	// These images will be used when a view is not available for a nested
	// network.
	private static BufferedImage DEFAULT_NESTED_NETWORK_IMAGE;
	private static BufferedImage RECURSIVE_NESTED_NETWORK_IMAGE;

	// Used to detect recursive rendering of nested networks.
	private static int nestedNetworkPaintingDepth = 0;

	static {
		// Initialize image icons for nested networks
		try {
			DEFAULT_NESTED_NETWORK_IMAGE = ImageIO.read(DNodeView.class.getClassLoader().getResource(
					"images/default_network.png"));
			RECURSIVE_NESTED_NETWORK_IMAGE = ImageIO.read(DNodeView.class.getClassLoader().getResource(
					"images/recursive_network.png"));
		} catch (final IOException e) {
			e.printStackTrace();
			DEFAULT_NESTED_NETWORK_IMAGE = null;
			RECURSIVE_NESTED_NETWORK_IMAGE = null;
		}
	}

	// Default size of node.
	static final float DEFAULT_WIDTH = 30.0f;
	static final float DEFAULT_HEIGHT = 30.0f;

	// Default node shape
	static final int DEFAULT_SHAPE = GraphGraphics.SHAPE_RECTANGLE;

	// Default border color
	static final Paint DEFAULT_BORDER_PAINT = Color.DARK_GRAY;
	static final Paint DEFAULT_NODE_PAINT = Color.BLUE;
	static final Paint DEFAULT_NODE_SELECTED_PAINT = Color.YELLOW;

	static final String DEFAULT_LABEL_TEXT = "";
	static final Font DEFAULT_LABEL_FONT = new Font("SansSerif", Font.PLAIN, 12);
	static final Paint DEFAULT_LABEL_PAINT = Color.DARK_GRAY;
	static final double DEFAULT_LABEL_WIDTH = 100.0;

	// Default opacity
	static final int DEFAULT_TRANSPARENCY = 255;

	// Parent network view
	private final DGraphView graphView;

	private boolean selected;

	private final long modelIdx;
	
	/**
	 * Stores the position of a nodeView when it's hidden so that when the
	 * nodeView is restored we can restore the view into the same position.
	 */
	float m_hiddenXMin = Float.MIN_VALUE;
	float m_hiddenYMin = Float.MIN_VALUE;
	float m_hiddenXMax = Float.MAX_VALUE;
	float m_hiddenYMax = Float.MAX_VALUE;

	// Nested Network View
	private DGraphView nestedNetworkView;

	// Show/hide flag for nested network graphics
	private boolean nestedNetworkVisible = true;

	// CG_LOCK is used for synchronizing custom graphics operations on this DNodeView.
	// Arrays are objects like any other and can be used for synchronization.
	// We use an array object assuming it takes up the least amount of memory:
	private final Object[] CG_LOCK = new Object[0];
	
	private final Map<VisualProperty<CyCustomGraphics>, CustomGraphicsInfo> cgInfoMap;

	// Label position
	private ObjectPosition labelPosition;

	// Cached extent information
	float m_xMin = Float.MIN_VALUE;
	float m_yMin = Float.MIN_VALUE;
	float m_xMax = Float.MAX_VALUE;
	float m_yMax = Float.MAX_VALUE;

	// Cached node depth.
	double m_zOrder = 0.0f;

	// Cached visibility information
	private boolean isVisible = true;
	
	private static final Logger logger = LoggerFactory.getLogger(DNodeView.class);
	
	DNodeView(final VisualLexicon lexicon,
			  final DGraphView graphView,
			  final CyNode model,
			  final CyServiceRegistrar serviceRegistrar) {
		super(model, lexicon, serviceRegistrar);
		
		if (graphView == null)
			throw new NullPointerException("View must never be null.");
		if (lexicon == null)
			throw new NullPointerException("Lexicon must never be null.");

		this.labelPosition = new ObjectPosition();
		this.modelIdx = model.getSUID();
		this.graphView = graphView;
		
		this.cgInfoMap = new TreeMap<>(new Comparator<VisualProperty<CyCustomGraphics>>() {
			@Override
			public int compare(VisualProperty<CyCustomGraphics> vp1, VisualProperty<CyCustomGraphics> vp2) {
				// Sort by: Custom Graphics 1, 2, 3, etc.
				return vp1.getIdString().compareTo(vp2.getIdString());
			}
		});
	}
	
	@Override
	public GraphView getGraphView() {
		return graphView;
	}
	
	@Override
	public CyNode getCyNode() {
		return model;
	}

	@Override
	public void setSelectedPaint(final Paint paint) {
		synchronized (graphView.m_lock) {
			if (paint == null)
				throw new NullPointerException("paint is null");

			final Paint transpColor = getTransparentColor(paint, graphView.m_nodeDetails.getTransparency(model));
			graphView.m_nodeDetails.setSelectedPaint(model, transpColor);
			
			if (isSelected())
				graphView.setContentChanged();
		}
	}

	@Override
	public void setUnselectedPaint(final Paint paint) {
		synchronized (graphView.m_lock) {
			if (paint == null)
				throw new NullPointerException("paint is null");

			final Paint transpColor = getTransparentColor(paint, graphView.m_nodeDetails.getTransparency(model));
			graphView.m_nodeDetails.setUnselectedPaint(model, transpColor);
			
			if (!isSelected())
				graphView.setContentChanged();
		}
	}

	@Override
	public void setBorderPaint(Paint paint) {
		synchronized (graphView.m_lock) {
			final Paint transColor = getTransparentColor(paint, graphView.m_nodeDetails.getBorderTransparency(model));
			graphView.m_nodeDetails.overrideBorderPaint(model, transColor);
			fixBorder();
			graphView.setContentChanged();
		}
	}

	@Override
	public void setBorderWidth(Float width) {
		synchronized (graphView.m_lock) {
			graphView.m_nodeDetails.overrideBorderWidth(model, width);
			graphView.setContentChanged();
		}
	}

	@Override
	public void setBorder(final Stroke stroke) {
		synchronized (graphView.m_lock) {
			graphView.m_nodeDetails.overrideBorderStroke(model, stroke);
			graphView.setContentChanged();
		}
	}

	private float m_borderDash = 0.0f;
	private float m_borderDash2 = 0.0f;
	private final static Color s_transparent = new Color(0, 0, 0, 0);

	// Callers of this method must be holding m_view.m_lock.
	private void fixBorder() {
		final Paint borderPaint = graphView.m_nodeDetails.getBorderPaint(model);
		if ((m_borderDash == 0.0f) && (m_borderDash2 == 0.0f))
			graphView.m_nodeDetails.overrideBorderPaint(model, borderPaint);
		else {
			final int size = (int) Math.max(1.0f, (int) (m_borderDash + m_borderDash2)); // Average
			// times
			// two.

			if ((size == graphView.m_lastSize) && (borderPaint == graphView.m_lastPaint)) {
				/* Use the cached texture paint. */} else {
				final BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
				final Graphics2D g2 = (Graphics2D) img.getGraphics();
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
				g2.setPaint(s_transparent);
				g2.fillRect(0, 0, size, size);
				g2.setPaint(borderPaint);
				g2.fillRect(0, 0, size / 2, size / 2);
				g2.fillRect(size / 2, size / 2, size / 2, size / 2);
				graphView.m_lastTexturePaint = new TexturePaint(img, new Rectangle2D.Double(0, 0, size, size));
				graphView.m_lastSize = size;
				graphView.m_lastPaint = borderPaint;
			}

			graphView.m_nodeDetails.overrideBorderPaint(model, graphView.m_lastTexturePaint);
		}
	}

	@Override
	public void setTransparency(int trans) {
		synchronized (graphView.m_lock) {
			Integer transparency;
			if (trans < 0 || trans > 255) {
				// If out of range, use default value.
				transparency = BasicVisualLexicon.NODE_TRANSPARENCY.getDefault();
			} else {
				transparency = trans;
			}

			graphView.m_nodeDetails.overrideTransparency(model, transparency);
			
			setUnselectedPaint(graphView.m_nodeDetails.getUnselectedPaint(model));
			setSelectedPaint(graphView.m_nodeDetails.getSelectedPaint(model));
			
			graphView.setContentChanged();
		}
	}
	
	public void setBorderTransparency(final int trans) {
		synchronized (graphView.m_lock) {
			final Integer transparency;
			if (trans < 0 || trans > 255) {
				// If out of range, use default value.
				transparency = BasicVisualLexicon.NODE_BORDER_TRANSPARENCY.getDefault();
			} else {
				transparency = trans;
			}

			graphView.m_nodeDetails.overrideBorderTransparency(model, transparency);
			
			final Color currentBorderPaint = ((Color) graphView.m_nodeDetails.getBorderPaint(model));
			setBorderPaint(currentBorderPaint);
			
			graphView.setContentChanged();
		}
	}

	@Override
	public boolean setWidth(final double originalWidth) {
		double newWidth = originalWidth;

		// Check bypass
		if (isValueLocked(DVisualLexicon.NODE_WIDTH))
			newWidth = getVisualProperty(DVisualLexicon.NODE_WIDTH);

		final double width = getWidth();
		
		if (newWidth == width) return false;
		
		synchronized (graphView.m_lock) {
			if (!graphView.m_spacial.exists(modelIdx, graphView.m_extentsBuff, 0)) {
				isVisible = false;
				return false;
			}
			isVisible = true;

			final double xCenter = (graphView.m_extentsBuff[0] + graphView.m_extentsBuff[2]) / 2.0d;
			final double wDiv2 = newWidth / 2.0d;
			final float xMin = (float) (xCenter - wDiv2);
			final float xMax = (float) (xCenter + wDiv2);

			if (!(xMax > xMin))
				throw new IllegalArgumentException("width is too small");

			graphView.m_spacial.delete(modelIdx);
			graphView.m_spacial.insert(modelIdx, xMin, graphView.m_extentsBuff[1], xMax,
					graphView.m_extentsBuff[3], m_zOrder);
			graphView.setContentChanged();

			m_xMin = xMin;
			m_yMin = graphView.m_extentsBuff[1];
			m_xMax = xMax;
			m_yMax = graphView.m_extentsBuff[3];

			return true;
		}
	}

	@Override
	public double getWidth() {
		if (!isVisible || m_xMax == Float.MAX_VALUE)
			return -1.0d;

		return (double)(m_xMax - m_xMin);
		/*
		synchronized (graphView.m_lock) {
			if (!graphView.m_spacial.exists(modelIdx, graphView.m_extentsBuff, 0))
				return -1.0d;

			return ((double) graphView.m_extentsBuff[2]) - graphView.m_extentsBuff[0];
		}
		*/
	}

	@Override
	public boolean setHeight(final double originalHeight) {
		double newHeight = originalHeight;
		
		// Check bypass
		if (isValueLocked(DVisualLexicon.NODE_HEIGHT))
			newHeight = getVisualProperty(DVisualLexicon.NODE_HEIGHT);

		final double height = getHeight();
		
		if (newHeight == height) return false;
		
		synchronized (graphView.m_lock) {
			if (!graphView.m_spacial.exists(modelIdx, graphView.m_extentsBuff, 0)) {
				isVisible = false;
				return false;
			}
			isVisible = true;

			final double yCenter = ((graphView.m_extentsBuff[1]) + graphView.m_extentsBuff[3]) / 2.0d;
			final double hDiv2 = newHeight / 2.0d;
			final float yMin = (float) (yCenter - hDiv2);
			final float yMax = (float) (yCenter + hDiv2);

			if (!(yMax > yMin))
				throw new IllegalArgumentException("height is too small max:" + yMax + " min:" + yMin + " center:"
						+ yCenter + " height:" + newHeight);

			graphView.m_spacial.delete(modelIdx);
			graphView.m_spacial.insert(modelIdx, graphView.m_extentsBuff[0], yMin, graphView.m_extentsBuff[2], yMax, m_zOrder);
			graphView.setContentChanged();

			m_xMin = graphView.m_extentsBuff[0];
			m_yMin = yMin;
			m_xMax = graphView.m_extentsBuff[2];
			m_yMax = yMax;

			return true;
		}
	}

	@Override
	public double getHeight() {
		if (!isVisible || m_yMax == Float.MAX_VALUE)
			return -1.0d;
		else
			return (double)(m_yMax - m_yMin);
		/*
		synchronized (graphView.m_lock) {
			if (!graphView.m_spacial.exists(modelIdx, graphView.m_extentsBuff, 0))
				return -1.0d;

			return ((double) graphView.m_extentsBuff[3]) - graphView.m_extentsBuff[1];
		}
		*/
	}

	@Override
	public void setOffset(double x, double y) {
		synchronized (graphView.m_lock) {
			if (!graphView.m_spacial.exists(modelIdx, graphView.m_extentsBuff, 0))
				return;

			final double wDiv2 = (((double) graphView.m_extentsBuff[2]) - graphView.m_extentsBuff[0]) / 2.0d;
			final double hDiv2 = (((double) graphView.m_extentsBuff[3]) - graphView.m_extentsBuff[1]) / 2.0d;
			final float xMin = (float) (x - wDiv2);
			final float xMax = (float) (x + wDiv2);
			final float yMin = (float) (y - hDiv2);
			final float yMax = (float) (y + hDiv2);

			if (!(xMax > xMin))
				throw new IllegalStateException("width of node has degenerated to zero after " + "rounding");

			if (!(yMax > yMin))
				throw new IllegalStateException("height of node has degenerated to zero after " + "rounding");

			graphView.m_spacial.delete(modelIdx);
			graphView.m_spacial.insert(modelIdx, xMin, yMin, xMax, yMax, m_zOrder);
			graphView.setContentChanged();
			setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION,x);
			setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION,y);
		}
	}

	@Override
	public Point2D getOffset() {
		synchronized (graphView.m_lock) {
			if (!graphView.m_spacial.exists(modelIdx, graphView.m_extentsBuff, 0))
				return null;

			final double xCenter = (((double) graphView.m_extentsBuff[0]) + graphView.m_extentsBuff[2]) / 2.0d;
			final double yCenter = (((double) graphView.m_extentsBuff[1]) + graphView.m_extentsBuff[3]) / 2.0d;

			return new Point2D.Double(xCenter, yCenter);
		}
	}

	@Override
	public void setXPosition(final double xPos) {

		synchronized (graphView.m_lock) {
			double wDiv2;
			isVisible = graphView.m_spacial.exists(modelIdx, graphView.m_extentsBuff, 0);

			if (isVisible)
				wDiv2 = (graphView.m_extentsBuff[2] - graphView.m_extentsBuff[0]) / 2.0d;
			else
				wDiv2 = 	(m_hiddenXMax - m_hiddenXMin) / 2.0d;

			if (wDiv2 <= 0)
				wDiv2 = 0.00001; // Some epsilon value

			final float xMin = (float) (xPos - wDiv2);
			final float xMax = (float) (xPos + wDiv2);

			// Rather than throw an exception, we fix things up with an epsilon
			// if (!(xMax > xMin)) {
				// throw new IllegalStateException("width of node has degenerated to zero after rounding");
			// }

			// If the node is visible, set the extents.
			if (isVisible) {
				graphView.m_spacial.delete(modelIdx);
				graphView.m_spacial.insert(modelIdx, xMin, graphView.m_extentsBuff[1], xMax, graphView.m_extentsBuff[3], m_zOrder);
				graphView.setContentChanged();
				m_xMin = xMin;
				m_yMin = graphView.m_extentsBuff[1];
				m_xMax = xMax;
				m_yMax = graphView.m_extentsBuff[3];

				// If the node is NOT visible (hidden), then update the hidden
				// extents. Doing
				// this will mean that the node view will be properly scaled and
				// rotated
				// relative to the other nodes.
			} else {
				m_hiddenXMax = xMax;
				m_hiddenXMin = xMin;
			}
		}
	}

	@Override
	public double getXPosition() {
		if (isVisible)
			return (m_xMin + m_xMax) / 2.0;
		else
			return (double) (m_hiddenXMin + m_hiddenXMax) / 2.0;

		/*
		synchronized (graphView.m_lock) {
			if (graphView.m_spacial.exists(modelIdx, graphView.m_extentsBuff, 0))
				return (((double) graphView.m_extentsBuff[0]) + graphView.m_extentsBuff[2]) / 2.0d;
			else
				return (double) (m_hiddenXMin + m_hiddenXMax) / 2.0;
		}
		*/
	}

	public void setZPosition(final double z) {
		if (m_zOrder == z)
			return;

		m_zOrder = z;
		if (m_zOrder != 0.0)
			graphView.setHaveZOrder(true);

		synchronized (graphView.m_lock) {
			graphView.m_spacial.setZOrder(modelIdx, m_zOrder);
		}
	}

	public double getZPosition() {
		return (double)m_zOrder;
	}

	@Override
	public void setYPosition(final double yPos) {
		
		synchronized (graphView.m_lock) {
			double hDiv2;

			isVisible = graphView.m_spacial.exists(modelIdx, graphView.m_extentsBuff, 0);

			if (isVisible)
				hDiv2 = ((graphView.m_extentsBuff[3]) - graphView.m_extentsBuff[1]) / 2.0d;
			else
				hDiv2 = (m_hiddenYMax - m_hiddenYMin) / 2.0d;

			if (hDiv2 <= 0)
				hDiv2 = 0.00001; // Some epsilon value

			final float yMin = (float) (yPos - hDiv2);
			final float yMax = (float) (yPos + hDiv2);

			// if (!(yMax > yMin))
			// 	throw new IllegalStateException("height of node has degenerated to zero after " + "rounding");

			// If the node is visible, set the extents.
			if (isVisible) {
				graphView.m_spacial.delete(modelIdx);
				graphView.m_spacial.insert(modelIdx, graphView.m_extentsBuff[0], yMin, graphView.m_extentsBuff[2], yMax, m_zOrder);
				graphView.setContentChanged();

				m_xMin = graphView.m_extentsBuff[0];
				m_yMin = yMin;
				m_xMax = graphView.m_extentsBuff[2];
				m_yMax = yMax;

				// If the node is NOT visible (hidden), then update the hidden
				// extents. Doing this will mean that the node view will be properly scaled and
				// rotated relative to the other nodes.
			} else {
				m_hiddenYMax = yMax;
				m_hiddenYMin = yMin;
			}
		}
	}

	@Override
	public double getYPosition() {
		if (isVisible)
			return (m_yMax + m_yMin) / 2.0d;
		else
			return ((m_hiddenYMin + m_hiddenYMax)) / 2.0d;
		/*
		synchronized (graphView.m_lock) {
			if (graphView.m_spacial.exists(modelIdx, graphView.m_extentsBuff, 0))
				return ((graphView.m_extentsBuff[1]) + graphView.m_extentsBuff[3]) / 2.0d;
			else
				return ((m_hiddenYMin + m_hiddenYMax)) / 2.0d;
		}
		*/
	}

	@Override
	public void select() {
		final boolean somethingChanged;

		synchronized (graphView.m_lock) {
			somethingChanged = selectInternal();

			// if (somethingChanged)
			// 	graphView.m_contentChanged = true;
		}
	}

	/**
	 * Should synchronize around m_view.m_lock.
	 * 
	 * @return true if selected.
	 */
	boolean selectInternal() {
		// Thread.dumpStack();
		if (selected)
			return false;

		selected = true;
		graphView.m_nodeDetails.select(model);
		graphView.m_selectedNodes.insert(modelIdx);

		return true;
	}

	@Override
	public void unselect() {
		final boolean somethingChanged;

		synchronized (graphView.m_lock) {
			somethingChanged = unselectInternal();

			// if (somethingChanged)
			// 	graphView.m_contentChanged = true;
		}
	}

	// Should synchronize around m_view.m_lock.
	boolean unselectInternal() {
		if (!selected)
			return false;

		selected = false;
		graphView.m_nodeDetails.unselect(model);
		graphView.m_selectedNodes.delete(modelIdx);

		return true;
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	@Override
	public boolean setSelected(final boolean selected) {
		if (selected)
			select();
		else
			unselect();

		return true;
	}

	@Override
	public boolean isHidden() {
		return graphView.isHidden(this);
	}
	
	@Override
	public void setShape(final NodeShape shape) {
		synchronized (graphView.m_lock) {
			graphView.m_nodeDetails.overrideShape(model, shape);
			graphView.setContentChanged();
		}
	}

	@Override
	public void setToolTip(final String tip) {
		synchronized (graphView.m_lock) {
			graphView.m_nodeDetails.overrideTooltipText(model, tip);
			graphView.setContentChanged();
		}
	}

	@Override
	public String getToolTip() {
		synchronized (graphView.m_lock) {
			return graphView.m_nodeDetails.getTooltipText(model);
		}
	}

	@Override
	public Paint getTextPaint() {
		synchronized (graphView.m_lock) {
			return graphView.m_nodeDetails.getLabelPaint(model, 0);
		}
	}

	@Override
	public void setTextPaint(Paint textPaint) {
		synchronized (graphView.m_lock) {
			
			if(textPaint != null) {
				final Paint transparentColor = getTransparentColor(textPaint, graphView.m_nodeDetails.getLabelTransparency(model));
				graphView.m_nodeDetails.overrideLabelPaint(model, 0, transparentColor);
				graphView.setContentChanged();
			}
		}
	}
	
	public void setLabelTransparency(Integer trans) {
		synchronized (graphView.m_lock) {
			Integer transparency;
			if (trans < 0 || trans > 255) {
				// If out of range, use default value.
				transparency = BasicVisualLexicon.NODE_LABEL_TRANSPARENCY.getDefault();
			} else {
				transparency = trans;
			}
			
			graphView.m_nodeDetails.overrideLabelTransparency(model, transparency);
			setTextPaint(graphView.m_nodeDetails.getLabelPaint(model, 0));
			graphView.setContentChanged();
		}
	}

	@Override
	public String getText() {
		synchronized (graphView.m_lock) {
			return graphView.m_nodeDetails.getLabelText(model, 0);
		}
	}

	@Override
	public void setText(String text) {
		synchronized (graphView.m_lock) {
			graphView.m_nodeDetails.overrideLabelText(model, 0, text);

			if (DEFAULT_LABEL_TEXT.equals(graphView.m_nodeDetails.getLabelText(model, 0)))
				graphView.m_nodeDetails.overrideLabelCount(model, 0);
			else
				graphView.m_nodeDetails.overrideLabelCount(model, 1);

			graphView.setContentChanged();
		}
	}

	/**
	 * Get label font type.
	 */
	@Override
	public Font getFont() {
		synchronized (graphView.m_lock) {
			return graphView.m_nodeDetails.getLabelFont(model, 0);
		}
	}

	/**
	 * Set label font type.
	 */
	@Override
	public void setFont(Font font) {
		synchronized (graphView.m_lock) {
			graphView.m_nodeDetails.overrideLabelFont(model, font);
			graphView.setContentChanged();
		}
	}

	private void ensureContentChanged() {
		synchronized (graphView.m_lock) {
			graphView.setContentChanged();
		}
	}

	/**
	 * Obtain the lock used for reading information about custom graphics. This
	 * is <EM>not</EM> needed for thread-safe custom graphic operations, but
	 * only needed for use with thread-compatible methods, such as
	 * customGraphicIterator(). For example, to iterate over all custom graphics
	 * without fear of the underlying custom graphics being mutated, you could
	 * perform:
	 * 
	 * <PRE>
	 *    DNodeView dnv = ...;
	 *    CustomGraphic cg = null;
	 *    synchronized (dnv.customGraphicLock()) {
	 *       Iterator<CustomGraphic> cgIt = dnv.customGraphicIterator();
	 *       while (cgIt.hasNext()) {
	 *          cg = cgIt.next();
	 *          // PERFORM your operations here.
	 *       }
	 *   }
	 * </PRE>
	 * 
	 * NOTE: A better concurrency approach would be to return the read lock from
	 * a java.util.concurrent.locks.ReentrantReadWriteLock. However, this
	 * requires users to manually lock and unlock blocks of code where many
	 * times try{} finally{} blocks are needed and if any mistake are made, a
	 * DNodeView may be permanently locked. Since concurrency will most likely
	 * be very low, we opt for the simpler approach of having users use
	 * synchronized {} blocks on a standard lock object.
	 * 
	 * @return the lock object used for custom graphics of this DNodeView.
	 */
	public Object customGraphicLock() {
		return CG_LOCK;
	}

	public double getLabelWidth() {
		synchronized (graphView.m_lock) {
			return graphView.m_nodeDetails.getLabelWidth(model);
		}
	}

	public void setLabelWidth(double width) {
		synchronized (graphView.m_lock) {
			graphView.m_nodeDetails.overrideLabelWidth(model, width);
			graphView.setContentChanged();
		}
	}

	public void setNestedNetworkImgVisible(boolean visible) {
		synchronized (graphView.m_lock) {
			graphView.m_nodeDetails.overrideNestedNetworkImgVisible(model, visible);
			graphView.setContentChanged();
		}
	}
	
	TexturePaint getNestedNetworkTexturePaint() {
		synchronized (graphView.m_lock) {
			++nestedNetworkPaintingDepth;

			try {
				if (nestedNetworkPaintingDepth > 1 || getModel().getNetworkPointer() == null || !nestedNetworkVisible)
					return null;

				final Boolean netImgVisible = graphView.m_nodeDetails.getNestedNetworkImgVisible(model);

				if (!Boolean.TRUE.equals(netImgVisible)) {
					return null;
				}

				final double IMAGE_WIDTH = getWidth() * NESTED_IMAGE_SCALE_FACTOR;
				final double IMAGE_HEIGHT = getHeight() * NESTED_IMAGE_SCALE_FACTOR;

				setNestedNetworkView();

				// Do we have a node w/ a self-reference?
				if (graphView == nestedNetworkView) {
					if (RECURSIVE_NESTED_NETWORK_IMAGE == null)
						return null;

					final Rectangle2D rect = new Rectangle2D.Double(-IMAGE_WIDTH / 2, -IMAGE_HEIGHT / 2, IMAGE_WIDTH,
							IMAGE_HEIGHT);
					return new TexturePaint(RECURSIVE_NESTED_NETWORK_IMAGE, rect);
				}
				
				if (nestedNetworkView != null) {					
					final double scaleFactor = graphView.getGraphLOD().getNestedNetworkImageScaleFactor();
					return nestedNetworkView.getSnapshot(IMAGE_WIDTH * scaleFactor, IMAGE_HEIGHT * scaleFactor);
				} else {
					if (DEFAULT_NESTED_NETWORK_IMAGE == null || !isVisible || getWidth() == -1 || getHeight() == -1)
						return null;

					final Rectangle2D rect = new Rectangle2D.Double(-IMAGE_WIDTH / 2, -IMAGE_HEIGHT / 2, IMAGE_WIDTH,
							IMAGE_HEIGHT);
					return new TexturePaint(DEFAULT_NESTED_NETWORK_IMAGE, rect);
				}
			} finally {
				--nestedNetworkPaintingDepth;
			}
		}
	}

	public void setNestedNetworkView() {
		if (this.getModel().getNetworkPointer() == null) {
			this.nestedNetworkView = null;
		} else {
			final CyNetworkViewManager netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
			final Iterator<CyNetworkView> viewIterator = netViewMgr.getNetworkViews(this.getModel().getNetworkPointer())
					.iterator();
			
			if (viewIterator.hasNext())
				this.nestedNetworkView = (DGraphView) viewIterator.next();
			else
				this.nestedNetworkView = null;
		}
	}

	/**
	 * Determines whether a nested network should be rendered as part of a
	 * node's view or not.
	 * 
	 * @return true if the node has a nested network and we want it rendered,
	 *         else false.
	 */
	public boolean nestedNetworkIsVisible() {
		return nestedNetworkVisible;
	}

	/**
	 * Set the visibility of a node's nested network when rendered.
	 * 
	 * @param makeVisible
	 *            forces the visibility of a nested network. Please note that
	 *            this call has no effect if a node has no associated nested
	 *            network!
	 */
	public void showNestedNetwork(final boolean makeVisible) {
		nestedNetworkVisible = makeVisible;
	}


	// from Label interface
	@Override
	public void setLabelPosition(final ObjectPosition p) {
		this.labelPosition = p;
		updateLabelPosition();
	}

	private void updateLabelPosition() {
		synchronized (graphView.m_lock) {
			graphView.m_nodeDetails
					.overrideLabelTextAnchor(model, 0, labelPosition.getAnchor());
			graphView.m_nodeDetails.overrideLabelNodeAnchor(model, 0, labelPosition.getTargetAnchor());
			graphView.m_nodeDetails.overrideLabelJustify(model, 0, labelPosition.getJustify());
			graphView.m_nodeDetails.overrideLabelOffsetVectorX(model, 0, labelPosition.getOffsetX());
			graphView.m_nodeDetails.overrideLabelOffsetVectorY(model, 0, labelPosition.getOffsetY());

			graphView.setContentChanged();
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void clearValueLock(final VisualProperty<?> vp) {
		final boolean isDefault;
		synchronized (graphView.m_lock) {
			isDefault = !visualProperties.containsKey(vp);
		}
		super.clearValueLock(vp);
		
		// Reset to the visual style default if visualProperties map doesn't contain this vp
		if (isDefault) {
			if (vp == BasicVisualLexicon.NODE_VISIBLE) // TODO: what if the default value of the visual style is different (e.g. invisible)?
				applyVisualProperty((VisualProperty) vp, vp.getDefault());
			else
				graphView.nodeViewDefaultSupport.setViewDefault((VisualProperty) vp,
						graphView.m_nodeDetails.getDefaultValue(vp));
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected <T, V extends T> void applyVisualProperty(final VisualProperty<? extends T> vpOriginal, V value) {
		VisualProperty<?> vp = vpOriginal;

		// Check to make sure our view hasn't gotten disconnected somewhere along the line
		if (graphView.getNodeView(this.getModel()) == null)
			return;
		
		// Null means set value to VP's default.
		if (value == null)
			value = (V) vp.getDefault();

		if (vp == DVisualLexicon.NODE_SHAPE) {
			setShape(((NodeShape) value));
		} else if (vp == DVisualLexicon.NODE_SELECTED_PAINT) {
			setSelectedPaint((Paint) value);
		} else if (vp == BasicVisualLexicon.NODE_SELECTED) {
			setSelected((Boolean) value);
		} else if (vp == BasicVisualLexicon.NODE_VISIBLE) {
			if (((Boolean) value).booleanValue()) {
				graphView.showGraphObject(this);
				isVisible = true;
			} else {
				graphView.hideGraphObject(this);
				isVisible = false;
			}
		} else if (vp == BasicVisualLexicon.NODE_FILL_COLOR) {
			setUnselectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.NODE_BORDER_PAINT) {
			setBorderPaint((Color)value);
		} else if (vp == DVisualLexicon.NODE_BORDER_TRANSPARENCY) {
			final Integer opacity = ((Number) value).intValue();
			setBorderTransparency(opacity);
		} else if (vp == DVisualLexicon.NODE_BORDER_WIDTH) {
			setBorderWidth(new Float(((Number) value).floatValue()));
		} else if (vp == DVisualLexicon.NODE_BORDER_LINE_TYPE) {
			final DLineType dLineType = DLineType.getDLineType((LineType) value);
			final float currentBorderWidth = graphView.m_nodeDetails.getBorderWidth(model);
			setBorder(dLineType.getStroke(currentBorderWidth));
		} else if (vp == DVisualLexicon.NODE_TRANSPARENCY) {
			setTransparency(((Number) value).intValue());
		} else if (vp == BasicVisualLexicon.NODE_WIDTH) {
			setWidth(((Number) value).doubleValue());
		} else if (vp == BasicVisualLexicon.NODE_HEIGHT) {
			setHeight(((Number) value).doubleValue());
		} else if (vp == BasicVisualLexicon.NODE_LABEL) {
			setText(value.toString());
		} else if (vp == BasicVisualLexicon.NODE_LABEL_WIDTH) {
			setLabelWidth(((Number) value).doubleValue());
		} else if (vp == BasicVisualLexicon.NODE_X_LOCATION) {
			setXPosition(((Number) value).doubleValue());
		} else if (vp == BasicVisualLexicon.NODE_Y_LOCATION) {
			setYPosition(((Number) value).doubleValue());
		} else if (vp == BasicVisualLexicon.NODE_Z_LOCATION) {
			setZPosition(((Number) value).doubleValue());
		} else if (vp == DVisualLexicon.NODE_TOOLTIP) {
			setToolTip(value.toString());
		} else if (vp == BasicVisualLexicon.NODE_LABEL_COLOR) {
			setTextPaint((Color)value);
		} else if (vp == BasicVisualLexicon.NODE_LABEL_TRANSPARENCY) {
			final int opacity = ((Number) value).intValue();
			setLabelTransparency(opacity);
		} else if (vp == DVisualLexicon.NODE_LABEL_FONT_FACE) {
			final float currentFontSize = graphView.m_nodeDetails.getLabelFont(model, 0).getSize();
			final Font newFont = ((Font) value).deriveFont(currentFontSize);
			setFont(newFont);
		} else if (vp == DVisualLexicon.NODE_LABEL_FONT_SIZE) {
			final float newSize = ((Number) value).floatValue();
			final Font newFont = graphView.m_nodeDetails.getLabelFont(model, 0).deriveFont(newSize);
			setFont(newFont);
		} else if (vp == DVisualLexicon.NODE_LABEL_POSITION) {
			this.setLabelPosition((ObjectPosition) value);
		} else if (vp == BasicVisualLexicon.NODE_NESTED_NETWORK_IMAGE_VISIBLE) {
			setNestedNetworkImgVisible(Boolean.TRUE.equals(value));
		} else if (vp instanceof CustomGraphicsVisualProperty) {
			setCustomGraphics((CustomGraphicsVisualProperty) vp, (CyCustomGraphics<CustomGraphicLayer>) value);
		} else if (vp instanceof ObjectPositionVisualProperty) {
			setCustomGraphicsPosition(vp, (ObjectPosition) value);
		} else if (CG_SIZE_PATTERN.matcher(vp.getIdString()).matches()) {
			setCustomGraphicsSize((VisualProperty<Double>) vp, (Double) value);
		}
	}

	public boolean getExtents(float[] extents, int offset) {
		if (!isVisible) return false;

		if (m_xMin == Float.MIN_VALUE &&
		    m_yMin == Float.MIN_VALUE &&
				m_xMax == Float.MAX_VALUE &&
				m_yMax == Float.MAX_VALUE) {
			// Whatever we think, we're not really here...
			return false;
		}

		extents[offset] = m_xMin;
		extents[offset+1] = m_yMin;
		extents[offset+2] = m_xMax;
		extents[offset+3] = m_yMax;
		return true;
	}

	public Map<VisualProperty<CyCustomGraphics>, CustomGraphicsInfo> getCustomGraphics() {
		return cgInfoMap;
	}
	
	private void setCustomGraphics(final CustomGraphicsVisualProperty vp, 
								   final CyCustomGraphics<CustomGraphicLayer> cg) {
		synchronized (CG_LOCK) {
			final CustomGraphicsInfo info = getCustomGraphicsInfo(vp);
			info.setCustomGraphics(cg);
		}
	}

	@SuppressWarnings("rawtypes")
	private void setCustomGraphicsPosition(final VisualProperty<?> vp, final ObjectPosition position) {
		// No need to modify
		if (position == null) return;
		
		final VisualProperty<CyCustomGraphics> parent = getParentCustomGraphicsProperty(vp);
		if (parent == null) return;
		
		synchronized (CG_LOCK) {
			final CustomGraphicsInfo info = getCustomGraphicsInfo(parent);
			info.setPosition(position);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void setCustomGraphicsSize(final VisualProperty<Double> vp, final Double size) {
		if (size == null) return;
		
		final VisualProperty<CyCustomGraphics> parent = DVisualLexicon.getAssociatedCustomGraphicsVP(vp);
		if (parent == null) return;
		
		synchronized (CG_LOCK) {
			final CustomGraphicsInfo info = getCustomGraphicsInfo(parent);
			info.setSize(size);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private VisualProperty<CyCustomGraphics> getParentCustomGraphicsProperty(final VisualProperty<?> vp) {
		VisualProperty<CyCustomGraphics> parent = null;
		
		// Use the visual property tree to retrieve its parent.
		final VisualLexiconNode lexNode = lexicon.getVisualLexiconNode(vp);
		final Collection<VisualLexiconNode> leavs = lexNode.getParent().getChildren();
		
		for (VisualLexiconNode vlNode : leavs) {
			if (vlNode.getVisualProperty().getRange().getType().equals(CyCustomGraphics.class)) {
				parent = (VisualProperty<CyCustomGraphics>) vlNode.getVisualProperty();
				break;
			}
		}

		if (parent == null)
			logger.error("Associated CustomGraphics VisualProperty is missing for " + vp.getDisplayName());
		
		return parent;
	}
	
	/**
	 * If a CustomGraphicsInfo for the passed VisualProperty does not exist yet, it creates a new one.
	 */
	@SuppressWarnings("rawtypes")
	private CustomGraphicsInfo getCustomGraphicsInfo(final VisualProperty<CyCustomGraphics> vp) {
		synchronized (CG_LOCK) {
			CustomGraphicsInfo info = cgInfoMap.get(vp);
			
			if (info == null) {
				info = new CustomGraphicsInfo(vp);
				cgInfoMap.put(vp, info);
			}
			
			return info;
		}
	}

	public boolean isVisible() {
		return isVisible;
	}

	@Override
	protected <T, V extends T> V getDefaultValue(VisualProperty<T> vp) {
		return graphView.m_nodeDetails.getDefaultValue(vp);
	}
	
	@Override
	protected DGraphView getDGraphView() {
		return graphView;
	}
}
