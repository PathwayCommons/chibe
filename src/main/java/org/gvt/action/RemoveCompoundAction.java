package org.gvt.action;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.gvt.ChisioMain;
import org.gvt.command.RemoveCompoundCommand;
import org.gvt.editpart.ChsCompoundEditPart;
import org.gvt.model.CompoundModel;

import java.util.Iterator;

/**
 * Action for deleting the compound node without deleting the inner nodes.
 * Children are kept, they are taken out from compound node and stay in the same
 * absolute location.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class RemoveCompoundAction extends Action
{
	CompoundModel root;
	ChisioMain window = null;
	ChisioMain main;

	/**
	 * Constructor
	 */
	public RemoveCompoundAction(ChisioMain main)
	{
		super("Hide Compound");
		this.setImageDescriptor(ImageDescriptor.createFromFile(
			getClass(), "../icon/delete-comp.gif"));
		this.setToolTipText(getText());
		this.main = main;
	}

	public void run()
	{
		ScrollingGraphicalViewer viewer = main.getViewer();

		if (viewer == null) return;

		// Iterates selected objects; for each of selected objects, delete
		// command is executed
		Iterator selectedObjects =
			((IStructuredSelection) viewer.getSelection()).iterator();

		while (selectedObjects.hasNext())
		{
			EditPart childEditPart = (EditPart) selectedObjects.next();

			// if selected one is a node or compound delete is called
			if (childEditPart instanceof ChsCompoundEditPart)
			{
				RemoveCompoundCommand command = new RemoveCompoundCommand();
				command.setCompound((CompoundModel) childEditPart.getModel());
				command.execute();
			}
		}
	}
}