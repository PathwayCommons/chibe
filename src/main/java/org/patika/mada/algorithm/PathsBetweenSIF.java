package org.patika.mada.algorithm;

import org.patika.mada.graph.Edge;
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
public class PathsBetweenSIF
{
	private Set<Node> sourceSeed;
	private Set<Node> targetSeed;
	private boolean directed;
	private int limit;

	/**
	 * If false, then any path in length limit will come. If true, shortest=k limit will be used,
	 * again bounded by limit.
	 */
	private boolean useShortestPlusK = false;

	/**
	 * If true, will ignore cycles.
	 */
	private boolean ignoreSelfLoops = true;

	/**
	 * If true, then a shortest path will be considered for each distinct pair. If false, then a
	 * shortest path length per gene will be used.
	 */
	private boolean considerAllPairs = false;
	/**
	 * If true, and if the reverse path is longer, it wont be retrieved.
	 */
	private boolean shortestAnyDir = true;
	private Map<Node, Map<Node, Integer>> shortestPairLengths;
	private Map<Node, Integer> shortestSingleLengths;
	private int k = 0;

	Set<GraphObject> goi;

	private Map<GraphObject, Map<Integer, Set<Node>>> fwdLabel;
	private Map<GraphObject, Map<Integer, Set<Node>>> bkwLabel;
	private Map<GraphObject, Map<Integer, Set<Node>>> labelMap;

	private Set<GraphObject> visitedGlobal;
	private Set<GraphObject> visitedStep;

	public PathsBetweenSIF(Set<Node> seed, boolean directed, int limit)
	{
		this.sourceSeed = seed;
		this.targetSeed = seed;
		this.directed = directed;
		this.limit = limit;
	}

	public PathsBetweenSIF(Set<Node> sourceSeed, Set<Node> targetSeed, boolean directed, int limit)
	{
		this.sourceSeed = sourceSeed;
		this.targetSeed = targetSeed;
		this.directed = directed;
		this.limit = limit;
	}

	public void setUseShortestPlusK(boolean useShortestPlusK)
	{
		this.useShortestPlusK = useShortestPlusK;
	}

	public void setIgnoreSelfLoops(boolean ignoreSelfLoops)
	{
		this.ignoreSelfLoops = ignoreSelfLoops;
	}

	public void setConsiderAllPairs(boolean considerAllPairs)
	{
		this.considerAllPairs = considerAllPairs;
	}

	public void setShortestAnyDir(boolean shortestAnyDir)
	{
		this.shortestAnyDir = shortestAnyDir;
	}

	public void setShortestPairLengths(Map<Node, Map<Node, Integer>> shortestPairLengths)
	{
		this.shortestPairLengths = shortestPairLengths;
	}

	public void setK(int k)
	{
		this.k = k;
	}

