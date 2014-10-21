package org.gvt.model.biopaxl3;

import org.biopax.paxtools.model.level3.*;
import org.gvt.model.NodeModel;
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
	Control cont;

	/**
	 * Sign of this effecting process.
	 */
	protected int sign;

	public EffectorSecondHalf(NodeModel source, NodeModel target, Control cont)
	{
		super(source, target);
		assert source instanceof ChbControl;
		assert target instanceof ChbConversion || target instanceof ChbControl ||
			target instanceof Hub || target instanceof ChbTempReac;

		this.cont = cont;
		this.sign = ChbControl.isActivation(cont) ? Edge.POSITIVE : Edge.NEGATIVE;

		setArrow(
			cont instanceof Catalysis ? "Catalysis" :
				cont.getControlType() == null ? "Modulation" :
					this.isPositive() ? "Stimulation" : "Inhibition");

		setColor(this.isPositive() ? ChbControl.EDGE_COLOR_ACTIVATE : ChbControl.EDGE_COLOR_INHIBIT);
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

	public Control getControl()
	{
		return cont;
	}

	public String getIDHash()
	{
		return cont.getRDFId() + ((IBioPAXL3Node) getTargetNode()).getIDHash();
	}
}
