package org.patika.mada.algorithm;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;

/**
 * Searches the neighborhood of given source set of nodes
 * 
 * @author Shatlyk Ashyralyev
 */
public class LocalNeighborhoodQuery
{
    /**
     * Set of source nodes.
     */
	private Set<Node> sourceNodes;
    
    /**
     * Booleans to determine the directin of search.
     */
    private boolean isUpstream;
    private boolean isDownstream;

    /**
     * Stop distance.
     */
    private int limit;

    /**
     * Maps to hold forward and backward BFS results
     */
    private Map<GraphObject, Integer> mapBackward;
	private Map<GraphObject, Integer> mapForward;
    
    /**
     * Result Set of Neighborhood Query
     */
    private Set<GraphObject> queryResult = new HashSet<GraphObject>();
    
    /**
     * Constructor for Neighborhood Query.
     */
    public LocalNeighborhoodQuery(Set<Node> sourceNodes, boolean isUpstream, boolean isDownstream, int limit)
    {
        this.sourceNodes = sourceNodes;
        this.isUpstream = isUpstream;
        this.isDownstream = isDownstream;
        this.limit = limit;
    }  
    
    /**
     * Method to run query
     */
    public Set<GraphObject> run()
    {
    	BFS bfsBackward = null;
		BFS bfsForward = null;
		
    	//if upstream is selected
		if (isUpstream)
		{
			//run BFS in upstream direction
			bfsBackward = 
				new BFS(sourceNodes, null, false, this.limit);

			mapBackward = bfsBackward.run();

			//add result of BFS to result Set
			queryResult.addAll(mapBackward.keySet());
		}
		//if downstream is selected
		if (isDownstream)
		{
			//run BFS in downstream direction
			bfsForward = 
				new BFS(sourceNodes, null, true, this.limit);

			mapForward = bfsForward.run();

			//add result of BFS to result Set
			queryResult.addAll(mapForward.keySet());
		}
    	
        //Return the result of query
    	return this.queryResult;
    }
}