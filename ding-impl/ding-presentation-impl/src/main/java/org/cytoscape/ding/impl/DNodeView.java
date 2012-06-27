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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
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

import org.cytoscape.ding.DNodeShape;
import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.EdgeView;
import org.cytoscape.ding.GraphView;
import org.cytoscape.ding.Label;
import org.cytoscape.ding.NodeView;
import org.cytoscape.ding.ObjectPosition;
import org.cytoscape.ding.customgraphics.CyCustomGraphics;
import org.cytoscape.ding.customgraphics.Layer;
import org.cytoscape.ding.customgraphics.NullCustomGraphics;
import org.cytoscape.ding.customgraphics.vector.VectorCustomGraphics;
import org.cytoscape.ding.impl.customgraphics.CustomGraphicsPositionCalculator;
import org.cytoscape.ding.impl.visualproperty.CustomGraphicsVisualProperty;
import org.cytoscape.ding.impl.visualproperty.ObjectPositionVisualProperty;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.graph.render.stateful.CustomGraphic;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependency;

/**
 * DING implementation of View Model and Presentation.
 */
public class DNodeView extends AbstractDViewModel<CyNode> implements NodeView, Label {
	
	private final static Set<CustomGraphic> EMPTY_CUSTOM_GRAPHICS = new LinkedHashSet<CustomGraphic>(0);

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

	// The FixedGraph index (non-negative)
	private final long m_inx;

	// Node color
//	private Paint m_unselectedPaint;
//	private Paint m_selectedPaint;
	
	private boolean selected;
	private Paint borderPaint;
	private int transparency;
	private float fontSize = 12;

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
	private LinkedHashSet<CustomGraphic> orderedCustomGraphicLayers;

	// CG_LOCK is used for synchronizing custom graphics operations on this
	// DNodeView.
	// Arrays are objects like any other and can be used for synchronization. We
	// use an array
	// object assuming it takes up the least amount of memory:
	private final Object[] CG_LOCK = new Object[0];

	// Map from NodeCustomGraphics Visual Property to native CustomGraphics
	// objects.
	private final Map<VisualProperty<?>, Set<CustomGraphic>> cgMap;

	// Locations of Custom Graphics
	private Map<CustomGraphic, ObjectPosition> graphicsPositions;

	// Label position
	private ObjectPosition labelPosition;

	// Visual Properties used in this node view.
	private final VisualLexicon lexicon;

	private final VisualMappingManager vmm;
	
	private final CyNetworkViewManager netViewMgr; 
	
	public DNodeView(final VisualLexicon lexicon, final DGraphView graphView, long inx, final CyNode model, final VisualMappingManager vmm,
			final CyNetworkViewManager netViewMgr) {
		super(model);
		
		if (graphView == null)
			throw new NullPointerException("View must never be null!");
		if (lexicon == null)
			throw new NullPointerException("Lexicon must never be null!");

		this.lexicon = lexicon;
		this.vmm = vmm;
		this.labelPosition = new ObjectPositionImpl();

		this.netViewMgr = netViewMgr;
		
		// Initialize custom graphics pool.
		cgMap = new HashMap<VisualProperty<?>, Set<CustomGraphic>>();

		this.graphView = graphView;

		m_inx = inx;
		borderPaint = graphView.m_nodeDetails.borderPaint(model);
		transparency = DEFAULT_TRANSPARENCY;
	}

	@Override
	public GraphView getGraphView() {
		return graphView;
	}

	@Override
	public long getGraphPerspectiveIndex() {
		return m_inx;
	}

	@Override
	public List<EdgeView> getEdgeViewsList(final NodeView otherNodeView) {
		synchronized (graphView.m_lock) {
			return graphView.getEdgeViewsList(getModel(), ((DNodeView)otherNodeView).getModel());
		}
	}
	
	@Override
	public int getShape() {
		synchronized (graphView.m_lock) {
			return graphView.m_nodeDetails.shape(model);
		}
	}

