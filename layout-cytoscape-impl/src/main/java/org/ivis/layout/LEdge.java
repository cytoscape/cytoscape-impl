package org.ivis.layout;

import java.util.*;

import org.ivis.util.*;

/**
 * This class represents an edge (l-level) for layout purposes.
 *
 * @author Erhan Giral
 * @author Ugur Dogrusoz
 * @author Cihan Kucukkececi
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class LEdge extends LGraphObject
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/*
	 * Source and target nodes of this edge
	 */
	protected LNode source;
	protected LNode target;

	/*
	 * Whether this edge is an intergraph one
	 */
	protected boolean isInterGraph;

	/*
	 * The length of this edge ( l = sqrt(x^2 + y^2) )
	 */
	protected double length;
	protected double lengthX;
	protected double lengthY;

	/*
	 * Whether source and target node rectangles intersect, requiring special
	 * treatment
	 */
	protected boolean isOverlapingSourceAndTarget = false;

	/*
	 * Bend points for this edge
	 */
	protected List<PointD> bendpoints;

	/*
	 * Lowest common ancestor graph (lca), and source and target nodes in lca
	 */
	protected LGraph lca;
	protected LNode sourceInLca;
	protected LNode targetInLca;

// -----------------------------------------------------------------------------
// Section: Constructors and initializations
// -----------------------------------------------------------------------------
	/*
	 * Constructor
	 */
	protected LEdge(LNode source, LNode target, Object vEdge)
	{
		super(vEdge);

		this.bendpoints = new ArrayList();

		this.source = source;
		this.target = target;
	}

// -----------------------------------------------------------------------------
// Section: Accessors
// -----------------------------------------------------------------------------
	/**
	 * This method returns the source node of this edge.
	 */
	public LNode getSource()
	{
		return this.source;
	}

	/**
	 * This method sets the source node of this edge.
	 */
	public void setSource(LNode source)
	{
		this.source = source;
	}

	/**
	 * This method returns the target node of this edge.
	 */
	public LNode getTarget()
	{
		return this.target;
	}

	/**
	 * This method sets the target node of this edge.
	 */
	public void setTarget(LNode target)
	{
		this.target = target;
	}

	/**
	 * This method returns whether or not this edge is an inter-graph edge.
	 */
	public boolean isInterGraph()
	{
		return this.isInterGraph;
	}

	/**
	 * This method returns the length of this edge. Note that this value might
	 * be out-dated at times during a layout operation.
	 */
	public double getLength()
	{
		return this.length;
	}

	/**
	 * This method returns the x component of the length of this edge. Note that
	 * this value might be out-dated at times during a layout operation.
	 */
	public double getLengthX()
	{
		return this.lengthX;
	}

	/**
	 * This method returns the y component of the length of this edge. Note that
	 * this value might be out-dated at times during a layout operation.
	 */
	public double getLengthY()
	{
		return this.lengthY;
	}

	/**
	 * This method returns whether or not this edge has overlapping source and
	 * target.
	 */
	public boolean isOverlapingSourceAndTarget()
	{
		return this.isOverlapingSourceAndTarget;
	}

	/**
	 * This method resets the overlapping source and target status of this edge.
	 */
	public void resetOverlapingSourceAndTarget()
	{
		this.isOverlapingSourceAndTarget = false;
	}

	/**
	 * This method returns the list of bend points of this edge.
	 */
	public List<PointD> getBendpoints()
	{
		return this.bendpoints;
	}

	/**
	 * This method clears all existing bendpoints and sets given bendpoints as 
	 * the new ones.
	 */
	public void reRoute(List<PointD> bendPoints)
	{
		this.bendpoints.clear();
		
		this.bendpoints.addAll(bendPoints);
	}

	public LGraph getLca()
	{
		return this.lca;
	}

	public LNode getSourceInLca()
	{
		return this.sourceInLca;
	}

	public LNode getTargetInLca()
	{
		return this.targetInLca;
	}

// -----------------------------------------------------------------------------
// Section: Remaining methods
// -----------------------------------------------------------------------------
	/**
	 * This method returns the end of this edge different from the input one.
	 */
	public LNode getOtherEnd(LNode node)
	{
		if (this.source.equals(node))
		{
			return this.target;
		}
		else if (this.target.equals(node))
		{
			return this.source;
		}
		else
		{
			throw new IllegalArgumentException(
				"Node is not incident " + "with this edge");
		}
	}

	/**
	 * This method finds the other end of this edge, and returns its ancestor
	 * node, possibly the other end node itself, that is in the input graph. It
	 * returns null if none of its ancestors is in the input graph.
	 */
	public LNode getOtherEndInGraph(LNode node, LGraph graph)
	{
		LNode otherEnd = this.getOtherEnd(node);
		LGraph root = graph.getGraphManager().getRoot();

		while (true)
		{
			if (otherEnd.getOwner() == graph)
			{
				return otherEnd;
			}

			if (otherEnd.getOwner() == root)
			{
				break;
			}

			otherEnd = otherEnd.getOwner().getParent();
		}

		return null;
	}

	/**
	 * This method updates the length of this edge as well as whether or not the
	 * rectangles representing the geometry of its end nodes overlap.
	 */
	public void updateLength()
	{
		double[] clipPointCoordinates = new double[4];

		this.isOverlapingSourceAndTarget =
			IGeometry.getIntersection(this.target.getRect(),
				this.source.getRect(),
				clipPointCoordinates);

		if (!this.isOverlapingSourceAndTarget)
		{
			// target clip point minus source clip point gives us length

			this.lengthX = clipPointCoordinates[0] - clipPointCoordinates[2];
			this.lengthY = clipPointCoordinates[1] - clipPointCoordinates[3];

			if (Math.abs(this.lengthX) < 1.0)
			{
				this.lengthX = IMath.sign(this.lengthX);
			}

			if (Math.abs(this.lengthY) < 1.0)
			{
				this.lengthY = IMath.sign(this.lengthY);
			}

			this.length = Math.sqrt(
				this.lengthX * this.lengthX + this.lengthY * this.lengthY);
		}
	}

	/**
	 * This method updates the length of this edge using the end nodes centers
	 * as opposed to clipping points to simplify calculations involved.
	 */
	public void updateLengthSimple()
	{
		// target center minus source center gives us length

		this.lengthX = this.target.getCenterX() - this.source.getCenterX();
		this.lengthY = this.target.getCenterY() - this.source.getCenterY();

		if (Math.abs(this.lengthX) < 1.0)
		{
			this.lengthX = IMath.sign(this.lengthX);
		}

		if (Math.abs(this.lengthY) < 1.0)
		{
			this.lengthY = IMath.sign(this.lengthY);
		}

		this.length = Math.sqrt(
			this.lengthX * this.lengthX + this.lengthY * this.lengthY);
	}

// -----------------------------------------------------------------------------
// Section: Testing methods
// -----------------------------------------------------------------------------
	/**
	 * This method prints the topology of this edge.
	 */
	public void printTopology()
	{
		System.out.print( (this.label == null ? "?" : this.label) + "[" +
			(this.source.label == null ? "?" : this.source.label) + "-" +
			(this.target.label == null ? "?" : this.target.label) + "] ");
	}
}