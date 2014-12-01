package org.patika.mada.algorithm;

import org.patika.mada.graph.Edge;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;

import java.util.*;

/**
 * Does not handle compound nodes. For binary netowrks only. This algorithm treats the network as if
 * undirected. First finds all linker nodes: whose neighborhood contain at least 2 query genes. Then
 * links the query genes to these linkers.
 *
 * @author Ozgun Babur
 */
public class PathsBetweenSIFWithLinkers
{
	private Set<Node> sourceSeed;

	Set<GraphObject> goi;

	public PathsBetweenSIFWithLinkers(Set<Node> seed)
	{
		this.sourceSeed = seed;
	}

	public Set<GraphObject> run()
	{
		this.goi = new HashSet<GraphObject>();
		Set<GraphObject> goi = new HashSet<GraphObject>(sourceSeed);

		Set<GraphObject> consider = new HashSet<GraphObject>();
		for (Node node : sourceSeed)
		{
			consider.addAll(getNeighbors(node));
		}
		consider.addAll(sourceSeed);

		for (GraphObject go : consider)
		{
			if (go instanceof Node)
			{
				Node node = (Node) go;

				Set<GraphObject> neigh = getNeighbors(node);

				neigh.retainAll(sourceSeed);

				if (neigh.size() > 1) goi.add(go);
			}
		}

		for (GraphObject go : consider)
		{
			if (go instanceof Edge)
			{
				Edge edge = (Edge) go;
				if (goi.contains(edge.getSourceNode()) && goi.contains(edge.getTargetNode()))
				{
					this.goi.add(edge);
					this.goi.add(edge.getSourceNode());
					this.goi.add(edge.getTargetNode());
				}
			}
		}

		return this.goi;
	}

	public Set<GraphObject> getNeighbors(Node node)
	{
		Set<GraphObject> n = new HashSet<GraphObject>();
		for (Edge edge : node.getUpstream())
		{
			n.add(edge);
			n.add(edge.getSourceNode());
		}
		for (Edge edge : node.getDownstream())
		{
			n.add(edge);
			n.add(edge.getTargetNode());
		}
		return n;
	}
}
