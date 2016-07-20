package org.ivis.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.ivis.util.PointD;
import org.ivis.util.IGeometry;

/**
 * This class represents a cluster manager for layout purposes. A cluster manager
 * maintains a collection of clusters.
 *
 * @author Shatlyk Ashyralyyev 
 * @author Can Cagdas Cengiz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class ClusterManager
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/*
	 * Clusters maintained by this cluster manager.
	 */
	protected ArrayList clusters;
	
	/*
	 * Boolean variable used for storing whether polygons are used during layout
	 */
	protected boolean polygonUsed;
	
// -----------------------------------------------------------------------------
// Section: Constructors
// -----------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	public ClusterManager()
	{
		this.clusters = new ArrayList<Cluster>();
		
		// default is false
		this.polygonUsed = false;
	}
	
// -----------------------------------------------------------------------------
// Section: Getters and Setters
// -----------------------------------------------------------------------------
	/**
	 * This method returns the list of clusters maintained by this 
	 * cluster manager.
	 */
	public ArrayList<Cluster> getClusters()
	{
		return this.clusters;
	}
	
	/**
	 * This method sets the polygonUsed variable
	 */
	public void setPolygonUsed(boolean polygonUsed)
	{
		this.polygonUsed = polygonUsed;
	}
	
	
	/**
	 * This method returns clusterIDs of all existing clusters as sorted array.
	 */
	public ArrayList<Integer> getClusterIDs()
	{
		ArrayList<Integer> result = new ArrayList<Integer>();
		
		Iterator iterator = this.clusters.iterator();
		
		while (iterator.hasNext()) 
		{
			Cluster cluster = (Cluster) iterator.next();
			if (cluster.getClusterID() > 0)
			{
				result.add(cluster.getClusterID());
			}
		}
		
		Collections.sort(result);
		
		return result;
	}
	
	
// -----------------------------------------------------------------------------
// Section: Remaining Methods
// -----------------------------------------------------------------------------
	/**
	 * This method creates a new cluster from given clusterID and clusterName.
	 * New cluster is maintained by this cluster manager.
	 */
	public void createCluster(int clusterID, String clusterName)
	{
		// allocate new empty LCluster instance
		Cluster cluster = new Cluster(this, clusterID, clusterName);
		
		// add the cluster into cluster list of this cluster manager
		this.clusters.add(cluster);
	}
	
	/**
	 * This method creates a new cluster from given clusterName.
	 * New cluster is maintained by this cluster manager.
	 */
	public void createCluster(String clusterName)
	{
		// allocate new empty LCluster instance
		Cluster lCluster = new Cluster(this, clusterName);
		
		// add the cluster into cluster list of this cluster manager
		this.clusters.add(lCluster);
	}	
	
	/**
	 * This method adds the given cluster into cluster manager of the graph.
	 */
	public void addCluster(Cluster cluster)
	{
		cluster.setClusterManager(this);
		
		// add the cluster into cluster list of this cluster manager
		this.clusters.add(cluster);
	}
	
	/**
	 * Removes the given cluster from the graph.
	 */
	public void removeCluster(Cluster cluster)
	{	
		// deletes the cluster information from graph
		cluster.delete();
	}
	
	/**
	 * This method checks if the given cluster ID is used before.
	 * If same ID is used before, it returns true, otherwise it returns false.
	 */
	public boolean isClusterIDUsed(int clusterID)
	{
		// get an iterator for cluster list
		Iterator<Cluster> itr = this.clusters.iterator();
		
		// iterate over all clusters and check if clusterID is used before
		while (itr.hasNext())
		{
			Cluster cluster = itr.next();
			
			if (cluster.getClusterID() == clusterID)
			{
				return true;
			}
		}
		
		// not used before
		return false;
	}

	/**
	 * This method returns the cluster with given cluster ID, if no such cluster
	 * it returns null;
	 */
	public Cluster getClusterByID(int clusterID)
	{
		// get an iterator for cluster list
		Iterator<Cluster> itr = this.clusters.iterator();
		
		// iterate over all clusters and check if clusterID is same
		while (itr.hasNext())
		{
			Cluster cluster = itr.next();
			
			if (cluster.getClusterID() == clusterID)
			{
				return cluster;
			}
		}
		
		// no such cluster
		return null;
	}
	
	/**
	 * This method removes all clusters from graph. First it copies all cluster
	 * IDs. After that calls delete() method of each cluster.
	 */
	public void clearClusters()
	{
		// first, copy of cluster ids is stored in order to prevent 
		// pointer problems
		ArrayList<Integer> clusterIDs = new ArrayList<Integer>();
		
		Iterator<Cluster> iter = this.clusters.iterator();
		
		while ( iter.hasNext() )
		{
			clusterIDs.add(iter.next().getClusterID());
		}
		
		for (Integer id : clusterIDs)
		{
			getClusterByID(id).delete();
		}
	}
// -----------------------------------------------------------------------------
// Section: Class variables
// -----------------------------------------------------------------------------
	/*
	 * idCounter is used to set the ID's of clusters. Each time when some
	 * cluster ID is set, it should incremented by 1.
	 */
	public static int idCounter = 1;
	
	/**
	 * This method finds the overlapping clusters. The calculation uses 
	 * the points of cluster polygons. The overlap information is returned
	 * in arraylist of object arrays. An object array has the following 
	 * elements.
	 * [0] = Id of the first cluster
	 * [1] = Id of the second cluster
	 * [2] = Overlap in x-axis
	 * [3] = Overlap in y-axis  
	 */
	public ArrayList<Object []> getOverlapInformation()
	{
		Object [] overlap;
		ArrayList<Object []> overlapInfo;
		Cluster c1;
		Cluster c2;
		
		ArrayList<PointD> p1;
		ArrayList<PointD> p2;
		
		int numberOfClusters;
		
		numberOfClusters = clusters.size();
		overlapInfo = new ArrayList<Object []>();
		
		// loop is optimized such that each pair is compared only once
		
		for (int i = 0; i < numberOfClusters; i++)
		{
			c1 = (Cluster) clusters.get(i);
			p1 = c1.getPolygon();
			
			for (int j = i + 1; j < numberOfClusters; j++)
			{
				c2 = (Cluster) clusters.get(j);
				p2 = c2.getPolygon();
				
				// System.out.println("Checking clusters "+c1.clusterID+ " and "+ c2.clusterID); //test
				
				if ( (p1.size() > 3) && (p2.size() > 3) )
				{
					overlap = IGeometry.convexPolygonOverlap(p1,p2);
					if ((double) overlap[0] != 0.0)
					{
						/*System.out.println("The clusters " + c1.clusterID + 
								" and " + c2.clusterID + " overlap."); // test
						*/
						Object [] newOverlap = new Object[4];
						
						newOverlap[0] = c1.clusterID;
						newOverlap[1] = c2.clusterID;
						newOverlap[2] = overlap[0]; // overlap amount
						newOverlap[3] = overlap[1]; // overlap direction
						
						PointD temp;
						temp = IGeometry.getXYProjection(((double) overlap[0]),
								((PointD) overlap[1]));
						
						newOverlap[2] = temp.x; // overlap in x						
						newOverlap[3] = temp.y; // overlap in y
						
						overlapInfo.add(newOverlap);
					}	
				}
			}
			
		}
		return overlapInfo;
	}
				
	
} //end of class


