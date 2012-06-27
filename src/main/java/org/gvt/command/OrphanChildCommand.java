package org.gvt.command;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;
import org.gvt.model.*;

/**
 * This command removes the given child from given parent.
 *
 * @author Cihan Kucukkececi (modified by)
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class OrphanChildCommand extends Command
{
	private Point oldLocation;

	private CompoundModel parent;

	private NodeModel child;

	private boolean onlyRemoveCompound = false;

	public OrphanChildCommand()
	{
		super("OrphanChildCommand");
	}

	public void setOnlyRemoveCompound(boolean onlyRemoveCompound)
	{
		this.onlyRemoveCompound = onlyRemoveCompound;
	}
	
	public void execute()
	{
		this.oldLocation = child.getLocation();
		parent.removeChild(child);
		child.setParentModel(null);

		if (!onlyRemoveCompound)
		{
			parent.calculateSizeUp();
		}
	}

	public void redo()
	{
		parent.removeChild(child);
	}

	public void setChild(NodeModel child)
	{
		this.child = child;
	}

	public void setParent(CompoundModel parent)
	{
		this.parent = parent;
	}

	public void undo()
	{
		child.setLocation(oldLocation);
		parent.addChild(child);
		child.setParentModel(parent);
	}

	public NodeModel getChild()
	{
		return child;
	}
}