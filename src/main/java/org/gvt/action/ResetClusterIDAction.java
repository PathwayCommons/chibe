package org.gvt.action;

import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gef.EditPart;
import org.gvt.ChisioMain;
import org.gvt.model.NodeModel;
import org.gvt.editpart.*;

/**
 * This action resets cluster id of selected nodes. reseting is done by setting
 * the cluster id to "0" value. "0" means does not belong to any cluster.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class ResetClusterIDAction extends Action
{
	ScrollingGraphicalViewer viewer;

	private ChisioMain main;

	/**
	 * Constructor
	 */
	public ResetClusterIDAction(ScrollingGraphicalViewer view)
	{
		super("Reset Clusters of Selected");
		viewer = view;
		setToolTipText("Reset Clusters of Selected");
	}

	public ResetClusterIDAction(ChisioMain main)
	{
		super("Reset Clusters of Selected");
		this.main = main;
		setToolTipText("Reset Clusters of Selected");
	}

	public void run()
	{
		if (main != null)
		{
			viewer = main.getViewer();
		}

		// Iterates the selected objects to delete
		Iterator selectedObjects =
			((IStructuredSelection) viewer.getSelection()).iterator();

		// for each of seleceted objects, delete command is executed
		while (selectedObjects.hasNext())
		{
			EditPart childEditPart = (EditPart) selectedObjects.next();

			// if selected one is a node or compound, set ClusterID to "0".
			if (childEditPart instanceof ChsNodeEditPart)
			{
				NodeModel node = (NodeModel) childEditPart.getModel();
				node.resetClusters();
			}
		}
	}
}