package org.patika.mada.algorithm;

import org.patika.mada.graph.Edge;
import org.patika.mada.graph.Graph;
import org.patika.mada.graph.Node;
import org.patika.mada.util.ExperimentData;

import java.util.*;

@SuppressWarnings("unchecked")
/**
 * Marks bfs distances between significant nodes.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class MarkDistances
{
	/**
	 * Graph to work on
	 */
	private Graph graph;

	/**
	 * Labeling radius
	 */
	private int limit;

	public MarkDistances(Graph graph, int limit)
	{
		this.graph = graph;
		this.limit = limit;
	}

	/**
	 * Algorithm runs on the graphs and labels in the radius specified with the limit.
	 */
	public void run()
	{
		for (Node node : graph.getNodes())
		{
			if (node.hasSignificantExperimentalChange(ExperimentData.EXPRESSION_DATA))
			{
				labelBFS(node, FORWARD);
				clearStepLabels();
				labelBFS(node, BACKWARD);
				clearStepLabels();
			}
		}
	}
	
	/**
	 * This method is called for each significant node. It runs a forward bfs search
	 * that will label the neighborhood with the distance from this node.
	 */
	private void labelBFS(Node root, boolean direction)
	{
		// Label node to be distance 0 from or to itself
		putDistance(root, root, direction, 0);
		
		LinkedList<Node> queue = new LinkedList<Node>();
		queue.add(root);
		
		while(!queue.isEmpty())
		{
			bfsStep(queue, root, direction);
		}
	}
	
	/**
	 * Assigns distances to and enqueues downstream or upstream according to direction.
	 */
	private void bfsStep(LinkedList<Node> queue, Node root, boolean direction)
	{
		// Next node in queue
		Node node = queue.poll();

		// Get previously assigned distance. This will not change, but will be used for labeling
		// neighbors.

		assert !isVisited(node) || node.hasLabel(VISITED_WO_CH) || node.hasLabel(VISITED_WO_PR);
		
		int d = !isVisited(node) ? getDistance(node, root, direction) : 
			(Integer) node.getLabel(TEMP_DIST);

		// Put parents in front of the queue if not already coming from a parent.
		
		if (!node.hasLabel(PARENT_LOCK) && (!isVisited(node) || node.hasLabel(VISITED_WO_PR)))
		{
			for (Node parent : node.getParents())
			{
				if (!isVisited(parent) || parent.hasLabel(VISITED_WO_PR))
				{
					if (queue.contains(parent))
					{
						queue.remove(parent);
					}
					queue.addFirst(parent);
					
					if (!isVisited(parent))
					{
						parent.putLabel(CHILD_LOCK);
						putDistance(parent, root, direction, d);
					}
					else
					{
						parent.putLabel(TEMP_DIST, d);
					}
				}
			}
		}
		
		// Put children in front of the queue if not already coming from a child.
		
		if (!node.hasLabel(CHILD_LOCK) && (!isVisited(node) || node.hasLabel(VISITED_WO_CH)))
		{
			for (Node child : node.getChildren())
			{
				if (!isVisited(child) || child.hasLabel(VISITED_WO_CH))
				{
					if (queue.contains(child))
					{
						queue.remove(child);
					}
					queue.addFirst(child);
					
					if (!isVisited(child))
					{
						putDistance(child, root, direction, d);
						child.putLabel(PARENT_LOCK);						
					}
					else
					{
						child.putLabel(TEMP_DIST, d);
					}
				}
			}
		}
		
		// Proceed to up/downstream only if not reached the distance limit
		
		if (d < limit && !isVisited(node))
		{
			// Iterate neighbors
	
			for (Edge edge : getEdges(node, direction))
			{
				// Do not consider non-causative edges
				if (!edge.isCausative()) continue;

				Node neigh = getEdgeEnd(edge, direction);

				boolean step = neigh.isBreadthNode() && edge.isBreadthEdge();
				
				// Process only if neighbor is not already in the queue.
				
				if (!queue.contains(neigh))
				{
					if (!isVisited(neigh))
					{
						putDistance(neigh, root, direction, step ? d+1 : d);
		
						// Enqueue neighbor

						if (step)
						{
							queue.addLast(neigh);
						}
						else
						{
							if (queue.contains(neigh))
							{
								queue.remove(neigh);
							}
							queue.addFirst(neigh);
						}
					}
					else if (neigh.hasLabel(VISITED_WO_CH) || neigh.hasLabel(VISITED_WO_PR))
					{
						neigh.putLabel(TEMP_DIST, step ? d+1 : d);

						if (step)
						{
							queue.addLast(neigh);
						}
						else
						{
							if (queue.contains(neigh))
							{
								queue.remove(neigh);
							}
							queue.addFirst(neigh);
						}
					}
				}
			}
		}
		
		// Mark the node as visited.
		markVisited(node);
		
		if (node.hasLabel(PARENT_LOCK))
		{
			node.removeLabel(PARENT_LOCK);
			node.putLabel(VISITED_WO_PR);
		}
		else if (node.hasLabel(CHILD_LOCK))
		{
			node.removeLabel(CHILD_LOCK);
			node.putLabel(VISITED_WO_CH);
		}
		else if (node.hasLabel(VISITED_WO_CH))
		{
			node.removeLabel(VISITED_WO_CH);
		}
		else if (node.hasLabel(VISITED_WO_PR))
		{
			node.removeLabel(VISITED_WO_PR);
		}
	}

	/**
	 * Gets the related distance map of the node. Creates one if not exists.
	 */
	private Map<Node, Integer> getDistMap(Node node, boolean direction)
	{
		if (!node.hasLabel((direction == FORWARD) ? DIST_FROM : DIST_TO))
		{
			node.putLabel((direction == FORWARD) ? DIST_FROM : DIST_TO, new HashMap<Node, Integer>());
		}
		
		return (Map<Node, Integer>) node.getLabel((direction == FORWARD) ? DIST_FROM : DIST_TO);
	}

	/**
	 * Gets the latest calculated distance of the node from/to the reference according to the 
	 * direction.
	 */
	private int getDistance(Node node, Node ref, boolean direction)
	{
		Map<Node, Integer> distmap = getDistMap(node, direction);
		int d = Integer.MAX_VALUE;
		if (distmap.containsKey(ref))
		{
			d = distmap.get(ref);
		}
		return d;
	}

	/**
	 * Sets the distance of the node from/to the reference node according to the direction to the 
	 * specified value.
	 */
	private void putDistance(Node node, Node ref, boolean direction, int d)
	{
		Map<Node, Integer> distmap = getDistMap(node, direction);
		distmap.put(ref, d);
	}

	/**
	 * Gets the next end of the edge according to direction.
	 */
	private Node getEdgeEnd(Edge edge, boolean direction)
	{
		return (direction == FORWARD) ? edge.getTargetNode() : edge.getSourceNode();
	}
	
	/**
	 * Get edges of the node according to the direction.
	 */
	private Collection<? extends Edge> getEdges(Node node, boolean direction)
	{
		return (direction == FORWARD) ? node.getDownstream() : node.getUpstream();
	}
	
	/**
	 * Checks if the node was processed before.
	 */
	private void markVisited(Node node)
	{
		node.putLabel(VISITED, true);
	}

	/**
	 * Checks if the node is visited. This will return true only if
	 * the node is processed and removed from queue.
	 */
	private boolean isVisited(Node node)
	{
		// We do not check the value here because nodes are labeled with this
		// key only if they are visited, and this is irreversible.
		return node.hasLabel(VISITED);
	}
	
	/**
	 * Clears distance labels that are attached during run of this algorithm.
	 * This method is public because we need these labels stay for somewhile
	 * and another class will call this when it finishes.
	 */
	public void clearDistLabels()
	{
		for (Node node : graph.getNodes())
		{
			node.removeLabel(DIST_FROM);
			node.removeLabel(DIST_TO);
		}
	}
	
	/**
	 * Clears visited tags that are attached during run of this algorithm.
	 */
	private void clearStepLabels()
	{
		graph.removeLabels(Arrays.asList(VISITED, VISITED_WO_CH, VISITED_WO_PR, TEMP_DIST));
	}

	public static final String DIST_TO = "DIST_TO";
	public static final String DIST_FROM = "DIST_FROM";
	public static final boolean FORWARD = true;
	public static final boolean BACKWARD = false;
	public static final String VISITED = "VISITED";
	public static final String VISITED_WO_CH = "VISITED_WO_CH";
	public static final String VISITED_WO_PR = "VISITED_WO_PR";
	public static final String TEMP_DIST = "TEMP_DIST";
	
	/**
	 * If this lock is present, then search cannot proceed to equivalent parents.
	 */
	public static final Integer PARENT_LOCK = 13;

	/**
	 * If this lock is present, then search cannot proceed to equivalent children.
	 */
	public static final Integer CHILD_LOCK = 14;
	
	
}
