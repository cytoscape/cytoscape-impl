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
import org.cytoscape.ding.EdgeView;
import org.cytoscape.ding.GraphView;
import org.cytoscape.ding.Label;
import org.cytoscape.ding.NodeView;
import org.cytoscape.ding.ObjectPosition;
import org.cytoscape.ding.customgraphics.CyCustomGraphics;
import org.cytoscape.ding.customgraphics.Layer;
import org.cytoscape.ding.customgraphics.NullCustomGraphics;
import org.cytoscape.ding.impl.customgraphics.CustomGraphicsPositionCalculator;
import org.cytoscape.ding.impl.customgraphics.vector.VectorCustomGraphics;
import org.cytoscape.ding.impl.visualproperty.CustomGraphicsVisualProperty;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.graph.render.stateful.CustomGraphic;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;

/**
 * DING implementation of View Model and Presentation.
 * 
 */
public class DNodeView extends AbstractDViewModel<CyNode> implements NodeView, Label {
	
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
	private final int m_inx;

	// Selection flag.
	private boolean m_selected;

	// Node color
	private Paint m_unselectedPaint;
	private Paint m_selectedPaint;
	private Paint m_borderPaint;

	// Opacity
	private int transparency;

	// Font size
	private float fontSize = 12;

	/**
	 * Stores the position of a nodeView when it's hidden so that when the
	 * nodeView is restored we can restore the view into the same position.
	 */
	float m_hiddenXMin = Float.MIN_VALUE;
	float m_hiddenYMin = Float.MIN_VALUE;
	float m_hiddenXMax = Float.MAX_VALUE;
	float m_hiddenYMax = Float.MAX_VALUE;

	// Tool Tip text
	private String m_toolTipText;

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

	// Will be used when Custom graphics is empty.
	private final static Set<CustomGraphic> EMPTY_CUSTOM_GRAPHICS = new LinkedHashSet<CustomGraphic>(0);

	// Map from NodeCustomGraphics Visual Property to native CustomGraphics
	// objects.
	private final Map<VisualProperty<?>, Set<CustomGraphic>> cgMap;

	// Locations of Custom Graphics
	private Map<CustomGraphic, ObjectPosition> graphicsPositions;

	// Label position
	private ObjectPosition labelPosition;

	// Visual Properties used in this node view.
	private final VisualLexicon lexicon;

	
	DNodeView(final VisualLexicon lexicon, final DGraphView graphView, int inx, final CyNode model) {
		super(model);
		
		if (graphView == null)
			throw new NullPointerException("View must never be null!");
		if (lexicon == null)
			throw new NullPointerException("Lexicon must never be null!");

		this.lexicon = lexicon;
		this.labelPosition = new ObjectPositionImpl();

		// Initialize custom graphics pool.
		cgMap = new HashMap<VisualProperty<?>, Set<CustomGraphic>>();

		this.graphView = graphView;

		m_inx = inx;

		m_selected = false;

		m_unselectedPaint = graphView.m_nodeDetails.fillPaint(m_inx);
		m_selectedPaint = DEFAULT_NODE_SELECTED_PAINT;
		m_borderPaint = graphView.m_nodeDetails.borderPaint(m_inx);

		transparency = DEFAULT_TRANSPARENCY;
	}

