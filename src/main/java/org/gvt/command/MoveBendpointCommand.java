package org.gvt.command;

import org.eclipse.draw2d.Bendpoint;
import org.gvt.model.EdgeBendpoint;

/**
 * This class maintains the move command for bendpoints. This command is called
 * when a bendpoint is moved.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class MoveBendpointCommand
	extends BendpointCommand
{
	private Bendpoint oldBendpoint;

	public void execute()
	{
		EdgeBendpoint bp = new EdgeBendpoint();
		bp.setRelativeDimensions(getFirstRelativeDimension(),
			getSecondRelativeDimension());
		setOldBendpoint((Bendpoint) getWire().getBendpoints().get(getIndex()));
		getWire().setBendpoint(getIndex(), bp);
		super.execute();
	}

	protected Bendpoint getOldBendpoint()
	{
		return oldBendpoint;
	}

	public void setOldBendpoint(Bendpoint bp)
	{
		oldBendpoint = bp;
	}

	public void undo()
	{
		super.undo();
		getWire().setBendpoint(getIndex(), getOldBendpoint());
	}
}