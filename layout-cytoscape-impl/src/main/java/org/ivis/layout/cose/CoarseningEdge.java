package org.ivis.layout.cose;

/**
 * This class implements Coarsening Graph specific data and functionality for edges.
 *
 * @author Alper Karacelik
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class CoarseningEdge extends CoSEEdge
{
// -----------------------------------------------------------------------------
// Section: Constructors and initializations
// -----------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	public CoarseningEdge(CoSENode source, CoSENode target, Object vEdge)
	{
		super(source, target, vEdge);
	}
	
	public CoarseningEdge()
	{
		this(null, null, null);
	}
}
