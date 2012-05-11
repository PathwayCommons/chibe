package org.gvt.action;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.model.CompoundModel;
import org.gvt.model.GraphObject;

import java.util.Iterator;

/**
 * Action for highlighting the selected objects in the graph.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class HighlightSelectedAction extends Action
{
	ChisioMain main;

	/**
	 * Constructor
	 */
	public HighlightSelectedAction(ChisioMain main)
	{
		super("Highlight Selected");
		setImageDescriptor(ImageDescriptor.createFromFile(getClass(), "../icon/highlight.png"));
		setToolTipText(getText());
		this.main = main;
	}

	public void run()
	{
		ScrollingGraphicalViewer viewer = main.getViewer();

		if (viewer == null) return;

		// Iterates selected objects; for each selected objects, highlights them
		Iterator selectedObjects =
			((IStructuredSelection) viewer.getSelection()).iterator();

		while (selectedObjects.hasNext())
		{
			Object model = ((EditPart)selectedObjects.next()).getModel();

			if (model instanceof GraphObject)
			{
				((GraphObject) model).setHighlightColor(main.higlightColor);
				((GraphObject) model).setHighlight(true);
			}
		}
	}
}