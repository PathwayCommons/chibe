package org.gvt.model;

import java.util.*;

/**
 * This class implements an iterator for iterating over edges of a compound
 * graph structure rooted at the provided root model for the given edge type
 * (all edges, intra-graph edges only or inter-graph edges only). Make sure
 * that you use it in a regular style hasNext() preceeding each next() call!
 *
 * @author Ugur Dogrusoz
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class NodeIterator implements Iterator<NodeModel>
{
	// edge type: all, intra-graph or inter-graph
	private int ledgeType;

	// collection of all edges for given gvt compound model
	private HashSet<NodeModel> lnodes;

	// iterator over the constructed edge set
	private Iterator<NodeModel> literator;

	// next edge during iteration (element to be returned by next())
	private NodeModel lnext;

	// boolean value for recursively iteration
	private boolean lisRecursive;

	/**
	 * Constructor
	 *
	 * @param root root of gvt compound model whose edges are to be iterated
	 *
	 */
	public NodeIterator(CompoundModel root, boolean isRecursive)
	{
		this.lisRecursive = isRecursive;
		this.lnodes = new HashSet<NodeModel>();
		this.constructNodes(root);
		this.literator = this.lnodes.iterator();
	}

	/*
	 * This method puts all edges under the given root model in the edge set
	 * of this iterator object.
	 */
	private void constructNodes(CompoundModel root)
	{
		Iterator iter = root.children.iterator();
		NodeModel node;

		while (iter.hasNext())
		{
			node = (NodeModel) iter.next();

			this.lnodes.add(node);

			if (node instanceof CompoundModel && lisRecursive)
			{
				this.constructNodes((CompoundModel) node);
			}
		}
	}

	/**
	 * This method checks whether there are any more edges of the specified
	 * type to be iterated. In order to do that, it has to iterate over edges
	 * until an edge of desired type is reached. This node is kept in an
	 * instance variable for use by next().
	 */
	public boolean hasNext()
	{
		this.findNext();

		return (this.lnext != null);
	}


	/**
	 * This method returns the next node in the node list of this iterator.
	 */
	public NodeModel next()
	{
		return this.lnext;
	}

	/*
	 * This method skips over all edges that do not belong to the specified
	 * type during construction of this iterator, and stores the next element
	 * in the associated attribute.
	 */
	private void findNext()
	{
		this.lnext = null;

		while (this.literator.hasNext())
		{
			this.lnext = this.literator.next();
		}
	}

	/**
	 * This method is not used by this iterator.
	 */
	public void remove()
	{
	}

	public Set<NodeModel> getNodes()
	{
		return lnodes;
	}
}