	@Override
	public GraphView getGraphView() {
		return graphView;
	}

	
	@Override
	public int getGraphPerspectiveIndex() {
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
			return graphView.m_nodeDetails.shape(m_inx);
		}
	}


	@Override
	public void setSelectedPaint(Paint paint) {
		synchronized (graphView.m_lock) {
			if (paint == null)
				throw new NullPointerException("paint is null");

			m_selectedPaint = paint;

			if (isSelected()) {
				graphView.m_nodeDetails.overrideFillPaint(m_inx, m_selectedPaint);

				if (m_selectedPaint instanceof Color)
					graphView.m_nodeDetails.overrideColorLowDetail(m_inx, (Color) m_selectedPaint);

				graphView.m_contentChanged = true;
			}
		}
	}


	@Override
	public Paint getSelectedPaint() {
		return m_selectedPaint;
	}


	@Override
	public void setUnselectedPaint(Paint paint) {
		synchronized (graphView.m_lock) {
			if (paint == null)
				throw new NullPointerException("paint is null");

			m_unselectedPaint = paint;

			if (!isSelected()) {
				graphView.m_nodeDetails.overrideFillPaint(m_inx, m_unselectedPaint);

				if (m_unselectedPaint instanceof Color) {
					m_unselectedPaint = new Color(((Color) m_unselectedPaint).getRed(),
							((Color) m_unselectedPaint).getGreen(), ((Color) m_unselectedPaint).getBlue(), transparency);
					graphView.m_nodeDetails.overrideColorLowDetail(m_inx, (Color) m_unselectedPaint);
				}

				graphView.m_contentChanged = true;
			}
		}
	}

	
	@Override
	public Paint getUnselectedPaint() {
		return m_unselectedPaint;
	}


	@Override
	public void setBorderPaint(Paint paint) {
		synchronized (graphView.m_lock) {
			m_borderPaint = paint;
			fixBorder();
			graphView.m_contentChanged = true;
		}
	}


	@Override
	public Paint getBorderPaint() {
		return m_borderPaint;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param width
	 *            DOCUMENT ME!
	 */
	@Override
	public void setBorderWidth(float width) {
		synchronized (graphView.m_lock) {
			graphView.m_nodeDetails.overrideBorderWidth(m_inx, width);
			graphView.m_contentChanged = true;
		}
	}


	@Override
	public float getBorderWidth() {
		synchronized (graphView.m_lock) {
			return graphView.m_nodeDetails.borderWidth(m_inx);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param stroke
	 *            DOCUMENT ME!
	 */
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
			graphView.m_nodeDetails.overrideBorderPaint(m_inx, m_borderPaint);
		else {
			final int size = (int) Math.max(1.0f, (int) (m_borderDash + m_borderDash2)); // Average
			// times
			// two.

			if ((size == graphView.m_lastSize) && (m_borderPaint == graphView.m_lastPaint)) {
				/* Use the cached texture paint. */} else {
				final BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
				final Graphics2D g2 = (Graphics2D) img.getGraphics();
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
				g2.setPaint(s_transparent);
				g2.fillRect(0, 0, size, size);
				g2.setPaint(m_borderPaint);
				g2.fillRect(0, 0, size / 2, size / 2);
				g2.fillRect(size / 2, size / 2, size / 2, size / 2);
				graphView.m_lastTexturePaint = new TexturePaint(img, new Rectangle2D.Double(0, 0, size, size));
				graphView.m_lastSize = size;
				graphView.m_lastPaint = m_borderPaint;
			}

			graphView.m_nodeDetails.overrideBorderPaint(m_inx, graphView.m_lastTexturePaint);
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

	/**
	 * DOCUMENT ME!
	 * 
	 * @param trans
	 *            DOCUMENT ME!
	 */
	@Override
	public void setTransparency(int trans) {
		synchronized (graphView.m_lock) {
			if (trans < 0 || trans > 255)
				throw new IllegalArgumentException("Transparency is out of range.");
			transparency = trans;

			if (m_unselectedPaint instanceof Color) {

				m_unselectedPaint = new Color(((Color) m_unselectedPaint).getRed(),
						((Color) m_unselectedPaint).getGreen(), ((Color) m_unselectedPaint).getBlue(), trans);

				graphView.m_nodeDetails.overrideFillPaint(m_inx, m_unselectedPaint);

				graphView.m_nodeDetails.overrideColorLowDetail(m_inx, (Color) m_unselectedPaint);
			}

			graphView.m_contentChanged = true;
		}

	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	public int getTransparency() {
		return transparency;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param width
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	public boolean setWidth(double width) {
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

			final double w = ((double) xMax) - xMin;
			final double h = ((double) graphView.m_extentsBuff[3]) - graphView.m_extentsBuff[1];

			if (!(Math.max(w, h) < (1.99d * Math.min(w, h))) && (getShape() == GraphGraphics.SHAPE_ROUNDED_RECTANGLE))
				setShape(NodeShapeVisualProperty.RECTANGLE);

			graphView.m_contentChanged = true;

			return true;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public double getWidth() {
		synchronized (graphView.m_lock) {
			if (!graphView.m_spacial.exists(m_inx, graphView.m_extentsBuff, 0))
				return -1.0d;

			return ((double) graphView.m_extentsBuff[2]) - graphView.m_extentsBuff[0];
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param height
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean setHeight(double height) {
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

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	public double getHeight() {
		synchronized (graphView.m_lock) {
			if (!graphView.m_spacial.exists(m_inx, graphView.m_extentsBuff, 0))
				return -1.0d;

			return ((double) graphView.m_extentsBuff[3]) - graphView.m_extentsBuff[1];
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	public Label getLabel() {
		return this;
	}

	/**
	 * FIXME: need to separate Label from this.
	 * 
	 * @param x
	 *            DOCUMENT ME!
	 * @param y
	 *            DOCUMENT ME!
	 */
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
		}
	}

	/**
	 * FIXME: remove label-related methods.
	 * 
	 * @return DOCUMENT ME!
	 */
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


	public void setYPosition(double yPos) {
		synchronized (graphView.m_lock) {
			final double hDiv2;
			final boolean nodeVisible = graphView.m_spacial.exists(m_inx, graphView.m_extentsBuff, 0);

			if (nodeVisible)
				hDiv2 = (((double) graphView.m_extentsBuff[3]) - graphView.m_extentsBuff[1]) / 2.0d;
			else
				hDiv2 = (double) (m_hiddenYMax - m_hiddenYMin) / 2.0d;

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
				// extents. Doing
				// this will mean that the node view will be properly scaled and
				// rotated
				// relative to the other nodes.
			} else {
				m_hiddenYMax = yMax;
				m_hiddenYMin = yMin;
			}
		}
	}


	public double getYPosition() {
		synchronized (graphView.m_lock) {
			if (graphView.m_spacial.exists(m_inx, graphView.m_extentsBuff, 0))
				return (((double) graphView.m_extentsBuff[1]) + graphView.m_extentsBuff[3]) / 2.0d;
			else
				return ((double) (m_hiddenYMin + m_hiddenYMax)) / 2.0d;
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
		if (m_selected)
			return false;

		m_selected = true;
		graphView.m_nodeDetails.overrideFillPaint(m_inx, m_selectedPaint);

		if (m_selectedPaint instanceof Color)
			graphView.m_nodeDetails.overrideColorLowDetail(m_inx, (Color) m_selectedPaint);

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
		if (!m_selected)
			return false;

		m_selected = false;
		graphView.m_nodeDetails.overrideFillPaint(m_inx, m_unselectedPaint);

		if (m_unselectedPaint instanceof Color)
			graphView.m_nodeDetails.overrideColorLowDetail(m_inx, (Color) m_unselectedPaint);

		graphView.m_selectedNodes.delete(m_inx);

		return true;
	}

	@Override
	public boolean isSelected() {
		return m_selected;
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

			graphView.m_nodeDetails.overrideShape(m_inx, dShape);
			graphView.m_contentChanged = true;
		}
	}


	@Override
	public void setToolTip(final String tip) {
		m_toolTipText = tip;
	}


	@Override
	public String getToolTip() {
		return m_toolTipText;
	}


	@Override
	public Paint getTextPaint() {
		synchronized (graphView.m_lock) {
			return graphView.m_nodeDetails.labelPaint(m_inx, 0);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param textPaint
	 *            DOCUMENT ME!
	 */
	@Override
	public void setTextPaint(Paint textPaint) {
		synchronized (graphView.m_lock) {
			graphView.m_nodeDetails.overrideLabelPaint(m_inx, 0, textPaint);
			graphView.m_contentChanged = true;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param threshold
	 *            DOCUMENT ME!
	 */
	public void setGreekThreshold(double threshold) {
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public String getText() {
		synchronized (graphView.m_lock) {
			return graphView.m_nodeDetails.labelText(m_inx, 0);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param text
	 *            DOCUMENT ME!
	 */
	public void setText(String text) {
		synchronized (graphView.m_lock) {
			graphView.m_nodeDetails.overrideLabelText(m_inx, 0, text);

			if (DEFAULT_LABEL_TEXT.equals(graphView.m_nodeDetails.labelText(m_inx, 0)))
				graphView.m_nodeDetails.overrideLabelCount(m_inx, 0);
			else
				graphView.m_nodeDetails.overrideLabelCount(m_inx, 1);

			graphView.m_contentChanged = true;
		}
	}

	/**
	 * Get label font type.
	 */
	@Override
	public Font getFont() {
		synchronized (graphView.m_lock) {
			return graphView.m_nodeDetails.labelFont(m_inx, 0);
		}
	}

	/**
	 * Set label font type.
	 */
	@Override
	public void setFont(Font font) {
		synchronized (graphView.m_lock) {
			graphView.m_nodeDetails.overrideLabelFont(m_inx, 0, font);
			graphView.m_contentChanged = true;
		}
	}

	/**
	 * Adds a given CustomGraphic, <EM>in draw order</EM>, to this DNodeView in
	 * a thread-safe way. Each CustomGraphic will be drawn in the order is was
	 * added. So, if you care about draw order (as for overlapping graphics),
	 * make sure you add them in the order you desire. Note that since
	 * CustomGraphics may be added by multiple plugins, your additions may be
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
			else {
				retVal = orderedCustomGraphicLayers.add(cg);
				// ????graphicsPositions.put(cg,
				// ObjectPositionImpl.DEFAULT_POSITION);
			}
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
	 * Return a non-null, read-only Iterator over all CustomGraphics contained
	 * in this DNodeView. The Iterator will return each CustomGraphic in draw
	 * order. The Iterator cannot be used to modify the underlying set of
	 * CustomGraphics.
	 * 
	 * @return The CustomGraphics Iterator. If no CustomGraphics are associated
	 *         with this DNOdeView, an empty Iterator is returned.
	 * @throws UnsupportedOperationException
	 *             if an attempt is made to use the Iterator's remove() method.
	 * @since Cytoscape 2.6
	 */
	public Iterator<CustomGraphic> customGraphicIterator() {
		final Iterable<CustomGraphic> toIterate;
		synchronized (CG_LOCK) {
			if (orderedCustomGraphicLayers == null) {
				toIterate = EMPTY_CUSTOM_GRAPHICS;
			} else {
				toIterate = orderedCustomGraphicLayers;
			}
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
			return graphView.m_nodeDetails.labelWidth(m_inx);
		}
	}

	public void setLabelWidth(double width) {
		synchronized (graphView.m_lock) {
			graphView.m_nodeDetails.overrideLabelWidth(m_inx, width);
			graphView.m_contentChanged = true;
		}
	}

	TexturePaint getNestedNetworkTexturePaint() {
		synchronized (graphView.m_lock) {
			++nestedNetworkPaintingDepth;
			try {
				if (nestedNetworkPaintingDepth > 1 || getModel().getNetwork() == null
						|| !nestedNetworkVisible)
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

	public void setNestedNetworkView(final DGraphView nestedNetworkView) {
		this.nestedNetworkView = nestedNetworkView;
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
					.overrideLabelTextAnchor(m_inx, 0, labelPosition.getAnchor().getConversionConstant());
			graphView.m_nodeDetails.overrideLabelNodeAnchor(m_inx, 0, labelPosition.getTargetAnchor()
					.getConversionConstant());
			graphView.m_nodeDetails.overrideLabelJustify(m_inx, 0, labelPosition.getJustify().getConversionConstant());
			graphView.m_nodeDetails.overrideLabelOffsetVectorX(m_inx, 0, labelPosition.getOffsetX());
			graphView.m_nodeDetails.overrideLabelOffsetVectorY(m_inx, 0, labelPosition.getOffsetY());

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
	public <T, V extends T> void setVisualProperty(final VisualProperty<? extends T> vpOriginal, final V value) {
		
		final VisualProperty<?> vp;
		final VisualLexiconNode treeNode = lexicon.getVisualLexiconNode(vpOriginal);
		if (treeNode == null)
			return;

		if (treeNode.getChildren().size() != 0) {
			// This is not leaf.
			final Collection<VisualLexiconNode> children = treeNode.getChildren();
			boolean shouldApply = false;
			for (VisualLexiconNode node : children) {
				if (node.isDepend()) {
					shouldApply = true;
					break;
				}
			}

			if (shouldApply == false)
				return;
		}

		if (treeNode.isDepend()) {
			// Do not use this. Parent will be applied.
			return;
		} else
			vp = vpOriginal;

		if (vp == DVisualLexicon.NODE_SHAPE) {
			setShape(((NodeShape) value));
		} else if (vp == DVisualLexicon.NODE_SELECTED_PAINT) {
			if(value == null)
				return;
			
			setSelectedPaint((Paint) value);
		} else if (vp == MinimalVisualLexicon.NODE_SELECTED) {
			setSelected((Boolean) value);
		} else if (vp == MinimalVisualLexicon.NODE_VISIBLE) {
			if (((Boolean) value).booleanValue())
				graphView.showGraphObject(this);
			else
				graphView.hideGraphObject(this);
		} else if (vp == MinimalVisualLexicon.NODE_FILL_COLOR) {
			if(value == null)
				return;
			
			setUnselectedPaint((Paint) value);
		} else if (vp == DVisualLexicon.NODE_BORDER_PAINT) {
			setBorderPaint((Paint) value);
		} else if (vp == DVisualLexicon.NODE_BORDER_WIDTH) {
			setBorderWidth(((Number) value).floatValue());
		} else if (vp == DVisualLexicon.NODE_BORDER_LINE_TYPE) {
			DLineType dLineType = DLineType.getDLineType((LineType) value);
			setBorder(dLineType.getStroke(this.getBorderWidth()));
		} else if (vp == DVisualLexicon.NODE_TRANSPARENCY) {
			setTransparency(((Integer) value));
		} else if (vp == MinimalVisualLexicon.NODE_WIDTH) {
			setWidth(((Number) value).doubleValue());
		} else if (vp == MinimalVisualLexicon.NODE_HEIGHT) {
			setHeight(((Number) value).doubleValue());
		} else if (vp == MinimalVisualLexicon.NODE_SIZE) {
			setWidth(((Number) value).doubleValue());
			setHeight(((Number) value).doubleValue());
		} else if (vp == MinimalVisualLexicon.NODE_LABEL) {
			setText(value.toString());
		} else if (vp == MinimalVisualLexicon.NODE_X_LOCATION) {
			setXPosition(((Number) value).doubleValue());
		} else if (vp == MinimalVisualLexicon.NODE_Y_LOCATION) {
			setYPosition(((Number) value).doubleValue());
		} else if (vp == DVisualLexicon.NODE_TOOLTIP) {
			setToolTip((String) value);
		} else if (vp == MinimalVisualLexicon.NODE_LABEL_COLOR) {
			setTextPaint((Paint) value);
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
		}
		visualProperties.put(vp, value);
	}

	private void applyCustomGraphics(final VisualProperty<?> vp, final CyCustomGraphics<CustomGraphic> customGraphics) {

		Set<CustomGraphic> dCustomGraphicsSet = cgMap.get(vp);
		if (dCustomGraphicsSet == null)
			dCustomGraphicsSet = new HashSet<CustomGraphic>();

		// ObjectPosition newPosition = null;
		for (final CustomGraphic cg : dCustomGraphicsSet) {
			// newPosition = this.graphicsPositions.get(cg);
			removeCustomGraphic(cg);
		}

		dCustomGraphicsSet.clear();

		if (customGraphics == null || customGraphics instanceof NullCustomGraphics)
			return;

		final List<Layer<CustomGraphic>> layers = customGraphics.getLayers();

		// No need to update
		if (layers == null || layers.size() == 0)
			return;

		// FIXME: size dependency
		// Check dependency. Sync size or not.
		final VisualProperty<?> cgSizeVP = DVisualLexicon.getAssociatedCustomGraphicsSizeVP(vp);
		final VisualLexiconNode sizeTreeNode = lexicon.getVisualLexiconNode(cgSizeVP);
		boolean sync = sizeTreeNode.isDepend();

		final VisualProperty<ObjectPosition> cgPositionVP = DVisualLexicon.getAssociatedCustomGraphicsPositionVP(vp);
		final ObjectPosition positionValue = getVisualProperty(cgPositionVP);
		// final ObjectPosition position = this.graphicsPositions.get(vp);

		// System.out.print("CG Position for: " +
		// cgPositionVP.getDisplayName());
		// System.out.println(" = " + positionValue);

		for (Layer<CustomGraphic> layer : layers) {
			// Assume it's a Ding layer
			CustomGraphic newCG = layer.getLayerObject();
			// if(newPosition != null) {
			// newCG = this.moveCustomGraphicsToNewPosition(newCG, newPosition);
			// System.out.println("MOVED: " + vp.getDisplayName());
			// }

			CustomGraphic finalCG = newCG;
			if (sync) {
				// Size is locked to node size.
				finalCG = syncSize(customGraphics, newCG, lexicon.getVisualLexiconNode(MinimalVisualLexicon.NODE_WIDTH)
						.isDepend());
			}
			// addCustomGraphic(resized);
			// dCustomGraphicsSet.add(resized);
			// } else {
			// addCustomGraphic(newCG);
			// dCustomGraphicsSet.add(newCG);
			// }

			finalCG = moveCustomGraphicsToNewPosition(finalCG, positionValue);

			addCustomGraphic(finalCG);
			dCustomGraphicsSet.add(finalCG);
		}
		// this.currentMap.put(dv, targets);

		// if(position != null &&
		// position.equals(ObjectPositionImpl.DEFAULT_POSITION) == false) {
		// // Transform
		// this.transformCustomGraphics(dCustomGraphicsSet, vp, position);
		// }

		cgMap.put(vp, dCustomGraphicsSet);

		// Flag this as used Custom Graphics
		// Cytoscape.getVisualMappingManager().getCustomGraphicsManager().setUsedInCurrentSession(graphics,
		// true);

		// System.out.println("CG Applyed: " + vp.getDisplayName());
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

		// System.out.println("Got associated CG = " + parent.getDisplayName());

		final Set<CustomGraphic> currentCG = cgMap.get(parent);

		if (currentCG == null || currentCG.size() == 0) {
			// Ignore if no CG is available.
			// System.out.println("    CG not found.  No need to update: " +
			// parent.getDisplayName());
			return;
		}

		final Set<CustomGraphic> newList = new HashSet<CustomGraphic>();
		for (CustomGraphic g : currentCG) {
			newList.add(moveCustomGraphicsToNewPosition(g, position));
			// this.removeCustomGraphic(g);
		}

		currentCG.clear();
		currentCG.addAll(newList);

		this.cgMap.put(parent, currentCG);
		// this.graphicsPositions.put(parent, position);

		// System.out.println("Position applied of CG = " + vp.getDisplayName()
		// + " <--- " + parent.getDisplayName());
	}

	private CustomGraphic syncSize(CyCustomGraphics<?> graphics, final CustomGraphic cg, final boolean whLock) {

		final double nodeW = this.getWidth();
		final double nodeH = this.getHeight();

		final Shape originalShape = cg.getShape();
		final Rectangle2D originalBounds = originalShape.getBounds2D();
		final double cgW = originalBounds.getWidth();
		final double cgH = originalBounds.getHeight();

		// In case size is same, return the original.
		if (nodeW == cgW && nodeH == cgH)
			return cg;

		// Check width/height lock status
		// final boolean whLock = dep.check(NODE_SIZE_LOCKED);

		final AffineTransform scale;
		final float fit = graphics.getFitRatio();

		if (whLock || graphics instanceof VectorCustomGraphics) {
			scale = AffineTransform.getScaleInstance(fit * nodeW / cgW, fit * nodeH / cgH);
		} else {
			// Case 1: node height value is larger than width
			if (nodeW >= nodeH) {
				scale = AffineTransform.getScaleInstance(fit * (nodeW / cgW) * (nodeH / nodeW), fit * nodeH / cgH);
				// scale = AffineTransform.getScaleInstance(nodeH/nodeW, 1);
			} else {
				scale = AffineTransform.getScaleInstance(fit * nodeW / cgW, fit * (nodeH / cgH) * (nodeW / nodeH));
				// scale = AffineTransform.getScaleInstance(1, nodeW/nodeH);
			}

		}
		return new CustomGraphic(scale.createTransformedShape(originalShape), cg.getPaintFactory());
	}

//	@Override
//	public <T> T getVisualProperty(final VisualProperty<T> vp) {
//		Object value = null;
//		
//		if (vp == DVisualLexicon.NODE_SHAPE) {
//			value = Integer.valueOf(getShape());
//		} else if (vp == DVisualLexicon.NODE_SELECTED_PAINT) {
//			value = getSelectedPaint();
//		} else if (vp == MinimalVisualLexicon.NODE_SELECTED) {
//			value = Boolean.valueOf(isSelected());
//		} else if (vp == MinimalVisualLexicon.NODE_VISIBLE) {
//			value = !graphView.isHidden(this);
//		} else if (vp == MinimalVisualLexicon.NODE_FILL_COLOR) {
//			value = getUnselectedPaint();
//		} else if (vp == DVisualLexicon.NODE_BORDER_PAINT) {
//			value = getBorderPaint();
//		} else if (vp == DVisualLexicon.NODE_BORDER_WIDTH) {
//			value = getBorderWidth();
//		} else if (vp == DVisualLexicon.NODE_BORDER_LINE_TYPE) {
//			value = this.getBorder();
//		} else if (vp == DVisualLexicon.NODE_TRANSPARENCY) {
//			value = getTransparency();
//		} else if (vp == MinimalVisualLexicon.NODE_WIDTH) {
//			value = getWidth();
//		} else if (vp == MinimalVisualLexicon.NODE_HEIGHT) {
//			value = getHeight();
//		} else if (vp == MinimalVisualLexicon.NODE_SIZE) {
//			value = getWidth();
//		} else if (vp == MinimalVisualLexicon.NODE_LABEL) {
//			value = getText();
//		} else if (vp == MinimalVisualLexicon.NODE_X_LOCATION) {
//			value = getXPosition();
//		} else if (vp == MinimalVisualLexicon.NODE_Y_LOCATION) {
//			value = getYPosition();
//		} else if (vp == DVisualLexicon.NODE_TOOLTIP) {
//			value = getToolTip();
//		} else if (vp == MinimalVisualLexicon.NODE_LABEL_COLOR) {
//			value = getTextPaint();
//		} else if (vp == DVisualLexicon.NODE_LABEL_FONT_FACE) {
//			value = getFont();
//		} else if (vp == DVisualLexicon.NODE_LABEL_FONT_SIZE) {
//			value = getFont().getSize();
//		} else if (vp == DVisualLexicon.NODE_LABEL_POSITION) {
//			value = getLabelPosition();
//		} else if (vp instanceof CustomGraphicsVisualProperty) {
//			// FIXME!
//		} else
//			value = vp.getDefault();
//		
//		return (T) value;
//	}
}