	public Set<GraphObject> run()
	{
		goi = new HashSet<GraphObject>();
		visitedGlobal = new HashSet<GraphObject>();
		visitedStep = new HashSet<GraphObject>();

		if (directed)
		{
			this.fwdLabel = new HashMap<GraphObject, Map<Integer, Set<Node>>>();
			this.bkwLabel = new HashMap<GraphObject, Map<Integer, Set<Node>>>();
		}
		else this.labelMap = new HashMap<GraphObject, Map<Integer, Set<Node>>>();

		for (Node node : sourceSeed)
		{
			initSeed(node);

			if (directed)
			{
				runBFS_directed(node, FORWARD);
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

		for (Node node : targetSeed)
		{
			if (!directed && sourceSeed.contains(node)) continue;

			initSeed(node);

			if (directed)
			{
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

		if (useShortestPlusK) findShortestPaths();

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
		visitedStep.add(seed);

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
		visitedStep.add(seed);

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
				if (visitedStep.contains(edge)) continue;

				setLabel(edge, label, !upstr && label.equals(DIST_FORWARD) ? d + 1 : d);

				Node n = upstr ? edge.getSourceNode() : edge.getTargetNode();

				int d_n = getLabel(n, label);

				if (d_n > d + 1)
				{
					if (d + 1 < limit && !visitedStep.contains(n) && !queue.contains(n)
						&& (!ignoreSelfLoops || !(sourceSeed.contains(n) || targetSeed.contains(n))))
						queue.add(n);

					setLabel(n, label, d + 1);
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
		for (GraphObject go : visitedGlobal)
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
			return this.distanceSatisfies(go, fwdLabel, bkwLabel);
		}
		else
		{
			return this.distanceSatisfies(go, labelMap, labelMap);

			// just to remember old

//			if (!labelMap.containsKey(go)) return false;
//
//			for (Integer i : labelMap.get(go).keySet())
//			{
//				if (i <= limit && labelMap.get(go).get(i).size() > 1) return true;
//			}
//			return false;
		}
	}

	private boolean distanceSatisfies(GraphObject go,
		Map<GraphObject, Map<Integer, Set<Node>>> fwdLabel,
		Map<GraphObject, Map<Integer, Set<Node>>> bkwLabel)
	{
		if (!fwdLabel.containsKey(go) || !bkwLabel.containsKey(go)) return false;

		for (Integer i : fwdLabel.get(go).keySet())
		{
			for (Integer j : bkwLabel.get(go).keySet())
			{
				int dist = i + j;

				if (!directed && go instanceof Edge) dist++;

				if (dist <= limit)
				{
					if (setsSatisfy(fwdLabel.get(go).get(i), bkwLabel.get(go).get(j), dist))
						return true;
				}
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
		if (goi.contains(node) && !(sourceSeed.contains(node) || targetSeed.contains(node)))
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
		for (GraphObject go : visitedStep)
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
		visitedStep.clear();
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
		visitedStep.add(go);
		visitedGlobal.add(go);
	}

	private void recordDistances(Node seed)
	{
		for (GraphObject go : visitedGlobal)
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

	private boolean setsSatisfy(Set<Node> set1, Set<Node> set2, int length)
	{
		assert !set1.isEmpty();
		assert !set2.isEmpty();

		if (useShortestPlusK)
		{
			for (Node source : set1)
			{
				for (Node target : set2)
				{
					if (ignoreSelfLoops && source.equals(target)) continue;
					if (!sourceSeed.contains(source) || !targetSeed.contains(target)) continue;

					if ((considerAllPairs && shortestPairLengths.containsKey(source) &&
						shortestPairLengths.get(source).containsKey(target)) ||
						(!considerAllPairs && shortestSingleLengths.containsKey(source) &&
						shortestSingleLengths.containsKey(target)))
					{
						// decide limit
						int limit;

						if (considerAllPairs)
						{
							limit = shortestPairLengths.get(source).get(target);
							if (shortestAnyDir && shortestPairLengths.containsKey(target) &&
								shortestPairLengths.get(target).containsKey(source))
							{
								limit = Math.min(limit, shortestPairLengths.get(target).get(source));
							}
						}
						else
						{
							limit = Math.max(shortestSingleLengths.get(source),
								shortestSingleLengths.get(target));
						}

						limit = Math.min(limit + k, this.limit);

						if (limit >= length) return true;
					}
				}
			}
			return false;
		}
		else
		{
			for (Node source : set1)
			{
				for (Node target : set2)
				{
					if (ignoreSelfLoops && source.equals(target)) continue;
					if (sourceSeed.contains(source) && targetSeed.contains(target)) return true;
				}
			}
			return false;
		}
	}

	private void findShortestPaths()
	{
		if (directed) this.findShortestPaths(fwdLabel, bkwLabel);
		else this.findShortestPaths(labelMap, labelMap);
	}

	private void findShortestPaths(Map<GraphObject, Map<Integer, Set<Node>>> fwdLabel,
		Map<GraphObject, Map<Integer, Set<Node>>> bkwLabel)
	{
		if (considerAllPairs) shortestPairLengths = new HashMap<Node, Map<Node, Integer>>();
		else shortestSingleLengths = new HashMap<Node, Integer>();

		for (GraphObject go : fwdLabel.keySet())
		{
			if (go instanceof Edge) continue;

			Map<Integer, Set<Node>> fwMap = fwdLabel.get(go);
			Map<Integer, Set<Node>> bwMap = bkwLabel.get(go);

			if (fwMap == null || bwMap == null) continue;

			for (Integer d1 : fwMap.keySet())
			{
				for (Node source : fwMap.get(d1))
				{
					for (Integer d2 : bwMap.keySet())
					{
						if (d1 + d2 > limit) continue;

						for (Node target : bwMap.get(d2))
						{
							if (ignoreSelfLoops && source.equals(target)) continue;

							if (considerAllPairs)
							{
								if (!shortestPairLengths.containsKey(source))
								{
									shortestPairLengths.put(source, new HashMap<Node, Integer>());
								}

								if (!shortestPairLengths.get(source).containsKey(target) ||
									shortestPairLengths.get(source).get(target) > d1 + d2)
								{
									shortestPairLengths.get(source).put(target, d1 + d2);
								}
							}
							else
							{
								if (!shortestSingleLengths.containsKey(source) ||
									shortestSingleLengths.get(source) > d1 + d2)
								{
									shortestSingleLengths.put(source, d1 + d2);
								}
								if (!shortestSingleLengths.containsKey(target) ||
									shortestSingleLengths.get(target) > d1 + d2)
								{
									shortestSingleLengths.put(target, d1 + d2);
								}
							}
						}
					}
				}
			}
		}
	}

	public static final String DIST = "DIST";
	public static final String DIST_FORWARD = "DIST_FORWARD";
	public static final String DIST_BACKWARD = "DIST_BACKWARD";

	public static final boolean FORWARD = true;
	public static final boolean BACKWARD = false;
	public static final boolean UPSTREAM = true;
	public static final boolean DOWNSTREAM = false;
}
