package org.gvt.command;

import org.eclipse.draw2d.Bendpoint;

/**
 * This class maintains the delete command for bendpoints. this command is
 * called when a bendpoint is deleted.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class DeleteBendpointCommand extends BendpointCommand
{
	private Bendpoint bendpoint;

	public void execute()
	{
		bendpoint = (Bendpoint) getWire().getBendpoints().get(getIndex());
		getWire().removeBendpoint(getIndex());
		super.execute();
	}

	public void undo()
	{
		super.undo();
		getWire().insertBendpoint(getIndex(), bendpoint);
	}
}