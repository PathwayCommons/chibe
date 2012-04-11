package org.gvt.model.biopaxl3;

import org.biopax.paxtools.model.level3.Evidence;
import org.biopax.paxtools.model.level3.Interaction;
import org.eclipse.swt.graphics.Color;
import org.gvt.model.NodeModel;
import org.patika.mada.graph.Edge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Used for showing pairwise relations between physical entities.
 * 
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class Member extends BioPAXEdge
{
	public Member(NodeModel parent, NodeModel child)
	{
		super(parent, child);

		assert parent instanceof Actor || parent instanceof ChbComplex;
		assert child instanceof Actor || child instanceof ChbComplex;

		setArrow("Target");
		setColor(COLOR);
		setStyle("Dashed");
	}

	public Member(Member excised, Map<NodeModel, NodeModel> map)
	{
		super(excised, map);
	}

	private static final Color COLOR = new Color(null, 180, 180, 50);

	public int getSign()
	{
		return Edge.NO_SIGN;
	}

	public boolean isDirected()
	{
		return false;
	}

	@Override
	public boolean isEquivalenceEdge()
	{
		return true;
	}
}
