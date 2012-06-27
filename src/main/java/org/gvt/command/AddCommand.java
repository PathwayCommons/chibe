package org.gvt.command;

import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.commands.Command;
import org.gvt.model.*;

/**
 * This command adds the given child to the given parent's children list.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */

public class AddCommand extends Command
{
	private NodeModel child;

	private CompoundModel parent;

	public AddCommand()
	{
		super("AddCommand_Label");
	}

	public void execute()
	{
		parent.addChild(child);
		child.setParentModel(parent);
	}

	public CompoundModel getParent()
	{
		return parent;
	}

	public void redo()
	{
		parent.addChild(child);
		child.setParentModel(parent);
	}

	public void setChild(NodeModel subpart)
	{
		child = subpart;
	}

	public void setParent(CompoundModel newParent)
	{
		parent = newParent;
	}

	public void undo()
	{
		parent.removeChild(child);
	}

	public void setConstraint(Rectangle rect)
	{
		child.setConstraint(rect);
	}
}