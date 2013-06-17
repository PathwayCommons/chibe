package org.patika.mada.util;

import org.eclipse.swt.graphics.Color;

/**
 * The interafce for the node associatable and representable data. These data must be able to say
 * how to color the nodes and what to set the tooltip text.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public interface Representable
{
	/**
	 * Tells if this object alters node color.
	 * @return true if alters
	 */
	public boolean alterNodeColor();

	/**
	 * Gets the representing color.
	 */
	public Color getNodeColor();

	/**
	 * Tells if this object alters tool tip text.
	 * @return true if alters
	 */
	public boolean alterToolTipText();

	/**
	 * Gets what to display in tooltip.
	 */
	public String getToolTipText();

	/**
	 * Tells if this object alters text color.
	 * @return true if alters
	 */
	public boolean alterTextColor();

	/**
	 * Gets the representing text color
	 */
	public Color getTextColor();
}
