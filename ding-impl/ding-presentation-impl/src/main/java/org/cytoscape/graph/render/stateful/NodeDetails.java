package org.cytoscape.graph.render.stateful;

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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.util.Collections;
import java.util.Map;

import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.property.values.Justification;
import org.cytoscape.view.presentation.property.values.Position;


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
	 * ******************* These are no longer used **********************
	 * Specifies that an anchor point lies at the center of a bounding box.
	 *
	public static final byte ANCHOR_CENTER = 0;

	/**
	 * Specifies that an anchor point lies on the north edge of a bounding box,
	 * halfway between the east and west edges.
	 *
	public static final byte ANCHOR_NORTH = 1;

	/**
	 * Specifies that an anchor point lies on the northeast corner of a bounding
	 * box.
	 *
	public static final byte ANCHOR_NORTHEAST = 2;

	/**
	 * Specifies that an anchor point lies on the east edge of a bounding box,
	 * halfway between the north and south edges.
	 *
	public static final byte ANCHOR_EAST = 3;

	/**
	 * Specifies that an anchor point lies on the southeast corner of a bounding
	 * box.
	 *
	public static final byte ANCHOR_SOUTHEAST = 4;

	/**
	 * Specifies that an anchor point lies on the south edge of a bounding box,
	 * halfway between the east and west edges.
	 *
	public static final byte ANCHOR_SOUTH = 5;

	/**
	 * Specifies that an anchor point lies on the southwest corner of a bounding
	 * box.
	 *
	public static final byte ANCHOR_SOUTHWEST = 6;

	/**
	 * Specifies that an anchor point lies on the west edge of a bounding box,
	 * halfway between the north and south edges.
	 *
	public static final byte ANCHOR_WEST = 7;

	/**
	 * Specifies that an anchor point lies on the northwest corner of a bounding
	 * box.
	 *
	public static final byte ANCHOR_NORTHWEST = 8;

	/**
	 * Used for range checking the anchor values.
	 *
	// Seems like these values should really be an enum...:
	public static final byte MAX_ANCHOR_VAL = 8;

	/**
	 * Specifies that the lines in a multi-line node label should each have a
	 * center point with similar X coordinate.
	 *
	public static final byte LABEL_WRAP_JUSTIFY_CENTER = 64;

	/**
	 * Specifies that the lines of a multi-line node label should each have a
	 * leftmost point with similar X coordinate.
	 *
	public static final byte LABEL_WRAP_JUSTIFY_LEFT = 65;

	/**
	 * Specifies that the lines of a multi-line node label should each have a
	 * rightmost point with similar X coordinate.
	 *
	public static final byte LABEL_WRAP_JUSTIFY_RIGHT = 66;
	*/
	
	private static final Stroke DEF_BORDER_STROKE = new BasicStroke(2.0f);

	public double getWidth(final CyNode node) {
		return 0.0;
	}

	public double getHeight(final CyNode node) {
		return 0.0;
	}
	
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
		return Color.RED;
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
		return Color.RED;
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
	public Position getLabelTextAnchor(final CyNode node, final int labelInx) {
		return Position.CENTER;
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
	public Position getLabelNodeAnchor(final CyNode node, final int labelInx) {
		return Position.CENTER;
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
	public Justification getLabelJustify(final CyNode node, final int labelInx) {
		return Justification.JUSTIFY_CENTER;
	}

	@SuppressWarnings("unchecked")
	public Map<VisualProperty<CyCustomGraphics>, CustomGraphicsInfo> getCustomGraphics(final CyNode node) {
		return Collections.EMPTY_MAP;
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
