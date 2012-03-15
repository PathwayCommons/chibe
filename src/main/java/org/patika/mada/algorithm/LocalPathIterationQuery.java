package org.patika.mada.algorithm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gvt.model.EntityAssociated;
import org.gvt.model.BioPAXGraph;
import org.gvt.util.EntityHolder;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;

/**
 * Finds all-pair shortest paths.
 * Shows there shortest paths in descending order, 
 * so that longest shortest-path can be selected to highlight.
 *
 * @author Shatlyk Ashyralyev
 */
public class LocalPathIterationQuery
{
	/**
	 * Root Graph
	 */
	private BioPAXGraph rootGraph;
	
	/**
	 * Nodes of graph
	 */
	private Set<EntityHolder> allEntities;
	
    /**
     * Node Pairs with current shortest paths
     */
    private Map<NodePair, NodePair>  shortestPaths;
        
    /**
     * Constructor for Common Stream with Entity States.
     */
    public LocalPathIterationQuery(BioPAXGraph rootGraph)
    {
        this.rootGraph = rootGraph;
        this.allEntities = this.rootGraph.getAllEntities();
        this.shortestPaths = new HashMap<NodePair, NodePair>();
    }
    
    /**
     * Method to run query
     */
    public Set<NodePair> run()
    {
    	BFS bfs = null;

    	//for every node
    	for (EntityHolder entity : allEntities)
		{
			//States of entity in Node type
			Set<Node> entityNodeStates = new HashSet<Node>();

			//States of entity in GraphObject type
			Set<Node> entityStates = rootGraph.getRelatedStates(entity);

			//Convert GraphObjects to Nodes
			for (GraphObject go : entityStates)
			{
				if (go instanceof Node)
				{
					entityNodeStates.add((Node) go);
				}
			}

			//new BFS
			bfs = new BFS(entityNodeStates, null, true, Integer.MAX_VALUE);
			Map<GraphObject, Integer> BFSResult = bfs.run();

			//for ever graphObject from result of bfs result
			for (GraphObject graphObject : BFSResult.keySet())
			{
				//if graphObject is node and not a complex member
				if ((graphObject instanceof Node) && !(((Node) graphObject).isComplexMember()))
				{
					//previously calculated shortest distance
					int previousShortestDist;
					//shortest distance from current BFS
					int currentShortestDist;

					//element of resultant set of BFS
					Node nodeB = (Node) graphObject;

					//find label from result of BFS
					currentShortestDist = BFSResult.get(graphObject);

					//new distance between two nodes
					if(nodeB.isBreadthNode())
					{
						NodePair keyPair =
							new NodePair(entity, ((EntityAssociated) nodeB).getEntity(), currentShortestDist);

						//if shortest path is previously calculated
						if (shortestPaths.containsKey(keyPair))
						{
							previousShortestDist =
								shortestPaths.get(keyPair).getCurrentShortestPath();

							//if short then change distance in mapping
							if (currentShortestDist < previousShortestDist)
							{
								shortestPaths.get(keyPair).
									setCurrentShortestPath(currentShortestDist);
							}
						}
						//if shortest path is calculates for first time
						else
						{
							shortestPaths.put(keyPair, keyPair);
						}
					}
				}
			}
		}

    	//New array of pairs
		NodePair[] pairArray = new NodePair[shortestPaths.size()];
		//Convert ArrayList to array
		pairArray = shortestPaths.keySet().toArray(pairArray);
		//Sort pairs according to lables
		Arrays.sort(pairArray);

		Set<NodePair> queryResult = new HashSet<NodePair>();
		
		//convert array to Set of NodePairs
		for (NodePair pair : pairArray)
		{
			queryResult.add(pair);
		}
		
    	//return query result
    	return queryResult;
    }
    
    public class NodePair implements Comparable
    {
    	//1st and 2nd element of node pair
    	private EntityHolder nodeA;
    	private EntityHolder nodeB;
    	
    	//current shortest path between two nodes
    	private Integer currentShortestPath;
    	
    	/**
    	 * Constructor
    	 */
    	public NodePair(EntityHolder nodeA, EntityHolder nodeB, int currentShortestPath)
    	{
    		this.nodeA = nodeA;
    		this.nodeB = nodeB;
    		this.currentShortestPath = currentShortestPath;
    	}
    	
    	/**
    	 * Getters
    	 */
    	public EntityHolder getNodeA()
		{
			return this.nodeA;
		}

    	public EntityHolder getNodeB()
		{
			return this.nodeB;
		}
    	
    	public int getCurrentShortestPath()
    	{
    		return this.currentShortestPath;
    	}
    	
    	public void setCurrentShortestPath(int currentShortestPath)
    	{
    		this.currentShortestPath = currentShortestPath;
    	}

		/**
    	 * Check if two NodePairs are equal
    	 */
    	public boolean equals(Object obj)
    	{
    		//if not node pair return false
    		if (!(obj instanceof NodePair))
    		{
    			return false;
    		}
    		
    		//cast object to nodepair
    		NodePair pair = (NodePair) obj;
    		
    		//if both nodes are equal return true
    		if (this.nodeA.equals(pair.getNodeA()) &&
    			this.nodeB.equals(pair.getNodeB()))
    		{
    			return true;
    		}
   		
    		//else return false
    		return false;
    	}
    	
    	/**
    	 * HashCode for nodePair
    	 */
    	public int hashCode()
    	{
    		//hash code of both nodes
    		int hashA = this.nodeA.hashCode();
    		int hashB = this.nodeB.hashCode();
    		
    		//return sum of hashcodes of two nodes
    		return hashA + hashB;	
    	}

		public int compareTo(Object obj)
		{
			//if not node pair return false
    		if (!(obj instanceof NodePair))
    		{
    			return 0;
    		}
    		
    		//cast object to nodepair
    		NodePair pair = (NodePair) obj;

			// Compare nodePairs based on distances, if equal on first node,
			// if equal on second node.
			int result = this.currentShortestPath.compareTo(pair.getCurrentShortestPath());
			if (result == 0)
			{
				result = this.nodeA.getName().compareTo(pair.getNodeA().getName());
			}

			if (result == 0)
			{
				result = this.nodeB.getName().compareTo(pair.getNodeB().getName());
			}

			return result;
		}
    }
}
