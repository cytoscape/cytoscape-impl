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
package org.cytoscape.graph.render.stateful;

import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Defines visual properties of a node modulo the node size and location Even
 * though this class is not declared abstract, in most situations it makes sense
 * to override at least some of its methods in order to gain control over node
 * visual properties.
 * <p>
 * To understand the significance of each method's return value, it makes sense
 * to become familiar with the API cytoscape.render.immed.GraphGraphics.
 */
public class NodeDetails {
	/**
	 * Specifies that an anchor point lies at the center of a bounding box.
	 */
	public static final byte ANCHOR_CENTER = 0;

	/**
	 * Specifies that an anchor point lies on the north edge of a bounding box,
	 * halfway between the east and west edges.
	 */
	public static final byte ANCHOR_NORTH = 1;

	/**
	 * Specifies that an anchor point lies on the northeast corner of a bounding
	 * box.
	 */
	public static final byte ANCHOR_NORTHEAST = 2;

	/**
	 * Specifies that an anchor point lies on the east edge of a bounding box,
	 * halfway between the north and south edges.
	 */
	public static final byte ANCHOR_EAST = 3;

	/**
	 * Specifies that an anchor point lies on the southeast corner of a bounding
	 * box.
	 */
	public static final byte ANCHOR_SOUTHEAST = 4;

	/**
	 * Specifies that an anchor point lies on the south edge of a bounding box,
	 * halfway between the east and west edges.
	 */
	public static final byte ANCHOR_SOUTH = 5;

	/**
	 * Specifies that an anchor point lies on the southwest corner of a bounding
	 * box.
	 */
	public static final byte ANCHOR_SOUTHWEST = 6;

	/**
	 * Specifies that an anchor point lies on the west edge of a bounding box,
	 * halfway between the north and south edges.
	 */
	public static final byte ANCHOR_WEST = 7;

	/**
	 * Specifies that an anchor point lies on the northwest corner of a bounding
	 * box.
	 */
	public static final byte ANCHOR_NORTHWEST = 8;

	/**
	 * Used for range checking the anchor values.
	 */
	// Seems like these values should really be an enum...:
	public static final byte MAX_ANCHOR_VAL = 8;

	/**
	 * Specifies that the lines in a multi-line node label should each have a
	 * center point with similar X coordinate.
	 */
	public static final byte LABEL_WRAP_JUSTIFY_CENTER = 64;

	/**
	 * Specifies that the lines of a multi-line node label should each have a
	 * leftmost point with similar X coordinate.
	 */
	public static final byte LABEL_WRAP_JUSTIFY_LEFT = 65;

	/**
	 * Specifies that the lines of a multi-line node label should each have a
	 * rightmost point with similar X coordinate.
	 */
	public static final byte LABEL_WRAP_JUSTIFY_RIGHT = 66;
	
	private static final Stroke DEF_BORDER_STROKE = new BasicStroke(2.0f);

	/**
	 * Hashmap which records selected state of nodes - information used by
	 * GraphRenderer.renderGraph() to render selected nodes on top of unselected
	 * nodes.
	 */
	Map<Integer, Boolean> selectedMap = new HashMap<Integer, Boolean>();

	/**
	 * Returns the color of node in low detail rendering mode. By default this
	 * method returns Color.red. It is an error to return null in this method.
	 * <p>
	 * In low detail rendering mode, this is the only method from this class
	 * that is looked at. The rest of the methods in this class define visual
	 * properties that are used in full detail rendering mode. In low detail
	 * rendering mode translucent colors are not supported whereas in full
	 * detail rendering mode they are.
	 */
	public Color getColorLowDetail(final CyNode node) {
		return Color.red;
	}

	/**
	 * Returns a GraphGraphics.SHAPE_* constant (or a custom node shape that an
	 * instance of GraphGraphics understands); this defines the shape that this
	 * node takes. By default this method returns GraphGraphics.SHAPE_RECTANGLE.
	 * Take note of certain constraints specified in
	 * GraphGraphics.drawNodeFull() that pertain to rounded rectangles.
	 */
	public byte getShape(final CyNode node) {
		return GraphGraphics.SHAPE_RECTANGLE;
	}

	/**
	 * Returns the paint of the interior of the node shape. By default this
	 * method returns Color.red. It is an error to return null in this method.
	 */
	public Paint getFillPaint(final CyNode node) {
		return Color.red;
	}

