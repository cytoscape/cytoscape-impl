package org.cytoscape.ding;

import java.awt.geom.Point2D;

/**
 * A class that encapsulates the representation of the bend used for a 
 * particular EdgeView.
 *
 * @author Mike Smoot (mes5k) 
 */
public interface  Bend {

  /**
   * Given a list of points removes all existing handles/handlePoints 
	 * and adds new ones for those specified in the List.
	 * @param bendPoints A list of Point2Ds to create new handles.
   */
	public void setHandles( java.util.List bendPoints );

	/**
	 * Returns a (new) List of clones of the Point2Ds that locate the handles.
	 */
	public java.util.List<Point2D> getHandles();

	/**
	 *  Moves the handle specified at the given index to the given point.
	 *  @param i Index of the handle to move.
	 *  @param pt Point2D to which to move the specified handle.
	 */
	public void moveHandle( int i, Point2D pt );

  /**
   * Returns the handle Point2D closest to the source node.
   */
  public Point2D getSourceHandlePoint ();

  /**
   * Returns the handle Point2D closest to the target node.
   */
  public Point2D getTargetHandlePoint ();

  /**
   * Add a PHandle to the edge at the point specified. Acts as
   * an interface to actuallyAddHandle() which does the actual adding.
   *
   * @param pt The point at which to draw the PHandle and to which the
   *        PHandle will be attached via the locator.
   */
  public void addHandle ( Point2D pt );

  /**
   * Add a PHandle to the edge at the point and index specified. Acts as
   * an interface to actuallyAddHandle() which does the actual adding.
   *
   * @param insertIndex The index at which to add the PHandle to the 
   * list of handles.
   * @param pt The point at which to draw the PHandle and to which the
   *        PHandle will be attached via the locator.
   */
	public void addHandle ( int insertIndex , Point2D pt );


  /**
   * Removes the PHandle at the specified point.
   *
   * @param pt If this point intersects an existing PHandle, then remove that
   *        PHandle.
   */
  public void removeHandle ( Point2D pt );

 

  /**
   * Removes the PHandle at the given index. 
   *
   * @param i The index of the PHandle to remove. 
   */
  public void removeHandle ( int i );


   /**
   * Removes all handles
   */
  public void removeAllHandles () ;

  /**
   * Checks to see if a PHandle already exists for the given point.
   *
   * @param pt If this point intersects a currently existing PHandle, then
   *        return true, else return false.
   */
  public boolean handleAlreadyExists ( Point2D pt );
	
	/**
	 * Draws any handles previously added.
	 */
	public void drawSelected();

	/**
	 * Removes any handles from the display.
	 */
	public void drawUnselected();

	/**
	 * Returns a list of points that define what gets drawn and hence
	 * what is visible to the user.
	 */
	public Point2D[] getDrawPoints();
}
