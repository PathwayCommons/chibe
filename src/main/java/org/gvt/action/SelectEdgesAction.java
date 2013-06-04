package org.gvt.action;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.gvt.ChisioMain;
import org.gvt.editpart.ChsEdgeEditPart;
import org.gvt.editpart.ChsNodeEditPart;
import org.gvt.editpart.ChsRootEditPart;

import java.util.Collections;

/**
 * Action for selecting the edges in graph.
 * Selection type can be : All edges, intra-edges, inter-edges
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class SelectEdgesAction extends Action
{
	ChisioMain main;

	int selectionType;

	/**
	 * Constructor
	 */
	public SelectEdgesAction(ChisioMain main, int selectionType)
	{
		this.selectionType = selectionType;
		this.main = main;

		if (selectionType == SelectEdgesAction.ALL_EDGES)
		{
			setText("Select All Edges");
		}
		else if (selectionType == SelectEdgesAction.INTRA_GRAPH_EDGES)
		{
			setText("Select Intra-Graph Edges");
		}
		else if (selectionType == SelectEdgesAction.INTER_GRAPH_EDGES)
		{
			setText("Select Inter-Graph Edges");
		}
		setToolTipText(getText());
	}

	public void run()
	{

		ScrollingGraphicalViewer viewer = main.getViewer();
		
		if (viewer != null)
		{
			ChsRootEditPart root = (ChsRootEditPart) viewer.getRootEditPart().getChildren().get(0);
			main.getViewer().deselectAll();
			selectEdges(root);
		}
	}

	public void selectEdges(EditPart parent)
	{
		for (int i = 0; i < parent.getChildren().size(); i++)
		{
			ChsNodeEditPart node = (ChsNodeEditPart) parent.getChildren().get(i);

			for (int s = 0; s < node.getSourceConnections().size(); s++)
			{
				ChsEdgeEditPart edge =
					(ChsEdgeEditPart) node.getSourceConnections().get(s);

				if (edge.getEdgeModel().isIntragraph())
				{
					if (selectionType != SelectEdgesAction.INTER_GRAPH_EDGES)
					{
						main.getViewer().appendSelection(edge);
					}
				}
				else
				{
					if (selectionType != SelectEdgesAction.INTRA_GRAPH_EDGES)
					{
						main.getViewer().appendSelection(edge);
					}
				}
			}

			for (int s = 0; s < node.getTargetConnections().size(); s++)
			{
				ChsEdgeEditPart edge =
					(ChsEdgeEditPart) node.getTargetConnections().get(s);

				if (edge.getEdgeModel().isIntragraph())
				{
					if (selectionType != SelectEdgesAction.INTER_GRAPH_EDGES)
					{
						main.getViewer().appendSelection(edge);
					}
				}
				else
				{
					if (selectionType != SelectEdgesAction.INTRA_GRAPH_EDGES)
					{
						main.getViewer().appendSelection(edge);
					}
				}
			}

			if (node.getChildren() != Collections.EMPTY_LIST)
			{
				selectEdges(node);
			}
		}
	}

	public static final int ALL_EDGES = 0;
	public static final int INTRA_GRAPH_EDGES = 1;
	public static final int INTER_GRAPH_EDGES = 2;
}