	/**
	 * Returns the border width of the node shape. By default this method
	 * returns zero. Take note of certain constraints specified in
	 * GraphGraphics.drawNodeFull().
	 */
	public float getBorderWidth(final CyNode node) {
		return 0.0f;
	}
	
	public Stroke getBorderStroke(final CyNode node) {
		return DEF_BORDER_STROKE;
	}

	/**
	 * Returns the paint of the border of the node shape. By default this method
	 * returns null. This return value is ignored if borderWidth(node) returns
	 * zero; it is an error to return null if borderWidth(node) returns a value
	 * greater than zero.
	 */
	public Paint getBorderPaint(final CyNode node) {
		return Color.DARK_GRAY;
	}

	/**
	 * Returns the number of labels that this node has. By default this method
	 * returns zero.
	 */
	public int getLabelCount(final CyNode node) {
		return 0;
	}

	/**
	 * Returns a label's text. By default this method always returns null. This
	 * method is only called by the rendering engine if labelCount(node) returns
	 * a value greater than zero. It is an error to return null if this method
	 * is called by the rendering engine.
	 * <p>
	 * To specify multiple lines of text in a node label, simply insert the '\n'
	 * character between lines of text.
	 * 
	 * @param labelInx
	 *            a value in the range [0, labelCount(node)-1] indicating which
	 *            node label in question.
	 */
	public String getLabelText(final CyNode node, final int labelInx) {
		return null;
	}

	/**
	 * Returns the font to use when rendering this label. By default this method
	 * always returns null. This method is only called by the rendering engine
	 * if labelCount(node) returns a value greater than zero. It is an error to
	 * return null if this method is called by the rendering engine.
	 * 
	 * @param labelInx
	 *            a value in the range [0, labelCount(node)-1] indicating which
	 *            node label in question.
	 */
	public Font getLabelFont(final CyNode node, final int labelInx) {
		return null;
	}

	/**
	 * Returns an additional scaling factor that is to be applied to the font
	 * used to render this label; this scaling factor, applied to the point size
	 * of the font returned by labelFont(node, labelInx), yields a new virtual
	 * font that is used to render the text label. By default this method always
	 * returns 1.0. This method is only called by the rendering engine if
	 * labelCount(node) returns a value greater than zero.
	 * 
	 * @param labelInx
	 *            a value in the range [0, labelCount(node)-1] indicating which
	 *            node label in question.
	 */
	public double labelScaleFactor(final CyNode node, final int labelInx) {
		return 1.0d;
	}

	/**
	 * Returns the paint of a text label. By default this method always returns
	 * null. This method is only called by the rendering engine if
	 * labelCount(node) returns a value greater than zero. It is an error to
	 * return null if this method is called by the rendering engine.
	 * 
	 * @param labelInx
	 *            a value in the range [0, labelCount(node)-1] indicating which
	 *            node label in question.
	 */
	public Paint getLabelPaint(final CyNode node, final int labelInx) {
		return Color.DARK_GRAY;
	}

	/**
	 * By returning one of the ANCHOR_* constants, specifies where on a text
	 * label's logical bounds box an anchor point lies. This <i>text anchor
	 * point</i> together with the node anchor point and label offset vector
	 * determines where, relative to the node, the text's logical bounds box is
	 * to be placed. The text's logical bounds box is placed such that the label
	 * offset vector plus the node anchor point equals the text anchor point.
	 * <p>
	 * By default this method always returns ANCHOR_CENTER. This method is only
	 * called by the rendering engine if labelCount(node) returns a value
	 * greater than zero.
	 * 
	 * @param labelInx
	 *            a value in the range [0, labelCount(node)-1] indicating which
	 *            node label in question.
	 * @see #ANCHOR_CENTER
	 * @see #getLabelNodeAnchor(int, int)
	 * @see #getLabelOffsetVectorX(int, int)
	 * @see #getLabelOffsetVectorY(int, int)
	 */
	public byte getLabelTextAnchor(final CyNode node, final int labelInx) {
		return ANCHOR_CENTER;
	}

