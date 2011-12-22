package org.cytoscape.ding;

import java.awt.geom.Point2D;
import java.util.List;


/**
 * A class that encapsulates the representation of the bend used for a
 * particular EdgeView.
 * 
 * @author Mike Smoot (mes5k)
 */
public interface Bend {
	
//	List<Handle> getAllHandles();
//	void addHandle(final Handle handle);
//
//	void removeHandle(final int handleIndex);
//	void removeHandle(final Handle handle);
//	void removeAllHandles();
//
//	int getIndex(final Handle handle);

	/**
	 * Given a list of points removes all existing handles/handlePoints and adds
	 * new ones for those specified in the List.
	 * 
	 * @param bendPoints
	 *            A list of Point2Ds to create new handles.
	 */
	void setHandles(final List<Point2D> bendPoints);

	/**
	 * Returns a (new) List of clones of the Point2Ds that locate the handles.
	 */
	List<Point2D> getHandles();

	/**
	 * Moves the handle specified at the given index to the given point.
	 * 
	 * @param handleIndex
	 *            Index of the handle to move.
	 * @param handlePosition
	 *            Point2D to which to move the specified handle.
	 */
	void moveHandle(final int handleIndex, final Point2D handlePosition);

	/**
	 * Returns the handle Point2D closest to the source node.
	 */
	Point2D getSourceHandlePoint();

	/**
	 * Returns the handle Point2D closest to the target node.
	 */
	Point2D getTargetHandlePoint();

	/**
	 * Add a PHandle to the edge at the point specified. Acts as an interface to
	 * actuallyAddHandle() which does the actual adding.
	 * 
	 * @param handlePosition
	 *            The point at which to draw the PHandle and to which the
	 *            PHandle will be attached via the locator.
	 */
	void addHandle(final Point2D handlePosition);

	/**
	 * Add a PHandle to the edge at the point and index specified. Acts as an
	 * interface to actuallyAddHandle() which does the actual adding.
	 * 
	 * @param handleIndex
	 *            The index at which to add the PHandle to the list of handles.
	 * @param handlePosition
	 *            The point at which to draw the PHandle and to which the
	 *            PHandle will be attached via the locator.
	 */
	void addHandle(final int handleIndex, final Point2D handlePosition);

	/**
	 * Removes the PHandle at the specified point.
	 * 
	 * @param handlePosition
	 *            If this point intersects an existing PHandle, then remove that
	 *            PHandle.
	 */
	void removeHandle(final Point2D handlePosition);

	/**
	 * Removes the PHandle at the given index.
	 * 
	 * @param handleIndex
	 *            The index of the PHandle to remove.
	 */
	void removeHandle(final int handleIndex);

	/**
	 * Removes all handles
	 */
	void removeAllHandles();

	/**
	 * Checks to see if a PHandle already exists for the given point.
	 * 
	 * @param pt
	 *            If this point intersects a currently existing PHandle, then
	 *            return true, else return false.
	 */
	boolean handleAlreadyExists(final Point2D handlePosition);

	/**
	 * Draws any handles previously added.
	 */
	void drawSelected();

	/**
	 * Removes any handles from the display.
	 */
	void drawUnselected();

	/**
	 * Returns a list of points that define what gets drawn and hence what is
	 * visible to the user.
	 */
	Point2D[] getDrawPoints();
}
