package org.patika.mada.graph;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public interface Edge extends GraphObject
{
	//==============================================================================================
	// Section: Connectivity
	//==============================================================================================

	/**
	 * Gets the source node of this edge.
	 */
	public Node getSourceNode();

	/**
	 * Gets the target node of this edge.
	 */
	public Node getTargetNode();

	//==============================================================================================
	// Section: Edge properties
	//==============================================================================================
	
	/**
	 * Asks if this is a positive edge.
	 */
	public boolean isPositive();

	/**
	 * Asks if this is a negative edge.
	 */

	public boolean isNegative();
	/**
	 * Should return POSITIVE if positive, NEGATIVE if negative.
	 */
	public int getSign();

	/**
	 * Asks if this edge is on a way of transcriptional regulation.
	 */
	public boolean isTranscription();

	/**
	 * PTM: Post Translational Modification.
	 */
	public boolean isPTM();

	/**
	 * Tells whether the edge is directed.
	 */
	public boolean isDirected();

	/**
	 * Tells if it is possible to contruct causal relations using this edge.
	 * @return true if causative
	 */
	public boolean isCausative();

	/**
	 * Tells if the edge may connect two nodes that are considered to be distant from each other by
	 * a non-zero value. During a breadth-first traversal, if the edge is not breadth edge, then no
	 * distance would be progressed.
	 */
	public boolean isBreadthEdge();

	/**
	 * Tells if the edge connects equivalent nodes. For instance generics and members are connected
	 * with equivalence edges.
	 */
	public boolean isEquivalenceEdge();

	/**
	 * Possible sign.
	 */
	public static final int POSITIVE = 1;

	/**
	 * Possible sign.
	 */
	public static final int NEGATIVE = -1;

	/**
	 * Possible sign.
	 */
	public static final int NO_SIGN = 0;
}
