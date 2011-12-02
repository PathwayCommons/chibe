package org.gvt.model.biopaxl3;

import org.biopax.paxtools.model.level3.Level3Element;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Color;
import org.patika.mada.graph.Node;
import org.gvt.model.IBioPAXNode;

import java.util.Collection;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public interface IBioPAXL3Node extends IBioPAXNode
{
	/**
	 * Gets the biopax elemts that the layout information will be stored.
	 * @return layout related biopax elements
	 */
	public Collection<? extends Level3Element> getRelatedModelElements();
}
