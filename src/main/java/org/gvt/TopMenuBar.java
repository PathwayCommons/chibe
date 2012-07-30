package org.gvt;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.gvt.action.*;

/**
 * This class implements the top menu bar.
 *
 * @author Cihan Kucukkececi
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class TopMenuBar
{
	public static ChisioMain chisio;
	
	/**
	 * Creates the bar menu for the main window
	 */
	public static MenuManager createBarMenu(ChisioMain main)
	{
		chisio = main;

		MenuManager menuBar = new MenuManager("");
		MenuManager modelMenu = new MenuManager("&Model");
		MenuManager pathwayMenu = new MenuManager("&Pathway");
		MenuManager sifMenu = new MenuManager("&SIF");
		MenuManager viewMenu = new MenuManager("&View");
		MenuManager editMenu = new MenuManager("&Edit");
		MenuManager layoutMenu = new MenuManager("&Layout");
		MenuManager dataMenu = new MenuManager("&Data");
		MenuManager queryMenu = new MenuManager("&Query");
		MenuManager helpMenu = new MenuManager("&Help");

		// MODEL
		menuBar.add(modelMenu);
		modelMenu.add(new LoadBioPaxModelAction(chisio));
        modelMenu.add(new MergeAction(chisio));
        modelMenu.add(new CloseBioPAXFileAction(chisio));
		modelMenu.add(new SaveBioPAXFileAction(chisio));
		modelMenu.add(new SaveAsBioPAXFileAction(chisio));
		modelMenu.add(new Separator());
		modelMenu.add(new ExitAction("Exit", chisio));

		// PATHWAY
		menuBar.add(pathwayMenu);
		pathwayMenu.add(new OpenPathwaysAction(chisio));
			MenuManager closePathwayMenu = new MenuManager("&Close");
			closePathwayMenu.add(new ClosePathwayAction(chisio));
			closePathwayMenu.add(new ClosePathwayAction(chisio, true));
		pathwayMenu.add(closePathwayMenu);
		pathwayMenu.add(new Separator());
		pathwayMenu.add(new RenamePathwayAction(chisio));
		pathwayMenu.add(new Separator());
			MenuManager newPathwayMenu = new MenuManager("&Create");
			newPathwayMenu.add(new DuplicatePathwayAction(chisio));
			newPathwayMenu.add(new GetNeighborhoodOfSelectedEntityAction(chisio));
			newPathwayMenu.add(new CropAction(chisio, CropAction.CropTo.HIGHLIGHTED));
			newPathwayMenu.add(new CropAction(chisio, CropAction.CropTo.SELECTED));
		pathwayMenu.add(newPathwayMenu);
		MenuManager deletePathwayMenu = new MenuManager("&Delete");
			deletePathwayMenu.add(new DeletePathwayAction(chisio));
			deletePathwayMenu.add(new DeletePathwayAction(chisio, true));
		pathwayMenu.add(deletePathwayMenu);
		pathwayMenu.add(new Separator());
		pathwayMenu.add(new UpdatePathwayAction(chisio, true));
		pathwayMenu.add(new Separator());
		pathwayMenu.add(new SaveAsImageAction(chisio, true));
		pathwayMenu.add(new SaveAsImageAction(chisio, false));
		pathwayMenu.add(new SaveAsGraphMLAction(chisio));
		pathwayMenu.add(new PrintAction(chisio));

		// SIF
		menuBar.add(sifMenu);
		sifMenu.add(new ExportToSIFAction(chisio));
		sifMenu.add(new Separator());
		sifMenu.add(new LoadSIFFileAction(chisio));
		sifMenu.add(new SaveToSIFFileAction(chisio));
		sifMenu.add(new Separator());
		sifMenu.add(new GOIofSIFAction(chisio));

		// VIEW
		menuBar.add(viewMenu);

		MenuManager selectNodesMenu = new MenuManager("&Select Nodes");
		selectNodesMenu.add(new SelectNodesAction(chisio, SelectNodesAction.ALL_NODES));
		selectNodesMenu.add(new SelectNodesAction(chisio, SelectNodesAction.SIMPLE_NODES));
		selectNodesMenu.add(new SelectNodesAction(chisio, SelectNodesAction.COMPOUND_NODES));

		MenuManager selectEdgesMenu = new MenuManager("&Select Edges");
		selectEdgesMenu.add(new SelectEdgesAction(chisio, SelectEdgesAction.ALL_EDGES));
		selectEdgesMenu.add(new SelectEdgesAction(chisio, SelectEdgesAction.INTRA_GRAPH_EDGES));
		selectEdgesMenu.add(new SelectEdgesAction(chisio, SelectEdgesAction.INTER_GRAPH_EDGES));

		MenuManager zoomMenu = new MenuManager("&Zoom");
		zoomMenu.add(new ZoomAction(chisio, -1, null));
		zoomMenu.add(new ZoomAction(chisio, 1, null));
		zoomMenu.add(new Separator());
		zoomMenu.add(new ZoomAction(chisio, 50, null));
		zoomMenu.add(new ZoomAction(chisio, 100, null));
		zoomMenu.add(new ZoomAction(chisio, 200, null));
		zoomMenu.add(new ZoomAction(chisio, 500, null));
		zoomMenu.add(new ZoomAction(chisio, 1000, null));

		viewMenu.add(new SelectionToolAction("Select Tool", chisio));
		viewMenu.add(selectNodesMenu);
		viewMenu.add(selectEdgesMenu);
		viewMenu.add(new Separator());

		viewMenu.add(new MarqueeZoomToolAction("Marquee Zoom Tool", chisio));
		viewMenu.add(zoomMenu);
		viewMenu.add(new ZoomAction(chisio, 0, null));

		// EDIT
		menuBar.add(editMenu);

		editMenu.add(new HighlightByNameAction(chisio));
		editMenu.add(new HighlightSelectedAction(chisio));
		editMenu.add(new HighlightPathsBetweenSelectedAction(chisio));
		editMenu.add(new RemoveHighlightFromSelectedAction(chisio));
		editMenu.add(new RemoveHighlightsAction(chisio));
		editMenu.add(new Separator());
		editMenu.add(new DeleteAction(chisio));
		editMenu.add(new RemoveCompoundAction(chisio));
		editMenu.add(new Separator());
		editMenu.add(new InspectorAction(chisio, false));

		// LAYOUT
		menuBar.add(layoutMenu);
		layoutMenu.add(new CoSELayoutAction(chisio));
		layoutMenu.add(new SpringLayoutAction(chisio));
		layoutMenu.add(new Separator());
		layoutMenu.add(new LayoutInspectorAction(chisio));

		// DATA
		menuBar.add(dataMenu);
		dataMenu.add(new FetchFromGEOAction(chisio));
        dataMenu.add(new FetchFromCBioPortalAction(chisio));
		dataMenu.add(new LoadRawExpDataAction(chisio));
		dataMenu.add(new LoadExperimentDataAction(chisio));
		dataMenu.add(new Separator());
		dataMenu.add(new DataManagementAction(chisio));
		dataMenu.add(new Separator());
		dataMenu.add(new ColorWithExperimentAction(chisio));
		dataMenu.add(new RemoveExperimentColorAction(chisio));
		dataMenu.add(new Separator());
		dataMenu.add(new SearchCausativePathsAction(chisio, false));
		dataMenu.add(new SearchCausativePathsAction(chisio, true));
        dataMenu.add(new HighlightWithDataValuesAction(chisio));

		// QUERY

		menuBar.add(queryMenu);

		MenuManager localQueryMenu = new MenuManager("&Local");
		queryMenu.add(localQueryMenu);
		localQueryMenu.add(new LocalNeighborhoodQueryAction(chisio, false));
		localQueryMenu.add(new LocalGoIQueryAction(chisio, false));
		localQueryMenu.add(new LocalPoIQueryAction(chisio));
		localQueryMenu.add(new LocalCommonStreamQueryAction(chisio, false));
		localQueryMenu.add(new LocalCompartmentQueryAction(chisio));
		localQueryMenu.add(new LocalPathIterationQueryAction(chisio));


		// Query old pathway commons
		MenuManager pcOldMenu = new MenuManager("&Pathway Commons (Level 2)");
		queryMenu.add(pcOldMenu);
		pcOldMenu.add(new QueryNeighborsAction(chisio, false));
		pcOldMenu.add(new QueryPathwaysAction(chisio, false));

		MenuManager pcNewMenu = new MenuManager("&Pathway Commons (Level 3)");

		// Query new pathway commons
		queryMenu.add(pcNewMenu);
		pcNewMenu.add(new QueryPCNeighborsAction(chisio, false));
		pcNewMenu.add(new QueryPCPathsBetweenAction(chisio, false));
		pcNewMenu.add(new QueryPCPathsFromToAction(chisio));
		pcNewMenu.add(new QueryPCCommonStreamAction(chisio));
		pcNewMenu.add(new Separator());
		pcNewMenu.add(new QueryPCPathwaysAction(chisio));
		pcNewMenu.add(new QueryPCGetAction(chisio, false));

		// HELP
		menuBar.add(helpMenu);
		helpMenu.add(new HowToUseAction(chisio));
        helpMenu.add(new LegendAction(chisio));
		helpMenu.add(new AboutAction(chisio));

		return menuBar;
	}
}