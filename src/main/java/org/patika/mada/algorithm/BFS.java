package org.patika.mada.algorithm;

import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;
import org.patika.mada.graph.Edge;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Implements breadth-first search. Takes a set of source nodes, distance limit and labels nodes
 * towards one direction, with their breadth distances.
 *
 * @author Ozgun Babur
 * @author Merve Cakir
 */
public class BFS
{
	/**
	 * Distance labels. Missing label interpreted as infinitive.
	 */
	private Map<GraphObject, Integer> dist;

	/**
	 * Color labels. Missing color interpreted as white.
	 */
	private Map<GraphObject, Integer> colors;

	/**
	 * BFS starts from source nodes. They get the label 0.
	 */
	private Set<Node> sourceSet;

	/**
	 * BFS will not further traverse neighbors of any node in the stopSet.
	 */
	private Set<Node> stopSet;

	/**
	 * Whether the direction is FORWARD, it is REVERSE otherwise.
	 */
	private boolean isFwd;

	/**
	 * Stop distance.
	 */
	private int limit;

	public BFS(Set<Node> sourceSet, Set<Node> stopSet, boolean direction, int limit)
	{
		this.sourceSet = sourceSet;
		this.stopSet = stopSet;
		this.isFwd = direction;
		this.limit = limit;
	}

	public Map<GraphObject, Integer> run()
	{
		// Initialize label, maps and queue

		dist = new HashMap<GraphObject, Integer>();
		colors = new HashMap<GraphObject, Integer>();
		LinkedList<Node> queue = new LinkedList<Node>();


		// Initialize dist and color of source set

		for (Node source : sourceSet)
		{
			setLabel(source, 0);
			setColor(source, GRAY);
		}

		// Add all source nodes to the queue if traversal is needed

		if (limit > 0)
		{
			queue.addAll(sourceSet);
		}

		// Process the queue

		while (!queue.isEmpty())
		{
			Node current = queue.remove(0);

			// Process edges towards the direction

			for (Edge edge : isFwd ? current.getDownstream() : current.getUpstream())
			{
				// Label the edge considering direction of traversal and type of current node

				if (isFwd || !current.isBreadthNode() || edge.isEquivalenceEdge())
				{
					setLabel(edge, getLabel(current));
				}
				else
				{
					setLabel(edge, getLabel(current) + 1);
				}

				// Get the other end of the edge
				Node neigh = isFwd ? edge.getTargetNode() : edge.getSourceNode();

				// Process the neighbor if not processed or not in queue

				if (getColor(neigh) == WHITE)
				{
					// Label the neighbor according to the search direction and node type

					if (!neigh.isBreadthNode() || !isFwd || edge.isEquivalenceEdge())
					{
						setLabel(neigh, getLabel(edge));
					}
					else
					{
						setLabel(neigh, getLabel(current) + 1);
					}

					// Check if we need to stop traversing the neighbor, enqueue otherwise
					
					if ((stopSet == null || !stopSet.contains(neigh)) &&
						(!neigh.isBreadthNode() || getLabel(neigh) < limit))
					{
						setColor(neigh, GRAY);

						// Enqueue the node according to its type

						if (neigh.isBreadthNode())
						{
							queue.addLast(neigh);
						}
						else
						{
							// Non-breadth nodes are added in front of the queue
							queue.addFirst(neigh);
						}
					}
					else
					{
						// If we do not want to traverse this meighbor, we paint it black
						setColor(neigh, BLACK);
					}
				}
			}

			// Current node is processed
			setColor(current, BLACK);
		}

		return dist;
	}

	private int getColor(Node node)
	{
		if (!colors.containsKey(node))
		{
			// Absence of color is interpreted as white
			return WHITE;
		}
		else
		{
			return colors.get(node);
		}
	}

	private void setColor(Node node, int color)
	{
		colors.put(node, color);
	}

	public int getLabel(GraphObject go)
	{
		if (!dist.containsKey(go))
		{
			// Absence of label is interpreted as infinite
			return Integer.MAX_VALUE-(limit*2);
		}
		else
		{
			return dist.get(go);
		}
	}

	private void setLabel(GraphObject go, int label)
	{
		dist.put(go, label);
	}

	/**
	 * Color white indicates the node is not processed.
	 */
	public static final int WHITE = 0;

	/**
	 * Color gray indicates that the node is in queue waiting to be procecessed.
	 */
	public static final int GRAY = 1;

	/**
	 * Color black indicates that the node was processed.
	 */
	public static final int BLACK = 2;
}
