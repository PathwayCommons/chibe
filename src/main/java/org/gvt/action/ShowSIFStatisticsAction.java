package org.gvt.action;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.editpart.ChsRootEditPart;
import org.gvt.model.*;
import org.gvt.model.basicsif.BasicSIFGraph;
import org.gvt.model.sifl3.SIFGraph;

import java.util.*;

/**
 * Action for displaying SIF graph statistics, such as number of genes, relations, average degree, etc.
 *
 * @author Ozgun Babur
 */
public class ShowSIFStatisticsAction extends Action
{
	ChisioMain main;

	/**
	 * Constructor
	 */
	public ShowSIFStatisticsAction(ChisioMain main)
	{
		super("Show Graph Statistics ...");
		setToolTipText(getText());
		this.main = main;
	}

	public void run()
	{
		ScrollingGraphicalViewer viewer = main.getViewer();

		if (viewer == null) return;

		BioPAXGraph graph = main.getPathwayGraph();

		if (!(graph instanceof BasicSIFGraph || graph instanceof SIFGraph))
		{
			return;
		}

		// Find number of relations

		int relNum = 0;

		for (Object o : graph.getEdges())
		{
			EdgeModel edge = (EdgeModel) o;
			int sourceNum = edge.getSource() instanceof CompoundModel ?
				((CompoundModel) edge.getSource()).getChildren().size() : 1;
			int targetNum = edge.getTarget() instanceof CompoundModel ?
				((CompoundModel) edge.getTarget()).getChildren().size() : 1;

			relNum += sourceNum * targetNum;
		}

		for (Object o : graph.getNodes())
		{
			if (o instanceof CompoundModel)
			{
				CompoundModel com = (CompoundModel) o;
				if (com.getText() != null && com.getText().length() > 0)
				{
					int c = com.getChildren().size();
					relNum += (c * (c - 1)) / 2;
				}
			}
		}

		// Find number of genes

		Set<NodeModel> all = new HashSet<NodeModel>();
		Set<NodeModel> source = new HashSet<NodeModel>();
		Set<NodeModel> target = new HashSet<NodeModel>();

		for (Object o : graph.getNodes())
		{
			NodeModel node = (NodeModel) o;

			Set<NodeModel> set = new HashSet<NodeModel>();

			if (node instanceof CompoundModel) set.addAll(((CompoundModel) node).getChildren());
			else set.add(node);

			all.addAll(set);

			if (hasDirectedEdges(node.getSourceConnections())) source.addAll(set);
			if (hasDirectedEdges(node.getTargetConnections())) target.addAll(set);
		}

		MessageDialog.openInformation(main.getShell(),
			"Graph Statistics", "\nGenes = " + all.size() +
				"\nSource genes = " + source.size() +
				"\nTarget genes = " + target.size() +
				"\nRelations = " + relNum + "\n");
	}

	private boolean hasDirectedEdges(List list)
	{
		for (Object o : list)
		{
			if (o instanceof EdgeModel)
			{
				EdgeModel edge = (EdgeModel) o;
				String arrow = edge.getArrow();
				if (arrow != null && !arrow.equals("None")) return true;
			}
		}
		return false;
	}
}