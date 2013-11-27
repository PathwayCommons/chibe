package org.gvt.action;

import java.util.*;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gef.EditPart;
import org.gvt.ChisioMain;
import org.gvt.model.NodeModel;
import org.gvt.editpart.*;
import org.ivis.layout.Cluster;

/**
 * This action assigns a new cluster id to selected nodes. new id is choosen
 * by searching the available minimum int value.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class AssignNewClusterIDAction extends Action
{
	ScrollingGraphicalViewer viewer;

	private ChisioMain main;

	/**
	 * Constructor
	 */
	public AssignNewClusterIDAction(ScrollingGraphicalViewer view)
	{
		super("Assign Selected to New Cluster");
		viewer = view;
	}

	public AssignNewClusterIDAction(ChisioMain main)
	{
		super("Assign Selected to New Cluster");
		this.main = main;
		setToolTipText("Assign Selected to New Cluster");
	}

	public void run()
	{
		if(main != null)
		{
			viewer = main.getViewer();
		}
		// Iterates the selected objects to delete
		Iterator selectedObjects = ((IStructuredSelection) viewer.
			getSelection()).iterator();
		
		List clusterIDs = main.getPathwayGraph().getClusterManager().getClusterIDs();
		
		int newID = 1;
		for(int i= 0 ; i < clusterIDs.size(); i++)
		{
			if((Integer)clusterIDs.get(i) > newID)
			{
				break;
			}
			newID++;
		}
		// for each of seleceted objects, delete command is executed
		while (selectedObjects.hasNext())
		{
			EditPart childEditPart = (EditPart) selectedObjects.next();

			// if selected one is a node or compound, assign new cluster id
			if (childEditPart instanceof ChsNodeEditPart)
			{
				NodeModel node = (NodeModel) childEditPart.getModel();
				
				// reset previous clusters
				node.resetClusters();
				// add new cluster
				node.addCluster(newID);
				
				Cluster cluster = this.main.getPathwayGraph().getClusterManager().
					getClusterByID(newID);
				
				cluster.calculatePolygon();
				
				if (this.main.isClusterBoundShown)
				{
					this.main.getHighlightLayer().addHighlightToCluster(cluster);
				}
			}
		}
	}
}