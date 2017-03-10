package org.ivis.layout;

/**
 * This interface is used for clustering purposes. Any class that
 * implements this interface may be used as a cluster object. The main purpose
 * of this interface is to easily map LNode and NodeModel among each other,
 * for layout purposes.
 *
 * @author Shatlyk Ashyralyyev
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public interface Clustered {
	/**
	 * This method add this node model into a cluster with given cluster ID. If
	 * such cluster doesn't exist in ClusterManager, it creates a new cluster.
	 */
	public abstract void addCluster(int clusterID);
	
	/**
	 * This method adds a new cluster into clustered object's clusters
	 */
	public abstract void addCluster(Cluster cluster);
	
	/**
	 * This method removes the cluster from clustered object's clusters
	 */
	public abstract void removeCluster(Cluster cluster);
	
	/**
	 * This method resets all clusters of the clustered object
	 */
	public abstract void resetClusters();

	/**
	 * This method returns the left of this node.
	 */
	public abstract double getLeft();
	
	/**
	 * This method returns the right of this node.
	 */
	public abstract double getRight();
	
	/**
	 * This method returns the top of this node.
	 */
	public abstract double getTop();

	/**
	 * This method returns the bottom of this node.
	 */
	public abstract double getBottom();
	
	/**
	 * This method returns the parent of clustered object.
	 * If it is a root object, then null should be returned.
	 */
	public abstract Clustered getParent();
}
