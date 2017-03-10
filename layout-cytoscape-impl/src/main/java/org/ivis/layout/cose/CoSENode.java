package org.ivis.layout.cose;

import java.util.Iterator;
import java.awt.Point;
import java.awt.Dimension;

import org.ivis.layout.*;
import org.ivis.layout.fd.FDLayoutNode;
import org.ivis.layout.sbgn.SbgnProcessNode.Orientation;
import org.ivis.util.IMath;

/**
 * This class implements CoSE specific data and functionality for nodes.
 *
 * @author Erhan Giral
 * @author Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class CoSENode extends FDLayoutNode
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/**
	 * This node is constructed by contracting pred1 and pred2 from Mi-1
	 * next is constructed by contracting this node and another node from Mi
	 */
	private CoSENode pred1;
	private CoSENode pred2;
	private CoSENode next;
	
	/**
	 * Processed flag for CoSENode is needed during the coarsening process
	 * a node can be the next node of two different nodes. 
	 * so it can already be processed during the coarsening process
	 */
	private boolean processed;
	
	/**
	 * added for comparison to sbgn pd layout
	 * TODO - you may remove this
	 */
	public double OKCount = 0;
	
	/**
	 * added for comparison to sbgn pd layout
	 * TODO - you may remove this
	 */
	public Orientation orient;

	/**
	 * added for comparison to sbgn pd layout
	 * TODO - you may remove this
	 */
	public boolean isOrientationProper;
// -----------------------------------------------------------------------------
// Section: Constructors and initialization
// -----------------------------------------------------------------------------
	/*
	 * Constructor
	 */
	public CoSENode(LGraphManager gm, Object vNode)
	{
		super(gm, vNode);
	}

	/**
	 * Alternative constructor
	 */
	public CoSENode(LGraphManager gm, Point loc, Dimension size, Object vNode)
	{
		super(gm, loc, size, vNode);
	}

// -----------------------------------------------------------------------------
// Section: Remaining methods
// -----------------------------------------------------------------------------
	/*
	 * This method recalculates the displacement related attributes of this
	 * object. These attributes are calculated at each layout iteration once,
	 * for increasing the speed of the layout.
	 */
	public void move()
	{
		CoSELayout layout = (CoSELayout) this.graphManager.getLayout();
		double maxNodeDisplacement = layout.coolingFactor * layout.maxNodeDisplacement;

		this.displacementX = layout.coolingFactor *
			(this.springForceX + this.repulsionForceX + this.gravitationForceX);
		this.displacementY = layout.coolingFactor *
			(this.springForceY + this.repulsionForceY + this.gravitationForceY);

		if (Math.abs(this.displacementX) > maxNodeDisplacement)
		{
			this.displacementX = maxNodeDisplacement * IMath.sign(this.displacementX);
		}

		if (Math.abs(this.displacementY) > maxNodeDisplacement)
		{
			this.displacementY = maxNodeDisplacement * IMath.sign(this.displacementY);
		}

		// Apply simulated annealing here
//		if (Math.random() < CoSELayout.annealingProbability && CoSELayout.simulatedAnnealingOn)
//		{
//			this.displacementX = -this.displacementX;
//			this.displacementY = -this.displacementY;
//
//			CoSELayout.randomizedMovementCount++;
//		}
//		else
//		{
//			CoSELayout.nonRandomizedMovementCount++;
//		}

		if (this.child == null)
		// a simple node, just move it
		{
			this.moveBy(this.displacementX, this.displacementY);
		}
		else if (this.child.getNodes().size() == 0)
		// an empty compound node, again just move it
		{
			this.moveBy(this.displacementX, this.displacementY);
		}
		// non-empty compound node, propogate movement to children as well
		else
		{
			this.propogateDisplacementToChildren(this.displacementX,
				this.displacementY);
		}

//		System.out.printf("\t%s@[%5.1f,%5.1f] s=(%5.1f,%5.1f) r=(%5.1f,%5.1f) g=(%5.1f,%5.1f)\n",
//			new Object [] {this.label,
//			this.getLeft(), this.getTop(),
//			this.springForceX, this.springForceY,
//			this.repulsionForceX, this.repulsionForceY,
//			this.gravitationForceX, this.gravitationForceY});

		layout.totalDisplacement +=
			Math.abs(this.displacementX) + Math.abs(this.displacementY);
	}

	/*
	 * This method applies the transformation of a compound node (denoted as
	 * root) to all the nodes in its children graph
	 */
	public void propogateDisplacementToChildren(double dX, double dY)
	{
		Iterator nodeIter = this.getChild().getNodes().iterator();

		while (nodeIter.hasNext())
		{
			CoSENode lNode = (CoSENode) nodeIter.next();

			if (lNode.getChild() == null)
			{
				lNode.moveBy(dX, dY);
				lNode.displacementX += dX;
				lNode.displacementY += dY;
			}
			else
			{
				lNode.propogateDisplacementToChildren(dX, dY);
			}
		}
	}

	/*
	 * This method resets the forces acting on this node object and the displacement value
	 */
	public void reset()
	{
		this.springForceX = 0;
		this.springForceY = 0;
		this.repulsionForceX = 0;
		this.repulsionForceY = 0;
		this.gravitationForceX = 0;
		this.gravitationForceY = 0;
		this.displacementX = 0;
		this.displacementY = 0;
	}
		
// -----------------------------------------------------------------------------
// Section: Getters and setters
// -----------------------------------------------------------------------------
	public void setPred1(CoSENode pred1)
	{
		this.pred1 = pred1;
	}

	public CoSENode getPred1()
	{
		return pred1;
	}

	public void setPred2(CoSENode pred2)
	{
		this.pred2 = pred2;
	}

	public CoSENode getPred2()
	{
		return pred2;
	}

	public void setNext(CoSENode next)
	{
		this.next = next;
	}

	public CoSENode getNext()
	{
		return next;
	}

	public void setProcessed(boolean processed)
	{
		this.processed = processed;
	}

	public boolean isProcessed()
	{
		return processed;
	}
}