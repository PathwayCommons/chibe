package org.patika.mada.algorithm;

import org.patika.mada.graph.Graph;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;
import org.patika.mada.util.CausativePath;
import org.patika.mada.util.Path;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class AlgoRunner
{
	public static Map<Node, Map<Integer, List<CausativePath>>> searchCausativePaths(
		Graph graph, Set<? extends Node> targets, int limit, int t, int k)
	{
		MarkDistances md = new MarkDistances(graph, limit);
		md.run();
		MarkShortestPlusPaths mspp = new MarkShortestPlusPaths(graph, limit, k);
		mspp.run();
		SearchCauses sc = new SearchCauses(graph, targets, limit, t, k);
		return sc.run();
	}

	/**
	 * Beware of the exhaustive search!!
	 * @param graph
	 * @param target
	 * @param limit
	 * @return map from targets to paths
	 */
	public static Map<Node, Map<Integer, List<Path>>> searchPathsBetween(
		Graph graph, Collection<Node> target, int limit)
	{
		SearchPathsBetween spb = new SearchPathsBetween(graph, target, limit);
		return spb.run();
	}

	/**
	 * Gets a merge graph of all paths between seed nodes shorter than a limit.
	 * @param graph
	 * @param seed
	 * @param limit
	 * @param directed
	 * @return
	 */
	public static Collection<GraphObject> searchGraphOfInterest(Graph graph, Set<Node> seed,
		int limit, boolean directed)
	{
		GraphOfInterets goi = new GraphOfInterets(seed, directed, graph, limit);
		return goi.run();
	}

	public static Collection<GraphObject> searchNeighborhood(Set<Node> seed,
		int limit, boolean upstream, boolean downstream)
	{
		LocalNeighborhoodQuery nq = new LocalNeighborhoodQuery(seed, upstream, downstream, limit);
		return nq.run();
	}

	public static Collection<GraphObject> searchPathsFromTo(Set<Node> source, Set<Node> target,
		int limit)
	{
		LocalPoIQuery poi = new LocalPoIQuery(source, target, LocalPoIQuery.NORMAL_LIMIT, limit,
			true);
		return poi.run();
	}

	public static Collection<GraphObject> searchCommonStream(Set<Node> source, boolean downstream,
		int limit)
	{
		LocalCommonStreamQuery csq = new LocalCommonStreamQuery(source, downstream, limit);
		Set<Node> common = csq.run();
		return searchPathsFromTo(source, common, limit);
	}
}
