package org.patika.mada.algorithm;

import org.gvt.model.basicsif.BasicSIFEdge;
import org.patika.mada.graph.Edge;
import org.patika.mada.graph.Graph;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;

import java.util.*;

/**
 * Does not handle compound nodes. For binary netowrks only.
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

	private Map<GraphObject, Map<Integer, Set<Node>>> fwdLabel;
	private Map<GraphObject, Map<Integer, Set<Node>>> bkwLabel;
	private Map<GraphObject, Map<Integer, Set<Node>>> labelMap;

	private Set<GraphObject> visitedObjects;

	public GraphOfInterets(Set<Node> seed, boolean directed, Graph graph, int limit)
	{
		this.seed = seed;
		this.directed = directed;
		this.graph = graph;
		this.limit = limit;
	}

	public Set<GraphObject> run()
	{
		goi = new HashSet<GraphObject>();
		visitedObjects = new HashSet<GraphObject>();

		if (directed)
		{
			this.fwdLabel = new HashMap<GraphObject, Map<Integer, Set<Node>>>();
			this.bkwLabel = new HashMap<GraphObject, Map<Integer, Set<Node>>>();
		}
		else this.labelMap = new HashMap<GraphObject, Map<Integer, Set<Node>>>();

		for (Node node : seed)
		{
			initSeed(node);

			if (directed)
			{
				runBFS_directed(node, FORWARD);
				runBFS_directed(node, BACKWARD);
			}
			else
			{
				runBFS_undirected(node);
			}

			// Record distances for that seed node
			recordDistances(node);

			// Remove all algorithm specific labels
			clearLabels();
		}

		// Reformat the label maps

		if (directed)
		{
			mergeLabels(fwdLabel);
			mergeLabels(bkwLabel);
		}
		else mergeLabels(labelMap);


		// Select graph objects that are traversed with the BFS. It is important to process nodes
		// before edges.
		selectSatisfyingElements();

		// Prune so that no non-seed degree-1 nodes remain
		pruneResult();

		assert checkEdgeSanity();

		return goi;
	}

	private void runBFS_directed(Node seed, boolean direction)
	{
		assert directed;

		// Initialize queue to contain all seed nodes

		LinkedList<Node> queue = new LinkedList<Node>();
		queue.add(seed);

		// Run BFS forward or backward till queue is not empty

		while (!queue.isEmpty())
		{
			Node node = queue.poll();
			BFS_directed(node, direction, queue);
		}
	}

	private void runBFS_undirected(Node seed)
	{
		assert !directed;

		// Initialize queue to contain all seed nodes

		LinkedList<Node> queue = new LinkedList<Node>();
		queue.add(seed);

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
		int d = getLabel(node, label);

		if (d < limit)
		{
			for (Edge edge : upstr? node.getUpstream() : node.getDownstream())
			{
				setLabel(edge, label, !upstr && label.equals(DIST_FORWARD) ? d + 1 : d);

				Node n = upstr ? edge.getSourceNode() : edge.getTargetNode();

				int d_n = getLabel(n, label);

				if (d_n > d + 1)
				{
					setLabel(n, label, d + 1);
					if (d + 1 < limit && !queue.contains(n)) queue.add(n);
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

	private void initSeed(GraphObject obj)
	{
		if (directed)
		{
			setLabel(obj, DIST_FORWARD, 0);
			setLabel(obj, DIST_BACKWARD, 0);
		}
		else
		{
			setLabel(obj, DIST, 0);
		}
	}
	
	private void selectSatisfyingElements()
	{
		for (GraphObject go : visitedObjects)
		{
			if (distanceSatisfies(go))
			{
				goi.add(go);
			}
		}

		// Remove edges in the result whose node is not in the result

		Set<Edge> extra = new HashSet<Edge>();
		for (GraphObject go : goi)
		{
			if (go instanceof Edge)
			{
				Edge edge = (Edge) go;
				if (!goi.contains(edge.getSourceNode()) || !goi.contains(edge.getTargetNode()))
				{
					extra.add(edge);
				}
			}
		}
		goi.removeAll(extra);
	}

	private boolean distanceSatisfies(GraphObject go)
	{
		if (directed)
		{
			if (!fwdLabel.containsKey(go) || !bkwLabel.containsKey(go)) return false;

			for (Integer i : fwdLabel.get(go).keySet())
			{
				for (Integer j : bkwLabel.get(go).keySet())
				{
					if (i + j <= limit)
					{
						if (setsSatisfy(fwdLabel.get(go).get(i), bkwLabel.get(go).get(j)))
							return true;
					}
				}
			}
		}
		else
		{
			if (!labelMap.containsKey(go)) return false;

			for (Integer i : labelMap.get(go).keySet())
			{
				if (i <= limit && labelMap.get(go).get(i).size() > 1) return true;
			}
		}

		return false;
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
		for (GraphObject go : visitedObjects)
		{
			if (directed)
			{
				go.removeLabel(DIST_FORWARD);
				go.removeLabel(DIST_BACKWARD);
			}
			else
			{
				go.removeLabel(DIST);
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

	private int getLabel(GraphObject go, String label)
	{
		if (go.hasLabel(label)) return (Integer) go.getLabel(label);
		else return Integer.MAX_VALUE / 2;
	}
	private void setLabel(GraphObject go, String label, Integer value)
	{
		go.putLabel(label, value);
		visitedObjects.add(go);
	}

	private void recordDistances(Node seed)
	{
		for (GraphObject go : visitedObjects)
		{
			if (directed)
			{
				recordDistance(go, seed, DIST_FORWARD, fwdLabel);
				recordDistance(go, seed, DIST_BACKWARD, bkwLabel);
			}
			else recordDistance(go, seed, DIST, labelMap);
		}
	}

	private void recordDistance(GraphObject go, Node seed, String label,
		Map<GraphObject, Map<Integer, Set<Node>>> map)
	{
		int d = getLabel(go, label);
		if (d > limit) return;
		if (!map.containsKey(go)) map.put(go, new HashMap<Integer, Set<Node>>());
		if (!map.get(go).containsKey(d)) map.get(go).put(d, new HashSet<Node>());
		map.get(go).get(d).add(seed);
	}

	private void mergeLabels(Map<GraphObject, Map<Integer, Set<Node>>> map)
	{
		for (GraphObject go : map.keySet())
		{
			for (int i = 0; i < limit; i++)
			{
				if (map.get(go).containsKey(i))
				{
					for (int j = i+1; j <= limit; j++)
					{
						if (map.get(go).containsKey(j))
						{
							map.get(go).get(j).addAll(map.get(go).get(i));
						}
					}
				}
			}
		}
	}

	private boolean setsSatisfy(Set<Node> set1, Set<Node> set2)
	{
		assert !set1.isEmpty();
		assert !set2.isEmpty();

		return set1.size() > 1 || set2.size() > 1 ||
			!set1.containsAll(set2) || !set2.containsAll(set1);
	}

	public static final String DIST = "DIST";
	public static final String DIST_FORWARD = "DIST_FORWARD";
	public static final String DIST_BACKWARD = "DIST_BACKWARD";

	public static final boolean FORWARD = true;
	public static final boolean BACKWARD = false;
	public static final boolean UPSTREAM = true;
	public static final boolean DOWNSTREAM = false;
}
