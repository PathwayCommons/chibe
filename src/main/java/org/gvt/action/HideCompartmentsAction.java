package org.gvt.action;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.gvt.ChisioMain;
import org.gvt.command.RemoveCompoundCommand;
import org.gvt.editpart.ChsCompoundEditPart;
import org.gvt.model.CompoundModel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Action for deleting the compound node without deleting the inner nodes.
 * Children are kept, they are taken out from compound node and stay in the same
 * absolute location.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class HideCompartmentsAction extends Action
{
	CompoundModel root;
	ChisioMain main;

	/**
	 * Constructor
	 */
	public HideCompartmentsAction(ChisioMain main)
	{
		super("Hide Compartments");
		this.setImageDescriptor(ImageDescriptor.createFromFile(
			ChisioMain.class, "icon/delete-comp.gif"));
		this.setToolTipText(getText());
		this.main = main;
	}

	public void run()
	{
		ScrollingGraphicalViewer viewer = main.getViewer();

		if (viewer == null) return;

		Set<ChsCompoundEditPart> set = new HashSet<ChsCompoundEditPart>();
		collectCompartments(viewer.getRootEditPart().getContents(), set);

		for (ChsCompoundEditPart compartment : set)
		{
			RemoveCompoundCommand command = new RemoveCompoundCommand();
			command.setCompound((CompoundModel) compartment.getModel());
			command.execute();
		}
	}
	
	protected void collectCompartments(EditPart ep, Set<ChsCompoundEditPart> compartments)
	{
		if (ep instanceof ChsCompoundEditPart &&
			(ep.getModel() instanceof org.gvt.model.biopaxl3.Compartment ||
				ep.getModel() instanceof org.gvt.model.biopaxl2.Compartment))
		{
			compartments.add((ChsCompoundEditPart) ep);
		}

		for (Object o : ep.getChildren())
		{
			if (o instanceof EditPart) collectCompartments((EditPart) o, compartments);
		}
	}
}