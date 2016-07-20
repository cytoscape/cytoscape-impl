package org.ivis.layout;

import java.util.*;
import java.awt.Point;
import java.awt.Dimension;

import org.ivis.layout.fd.FDLayoutConstants;
import org.ivis.util.IGeometry;
import org.ivis.util.Transform;
import org.ivis.util.RectangleD;
import org.ivis.util.PointD;

/**
 * This class represents a node (l-level) for layout purposes. A node maintains
 * a list of its incident edges, which includes inter-graph edges. Every node
 * has an owner graph, except for the root node, which resides at the top of the
 * nesting hierarchy along with its child graph (the root graph).
 *
 * @author Erhan Giral
 * @author Ugur Dogrusoz
 * @author Cihan Kucukkececi
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class LNode extends LGraphObject implements Clustered
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/*
	 * Owner graph manager of this node
	 */
	protected LGraphManager graphManager;

	/**
	 * Possibly null child graph of this node
	 */
	protected LGraph child;

	/*
	 * Owner graph of this node; cannot be null
	 */
	protected LGraph owner;

	/*
	 * List of edges incident with this node
	 */
	protected List edges;

	/*
	 * Geometry of this node
	 */
	protected RectangleD rect;
	
	/*
	 * List of clusters, this node belongs to.
	 */
	protected List<Cluster> clusters;

	/*
	 * Estimated initial size (needed for compound node size estimation)
	 */
	private int estimatedSize = Integer.MIN_VALUE;

	/*
	 * Depth of this node in nesting hierarchy. Nodes in the root graph are of
	 * depth 1, nodes in the child graph of a node in the graph are of depth 2,
	 * etc.
	 */
	protected int inclusionTreeDepth = Integer.MAX_VALUE;

// -----------------------------------------------------------------------------
// Section: Constructors and initialization
// -----------------------------------------------------------------------------
	/*
	 * Constructor
	 */
	protected LNode(LGraphManager gm, Object vNode)
	{
		super(vNode);
		this.initialize();
		this.graphManager = gm;
		rect = new RectangleD();
	}

	/*
	 * Alternative constructor
	 */
	protected LNode(LGraphManager gm, Point loc, Dimension size, Object vNode)
	{
		super(vNode);
		this.initialize();
		this.graphManager = gm;
		rect = new RectangleD(loc.x, loc.y, size.width, size.height);
	}

	/*
	 * Alternative constructor
	 */
	protected LNode(Layout layout, Object vNode)
	{
		super(vNode);
		this.initialize();
		this.graphManager = layout.graphManager;
		rect = new RectangleD();
	}

	public void initialize()
	{
		this.edges = new LinkedList();
		this.clusters = new LinkedList<Cluster>();
	}

