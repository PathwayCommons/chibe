package org.patika.mada.algorithm;

import org.patika.mada.graph.Edge;
import org.patika.mada.graph.Graph;
import org.patika.mada.graph.Node;
import org.patika.mada.util.Path;

import java.util.*;

@SuppressWarnings({"JavaDoc"})

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class SearchPathsBetween
{
	//==============================================================================================
	// Section: Instance variables
	//==============================================================================================

	/**
	 * Graph to work on
	 */
	private Graph graph;

	/**
	 * Target node to search for causes of its experiment data
	 */
	private Collection<Node> interest;

	/**
	 * The maximum length of a compatible path
	 */
	private int limit;

	/**
	 * This complex structure holds the resulting paths we found.
	 */
	private Map<Node, Map<Integer, List<Path>>> result;

	//==============================================================================================
	// Section: Constructors
	//==============================================================================================

	public SearchPathsBetween(Graph graph, Collection<Node> interest, int limit)
	{
		this.graph = graph;
		this.interest = interest;
		this.limit = limit;
	}

	//==============================================================================================
	// Section: Methods
	//==============================================================================================

	/**
	 * Runs the algorithm for necessary targets.
	 */
	public Map<Node, Map<Integer, List<Path>>> run()
	{
		result = new HashMap<Node, Map<Integer, List<Path>>>();

		for (Node target : interest)
		{
			searchForTarget(target);
		}

		clearLabels();
		return result;
	}

	/**
	 * This method can be called for each target independently.
	 */
	private void searchForTarget(Node target)
	{
		// Current path
		CurrentPath<Node> path = new CurrentPath<Node>();

		traverseUpstream(target, target, path);
		traverseRelatives(target, target, path, TOWARDS_CHILDREN);
		traverseRelatives(target, target, path, TOWARDS_PARENTS);
	}

	/**
	 * Proceeds to the upstream of the current node.
	 * @param node current node
	 * @param target node that we start from
	 * @param path nodes on current path
	 */
	private void traverseUpstream(Node node, Node target, CurrentPath<Node> path)
	{
		// Stop if the maximum length of compatible path for this target is hit.

		assert path.getSize() <= limit :
			"Length limit is violated. path size: " + path.getSize() + " limit: " + limit;

		if (path.getSize() == limit)
		{
			return;
		}

		// Iterate upstream

		for (Edge edge : node.getUpstream())
		{
			// Do not consider non-causative edges
			if (!edge.isDirected()) continue;

			Node neigh = edge.getSourceNode();

			// Do it if will not cause a cycle

			if (neigh != target && !neigh.hasLabel(ON_PATH))
			{
				neigh.putLabel(EDGE, edge);
				neigh.putLabel(ON_PATH, true);
				path.addFirst(neigh, neigh.isBreadthNode());

				checkAndProceed(neigh, target, path, TOWARDS_BOTHWAYS);

				path.removeFirst(neigh.isBreadthNode());
				neigh.removeLabel(ON_PATH);
				neigh.removeLabel(EDGE);
			}
		}
	}

	/**
	 * Proceeds towards children or parents according to the direction parameter.
	 */
	private void traverseRelatives(Node node, Node target, CurrentPath<Node> path,  int direction)
	{
		for (Node relative : direction == TOWARDS_CHILDREN ? node.getChildren() : node.getParents())
		{
			if (relative != target && !relative.hasLabel(ON_PATH))
			{
				relative.putLabel(ON_PATH, true);
				path.addFirst(relative, false);

				checkAndProceed(relative, target, path, direction);

				path.removeFirst(false);
				relative.removeLabel(ON_PATH);
			}
		}
	}

	/**
	 * Checks the compatibility, records if a compatible path is found, otherwise proceeds to
	 * upstream.
	 * @param node current node
	 * @param target node that we start from
	 * @param path nodes on current path
	 */
	private void checkAndProceed(Node node, Node target, CurrentPath<Node> path,
		int relativeDirection)
	{
		if (interest.contains(node))
		{
			recordPath(target, path);
		}


		// A compatible path won't have a significant node in the middle. That's why we have "else"
		// here

		else
		{
			traverseUpstream(node, target, path);

			if (relativeDirection == TOWARDS_CHILDREN || relativeDirection == TOWARDS_BOTHWAYS)
			{
				traverseRelatives(node, target, path, TOWARDS_CHILDREN);
			}
			if (relativeDirection == TOWARDS_PARENTS || relativeDirection == TOWARDS_BOTHWAYS)
			{
				traverseRelatives(node, target, path, TOWARDS_PARENTS);
			}
		}
	}

	/**
	 * Records the compatible path found and updates the max length of the compatible path for the
	 * target if necessary.
	 */
	private void recordPath(Node target, CurrentPath<Node> currPath)
	{
		int length = currPath.getSize();

		assert length <= limit : "Found a path longer than limit.";

		// Create the path and add to the result

		Path path = new Path();

		for (Node node : currPath)
		{
			path.addNode(node);

			if (node.hasLabel(EDGE))
			{
				path.addEdge((Edge) node.getLabel(EDGE));
			}
		}
		path.addNode(target);

		if (!result.containsKey(target))
		{
			result.put(target, new HashMap<Integer, List<Path>>());
		}
		Map<Integer, List<Path>> pathMap = result.get(target);

		if (!pathMap.containsKey(length))
		{
			pathMap.put(length, new ArrayList<Path>());
		}
		List<Path> paths = pathMap.get(length);
		paths.add(path);
	}

	/**
	 * Cleanup.
	 */
	private void clearLabels()
	{
		graph.removeLabels(Arrays.asList(EDGE, ON_PATH));
	}

	private class CurrentPath<E> extends LinkedList<E>
	{
		private int size;

		private CurrentPath()
		{
			size = 0;
		}

		public boolean add(E e, boolean increaseSize)
		{
			if (increaseSize)
			{
				size++;
			}

			return super.add(e);
		}

		public boolean remove(E e, boolean decreaseSize)
		{
			if (decreaseSize)
			{
				size--;
			}

			return super.remove(e);
		}

		public void addFirst(E e, boolean increaseSize)
		{
			if (increaseSize)
			{
				size++;
			}

			super.addFirst(e);
		}

		public E removeFirst(boolean decreaseSize)
		{
			if (decreaseSize)
			{
				size--;
			}

			return super.removeFirst();
		}

		public int getSize()
		{
			return size;
		}
	}

	//==============================================================================================
	// Section: Class constants
	//==============================================================================================

	/**
	 * Used for remembering the edge that was traversed during search of the current path.
	 */
	private static final String EDGE = "EDGE";

	/**
	 * Nodes on the current path are marked with this.
	 */
	private static final String ON_PATH = "ON_PATH";

	// Direction constants

	private static final int TOWARDS_CHILDREN = 0;
	private static final int TOWARDS_PARENTS = 1;
	private static final int TOWARDS_BOTHWAYS = 2;
}