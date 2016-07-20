package org.ivis.layout;

import java.util.*;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * This class represents a graph (l-level) for layout purposes. A graph
 * maintains a list of nodes and (intra-graph) edges. An l-level graph is always
 * a child of an l-level compound node. The root of the compound graph structure
 * is a child of the root node, which is the only node in a compound structure
 * without an owner graph.
 *
 * @author Erhan Giral
 * @author Ugur Dogrusoz
 * @author Cihan Kucukkececi
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class LGraph extends LGraphObject
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/*
	 * Nodes maintained by this graph
	 */
	private List nodes;

	/*
	 * Edges whose source and target nodes are in this graph
	 */
	private List edges;

	/*
	 * Owner graph manager
	 */
	private LGraphManager graphManager;

	/*
	 * Parent node of this graph. This should never be null (the parent of the
	 * root graph is the root node) when this graph is part of a compound
	 * structure (i.e. a graph manager).
	 */
	protected LNode parent;

	/*
	 * Geometry of this graph (i.e. that of its tightest bounding rectangle,
	 * also taking margins into account)
	 */
	private int top;
	private int left;
	private int bottom;
	private int right;

	/*
	 * Estimated size of this graph based on estimated sizes of its contents
	 */
	protected int estimatedSize = Integer.MIN_VALUE;

	/*
	 * Margins of this graph to be applied on bouding rectangle of its contents
	 */
	private int margin = LayoutConstants.DEFAULT_GRAPH_MARGIN;

	/*
	 * Whether the graph is connected or not, taking indirect edges (e.g. an
	 * edge connecting a child node of a node of this graph to another node of
	 * this graph) into account.
	 */
	private boolean isConnected;

// -----------------------------------------------------------------------------
// Section: Constructors and initialization
// -----------------------------------------------------------------------------
	/*
	 * Constructor
	 */
	protected LGraph(LNode parent, LGraphManager graphMgr, Object vGraph)
	{
		super(vGraph);
		this.initialize();
		this.parent = parent;
		this.graphManager = graphMgr;
	}

	/*
	 * Alternative constructor
	 */
	protected LGraph(LNode parent, Layout layout, Object vGraph)
	{
		super(vGraph);
		this.initialize();
		this.parent = parent;
		this.graphManager = layout.graphManager;
	}

	private void initialize()
	{
		this.edges = new ArrayList();
		this.nodes = new ArrayList();
		this.isConnected = false;
	}

// -----------------------------------------------------------------------------
// Section: Accessors
// -----------------------------------------------------------------------------
	/**
	 * This method returns the list of nodes in this graph.
	 */
	public List getNodes()
	{
		return nodes;
	}

	/**
	 * This method returns the list of edges in this graph.
	 */
	public List getEdges()
	{
		return edges;
	}

	/**
	 * This method returns the graph manager of this graph.
	 */
	public LGraphManager getGraphManager()
	{
		return graphManager;
	}

	/**
	 * This method returns the parent node of this graph. If this graph is the
	 * root of the nesting hierarchy, then null is returned.
	 */
	public LNode getParent()
	{
		return parent;
	}

	/**
	 * This method returns the left of the bounds of this graph. Notice that
	 * bounds are not always up-to-date.
	 */
	public int getLeft()
	{
		return this.left;
	}

	/**
	 * This method returns the right of the bounds of this graph. Notice that
	 * bounds are not always up-to-date.
	 */
	public int getRight()
	{
		return this.right;
	}

	/**
	 * This method returns the top of the bounds of this graph. Notice that
	 * bounds are not always up-to-date.
	 */
	public int getTop()
	{
		return this.top;
	}

	/**
	 * This method returns the bottom of the bounds of this graph. Notice that
	 * bounds are not always up-to-date.
	 */
	public int getBottom()
	{
		return this.bottom;
	}

	/**
	 * This method returns the bigger of the two dimensions of this graph.
	 */
	public int getBiggerDimension()
	{
		assert (this.right - this.left >= 0) && (this.bottom - this.top >= 0);
		return Math.max(this.right - this.left, this.bottom - this.top);
	}

	/**
	 * This method returns whether this graph is connected or not.
	 */
	public boolean isConnected()
	{
		return this.isConnected;
	}

	/**
	 * This method returns the margins of this graph to be applied on the
	 * bounding rectangle of its contents.
	 */
	public int getMargin()
	{
		return this.margin;
	}

	/**
	 * This method sets the margins of this graphs to be applied on the
	 * bounding rectangle of its contents.
	 */
	public void setMargin(int margin)
	{
		this.margin = margin;
	}

