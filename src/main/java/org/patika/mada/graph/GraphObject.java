package org.patika.mada.graph;

import java.util.Set;
import java.util.List;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public interface GraphObject
{
	//==============================================================================================
	// Section: Identification
	//==============================================================================================

	/**
	 * Gets the stable identifier.
	 */
	public int getId();

	//==========================================================================
	// Section: Labeling
	//==========================================================================

	/**
	 * Puts the specified label.
	 */
	public void putLabel(Object label);
	
	/**
	 * Updates the value of the label. Or creates if does not exist.
	 */
	public void putLabel(Object label, Object value);
	
	/**
	 * Checks if the specified label type exists on the node.
	 */
	public boolean hasLabel(Object label);
	
	/**
	 * Checks if the specified type label exists and its value matches to the 
	 * second parameter.
	 */
	public boolean hasLabel(Object label, Object value);

	/**
	 * Gets the associated label on the object specified with the key.
	 */
	public Object getLabel(Object label);
	
	/**
	 * Removes the label specified with the parameter key.
	 */
	public void removeLabel(Object label);

	/**
	 * Hihlights the object.
	 * @param highlight on (true) or off (false)
	 */
	public void setHighlight(boolean highlight);

	/**
	 * Checks if the object is highlighted.
	 * @return true if is highlighted
	 */
	public boolean isHighlighted();

	/**
	 * This method is used by the excise mechanism. Each object knows its requisite other objects
	 * to be excised properly.
	 * @return requisites
	 */
	public Set<GraphObject> getRequisites();

	/**
	 * Gets a list of properties to inspect in the inspector window for the related graph object.
	 */
	public List<String[]> getInspectable();
}
