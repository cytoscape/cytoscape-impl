package org.ivis.layout.cose;

import org.ivis.layout.LGraphManager;
import org.ivis.layout.LNode;

/**
 * This class holds coarsening process specific node data and implementations
 *
 * @author Alper Karacelik
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class CoarseningNode extends LNode
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------

	/**
	 * A coarsening node in G (coarsening graph) 
	 * references a CoSENode in M (CoSE graph manager)
	 */
	private CoSENode reference;
	
	/**
	 * node1 and node2 hold the contracted nodes
	 */
	private CoarseningNode node1;
	private CoarseningNode node2;
	/**
	 * matched flag of the coarsening node
	 */
	private boolean matched;
	
	/**
	 * weight
	 */
	private int weight;
	
// -----------------------------------------------------------------------------
// Section: Constructors and initialization
// -----------------------------------------------------------------------------
	
	/*
	 * Constructor
	 */
	protected CoarseningNode(LGraphManager gm, Object vNode)
	{
		super(gm, vNode);
		this.weight = 1;
	}

	public CoarseningNode()
	{
		this(null, null);
	}
	
// -----------------------------------------------------------------------------
// Section: Getters and setter
// -----------------------------------------------------------------------------
	
	public void setMatched(boolean matched)
	{
		this.matched = matched;
	}

	public boolean isMatched()
	{
		return matched;
	}

	public void setWeight(int weight)
	{
		this.weight = weight;
	}

	public int getWeight()
	{
		return weight;
	}

	public void setNode1(CoarseningNode node1)
	{
		this.node1 = node1;
	}

	public CoarseningNode getNode1()
	{
		return node1;
	}

	public void setNode2(CoarseningNode node2)
	{
		this.node2 = node2;
	}

	public CoarseningNode getNode2()
	{
		return node2;
	}

	public void setReference(CoSENode reference)
	{
		this.reference = reference;
	}

	public CoSENode getReference()
	{
		return reference;
	}

// -----------------------------------------------------------------------------
// Section: Other methods
// -----------------------------------------------------------------------------
	/**
	 * This method returns the matching of this node
	 * if this node does not have any unmacthed neighbor then returns null
	 */
	public CoarseningNode getMatching ( )
	{
		CoarseningNode minWeighted = null;
		int minWeight = Integer.MAX_VALUE;

		for (Object obj: this.getNeighborsList())
		{
			CoarseningNode v = (CoarseningNode) obj;
			
			if ((!v.isMatched()) && (v != this) && (v.getWeight() < minWeight))
			{
				minWeighted = v;
				minWeight = v.getWeight();
			}
		}

		return minWeighted;
	}
}
