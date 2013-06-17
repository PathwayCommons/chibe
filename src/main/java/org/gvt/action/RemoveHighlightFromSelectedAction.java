package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.model.CompoundModel;
import org.gvt.model.GraphObject;

/**
 * Action for removing the highlight from selected objects in the graph.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class RemoveHighlightFromSelectedAction extends Action
{
	CompoundModel root;
	ChisioMain main;

	/**
	 * Constructor
	 */
	public RemoveHighlightFromSelectedAction(ChisioMain main)
	{
		super("Unhighlight Selected");
		setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class, "icon/unhighlight.png"));
		setToolTipText(getText());
		this.main = main;
	}

	public void run()
	{
		if (main.getViewer() == null) return;

		for (Object model : main.getSelectedModel())
		{
			if (model instanceof GraphObject)
			{
				((GraphObject) model).setHighlight(false);
			}
		}
	}
}
