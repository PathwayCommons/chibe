package org.gvt.editpolicy;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editpolicies.ContainerEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.GroupRequest;
import org.gvt.command.OrphanChildCommand;
import org.gvt.model.GraphObject;
import org.gvt.model.NodeModel;
import org.gvt.model.CompoundModel;

/**
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class ChsContainerEditPolicy	extends ContainerEditPolicy
{
	protected Command getCreateCommand(CreateRequest request)
	{
		return null;
	}

	public Command getOrphanChildrenCommand(GroupRequest request)
	{
		List parts = request.getEditParts();
		CompoundCommand result =
			new CompoundCommand("MyContainerEditPolicy_OrphanCommandLabelText");

		for (int i = 0; i < parts.size(); i++) {
			OrphanChildCommand orphan = new OrphanChildCommand();
			orphan.setChild((NodeModel)((EditPart)parts.get(i)).getModel());
			orphan.setParent((CompoundModel)getHost().getModel());
			result.add(orphan);
		}

		return result.unwrap();
	}
}