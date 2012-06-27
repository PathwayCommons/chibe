package org.gvt.command;

import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.commands.Command;
import org.gvt.model.*;

/**
 * This command creates the given child in the given parent's children list.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class CreateCommand extends Command
{
	private NodeModel child;

	private CompoundModel parent;

	public CreateCommand(CompoundModel parent, NodeModel child)
	{
		this.parent = parent;
		this.child = child;
	}

	public boolean canExecute()
	{
		return true;
	}

	public void execute()
	{
		parent.addChild(child);
		child.setParentModel(parent);
		parent.calculateSizeUp();
	}

	public void setConstraint(Rectangle rect)
	{
		if (child instanceof CompoundModel)
		{
			child.setConstraint(new Rectangle(rect.getLocation(),
				CompoundModel.DEFAULT_SIZE));
		}
		else if (rect.width <= 0 && rect.height <= 0)
		{
			child.setConstraint(new Rectangle(rect.getLocation(),
				NodeModel.DEFAULT_SIZE));	
		}
		else
		{
			child.setConstraint(rect);
		}
	}
}