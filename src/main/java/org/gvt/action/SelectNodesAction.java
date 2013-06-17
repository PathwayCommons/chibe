package org.gvt.action;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.gvt.ChisioMain;
import org.gvt.editpart.ChsRootEditPart;

import java.util.Collections;

/**
 * Action for selecting the nodes in graph.
 * Selection type can be : All nodes, simple nodes, compound nodes
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class SelectNodesAction extends Action
{
	ChisioMain main;

	int selectionType;

	/**
	 * Constructor
	 */
	public SelectNodesAction(ChisioMain main, int selectionType)
	{
		this.selectionType = selectionType;
		this.main = main;

		if (selectionType == ALL_NODES)
		{
			setText("Select All Nodes");
		}
		else if (selectionType == SIMPLE_NODES)
		{
			setText("Select Simple Nodes");
		}
		else if (selectionType == COMPOUND_NODES)
		{
			setText("Select Compound Nodes");
		}
		setToolTipText(getText());
	}

	public void run()
	{
		ScrollingGraphicalViewer viewer = main.getViewer();

		if (viewer != null)
		{
			ChsRootEditPart root = (ChsRootEditPart) viewer.getRootEditPart().getChildren().get(0);
			viewer.deselectAll();
			selectNodes(root);
		}
	}

	public void selectNodes(EditPart parent)
	{
		for (int i = 0; i < parent.getChildren().size(); i++)
		{
			EditPart node = (EditPart) parent.getChildren().get(i);

			if (node.getChildren() != Collections.EMPTY_LIST)
			{
				if (selectionType != SIMPLE_NODES)
				{
					main.getViewer().appendSelection(node);
				}

				selectNodes(node);
			}
			else
			{
				if (selectionType != COMPOUND_NODES)
				{
					main.getViewer().appendSelection(node);
				}
			}
		}
	}

	public static final int ALL_NODES = 0;
	public static final int SIMPLE_NODES = 1;
	public static final int COMPOUND_NODES = 2;
}