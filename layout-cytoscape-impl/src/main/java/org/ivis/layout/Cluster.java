package org.ivis.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import org.ivis.util.PointD;

/**
 * This class represents a cluster for layout purpose. A cluster maintains
 * a list of nodes, which belong to the cluster. Every cluster has its own name 
 * and unique ID.
 *
 * @author Shatlyk Ashyralyyev
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class Cluster implements Comparable
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/*
	 * List of clustered objects that belong to the cluster
	 */
	protected Set<Clustered> nodes;
	
	/*
	 * Owner cluster manager.
	 */
	protected ClusterManager clusterManager;
	
	/*
	 * Unique ID of the cluster 
	 */
	protected int clusterID;
	
	/*
	 * Name of the cluster
	 */
	protected String clusterName;
	
	/*
	 * Polygon that covers all nodes of the cluster
	 */
	protected ArrayList<PointD> polygon;

// -----------------------------------------------------------------------------
// Section: Constructors
// -----------------------------------------------------------------------------
	/**
	 * Constructor for creating a Cluster from cluster name for a given 
	 * ClusterManager. Cluster ID is generated from idCounter.
	 */
	public Cluster(ClusterManager clusterManager, String clusterName)
	{
		this.nodes = new HashSet<Clustered>();
		this.polygon = new ArrayList<PointD>();
		
		// set the cluster manager
		this.clusterManager = clusterManager;
		this.clusterName = clusterName;
		
		// find a free clusterID
		if (this.clusterManager != null)
		{
			while (!this.clusterManager.isClusterIDUsed(ClusterManager.idCounter))
			{
				ClusterManager.idCounter++;
			}
		}
		this.clusterID = ClusterManager.idCounter;	
		
		// each cluster has its own cluster ID, counter is incremented by 1
		ClusterManager.idCounter++;
	}
	
	/**
	 * Constructor for creating a Cluster when clusterID is specified by user.
	 */
	public Cluster(ClusterManager clusterManager, int clusterID, String clusterName)
	{
		this.nodes = new HashSet<Clustered>();
		this.polygon = new ArrayList<PointD>();

		// set the cluster manager
		this.clusterManager = clusterManager;
		
		// check if clusterID is used before
		if (this.clusterManager != null)
		{
			// if cluster ID is used before, set the cluster id automatically
			if (this.clusterManager.isClusterIDUsed(clusterID))
			{
				// print error message
				System.err.println("Cluster ID " + clusterID + " is used" + 
						" before. ClusterID is set automatically.");
				
				// find first free clusterID that can be used
				while (this.clusterManager.isClusterIDUsed(ClusterManager.idCounter))
				{
					ClusterManager.idCounter++;
				}
				// set cluster ID automatically
				clusterID = ClusterManager.idCounter;

				// each cluster has its own cluster ID, counter is incremented by 1
				ClusterManager.idCounter++;
			}
		}
		
		// set cluster name and id
		this.clusterName = clusterName;
		this.clusterID = clusterID;
	}
	
	/**
	 * Copy constructor.
	 */
	public Cluster(Cluster c)
	{
		this.nodes = new HashSet<Clustered>();
		
		for(Object o: c.nodes)
		{
			Clustered node = (Clustered) o;
			this.nodes.add(node);
		}
		
		this.polygon = new ArrayList<PointD>();
		
		for(Object o: c.polygon)
		{
			PointD pt = (PointD) o;
			this.polygon.add(pt);
		}
		
		
		this.clusterName = c.clusterName;
		this.clusterID = c.clusterID;
		
	}
	
// -----------------------------------------------------------------------------
// Section: Accessors
// -----------------------------------------------------------------------------
	/**
	 * This method returns a set of nodes that belong to this cluster. 
	 */
	public Set<Clustered> getNodes()
	{
		return this.nodes;
	}

	/**
	 * This method returns the ID of this cluster.
	 */
	public int getClusterID()
	{
		return this.clusterID;
	}

	/**
	 * This method sets the cluster manager of this cluster
	 */
	public void setClusterManager(ClusterManager clusterManager)
	{
		this.clusterManager = clusterManager;
	}
	
	/**
	 * This method returns the name of this cluster.
	 */
	public String getClusterName()
	{
		return this.clusterName;
	}

	/**
	 * This method sets the name of this cluster.
	 */
	public void setClusterName(String clusterName)
	{
		this.clusterName = clusterName;
	}
	
	/**
	 * This method returns the polygon
	 */
	public ArrayList<PointD> getPolygon()
	{
		return this.polygon;
	}
	
	/**
	 * This method sets the polygon
	 */
	public void setPolygon(ArrayList<PointD> points)
	{
		this.polygon = points;
	}

