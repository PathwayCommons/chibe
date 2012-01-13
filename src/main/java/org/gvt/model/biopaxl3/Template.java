package org.gvt.model.biopaxl3;

import org.eclipse.swt.graphics.Color;
import org.gvt.model.NodeModel;
import org.patika.mada.graph.Edge;

import java.util.Map;

/**
 * Left of a conversion.
 * 
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class Template extends BioPAXEdge
{
	public Template(NodeModel source, ChbTempReac target)
	{
		super(source, target);

		assert source instanceof Actor;

//		setArrow("Target");
		setColor(COLOR);
	}

	public Template(Template excised, Map<NodeModel, NodeModel> map)
	{
		super(excised, map);
	}

	private static final Color COLOR = new Color(null, 0, 50, 50);

	public int getSign()
	{
		return Edge.POSITIVE;
	}
}
