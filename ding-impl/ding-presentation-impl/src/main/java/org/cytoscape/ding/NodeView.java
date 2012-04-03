package org.cytoscape.ding;


import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.List;

import org.cytoscape.view.presentation.property.values.NodeShape;


/**
 * Legacy interface of ding node view.
 *
 */
public interface NodeView extends GraphViewObject {

	/**
	 * @return the index of this node in the perspective to which we are in a view on.
	 */
	long getGraphPerspectiveIndex();


	/**
	 * @return The list of EdgeViews connecting these two nodes. Possibly null.
	 */
	List<EdgeView> getEdgeViewsList(final NodeView otherNode);
	

	/**
	 * Shape is currently defined via predefined variables in
	 * the NodeView interface. To get the actual java.awt.Shape
	 * use getPathReference()
	 * 
	 * @return the current int-tpye shape
	 */
	int getShape() ;

	
	/**
	 * This sets the Paint that will be used by this node
	 * when it is painted as selected.
	 * @param paint The Paint to be used
	 */
	void setSelectedPaint(Paint paint) ;

	
	/**
	 * @return the currently set selection Paint
	 */
	Paint getSelectedPaint() ;

	
	/**
	 * Set the default paint of this node
	 * @param paint the default Paint of this node
	 */
	void setUnselectedPaint(final Paint paint ) ;

	
	/**
	 * @return the currently set paint
	 */
	Paint getUnselectedPaint();
	

	/**
	 * @param b_paint the paint the border will use
	 */
	void setBorderPaint( Paint b_paint ) ;

	/**
	 * @return the currently set BOrder Paint
	 */
	Paint getBorderPaint() ;

	/**
	 * @param border_width The width of the border.
	 */
	void setBorderWidth( float border_width ) ;

	/**
	 * @return the currently set Border width
	 */
	float getBorderWidth () ;


	/**
	 * @param stroke the new stroke for the border
	 */
	void setBorder(final Stroke stroke);


	/**
	 * @return the current border
	 */
	Stroke getBorder();

	/**
	 * @param trans new value for the transparency
	 */
	void setTransparency ( int trans );

	/**
	 * @return the value for the transparency for this node
	 */
	int getTransparency ();


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

	/**
	 * @return The Value of the label
	 */
	public Label getLabel ();

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
	
	ObjectPosition getLabelPosition();
	void setLabelPosition(final ObjectPosition p);
	
}
