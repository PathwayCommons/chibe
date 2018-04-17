package org.gvt;

import org.biopax.paxtools.pattern.miner.SIFEnum;
import org.cbio.causality.data.portal.CBioPortalAccessor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.*;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.gvt.action.*;
import org.gvt.editpart.ChsEdgeEditPart;
import org.gvt.editpart.ChsRootEditPart;
import org.gvt.model.NodeModel;
import org.gvt.model.basicsif.BasicSIFEdge;
import org.gvt.model.basicsif.BasicSIFGraph;
import org.gvt.model.basicsif.BasicSIFGroup;
import org.gvt.model.basicsif.BasicSIFNode;
import org.gvt.model.biopaxl3.Actor;
import org.gvt.model.sifl3.SIFEdge;
import org.gvt.model.sifl3.SIFGroup;
import org.gvt.model.sifl3.SIFNode;
import org.patika.mada.util.ExperimentData;

/**
 * This class maintains Popup Menus creation.
 * 
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class PopupManager extends MenuManager
{
	// coordinates of the point that mouse was clicked
	private Point clickLocation;

	private ChisioMain main;

	/**
	 * Constructor
	 * 
	 * @param main
	 */
	public PopupManager(ChisioMain main)
	{
		this.main = main;
	}

	/**
	 * Creates the popup menus according to clicked object
	 * 
	 * @param manager
	 */
	public void createActions(IMenuManager manager)
	{
		EditPart ep = main.getViewer().findObjectAt(clickLocation);

		if (ep instanceof RootEditPart)
		{
			// GRAPH POPUP

			boolean sif = false;
			boolean tcgasif = false;
			boolean basicsif = false;
			Object o = ((ChsRootEditPart) ep.getChildren().get(0)).getModel();
			if (o instanceof BasicSIFGraph || o instanceof org.gvt.model.sifl3.SIFGraph)
			{
				sif = true;
				basicsif = o instanceof BasicSIFGraph;

				tcgasif = TCGASIFAction.okToRun(main, false);
			}

			if (tcgasif)
			{
				manager.add(new ShowMutexGroupsAction(main));
				manager.add(new HighlightTCGACaseAction(main));
				manager.add(new UpdateTCGASIFForACaseAction(main));
				manager.add(new LoadTCGASpecificReactionsAction(main));
				manager.add(new Separator());
			}

			if (basicsif)
			{
				manager.add(new ShowFormatSeriesAction(main));
				manager.add(new Separator());
			}

			if (sif)
			{
				manager.add(new SaveSIFLayoutAction(main));
				manager.add(new LoadSIFLayoutAction(main));
				manager.add(new Separator());
				manager.add(new ShowDruggableAction(main));
				manager.add(new HighlightCancerGenesActions(main));
				manager.add(new HighlightSIFDifferenceAction(main));
				manager.add(new ShowSIFStatisticsAction(main));
				manager.add(new Separator());
			}

			ClosePathwayAction action = new ClosePathwayAction(main);
			if (sif) action.setText("Close Graph");
			manager.add(action);

			if (!sif)
			{
				manager.add(new DuplicatePathwayAction(main));
				manager.add(new DeletePathwayAction(main));
				manager.add(new UpdatePathwayAction(main, false));
			}
			manager.add(new Separator());

			if (ChisioMain.cBioPortalAccessor != null)
			{
				manager.add(new FetchFromCBioPortalAction(main,
					FetchFromCBioPortalAction.CURRENT_STUDY));
				manager.add(new Separator());
			}

			manager.add(new ZoomAction(main, 1, null));//clickLocation));
			manager.add(new ZoomAction(main, -1, null));//clickLocation));
			manager.add(new Separator());
			manager.add(new RemoveHighlightsAction(main));

			if (sif) manager.add(new RemoveColorsAction(main));

			manager.add(new InspectorAction(main, true));
			manager.add(new Separator());
			manager.add(new LayoutInspectorAction(main));
			main.getViewer().select(ep);
		}
		else if (ep instanceof NodeEditPart)
		{
			// NODE-COMPOUND POPUP

			Object o = ep.getModel();
			if ((o instanceof BasicSIFGroup && !((BasicSIFGroup) o).getMediators().isEmpty()) ||
				(o instanceof SIFGroup && !((SIFGroup) o).getMediators().isEmpty()))
			{
				QueryPCGetAction query = new QueryPCGetAction(main, true, QueryPCAction.QueryLocation.PC_MECH);
				query.setText("Detailed View");
				manager.add(query);
				manager.add(new Separator());
			}

			ExperimentData data = DataLegendAction.getData((NodeModel) o, main);
			if (data != null)
				manager.add(new DataLegendAction(main, data));

			manager.add(new HighlightSelectedAction(main));
			manager.add(new RemoveHighlightFromSelectedAction(main));
			manager.add(new DeleteAction(main));
			manager.add(new InspectorAction(main, false));
			manager.add(new Separator());

			MenuManager localQueryMenu = new MenuManager("&Local Query");
			localQueryMenu.add(new LocalNeighborhoodQueryAction(main, true));
			localQueryMenu.add(new LocalGoIQueryAction(main, true));
			localQueryMenu.add(new LocalCommonStreamQueryAction(main, true));
			manager.add(localQueryMenu);

			MenuManager pcQueryMenu = new MenuManager("&Pathway Commons Query (Level 3)");
			MenuManager neighMenu = new MenuManager("&Neighborhood");
			neighMenu.add(new QueryPCNeighborsAction(main, false, true, QueryPCAction.QueryLocation.PC_MECH));
			neighMenu.add(new QueryPCNeighborsAction(main, true, false, QueryPCAction.QueryLocation.PC_MECH));
			neighMenu.add(new QueryPCNeighborsAction(main, true, true, QueryPCAction.QueryLocation.PC_MECH));
			pcQueryMenu.add(neighMenu);
			pcQueryMenu.add(new QueryPCPathsBetweenAction(main, true, QueryPCAction.QueryLocation.PC_MECH));
			MenuManager commStreamMenu = new MenuManager("&Common Stream");
			commStreamMenu.add(new QueryPCCommonStreamAction(main, true, QueryPCAction.QueryLocation.PC_MECH));
			commStreamMenu.add(new QueryPCCommonStreamAction(main, false, QueryPCAction.QueryLocation.PC_MECH));
			pcQueryMenu.add(commStreamMenu);
            manager.add(pcQueryMenu);

            NodeEditPart nep = (NodeEditPart) ep;
            Object model = nep.getModel();
            CBioPortalAccessor portalAccessor = ChisioMain.cBioPortalAccessor;

            if( (model instanceof Actor || model instanceof BasicSIFNode || model instanceof SIFNode)
                    && main.hasExperimentData(ExperimentData.CBIOPORTAL_ALTERATION_DATA)
                    && portalAccessor != null
                    && !portalAccessor.getCurrentGeneticProfiles().isEmpty() )
            {
                manager.add(new Separator());
                manager.add(new CBioPortalDataStatisticsAction(main));
            }
        }
		else if (ep instanceof ChsEdgeEditPart)
		{
			// EDGE POPUP

			Object o = ep.getModel();
			if (o instanceof BasicSIFEdge || o instanceof SIFEdge)
			{
				String tag = o instanceof BasicSIFEdge ? ((BasicSIFEdge) o).getTag() : ((SIFEdge) o).getTag();

				if (tag.equals(SIFEnum.IN_COMPLEX_WITH.getTag()))
				{
					QueryPCNeighborsAction query = new QueryPCNeighborsAction(main, true, true,
						QueryPCAction.QueryLocation.PC_MECH);
					query.setText("Detailed View");
					query.setIgnoreIDsOfSelectedNodes(true);
					manager.add(query);
				}
				else
				{
					QueryPCGetAction query = new QueryPCGetAction(main, true, QueryPCAction.QueryLocation.PC_MECH);
					query.setText("Detailed View");
					query.setIgnoreIDsOfSelectedNodes(true);
					manager.add(query);

					query = new QueryPCGetAction(main, true, QueryPCAction.QueryLocation.FILE_MECH);
					query.setLocalFilename("/home/babur/Documents/Analyses/CPTACBreastCancer/BigMech/REACH/model.owl");
//					query.setLocalFilename("/home/babur/Documents/PC/PathwayCommons.8.Detailed.BIOPAX.owl");
					query.setText("Detailed View On Temp Model");
					manager.add(query);
				}

				manager.add(new Separator());
			}

			manager.add(new HighlightSelectedAction(main));
			manager.add(new RemoveHighlightFromSelectedAction(main));
			manager.add(new DeleteAction(main));
			manager.add(new InspectorAction(main, false));
		}
	}

	/**
	 * Setter method
	 * 
	 * @param clickLocation
	 */
	public void setClickLocation(Point clickLocation)
	{
		this.clickLocation = clickLocation;
	}
}
