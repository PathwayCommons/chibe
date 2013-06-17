package org.patika.mada.algorithm;

import org.patika.mada.graph.Graph;
import org.patika.mada.graph.Node;
import org.patika.mada.util.ExperimentData;

import java.util.*;

@SuppressWarnings("unchecked")

/**
 * This class marks nodes indicating whether they are on a shortest and plus something) path of two
 * other nodes.
 * 
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class MarkShortestPlusPaths
{
	//==============================================================================================
	// Section: Instance variables
	//==============================================================================================

	/**
	 * Graph to work on
	 */
	private Graph graph;

	/**
	 * Labeling radius
	 */
	private int limit;

	/**
	 * Will mark paths of length shortest + k
	 */
	private int k;

	public MarkShortestPlusPaths(Graph graph, int limit, int k)
	{
		this.graph = graph;
		this.limit = limit;
		this.k = k;
	}

	//==============================================================================================
	// Section: Methods
	//==============================================================================================
	
	/**
	 * Make sure that distances are marked on the graph before running this.
	 * 
	 * For each node, this code checks any combination of from-to distance pairs. If the node is on 
	 * a shortest+k path between from-to, then the to_from_path_map is used to mark this.
	 */
	public void run()
	{
		for (Node node : graph.getNodes())
		{
			Map<Node, Integer> distFrom = (Map<Node, Integer>) node.getLabel(MarkDistances.DIST_FROM);
			Map<Node, Integer> distTo = (Map<Node, Integer>) node.getLabel(MarkDistances.DIST_TO);

			// Ignore nodes that are not in the middle of something

			if (distFrom == null || distTo == null)
			{
				continue;
			}

			// for each from-to pairs  check if this node is on a shortest+k path
			
			for (Node from : distFrom.keySet())
			{
				assert from.hasSignificantExperimentalChange(ExperimentData.EXPRESSION_DATA);

				// We do not want to mark this node as on its shortest path
//				if (from == node)
//				{
//					continue;
//				}

				for (Node to : distTo.keySet())
				{
					// We do not want to mark this node as on its shortest path
//					if (to == node)
//					{
//						continue;
//					}
					if (from == node)
					{
						continue;
					}

					int s = getShortestDistance(from, to);
					
					if (s <= limit)
					{
						// If shortest+k is higher than limit, then use limit
						int d = Math.min(s + k, limit);

						// If the length of the path from-to that contains this node
						// is within limits, mark it.

						int foundDistance = distFrom.get(from) + distTo.get(to);

						if (!node.isBreadthNode())
						{
							foundDistance++;
						}

						if (foundDistance <= d)
						{
							Map<Node, Set<Node>> tofromPathMap = getToFromPathMap(node);
							
							if (!tofromPathMap.containsKey(to))
							{
								tofromPathMap.put(to, new HashSet<Node>());
							}
							
							Set<Node> fromSet = tofromPathMap.get(to);
							fromSet.add(from);
							
							// So, during a backwards traversing, starting at "to", when we visit 
							// this node, then we will know where to go.
						}
					}
				}
			}
		}
	}
	
	/**
	 * Finds the length of the shortest path between nodes from-to. This uses the labels on nodes
	 * which was previously introduced in MarkDistances algorithm.
	 */
	private int getShortestDistance(Node from, Node to)
	{
		Map<Node, Integer> distTo = (Map<Node, Integer>) from.getLabel(MarkDistances.DIST_TO);
		
		int s = Integer.MAX_VALUE;
		
		if (distTo.containsKey(to))
		{
			s = distTo.get(to);
		}
		return s;
	}
	
	/**
	 * Creates or gets to_from_path_map.
	 */
	private Map<Node, Set<Node>> getToFromPathMap(Node node)
	{
		if (!node.hasLabel(TO_FROM_PATH_MAP))
		{
			node.putLabel(TO_FROM_PATH_MAP, new HashMap<Node, Set<Node>>());
		}
		
		return (Map<Node, Set<Node>>) node.getLabel(TO_FROM_PATH_MAP);
	}
	
	public void clearLabels()
	{
		graph.removeLabels(Arrays.asList(TO_FROM_PATH_MAP,
			MarkDistances.DIST_FROM, MarkDistances.DIST_TO));
	}
	
	//==============================================================================================
	// Section: Class constants
	//==============================================================================================
	
	/**
	 * Used to mark nodes on interesting paths.
	 */
	public static final String TO_FROM_PATH_MAP = "TO_FROM_PATH_MAP";
}
