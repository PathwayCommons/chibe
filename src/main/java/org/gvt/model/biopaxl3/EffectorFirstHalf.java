package org.gvt.model.biopaxl3;

import org.eclipse.swt.graphics.Color;
import org.gvt.model.NodeModel;
import org.patika.mada.graph.Edge;

import java.util.Map;

/**
 * Used for modeling modulated controls. This edge connects the controller to the
 * control node.
 * 
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class EffectorFirstHalf extends BioPAXEdge
{
	public EffectorFirstHalf(NodeModel source, NodeModel target)
	{
		super(source, target);
		
		assert source instanceof Actor || source instanceof ChbComplex;
		assert target instanceof ChbControl;

//		setArrow("Target");
		setColor(COLOR);
	}

	public EffectorFirstHalf(EffectorFirstHalf excised, Map<NodeModel, NodeModel> map)
	{
		super(excised, map);
	}

	private static final Color COLOR = new Color(null, 0, 0, 0);

	public int getSign()
	{
		return Edge.POSITIVE;
	}


}
