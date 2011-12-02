package org.gvt.action;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.gvt.ChisioMain;
import org.gvt.command.DeleteCommand;
import org.gvt.command.DeleteConnectionCommand;
import org.gvt.editpart.ChsEdgeEditPart;
import org.gvt.editpart.ChsNodeEditPart;
import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;

import java.util.Iterator;

/**
 * This class maintains action for deleting the graph objects.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class DeleteAction extends Action
{
	private ChisioMain main;

	public DeleteAction(ChisioMain main)
	{
		super("Hide Selected");
		setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class, "icon/delete.gif"));
		this.main = main;
		setToolTipText(getText());
	}

	public void run()
	{
		ScrollingGraphicalViewer viewer = main.getViewer();

		if (viewer == null) return;

		// Iterates the selected objects to delete
		Iterator selectedObjects = ((IStructuredSelection) viewer
			.getSelection()).iterator();
		// for each of seleceted objects, delete command is executed
		while (selectedObjects.hasNext())
		{
			EditPart childEditPart = (EditPart) selectedObjects.next();
			// if selected one is a node or compound DeleteCommand is called
			if (childEditPart instanceof ChsNodeEditPart)
			{
				NodeModel node = (NodeModel) childEditPart.getModel();
				DeleteCommand command = new DeleteCommand();
				command.setChild(node);
				EditPart parent = childEditPart.getParent();
				command.setParent((CompoundModel) parent.getModel());
				command.execute();
			}
			// else if it is an edge, DeleteConnectionCommand is called
			else if (childEditPart instanceof ChsEdgeEditPart)
			{
				{
					DeleteConnectionCommand command
						= new DeleteConnectionCommand();
					command.setConnectionModel(childEditPart.getModel());
					command.execute();
				}
			}
		}
	}
}