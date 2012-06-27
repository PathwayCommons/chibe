package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.gvt.model.CompoundModel;
import org.gvt.model.CompoundModel;

/**
 * This class maintains the action for changing the margins of compound nodes.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class ChangeMarginAction extends Action
{
	// Root model of the graph
	CompoundModel root;

	// new value for margin of compound nodes
	int newMargin;

	public ChangeMarginAction(CompoundModel root, int margin)
	{
		this.root = root;
		newMargin = margin;
	}

	public void run()
	{
		// creates a new compound model for setting the margin value
		CompoundModel cm = new CompoundModel();
		cm.setParentModel(root);
		cm.setMarginSize(newMargin);
	}
}