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
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.GraphView;
import org.cytoscape.ding.Label;
import org.cytoscape.ding.NodeView;
import org.cytoscape.ding.ObjectPosition;
import org.cytoscape.ding.customgraphics.CustomGraphicsPositionCalculator;
import org.cytoscape.ding.customgraphics.NullCustomGraphics;
import org.cytoscape.ding.impl.visualproperty.CustomGraphicsVisualProperty;
import org.cytoscape.ding.impl.visualproperty.ObjectPositionVisualProperty;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.PaintedShape;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependency;

/**
 * DING implementation of View Model and Presentation.
 */
public class DNodeView extends AbstractDViewModel<CyNode> implements NodeView, Label {
	
	private final static Set<CustomGraphicLayer> EMPTY_CUSTOM_GRAPHICS = new LinkedHashSet<CustomGraphicLayer>(0);

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

	// A LinkedHashSet of the custom graphics associated with this
	// DNodeView. We need the HashSet linked since the ordering of
	// custom graphics is important. For space considerations, we
	// keep _customGraphics null when there are no custom
	// graphics--event though this is a bit more complicated:
	private LinkedHashSet<CustomGraphicLayer> orderedCustomGraphicLayers;

	// CG_LOCK is used for synchronizing custom graphics operations on this
	// DNodeView.
	// Arrays are objects like any other and can be used for synchronization. We
	// use an array
	// object assuming it takes up the least amount of memory:
	private final Object[] CG_LOCK = new Object[0];

	// Map from NodeCustomGraphics Visual Property to native CustomGraphicLayers
	// objects.
	private final Map<VisualProperty<?>, Set<CustomGraphicLayer>> cgMap;

	// Locations of Custom Graphics
	private Map<CustomGraphicLayer, ObjectPosition> graphicsPositions;

	// Label position
	private ObjectPosition labelPosition;

	private final VisualMappingManager vmm;
	
	private final CyNetworkViewManager netViewMgr; 
	
