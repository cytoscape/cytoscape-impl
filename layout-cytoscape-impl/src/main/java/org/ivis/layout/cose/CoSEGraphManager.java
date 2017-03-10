package org.ivis.layout.cose;

import java.util.ArrayList;
import java.util.HashMap;

import org.ivis.layout.LEdge;
import org.ivis.layout.LGraphManager;
import org.ivis.layout.LNode;
import org.ivis.layout.Layout;

/**
 * This class implements a graph-manager for CoSE layout specific data and
 * functionality.
 *
 * @author Alper Karacelik
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class CoSEGraphManager extends LGraphManager
{
// -----------------------------------------------------------------------------
// Section: Constructors and initialization
// -----------------------------------------------------------------------------
	public CoSEGraphManager(Layout layout)
	{
		super(layout);
	}
// -----------------------------------------------------------------------------
// Section: Coarsening
// -----------------------------------------------------------------------------
	/**
	 * This method returns a list of CoSEGraphManager. 
	 * Returned list holds graphs finer to coarser (M0 to Mk)
	 * Additionally, this method is only called by M0.
	 */
	public ArrayList<CoSEGraphManager> coarsenGraph()
	{
		// MList holds graph managers from M0 to Mk
		ArrayList<CoSEGraphManager> MList = new ArrayList<CoSEGraphManager>();
		int prevNodeCount;
		int currNodeCount;
		
		// "this" graph manager holds the finest (input) graph
		MList.add(this);
		
		// coarsening graph G holds only the leaf nodes and the edges between them 
		// which are considered for coarsening process
		CoarseningGraph G = new CoarseningGraph(this.getLayout());
		
		// construct G0
		convertToCoarseningGraph((CoSEGraph)this.getRoot(), G);
		currNodeCount = G.getNodes().size();

		CoSEGraphManager lastM, newM;
		// if two graphs Gi and Gi+1 have the same order, 
		// then Gi = Gi+1 is the coarsest graph (Gk), so stop coarsening process
		do {
			prevNodeCount = currNodeCount;

			// coarsen Gi
			G.coarsen();

			// get current coarsest graph lastM = Mi and construct newM = Mi+1
			lastM = MList.get((MList.size()-1));
			newM = coarsen(lastM);
			
			MList.add(newM);
			currNodeCount = G.getNodes().size();

		} while ((prevNodeCount != currNodeCount) && (currNodeCount > 1));

		// change currently being used graph manager
		this.getLayout().setGraphManager(this);
		
		MList.remove( MList.size()-1 );
		return MList;
	}
	
	/**
	 * This method converts given CoSEGraph to CoarseningGraph G0
	 * G0 consists of leaf nodes of CoSEGraph and edges between them
	 */
	private void convertToCoarseningGraph(CoSEGraph coseG, CoarseningGraph G)
	{
		// we need a mapping between nodes in M0 and G0, for constructing the edges of G0
		HashMap map = new HashMap();

		// construct nodes of G0
		for (Object obj: coseG.getNodes())
		{
			CoSENode v = (CoSENode) obj;
			// if current node is compound, 
			// then make a recursive call with child graph of current compound node 
			if (v.getChild() != null)
			{
				convertToCoarseningGraph((CoSEGraph)v.getChild(), G);
			}
			// otherwise current node is a leaf, and should be in the G0
			else
			{
				// v is a leaf node in CoSE graph, and is referenced by u in G0
				CoarseningNode u = new CoarseningNode();
				u.setReference(v);
				
				// construct a mapping between v (from CoSE graph) and u (from coarsening graph)
				map.put(v, u);
				
				G.add( u );
			}
		}

		// construct edges of G0
		for (Object obj: coseG.getEdges())
		{
			LEdge e = (LEdge) obj;
			// if neither source nor target of e is a compound node
			// then, e is an edge between two leaf nodes
			if ((e.getSource().getChild() == null) && (e.getTarget().getChild() == null))
			{
				G.add(new CoarseningEdge(), (LNode)map.get(e.getSource()), (LNode)map.get(e.getTarget()) );
			}
		}
	}

	/**
	 * This method gets Mi (lastM) and coarsens to Mi+1
	 * Mi+1 is returned.
	 */
	private CoSEGraphManager coarsen(CoSEGraphManager lastM)
	{
		// create Mi+1 and root graph of it
		CoSEGraphManager newM = new CoSEGraphManager(lastM.getLayout());
		
		// change currently being used graph manager
		newM.getLayout().setGraphManager(newM);
		newM.addRoot();
		
		newM.getRoot().vGraphObject = lastM.getRoot().vGraphObject;

		// construct nodes of the coarser graph Mi+1
		this.coarsenNodes((CoSEGraph)lastM.getRoot(), (CoSEGraph)newM.getRoot());

		// change currently being used graph manager
		lastM.getLayout().setGraphManager(lastM);
		
		// add edges to the coarser graph Mi+1
		this.addEdges(lastM, newM);

		return newM;
	}

	/**
	 * This method coarsens nodes of Mi and creates nodes of the coarser graph Mi+1
	 * g: Mi, coarserG: Mi+1
	 */
	private void coarsenNodes(CoSEGraph g, CoSEGraph coarserG)
	{
		for (Object obj: g.getNodes())
		{
			CoSENode v = (CoSENode) obj;
			// if v is compound
			// then, create the compound node v.next with an empty child graph
			// and, make a recursive call with v.child (Mi) and v.next.child (Mi+1)
			if (v.getChild() != null)
			{
				v.setNext((CoSENode) coarserG.getGraphManager().getLayout().newNode(null));
				coarserG.getGraphManager().add(coarserG.getGraphManager().getLayout().newGraph(null), 
					v.getNext());
				v.getNext().setPred1(v);
				coarserG.add(v.getNext());
				
				//v.getNext().getChild().vGraphObject = v.getChild().vGraphObject;
				
				coarsenNodes ((CoSEGraph) v.getChild(), (CoSEGraph) v.getNext().getChild());
			}
			else
			{
				// v.next can be referenced by two nodes, so first check if it is processed before
				if (!v.getNext().isProcessed())
				{
					coarserG.add( v.getNext() );
					v.getNext().setProcessed(true);
				}
			}
			
			//v.getNext().vGraphObject = v.vGraphObject;
			
			// set location
			v.getNext().setLocation(v.getLocation().x, v.getLocation().y);
			v.getNext().setHeight(v.getHeight());
			v.getNext().setWidth(v.getWidth());
		}	
	}

	/**
	 * This method adds edges to the coarser graph.
	 * It should be called after coarsenNodes method is executed
	 * lastM: Mi, newM: Mi+1
	 */
	private void addEdges(CoSEGraphManager lastM, CoSEGraphManager newM)
	{
		for (Object obj: lastM.getAllEdges())
		{
			LEdge e = (LEdge) obj;
			// if e is an inter-graph edge or source or target of e is compound 
			// then, e has not contracted during coarsening process. Add e to the coarser graph.			
			if ( (e.isInterGraph()) || 
				(e.getSource().getChild() != null) || 
				(e.getTarget().getChild() != null) )
			{
				// check if e is not added before
				if ( ! ((CoSENode)e.getSource()).getNext().getNeighborsList().
					contains(((CoSENode)e.getTarget()).getNext()) )
				{
					newM.add(newM.getLayout().newEdge(null), 
						((CoSENode)e.getSource()).getNext(), 
						((CoSENode)e.getTarget()).getNext());
				}
			}

			// otherwise, if e is not contracted during coarsening process
			// then, add it to the  coarser graph
			else
			{
				if (((CoSENode)e.getSource()).getNext() != ((CoSENode)e.getTarget()).getNext())
				{
					// check if e is not added before
					if ( ! ((CoSENode)e.getSource()).getNext().getNeighborsList().
						contains(((CoSENode)e.getTarget()).getNext()) )
					{
						newM.add(newM.getLayout().newEdge(null), 
							((CoSENode)e.getSource()).getNext(), 
							((CoSENode)e.getTarget()).getNext());
					}
				}
			}
		}
	}

	
}
