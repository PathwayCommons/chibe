package org.gvt.command;

import org.gvt.model.EdgeBendpoint;

/**
 * This class maintains the add command for bendpoints. this command is called
 * when a bendpoint is created.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class CreateBendpointCommand extends BendpointCommand
{
	public void execute()
	{
		EdgeBendpoint ebp = new EdgeBendpoint();
		ebp.setRelativeDimensions(
			getFirstRelativeDimension(),
			getSecondRelativeDimension());
		getWire().insertBendpoint(getIndex(), ebp);
		super.execute();
	}

	public void undo()
	{
		super.undo();
		getWire().removeBendpoint(getIndex());
	}
}