// -----------------------------------------------------------------------------
// Section: Topology related
// -----------------------------------------------------------------------------
	/**
	 * This methods adds the given node to this graph. We assume this graph has
	 * a proper graph manager.
	 */
	public LNode add(LNode newNode)
	{
		assert (this.graphManager != null) : "Graph has no graph mgr!";
		assert (!this.getNodes().contains(newNode)) : "Node already in graph!";
		newNode.setOwner(this);
		this.getNodes().add(newNode);

		return newNode;
	}

	/**
	 * This methods adds the given edge to this graph with specified nodes as
	 * source and target.
	 */
	public LEdge add(LEdge newEdge, LNode sourceNode, LNode targetNode)
	{
		assert (this.getNodes().contains(sourceNode) &&
			(this.getNodes().contains(targetNode))) :
				"Source or target not in graph!";
		assert (sourceNode.owner == targetNode.owner &&
			sourceNode.owner == this) :
			"Both owners must be this graph!";

		if (sourceNode.owner != targetNode.owner)
		{
			return null;
		}
		// set source and target
		newEdge.source = sourceNode;
		newEdge.target = targetNode;

		// set as intra-graph edge
		newEdge.isInterGraph = false;

		// add to graph edge list
		this.getEdges().add(newEdge);

		// add to incidency lists
		sourceNode.edges.add(newEdge);

		if (targetNode != sourceNode)
		{
			targetNode.edges.add(newEdge);
		}
		
		return newEdge;
	}

	/**
	 * This method removes the input node from this graph. If the node has any
	 * incident edges, they are removed from the graph (the graph manager for
	 * inter-graph edges) as well.
	 */
	public void remove(LNode node)
	{
		assert (node != null) : "Node is null!";
		assert (node.owner != null && node.owner == this) :
			"Owner graph is invalid!";
		assert (this.graphManager != null) : "Owner graph manager is invalid!";

		// remove incident edges first (make a copy to do it safely)
		List edgesToBeRemoved = new ArrayList();

		edgesToBeRemoved.addAll(node.edges);

		LEdge edge;
		for (Object obj : edgesToBeRemoved)
		{
			edge = (LEdge) obj;

			if (edge.isInterGraph)
			{
				this.graphManager.remove(edge);
			}
			else
			{
				edge.source.owner.remove(edge);
			}
		}

		// now the node itself
		assert (this.nodes.contains(node)) : "Node not in owner node list!";
		this.nodes.remove(node);
	}

	/**
	 * This method removes the input edge from this graph. Should not be used
	 * for inter-graph edges.
	 */
	public void remove(LEdge edge)
	{
		assert (edge != null) : "Edge is null!";
		assert (edge.source != null && edge.target != null) :
			"Source and/or target is null!";
		assert (edge.source.owner != null && edge.target.owner != null &&
			edge.source.owner == this && edge.target.owner == this) :
				"Source and/or target owner is invalid!";

		// remove edge from source and target nodes' incidency lists

		assert (edge.source.edges.contains(edge) &&
			edge.target.edges.contains(edge)) :
				"Source and/or target doesn't know this edge!";

		edge.source.edges.remove(edge);

		if (edge.target != edge.source)
		{
			edge.target.edges.remove(edge);
		}

		// remove edge from owner graph's edge list

		assert (edge.source.owner.getEdges().contains(edge)) :
			"Not in owner's edge list!";

		edge.source.owner.getEdges().remove(edge);
	}