	/**
	 * By returning one of the ANCHOR_* constants, specifies where on the node's
	 * extents rectangle an anchor point lies. This <i>node anchor point</i>
	 * together with the text anchor point and label offset vector determines
	 * where, relative to the node, the text's logical bounds box is to be
	 * placed. The text's logical bounds box is placed such that the label
	 * offset vector plus the node anchor point equals the text anchor point.
	 * <p>
	 * By default this method always returns ANCHOR_CENTER. This method is only
	 * called by the rendering engine if labelCount(node) returns a value
	 * greater than zero.
	 * 
	 * @param labelInx
	 *            a value in the range [0, labelCount(node)-1] indicating which
	 *            node label in question.
	 * @see #ANCHOR_CENTER
	 * @see #getLabelTextAnchor(int, int)
	 * @see #getLabelOffsetVectorX(int, int)
	 * @see #getLabelOffsetVectorY(int, int)
	 */
	public byte getLabelNodeAnchor(final CyNode node, final int labelInx) {
		return ANCHOR_CENTER;
	}

	/**
	 * Specifies the X component of the vector that separates a text anchor
	 * point from a node anchor point. This <i>label offset vector</i> together
	 * with the text anchor point and node anchor point determines where,
	 * relative to the node, the text's logical bounds box is to be placed. The
	 * text's logical bounds box is placed such that the label offset vector
	 * plus the node anchor point equals the text anchor point.
	 * <p>
	 * By default this method always returns zero. This method is only called by
	 * the rendering engine if labelCount(node) returns a value greater than
	 * zero.
	 * 
	 * @param labelInx
	 *            a value in the range [0, labelCount(node)-1] indicating which
	 *            node label in question.
	 * @see #getLabelOffsetVectorY(int, int)
	 * @see #getLabelTextAnchor(int, int)
	 * @see #getLabelNodeAnchor(int, int)
	 */
	public float getLabelOffsetVectorX(final CyNode node, final int labelInx) {
		return 0.0f;
	}

	/**
	 * Specifies the Y component of the vector that separates a text anchor
	 * point from a node anchor point. This <i>label offset vector</i> together
	 * with the text anchor point and node anchor point determines where,
	 * relative to the node, the text's logical bounds box is to be placed. The
	 * text's logical bounds box is placed such that the label offset vector
	 * plus the node anchor point equals the text anchor point.
	 * <p>
	 * By default this method always returns zero. This method is only called by
	 * the rendering engine if labelCount(node) returns a value greater than
	 * zero.
	 * 
	 * @param labelInx
	 *            a value in the range [0, labelCount(node)-1] indicating which
	 *            node label in question.
	 * @see #getLabelOffsetVectorX(int, int)
	 * @see #getLabelTextAnchor(int, int)
	 * @see #getLabelNodeAnchor(int, int)
	 */
	public float getLabelOffsetVectorY(final CyNode node, final int labelInx) {
		return 0.0f;
	}

	/**
	 * By returning one of the LABEL_WRAP_JUSTIFY_* constants, determines how to
	 * justify a node label spanning multiple lines. The choice made here does
	 * not affect the size of the logical bounding box of a node label's text.
	 * The lines of text are justified within that logical bounding box.
	 * <p>
	 * By default this method always returns LABEL_WRAP_JUSTIFY_CENTER. This
	 * return value is ignored if labelText(node, labelInx) returns a text
	 * string that does not span multiple lines.
	 * 
	 * @see #LABEL_WRAP_JUSTIFY_CENTER
	 */
	public byte getLabelJustify(final CyNode node, final int labelInx) {
		return LABEL_WRAP_JUSTIFY_CENTER;
	}

	/**
	 * Specifies the X component of the vector that separates the location of a
	 * rendered graphic from the node's anchor point for that graphic. By
	 * default this method always returns zero. This method is only called by
	 * the rendering engine if graphicCount(node) returns a value greater than
	 * zero.
	 * 
	 * @param graphicInx
	 *            a value in the range [0, graphicCount(node)-1] indicating
	 *            which node graphic in question.
	 * @see #graphicOffsetVectorY(int, int)
	 * @see #graphicNodeAnchor(int, int)
	 */
	public float graphicOffsetVectorX(final CyNode node, final int graphicInx) {
		return 0.0f;
	}

