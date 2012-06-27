package org.ivis.layout.cluster;

import java.util.*;

import org.ivis.layout.*;
import org.ivis.layout.cose.CoSEEdge;
import org.ivis.layout.cose.CoSELayout;
import org.ivis.layout.cose.CoSENode;

/**
 * This layout arranges the nodes with the CoSE Layout by partitioning the nodes
 * according to their cluster IDs.
 *
 * @author Cihan Kucukkececi
 * @author Selcuk Onur Sumer
 * @author Shatlyk Ashyralyyev
 * @author Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class ClusterLayout extends CoSELayout
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/**
	 * Margins of graphs corresponding to paddings around clusters
	 */
	public int graphMargin = LayoutConstants.DEFAULT_GRAPH_MARGIN;

// -----------------------------------------------------------------------------
// Section: Constructors
// -----------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	public ClusterLayout()
	{
		super();
	}

// -----------------------------------------------------------------------------
// Section: Methods
// -----------------------------------------------------------------------------
	/**
	 * This method is used to set CoSE layout parameters that are specific to
	 * Cluster layout.
	 */
	public void initParameters()
	{
		super.initParameters();

		if (!this.isSubLayout)
		{
			LayoutOptionsPack.Cluster layoutOptionsPack =
				LayoutOptionsPack.getInstance().getCluster();

			if (layoutOptionsPack.getIdealEdgeLength() < 10)
			{
				this.idealEdgeLength = ClusterConstants.DEFAULT_EDGE_LENGTH;
			}
			else
			{
				this.idealEdgeLength = layoutOptionsPack.getIdealEdgeLength();
			}

			// We try to separate clusters from each other by simply adjusting
			// graph margins and increasing compound gravitation.

			this.graphMargin = (int) transform(
					layoutOptionsPack.getClusterSeperation(),
					ClusterLayout.DEFAULT_CLUSTER_SEPARATION);

			this.compoundGravityConstant = transform(
				layoutOptionsPack.getClusterGravityStrength(),
					ClusterConstants.DEFAULT_COMPOUND_GRAVITY_STRENGTH);
		}

		// These are remaining parameters maintained by CoSE options pack, reset
		// them to default to ignore CoSE layout options customized by the user.

		this.springConstant = ClusterConstants.DEFAULT_SPRING_STRENGTH;
		this.repulsionConstant = ClusterConstants.DEFAULT_REPULSION_STRENGTH;
		this.gravityConstant = ClusterConstants.DEFAULT_GRAVITY_STRENGTH;
	}

	/**
	 * This method sorts the clusterIDs and merges them to get a string, that
	 * represents the region.
	 * 
	 * @param node : node whose clusterIDs are to be returned
	 * @return sorted and merged String version of clusterIDs
	 */
	private String getRegionName(LNode node)
	{
		String result = "";
		
		// Get all clusterIDs of clusters that node belongs to
		ArrayList<Integer> clusterIDs = new ArrayList<Integer>();
		List clusters = node.getClusters();
		Iterator itr = clusters.iterator();
		
		while (itr.hasNext())
		{
			Cluster cluster = (Cluster) itr.next();

			if (!clusterIDs.contains(cluster.getClusterID()))
			{
				clusterIDs.add(cluster.getClusterID());
			}
		}
		
		// Sort cluster IDs
		Collections.sort(clusterIDs);
		
		// Get String
		result += clusterIDs.get(0);

		for (int i = 1; i < clusterIDs.size(); i++ )
		{
			result += "," + clusterIDs.get(i);
		}
		
		return result;
	}
	
	/**
	 * This method overrides the calcIdealEdgeLengths of CoseLayout
	 * and calculates the inter cluster edge lengths  
	 */
	protected void calcIdealEdgeLengths()
	{
		super.calcIdealEdgeLengths();
		
		CoSEEdge edge;
		
		for (Object obj : this.graphManager.getAllEdges())
		{
			edge = (CoSEEdge) obj;

			if (edge.getTarget().getChild() != null
					&& edge.getSource().getChild() != null)
			{
				edge.idealLength = edge.idealLength / 4;
			}
		}		
	}

	/**
	 * This method checks if 2 sorted arrays of integers are subsequences of
	 * each other.
	 */
	private boolean isSubSequence(String key1, String key2)
	{
		ArrayList<Integer> seq1 = new ArrayList<Integer>();
		ArrayList<Integer> seq2 = new ArrayList<Integer>();

		String[] ids1 = key1.split(",");
		String[] ids2 = key2.split(",");
		
		for (String idString : ids1)
		{
			if (!idString.equals(""))
			{
				seq1.add(Integer.parseInt(idString));
			}
		}
		
		for (String idString : ids2)
		{
			if (!idString.equals(""))
			{
				seq2.add(Integer.parseInt(idString));
			}
		}
		
		int size1 = seq1.size();
		int size2 = seq2.size();
		
		if (size1 < size2)
		{
			for (int i = 0; i < size2 - size1 + 1; i++)
			{
				boolean found = false;
				for (int j = i; j < i + size1; j++)
				{
					if (!seq1.get(j - i).equals(seq2.get(j)))
					{
						found = true;
						break;
					}
				}
				if (!found)
				{
					return true;
				}
			}
		} 
		else if (size1 > size2)
		{
			for (int i = 0; i < size1 - size2 + 1; i++)
			{
				boolean found = false;

				for (int j = i; j < i + size2; j++)
				{
					if (!seq2.get(j - i).equals(seq1.get(j)))
					{
						found = true;
						break;
					}
				}

				if (!found)
				{
					return true;
				}
			}
		}
		else
		{
			//this returns false, because the only case when sequences 
			//are equal should return false as well
		}
		
		return false;
	}
	
	/**
	 * This method is the main method of this layout style.
	 */
	public boolean layout()
	{
		// cluster id to cluster node mapping
		HashMap<String, LNode> clusterMap = new HashMap<String, LNode>();
		
		// edges which should be re-added after topology change
		List<LEdge> edgesToAdd = new ArrayList<LEdge>();
		
		LGraph rootGraph = this.getGraphManager().getRoot();
		List<LNode> nodeList = new ArrayList<LNode>();

		LGraph childGraph;
		LNode clusterNode;
		
		// get node list of the root graph
		
		for (Object obj : rootGraph.getNodes())
		{
			LNode node = (LNode) obj;
			nodeList.add(node);
		}
		
		// for each cluster, create a compound node, and for each
		// compound node add a graph to the root graph
		
		for (LNode node : nodeList)
		{	
			if (!node.getClusters().isEmpty())
			{
				String regionName = getRegionName(node);
				clusterNode = clusterMap.get(regionName);
				//clusterNode = clusterMap.get(node.getClusterID());
				
				if(clusterNode == null)
				{
					// create new node for current cluster
					clusterNode = this.newNode(node.vGraphObject);

					// create new child graph for this cluster
					childGraph = this.newGraph(null);
					childGraph.setMargin(this.graphMargin);

					// add new graph for the current cluster
					this.getGraphManager().add(childGraph, clusterNode);
					
					// update cluster map
					clusterMap.put(regionName, clusterNode);
					
					// add cluster node to the root graph
					rootGraph.add(clusterNode);
				}
				
				// add all incident edges of the current node
				edgesToAdd.addAll(node.getEdges());
				
				// remove node from the root graph
				rootGraph.remove(node);
				
				// add current node to its cluster
				clusterNode.getChild().add(node);
			}
		}
		
		// assign inter cluster edges
		for (String key1 : clusterMap.keySet())
		{
			for(String key2 : clusterMap.keySet())
			{
				if ( isSubSequence(key1, key2) )
				{
					CoSENode source = (CoSENode) clusterMap.get(key1);
					CoSENode destination = (CoSENode) clusterMap.get(key2);
					
					LEdge newEdge = new CoSEEdge(source, destination, null);
					
					this.graphManager.add(newEdge, source, destination);
				}
			}
		}
		
		// re-add all edges, which are removed during node remove, to the graph
		
		for (LEdge edge : edgesToAdd)
		{
			this.graphManager.add(edge, edge.getSource(), edge.getTarget());
		}
		
		// topology is changed, reset all edges
		this.graphManager.resetAllEdges();
		
		// clear edge list for future use
		edgesToAdd.clear();

		// CoSE layout is run with newly created dummy compounds
		boolean result = super.layout();

		// for debugging purposes
//		GraphMLWriter graphMLWriter =
//			new GraphMLWriter("D:\\cluster_before.graphml");
//		graphMLWriter.saveGraph(this.graphManager);

		// calculate polygons of the clusters
		for (Cluster cluster : this.graphManager.getClusterManager().getClusters())
		{
			cluster.calculatePolygon();
		}
		
		// After layout operation, remove cluster compounds and restore 
		// nodes in each compound to their previous locations

		for (String key : clusterMap.keySet())
		{
			clusterNode = clusterMap.get(key);
			
			// get node list of the cluster graph
			
			nodeList.clear();
						
			for (Object obj : clusterNode.getChild().getNodes())
			{
				LNode node = (LNode) obj;
				nodeList.add(node);
			}
			
			// remove all nodes of the cluster node and add the nodes to
			// the root graph
			
			for (LNode node : nodeList)
			{
				edgesToAdd.addAll(node.getEdges());
				clusterNode.getChild().remove(node);
				rootGraph.add(node);
			}
			
			// remove cluster node from root graph
			rootGraph.remove(clusterNode);
		}
		
		// re-add all edges, which are removed during node remove, to the graph
		
		for (LEdge edge : edgesToAdd)
		{
			this.graphManager.add(edge, edge.getSource(), edge.getTarget());
		}
		
		// topology is changed, reset all edges
		this.graphManager.resetAllEdges();
		
		return result;
	}

// -----------------------------------------------------------------------------
// Section: Class Variables
// -----------------------------------------------------------------------------
	/**
	 * Default margins of the dummy compounds corresponding to clusters;
	 * determines how much the clusters should be separated.
	 */
	public static final int DEFAULT_CLUSTER_SEPARATION = 40;
}