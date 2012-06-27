package org.patika.mada.algorithm;

import org.patika.mada.graph.Edge;
import org.patika.mada.graph.Graph;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Does not handle compound nodes.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class GraphOfInterets
{
	private Set<Node> seed;
	private boolean directed;
	private Graph graph;
	private int limit;
	Set<GraphObject> goi;

	public GraphOfInterets(Set<Node> seed, boolean directed, Graph graph, int limit)
	{
		this.seed = seed;
		this.directed = directed;
		this.graph = graph;
		this.limit = limit;
	}

	public Set<GraphObject> run()
	{
//		System.out.println("---------\nGOI algorithm starting");
//		System.out.println("seed.size() = " + seed.size());
//		System.out.println("graph.getNodes() = " + graph.getNodes().size());
//		System.out.println("graph.getEdges() = " + graph.getEdges().size());
//		System.out.println("directed = " + directed);
//		System.out.println("limit = " + limit);

		goi = new HashSet<GraphObject>();

		// Init node and edge distances. seed <- 0, non-seed <- max
		for (Node node : graph.getNodes())
		{
			initGraphObject(node);
		}
		for (Edge edge : graph.getEdges())
		{
			initGraphObject(edge);
		}

		if (directed)
		{
			runBFS_directed(FORWARD);
			runBFS_directed(BACKWARD);
		}
		else
		{
			runBFS_undirected();
		}

		// Select graph objects that are traversed with the BFS. It is important to process nodes
		// before edges.

		selectSatisfyingElements(graph.getNodes());
		selectSatisfyingElements(graph.getEdges());

		// Prune so that no non-seed degree-1 nodes remain
		pruneResult();

		// Remove all algorithm specific labels
		clearLabels();

		assert checkEdgeSanity();

		return goi;
	}

	private void runBFS_directed(boolean direction)
	{
		assert directed;

		// Initialize queue to contain all seed nodes

		LinkedList<Node> queue = new LinkedList<Node>();
		queue.addAll(seed);

		// Run BFS forward or backward till queue is not empty

		while (!queue.isEmpty())
		{
			Node node = queue.poll();
			BFS_directed(node, direction, queue);
		}
	}

	private void runBFS_undirected()
	{
		assert !directed;

		// Initialize queue to contain all seed nodes

		LinkedList<Node> queue = new LinkedList<Node>();
		queue.addAll(seed);

		// Run BFS till queue is not empty

		while (!queue.isEmpty())
		{
			Node node = queue.poll();
			BFS_undirected(node, queue);
		}
	}

	private void BFS_directed(Node node, boolean forward, LinkedList<Node> queue)
	{
		assert directed;

		if (forward)
		{
			BFStep(node, DOWNSTREAM, DIST_FORWARD, queue);
		}
		else
		{
			BFStep(node, UPSTREAM, DIST_BACKWARD, queue);
		}
	}

	private void BFStep(Node node, boolean upstr, String label, LinkedList<Node> queue)
	{
		int d = (Integer) node.getLabel(label);

		if (d < limit)
		{
			for (Edge edge : upstr? node.getUpstream() : node.getDownstream())
			{
				Node n = upstr ? edge.getSourceNode() : edge.getTargetNode();

				edge.putLabel(label, !upstr && label.equals(DIST_FORWARD) ? d + 1 : d);

				if (n.hasLabel(label))
				{
					int d_n = (Integer) n.getLabel(label);

					if (d_n > d + 1)
					{
						n.putLabel(label, d + 1);
						if (d + 1 < limit && !queue.contains(n)) queue.add(n);
					}
				}
			}
		}
	}

	private void BFS_undirected(Node node, LinkedList<Node> queue)
	{
		assert !directed;

		BFStep(node, UPSTREAM, DIST, queue);
		BFStep(node, DOWNSTREAM, DIST, queue);
	}

	private void initGraphObject(GraphObject obj)
	{
		if (seed.contains(obj))
		{
			initSeed(obj);
		}
		else
		{
			initNonSeed(obj);
		}
	}

	private void initSeed(GraphObject obj)
	{
		if (directed)
		{
			obj.putLabel(DIST_FORWARD, 0);
			obj.putLabel(DIST_BACKWARD, 0);
		}
		else
		{
			obj.putLabel(DIST, 0);
		}
	}
	
	private void initNonSeed(GraphObject obj)
	{
		if (directed)
		{
			obj.putLabel(DIST_FORWARD, limit * 2);
			obj.putLabel(DIST_BACKWARD, limit * 2);
		}
		else
		{
			obj.putLabel(DIST, limit * 2);
		}
	}

	private void selectSatisfyingElements(Collection<? extends GraphObject> ojects)
	{
		for (GraphObject go : ojects)
		{
			if (distanceSatisfies(go))
			{
				if (go instanceof Edge)
				{
					Edge edge = (Edge) go;
					if (!goi.contains(edge.getSourceNode()) || !goi.contains(edge.getTargetNode()))
					{
						continue;
					}
				}
				goi.add(go);
			}
		}
	}

	private boolean distanceSatisfies(GraphObject go)
	{
		if (directed)
		{
			return (Integer) go.getLabel(DIST_FORWARD) +
					(Integer) go.getLabel(DIST_BACKWARD) <= limit;
		}
		else
		{
			return (Integer) go.getLabel(DIST) <= limit;
		}
	}

	private void pruneResult()
	{
		for (GraphObject go : new HashSet<GraphObject>(goi))
		{
			if (go instanceof Node)
			{
				prune((Node) go);
			}
		}
	}

	private void prune(Node node)
	{
		if (goi.contains(node) && !seed.contains(node))
		{
			if (getNeighborsInResult(node).size() <= 1)
			{
				goi.remove(node);
				goi.removeAll(node.getUpstream());
				goi.removeAll(node.getDownstream());

				for (Node n : getNeighborsOverResultEdges(node))
				{
					prune(n);
				}
			}
		}
	}


	private Set<Node> getNeighborsOverResultEdges(Node node)
	{
		Set<Node> set = new HashSet<Node>();

		for (Edge edge : node.getUpstream())
		{
			if (goi.contains(edge))
			{
				set.add(edge.getSourceNode());
			}
		}
		for (Edge edge : node.getDownstream())
		{
			if (goi.contains(edge))
			{
				set.add(edge.getTargetNode());
			}
		}
		set.remove(node);
		return set;
	}

	private Set<Node> getNeighborsInResult(Node node)
	{
		Set<Node> set = getNeighborsOverResultEdges(node);
		set.retainAll(goi);
		return set;
	}

	private void clearLabels()
	{
		for (Node node : graph.getNodes())
		{
			if (directed)
			{
				node.removeLabel(DIST_FORWARD);
				node.removeLabel(DIST_BACKWARD);
			}
			else
			{
				node.removeLabel(DIST);
			}
		}
		for (Edge edge : graph.getEdges())
		{
			if (directed)
			{
				edge.removeLabel(DIST_FORWARD);
				edge.removeLabel(DIST_BACKWARD);
			}
			else
			{
				edge.removeLabel(DIST);
			}
		}
	}

	private boolean checkEdgeSanity()
	{
		for (GraphObject go : goi)
		{
			if (go instanceof Edge)
			{
				Edge edge = (Edge) go;

				assert goi.contains(edge.getSourceNode());
				assert goi.contains(edge.getTargetNode());
			}
		}
		return true;
	}

	public static final String DIST = "DIST";
	public static final String DIST_FORWARD = "DIST_FORWARD";
	public static final String DIST_BACKWARD = "DIST_BACKWARD";

	public static final boolean FORWARD = true;
	public static final boolean BACKWARD = false;
	public static final boolean UPSTREAM = true;
	public static final boolean DOWNSTREAM = false;
}
