package org.gvt.model.biopaxl2;

import org.biopax.paxtools.model.level2.*;
import org.gvt.model.NodeModel;
import org.patika.mada.graph.Edge;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * When the controller has no modulator, this edge is used to connect controller
 * actor to the controlled interaction.
 * 
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class NonModulatedEffector extends PEPBasedEdge
{
	/**
	 * Control in BioPAX.
	 */
	private control cont;

	private interaction controlled;

	private int sign;

	public NonModulatedEffector(NodeModel source, NodeModel target, control cont,
		physicalEntityParticipant pep, interaction controlled)
	{
		super(source, target, pep);

		assert source instanceof Actor || source instanceof Complex;
		assert target instanceof Conversion || target instanceof Control || target instanceof Hub;

		this.cont = cont;
		this.controlled = controlled;
		this.sign = Control.isActivation(cont) ? Edge.POSITIVE : Edge.NEGATIVE;

		setColor(isPositive() ? Control.EDGE_COLOR_ACTIVATE : Control.EDGE_COLOR_INHIBIT);
		setArrow(
			cont instanceof catalysis ? "Catalysis" :
				cont.getCONTROL_TYPE() == null ? "Modulation" :
					this.isPositive() ? "Stimulation" : "Inhibition");

	}

	public NonModulatedEffector(NonModulatedEffector excised, Map<NodeModel, NodeModel> map)
	{
		super(excised, map);
		this.cont = excised.getControl();
		this.controlled = excised.getControlled();
		this.sign = excised.getSign();
	}

	public control getControl()
	{
		return cont;
	}

	public interaction getControlled()
	{
		return controlled;
	}

	public int getSign()
	{
		return sign;
	}

	public String getIDHash()
	{
		return super.getIDHash() + cont.getRDFId();
	}

	public List<String[]> getInspectable()
	{
		List<String[]> list = new ArrayList<String[]>();

		BioPAXNode.addNamesAndTypeAndID(list, cont);

		for (evidence ev : cont.getEVIDENCE())
		{
			list.add(new String[]{"Evidence", ev.toString()});
		}

		if (!cont.getINTERACTION_TYPE().isEmpty())
		{
			String s = BioPAXNode.formatInString(cont.getINTERACTION_TYPE());
			list.add(new String[]{"Interaction Type", s});
		}

		if (cont.getCONTROL_TYPE() != null)
		{
			list.add(new String[]{"Control Type", cont.getCONTROL_TYPE().toString()});
		}

		BioPAXNode.addDataSourceAndXrefAndComments(list, cont);

		return list;
	}
}
