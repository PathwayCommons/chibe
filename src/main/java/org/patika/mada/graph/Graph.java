package org.patika.mada.graph;

import java.util.Collection;

/**
 * A graph holds its nodes and edges. Insertion methods also ensures integrity.
 * 
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public interface Graph
{
	public Collection<? extends Node> getNodes();
	public Collection<? extends Edge> getEdges();

	/**
	 * Removes the parameter labels from all nodes
	 * @param labels keys of labels to remove
	 */
	public void removeLabels(Collection labels);

	public Graph excise(Collection<GraphObject> objects);
}
