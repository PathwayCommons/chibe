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
public class EdgeIterator implements Iterator
{
	// edge type: all, intra-graph or inter-graph
	private int ledgeType;
	
	// collection of all edges for given gvt compound model
	private HashSet ledges;
	
	// iterator over the constructed edge set
	private Iterator literator;
	
	// next edge during iteration (element to be returned by next())
	private EdgeModel lnext;

	/**
	 * boolean value for recursively iteration within the given root
	 * by visiting its subgraphs
	 */ 
	private boolean lisRecursive;
	
	/**
	 * boolean value for checking the current edge's source and 
	 * target nodes are within the given root or not. Both owners'
	 * of source and target node must be subgraph or itself of
	 * given root graph
	 */ 
	private boolean lonlyEndsWithinRoot;
	
	/**
	 *  starting root for the edge iteration
	 */
	private CompoundModel startingRoot;
	
	/**
	 * Constructor
	 * 
	 * @param root Compound model whose edges are to be iterated
	 * @param edgeType Type of edges to be iterated
	 * @param isRecursive Recursively iterate subgraphs
	 * @param onlyEndsWithinRoot Source and target node of an 
	 * 	edge must be within given root
	 */
	public EdgeIterator(CompoundModel root, 
		int edgeType, 
		boolean isRecursive, 
		boolean onlyEndsWithinRoot)
	{
		this.startingRoot = root;
		this.ledgeType = edgeType;
		this.lisRecursive = isRecursive;
		this.lonlyEndsWithinRoot = onlyEndsWithinRoot;
		this.ledges = new HashSet();
		this.constructEdges(root);
		this.literator = this.ledges.iterator();
	}
	
	/**
	 * This method puts all edges under the given root model in the edge set
	 * of this iterator object.
	 */
	private void constructEdges(CompoundModel root)
	{
		Iterator iter = root.children.iterator();
		NodeModel node;
		
		while (iter.hasNext())
		{
			node = (NodeModel) iter.next();
			
			this.ledges.addAll(node.sourceConnections);
			this.ledges.addAll(node.targetConnections);
			
			if (node instanceof CompoundModel && lisRecursive)
			{
				this.constructEdges((CompoundModel) node);
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
	 * This method returns the next edge in the edge list of this iterator.
	 */
	public Object next()
	{
		return this.lnext;
	}
	
	/**
	 * This method skips over all edges that do not belong to the specified
	 * type during construction of this iterator, and stores the next element
	 * in the associated attribute.
	 */
	private void findNext()
	{
		EdgeModel edge;
		boolean isIntragraph;
		this.lnext = null;
		
		while (this.literator.hasNext())
		{
			edge = (EdgeModel) this.literator.next();
			isIntragraph = edge.isIntragraph();
			
			if (this.ledgeType == CompoundModel.ALL_EDGES)
			{
				if (lonlyEndsWithinRoot)
				{
					if (startingRoot.isAncestorofNode(edge.getSource())
						&& startingRoot.isAncestorofNode(edge.getTarget()))
					{
						this.lnext = edge;
						break;
					}
				}
				else
				{
					this.lnext = edge;
					break;
				}
			}
			else if (this.ledgeType == CompoundModel.INTRA_GRAPH_EDGES)
			{
				if (isIntragraph)
				{
					this.lnext = edge;
					break;
				}
			}
			// intergraph edges only
			else
			{
				if (!isIntragraph)
				{
					if( lonlyEndsWithinRoot)
					{
						if( startingRoot.isAncestorofNode(edge.getSource())
							&& startingRoot.isAncestorofNode(edge.getTarget()))
						{
							this.lnext = edge;
							break;
						}
					}
					else
					{
						this.lnext = edge;
						break;
					}
				}
			}
		}
	}
	
	/**
	 * This method is not used by this iterator.
	 */
	public void remove()
	{
	}

	public Set getEdges()
	{
		return ledges;
	}
}