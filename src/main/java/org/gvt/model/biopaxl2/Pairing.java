package org.gvt.model.biopaxl2;

import org.biopax.paxtools.model.level2.interaction;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.evidence;
import org.biopax.paxtools.model.level2.physicalInteraction;
import org.eclipse.swt.graphics.Color;
import org.gvt.model.NodeModel;
import org.gvt.util.ID;
import org.patika.mada.graph.Edge;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Used for showing pairwise relations between physical entities.
 * 
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class Pairing extends PEPBasedEdge
{
	interaction inter;

	public Pairing(interaction inter, physicalEntityParticipant first,
		NodeModel pair1, NodeModel pair2)
	{
		super(pair1, pair2, first);
		
		assert pair1 instanceof Actor || pair1 instanceof Complex;
		assert pair2 instanceof Actor || pair2 instanceof Complex;

		this.inter = inter;

		setColor(COLOR);
	}

	public Pairing(Pairing excised, Map<NodeModel, NodeModel> map)
	{
		super(excised, map);
		this.inter = excised.getInteraction();
	}

	public interaction getInteraction()
	{
		return inter;
	}

	private static final Color COLOR = new Color(null, 100, 100, 100);

	public int getSign()
	{
		return Edge.NO_SIGN;
	}

	public boolean isDirected()
	{
		return false;
	}

	public String getIDHash()
	{
		return super.getIDHash() + ID.get(inter);
	}

	public List<String[]> getInspectable()
	{
		List<String[]> list = new ArrayList<String[]>();

		BioPAXNode.addNamesAndTypeAndID(list, inter);

		for (evidence ev : inter.getEVIDENCE())
		{
			list.add(new String[]{"Evidence", ev.toString()});
		}

		if (inter instanceof physicalInteraction)
		{
			if (!((physicalInteraction) inter).getINTERACTION_TYPE().isEmpty())
			{
				String s = BioPAXNode.formatInString(
					((physicalInteraction) inter).getINTERACTION_TYPE());
				list.add(new String[]{"Interaction Type", s});
			}
		}

		BioPAXNode.addDataSourceAndXrefAndComments(list, inter);

		return list;
	}
}
