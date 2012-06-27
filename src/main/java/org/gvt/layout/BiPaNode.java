package org.gvt.layout;

import org.gvt.model.NodeModel;
import org.ivis.layout.LGraphManager;

import java.awt.*;

/**
 * Node for biopax graph layout.
 */
public class BiPaNode extends org.ivis.layout.cose.CoSENode
{
	private int type;
	private boolean hasInfo;
	private String text;

	public static final int COMPLEX = 1;
	public static final int MEMBER = 2;
	public static final int COMPARTMENT = 3;
	public static final int SIMPLE = 4;

	public BiPaNode(LGraphManager gm, Object vNode)
	{
		super(gm, vNode);
		setParam((NodeModel) vNode);
	}

	public BiPaNode(LGraphManager gm, Point loc, Dimension size, Object vNode)
	{
		super(gm, loc, size, vNode);
		setParam((NodeModel) vNode);
	}

	private void setParam(NodeModel model)
	{
		if (model != null) this.text = model.getText();

		if (model instanceof org.gvt.model.biopaxl2.Complex ||
			model instanceof org.gvt.model.biopaxl3.ChbComplex)
		{
			type = COMPLEX;
		}
		else if (model instanceof org.gvt.model.biopaxl2.ComplexMember ||
			model instanceof org.gvt.model.biopaxl3.ComplexMember)
		{
			type = MEMBER;
		}
		else if (model instanceof org.gvt.model.biopaxl2.Compartment ||
			model instanceof org.gvt.model.biopaxl3.Compartment)
		{
			type = COMPARTMENT;
		}
		else
		{
			type = SIMPLE;
		}

		this.hasInfo = model instanceof org.gvt.model.biopaxl2.Actor &&
			((org.gvt.model.biopaxl2.Actor) model).hasInfoString();

		if (!hasInfo)
		{
			this.hasInfo = model instanceof org.gvt.model.biopaxl3.Actor &&
				((org.gvt.model.biopaxl3.Actor) model).hasInfoString();
		}
	}

	public boolean isComplexMember()
	{
		return type == MEMBER;
	}

	public boolean isComplex()
	{
		return type == COMPLEX;
	}

	public boolean isCompartment()
	{
		return type == COMPARTMENT;
	}

	public boolean hasInfo()
	{
		return hasInfo;
	}
}
