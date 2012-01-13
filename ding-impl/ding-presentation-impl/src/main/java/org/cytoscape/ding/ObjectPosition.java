package org.cytoscape.ding;

/**
 * Interface representing relative location of graphics objects,
 * such as labels or custom graphics.
 * 
 *
 */
public interface ObjectPosition {
	
	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Position getAnchor();

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Position getTargetAnchor();

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Justification getJustify();

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public double getOffsetX();

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public double getOffsetY();

	/**
	 *  DOCUMENT ME!
	 *
	 * @param b DOCUMENT ME!
	 */
	public void setAnchor(Position position);

	/**
	 *  DOCUMENT ME!
	 *
	 * @param b DOCUMENT ME!
	 */
	public void setTargetAnchor(Position position);

	/**
	 *  DOCUMENT ME!
	 *
	 * @param b DOCUMENT ME!
	 */
	public void setJustify(Justification position);

	/**
	 *  DOCUMENT ME!
	 *
	 * @param d DOCUMENT ME!
	 */
	public void setOffsetX(double d);

	/**
	 *  DOCUMENT ME!
	 *
	 * @param d DOCUMENT ME!
	 */
	public void setOffsetY(double d);

	
	/**
	 * Create a short String to be used by reader/writer.
	 * @return serialized string.
	 */
	String toSerializableString();
}