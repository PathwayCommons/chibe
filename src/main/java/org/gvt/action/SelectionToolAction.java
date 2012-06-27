package org.gvt.action;

import org.eclipse.gef.*;
import org.eclipse.gef.tools.SelectionTool;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;

/**
 * Tool to select and manipulate figures.
 * A selection tool is in one of three states, e.g., background selection,
 * figure selection, handle manipulation.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class SelectionToolAction extends AbstractGEFToolAction
{
	public SelectionToolAction(String text, ChisioMain main)
	{
		super(text, main);
		setImageDescriptor(ImageDescriptor.createFromFile(
			ChisioMain.class, "icon/select.png"));
	}

	protected Tool createTool()
	{
		SelectionTool tool = new SelectionTool()
		{
			public void activate()
			{
				setChecked(true);
				super.activate();
			}

			public void deactivate()
			{
				setChecked(false);
				super.deactivate();
			}
		};

		if (main.getEditDomain() != null)
		{
			main.getEditDomain().setDefaultTool(tool);
			main.getEditDomain().setActiveTool(tool);
		}

		return tool;
	}
}