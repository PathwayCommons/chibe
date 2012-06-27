package org.ivis.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This class represents a cluster manager for layout purposes. A cluster manager
 * maintains a collection of clusters.
 *
 * @author Shatlyk Ashyralyyev
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
}