// -----------------------------------------------------------------------------
// Section: Remaining methods
// -----------------------------------------------------------------------------
	/**
	 * This method adds the given clustered node into this cluster.
	*/	
	public void addNode(Clustered node)
	{		
		node.addCluster(this);
	}
	
	/**
	 * This method removes the given clustered node from this cluster.
	 */
	public void removeNode(Clustered node)
	{
		node.removeCluster(this);
	}
	
	/**
	 * This method deletes the cluster information from the graph.
	 */
	public void delete()
	{	
		// get copy of nodes in order to prevent pointer problems
		ArrayList<Clustered> copy = new ArrayList<Clustered>();
		copy.addAll(this.nodes);
		
		for (Clustered node : copy)
		{
			node.removeCluster(this);
		}

		// delete this cluster form cluster managers cluster list
		this.clusterManager.getClusters().remove(this);
	}
	
	/**
	 * This method calculates the convex polygon bounding all nodes of 
	 * this cluster.
	 */
	public void calculatePolygon()
	{
		if (this.clusterID == 0)
		{
			return;
		}
		calculateConvexHull();
	}
	
	/**
	 * This method collects all boundary points of all nodes.
	 */
	private void findPoints()
	{
		this.polygon.clear();

		if (this.nodes.isEmpty())
		{
			return;
		}
		
		Iterator<Clustered> nodeItr = this.nodes.iterator();
		Clustered node;
		
		while (nodeItr.hasNext())
		{
			node = nodeItr.next();
			
			double left = node.getLeft();
			double right = node.getRight();
			double top = node.getTop();
			double bottom = node.getBottom();
			
			Clustered parent = node.getParent();
			
			//calculate absolute position
			while ( parent != null )
			{
				left += parent.getLeft();
				right += parent.getLeft();
				
				top += parent.getTop();
				bottom += parent.getTop();
				
				parent = parent.getParent();
			}

			// draw cluster boundaries with a little buffer around if node is
			// wide and tall enough; otherwise, accurate boundary checks will
			// not be feasible (see InsideClusterBounds methods)!

			if (right - left > 2.0 * LayoutConstants.CLUSTER_BOUNDARY_MARGIN)
			{
				left -= LayoutConstants.CLUSTER_BOUNDARY_MARGIN;
				right += LayoutConstants.CLUSTER_BOUNDARY_MARGIN;
			}

			if (bottom - top > 2.0 * LayoutConstants.CLUSTER_BOUNDARY_MARGIN)
			{
				top -= LayoutConstants.CLUSTER_BOUNDARY_MARGIN;
				bottom += LayoutConstants.CLUSTER_BOUNDARY_MARGIN;
			}

			this.polygon.add(new PointD(left, top));
			this.polygon.add(new PointD(right, top));
			this.polygon.add(new PointD(right, bottom));
			this.polygon.add(new PointD(left, bottom));
		}
	}
	/**
	 * This method computes the convex hull of given points in O(N*logN) time.
	 * Very similar algorithm to Graham Scan is implemented.
	 */
	private void calculateConvexHull()
	{
		// find points
		findPoints();
		
		if (this.polygon.isEmpty())
		{
			return;
		}
		
		// sort points in increasing order of x coordinates, in case of tie
		// point with higher y coordinate comes first
		Collections.sort(this.polygon, new PointComparator());

		Stack<PointD> upperHull = new Stack<PointD>();
		Stack<PointD> lowerHull = new Stack<PointD>();
		
		int n = this.polygon.size();
		if ( n < 3 )
		{
			// no polygon
			return;
		}
		// push first 2 points
		upperHull.push(this.polygon.get(0));
		upperHull.push(this.polygon.get(1));
		
		// calculate upper hull
		for (int i = 2; i < this.polygon.size(); i++)
		{
			PointD pt3 = this.polygon.get(i);
			
			while (true) 
			{
				PointD pt2 = upperHull.pop();
				// 2 points should be pushed back
				if (upperHull.empty())
				{
					upperHull.push(pt2);
					upperHull.push(pt3);
					break;
				}
				
				PointD pt1 = upperHull.peek();
				
				if (rightTurn(pt1, pt2, pt3))
				{
					upperHull.push(pt2);
					upperHull.push(pt3);
					break;
				}	
			}
		}

		lowerHull.push(this.polygon.get(n-1));
		lowerHull.push(this.polygon.get(n-2));
		
		// calculate lower hull
		for (int i = n-3; i >= 0; i--)
		{
			PointD pt3 = this.polygon.get(i);
			
			while (true) 
			{
				PointD pt2 = lowerHull.pop();
				// 2 points should be pushed back
				if (lowerHull.empty())
				{
					lowerHull.push(pt2);
					lowerHull.push(pt3);
					break;
				}
				
				PointD pt1 = lowerHull.peek();
				
				if (rightTurn(pt1, pt2, pt3))
				{
					lowerHull.push(pt2);
					lowerHull.push(pt3);
					break;
				}	
			}
		}
		
		// construct convex hull
		this.polygon.clear();
		n = lowerHull.size();
		for (int i=0; i < n; i++)
		{
			this.polygon.add(lowerHull.pop());
		}	

		n = upperHull.size();
		for (int i=0; i < n; i++)
		{
			this.polygon.add(upperHull.pop());
		}
	}

	/**
	 * This method check whether it is a right turn.
	 */
	private static boolean rightTurn(PointD pt1, PointD pt2, PointD pt3)
	{
		// first vector
		double x1 = pt2.x - pt1.x;
		double y1 = - (pt2.y - pt1.y);
		
		// second vector
		double x2 = pt3.x - pt2.x;
		double y2 = - (pt3.y - pt2.y);
		
		// decide using cross product, right hand rule is applied
		if ((x1* y2 - y1 * x2) <= 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * This method checks whether or not the given node is completely within the
	 * bounds of this cluster (its convex polygon).
	 */
	public boolean isCompletelyInsideClusterBounds(Clustered node)
	{
		double left = node.getLeft();
		double right = node.getRight();
		double top = node.getTop();
		double bottom = node.getBottom();

		// cluster boundaries were drawn with a certain margin!

		if (right - left > 2.0 * LayoutConstants.CLUSTER_BOUNDARY_MARGIN)
		{
			left += LayoutConstants.CLUSTER_BOUNDARY_MARGIN;
			right -= LayoutConstants.CLUSTER_BOUNDARY_MARGIN;
		}

		if (bottom - top > 2.0 * LayoutConstants.CLUSTER_BOUNDARY_MARGIN)
		{
			top += LayoutConstants.CLUSTER_BOUNDARY_MARGIN;
			bottom -= LayoutConstants.CLUSTER_BOUNDARY_MARGIN;
		}

		return (this.isInside(new PointD(left, top)) &&
			this.isInside(new PointD(left, bottom)) &&
			this.isInside(new PointD(right, top)) &&
			this.isInside(new PointD(right, bottom)));
	}

	/**
	 * This method checks whether or not the given node is (at least) partially
	 * inside the bounds of this cluster (its convex polygon).
	 */
	public boolean isPartiallyInsideClusterBounds(Clustered node)
	{
		double left = node.getLeft();
		double right = node.getRight();
		double top = node.getTop();
		double bottom = node.getBottom();

		// cluster boundaries were drawn with a certain margin!

		if (right - left > 2.0 * LayoutConstants.CLUSTER_BOUNDARY_MARGIN)
		{
			left += LayoutConstants.CLUSTER_BOUNDARY_MARGIN;
			right -= LayoutConstants.CLUSTER_BOUNDARY_MARGIN;
		}

		if (bottom - top > 2.0 * LayoutConstants.CLUSTER_BOUNDARY_MARGIN)
		{
			top += LayoutConstants.CLUSTER_BOUNDARY_MARGIN;
			bottom -= LayoutConstants.CLUSTER_BOUNDARY_MARGIN;
		}

		return (this.isInside(new PointD(left, top)) ||
			this.isInside(new PointD(left, bottom)) ||
			this.isInside(new PointD(right, top)) ||
			this.isInside(new PointD(right, bottom)));
	}

	/**
	 * This method checks whether or not the given point is inside the bounds of
	 * this cluster (its convex polygon).
	 */
	boolean isInside(PointD p)
	{
		int n = this.polygon.size();

		// There must be at least 3 vertices in polygon[]
		if (n < 3)  return false;

		// Create a point for line segment from p to infinite
		PointD extreme = new PointD(Double.MAX_VALUE, p.y);

		// Count intersections of the above line with sides of polygon
		int count = 0;
		int i = 0;
		PointD p_i;
		PointD p_next;

		do
		{
			int next = (i + 1) % n;

			// Check if the line segment from 'p' to 'extreme' intersects
			// with the line segment from 'polygon[i]' to 'polygon[next]'
			p_i = this.polygon.get(i);
			p_next = this.polygon.get(next);

			if (doIntersect(p_i, p_next, p, extreme))
			{
				// If the point 'p' is co-linear with line segment 'i-next',
				// then check if it lies on segment. If it lies, return true,
				// otherwise false
				if (this.calcOrientation(p_i, p, p_next) == 0)
				{
					return this.isOnSegment(p_i, p, p_next);
				}

				if (this.calcOrientation(p, p_i, extreme) != 0)
				{
					count++;
				}
			}

			i = next;

		} while (i != 0);

		// Return true if count is odd, false otherwise
		return count % 2 == 1;
	}

	/**
	 * Given three co-linear points p, q, r, this method checks if point q lies
	 * on line segment p-r.
	 */
	public boolean isOnSegment(PointD p, PointD q, PointD r)
	{
		if (q.x <= Double.max(p.x, r.x) && q.x >= Double.min(p.x, r.x) &&
			q.y <= Double.max(p.y, r.y) && q.y >= Double.min(p.y, r.y))
		{
			return true;
		}

		return false;
	}

	/**
	 * This method finds the orientation of ordered triplet (p, q, r), and
	 * returns following values:
	 * 	0 --> p, q and r are co-linear,
	 * 	1 --> clockwise,
	 * 	2 --> counter-clockwise.
	 */
	public int calcOrientation(PointD p, PointD q, PointD r)
	{
		int val = (int)((q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y));

		if (val == 0)
			return 0;  // co-linear

		return (val > 0)? 1: 2; // clockwise or counter-clockwise
	}

	/**
	 * This method returns true if line segment p1-q1 and p2-q2 intersect.
	 */
	public boolean doIntersect(PointD p1, PointD q1, PointD p2, PointD q2)
	{
		// Find the four orientations needed for general and
		// special cases
		int o1 = this.calcOrientation(p1, q1, p2);
		int o2 = this.calcOrientation(p1, q1, q2);
		int o3 = this.calcOrientation(p2, q2, p1);
		int o4 = this.calcOrientation(p2, q2, q1);

		// General case
		if (o1 != o2 && o3 != o4)
			return true;

		// Special Cases
		// p1, q1 and p2 are co-linear and p2 lies on segment p1-q1
		if (o1 == 0 && this.isOnSegment(p1, p2, q1)) return true;

		// p1, q1 and p2 are co-linear and q2 lies on segment p1-q1
		if (o2 == 0 && this.isOnSegment(p1, q2, q1)) return true;

		// p2, q2 and p1 are co-linear and p1 lies on segment p2-q2
		if (o3 == 0 && this.isOnSegment(p2, p1, q2)) return true;

		// p2, q2 and q1 are co-linear and q1 lies on segment p2-q2
		if (o4 == 0 && this.isOnSegment(p2, q1, q2)) return true;

		return false; // Doesn't fall in any of the above cases
	}

	/**
	 * Method to make 2 clusters comparable
	 */
	public int compareTo(Object obj)
	{
		if (obj instanceof Cluster)
		{
			Cluster cluster = (Cluster) obj;
			
			// compare ID's of two clusters
			return ((Integer)this.clusterID).compareTo(cluster.getClusterID());
		}
		return 0;
	}
	
	/**
	 * This is a helper class for sorting PointD objects
	 *
	 */
	private class PointComparator implements Comparator<PointD>
	{
		/**
		 * Override
		 */
		public int compare(PointD o1, PointD o2) {
			PointD pt1 = (PointD) o1;
			PointD pt2 = (PointD) o2;
			
			if(pt1.x < pt2.x) return -1;
			else if(pt1.x > pt2.x) return 1;
			else if(Math.abs(pt1.x-pt2.x) < 1e-9  && pt1.y > pt2.y) return -1;
			else if(Math.abs(pt1.x-pt2.x) < 1e-9 && pt1.y < pt2.y) return 1;
			
			return 0;
		}
		
	}
}
