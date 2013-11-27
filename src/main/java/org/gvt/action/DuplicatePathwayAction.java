package org.gvt.action;

import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.model.BioPAXGraph;
import org.gvt.util.PathwayHolder;

/**
 * Action for closing the currently open tab.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class DuplicatePathwayAction extends Action
{
	private ChisioMain main;

	/**
	 * Constructor
	 */
	public DuplicatePathwayAction(ChisioMain main)
	{
		super("Duplicate Pathway");
		setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class, "icon/pathway-create-duplicate.png"));
		setToolTipText(getText());
		this.main = main;
	}

	public void run()
	{
		BioPAXGraph original = main.getPathwayGraph();

		if (original == null)
		{
			MessageDialog.openError(main.getShell(), "Error!", "No BioPAX pathway.");
			return;
		}

		if (!original.isMechanistic())
		{
			MessageDialog.openError(main.getShell(), "Not Supported!",
				"Duplication is supported only in mechanistic views.");

			return;
		}

		original.recordLayout();

		PathwayHolder p = original.getPathway();

		String name = p.getName();

		// If the name contains a copy number at the end (like "pathway name (2)"), remove it

		if (name.indexOf(" ") > 0)
		{
			String last = name.substring(name.lastIndexOf(" ") + 1);
			if (last.indexOf("(") == 0 && last.indexOf(")") == last.length() - 1)
			{
				boolean isdigit = true;
				for (int i = 1; isdigit && i < last.length() - 1; i++)
				{
					isdigit = Character.isDigit(last.charAt(i));
				}

				if (isdigit) name = name.substring(0, name.lastIndexOf(" "));
			}
		}

		PathwayHolder newP = new PathwayHolder(main.getBioPAXModel(), name);
		new OpenPathwaysAction(main, newP.getName());
	}
}