package org.ivis.layout.cose;

import org.ivis.layout.LGraph;
import org.ivis.layout.LNode;
import org.ivis.layout.Layout;

/**
 * This class holds coarsening process specific graph data and implementations
 *
 * @author: Alper Karacelik
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class CoarseningGraph extends LGraph
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/**
	 * during the coarsening process, 
	 * CoSE nodes of coarser graph is created by the help of layout instance 
	 */
	private Layout layout;
	
// -----------------------------------------------------------------------------
// Section: Constructors and initialization
// -----------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	protected CoarseningGraph(LNode parent, Layout layout, Object vGraph)
	{
		super(parent, layout, vGraph);
	}
	
	public CoarseningGraph(Layout _layout)
	{
		this(null, _layout, null);
		this.layout = _layout;
	}

// -----------------------------------------------------------------------------
// Section: Coarsening
// -----------------------------------------------------------------------------
	/**
	 * This method coarsens Gi to Gi+1
	 */
	public void coarsen()
	{
		this.unmatchAll();
		
		CoarseningNode v, u;
		
		if (this.getNodes().size() > 0)
		{
			// match each node with the one of the unmatched neighbors has minimum weight
			// if there is no unmatched neighbor, then match current node with itself
			while (!((CoarseningNode) this.getNodes().get(0)).isMatched())
			{
				// get an unmatched node (v) and (if exists) matching of it (u).
				v = (CoarseningNode) this.getNodes().get(0);
				u = v.getMatching();
				
				// node t is constructed by contracting u and v
				contract( v, u );
			}
			
			// construct pred1, pred2, next fields of referenced node from CoSEGraph
			for ( Object obj: this.getNodes() )
			{
				CoarseningNode y = (CoarseningNode) obj;
				
				// new CoSE node will be in Mi+1
				CoSENode z = (CoSENode) this.layout.newNode(null);
				
				z.setPred1(y.getNode1().getReference());
				y.getNode1().getReference().setNext(z);
				
				// if current node is not matched with itself
				if ( y.getNode2() != null )
				{
					z.setPred2(y.getNode2().getReference());
					y.getNode2().getReference().setNext(z);
				}
				
				y.setReference(z);
			}
		}
	}
	
	/**
	 * This method unflags all nodes as unmatched
	 * it should be called before each coarsening process
	 */
	private void unmatchAll()
	{
		for ( Object obj: this.getNodes() )
		{
			CoarseningNode v = (CoarseningNode) obj;
			v.setMatched(false);
		}
	}
	
	/**
	 * This method contracts v and u
	 */
	private void contract( CoarseningNode v, CoarseningNode u)
	{
		// t will be constructed by contracting v and u		
		CoarseningNode t = new CoarseningNode();
		this.add(t);
		
		t.setNode1( v );
		for ( Object obj: v.getNeighborsList() )
		{
			CoarseningNode x = (CoarseningNode) obj;
			if (x != t)
			{
				this.add( new CoarseningEdge(), t, x );
			}
		}
		t.setWeight( v.getWeight() );
		
		//remove contracted node from the graph
		this.remove(v);
		
		// if v has an unmatched neighbor, then u is not null and t.node2 = u
		// otherwise, leave t.node2 as null
		if ( u != null )
		{
			t.setNode2( u );
			for ( Object obj: u.getNeighborsList() )
			{
				CoarseningNode x = (CoarseningNode) obj;
				if (x != t)
				{
					add( new CoarseningEdge(), t, x );
				}
			}
			t.setWeight( t.getWeight() + u.getWeight() );
			
			//remove contracted node from the graph
			this.remove(u);
		}
		
		// t should be flagged as matched
		t.setMatched( true );
	}
	
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	public Layout getLayout()
	{
		return layout;
	}

	public void setLayout(Layout layout)
	{
		this.layout = layout;
	}
}
