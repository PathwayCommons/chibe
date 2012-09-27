package org.gvt;

import org.biopax.paxtools.causality.data.CBioPortalAccessor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.*;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.gvt.action.*;
import org.gvt.editpart.ChsEdgeEditPart;
import org.gvt.model.biopaxl3.Actor;
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
			manager.add(new ClosePathwayAction(main));
			manager.add(new DuplicatePathwayAction(main));
			manager.add(new DeletePathwayAction(main));
			manager.add(new UpdatePathwayAction(main, false));
			manager.add(new Separator());
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

            if( model instanceof Actor
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