	@Override
	public void setSelectedPaint(final Paint paint) {
		synchronized (graphView.m_lock) {
			if (paint == null)
				throw new NullPointerException("paint is null");

			graphView.m_nodeDetails.setSelectedPaint(model, paint);
			
			if (isSelected()) {
//				graphView.m_nodeDetails.overrideFillPaint(model, paint);
//
//				if (paint instanceof Color)
//					graphView.m_nodeDetails.overrideColorLowDetail(model, (Color) paint);

				graphView.m_contentChanged = true;
			}
		}
	}

	@Override
	public Paint getSelectedPaint() {
		return graphView.m_nodeDetails.selectedPaint(model);
	}

	@Override
	public void setUnselectedPaint(Paint paint) {
		synchronized (graphView.m_lock) {
			if (paint == null)
				throw new NullPointerException("paint is null");

			if (paint instanceof Color) {
				final Color c = (Color) paint;
				
				if (c.getAlpha() != transparency)
					paint = new Color(c.getRed(), c.getGreen(), c.getBlue(), transparency);
			}
			
			graphView.m_nodeDetails.setUnselectedPaint(model, paint);
			
			if (!isSelected())
				graphView.m_contentChanged = true;
		}
	}
	
	@Override
	public Paint getUnselectedPaint() {
		return graphView.m_nodeDetails.unselectedPaint(model);
	}

	@Override
	public void setBorderPaint(Paint paint) {
		synchronized (graphView.m_lock) {
			borderPaint = paint;
			fixBorder();
			graphView.m_contentChanged = true;
		}
	}

	@Override
	public Paint getBorderPaint() {
		return borderPaint;
	}

	@Override
	public void setBorderWidth(float width) {
		synchronized (graphView.m_lock) {
			graphView.m_nodeDetails.overrideBorderWidth(model, width);
			graphView.m_contentChanged = true;
		}
	}

	@Override
	public float getBorderWidth() {
		synchronized (graphView.m_lock) {
			return graphView.m_nodeDetails.borderWidth(model);
		}
	}

	@Override
	public void setBorder(Stroke stroke) {
		if (stroke instanceof BasicStroke) {
			synchronized (graphView.m_lock) {
				setBorderWidth(((BasicStroke) stroke).getLineWidth());

				final float[] dashArray = ((BasicStroke) stroke).getDashArray();

				if ((dashArray != null) && (dashArray.length > 1)) {
					m_borderDash = dashArray[0];
					m_borderDash2 = dashArray[1];
				} else {
					m_borderDash = 0.0f;
					m_borderDash2 = 0.0f;
				}

				fixBorder();
			}
		}
	}

	private float m_borderDash = 0.0f;
	private float m_borderDash2 = 0.0f;
	private final static Color s_transparent = new Color(0, 0, 0, 0);

