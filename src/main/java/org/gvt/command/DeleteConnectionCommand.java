package org.gvt.command;

import org.eclipse.gef.commands.Command;
import org.gvt.model.EdgeModel;

/**
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class DeleteConnectionCommand extends Command
{
	private EdgeModel connection;

	public void execute()
	{
		connection.setHighlight(false);
		connection.detachSource();
		connection.detachTarget();
		connection.getSource().getParentModel().calculateSizeUp();
	}

	public void setConnectionModel(Object model)
	{
		connection = (EdgeModel) model;
	}

	public void undo()
	{
		connection.attachSource();
		connection.attachTarget();
	}
}