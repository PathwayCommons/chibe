package org.gvt.editpolicy;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.GroupRequest;
import org.gvt.command.DeleteCommand;
import org.gvt.model.GraphObject;
import org.gvt.model.NodeModel;
import org.gvt.model.CompoundModel;

/**
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class ChsComponentEditPolicy
	extends org.eclipse.gef.editpolicies.ComponentEditPolicy
{
	protected Command createDeleteCommand(GroupRequest request)
	{
		Object parent = getHost().getParent().getModel();
		DeleteCommand deleteCmd = new DeleteCommand();
		deleteCmd.setParent((CompoundModel) parent);
		deleteCmd.setChild((NodeModel) getHost().getModel());
		return deleteCmd;
	}
}