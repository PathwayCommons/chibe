package org.gvt;

import org.cbio.causality.data.portal.CBioPortalAccessor;
import org.cbio.causality.idmapping.HGNC;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.*;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.gvt.action.*;
import org.gvt.editpart.ChsEdgeEditPart;
import org.gvt.editpart.ChsRootEditPart;
import org.gvt.model.EdgeModel;
import org.gvt.model.basicsif.BasicSIFEdge;
import org.gvt.model.basicsif.BasicSIFGraph;
import org.gvt.model.basicsif.BasicSIFNode;
import org.gvt.model.biopaxl3.Actor;
import org.gvt.model.sifl3.SIFEdge;
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
			Object o = ((ChsRootEditPart) ep.getChildren().get(0)).getModel();
			if (o instanceof BasicSIFGraph || o instanceof org.gvt.model.sifl2.SIFGraph ||
				o instanceof org.gvt.model.sifl3.SIFGraph)
			{
				sif = true;

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
			manager.add(new InspectorAction(main, true));
			manager.add(new Separator());
			manager.add(new LayoutInspectorAction(main));
			main.getViewer().select(ep);
		}
		else if (ep instanceof NodeEditPart)
		{
			// NODE-COMPOUND POPUP
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
			neighMenu.add(new QueryPCNeighborsAction(main, false, true));
			neighMenu.add(new QueryPCNeighborsAction(main, true, false));
			neighMenu.add(new QueryPCNeighborsAction(main, true, true));
			pcQueryMenu.add(neighMenu);
			pcQueryMenu.add(new QueryPCPathsBetweenAction(main, true));
			MenuManager commStreamMenu = new MenuManager("&Common Stream");
			commStreamMenu.add(new QueryPCCommonStreamAction(main, true));
			commStreamMenu.add(new QueryPCCommonStreamAction(main, false));
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

				EdgeModel model = (EdgeModel) o;
				String arrow = model.getArrow();
				boolean directed = !arrow.equals("None");
				String source = HGNC.getSymbol(model.getSource().getText());
				String target = HGNC.getSymbol(model.getTarget().getText());

				if (source != null && target != null)
				{
					if (directed)
					{
						QueryPCPathsFromToAction action = new QueryPCPathsFromToAction(main, source, target);
						action.setIncreaseLimitIfNoResult(true);
						manager.add(action);
					}
					else manager.add(new QueryPCPathsBetweenAction(main, source, target));
				}
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
