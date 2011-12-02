package org.gvt.layout;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.gvt.model.NodeModel;

/**
 * Node for biopax graph layout.
 */
public class BiPaNode extends CoSENode
{
	private int type;
	private boolean hasInfo;

	public static final int COMPLEX = 1;
	public static final int MEMBER = 2;
	public static final int COMPARTMENT = 3;
	public static final int SIMPLE = 4;

	public BiPaNode(LGraphManager gm, NodeModel model, Point loc, Dimension size)
	{
		super(gm, loc, size);
		setParam(model);
	}

	public BiPaNode(LGraphManager gm, NodeModel model)
	{
		super(gm);
		setParam(model);
	}

	private void setParam(NodeModel model)
	{
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

	public boolean isReduced()
	{
		if (this.isComplexMember()) return true;
		return super.isReduced();
	}
}
