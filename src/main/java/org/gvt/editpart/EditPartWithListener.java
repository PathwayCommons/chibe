package org.gvt.editpart;

import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.*;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.gvt.model.NodeModel;
import org.gvt.ChisioMain;

/**
 * This class maintains base editpart for other editparts. This editpart has
 * a listener to handle the changes.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
abstract public class EditPartWithListener extends AbstractGraphicalEditPart
	implements PropertyChangeListener
{
	public void setLayoutConstraint(
		EditPart child,
		IFigure childFigure,
		Object constraint)
	{
		childFigure.getParent().setConstraint(childFigure, constraint);
	}

	public void canOrphanChildren(Request request)
	{
		if ((request.getType().equals("orphan")))
			request.setType("move");
		else if ((request.getType().equals("add children")))
			request.setType("move");
	}

	public Command getCommand(Request request)
	{
		Command command = null;
		Command tmp;
		EditPolicyIterator i = getEditPolicyIterator();

		while (i.hasNext())
		{
			EditPolicy ep = i.next();

			/*OK*/
			if(!ChisioMain.transferNode)
				canOrphanChildren(request);

			tmp = ep.getCommand(request);


			if (command != null)
				command = command.chain(tmp);
			else
				command = tmp;
		}

		return command;
	}

	/**
	 * activates the listener for this editpart
	 */
	public void activate()
	{
		getNodeModel().addPropertyChangeListener(this);
		super.activate();
	}

	/**
	 * deactivates the listener for this editpart
	 */
	public void deactivate()
	{
		getNodeModel().removePropertyChangeListener(this);
		super.deactivate();
	}

	public NodeModel getNodeModel()
	{
		return (NodeModel) getModel();
	}
}