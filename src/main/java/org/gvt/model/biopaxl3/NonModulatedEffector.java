package org.gvt.model.biopaxl3;

import org.biopax.paxtools.model.level3.*;
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
public class NonModulatedEffector extends BioPAXEdge
{
	/**
	 * Control in BioPAX.
	 */
	private Control cont;

	private Interaction controlled;

	private int sign;

	public NonModulatedEffector(NodeModel source, NodeModel target, Control cont,
		Interaction controlled)
	{
		super(source, target);

		assert source instanceof Actor || source instanceof ChbComplex || source instanceof ChbPathway;
		assert target instanceof ChbConversion || target instanceof ChbControl || target instanceof Hub;

		setArrow("Target");
		this.cont = cont;
		this.controlled = controlled;
		this.sign = ChbControl.isActivation(cont) ? Edge.POSITIVE : Edge.NEGATIVE;

		setColor(isPositive() ? ChbControl.EDGE_COLOR_ACTIVATE : ChbControl.EDGE_COLOR_INHIBIT);
	}

	public NonModulatedEffector(NonModulatedEffector excised, Map<NodeModel, NodeModel> map)
	{
		super(excised, map);
		this.cont = excised.getControl();
		this.controlled = excised.getControlled();
		this.sign = excised.getSign();
	}

	public Control getControl()
	{
		return cont;
	}

	public Interaction getControlled()
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

		for (Evidence ev : cont.getEvidence())
		{
			list.add(new String[]{"Evidence", ev.toString()});
		}

		if (!cont.getInteractionType().isEmpty())
		{
			String s = BioPAXNode.formatInString(cont.getInteractionType());
			list.add(new String[]{"Interaction Type", s});
		}

		if (cont.getControlType() != null)
		{
			list.add(new String[]{"Control Type", cont.getControlType().toString()});
		}

		BioPAXNode.addDataSourceAndXrefAndComments(list, cont);

		return list;
	}
}
