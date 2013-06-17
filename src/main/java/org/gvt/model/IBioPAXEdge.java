package org.gvt.model;

import org.patika.mada.graph.Edge;
import org.eclipse.swt.graphics.Color;

/**
 * @author Ozgun Babur
 */
public interface IBioPAXEdge extends Edge
{
	public String getIDHash();

	public Color getHighlightColor();
	public void setHighlightColor(Color color);

}
