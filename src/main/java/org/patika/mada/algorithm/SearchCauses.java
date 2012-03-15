package org.patika.mada.algorithm;

import org.gvt.model.biopaxl2.Complex;
import org.gvt.model.biopaxl2.ComplexMember;
import org.patika.mada.graph.Edge;
import org.patika.mada.graph.Graph;
import org.patika.mada.graph.Node;
import org.patika.mada.util.CausativePath;
import org.patika.mada.util.ExperimentData;

import java.util.*;

@SuppressWarnings({"JavaDoc"})

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class SearchCauses
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
	private Set<? extends Node> targets;
	
	/**
	 * plus t argument for getting compatible paths of length shortest plus t (from any source to 
	 * the specific target).
	 */
	private int t;
	
	/**
	 * plus k argument for forcing compatible paths to be among of length shortest + k (from a 
	 * specific source to a specific target).
	 */
	private int k;
	
	/**
	 * The maximum length of a compatible path
	 */
	private int limit;
	
	/**
	 * This complex structure holds the resulting paths we found.
	 */
	private Map<Node, Map<Integer, List<CausativePath>>> result;

	//==============================================================================================
	// Section: Constructors
	//==============================================================================================
	
	public SearchCauses(Graph graph, Set<? extends Node> targets, int limit, int t, int k)
	{
		this.graph = graph;
		this.targets = targets;
		this.limit = limit;
		this.t = t;
		this.k = k;
	}

	//==============================================================================================
	// Section: Methods
	//==============================================================================================
	
	/**
	 * Runs the algorithm for necessary targets.
	 */
	public Map<Node, Map<Integer, List<CausativePath>>> run()
	{
		result = new HashMap<Node, Map<Integer, List<CausativePath>>>();

		for (Node target : targets != null ? targets : graph.getNodes())
		{
			if (target.hasSignificantExperimentalChange(ExperimentData.EXPRESSION_DATA))
			{
				searchForTarget(target);
			}
		}

		pruneResult();
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

		target.putLabel(MAX_PATH_LENGTH, limit);
		
		Set<Node> destSet = getDestinationSet(target, target);
		
		if (destSet != null && !destSet.isEmpty())
		{
			traverseUpstream(target, target, path, 1, destSet, new HashSet<Node>());
//			traverseRelatives(target, target, path, 1, destSet, new HashSet<Node>(), TOWARDS_CHILDREN);
//			traverseRelatives(target, target, path, 1, destSet, new HashSet<Node>(), TOWARDS_PARENTS);
		}
	}

	/**
	 * Proceeds to the upstream of the current node.
	 * @param node current node
	 * @param target node that we start from
	 * @param path nodes on current path
	 * @param sign the sign of the current path
	 */
	private void traverseUpstream(Node node, Node target, CurrentPath<Node> path, int sign,
			Set<Node> destSet, Set<Node> forbidden)
	{
		// Stop if the maximum length of compatible path for this target is hit.

		int maxLength = (Integer) target.getLabel(MAX_PATH_LENGTH);
		
		assert path.getSize() <= maxLength :
			"Length limit is violated. path size: " + path.getSize() + " maxlength: " + maxLength;
		
		if (path.getSize() == maxLength)
		{
			return;
		}

		// Iterate upstream
		
		for (Edge edge : node.getUpstream())
		{
			// Do not consider non-causative edges
			if (!edge.isCausative()) continue;

			int edgeSign = edge.getSign();
			Node neigh = edge.getSourceNode();

			// Do it if will not cause a cycle
			
			if (neigh != target && !neigh.hasLabel(ON_PATH) && !forbidden.contains(neigh))
			{
				// If this is the last reaction, then it should be a transcriptional relation
				if (false)//node == target && neigh.isEvent())
				{
					if (!neigh.isTranscriptionEvent()) continue;
				}
				
				Set<Node> newDestSet = filterDestinationSources(neigh, target, destSet);

				// Proceed only if there is some source to go.
				if (!newDestSet.isEmpty() || destSet.contains(neigh))
				{
					boolean distIncr = neigh.isBreadthNode() && edge.isBreadthEdge();

					Set<Node> tabu = keepDifference(forbidden, neigh.getTabuNodes());
					forbidden.addAll(tabu);
					neigh.putLabel(EDGE, edge);
					neigh.putLabel(ON_PATH, true);
					path.addFirst(neigh, distIncr);
					
					checkAndProceed(neigh, target, path, sign * edgeSign, newDestSet, forbidden,
						TOWARDS_BOTHWAYS);
					
					path.removeFirst(distIncr);
					neigh.removeLabel(ON_PATH);
					neigh.removeLabel(EDGE);
					forbidden.removeAll(tabu);
				}
			}
		}
	}

	/**
	 * Proceeds towards children or parents according to the direction parameter.
	 */
	private void traverseRelatives(Node node, Node target, CurrentPath<Node> path, int sign,
			Set<Node> destSet, Set<Node> forbidden, int direction)
	{
		for (Node relative : direction == TOWARDS_CHILDREN ? node.getChildren() : node.getParents())
		{
			if (relative != target && !relative.hasLabel(ON_PATH))
			{
				Set<Node> newDestSet = filterDestinationSources(relative, target, destSet);

				// Proceed only if there is some source to go.
				if (!newDestSet.isEmpty())
				{
					relative.putLabel(ON_PATH, true);
					path.addFirst(relative, false);
					
					checkAndProceed(relative, target, path, sign, newDestSet, forbidden, direction);
					
					path.removeFirst(false);
					relative.removeLabel(ON_PATH);
				}
			}
		}
	}
	
	/**
	 * Checks the compatibility, records if a compatible path is found, otherwise proceeds to 
	 * upstream.
	 * @param node current node
	 * @param target node that we start from
	 * @param path nodes on current path
	 * @param sign the sign of the current path
	 */
	private void checkAndProceed(Node node, Node target, CurrentPath<Node> path, int sign,
			Set<Node> destSet, Set<Node> forbidden, int relativeDirection)
	{
		// Check compatibility
		
		if (node.hasSignificantExperimentalChange(ExperimentData.EXPRESSION_DATA))
		{
			if (isCompatible(node, target, sign))
			{
				recordPath(node, target, path);
			}

			return;
		}
		
		// A compatible path won't have a significant node in the middle. That's why we have "else"
		// here
		
		else
		{
			traverseUpstream(node, target, path, sign, destSet, forbidden);
			
			if (relativeDirection == TOWARDS_CHILDREN || relativeDirection == TOWARDS_BOTHWAYS)
			{
				traverseRelatives(node, target, path, sign, destSet, forbidden, TOWARDS_CHILDREN);
			}
			if (relativeDirection == TOWARDS_PARENTS || relativeDirection == TOWARDS_BOTHWAYS)
			{
				traverseRelatives(node, target, path, sign, destSet, forbidden, TOWARDS_PARENTS);
			}
		}
	}
	
	/**
	 * Checks if regulation on two nodes is compatible with the sign, i.e. if "from" is up and "to"
	 * is down, then sign=-1 is compatible.
	 */
	private boolean isCompatible(Node from, Node to, int sign)
	{
		boolean identityProblemExists = from.sameEntity(to);

		if (!identityProblemExists)
		{
			if (from instanceof ComplexMember)
			{
				identityProblemExists = sameEntityWithAMember(
					(Complex) from.getParents().iterator().next(), to);
			}

			if (!identityProblemExists && to instanceof ComplexMember)
			{
					identityProblemExists = sameEntityWithAMember(
						(Complex) to.getParents().iterator().next(), from);
			}
		}
		return !identityProblemExists &&
			from.getExperimentDataSign(ExperimentData.EXPRESSION_DATA) *
			to.getExperimentDataSign(ExperimentData.EXPRESSION_DATA) == sign;
	}

	/**
	 * Checks if the node has the same physical entity with one of the members of the complex.
	 * @param c complex
	 * @param node node to check
	 * @return true if one member has same entity with the parameter node
	 */
	private boolean sameEntityWithAMember(Complex c, Node node)
	{
		for (Object o : c.getChildren())
		{
			Node child = (Node) o;

			if (child.sameEntity(node))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Records the compatible path found and updates the max length of the compatible path for the 
	 * target if necessary.
	 */
	private void recordPath(Node source, Node target, CurrentPath<Node> path)
	{
		int length = path.getSize();

		if (length > getShortestDistance(source, target) + k)
		{
			System.out.println("Found and ignored a non-(shortest+k) " +
				"path this should happen rarely.");
			
			return;
		}
		
		int max = (Integer) target.getLabel(MAX_PATH_LENGTH);

		assert length + t <= max : "Found a path longer than restricted lengh.";
		
		if (length + t < max)
		{		
			target.putLabel(MAX_PATH_LENGTH, length + t);
		}

		// Create the path and add to the result
		
		CausativePath compPath = new CausativePath();
		
		for (Node node : path)
		{
			compPath.addNode(node);

			if (node.hasLabel(EDGE))
			{
				compPath.addEdge((Edge) node.getLabel(EDGE));
			}
		}
		compPath.addNode(target);
		
		assert compPath.getLength() == length;

		if (!result.containsKey(target))
		{
			result.put(target, new HashMap<Integer, List<CausativePath>>());
		}
		Map<Integer, List<CausativePath>> pathMap = result.get(target);
		
		if (!pathMap.containsKey(length))
		{
			pathMap.put(length, new ArrayList<CausativePath>());
		}
		List<CausativePath> paths = pathMap.get(length);
		paths.add(compPath);
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
	 * Each node knows where to go if you are coming from a target. this destination set is 
	 * retrieved from a node using this method.
	 */
	private Set<Node> getDestinationSet(Node node, Node target)
	{
		// invalid assertion. remove when certain
//		assert node.hasLabel(MarkShortestPlusPaths.TO_FROM_PATH_MAP);
		
		Map<Node, Set<Node>> destMap = (Map<Node, Set<Node>>) node.getLabel(
			MarkShortestPlusPaths.TO_FROM_PATH_MAP);

		if (destMap == null)
		{
			return null;
		}
		else
		{
			return destMap.get(target);
		}
	}

	/**
	 * Scenario is: We started backwards search from "target" and now visiting "node". We also have 
	 * a list of destination sources that we are supposed to be searching. This list is inherited 
	 * from the previous step. Now we need to remove items from this list if "node" does not support 
	 * them. When we have no items in this list, this will the time to stop.
	 * 
	 * @param node current node
	 * @param target where we start from
	 * @param destSet destination set while coming from to the current node
	 * @return new filtered destination to traverse upstream of node
	 */
	private Set<Node> filterDestinationSources(Node node, Node target, Set<Node> destSet)
	{
		Set<Node> nodeDest = getDestinationSet(node, target);
		Set<Node> newSet = new HashSet<Node>();

		if (nodeDest != null)
		{
			for (Node n : destSet)
			{
				if (nodeDest.contains(n))
				{
					newSet.add(n);
				}
			}
		}
		return newSet;
	}

	/**
	 * Ensures that the set "diff" contains only elements which is not already in the first set.
	 * This method removed common elements from diff.
	 * @param set
	 * @param diff
	 */
	private Set<Node> keepDifference(Set<Node> set, Set<Node> diff)
	{
		if (!diff.isEmpty())
		{
			Iterator<Node> iter = diff.iterator();

			while (iter.hasNext())
			{
				Node node = iter.next();

				if (set.contains(node))
				{
					iter.remove();
				}
			}
		}
		return diff;
	}

	/**
	 * There may be some longer paths in the result other than shortest+t. This leaves them out.
	 */
	private void pruneResult()
	{
		for (Node target : result.keySet())
		{
			Map<Integer, List<CausativePath>> pathMap = result.get(target);

			for (int i = (Integer) target.getLabel(MAX_PATH_LENGTH) + 1 ; i <= limit; i++ )
			{
				pathMap.remove(i);
			}
		}
	}
	
	/**
	 * Cleanup.
	 */
	private void clearLabels()
	{
		graph.removeLabels(Arrays.asList(EDGE, ON_PATH, MAX_PATH_LENGTH,
			MarkDistances.DIST_FROM, MarkDistances.DIST_TO,
			MarkShortestPlusPaths.TO_FROM_PATH_MAP));
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

	/**
	 * This is max length of allowed causative path
	 */
	private static final String MAX_PATH_LENGTH = "MAX_PATH_LENGTH";

	// Direction constants

	private static final int TOWARDS_CHILDREN = 0;
	private static final int TOWARDS_PARENTS = 1;
	private static final int TOWARDS_BOTHWAYS = 2;
}
