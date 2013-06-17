package org.gvt.model.biopaxl2;

import org.eclipse.swt.graphics.Color;
import org.gvt.model.NodeModel;
import org.patika.mada.graph.Edge;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;

import java.util.Map;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class MultiTouch extends PEPBasedEdge
{
	public MultiTouch(NodeModel entity, Hub hub, physicalEntityParticipant pep)
	{
		super(entity, hub, pep);
		
		assert entity instanceof Actor || entity instanceof Complex;

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
