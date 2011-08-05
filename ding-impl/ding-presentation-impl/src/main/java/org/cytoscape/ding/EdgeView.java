package org.cytoscape.ding;


import java.awt.Paint;
import java.awt.Stroke;

import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.View;


public interface EdgeView  extends GraphViewObject {
	/**
	 * Draws splined curves for edges.
	 */
	public static int CURVED_LINES = 1;
  
	/**
	 * Draws straight lines for edges.
	 */
	public static int STRAIGHT_LINES = 2;

//	public static int NO_END = 0;
//	public static int WHITE_DELTA = 1;
//	public static int BLACK_DELTA = 2;
//	public static int EDGE_COLOR_DELTA = 3;
//	public static int WHITE_ARROW = 4;
//	public static int BLACK_ARROW = 5;
//	public static int EDGE_COLOR_ARROW = 6;
//	public static int WHITE_DIAMOND = 7;
//	public static int BLACK_DIAMOND = 8;
//	public static int EDGE_COLOR_DIAMOND = 9;
//	public static int WHITE_CIRCLE = 10;
//	public static int BLACK_CIRCLE = 11;
//	public static int EDGE_COLOR_CIRCLE = 12;
//	public static int WHITE_T = 13;
//	public static int BLACK_T = 14;
//	public static int EDGE_COLOR_T = 15;
//	public static int EDGE_HALF_ARROW_TOP = 16;
//	public static int EDGE_HALF_ARROW_BOTTOM = 17;
//	public static int EDGE_COLOR_HALF_TOP = 18;
//	public static int WHITE_HALF_TOP = 19;
//	public static int BLACK_HALF_TOP = 20;
//	public static int EDGE_COLOR_HALF_BOTTOM = 21;
//	public static int WHITE_HALF_BOTTOM = 22;
//	public static int BLACK_HALF_BOTTOM = 23;

	/**
	 * @return the index of this edge in the GraphPerspective
	 */
	public int getGraphPerspectiveIndex ();

	/**
	 * @return the index of this edge in the RootGraph
	 */
	public int getRootGraphIndex ();

	/**
	 * @return the Edge to which we are a view on
	 */
	public CyEdge getEdge();
	public View<CyEdge> getEdgeView();


	/**
	 * @param width set a new line width for this edge
	 */
	public void setStrokeWidth ( float width );

	/**
	 * @return the currently set edge width
	 */
	public float getStrokeWidth ();

	/**
	 * @param stroke the stroke to use on this edge
	 */
	public void setStroke ( Stroke stroke );

	/**
	 * @return the stroke used on this edge
	 */
	public Stroke getStroke ();

	/**
	 * @param line_type set a new line type for the edge
	 */
	public void setLineType ( int line_type );

	/**
	 * @return the currently set edge line type
	 */
	public int getLineType ();

	/**
	 * This really refers to the <B>Stroke</B>, 
	 * TODO: Make separte stroke methods
	 * @param paint the paint for this node
	 */
	public void setUnselectedPaint ( Paint paint );

	/**
	 * This really refers to the <B>Stroke</B>, 
	 * TODO: Make separte stroke methods
	 * @return the currently set edge Paint
	 */
	public Paint getUnselectedPaint ();

	/**
	 * This really refers to the <B>Stroke</B>, 
	 * TODO: Make separte stroke methods
	 * @param paint the paint for this node
	 */
	public void setSelectedPaint ( Paint paint );

	/**
	 * This really refers to the <B>Stroke</B>, 
	 * TODO: Make separte stroke methods
	 * @return the currently set edge Selectionpaint
	 */
	public Paint getSelectedPaint ();

	/**
	 * @return the currently set Source Edge End Type
	 */
	public Paint getSourceEdgeEndPaint ();

	/**
	 * @return the currently set Source Edge End Type
	 */
	public Paint getSourceEdgeEndSelectedPaint ();

