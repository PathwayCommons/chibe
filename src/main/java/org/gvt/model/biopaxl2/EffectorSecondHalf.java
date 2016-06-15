package org.gvt.model.biopaxl2;

import org.biopax.paxtools.model.level2.control;
import org.biopax.paxtools.model.level2.catalysis;
import org.biopax.paxtools.model.level2.modulation;
import org.gvt.model.NodeModel;
import org.gvt.util.ID;
import org.patika.mada.graph.Edge;

import java.util.Map;

/**
 * This is used when modeling modulated controls. This edge connects the control
 * node to the controlled interaction.
 * 
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class EffectorSecondHalf extends BioPAXEdge
{
	/**
	 * BioPAX control.
	 */
	control cont;

	/**
	 * Sign of this effecting process.
	 */
	protected int sign;

	public EffectorSecondHalf(NodeModel source, NodeModel target, control cont)
	{
		super(source, target);
		assert source instanceof Control;
		assert target instanceof Conversion || target instanceof Control || target instanceof Hub;

		this.cont = cont;
		this.sign = Control.isActivation(cont) ? Edge.POSITIVE : Edge.NEGATIVE;

		setArrow(
			cont instanceof catalysis ? "Catalysis" :
				cont.getCONTROL_TYPE() == null ? "Modulation" :
					this.isPositive() ? "Stimulation" : "Inhibition");

		setColor(this.isPositive() ? Control.EDGE_COLOR_ACTIVATE : Control.EDGE_COLOR_INHIBIT);
	}

	public EffectorSecondHalf(EffectorSecondHalf excised, Map<NodeModel, NodeModel> map)
	{
		super(excised, map);
		this.cont = excised.getControl();
		this.sign = excised.getSign();
	}

	public int getSign()
	{
		return sign;
	}

	public control getControl()
	{
		return cont;
	}

	public String getIDHash()
	{
		return ID.get(cont) + ((IBioPAXL2Node) getTargetNode()).getIDHash();
	}
}
