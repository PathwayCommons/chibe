package org.gvt.model.biopaxl2;

import org.eclipse.swt.graphics.Color;
import org.gvt.model.NodeModel;
import org.patika.mada.graph.Edge;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;

import java.util.Map;

/**
 * Left of a conversion.
 * 
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class Substrate extends PEPBasedEdge
{
	public Substrate(NodeModel source, Conversion target, physicalEntityParticipant pep)
	{
		super(source, target, pep);
		
		assert source instanceof Actor || source instanceof Complex;
		
//		setArrow("Target");
		setColor(COLOR);
	}

	public Substrate(Substrate excised, Map<NodeModel, NodeModel> map)
	{
		super(excised, map);
	}

	private static final Color COLOR = new Color(null, 0, 0, 0);

	public int getSign()
	{
		return Edge.POSITIVE;
	}
}
