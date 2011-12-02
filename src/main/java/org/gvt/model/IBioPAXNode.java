package org.gvt.model;

import org.biopax.paxtools.model.level2.Level2Element;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Color;
import org.patika.mada.graph.Node;

import java.util.Collection;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public interface IBioPAXNode extends Node
{
	/**
	 * This is a hash code for differentiating between chisio graph members that correspond to the
	 * same biopax model elements.
	 * @return id hash
	 */
	public String getIDHash();

	public void setLocationAbs(Point p);

	public Point getLocationAbs();

	/**
	 * Each node must know how to read data in their related biopax model and configure its
	 * appearance accordingly. There are simple properties of objects and do not include graph
	 * topological properties.
	 */
	public void configFromModel();

	public Color getHighlightColor();
	public void setHighlightColor(Color color);

	public boolean fetchLocation(String pathwayRDFID);
	public void recordLocation();
	public void eraseLocation();

	//----------------------------------------------------------------------------------------------
	// Section: Model tagging
	//----------------------------------------------------------------------------------------------

	public boolean hasModelTag(String tag);
	public String fetchModelTag(String tag);

}