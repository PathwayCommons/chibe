package org.patika.mada.graph;

import org.gvt.model.biopaxl2.Compartment;
import org.patika.mada.util.ExperimentData;
import org.patika.mada.util.Representable;
import org.patika.mada.util.XRef;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public interface Node extends GraphObject
{
	//==========================================================================
	// Section: Node properties
	//==========================================================================

	/**
	 * Checks if this node is a reaction.
	 * @return true if this is an event modeling node
	 */
	public boolean isEvent();

	/**
	 * Checks if this node is a complex member. This node is here because we do not have a common
	 * interface for biopax L2 and L3 objects.
	 * @return
	 */
	public boolean isComplexMember();

	/**
	 * Checks if the node is a transcription event.
	 * @return true if node is a transcription event
	 */
	public boolean isTranscriptionEvent();
	
	//==========================================================================
	// Section: Data methods
	//==========================================================================
	
	/**
	 * Asks if the node has expression data on it.
	 * @param key key to specify type of experiment data
	 * @return true if data of specific type is associated
	 */
	public boolean hasExperimentData(Object key);
	
	/**
	 * Gets the associated data.
	 * @param key key to specify type of experiment data
	 * @return associated data
	 */
	public Representable getRepresentableData(Object key);

	/**
	 * Gets the associated data.
	 * @param type type of experiment data
	 * @return associated data
	 */
	public ExperimentData getExperimentData(String type);

	/**
	 * Sets the given expression data.
	 * @param data to set
	 */
	public void setExperimentData(ExperimentData data);

	/**
	 * This method gets the list of references associated with this node.
	 * @return references list
	 */
	public List<XRef> getReferences();

	//==========================================================================
	// Section: Navigation
	//==========================================================================
	
	/**
	 * Gets nodes that this node contains.
	 * @return children
	 */
	public Collection<? extends Node> getChildren();
	
	/**
	 * Gets nodes that contain this node.
	 * @return parents
	 */
	public Collection<? extends Node> getParents();

	/**
	 * Method to get the containing graph.
	 * @return container graph
	 */
	public Graph getGraph();

	/**
	 * Gets upstream edges.
	 * @return upstream
	 */
	public Collection<? extends Edge> getUpstream();
	
	/**
	 * Gets downstream edges.
	 * @return downstream
	 */
	public Collection<? extends Edge> getDownstream();

	//==========================================================================
	// Section: Class constants
	//==========================================================================

	/**
	 * Will look for the specific experiment data type and check if its value is
	 * significant for considering.
	 *
	 * @param type type of the experiment
	 * @return true if node has significant expeirment data of specified type
	 */
	public boolean hasSignificantExperimentalChange(String type);

	/**
	 * Gets the sign of the specified experiment data. Beware that this throws a
	 * NullPointerException if the node does not have the data.
	 *
	 * @param type type of the experiment
	 * @return sigh of the specified experiment
	 */
	public int getExperimentDataSign(String type);

	/**
	 * Each node must have a name
	 * @return name of the node
	 */
	public String getName();

	/**
	 * Checks if the nodes belong to the same entity. This is used for not
	 * finding causative paths between states of the same entity.
	 *
	 * @param n other node
	 * @return true if they belong same entity
	 */
	public boolean sameEntity(Node n);

	/**
	 * This method says id this kind of node increases the distance when
	 * traversed. For instance conversions do not add a distance while states
	 * add.
	 *
	 * @return true if node is a breadth node.
	 */
	public boolean isBreadthNode();

	/**
	 * A tabu node of this node is the node that should not be traversed if this
	 * node is on the current path. This happens when we traverse a conversion.
	 * If we reach one of its substrates after traversing the conversion, then
	 * this should be handled like a cycle. The generic mechanism for handling
	 * this kind of cycles is this tabu node feature.
	 *
	 * @return tabu nodes
	 */
	public Set<Node> getTabuNodes();
}
