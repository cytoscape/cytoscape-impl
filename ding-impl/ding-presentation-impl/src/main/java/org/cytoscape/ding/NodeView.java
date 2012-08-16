package org.cytoscape.ding;


import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Point2D;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.presentation.property.values.NodeShape;


/**
 * Legacy interface of ding node view.
 *
 */
public interface NodeView extends GraphViewObject {

	CyNode getCyNode();

	/**
	 * This sets the Paint that will be used by this node
	 * when it is painted as selected.
	 * @param paint The Paint to be used
	 */
	void setSelectedPaint(Paint paint) ;


	/**
	 * Set the default paint of this node
	 * @param paint the default Paint of this node
	 */
	void setUnselectedPaint(final Paint paint ) ;

	/**
	 * @param b_paint the paint the border will use
	 */
	void setBorderPaint( Paint b_paint ) ;

	
	/**
	 * @param border_width The width of the border.
	 */
	void setBorderWidth( float border_width ) ;


	/**
	 * @param stroke the new stroke for the border
	 */
	void setBorder(final Stroke stroke);


	/**
	 * @param trans new value for the transparency
	 */
	void setTransparency ( int trans );


	/**
	 * TODO: Reconcile with Border Methods
	 * @param width the currently set width of this node
	 */
	public boolean setWidth ( double width ) ;

	/**
	 * TODO: Reconcile with Border Methods
	 * @return the currently set width of this node
	 */
	public double getWidth () ;

	/**
	 * TODO: Reconcile with Border Methods
	 * @param height the currently set height of this node
	 */
	public boolean setHeight ( double height ) ;

	/**
	 * TODO: Reconcile with Border Methods
	 * @return the currently set height of this node
	 */
	public double getHeight () ;


	public void setOffset ( double x, double y );
	public Point2D getOffset ();

	/**
	 * @param new_x_position the new X position for this node
	 */
	public void setXPosition(double new_x_position) ;


	/**
	 * note that unless updateNode() has been called, this may not be
	 * the "real" location of this node
	 * @return the current x position of this node
	 * @see #setXPosition
	 */
	public double getXPosition() ;

	/**
	 * @param new_y_position the new Y position for this node
	 */
	public void setYPosition(double new_y_position) ;


	/**
	 * note that unless updateNode() has been called, this may not be
	 * the "real" location of this node
	 * @return the current y position of this node
	 * @see #setYPosition
	 */
	double getYPosition() ;


	/**
	 * This draws us as selected
	 */
	void select() ;

	/**
	 * This draws us as unselected
	 */
	void unselect() ;

	/**
	 *
	 */
	boolean isSelected() ;

	/**
	 *
	 */
	public boolean setSelected(boolean selected) ;

	/**
	 *  @return true if the NodeView is hidden, else false
	 */
	public boolean isHidden();

	/**
	 * Set a new shape for the Node, based on one of the pre-defined shapes
	 * <B>Note:</B> calling setPathTo( Shape ), allows one to define their own
	 * java.awt.Shape ( i.e. A picture of Johnny Cash )
	 */
	public void setShape(final NodeShape shape) ;

	/**
	 * Sets what the tooltip will be for this NodeView
	 */
	void setToolTip ( String tip );
	String getToolTip();
	
	void setLabelPosition(final ObjectPosition p);
	
}
