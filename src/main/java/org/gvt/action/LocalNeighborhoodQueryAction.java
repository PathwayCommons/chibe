package org.gvt.action;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.biopax.paxtools.model.Model;
import org.eclipse.jface.dialogs.MessageDialog;
import org.gvt.ChisioMain;
import org.gvt.gui.NeighborhoodQueryParamDialog;
import org.gvt.gui.NeighborhoodQueryParamWithEntitiesDialog;
import org.gvt.util.EntityHolder;
import org.gvt.util.QueryOptionsPack;
import org.patika.mada.algorithm.LocalNeighborhoodQuery;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;

/**
 * This class creates the action for opening layout properties window.
 *
 * @author Ozgun Babur
 * @author Merve Cakir
 * @author Shatlyk Ashyralyev
 * 
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class LocalNeighborhoodQueryAction extends AbstractLocalQueryAction
{
	/**
	 * Dialog options are stored, in order to use next time dialog is opened.
	 */
	QueryOptionsPack options;

	/**
	 * To determine whether action is called from
	 * topMenuBar(false) or popUpManager(true)
	 */
	private boolean useSelection;

	/**
	 * Constructor
	 */
	public LocalNeighborhoodQueryAction(ChisioMain main, boolean useSelection)
	{
		super(main, "Neighborhood ...");
		setToolTipText(getText());
		options = new QueryOptionsPack();
		this.main = main;
		this.useSelection = useSelection;	
	}

	public void run()
	{	
		LocalNeighborhood();
	}


	public void LocalNeighborhood()
	{
		Model owlModel = this.main.getOwlModel();

		if (owlModel == null)
		{
			MessageDialog.openError(main.getShell(), "Error!",
				"Load or query a BioPAX model first!");

			return;
		}

		Set<Node> sourceNodes = new HashSet<Node>();

		//if action is called from PopupMenu
		if (useSelection)
		{
			if (main.getPathwayGraph() == null)
			{
				MessageDialog.openError(main.getShell(), "Error!",
					"This feature works only for BioPAX graphs");
			}

			//open dialog
			NeighborhoodQueryParamDialog dialog =
				new NeighborhoodQueryParamDialog(this.main);
			options = dialog.open(options);

			//Return if Cancel was pressed
			if ( !options.isCancel() )
			{	
				options.setCancel(true);
			}
			else
			{
				return;
			}
			
			//Get Selected Nodes in graph
			sourceNodes = getSelectedNodes();
		}
		//if action is called from TopMenuBar
		else
		{
			//open dialog
			NeighborhoodQueryParamWithEntitiesDialog dialog =
				new NeighborhoodQueryParamWithEntitiesDialog(main, main.getAllEntities());

			options = dialog.open(options);

			if ( !options.isCancel() )
			{	
				options.setCancel(true);
			}
			else
			{
				return;
			}
 
			//if cancel is not pressed, begin running algorithm
			
			List<EntityHolder> addedEntities = dialog.getAddedSourceEntities();

			Set<Node> sourceSet = main.getRootGraph().getRelatedStates(addedEntities);

			main.getRootGraph().replaceComplexMembersWithComplexes(sourceSet);

			for (GraphObject go : sourceSet)
			{
				if (go instanceof Node)
				{
					sourceNodes.add((Node) go);
				}
			}
		}

		//Neighborhood Query
		LocalNeighborhoodQuery neighborhood =
			new LocalNeighborhoodQuery(sourceNodes,
				options.isUpstream(),
				options.isDownstream(),
				options.getLengthLimit());
		
		//run query
		Set<GraphObject> result = neighborhood.run();
		
        //View result of query and Highlight it
        viewAndHighlightResult(result,
        	options.isCurrentView(),
        	"Neighborhood");
	}
}