// -----------------------------------------------------------------------------
// Section: Remaining methods
// -----------------------------------------------------------------------------
	/**
	 * This method calculates, updates and returns the left-top point of this
	 * graph including margins.
	 */
	public Point updateLeftTop()
	{
		int top = Integer.MAX_VALUE;
		int left = Integer.MAX_VALUE;
		int nodeTop;
		int nodeLeft;

		Iterator itr = this.getNodes().iterator();

		while (itr.hasNext())
		{
			LNode lNode = (LNode) itr.next();
			nodeTop = (int)(lNode.getTop());
			nodeLeft = (int)(lNode.getLeft());

			if (top > nodeTop)
			{
				top = nodeTop;
			}

			if (left > nodeLeft)
			{
				left = nodeLeft;
			}
		}

		// Do we have any nodes in this graph?
		if (top == Integer.MAX_VALUE)
		{
			return null;
		}

		this.left = left - this.margin;
		this.top =  top - this.margin;

		// Apply the margins and return the result
		return new Point(this.left, this.top);
	}

	/**
	 * This method calculates and updates the bounds of this graph including
	 * margins in a recursive manner, so that
	 * all compound nodes in this and lower levels will have up-to-date boundaries.
	 * Recursiveness of the function is controlled by the parameter named "recursive".
	 */
	public void updateBounds(boolean recursive)
	{
		// calculate bounds
		int left = Integer.MAX_VALUE;
		int right = -Integer.MAX_VALUE;
		int top = Integer.MAX_VALUE;
		int bottom = -Integer.MAX_VALUE;
		int nodeLeft;
		int nodeRight;
		int nodeTop;
		int nodeBottom;

		Iterator<LNode> itr = this.nodes.iterator();
		
		while (itr.hasNext())
		{
			LNode lNode = itr.next();

			// if it is a recursive call, and current node is compound
			if (recursive && lNode.child != null)
			{
				lNode.updateBounds();
			}
			nodeLeft = (int)(lNode.getLeft());
			nodeRight = (int)(lNode.getRight());
			nodeTop = (int)(lNode.getTop());
			nodeBottom = (int)(lNode.getBottom());

			if (left > nodeLeft)
			{
				left = nodeLeft;
			}

			if (right < nodeRight)
			{
				right = nodeRight;
			}

			if (top > nodeTop)
			{
				top = nodeTop;
			}

			if (bottom < nodeBottom)
			{
				bottom = nodeBottom;
			}
		}

		Rectangle boundingRect =
			new Rectangle(left, top, right - left, bottom - top);

		// Do we have any nodes in this graph?
		if (left == Integer.MAX_VALUE)
		{
			this.left =  (int)(this.parent.getLeft());
			this.right = (int)(this.parent.getRight());
			this.top =  (int)(this.parent.getTop());
			this.bottom = (int)(this.parent.getBottom());
		}

		this.left = boundingRect.x - this.margin;
		this.right = boundingRect.x + boundingRect.width + this.margin;
		this.top =  boundingRect.y - this.margin;
		// Label text dimensions are to be added for the bottom of the compound!
		this.bottom = boundingRect.y + boundingRect.height + this.margin;
	}

	/**
	 * This method returns the bounding rectangle of the given list of nodes. No
	 * margins are accounted for, and it returns a rectangle with top-left set
	 * to Integer.MAX_VALUE if the list is empty.
	 */
	public static Rectangle calculateBounds(List<LNode> nodes)
	{
		int left = Integer.MAX_VALUE;
		int right = -Integer.MAX_VALUE;
		int top = Integer.MAX_VALUE;
		int bottom = -Integer.MAX_VALUE;
		int nodeLeft;
		int nodeRight;
		int nodeTop;
		int nodeBottom;

		Iterator<LNode> itr = nodes.iterator();

		while (itr.hasNext())
		{
			LNode lNode = itr.next();
			nodeLeft = (int)(lNode.getLeft());
			nodeRight = (int)(lNode.getRight());
			nodeTop = (int)(lNode.getTop());
			nodeBottom = (int)(lNode.getBottom());

			if (left > nodeLeft)
			{
				left = nodeLeft;
			}

			if (right < nodeRight)
			{
				right = nodeRight;
			}

			if (top > nodeTop)
			{
				top = nodeTop;
			}

			if (bottom < nodeBottom)
			{
				bottom = nodeBottom;
			}
		}

		Rectangle boundingRect =
			new Rectangle(left, top, right - left, bottom - top);

		return boundingRect;
	}

	/**
	 * This method returns the depth of the parent node of this graph, if any,
	 * in the inclusion tree (nesting hierarchy).
	 */
	public int getInclusionTreeDepth()
	{
		if (this == this.graphManager.getRoot())
		{
			return 1;
		}
		else
		{
			return this.parent.getInclusionTreeDepth();
		}
	}

	/**
	 * This method returns estimated size of this graph.
	 */
	public int getEstimatedSize()
	{
		assert this.estimatedSize != Integer.MIN_VALUE;
		return this.estimatedSize;
	}

	/**
	 * This method sets the estimated size of this graph. We use this method to
	 * directly set this size in certain exceptional cases rather than
	 * calculating it from scratch (see calcEstimatedSize method).
	 */
	public void setEstimatedSize(int size)
	{
		this.estimatedSize = size;
	}

	/*
	 * This method calculates and returns the estimated size of this graph as
	 * well as the estimated sizes of the nodes in this graph recursively. The
	 * estimated size of a graph is based on the estimated sizes of its nodes.
	 * In fact, this value is the exact average dimension for non-compound nodes
	 * and it is a rather rough estimation on the dimension for compound nodes.
	 */
	public int calcEstimatedSize()
	{
		int size = 0;
		Iterator itr = this.nodes.iterator();

		while (itr.hasNext())
		{
			LNode lNode = (LNode) itr.next();
			size += lNode.calcEstimatedSize();
		}

		if (size == 0)
		{
			this.estimatedSize = LayoutConstants.EMPTY_COMPOUND_NODE_SIZE;
		}
		else
		{
			this.estimatedSize = (int)(size / Math.sqrt(this.nodes.size()));
		}

		return this.estimatedSize;
	}

	/**
	 * This method updates whether this graph is connected or not, taking
	 * indirect edges (e.g. an edge connecting a child node of a node of this
	 * graph to another node of this graph) into account.
	 */
	public void updateConnected()
	{
		if (this.nodes.size() == 0)
		{
			this.isConnected = true;
			return;
		}

		LinkedList<LNode> toBeVisited = new LinkedList<LNode>();
		Set<LNode> visited = new HashSet<LNode>();
		LNode currentNode = (LNode)this.nodes.get(0);
		List<LEdge> neighborEdges;
		LNode currentNeighbor;

		toBeVisited.addAll(currentNode.withChildren());

		while (!toBeVisited.isEmpty())
		{
			currentNode = toBeVisited.removeFirst();
			visited.add(currentNode);

			// Traverse all neighbors of this node
			neighborEdges = currentNode.getEdges();

			for (LEdge neighborEdge : neighborEdges)
			{
				currentNeighbor =
					neighborEdge.getOtherEndInGraph(currentNode, this);

				// Add unvisited neighbors to the list to visit
				if (currentNeighbor != null &&
					!visited.contains(currentNeighbor))
				{
					toBeVisited.addAll(currentNeighbor.withChildren());
				}
			}
		}

		this.isConnected = false;

		if (visited.size() >= this.nodes.size())
		{
			int noOfVisitedInThisGraph = 0;

			for (LNode visitedNode : visited)
			{
				if (visitedNode.owner == this)
				{
					noOfVisitedInThisGraph++;
				}
			}

			if (noOfVisitedInThisGraph == this.nodes.size())
			{
				this.isConnected = true;
			}
		}
	}
	
	/**
	 * This method reverses the given edge by swapping the source and target
	 * nodes of the edge.
	 * 
	 * @param edge	edge to be reversed
	 */
	public void reverse(LEdge edge)
	{
		edge.source.getOwner().getEdges().remove(edge);
		edge.target.getOwner().getEdges().add(edge);
		
		LNode swap = edge.source;
		edge.source = edge.target;
		edge.target = swap;
	}

// -----------------------------------------------------------------------------
// Section: Testing methods
// -----------------------------------------------------------------------------
	/**
	 * This method prints the topology of this graph.
	 */
	public void printTopology()
	{
		System.out.print((this.label == null ? "?" : this.label) + ": ");

		System.out.print("Nodes: ");
		LNode node;
		for (Object obj : this.nodes)
		{
			node = (LNode) obj;
			node.printTopology();
		}

		System.out.print("Edges: ");
		LEdge edge;
		for (Object obj : this.edges)
		{
			edge = (LEdge) obj;
			edge.printTopology();
		}
		System.out.println();
	}
}