// -----------------------------------------------------------------------------
// Section: Accessors
// -----------------------------------------------------------------------------
	/**
	 * This method returns the list of incident edges of this node.
	 */
	public List getEdges()
	{
		return this.edges;
	}

	/**
	 * This method returns the child graph of this node, if any. Only compound
	 * nodes will have child graphs.
	 */
	public LGraph getChild()
	{
		return child;
	}

	/**
	 * This method sets the child graph of this node. Only compound nodes will
	 * have child graphs.
	 */
	public void setChild(LGraph child)
	{
		assert (child == null || child.getGraphManager() == this.graphManager) :
			"Child has different graph mgr!";

		this.child = child;
	}

	/**
	 * This method returns the owner graph of this node.
	 */
	public LGraph getOwner()
	{
		assert (this.owner == null || this.owner.getNodes().contains(this));

		return this.owner;
	}

	/**
	 * This method sets the owner of this node as input graph.
	 */
	public void setOwner(LGraph owner)
	{
		this.owner = owner;
	}

	/**
	 * This method returns the width of this node.
	 */
	public double getWidth()
	{
		return this.rect.width;
	}

	/**
	 * This method sets the width of this node.
	 */
	public void setWidth(double width)
	{
		this.rect.width = width;
	}

	/**
	 * This method returns the height of this node.
	 */
	public double getHeight()
	{
		return this.rect.height;
	}

	/**
	 * This method sets the height of this node.
	 */
	public void setHeight(double height)
	{
		this.rect.height = height;
	}

	/**
	 * This method returns the left of this node.
	 */
	public double getLeft()
	{
		return this.rect.x;
	}

	/**
	 * This method returns the right of this node.
	 */
	public double getRight()
	{
		return this.rect.x + this.rect.width;
	}

	/**
	 * This method returns the top of this node.
	 */
	public double getTop()
	{
		return this.rect.y;
	}

	/**
	 * This method returns the bottom of this node.
	 */
	public double getBottom()
	{
		return this.rect.y + this.rect.height;
	}

	/**
	 * This method returns the x coordinate of the center of this node.
	 */
	public double getCenterX()
	{
		return this.rect.x + this.rect.width / 2;
	}

	/**
	 * This method returns the y coordinate of the center of this node.
	 */
	public double getCenterY()
	{
		return this.rect.y + this.rect.height / 2;
	}

	/**
	 * This method returns the center of this node.
	 */
	public PointD getCenter()
	{
		return new PointD(this.rect.x + this.rect.width / 2,
			this.rect.y + this.rect.height / 2);
	}

	/**
	 * This method returns the location (upper-left corner) of this node.
	 */
	public PointD getLocation()
	{
		return new PointD(this.rect.x, this.rect.y);
	}

	/**
	 * This method returns the geometry of this node.
	 */
	public RectangleD getRect()
	{
		return this.rect;
	}

	/**
	 * This method returns the diagonal length of this node.
	 */
	public double getDiagonal()
	{
		return Math.sqrt(this.rect.width * this.rect.width +
			this.rect.height * this.rect.height);
	}

	/**
	 * This method returns half the diagonal length of this node.
	 */
	public double getHalfTheDiagonal()
	{
		return Math.sqrt(this.rect.height * this.rect.height +
			this.rect.width * this.rect.width) / 2;
	}

	/**
	 * This method sets the geometry of this node.
	 */
	public void setRect(Point upperLeft, Dimension dimension)
	{
		this.rect.x = upperLeft.x;
		this.rect.y = upperLeft.y;
		this.rect.width = dimension.width;
		this.rect.height = dimension.height;
	}

	/**
	 * This method sets the center of this node.
	 */
	public void setCenter(double cx, double cy)
	{
		this.rect.x = cx - this.rect.width / 2;
		this.rect.y = cy - this.rect.height / 2;
	}

	/**
	 * This method sets the location of this node.
	 */
	public void setLocation(double x, double y)
	{
		this.rect.x = x;
		this.rect.y = y;
	}

	/**
	 * This method moves the geometry of this node by specified amounts.
	 */
	public void moveBy(double dx, double dy)
	{
		this.rect.x += dx;
		this.rect.y += dy;
	}
	
	/**
	 * This method returns the cluster ID of this node.
	 * Use with caution, because it returns the cluster id of the first cluster.
	 * If a node has multiple clusters, remaining cluster information
	 * may be accessed by getClusters() method.
	 */
	public String getClusterID()
	{
		if (this.clusters.isEmpty())
		{
			return null;
		}
		
		return (new Integer(this.clusters.get(0).clusterID)).toString();
	}
	
	/**
	 * This method returns the list of clusters this node belongs to.
	 */
	public List getClusters()
	{
		return this.clusters;
	}

