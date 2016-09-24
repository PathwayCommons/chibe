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
		modelMenu.add(new LoadBioPaxModelAction(chisio, true));
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
		pathwayMenu.add(new SaveAsSBGNMLAction(chisio, true));
		pathwayMenu.add(new PrintAction(chisio));

		// SIF
		menuBar.add(sifMenu);
		sifMenu.add(new ExportToSIFAction(chisio, true));
		sifMenu.add(new ExportToSIFAction(chisio, false));
		sifMenu.add(new Separator());
		sifMenu.add(new LoadSIFFileAction(chisio));
		sifMenu.add(new SaveToSIFFileAction(chisio));
		sifMenu.add(new Separator());
		sifMenu.add(new GOIofSIFAction(chisio));
		sifMenu.add(new Separator());
		MenuManager expMenu = new MenuManager("&Experimental");
		expMenu.add(new FindEnrichedInPCSifAction(chisio));
		expMenu.add(new LoadTCGASpecificSIFAction(chisio));
		expMenu.add(new LoadRPPAAction(chisio));
		sifMenu.add(expMenu);

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
		editMenu.add(new HighlightUsingFileAction(chisio));
		editMenu.add(new HighlightSelectedAction(chisio));
		editMenu.add(new SelectHighlightedAction(chisio));
		editMenu.add(new HighlightPathsBetweenSelectedAction(chisio));
		editMenu.add(new RemoveHighlightFromSelectedAction(chisio));
		editMenu.add(new RemoveHighlightsAction(chisio));
		editMenu.add(new Separator());
		editMenu.add(new DeleteAction(chisio));
		editMenu.add(new RemoveCompoundAction(chisio));
		editMenu.add(new HideCompartmentsAction(chisio));
		editMenu.add(new ShowCompartmentsAction(chisio));
		editMenu.add(new Separator());
		editMenu.add(new InspectorAction(chisio, false));

		// LAYOUT
		menuBar.add(layoutMenu);
		layoutMenu.add(new CoSELayoutAction(chisio));
		layoutMenu.add(new SpringLayoutAction(chisio));
		layoutMenu.add(new Separator());
		layoutMenu.add(new LayoutInspectorAction(chisio));
		layoutMenu.add(new Separator());
		MenuManager alignMenu = new MenuManager("&Align");
		layoutMenu.add(alignMenu);
		alignMenu.add(new AlignSelectedAction(chisio, AlignSelectedAction.Direction.TOP));
		alignMenu.add(new AlignSelectedAction(chisio, AlignSelectedAction.Direction.BOTTOM));
		alignMenu.add(new AlignSelectedAction(chisio, AlignSelectedAction.Direction.LEFT));
		alignMenu.add(new AlignSelectedAction(chisio, AlignSelectedAction.Direction.RIGHT));
		alignMenu.add(new AlignSelectedAction(chisio, AlignSelectedAction.Direction.X_CENTER));
		alignMenu.add(new AlignSelectedAction(chisio, AlignSelectedAction.Direction.Y_CENTER));

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
        dataMenu.add(new Separator());
        dataMenu.add(new ConnectToDAVIDAction(chisio));                

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

		// Query new pathway commons
		MenuManager pcNewMenu = new MenuManager("&Pathway Commons (in BioPAX)");
		queryMenu.add(pcNewMenu);
		pcNewMenu.add(new QueryPCNeighborsAction(chisio, QueryPCAction.QueryLocation.PC_MECH));
		pcNewMenu.add(new QueryPCPathsBetweenAction(chisio, false, QueryPCAction.QueryLocation.PC_MECH));
		pcNewMenu.add(new QueryPCPathsFromToAction(chisio, QueryPCAction.QueryLocation.PC_MECH));
		pcNewMenu.add(new QueryPCCommonStreamAction(chisio, QueryPCAction.QueryLocation.PC_MECH));
		pcNewMenu.add(new Separator());
		pcNewMenu.add(new QueryPCPathwaysAction(chisio, QueryPCAction.QueryLocation.PC_MECH));
		pcNewMenu.add(new QueryPCGetAction(chisio, false, QueryPCAction.QueryLocation.PC_MECH));
		pcNewMenu.add(new EnrichedPathwaysAction(main));
		pcNewMenu.add(new EnrichedReactionsAction(main));

		// Query new pathway commons
		MenuManager biopaxFileMenu = new MenuManager("&BioPAX File");
		queryMenu.add(biopaxFileMenu);
		biopaxFileMenu.add(new QueryPCNeighborsAction(chisio, QueryPCAction.QueryLocation.FILE_MECH));
		biopaxFileMenu.add(new QueryPCPathsBetweenAction(chisio, false, QueryPCAction.QueryLocation.FILE_MECH));
		biopaxFileMenu.add(new QueryPCPathsFromToAction(chisio, QueryPCAction.QueryLocation.FILE_MECH));
		biopaxFileMenu.add(new QueryPCCommonStreamAction(chisio, QueryPCAction.QueryLocation.FILE_MECH));
		biopaxFileMenu.add(new Separator());
		biopaxFileMenu.add(new QueryPCPathwaysAction(chisio, QueryPCAction.QueryLocation.FILE_MECH));
		biopaxFileMenu.add(new QueryPCGetAction(chisio, false, QueryPCAction.QueryLocation.FILE_MECH));
		biopaxFileMenu.add(new EnrichedReactionsAction(main));

		// Query new pathway commons in SIF
		MenuManager pcNewSIFMenu = new MenuManager("&Pathway Commons (in SIF)");
		queryMenu.add(pcNewSIFMenu);
		pcNewSIFMenu.add(new QueryPCNeighborsAction(chisio, QueryPCAction.QueryLocation.PC_SIF));
		pcNewSIFMenu.add(new QueryPCPathsBetweenAction(chisio, false, QueryPCAction.QueryLocation.PC_SIF));
		pcNewSIFMenu.add(new QueryPCPathsBetweenWithLinkersAction(chisio, false, QueryPCAction.QueryLocation.PC_SIF));
		pcNewSIFMenu.add(new QueryPCPathsBetweenWithMinimalLinkersAction(chisio, false, QueryPCAction.QueryLocation.PC_SIF));
		pcNewSIFMenu.add(new QueryPCPathsFromToAction(chisio, QueryPCAction.QueryLocation.PC_SIF));
		pcNewSIFMenu.add(new QueryPCCommonStreamAction(chisio, QueryPCAction.QueryLocation.PC_SIF));

		// Query new pathway commons in SIF
		MenuManager sifFileMenu = new MenuManager("&SIF File");
		queryMenu.add(sifFileMenu);
		sifFileMenu.add(new QueryPCNeighborsAction(chisio, QueryPCAction.QueryLocation.FILE_SIF));
		sifFileMenu.add(new QueryPCPathsBetweenAction(chisio, false, QueryPCAction.QueryLocation.FILE_SIF));
		sifFileMenu.add(new QueryPCPathsFromToAction(chisio, QueryPCAction.QueryLocation.FILE_SIF));
		sifFileMenu.add(new QueryPCCommonStreamAction(chisio, QueryPCAction.QueryLocation.FILE_SIF));

		// HELP
		menuBar.add(helpMenu);
		helpMenu.add(new HowToUseAction(chisio));
        helpMenu.add(new LegendAction(chisio));
		helpMenu.add(new AboutAction(chisio));

		return menuBar;
	}
}