	/**
	 * @return the currently set Target Edge End Type
	 */
	public Paint getTargetEdgeEndPaint ();

	/**
	 * @return the currently set Target Edge End Type
	 */
	public Paint getTargetEdgeEndSelectedPaint ();

	/**
	 * @param paint set the value for the source edge end  when selected
	 */
	public void setSourceEdgeEndSelectedPaint ( Paint paint );

	/**
	 * @param paint set the value for the target edge end  when selected
	 */
	public void setTargetEdgeEndSelectedPaint ( Paint paint );

	/**
	 * @param paint the new paint for the stroke of the source eged end
	 */
	public void setSourceEdgeEndStrokePaint ( Paint paint );

	/**
	 * @param paint the new paint for the stroke of the target eged end
	 */
	public void setTargetEdgeEndStrokePaint ( Paint paint );

	/**
	 * @param paint set the value for the source edge end 
	 */
	public void setSourceEdgeEndPaint ( Paint paint );
  
	/**
	 * @param paint set the value for the target edge end 
	 */
	public void setTargetEdgeEndPaint ( Paint paint );


	public void select ();

	public void unselect ();

	/**
	 * When we are selected then we draw ourselves red, and draw any handles.
	 */
	public boolean setSelected ( boolean state );

	/**
	 * @return selected state
	 */
	public boolean isSelected ();

	/**
	 * @return selected state
	 */
	public boolean getSelected ();

	/**
	 *  @return true if the EdgeView is hidden, else false
	 */
	public boolean isHidden();

	/**
	 * Add a PHandle to the edge at the point specified.
	 *
	 * @param pt The point at which to draw the PHandle and to which the
	 *        PHandle will be attached via the locator.
	 */
	//  public void addHandle ( Point2D pt );

	/**
	 * Removes the PHandle at the specified point.
	 *
	 * @param pt If this point intersects an existing PHandle, then remove that
	 *        PHandle.
	 */
	// public void removeHandle ( Point2D pt );

	/**
	 * Checks to see if a PHandle already exists for the given point.
	 *
	 * @param pt If this point intersects a currently existing PHandle, then
	 *        return true, else return false.
	 */
	//public boolean handleAlreadyExists ( Point2D pt );

	/**
	 * This is the main method called to update the drawing of the edge.
	 */
	public void updateEdgeView ();

	/**
	 * Draws the EdgeEnd, also sets the Source/Target Points to values such
	 * that the edge does not "go through" the end
	 */
	public void updateTargetArrow ();

	/**
	 * Draws the EdgeEnd, also sets the Source/Target Points to values such
	 * that the edge does not "go through" the end
	 */
	public void updateSourceArrow ();

	/**
	 * Sets the Drawing style for the edge end.
	 */
	public void setSourceEdgeEnd(int type);

	/**
	 * Sets the Drawing style for the edge end.
	 */
	public void setTargetEdgeEnd(int type);

  
	/**
	 * Return the Drawing style for the edge end.
	 */
	public int getSourceEdgeEnd();

	/**
	 * REturn the Drawing style for the edge end.
	 */
	public int getTargetEdgeEnd();

	/**
	 * Draws the Edge
	 */
	public void updateLine();

	/**
	 * Draws the edge as red and draws any handles previously added.
	 */
	public void drawSelected();

	/**
	 * Draws the edge as black and removes any handles from the display.
	 */
	public void drawUnselected();

	/**
	 * @return the Bend used 
	 */
	public Bend getBend ();
  
	public void clearBends ();

	public Label getLabel();

	/**
	 * Sets what the tooltip will be for this EdgeView
	 */
	public void setToolTip ( String tip );

	public void setLabelOffsetX(double x);
	public void setLabelOffsetY(double y);
	public void setEdgeLabelAnchor(int position);

	public double getLabelOffsetX();
	public double getLabelOffsetY();
	public int getEdgeLabelAnchor();
}
