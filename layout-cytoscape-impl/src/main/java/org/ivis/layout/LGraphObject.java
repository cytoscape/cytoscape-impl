package org.ivis.layout;

/**
 * This class implements a base class for l-level graph objects (nodes, edges,
 * and graphs).
 *
 * @author: Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class LGraphObject
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/**
	 * Associated view object
	 */
	public Object vGraphObject;

	/**
	 * Label
	 */
	public String label;
	
	/**
	 * Associated type
	 */
	public String type;
	
// -----------------------------------------------------------------------------
// Section: Constructors and initialization
// -----------------------------------------------------------------------------
	public LGraphObject(Object vGraphObject)
	{
		this.vGraphObject = vGraphObject;
	}
}