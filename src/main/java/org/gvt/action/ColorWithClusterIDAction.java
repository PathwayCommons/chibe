package org.gvt.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.gvt.ChisioMain;
import org.gvt.editpart.ChsRootEditPart;
import org.gvt.model.CompoundModel;
import org.gvt.model.ECluster;
import org.gvt.model.EdgeModel;
import org.gvt.model.NodeModel;

/**
 * This actions paints the nodes according to their cluster informations.
 * The nodes in the same cluster are painted with the same color. Color is
 * choosen randomly.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class ColorWithClusterIDAction extends Action
{
	ChisioMain main;

	/**
	 * Constructor
	 */
	public ColorWithClusterIDAction(ChisioMain main)
	{
		super("Color using Cluster IDs");
		this.setToolTipText("Color using Cluster IDs");
		setImageDescriptor(ImageDescriptor.createFromFile(
			ChisioMain.class,
			"icon/color-cluster.png"));

		this.main = main;
	}

	public void run()
	{
		List clusterIDs = main.getPathwayGraph().getClusterManager().
			getClusterIDs();

		for (int i = 0 ; i < clusterIDs.size(); i++)
		{
			Random rnd = new Random();
			Color c = new Color(null,
				rnd.nextInt(256),
				rnd.nextInt(256),
				rnd.nextInt(256));
			Iterator iter = this.main.getPathwayGraph().getClusterManager().
				getClusterByID((Integer) clusterIDs.get(i)).getNodes().
				iterator();

			while (iter.hasNext())
			{
				NodeModel node = (NodeModel) iter.next();
				// node color
				node.setColor(c);
				
				//set cluster color
				int clusterID = node.getClusters().get(0).getClusterID();
				
				ECluster cluster = (ECluster) node.getParentModel().
					getClusterManager().getClusterByID(clusterID);
				cluster.setHighlightColor(c);
			}
		}

		// Nodes which do not belong to any cluster, are colored with
		// default color
		CompoundModel root = main.getPathwayGraph();
		Iterator nodeIter = root.getNodes().iterator();

		while (nodeIter.hasNext())
		{
			NodeModel node = (NodeModel) nodeIter.next();

			if (node.getClusters().isEmpty())
			{
				if (node instanceof CompoundModel)
				{
					node.setColor(CompoundModel.DEFAULT_COLOR);
				}
				else
				{
					node.setColor(NodeModel.DEFAULT_COLOR);
				}
			}
		}

		// Color intra-graph edges lighter then inter-graph edges.
		for (Object edgeObject : root.getEdges())
		{
			EdgeModel edge = (EdgeModel) edgeObject;

			NodeModel src = edge.getSource();
			NodeModel trgt = edge.getTarget();

			if (src.hasCommonCluster(trgt))
			{
				edge.setColor(ColorConstants.gray);
			}
			else
			{
				edge.setColor(EdgeModel.DEFAULT_COLOR);
			}
		}
	}
}