	// Callers of this method must be holding m_view.m_lock.
	private void fixBorder() {
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
	public Stroke getBorder() {
		synchronized (graphView.m_lock) {
			if ((m_borderDash == 0.0f) && (m_borderDash2 == 0.0f))
				return new BasicStroke(getBorderWidth());
			else
				return new BasicStroke(getBorderWidth(), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f,
						new float[] { m_borderDash, m_borderDash2 }, 0.0f);
		}
	}

	@Override
	public void setTransparency(int trans) {
		synchronized (graphView.m_lock) {
			if (trans < 0 || trans > 255) {
				// If out of range, use default value.
				transparency = BasicVisualLexicon.NODE_TRANSPARENCY.getDefault();
			} else {
				transparency = trans;
			}

			final Paint unselectedPaint = getUnselectedPaint();

			if (unselectedPaint instanceof Color) {
				final Color unselectedColor = (Color) unselectedPaint;

				if (unselectedColor.getAlpha() != transparency)
					setUnselectedPaint(new Color(unselectedColor.getRed(), unselectedColor.getGreen(),
							unselectedColor.getBlue(), transparency));
			}
			
			graphView.m_contentChanged = true;
		}
	}

	@Override
	public int getTransparency() {
		return transparency;
	}

	@Override
	public boolean setWidth(final double originalWidth) {
		final Double width;
		
		// Check bypass
		if (isValueLocked(DVisualLexicon.NODE_WIDTH))
			width = getVisualProperty(DVisualLexicon.NODE_WIDTH);
		else
			width = originalWidth;
			
		synchronized (graphView.m_lock) {
			if (!graphView.m_spacial.exists(m_inx, graphView.m_extentsBuff, 0))
				return false;

			final double xCenter = (((double) graphView.m_extentsBuff[0]) + graphView.m_extentsBuff[2]) / 2.0d;
			final double wDiv2 = width / 2.0d;
			final float xMin = (float) (xCenter - wDiv2);
			final float xMax = (float) (xCenter + wDiv2);

			if (!(xMax > xMin))
				throw new IllegalArgumentException("width is too small");

			graphView.m_spacial.delete(m_inx);
			graphView.m_spacial.insert(m_inx, xMin, graphView.m_extentsBuff[1], xMax, graphView.m_extentsBuff[3]);

// TODO Is it really necessary?
//			final double w = ((double) xMax) - xMin;
//			final double h = ((double) graphView.m_extentsBuff[3]) - graphView.m_extentsBuff[1];
//			if (!(Math.max(w, h) < (1.99d * Math.min(w, h))) && (getShape() == GraphGraphics.SHAPE_ROUNDED_RECTANGLE))
//				setShape(NodeShapeVisualProperty.RECTANGLE);

			graphView.m_contentChanged = true;

			return true;
		}
	}

	@Override
	public double getWidth() {
		synchronized (graphView.m_lock) {
			if (!graphView.m_spacial.exists(m_inx, graphView.m_extentsBuff, 0))
				return -1.0d;

			return ((double) graphView.m_extentsBuff[2]) - graphView.m_extentsBuff[0];
		}
	}

	@Override
	public boolean setHeight(double originalHeight) {
		final Double height;
		// Check bypass
		if (isValueLocked(DVisualLexicon.NODE_HEIGHT))
			height = getVisualProperty(DVisualLexicon.NODE_HEIGHT);
		else
			height = originalHeight;
		
		synchronized (graphView.m_lock) {
			if (!graphView.m_spacial.exists(m_inx, graphView.m_extentsBuff, 0))
				return false;

			final double yCenter = (((double) graphView.m_extentsBuff[1]) + graphView.m_extentsBuff[3]) / 2.0d;
			final double hDiv2 = height / 2.0d;
			final float yMin = (float) (yCenter - hDiv2);
			final float yMax = (float) (yCenter + hDiv2);

			if (!(yMax > yMin))
				throw new IllegalArgumentException("height is too small max:" + yMax + " min:" + yMin + " center:"
						+ yCenter + " height:" + height);

			graphView.m_spacial.delete(m_inx);
			graphView.m_spacial.insert(m_inx, graphView.m_extentsBuff[0], yMin, graphView.m_extentsBuff[2], yMax);

			graphView.m_contentChanged = true;

			return true;
		}
	}

	@Override
	public double getHeight() {
		synchronized (graphView.m_lock) {
			if (!graphView.m_spacial.exists(m_inx, graphView.m_extentsBuff, 0))
				return -1.0d;

			return ((double) graphView.m_extentsBuff[3]) - graphView.m_extentsBuff[1];
		}
	}

	@Override
	public Label getLabel() {
		return this;
	}

	@Override
	public void setOffset(double x, double y) {
		synchronized (graphView.m_lock) {
			if (!graphView.m_spacial.exists(m_inx, graphView.m_extentsBuff, 0))
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

			graphView.m_spacial.delete(m_inx);
			graphView.m_spacial.insert(m_inx, xMin, yMin, xMax, yMax);
			graphView.m_contentChanged = true;
			setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION,x);
			setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION,y);
		}
	}

	@Override
	public Point2D getOffset() {
		synchronized (graphView.m_lock) {
			if (!graphView.m_spacial.exists(m_inx, graphView.m_extentsBuff, 0))
				return null;

			final double xCenter = (((double) graphView.m_extentsBuff[0]) + graphView.m_extentsBuff[2]) / 2.0d;
			final double yCenter = (((double) graphView.m_extentsBuff[1]) + graphView.m_extentsBuff[3]) / 2.0d;

			return new Point2D.Double(xCenter, yCenter);
		}
	}

	@Override
	public void setXPosition(double xPos) {
		synchronized (graphView.m_lock) {
			final double wDiv2;
			final boolean nodeVisible = graphView.m_spacial.exists(m_inx, graphView.m_extentsBuff, 0);

			if (nodeVisible)
				wDiv2 = (((double) graphView.m_extentsBuff[2]) - graphView.m_extentsBuff[0]) / 2.0d;
			else
				wDiv2 = (double) (m_hiddenXMax - m_hiddenXMin) / 2.0d;

			final float xMin = (float) (xPos - wDiv2);
			final float xMax = (float) (xPos + wDiv2);

			if (!(xMax > xMin))
				throw new IllegalStateException("width of node has degenerated to zero after rounding");

			// If the node is visible, set the extents.
			if (nodeVisible) {
				graphView.m_spacial.delete(m_inx);
				graphView.m_spacial.insert(m_inx, xMin, graphView.m_extentsBuff[1], xMax, graphView.m_extentsBuff[3]);
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
			if (graphView.m_spacial.exists(m_inx, graphView.m_extentsBuff, 0))
				return (((double) graphView.m_extentsBuff[0]) + graphView.m_extentsBuff[2]) / 2.0d;
			else
				return (double) (m_hiddenXMin + m_hiddenXMax) / 2.0;
		}
	}

	public void setYPosition(final double yPos) {
		final double hDiv2;
		
		synchronized (graphView.m_lock) {
			
			final boolean nodeVisible = graphView.m_spacial.exists(m_inx, graphView.m_extentsBuff, 0);

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
				graphView.m_spacial.delete(m_inx);
				graphView.m_spacial.insert(m_inx, graphView.m_extentsBuff[0], yMin, graphView.m_extentsBuff[2], yMax);
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
			if (graphView.m_spacial.exists(m_inx, graphView.m_extentsBuff, 0))
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
		if (selected)
			return false;

		selected = true;
		graphView.m_nodeDetails.select(model);
		graphView.m_selectedNodes.insert(m_inx);

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
		graphView.m_selectedNodes.delete(m_inx);

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
			final DNodeShape dShape;
			if (NodeShapeVisualProperty.isDefaultShape(shape))
				dShape = DNodeShape.getDShape(shape);
			else
				dShape = (DNodeShape) shape;

			graphView.m_nodeDetails.overrideShape(model, dShape);
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
			return graphView.m_nodeDetails.tooltipText(model);
		}
	}

	@Override
	public Paint getTextPaint() {
		synchronized (graphView.m_lock) {
			return graphView.m_nodeDetails.labelPaint(model, 0);
		}
	}

	@Override
	public void setTextPaint(Paint textPaint) {
		synchronized (graphView.m_lock) {
			graphView.m_nodeDetails.overrideLabelPaint(model, 0, textPaint);
			graphView.m_contentChanged = true;
		}
	}

	@Override
	public String getText() {
		synchronized (graphView.m_lock) {
			return graphView.m_nodeDetails.labelText(model, 0);
		}
	}

	@Override
	public void setText(String text) {
		synchronized (graphView.m_lock) {
			graphView.m_nodeDetails.overrideLabelText(model, 0, text);

			if (DEFAULT_LABEL_TEXT.equals(graphView.m_nodeDetails.labelText(model, 0)))
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
			return graphView.m_nodeDetails.labelFont(model, 0);
		}
	}

	/**
	 * Set label font type.
	 */
	@Override
	public void setFont(Font font) {
		synchronized (graphView.m_lock) {
			graphView.m_nodeDetails.overrideLabelFont(model, 0, font);
			graphView.m_contentChanged = true;
		}
	}

	/**
	 * Adds a given CustomGraphic, <EM>in draw order</EM>, to this DNodeView in
	 * a thread-safe way. Each CustomGraphic will be drawn in the order is was
	 * added. So, if you care about draw order (as for overlapping graphics),
	 * make sure you add them in the order you desire. Note that since
	 * CustomGraphics may be added by multiple apps, your additions may be
	 * interleaved with others.
	 * 
	 * <P>
	 * A CustomGraphic can only be associated with a DNodeView once. If you wish
	 * to have a custom graphic, with the same paint and shape information,
	 * occur in multiple places in the draw order, simply create a new
	 * CustomGraphic and add it.
	 * 
	 * @since Cytoscape 2.6
	 * @throws IllegalArgumentException
	 *             if shape or paint are null.
	 * @return true if the CustomGraphic was added to this DNodeView. false if
	 *         this DNodeView already contained this CustomGraphic.
	 * @see org.cytoscape.graph.render.stateful.CustomGraphic
	 */
	public boolean addCustomGraphic(final CustomGraphic cg) {
		boolean retVal = false;
		synchronized (CG_LOCK) {
			// Lazy instantiation
			if (orderedCustomGraphicLayers == null) {
				orderedCustomGraphicLayers = new LinkedHashSet<CustomGraphic>();
				graphicsPositions = new HashMap<CustomGraphic, ObjectPosition>();
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
	 *            the CustomGraphic for which we are checking containment.
	 * @since Cytoscape 2.6
	 */
	public boolean containsCustomGraphic(final CustomGraphic cg) {
		synchronized (CG_LOCK) {
			if (orderedCustomGraphicLayers == null)
				return false;
			return orderedCustomGraphicLayers.contains(cg);
		}
	}

	/**
	 * Return a read-only Iterator over all CustomGraphics contained
	 * in this DNodeView. The Iterator will return each CustomGraphic in draw
	 * order. The Iterator cannot be used to modify the underlying set of
	 * CustomGraphics.
	 * 
	 * @return The CustomGraphics Iterator. If no CustomGraphics are associated
	 *         with this DNOdeView, null is returned.
	 * @throws UnsupportedOperationException
	 *             if an attempt is made to use the Iterator's remove() method.
	 */
	public Iterator<CustomGraphic> customGraphicIterator() {
		synchronized (CG_LOCK) {
			final Set<CustomGraphic> toIterate;
			if (orderedCustomGraphicLayers == null)
				toIterate = EMPTY_CUSTOM_GRAPHICS;
			else
				toIterate = orderedCustomGraphicLayers;
			
			return new ReadOnlyIterator<CustomGraphic>(toIterate);
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
	public boolean removeCustomGraphic(CustomGraphic cg) {
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

	public void removeAllCustomGraphics() {
		synchronized (CG_LOCK) {
			if (orderedCustomGraphicLayers != null) {
				orderedCustomGraphicLayers.clear();
				graphicsPositions.clear();
			}
		}
		ensureContentChanged();
	}

	/**
	 * A thread-safe method returning the number of custom graphics associated
	 * with this DNodeView. If none are associated, zero is returned.
	 * 
	 * @since Cytoscape 2.6
	 */
	public int getNumCustomGraphics() {
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
			return graphView.m_nodeDetails.labelWidth(model);
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

				// Do we have a node w/ a self-reference?
				if (graphView == nestedNetworkView) {
					if (RECURSIVE_NESTED_NETWORK_IMAGE == null)
						return null;

					final Rectangle2D rect = new Rectangle2D.Double(-IMAGE_WIDTH / 2, -IMAGE_HEIGHT / 2, IMAGE_WIDTH,
							IMAGE_HEIGHT);
					return new TexturePaint(RECURSIVE_NESTED_NETWORK_IMAGE, rect);
				}
				
				setNestedNetworkView();
				
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
	public ObjectPosition getLabelPosition() {
		return labelPosition;
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

	private CustomGraphic moveCustomGraphicsToNewPosition(final CustomGraphic cg, final ObjectPosition newPosition) {
		if (cg == null || newPosition == null)
			throw new NullPointerException("CustomGraphic and Position cannot be null.");

		removeCustomGraphic(cg);

		// Create new graphics
		final CustomGraphic newCg = CustomGraphicsPositionCalculator.transform(newPosition, this, cg);

		this.addCustomGraphic(newCg);
		graphicsPositions.put(newCg, newPosition);

		return newCg;
	}
	
	@Override
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
			final Color borderColor = (Color) value;
			final int opacity = ((Color)getBorderPaint()).getAlpha();
			final Color newColor = new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), opacity);
			setBorderPaint(newColor);
		} else if (vp == DVisualLexicon.NODE_BORDER_TRANSPARENCY) {
			final Color borderColor = (Color) getBorderPaint();
			final Integer opacity = ((Number) value).intValue();
			if(borderColor.getAlpha() != opacity.intValue()) {
				Color newColor = new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), opacity);
				setBorderPaint(newColor);
			}
		} else if (vp == DVisualLexicon.NODE_BORDER_WIDTH) {
			setBorderWidth(((Number) value).floatValue());
		} else if (vp == DVisualLexicon.NODE_BORDER_LINE_TYPE) {
			DLineType dLineType = DLineType.getDLineType((LineType) value);
			setBorder(dLineType.getStroke(this.getBorderWidth()));
		} else if (vp == DVisualLexicon.NODE_TRANSPARENCY) {
			setTransparency(((Number) value).intValue());
		} else if (vp == BasicVisualLexicon.NODE_WIDTH) {
			setWidth(((Number) value).doubleValue());
		} else if (vp == BasicVisualLexicon.NODE_HEIGHT) {
			setHeight(((Number) value).doubleValue());
		} else if (vp == BasicVisualLexicon.NODE_SIZE) {
			setWidth(((Number) value).doubleValue());
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
			final Color labelColor = (Color) value;
			final int opacity = ((Color)getTextPaint()).getAlpha();
			final Color newColor = new Color(labelColor.getRed(), labelColor.getGreen(), labelColor.getBlue(), opacity);
			setTextPaint(newColor);
		} else if (vp == BasicVisualLexicon.NODE_LABEL_TRANSPARENCY) {
			final Color labelColor = (Color) getTextPaint();
			final Integer opacity = ((Number) value).intValue();
			if(labelColor.getAlpha() != opacity.intValue()) {
				Color newColor = new Color(labelColor.getRed(), labelColor.getGreen(), labelColor.getBlue(), opacity);
				setTextPaint(newColor);
			}
		} else if (vp == DVisualLexicon.NODE_LABEL_FONT_FACE) {
			final Font newFont = ((Font) value).deriveFont(fontSize);
			setFont(newFont);
		} else if (vp == DVisualLexicon.NODE_LABEL_FONT_SIZE) {
			float newSize = ((Number) value).floatValue();
			if (newSize != this.fontSize) {
				final Font newFont = getFont().deriveFont(newSize);
				setFont(newFont);
				fontSize = newSize;
			}
		} else if (vp == DVisualLexicon.NODE_LABEL_POSITION) {
			this.setLabelPosition((ObjectPosition) value);
		} else if (vp instanceof CustomGraphicsVisualProperty) {
			applyCustomGraphics(vp, (CyCustomGraphics<CustomGraphic>) value);
		} else if (vp instanceof ObjectPositionVisualProperty) {
			applyCustomGraphicsPosition(vp, (ObjectPosition) value);
		}
	}

	private void applyCustomGraphics(final VisualProperty<?> vp, final CyCustomGraphics<CustomGraphic> customGraphics) {
		Set<CustomGraphic> dCustomGraphicsSet = cgMap.get(vp);
		
		if (dCustomGraphicsSet == null)
			dCustomGraphicsSet = new HashSet<CustomGraphic>();

		for (final CustomGraphic cg : dCustomGraphicsSet)
			removeCustomGraphic(cg);

		dCustomGraphicsSet.clear();

		if (customGraphics == null || customGraphics instanceof NullCustomGraphics)
			return;

		final List<Layer<CustomGraphic>> layers = customGraphics.getLayers();

		// No need to update
		if (layers == null || layers.size() == 0)
			return;

		// Check dependency. Sync size or not.
		final VisualProperty<?> cgSizeVP = DVisualLexicon.getAssociatedCustomGraphicsSizeVP(vp);
		Set<VisualPropertyDependency<?>> dependencies = vmm.getCurrentVisualStyle().getAllVisualPropertyDependencies();
		boolean sync = false;
		
		for(VisualPropertyDependency dep:dependencies) {
			if(dep.getIdString().equals("nodeCustomGraphicsSizeSync")) {
				sync = dep.isDependencyEnabled();
				break;
			}
		}

		final VisualProperty<ObjectPosition> cgPositionVP = DVisualLexicon.getAssociatedCustomGraphicsPositionVP(vp);
		final ObjectPosition positionValue = getVisualProperty(cgPositionVP);

		for (Layer<CustomGraphic> layer : layers) {
			// Assume it's a Ding layer
			CustomGraphic newCG = layer.getLayerObject();
			CustomGraphic finalCG = newCG;

			if (sync) {
				// Size is locked to node size.				
				finalCG = syncSize(customGraphics, newCG);
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

		final Set<CustomGraphic> currentCG = cgMap.get(parent);

		if (currentCG == null || currentCG.size() == 0)
			return;

		final Set<CustomGraphic> newList = new HashSet<CustomGraphic>();
		
		for (CustomGraphic g : currentCG)
			newList.add(moveCustomGraphicsToNewPosition(g, position));

		currentCG.clear();
		currentCG.addAll(newList);

		this.cgMap.put(parent, currentCG);
	}

	private CustomGraphic syncSize(CyCustomGraphics<?> graphics, final CustomGraphic cg) {
		final double nodeW = this.getWidth();
		final double nodeH = this.getHeight();

		final Shape originalShape = cg.getShape();
		final Rectangle2D originalBounds = originalShape.getBounds2D();
		final double cgW = originalBounds.getWidth();
		final double cgH = originalBounds.getHeight();

		// In case size is same, return the original.
		if (nodeW == cgW && nodeH == cgH)
			return cg;

		final AffineTransform scale;
		final float fit = graphics.getFitRatio();

		// Case 1: if custom graphic is a vector fit width and length
		if (graphics instanceof VectorCustomGraphics) {
			scale = AffineTransform.getScaleInstance(fit * nodeW / cgW, fit * nodeH / cgH);
		} else {
			// Case 2: node height value is larger than width
			if (nodeW >= nodeH) {
				scale = AffineTransform.getScaleInstance(fit * (nodeW / cgW) * (nodeH / nodeW), fit * nodeH / cgH);
			} else {
				scale = AffineTransform.getScaleInstance(fit * nodeW / cgW, fit * (nodeH / cgH) * (nodeW / nodeH));
			}
		}
		
		return new CustomGraphic(scale.createTransformedShape(originalShape), cg.getPaintFactory());
	}
}