	/**
	 * Specifies the Y component of the vector that separates the location of a
	 * rendered graphic from the node's anchor point for that graphic. By
	 * default this method always returns zero. This method is only called by
	 * the rendering engine if graphicCount(node) returns a value greater than
	 * zero.
	 * 
	 * @param graphicInx
	 *            a value in the range [0, graphicCount(node)-1] indicating
	 *            which node graphic in question.
	 * @see #graphicOffsetVectorX(int, int)
	 * @see #graphicNodeAnchor(int, int)
	 */
	public float graphicOffsetVectorY(final CyNode node, final int graphicInx) {
		return 0.0f;
	}

	/**
	 * A thread-safe method returning the number of custom graphics associated
	 * with a given Node. If none are associated, zero is returned. NOTE: This
	 * method should be abstract, but since it isn't, any real use should
	 * override this method in a subclass.
	 * 
	 * @see #getCustomGraphics(int)
	 * @since Cytoscape 2.6
	 */
	public int getCustomGraphicCount(final CyNode node) {
		return 0;
	}

	/**
	 * Return a non-null, read-only Iterator over all CustomGraphicLayers contained
	 * in this Node. The Iterator will return each CustomGraphicLayer in draw order.
	 * The Iterator cannot be used to modify the underlying set of
	 * CustomGraphicLayers. NOTE: This method should be abstract, but since it isn't,
	 * any real use should override this method in a subclass.
	 * 
	 * @return The CustomGraphicLayers Iterator. If no CustomGraphicLayers are associated
	 *         with this Node, an empty Iterator is returned.
	 * @throws UnsupportedOperationException
	 *             if an attempt is made to use the Iterator's remove() method.
	 * @since Cytoscape 2.6
	 * @see #customGraphicsLock(int)
	 */
	// Should probably be getCustomGraphics(), but all the methods
	// seem to have this form.
	public Iterator<CustomGraphicLayer> getCustomGraphics(final CyNode node) {
		return new Iterator<CustomGraphicLayer>() {
			private Iterator<CustomGraphicLayer> _iterator = new ArrayList<CustomGraphicLayer>(0).iterator();

			public boolean hasNext() {
				return _iterator.hasNext();
			}

			public CustomGraphicLayer next() {
				return _iterator.next();
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Return the object used for synchronizing custom graphics operations for a
	 * given Node. This is used in conjunction with the customGraphics()
	 * Iterator to allow iteration over the custom graphics without fear of the
	 * underlying CustomGraphicLayers mutating. For example:
	 * 
	 * <PRE>
	 *    NodeDetails nd = ...;
	 *    synchronized (nd.customGraphicsLock(node)) {
	 *       Iterator<CustomGraphicLayer> dNodeIt = nodeDetails.customGraphics (node);
	 *       CustomGraphicLayer cg = null;
	 *       while (dNodeIt.hasNext()) {
	 *          cg = dNodeIt.next();
	 *          // DO STUFF WITH cg HERE.
	 *       }
	 *    }
	 * </PRE>
	 * 
	 * NOTE: This method should be abstract, but since it isn't, any real use
	 * should override this method in a subclass.
	 * 
	 * @since Cytoscape 2.6
	 */
	public Object customGraphicsLock(final CyNode node) {
		return this;
	}

	/**
	 * Used to set selected state of given node.
	 * 
	 * @param node
	 *            Integer
	 * @param selected
	 *            Boolean
	 */
	public void setSelected(Integer node, Boolean selected) {
		selectedMap.put(node, selected);
	}

	/**
	 * Used to get selected state of given node. If node does not exist in map,
	 * false is returned.
	 * 
	 * Used in GraphRenderer.renderGraph() to provide rendering of selected
	 * nodes above unselected nodes.
	 * 
	 * @return boolean
	 */
	public boolean getSelected(Integer node) {
		if (selectedMap.get(node) != null) {
			return selectedMap.get(node);
		}
		return false;
	}

	/**
	 * Returns the label width of the node. By default this method returns 100.
	 * Take note of certain constraints specified in
	 * GraphGraphics.drawNodeFull().
	 */
	public double getLabelWidth(final CyNode node) {
		return 100.0;
	}

	/**
	 * Child class should ovrride this method to render correct Nexted Network
	 * Image.
	 * 
	 * @param node
	 * @return
	 */
	public TexturePaint getNestedNetworkTexturePaint(final CyNode node) {
		return null;
	}
}
