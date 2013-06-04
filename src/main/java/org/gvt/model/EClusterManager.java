package org.gvt.model;

import java.util.ArrayList;
import java.util.Iterator;

import org.ivis.layout.Cluster;
import org.ivis.layout.ClusterManager;

/**
 * This class represents a cluster manager for editor purposes. A cluster manager
 * maintains a collection of clusters.
 *
 * @author Shatlyk Ashyralyyev
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class EClusterManager extends ClusterManager {
// -----------------------------------------------------------------------------
// Section: Constructors
// -----------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	public EClusterManager()
	{
		this.clusters = new ArrayList<ECluster>();
		
		// default is false
		this.polygonUsed = false;
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
		ECluster eCluster = new ECluster(this, clusterID, clusterName);
		
		// add the cluster into cluster list of this cluster manager
		this.clusters.add(eCluster);
	}
	
	/**
	 * This method creates a new cluster from given clusterName.
	 * New cluster is maintained by this cluster manager.
	 */
	public void createCluster(String clusterName)
	{
		// allocate new empty LCluster instance
		ECluster eCluster = new ECluster(this, clusterName);
		
		// add the cluster into cluster list of this cluster manager
		this.clusters.add(eCluster);
	}
}
