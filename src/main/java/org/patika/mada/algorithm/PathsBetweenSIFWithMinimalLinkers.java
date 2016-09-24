package org.patika.mada.algorithm;

import org.patika.mada.graph.Edge;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;

import java.util.*;

/**
 * Does not handle compound nodes. For binary netowrks only. This algorithm treats the network as if
 * undirected. First runs a regular paths-between to generate connected components from given source set. Then runs a
 * paths-between-with-linkers, but treating each connected component as a single agent, i.e., finds common neighborhood
 * of the connected components (a linker is selected if it is at the neighborhood of at least 2 different connected
 * components).
 *
 * @author Ozgun Babur
 */
public class PathsBetweenSIFWithMinimalLinkers
{
	private Set<Node> sourceSeed;

	Set<GraphObject> goi;

	public PathsBetweenSIFWithMinimalLinkers(Set<Node> seed)
	{
		this.sourceSeed = seed;
	}

	public Set<GraphObject> run()
	{
		// Detect connected components

		PathsBetweenSIF pb = new PathsBetweenSIF(sourceSeed, false, 1);
		Set<GraphObject> pbResult = pb.run();

		Set<Set<Node>> components = generateConnectedComponents(pbResult);
		for (Node node : getDetached(sourceSeed, components))
		{
			components.add(Collections.singleton(node));
		}

		Map<Node, Set<Node>> nodeToComp = mapFromComponentSet(components);

		// Detect nodes that have at least 2 connected components in their neighborhood

		Set<GraphObject> goi = new HashSet<GraphObject>(sourceSeed);

		Set<GraphObject> consider = new HashSet<GraphObject>();
		for (Set<Node> component : components)
		{
			consider.addAll(getNeighbors(component));
		}
		consider.addAll(sourceSeed);

		for (GraphObject go : consider)
		{
			if (go instanceof Node)
			{
				Set<GraphObject> neigh = getNeighbors((Node) go);

				if (findNumberOfConnectedComponents(neigh, nodeToComp) > 1)
				{
					goi.add(go);
				}
			}
		}

		this.goi = new HashSet<GraphObject>();

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

	private Map<Node, Set<Node>> mapFromComponentSet(Set<Set<Node>> components)
	{
		Map<Node, Set<Node>> nodeToComp = new HashMap<Node, Set<Node>>();
		for (Set<Node> component : components)
		{
			for (Node node : component)
			{
				if (nodeToComp.containsKey(node))
					throw new RuntimeException("Components have to be disjoint. This is violated here.");

				nodeToComp.put(node, component);
			}
		}
		return nodeToComp;
	}


	public int findNumberOfConnectedComponents(Set<GraphObject> objects, Map<Node, Set<Node>> nodeToComponent)
	{
		Set<Set<Node>> found = new HashSet<Set<Node>>();
		for (GraphObject o : objects)
		{
			if (nodeToComponent.containsKey(o))
			{
				found.add(nodeToComponent.get(o));
			}
		}
		return found.size();
	}

	public Set<GraphObject> getNeighbors(Set<Node> nodes)
	{
		Set<GraphObject> neigh = new HashSet<GraphObject>();
		for (Node node : nodes)
		{
			neigh.addAll(getNeighbors(node));
		}
		return neigh;
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

	private Set<Set<Node>> generateConnectedComponents(Set<GraphObject> objects)
	{
		Set<Set<Node>> components = new HashSet<Set<Node>>();

		for (GraphObject o : objects)
		{
			if (o instanceof Edge)
			{
				Edge e = (Edge) o;
				Set<Node> set1 = getRelatedSet(e.getSourceNode(), components);
				Set<Node> set2 = getRelatedSet(e.getTargetNode(), components);

				if (set1 == null)
				{
					if (set2 == null)
					{
						Set<Node> set = new MySet<Node>(new HashSet<Node>());
						set.add(e.getSourceNode());
						set.add(e.getTargetNode());
						components.add(set);

						for (Set<Node> mem : components)
						{
							assert components.contains(mem);
						}
					}
					else
					{
						set2.add(e.getSourceNode());

						for (Set<Node> mem : components)
						{
							assert components.contains(mem);
						}
					}
				}
				else if (set2 == null)
				{
					set1.add(e.getTargetNode());

					for (Set<Node> mem : components)
					{
						assert components.contains(mem);
					}
				}
				else if (set1 != set2)
				{
					set1.addAll(set2);
					components.remove(set2);

					for (Set<Node> mem : components)
					{
						assert components.contains(mem);
					}
				}

//				mapFromComponentSet(components);
			}
		}
		return components;
	}

	private Set<Node> getRelatedSet(Node node, Set<Set<Node>> sets)
	{
		Set<Node> result = null;
		for (Set<Node> set : sets)
		{
			if (set.contains(node))
			{
				if (result != null) throw new RuntimeException("Multiple sets contains same node. Should not happen.");

				result = set;
			}
		}
		return result;
	}

	private Set<Node> getDetached(Set<Node> sources, Set<Set<Node>> components)
	{
		Set<Node> detached = new HashSet<Node>(sources);
		Set<Node> connected = new HashSet<Node>();

		for (Set<Node> component : components)
		{
			connected.addAll(component);
		}

		detached.removeAll(connected);
		return detached;
	}

	class MySet<E> implements Set<E>
	{
		Set<E> set;

		public MySet(Set<E> set)
		{
			this.set = set;
		}

		@Override
		public int size()
		{
			return set.size();
		}

		@Override
		public boolean isEmpty()
		{
			return set.isEmpty();
		}

		@Override
		public boolean contains(Object o)
		{
			return set.contains(o);
		}

		@Override
		public Iterator<E> iterator()
		{
			return set.iterator();
		}

		@Override
		public Object[] toArray()
		{
			return set.toArray();
		}

		@Override
		public <T> T[] toArray(T[] a)
		{
			return set.toArray(a);
		}

		@Override
		public boolean add(E e)
		{
			return set.add(e);
		}

		@Override
		public boolean remove(Object o)
		{
			return set.remove(o);
		}

		@Override
		public boolean containsAll(Collection<?> c)
		{
			return set.containsAll(c);
		}

		@Override
		public boolean addAll(Collection<? extends E> c)
		{
			return set.addAll(c);
		}

		@Override
		public boolean retainAll(Collection<?> c)
		{
			return set.retainAll(c);
		}

		@Override
		public boolean removeAll(Collection<?> c)
		{
			return set.removeAll(c);
		}

		@Override
		public void clear()
		{
			set.clear();
		}
	}
}
