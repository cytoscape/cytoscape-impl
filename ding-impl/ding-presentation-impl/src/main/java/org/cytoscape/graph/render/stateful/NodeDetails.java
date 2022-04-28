package org.cytoscape.graph.render.stateful;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.util.Collections;
import java.util.Map;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.View;
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
public interface NodeDetails {
	
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
	

	double getWidth(View<CyNode> node);

	double getHeight(View<CyNode> node);
	
	
	double getXPosition(View<CyNode> node);
	
	double getYPosition(View<CyNode> node);
	
	double getZPosition(View<CyNode> node);
	
	
	/**
	 * Returns the color of node in low detail rendering mode.
	 * <p>
	 * In low detail rendering mode, this is the only method from this class
	 * that is looked at. The rest of the methods in this class define visual
	 * properties that are used in full detail rendering mode. In low detail
	 * rendering mode translucent colors are not supported whereas in full
	 * detail rendering mode they are.
	 */
	Color getColorLowDetail(CyNetworkViewSnapshot snapshot, View<CyNode> node) ;

	/**
	 * Returns a GraphGraphics.SHAPE_* constant (or a custom node shape that an
	 * instance of GraphGraphics understands); this defines the shape that this
	 * node takes.
	 * Take note of certain constraints specified in
	 * GraphGraphics.drawNodeFull() that pertain to rounded rectangles.
	 */
	byte getShape(View<CyNode> node);

	/**
	 * Returns the paint of the interior of the node shape. It is an error to return null in this method.
	 */
	Paint getFillPaint(View<CyNode> node);

	/**
	 * Returns the border width of the node shape. 
	 * Take note of certain constraints specified in
	 * GraphGraphics.drawNodeFull().
	 */
	float getBorderWidth(View<CyNode> node);
	
	Stroke getBorderStroke(View<CyNode> node);

	/**
	 * Returns the paint of the border of the node shape. 
	 * This return value is ignored if borderWidth(node) returns
	 * zero; it is an error to return null if borderWidth(node) returns a value
	 * greater than zero.
	 */
	Paint getBorderPaint(View<CyNode> node);

	/**
	 * Returns a label's text. This
	 * method is only called by the rendering engine if labelCount(node) returns
	 * a value greater than zero. It is an error to return null if this method
	 * is called by the rendering engine.
	 * <p>
	 * To specify multiple lines of text in a node label, simply insert the '\n'
	 * character between lines of text.
	 */
	String getLabelText(View<CyNode> node);
	
	String getTooltipText(View<CyNode> nodeView);

	/**
	 * Returns the font to use when rendering this label. 
	 * This method is only called by the rendering engine
	 * if labelCount(node) returns a value greater than zero. It is an error to
	 * return null if this method is called by the rendering engine.
	 */
	Font getLabelFont(View<CyNode> node, boolean forPdf);
	
	default Font getLabelFont(View<CyNode> node) {
		return getLabelFont(node, false);
	}

  /**
   * Returns the angle (in degrees) to rotate the label.
	 * This method is only called by the rendering engine
	 * if labelCount(node) returns a value greater than zero. It is an error to
	 * return null if this method is called by the rendering engine.
	 */
	default double getLabelRotation(View<CyNode> node) { return 0d; }


	/**
	 * Returns the paint of a text label. 
	 * This method is only called by the rendering engine if
	 * labelCount(node) returns a value greater than zero. It is an error to
	 * return null if this method is called by the rendering engine.
	 */
	Paint getLabelPaint(View<CyNode> node);
	
	Paint getLabelBackgroundPaint(View<CyNode> node);
	
	byte getLabelBackgroundShape(View<CyNode> node);

