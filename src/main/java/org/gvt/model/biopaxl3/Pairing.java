package org.gvt.model.biopaxl3;

import org.biopax.paxtools.model.level3.*;
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
public class Pairing extends BioPAXEdge
{
	Interaction inter;

	public Pairing(Interaction inter, NodeModel pair1, NodeModel pair2)
	{
		super(pair1, pair2);
		
		assert pair1 instanceof Actor || pair1 instanceof ChbComplex;
		assert pair2 instanceof Actor || pair2 instanceof ChbComplex;

		this.inter = inter;

		setColor(COLOR);
	}

	public Pairing(Pairing excised, Map<NodeModel, NodeModel> map)
	{
		super(excised, map);
		this.inter = excised.getInteraction();
	}

	public Interaction getInteraction()
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
		return ID.get(inter);
	}

	public List<String[]> getInspectable()
	{
		List<String[]> list = new ArrayList<String[]>();

		BioPAXNode.addNamesAndTypeAndID(list, inter);

		for (Evidence ev : inter.getEvidence())
		{
			list.add(new String[]{"Evidence", ev.toString()});
		}

		if (!inter.getInteractionType().isEmpty())
		{
			String s = BioPAXNode.formatInString(inter.getInteractionType());
			list.add(new String[]{"Interaction Type", s});
		}

		BioPAXNode.addDataSourceAndXrefAndComments(list, inter);

		return list;
	}
}
