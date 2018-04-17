package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.gvt.ChisioMain;
import org.gvt.editpart.ChsRootEditPart;
import org.gvt.model.CompoundModel;
import org.gvt.model.EdgeModel;
import org.gvt.model.NodeModel;

import java.util.Iterator;
import java.util.List;

/**
 * Action for removing the highlight from all objects in the graph.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class RemoveColorsAction extends Action
{
	ChisioMain main;
	static final Color WHITE = new Color(null, 255, 255, 255);

	/**
	 * Constructor
	 */
	public RemoveColorsAction(ChisioMain main)
	{
		super("Remove node colors");
		setToolTipText(getText());
		this.main = main;
	}

	public void run()
	{
		if (main.getViewer() == null) return;

		CompoundModel root = (CompoundModel) ((ChsRootEditPart)main.getViewer().
			getRootEditPart().getChildren().get(0)).getModel();

		Iterator<NodeModel> nodeIter = root.getNodes().iterator();

		while (nodeIter.hasNext())
		{
			NodeModel node = nodeIter.next();
			node.setColor(WHITE);
		}
	}
}