	/**
	 * By returning one of the ANCHOR_* constants, specifies where on a text
	 * label's logical bounds box an anchor point lies. This <i>text anchor
	 * point</i> together with the node anchor point and label offset vector
	 * determines where, relative to the node, the text's logical bounds box is
	 * to be placed. The text's logical bounds box is placed such that the label
	 * offset vector plus the node anchor point equals the text anchor point.
	 * <p>
	 * This method is only
	 * called by the rendering engine if labelCount(node) returns a value
	 * greater than zero.
	 * 
	 * @see #ANCHOR_CENTER
	 * @see #getLabelNodeAnchor(int, int)
	 * @see #getLabelOffsetVectorX(int, int)
	 * @see #getLabelOffsetVectorY(int, int)
	 */
	Position getLabelTextAnchor(View<CyNode> node);

	/**
	 * By returning one of the ANCHOR_* constants, specifies where on the node's
	 * extents rectangle an anchor point lies. This <i>node anchor point</i>
	 * together with the text anchor point and label offset vector determines
	 * where, relative to the node, the text's logical bounds box is to be
	 * placed. The text's logical bounds box is placed such that the label
	 * offset vector plus the node anchor point equals the text anchor point.
	 * <p>
	 * This method is only
	 * called by the rendering engine if labelCount(node) returns a value
	 * greater than zero.
	 * 
	 * @see #ANCHOR_CENTER
	 * @see #getLabelTextAnchor(int, int)
	 * @see #getLabelOffsetVectorX(int, int)
	 * @see #getLabelOffsetVectorY(int, int)
	 */
	Position getLabelNodeAnchor(View<CyNode> node);

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
	 * @see #getLabelOffsetVectorY(int, int)
	 * @see #getLabelTextAnchor(int, int)
	 * @see #getLabelNodeAnchor(int, int)
	 */
	default float getLabelOffsetVectorX(View<CyNode> node) {
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
	 * @see #getLabelOffsetVectorX(int, int)
	 * @see #getLabelTextAnchor(int, int)
	 * @see #getLabelNodeAnchor(int, int)
	 */
	default float getLabelOffsetVectorY(View<CyNode> node) {
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
	default Justification getLabelJustify(View<CyNode> node) {
		return Justification.JUSTIFY_CENTER;
	}

	@SuppressWarnings("rawtypes")
	default Map<VisualProperty<CyCustomGraphics>, CustomGraphicsInfo> getCustomGraphics(View<CyNode> node) {
		return Collections.emptyMap();
	}
	
	/**
	 * Specifies the X component of the vector that separates the location of a
	 * rendered graphic from the node's anchor point for that graphic. By
	 * default this method always returns zero. This method is only called by
	 * the rendering engine if graphicCount(node) returns a value greater than
	 * zero.
	 * 
	 * @see #graphicOffsetVectorY(int, int)
	 * @see #graphicNodeAnchor(int, int)
	 */
	default float graphicOffsetVectorX(View<CyNode> node) {
		return 0.0f;
	}

	/**
	 * Specifies the Y component of the vector that separates the location of a
	 * rendered graphic from the node's anchor point for that graphic. By
	 * default this method always returns zero. This method is only called by
	 * the rendering engine if graphicCount(node) returns a value greater than
	 * zero.
	 * 
	 * @see #graphicOffsetVectorX(int, int)
	 * @see #graphicNodeAnchor(int, int)
	 */
	default float graphicOffsetVectorY(View<CyNode> node) {
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
	default Object customGraphicsLock(View<CyNode> node) {
		return this;
	}

	/**
	 * Returns the label width of the node. By default this method returns 100.
	 * Take note of certain constraints specified in
	 * GraphGraphics.drawNodeFull().
	 */
	default double getLabelWidth(View<CyNode> node) {
		return 100.0;
	}

	/**
	 * Child class should ovrride this method to render correct Nexted Network Image.
	 */
	default TexturePaint getNestedNetworkTexturePaint(CyNetworkViewSnapshot netView, View<CyNode> node) {
		return null;
	}

	boolean isSelected(View<CyNode> nodeView);

	Integer getBorderTransparency(View<CyNode> nodeView);
}
