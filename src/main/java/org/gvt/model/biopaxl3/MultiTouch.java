package org.gvt.model.biopaxl3;

import org.eclipse.swt.graphics.Color;
import org.gvt.model.NodeModel;
import org.patika.mada.graph.Edge;

import java.util.Map;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class MultiTouch extends BioPAXEdge
{
	public MultiTouch(NodeModel entity, Hub hub)
	{
		super(entity, hub);
		
		assert entity instanceof Actor || entity instanceof ChbComplex;

		setColor(COLOR);
	}

	public MultiTouch(MultiTouch excised, Map<NodeModel, NodeModel> map)
	{
		super(excised, map);
	}

	public int getSign()
	{
		return Edge.NO_SIGN;
	}

	public boolean isDirected()
	{
		return false;
	}

	private static final Color COLOR = new Color(null, 100, 100, 100);
}
