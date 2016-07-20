package org.ivis.layout.fd;

import org.ivis.layout.LEdge;
import org.ivis.layout.LNode;

/**
 * This class implements common data and functionality for edges of all layout
 * styles that are force-directed.
 *
 * @author: Ugur Dogrusoz
 * 
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public abstract class FDLayoutEdge extends LEdge
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/**
	 * Desired length of this edge after layout
	 */
	public double idealLength = FDLayoutConstants.DEFAULT_EDGE_LENGTH;

// -----------------------------------------------------------------------------
// Section: Constructors and initialization
// -----------------------------------------------------------------------------
	/*
	 * Constructor
	 */
	public FDLayoutEdge(LNode source, LNode target, Object vEdge)
	{
		super(source, target, vEdge);
	}
}