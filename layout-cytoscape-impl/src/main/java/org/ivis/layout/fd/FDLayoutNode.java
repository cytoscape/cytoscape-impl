package org.ivis.layout.fd;

import java.awt.Point;
import java.awt.Dimension;

import org.ivis.layout.LNode;
import org.ivis.layout.LGraphManager;

/**
 * This class implements common data and functionality for nodes of all layout
 * styles that are force-directed.
 *
 * @author: Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public abstract class FDLayoutNode extends LNode
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/*
	 * Spring, repulsion and gravitational forces acting on this node
	 */
	public double springForceX;
	public double springForceY;
	public double repulsionForceX;
	public double repulsionForceY;
	public double gravitationForceX;
	public double gravitationForceY;

	/*
	 * Amount by which this node is to be moved in this iteration
	 */
	public double displacementX;
	public double displacementY;

	/**
	 * Start and finish grid coordinates that this node is fallen into
	 */
	public int startX;
	public int finishX;
	public int startY;
	public int finishY;
	
	/**
	 * Geometric neighbors of this node 
	 */
	public Object[] surrounding;
	
// -----------------------------------------------------------------------------
// Section: Constructors and initialization
// -----------------------------------------------------------------------------
	/*
	 * Constructor
	 */
	public FDLayoutNode(LGraphManager gm, Object vNode)
	{
		super(gm, vNode);
	}

	/**
	 * Alternative constructor
	 */
	public FDLayoutNode(LGraphManager gm, Point loc, Dimension size, Object vNode)
	{
		super(gm, loc, size, vNode);
	}

// -----------------------------------------------------------------------------
// Section: FR-Grid Variant Repulsion Force Calculation
// -----------------------------------------------------------------------------
	/**
	 * This method sets start and finish grid coordinates
	 */
	public void setGridCoordinates(int _startX, int _finishX, int _startY, int _finishY)
	{
		this.startX = _startX;
		this.finishX = _finishX;
		this.startY = _startY;
		this.finishY = _finishY;

	}

// -----------------------------------------------------------------------------
// Section: Remaining methods
// -----------------------------------------------------------------------------
	/*
	 * This method recalculates the displacement related attributes of this
	 * object. These attributes are calculated at each layout iteration once,
	 * for increasing the speed of the layout.
	 */
	abstract public void move();
	
	/*
	 * Abstract reset method that is designed to reset the force and displacement values 
	 * for this node object
	 */
	abstract public void reset();
}