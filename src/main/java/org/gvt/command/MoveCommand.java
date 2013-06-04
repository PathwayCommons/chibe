package org.gvt.command;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.geometry.Point;
import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;

/**
 * This class implements the move command. This command is called when a graph
 * object is moved.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class MoveCommand extends Command
{
	//moving object
	private NodeModel child;
	//moving object's parent
	private CompoundModel parent;

	private Rectangle constraint;

	private Rectangle oldConstraint;

	/**
	 * Constructor with editpart
	 */
	public MoveCommand(EditPart editPart, Rectangle rect)
	{
		child = (NodeModel) editPart.getModel();
		parent = (CompoundModel) editPart.getParent().getModel();
		constraint = rect;
	}

	/**
	 * Constructor with model
	 */
	public MoveCommand(NodeModel model, Rectangle rect)
	{
		child = model;
		parent = model.getParentModel();
		constraint = rect;
	}

	/**
	 * Handles the resizablity of compound nodes.
	 */
	public void execute()
	{
		oldConstraint = child.getConstraint().getCopy();
		child.setPositiveLocation(constraint);
		parent.calculateSizeUp();
	}

	public void undo()
	{
		child.setConstraint(oldConstraint);
		parent.calculateSizeUp();
	}

	public void redo()
	{
		execute();
	}

	public void setParent(CompoundModel parent) {
		this.parent = parent;
	}
}