// -----------------------------------------------------------------------------
// Section: Remaining methods
// -----------------------------------------------------------------------------
	/**
	 * This method returns all nodes emanating from this node.
	 */
	public List getEdgeListToNode(LNode to)
	{
		List<LEdge> edgeList = new ArrayList();
		LEdge edge;

		for (Object obj : this.edges)
		{
			edge = (LEdge) obj;

			if (edge.target == to)
			{
				assert (edge.source == this) : "Incorrect edge source!";
				
				edgeList.add(edge);
			}
		}

		return edgeList;
	}

	/**
	 *	This method returns all edges between this node and the given node.
	 */
	public List getEdgesBetween(LNode other)
	{
		List<LEdge> edgeList = new ArrayList();
		LEdge edge;

		for (Object obj : this.edges)
		{
			edge = (LEdge) obj;

			assert (edge.source == this || edge.target == this) :
				"Incorrect edge source and/or target";

			if ((edge.target == other) || (edge.source == other))
			{
				edgeList.add(edge);
			}
		}

		return edgeList;
	}

	/**
	 * This method returns whether or not input node is a neighbor of this node.
	 */
	public boolean isNeighbor(LNode node)
	{
		LEdge edge;

		for (Object obj : this.edges)
		{
			edge = (LEdge) obj;

			if (edge.source == node || edge.target == node)
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * This method returns a set of neighbors of this node.
	 */
	public Set getNeighborsList()
	{
		Set<LNode> neighbors = new HashSet();
		LEdge edge;

		for (Object obj : this.edges)
		{
			edge = (LEdge) obj;

			if (edge.source.equals(this))
			{
				neighbors.add(edge.target);
			}
			else
			{
				assert (edge.target.equals(this)) : "Incorrect incidency!";
				neighbors.add(edge.source);
			}
		}

		return neighbors;
	}

	/**
	 * This method returns a set of successors (outgoing nodes) of this node.
	 */
	public Set getSuccessors()
	{
		Set<LNode> neighbors = new HashSet();
		LEdge edge;

		for (Object obj : this.edges)
		{
			edge = (LEdge) obj;

			assert (edge.source.equals(this) || edge.target.equals(this)) :
				"Incorrect incidency!";

			if (edge.source.equals(this))
			{
				neighbors.add(edge.target);
			}
		}

		return neighbors;
	}

	/**
	 * This method forms a list of nodes, composed of this node and its children
	 * (direct and indirect).
	 */
	public List withChildren()
	{
		LinkedList<LNode> withNeighborsList = new LinkedList<LNode>();
		LNode childNode;

		withNeighborsList.add(this);

		if (this.child != null)
		{
			for (Object childObject : this.child.getNodes())
			{
				childNode = (LNode)childObject;

				withNeighborsList.addAll(childNode.withChildren());
			}
		}

		return withNeighborsList;
	}

	/**
	 * This method returns the estimated size of this node, taking into account
	 * node margins and whether this node is a compound one containing others.
	 */
	public int getEstimatedSize()
	{
		assert this.estimatedSize != Integer.MIN_VALUE;
		return this.estimatedSize;
	}

	/*
	 * This method calculates the estimated size of this node. If the node is
	 * a compound node, the operation is performed recursively. It also sets the
	 * initial sizes of compound nodes based on this estimate.
	 */
	public int calcEstimatedSize()
	{
		if (this.child == null)
		{
			return this.estimatedSize =
				(int)((this.rect.width + this.rect.height) / 2);
		}
		else
		{
			this.estimatedSize = this.child.calcEstimatedSize();
			this.rect.width = this.estimatedSize;
			this.rect.height = this.estimatedSize;

			return this.estimatedSize;
		}
	}

	/**
	 * This method positions this node randomly in both x and y dimensions. We
	 * assume the center to be at (WORLD_CENTER_X, WORLD_CENTER_Y).
	 */
	protected void scatter()
	{
		double randomCenterX;
		double randomCenterY;

		double minX = -LayoutConstants.INITIAL_WORLD_BOUNDARY;
		double maxX = LayoutConstants.INITIAL_WORLD_BOUNDARY;
		randomCenterX = LayoutConstants.WORLD_CENTER_X +
			(LNode.random.nextDouble() * (maxX - minX)) + minX;

		double minY = -LayoutConstants.INITIAL_WORLD_BOUNDARY;
		double maxY = LayoutConstants.INITIAL_WORLD_BOUNDARY;
		randomCenterY = LayoutConstants.WORLD_CENTER_Y +
			(LNode.random.nextDouble() * (maxY - minY)) + minY;

		this.rect.x = randomCenterX;
		this.rect.y = randomCenterY;
	}

	/**
	 * This method updates the bounds of this compound node.
	 */
	public void updateBounds()
	{
		assert this.getChild() != null;

		if (this.getChild().getNodes().size() != 0)
		{
			// wrap the children nodes by re-arranging the boundaries
			LGraph childGraph = this.getChild();
			childGraph.updateBounds(true);

			this.rect.x =  childGraph.getLeft();
			this.rect.y =  childGraph.getTop();

			this.setWidth(childGraph.getRight() - childGraph.getLeft() +
				2 * LayoutConstants.COMPOUND_NODE_MARGIN);
			this.setHeight(childGraph.getBottom() - childGraph.getTop() +
				2 * LayoutConstants.COMPOUND_NODE_MARGIN +
					LayoutConstants.LABEL_HEIGHT);
		}
	}

	/**
	 * This method returns the depth of this node in the inclusion tree (nesting
	 * hierarchy).
	 */
	public int getInclusionTreeDepth()
	{
		assert this.inclusionTreeDepth != Integer.MAX_VALUE;
		return this.inclusionTreeDepth;
	}

	/**
	 * This method returns all parents (direct or indirect) of this node in the
	 * nesting hierarchy.
	 */
	public Vector getAllParents()
	{
		Vector parents = new Vector();
		LNode rootNode = this.owner.getGraphManager().getRoot().getParent();
		LNode parent = this.owner.getParent();

		while (true)
		{
			if (parent != rootNode)
			{
				parents.add(parent);
			}
			else
			{
				break;
			}

			parent = parent.getOwner().getParent();
		}

		parents.add(rootNode);

		return parents;
	}

	/**
	 * This method transforms the layout coordinates of this node using input
	 * transform.
	 */
	public void transform(Transform trans)
	{
		double left = this.rect.x;

		if (left > LayoutConstants.WORLD_BOUNDARY)
		{
			left = LayoutConstants.WORLD_BOUNDARY;
		}
		else if (left < -LayoutConstants.WORLD_BOUNDARY)
		{
			left = -LayoutConstants.WORLD_BOUNDARY;
		}

		double top = this.rect.y;

		if (top > LayoutConstants.WORLD_BOUNDARY)
		{
			top = LayoutConstants.WORLD_BOUNDARY;
		}
		else if (top < -LayoutConstants.WORLD_BOUNDARY)
		{
			top = -LayoutConstants.WORLD_BOUNDARY;
		}

		PointD leftTop = new PointD(left, top);
		PointD vLeftTop = trans.inverseTransformPoint(leftTop);

		this.setLocation(vLeftTop.x, vLeftTop.y);
	}

// -----------------------------------------------------------------------------
// Section: Implementation of Clustered Interface
// -----------------------------------------------------------------------------
	/**
	 * This method add this node  into a cluster with given cluster ID. If
	 * such cluster doesn't exist in ClusterManager, it creates a new cluster.
	 */
	public void addCluster(int clusterID)
	{
		//get cluster manager of the graph manager of this LNode
		ClusterManager cm = this.graphManager.getClusterManager();
	
		Cluster cluster = cm.getClusterByID(clusterID);
		if (cluster == null)
		{
			cluster = new Cluster(cm, clusterID, "Cluster " + clusterID);
			cm.addCluster(cluster);
		}
		
		this.addCluster(cluster);
	}
	
	/**
	 * This method adds the given cluster into cluster list of this node, 
	 * and moreover it adds this node into set of nodes of the given cluster.
	 * If this node is a compound node, then this operation is done recursively.
	 */
	public void addCluster(Cluster cluster)
	{
		if (cluster == null)
		{
			return;
		}
		
		// check if it is not added before
		if ( !this.clusters.contains(cluster) )
		{
			// add given cluster into list of clusters
			this.clusters.add(cluster);
			
			// add this node to set of nodes of the cluster
			cluster.getNodes().add(this);
			
			// if this node is a compound node
			if ( this.child != null )
			{
				// get all nodes of the child graph
				// child nodes may be compound as well
				List childrenNodes = this.child.getNodes();
				
				Iterator itr = childrenNodes.iterator();
				
				// iterate over each child node
				while (itr.hasNext()) 
				{
					LNode childNode = (LNode) itr.next();
					
					// recursively add children nodes to the cluster
					childNode.addCluster(cluster);
				}
			}
		}
	}

	/**
	 * This method removes the given cluster from cluster list of this node, 
	 * and moreover it removes this node from the set of nodes of the given cluster.
	 * If this node is a compound node, then this operation is done recursively.
	 */
	public void removeCluster(Cluster cluster)
	{
		if (cluster == null)
		{
			return;
		}
		
		// check if given cluster exists
		if (this.clusters.contains(cluster))
		{
			// remove given cluster from list of clusters
			this.clusters.remove(cluster);
			
			// remove this node from set of nodes of the cluster
			cluster.getNodes().remove(this);
			
			// if this node is a compound node
			if (this.child != null)
			{
				// get all nodes of the child graph
				// child nodes may be compound as well
				List childrenNodes = this.child.getNodes();
				
				Iterator itr = childrenNodes.iterator();
				
				// iterate over each child node
				while (itr.hasNext()) 
				{
					LNode childNode = (LNode) itr.next();
					
					// recursively remove children nodes from the cluster
					childNode.removeCluster(cluster);
				}
			}
		}
	}
	
	/**
	 * This method resets all cluster information of this node.
	 * This node is deleted from all clusters it belongs to.
	 */
	public void resetClusters()
	{
		for ( Cluster cluster : this.clusters )
		{	
			cluster.getNodes().remove(this);
		}
		this.clusters.clear();
	}
	
	public Clustered getParent()
	{
		if(this.owner == null)
		{
			return null;
		}
		
		return this.owner.getParent();
	}
	
	/**
	 * This method checks if this node belongs to the given cluster
	 * Returns boolean true if this node belongs to the given cluster,
	 * and boolean false otherwise
	 */
	public boolean belongsToCluster(Cluster cluster)
	{
		if (this.clusters.contains(cluster))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

// -----------------------------------------------------------------------------
// Section: Testing methods
// -----------------------------------------------------------------------------
	/**
	 * This method prints the topology of this node.
	 */
	public void printTopology()
	{
		System.out.print(this.label == null ? "?" : this.label + "{");
		LEdge edge;
		LNode otherEnd;
		for (Object obj : this.edges)
		{
			edge = (LEdge) obj;
			otherEnd = edge.getOtherEnd(this);
			System.out.print(otherEnd.label == null ? "?" : otherEnd.label + ",");
		}
		System.out.print("} ");
	}

	/**
	 * This method returns true if two LNodes overlap along with the overlap 
	 * amount in x and y directions. Method returns false if there is no overlap.
	 */
	public boolean calcOverlap(LNode nodeB, double[] overlapAmount)
	{
		RectangleD rectA = this.getRect();
		RectangleD rectB = nodeB.getRect();
		//System.out.println("In LNODE OVERLAPS");
		if (rectA.intersects(rectB))
		// two nodes overlap
		{
			// calculate separation amount in x and y directions
			IGeometry.calcSeparationAmount(rectA,
				rectB,
				overlapAmount,
				FDLayoutConstants.DEFAULT_EDGE_LENGTH / 2.0);

			return true;
		}
		
		else
		{
			return false;
		}
	}

	public void calcIntersection(LNode nodeB, double[] clipPoints)
	{	
		IGeometry.getIntersection(this.rect, nodeB.rect, clipPoints);
	}
	
// -----------------------------------------------------------------------------
// Section: Class variables
// -----------------------------------------------------------------------------
	/*
	 * Used for random initial positioning
	 */
	private static Random random = new Random(Layout.RANDOM_SEED);
}