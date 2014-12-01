package org.gvt.action;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.editpart.ChsRootEditPart;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.EdgeModel;
import org.gvt.model.GraphObject;
import org.gvt.model.NodeModel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Action for selecting the highlighted objects in the graph.
 *
 * @author Ozgun Babur
 */
public class SelectHighlightedAction extends Action
{
	ChisioMain main;

	/**
	 * Constructor
	 */
	public SelectHighlightedAction(ChisioMain main)
	{
		super("Select Highlighted");
		setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class, "icon/highlight.png"));
		setToolTipText(getText());
		this.main = main;
	}

	public void run()
	{
		ScrollingGraphicalViewer viewer = main.getViewer();

		if (viewer == null) return;

		BioPAXGraph graph = main.getPathwayGraph();

		if (graph == null) return;

		Set<GraphObject> select = new HashSet<GraphObject>();

		for (Object o : graph.getNodes())
		{
			NodeModel node = (NodeModel) o;
			if (node.isHighlight())
			{
				select.add(node);
			}
		}

		for (Object o : graph.getEdges())
		{
			EdgeModel edge = (EdgeModel) o;
			if (edge.isHighlight())
			{
				select.add(edge);
			}
		}

		ChsRootEditPart root = (ChsRootEditPart) viewer.getRootEditPart().getChildren().get(0);
		selectNodes(root, select);
	}

	public void selectNodes(EditPart parent, Set<GraphObject> select)
	{
		for (int i = 0; i < parent.getChildren().size(); i++)
		{
			EditPart child = (EditPart) parent.getChildren().get(i);

			if (select.contains(child.getModel()))
			{
				main.getViewer().appendSelection(child);
			}

			if (child.getChildren() != Collections.EMPTY_LIST)
			{
				selectNodes(child, select);
			}
		}
	}
}