	DNodeView(final VisualLexicon lexicon, final DGraphView graphView, final CyNode model, final VisualMappingManager vmm,
			final CyNetworkViewManager netViewMgr) {
		super(model, lexicon);
		
		if (graphView == null)
			throw new NullPointerException("View must never be null.");
		if (lexicon == null)
			throw new NullPointerException("Lexicon must never be null.");

		this.vmm = vmm;
		this.labelPosition = new ObjectPositionImpl();

		this.netViewMgr = netViewMgr;
		this.modelIdx = model.getSUID();
		
		// Initialize custom graphics pool.
		cgMap = new HashMap<VisualProperty<?>, Set<CustomGraphicLayer>>();

		this.graphView = graphView;
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
				graphView.m_contentChanged = true;
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
				graphView.m_contentChanged = true;
		}
	}

	@Override
	public void setBorderPaint(Paint paint) {
		synchronized (graphView.m_lock) {
			final Paint transColor = getTransparentColor(paint, graphView.m_nodeDetails.getBorderTransparency(model));
			graphView.m_nodeDetails.overrideBorderPaint(model, transColor);
			fixBorder();
			graphView.m_contentChanged = true;
		}
	}

	@Override
	public void setBorderWidth(float width) {
		synchronized (graphView.m_lock) {
			graphView.m_nodeDetails.overrideBorderWidth(model, width);
			graphView.m_contentChanged = true;
		}
	}

	@Override
	public void setBorder(final Stroke stroke) {
		synchronized (graphView.m_lock) {
			graphView.m_nodeDetails.overrideBorderStroke(model, stroke);
			graphView.m_contentChanged = true;
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
			
			graphView.m_contentChanged = true;
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
			
			graphView.m_contentChanged = true;
		}
	}


	@Override
	public boolean setWidth(final double originalWidth) {
		final double width;

		// Check bypass
		if (isValueLocked(DVisualLexicon.NODE_WIDTH))
			width = getVisualProperty(DVisualLexicon.NODE_WIDTH);
		else
			width = originalWidth;

		resizeCustomGraphics(width/getWidth(), 1.0);

		synchronized (graphView.m_lock) {
			if (!graphView.m_spacial.exists(modelIdx, graphView.m_extentsBuff, 0))
				return false;

			final double xCenter = (graphView.m_extentsBuff[0] + graphView.m_extentsBuff[2]) / 2.0d;
			final double wDiv2 = width / 2.0d;
			final float xMin = (float) (xCenter - wDiv2);
			final float xMax = (float) (xCenter + wDiv2);

			if (!(xMax > xMin))
				throw new IllegalArgumentException("width is too small");

			graphView.m_spacial.delete(modelIdx);
			graphView.m_spacial.insert(modelIdx, xMin, graphView.m_extentsBuff[1], xMax,
					graphView.m_extentsBuff[3]);
			graphView.m_contentChanged = true;

			return true;
		}
	}

	@Override
	public double getWidth() {
		synchronized (graphView.m_lock) {
			if (!graphView.m_spacial.exists(modelIdx, graphView.m_extentsBuff, 0))
				return -1.0d;

			return ((double) graphView.m_extentsBuff[2]) - graphView.m_extentsBuff[0];
		}
	}

	@Override
	public boolean setHeight(final double originalHeight) {
		final double height;
		// Check bypass
		if (isValueLocked(DVisualLexicon.NODE_HEIGHT))
			height = getVisualProperty(DVisualLexicon.NODE_HEIGHT);
		else
			height = originalHeight;

		resizeCustomGraphics(1.0, height/getHeight());
		
		synchronized (graphView.m_lock) {
			if (!graphView.m_spacial.exists(modelIdx, graphView.m_extentsBuff, 0))
				return false;

			final double yCenter = ((graphView.m_extentsBuff[1]) + graphView.m_extentsBuff[3]) / 2.0d;
			final double hDiv2 = height / 2.0d;
			final float yMin = (float) (yCenter - hDiv2);
			final float yMax = (float) (yCenter + hDiv2);

			if (!(yMax > yMin))
				throw new IllegalArgumentException("height is too small max:" + yMax + " min:" + yMin + " center:"
						+ yCenter + " height:" + height);

			graphView.m_spacial.delete(modelIdx);
			graphView.m_spacial.insert(modelIdx, graphView.m_extentsBuff[0], yMin, graphView.m_extentsBuff[2], yMax);
			graphView.m_contentChanged = true;

			return true;
		}
	}

	@Override
	public double getHeight() {
		synchronized (graphView.m_lock) {
			if (!graphView.m_spacial.exists(modelIdx, graphView.m_extentsBuff, 0))
				return -1.0d;

			return ((double) graphView.m_extentsBuff[3]) - graphView.m_extentsBuff[1];
		}
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
			graphView.m_spacial.insert(modelIdx, xMin, yMin, xMax, yMax);
			graphView.m_contentChanged = true;
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
		
		final double wDiv2;
		
		synchronized (graphView.m_lock) {
			final boolean nodeVisible = graphView.m_spacial.exists(modelIdx, graphView.m_extentsBuff, 0);

			if (nodeVisible)
				wDiv2 = (graphView.m_extentsBuff[2] - graphView.m_extentsBuff[0]) / 2.0d;
			else
				wDiv2 = 	(m_hiddenXMax - m_hiddenXMin) / 2.0d;

			final float xMin = (float) (xPos - wDiv2);
			final float xMax = (float) (xPos + wDiv2);

			if (!(xMax > xMin))
				throw new IllegalStateException("width of node has degenerated to zero after rounding");

			// If the node is visible, set the extents.
			if (nodeVisible) {
				graphView.m_spacial.delete(modelIdx);
				graphView.m_spacial.insert(modelIdx, xMin, graphView.m_extentsBuff[1], xMax, graphView.m_extentsBuff[3]);
				graphView.m_contentChanged = true;

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

	public double getXPosition() {
		synchronized (graphView.m_lock) {
			if (graphView.m_spacial.exists(modelIdx, graphView.m_extentsBuff, 0))
				return (((double) graphView.m_extentsBuff[0]) + graphView.m_extentsBuff[2]) / 2.0d;
			else
				return (double) (m_hiddenXMin + m_hiddenXMax) / 2.0;
		}
	}

	public void setYPosition(final double yPos) {
		final double hDiv2;
		
		synchronized (graphView.m_lock) {
			
			final boolean nodeVisible = graphView.m_spacial.exists(modelIdx, graphView.m_extentsBuff, 0);

			if (nodeVisible)
				hDiv2 = ((graphView.m_extentsBuff[3]) - graphView.m_extentsBuff[1]) / 2.0d;
			else
				hDiv2 = (m_hiddenYMax - m_hiddenYMin) / 2.0d;

			final float yMin = (float) (yPos - hDiv2);
			final float yMax = (float) (yPos + hDiv2);

			if (!(yMax > yMin))
				throw new IllegalStateException("height of node has degenerated to zero after " + "rounding");

			// If the node is visible, set the extents.
			if (nodeVisible) {
				graphView.m_spacial.delete(modelIdx);
				graphView.m_spacial.insert(modelIdx, graphView.m_extentsBuff[0], yMin, graphView.m_extentsBuff[2], yMax);
				graphView.m_contentChanged = true;

				// If the node is NOT visible (hidden), then update the hidden
				// extents. Doing this will mean that the node view will be properly scaled and
				// rotated relative to the other nodes.
			} else {
				m_hiddenYMax = yMax;
				m_hiddenYMin = yMin;
			}
		}
	}

	public double getYPosition() {
		synchronized (graphView.m_lock) {
			if (graphView.m_spacial.exists(modelIdx, graphView.m_extentsBuff, 0))
				return ((graphView.m_extentsBuff[1]) + graphView.m_extentsBuff[3]) / 2.0d;
			else
				return ((m_hiddenYMin + m_hiddenYMax)) / 2.0d;
		}
	}

	@Override
	public void select() {
		final boolean somethingChanged;

		synchronized (graphView.m_lock) {
			somethingChanged = selectInternal();

			if (somethingChanged)
				graphView.m_contentChanged = true;
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

			if (somethingChanged)
				graphView.m_contentChanged = true;
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
			graphView.m_contentChanged = true;
		}
	}

	@Override
	public void setToolTip(final String tip) {
		synchronized (graphView.m_lock) {
			graphView.m_nodeDetails.overrideTooltipText(model, tip);
			graphView.m_contentChanged = true;
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
				graphView.m_contentChanged = true;
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
			graphView.m_contentChanged = true;
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

			graphView.m_contentChanged = true;
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
			graphView.m_contentChanged = true;
		}
	}

	/**
	 * Adds a given CustomGraphicLayer, <EM>in draw order</EM>, to this DNodeView in
	 * a thread-safe way. Each CustomGraphicLayer will be drawn in the order is was
	 * added. So, if you care about draw order (as for overlapping graphics),
	 * make sure you add them in the order you desire. Note that since
	 * CustomGraphicLayers may be added by multiple apps, your additions may be
	 * interleaved with others.
	 * 
	 * <P>
	 * A CustomGraphicLayer can only be associated with a DNodeView once. If you wish
	 * to have a custom graphic, with the same paint and shape information,
	 * occur in multiple places in the draw order, simply create a new
	 * CustomGraphicLayer and add it.
	 * 
	 * @since Cytoscape 2.6
	 * @throws IllegalArgumentException
	 *             if shape or paint are null.
	 * @return true if the CustomGraphicLayer was added to this DNodeView. false if
	 *         this DNodeView already contained this CustomGraphicLayer.
	 * @see org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer
	 */
	public boolean addCustomGraphic(final CustomGraphicLayer cg) {
		boolean retVal = false;
		synchronized (CG_LOCK) {
			// Lazy instantiation
			if (orderedCustomGraphicLayers == null) {
				orderedCustomGraphicLayers = new LinkedHashSet<CustomGraphicLayer>();
				graphicsPositions = new HashMap<CustomGraphicLayer, ObjectPosition>();
			}

			if (orderedCustomGraphicLayers.contains(cg))
				retVal = false;
			else
				retVal = orderedCustomGraphicLayers.add(cg);
		}
		ensureContentChanged();
		return retVal;
	}

	/**
	 * A thread-safe way to determine if this DNodeView contains a given custom
	 * graphic.
	 * 
	 * @param cg
	 *            the CustomGraphicLayer for which we are checking containment.
	 * @since Cytoscape 2.6
	 */
	public boolean containsCustomGraphic(final CustomGraphicLayer cg) {
		synchronized (CG_LOCK) {
			if (orderedCustomGraphicLayers == null)
				return false;
			return orderedCustomGraphicLayers.contains(cg);
		}
	}

	/**
	 * Return a read-only Iterator over all CustomGraphicLayers contained
	 * in this DNodeView. The Iterator will return each CustomGraphicLayer in draw
	 * order. The Iterator cannot be used to modify the underlying set of
	 * CustomGraphicLayers.
	 * 
	 * @return The CustomGraphicLayers Iterator. If no CustomGraphicLayers are associated
	 *         with this DNOdeView, null is returned.
	 * @throws UnsupportedOperationException
	 *             if an attempt is made to use the Iterator's remove() method.
	 */
	public Iterator<CustomGraphicLayer> customGraphicIterator() {
		synchronized (CG_LOCK) {
			final Set<CustomGraphicLayer> toIterate;
			if (orderedCustomGraphicLayers == null)
				toIterate = EMPTY_CUSTOM_GRAPHICS;
			else
				toIterate = orderedCustomGraphicLayers;
			
			return new ReadOnlyIterator<CustomGraphicLayer>(toIterate);
		}
	}

	/**
	 * A thread-safe method for removing a given custom graphic from this
	 * DNodeView.
	 * 
	 * @return true if the custom graphic was found an removed. Returns false if
	 *         cg is null or is not a custom graphic associated with this
	 *         DNodeView.
	 * @since Cytoscape 2.6
	 */
	public boolean removeCustomGraphic(CustomGraphicLayer cg) {
		boolean retVal = false;
		synchronized (CG_LOCK) {
			if (orderedCustomGraphicLayers != null) {
				retVal = orderedCustomGraphicLayers.remove(cg);
				graphicsPositions.remove(cg);
			}
		}
		ensureContentChanged();
		return retVal;
	}

	void removeAllCustomGraphics() {
		if (orderedCustomGraphicLayers != null) {
			orderedCustomGraphicLayers.clear();
			graphicsPositions.clear();
		}
		// ensureContentChanged();
	}

	/**
	 * A thread-safe method returning the number of custom graphics associated
	 * with this DNodeView. If none are associated, zero is returned.
	 * 
	 * @since Cytoscape 2.6
	 */
	int getNumCustomGraphics() {
		synchronized (CG_LOCK) {
			if (orderedCustomGraphicLayers == null)
				return 0;
			else
				return orderedCustomGraphicLayers.size();
		}
	}

	private void ensureContentChanged() {
		synchronized (graphView.m_lock) {
			graphView.m_contentChanged = true;
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

	private static final class ReadOnlyIterator<T> implements Iterator<T> {
		private final Iterator<? extends T> _iterator;

		public ReadOnlyIterator(Iterable<T> toIterate) {
			_iterator = toIterate.iterator();
		}

		public boolean hasNext() {
			return _iterator.hasNext();
		}

		public T next() {
			return _iterator.next();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	};


	public double getLabelWidth() {
		synchronized (graphView.m_lock) {
			return graphView.m_nodeDetails.getLabelWidth(model);
		}
	}

	public void setLabelWidth(double width) {
		synchronized (graphView.m_lock) {
			graphView.m_nodeDetails.overrideLabelWidth(model, width);
			graphView.m_contentChanged = true;
		}
	}

	TexturePaint getNestedNetworkTexturePaint() {
		synchronized (graphView.m_lock) {
			++nestedNetworkPaintingDepth;

			try {
				if (nestedNetworkPaintingDepth > 1 || getModel().getNetworkPointer() == null || !nestedNetworkVisible)
					return null;

				final Boolean netImgVisible = getVisualProperty(BasicVisualLexicon.NODE_NESTED_NETWORK_IMAGE_VISIBLE);

				if (!Boolean.TRUE.equals(netImgVisible))
					return null;

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
					if (DEFAULT_NESTED_NETWORK_IMAGE == null)
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

		if(this.getModel().getNetworkPointer() == null)
			this.nestedNetworkView = null;
		else{
			final Iterator<CyNetworkView> viewIterator =  netViewMgr.getNetworkViews(this.getModel().getNetworkPointer()).iterator();
			if (viewIterator.hasNext() ) 
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
					.overrideLabelTextAnchor(model, 0, labelPosition.getAnchor().getConversionConstant());
			graphView.m_nodeDetails.overrideLabelNodeAnchor(model, 0, labelPosition.getTargetAnchor()
					.getConversionConstant());
			graphView.m_nodeDetails.overrideLabelJustify(model, 0, labelPosition.getJustify().getConversionConstant());
			graphView.m_nodeDetails.overrideLabelOffsetVectorX(model, 0, labelPosition.getOffsetX());
			graphView.m_nodeDetails.overrideLabelOffsetVectorY(model, 0, labelPosition.getOffsetY());

			graphView.m_contentChanged = true;
		}
	}

	private CustomGraphicLayer moveCustomGraphicsToNewPosition(final CustomGraphicLayer cg, final ObjectPosition newPosition) {
		if (cg == null || newPosition == null)
			throw new NullPointerException("CustomGraphicLayer and Position cannot be null.");

		removeCustomGraphic(cg);

		// Create new graphics
		final CustomGraphicLayer newCg = CustomGraphicsPositionCalculator.transform(newPosition, this, cg);

		this.addCustomGraphic(newCg);
		graphicsPositions.put(newCg, newPosition);

		return newCg;
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
			if (((Boolean) value).booleanValue())
				graphView.showGraphObject(this);
			else
				graphView.hideGraphObject(this);
		} else if (vp == BasicVisualLexicon.NODE_FILL_COLOR) {
			setUnselectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.NODE_BORDER_PAINT) {
			setBorderPaint((Color)value);
		} else if (vp == DVisualLexicon.NODE_BORDER_TRANSPARENCY) {
			final Integer opacity = ((Number) value).intValue();
			setBorderTransparency(opacity);
		} else if (vp == DVisualLexicon.NODE_BORDER_WIDTH) {
			setBorderWidth(((Number) value).floatValue());
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
		}  else if (vp == BasicVisualLexicon.NODE_LABEL_WIDTH) {
			setLabelWidth(((Number) value).doubleValue());
		} else if (vp == BasicVisualLexicon.NODE_X_LOCATION) {
			setXPosition(((Number) value).doubleValue());
		} else if (vp == BasicVisualLexicon.NODE_Y_LOCATION) {
			setYPosition(((Number) value).doubleValue());
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
		} else if (vp instanceof CustomGraphicsVisualProperty) {
			applyCustomGraphics(vp, (CyCustomGraphics<CustomGraphicLayer>) value);
		} else if (vp instanceof ObjectPositionVisualProperty) {
			applyCustomGraphicsPosition(vp, (ObjectPosition) value);
		}
	}

	private void applyCustomGraphics(final VisualProperty<?> vp, 
	                                 final CyCustomGraphics<CustomGraphicLayer> customGraphics) {
		Set<CustomGraphicLayer> dCustomGraphicsSet = cgMap.get(vp);
		
		if (dCustomGraphicsSet == null)
			dCustomGraphicsSet = new HashSet<CustomGraphicLayer>();

		for (final CustomGraphicLayer cg : dCustomGraphicsSet)
			removeCustomGraphic(cg);

		dCustomGraphicsSet.clear();

		if (customGraphics == null || customGraphics instanceof NullCustomGraphics)
			return;

		final List<CustomGraphicLayer> layers = customGraphics.getLayers(graphView, this);

		// No need to update
		if (layers == null || layers.size() == 0)
			return;

		// Check dependency. Sync size or not.
		final VisualProperty<Double> cgSizeVP = DVisualLexicon.getAssociatedCustomGraphicsSizeVP(vp);
		boolean sync = syncToNode();
		
		final VisualProperty<ObjectPosition> cgPositionVP = DVisualLexicon.getAssociatedCustomGraphicsPositionVP(vp);
		final ObjectPosition positionValue = getVisualProperty(cgPositionVP);
		final Double customSize = getVisualProperty(cgSizeVP);

		for (CustomGraphicLayer newCG : layers) {
			// Assume it's a Ding layer
			CustomGraphicLayer finalCG = newCG;

			if (sync) {
				// System.out.println("Synching size to "+this.getWidth()+"x"+this.getHeight());
				// Size is locked to node size.				
				finalCG = syncSize(customGraphics, newCG, this.getWidth(), this.getHeight());
			} else if (customSize != null) {
				// System.out.println("Synching size to "+customSize);
				// Size should be set to customSize
				finalCG = syncSize(customGraphics, newCG, customSize, customSize);
			}
			finalCG = moveCustomGraphicsToNewPosition(finalCG, positionValue);

			addCustomGraphic(finalCG);
			dCustomGraphicsSet.add(finalCG);
		}

		cgMap.put(vp, dCustomGraphicsSet);
	}

	private void applyCustomGraphicsPosition(final VisualProperty<?> vp, final ObjectPosition position) {
		// No need to modify
		if (position == null)
			return;

		// Use dependency to retrieve its parent.
		final VisualLexiconNode lexNode = lexicon.getVisualLexiconNode(vp);
		final Collection<VisualLexiconNode> leavs = lexNode.getParent().getChildren();
		VisualProperty<?> parent = null;
		
		for (VisualLexiconNode vlNode : leavs) {
			if (vlNode.getVisualProperty().getRange().getType().equals(CyCustomGraphics.class)) {
				parent = vlNode.getVisualProperty();
				break;
			}
		}

		if (parent == null)
			throw new NullPointerException("Associated Custom Graphics VP is missing for " + vp.getDisplayName());

		final Set<CustomGraphicLayer> currentCG = cgMap.get(parent);

		if (currentCG == null || currentCG.size() == 0)
			return;

		final Set<CustomGraphicLayer> newList = new HashSet<CustomGraphicLayer>();
		
		for (CustomGraphicLayer g : currentCG)
			newList.add(moveCustomGraphicsToNewPosition(g, position));

		currentCG.clear();
		currentCG.addAll(newList);

		this.cgMap.put(parent, currentCG);
	}

	private void resizeCustomGraphics(double widthScale, double heightScale) {
		// System.out.println("resizing custom graphics to "+widthScale+", "+heightScale);
		boolean sync = syncToNode();
		if (!sync || cgMap == null || cgMap.isEmpty()) return;

		// Get all of our custom graphics layers
		for (VisualProperty<?> vp: cgMap.keySet()) {
			final Set<CustomGraphicLayer> currentCG = cgMap.get(vp);
			if (currentCG == null || currentCG.size() == 0) continue;

			// Resize
			final Set<CustomGraphicLayer> newList = new HashSet<CustomGraphicLayer>();
			for (CustomGraphicLayer g : currentCG)
				newList.add(resizeCustomGraphicsLayer(g, widthScale, heightScale));

			currentCG.clear();
			currentCG.addAll(newList);

			this.cgMap.put(vp, currentCG);
		}
	}

	CustomGraphicLayer resizeCustomGraphicsLayer(CustomGraphicLayer cg, double widthScale, double heightScale) {
		removeCustomGraphic(cg);
		AffineTransform scale = AffineTransform.getScaleInstance(widthScale, heightScale);
		CustomGraphicLayer newCG = cg.transform(scale);
		addCustomGraphic(newCG);
		return newCG;
	}

	private boolean syncToNode() {
		boolean sync = false;
		if (vmm.getCurrentVisualStyle() != null) {
			Set<VisualPropertyDependency<?>> dependencies = vmm.getCurrentVisualStyle().getAllVisualPropertyDependencies();
		
			for (VisualPropertyDependency<?> dep:dependencies) {
				if(dep.getIdString().equals("nodeCustomGraphicsSizeSync")) {
					sync = dep.isDependencyEnabled();
					break;
				}
			}
		}
		return sync;
	}

	private CustomGraphicLayer syncSize(CyCustomGraphics<CustomGraphicLayer> graphics, 
	                               final CustomGraphicLayer cg, double width, double height) {
		// final double nodeW = this.getWidth();
		// final double nodeH = this.getHeight();

		final Rectangle2D originalBounds = cg.getBounds2D();
		// If this is just a paint, getBounds2D will return null and
		// we can use our own width and height
		if (originalBounds == null) return cg;

		if (width == 0.0 || height == 0.0) return cg;

		final double cgW = originalBounds.getWidth();
		final double cgH = originalBounds.getHeight();

		// In case size is same, return the original.
		if (width == cgW && height == cgH)
			return cg;

		final AffineTransform scale;
		final float fit = graphics.getFitRatio();

		// Case 1: if custom graphic is a vector fit width and length
		if (cg instanceof PaintedShape) {
			scale = AffineTransform.getScaleInstance(fit * width / cgW, fit * height / cgH);
		} else {
			double scaleW = width/cgW;
			double scaleH = height/cgH;
			// Case 2: node height value is larger than width
			if (scaleW >= scaleH) {
				scale = AffineTransform.getScaleInstance(fit * scaleH, fit * scaleH);
				// scale = AffineTransform.getScaleInstance(fit * (width / cgW) * (height / width), fit * height / cgH);
			} else {
				scale = AffineTransform.getScaleInstance(fit * scaleW, fit * scaleW);
				// scale = AffineTransform.getScaleInstance(fit * (width / cgW) * (height / width), fit * height / cgH);
			}
		}
		
		return cg.transform(scale);
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
