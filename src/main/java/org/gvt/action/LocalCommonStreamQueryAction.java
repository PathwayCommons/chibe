package org.gvt.action;

import java.util.*;

import org.biopax.paxtools.model.Model;
import org.eclipse.jface.dialogs.MessageDialog;
import org.gvt.ChisioMain;
import org.gvt.gui.CommonStreamQueryParamDialog;
import org.gvt.gui.CommonStreamQueryParamWithEntitiesDialog;
import org.gvt.util.CommonStreamOptionsPack;
import org.gvt.util.EntityHolder;
import org.patika.mada.algorithm.LocalCommonStreamQuery;
import org.patika.mada.algorithm.LocalPoIQuery;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;

/**
 * This class creates the action for opening layout properties window.
 *
 * @author Shatlyk Ashyralyev
 * 
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class LocalCommonStreamQueryAction extends AbstractLocalQueryAction
{
	/**
	 * Dialog options are stored, in order to use next time dialog is opened.
	 */
	CommonStreamOptionsPack options;

	/**
	 * To determine whether action is called from
	 * topMenuBar(false) or popUpManager(true)
	 */
	private boolean useSelection;

	/**
	 * Constructor
	 */
	public LocalCommonStreamQueryAction(ChisioMain main, boolean useSelection)
	{
		super(main, "Common Stream Query");
		setToolTipText(getText());
		options = new CommonStreamOptionsPack();
		this.main = main;
		this.useSelection = useSelection;	
	}

	public void run()
	{	
		CommonStreamQuery();
	}

	public void CommonStreamQuery()
	{
		Model owlModel = this.main.getOwlModel();

		if (owlModel == null)
		{
			MessageDialog.openError(main.getShell(), "Error!",
				"Load or query a BioPAX model first!");

			return;
		}
		
		//remove previous highlights
		RemoveHighlightsAction rha = new RemoveHighlightsAction(this.main);
		rha.run();
		
		//result of Stream Query
		Set<Node> streamResult = new HashSet<Node>();
		
		Set<Node> sourceNodesSet = new HashSet<Node>();
		
		//Common Stream Query
		LocalCommonStreamQuery stream;
		
		//if action is called from PopupMenu
		if (useSelection)
		{
			//open dialog
			CommonStreamQueryParamDialog dialog = 
				new CommonStreamQueryParamDialog(this.main);
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
			sourceNodesSet = getSelectedNodes();
			
			//Common Stream Query
			stream = new LocalCommonStreamQuery(sourceNodesSet, 
				options.isDownstream(),
				options.getLengthLimit());
		}
		//if action is called from TopMenuBar
		else
		{
			//open dialog
			CommonStreamQueryParamWithEntitiesDialog dialog =
				new CommonStreamQueryParamWithEntitiesDialog(main);
			options = dialog.open(options);

			if ( !options.isCancel() )
			{	
				options.setCancel(true);
			}
			else
			{
				return;
			}

			//Get added entities from dialog
			List<EntityHolder> addedEntities = dialog.getAddedEntities();
			
			//States of each entity
			Set<Set<Node>> sourceStatesSet = new LinkedHashSet<Set<Node>>();

			//for each entity
			for (EntityHolder entity : addedEntities)
			{
				//States of entity in Node type
				Set<Node> entityNodeStates = new HashSet<Node>();

				//States of entity in GraphObject type
				Set<Node> entityStates =
					main.getRootGraph().getRelatedStates(entity);

				//Replace complex members with complexes
				main.getRootGraph().replaceComplexMembersWithComplexes(entityStates);

				//Convert GraphObjects to Nodes
				for (GraphObject go : entityStates)
				{
					if (go instanceof Node)
					{
						entityNodeStates.add((Node) go);
					}
				}

				//Add states of entity to collection of states
				sourceStatesSet.add(entityNodeStates);
				sourceNodesSet.addAll(entityNodeStates);
			}

			//Common Stream Query
			stream = new LocalCommonStreamQuery(sourceStatesSet, 
				options.isDownstream(),
				options.getLengthLimit());
		}
		
		//Run Common Stream Query and get result
		streamResult = stream.run();
		
		/**
         * To add the missing paths between the source set and the result set
         * to the query result, PoI must be performed.
         */
        LocalPoIQuery poi = null;
        
        //if downsteam, PoI from source to result
        if (options.isDownstream())
        {
            poi = new LocalPoIQuery(sourceNodesSet,
            	streamResult,
                true,
                options.getLengthLimit(),
                false);
        }
        //if upstream, PoI from result to source
        else
        {
            poi = new LocalPoIQuery(streamResult,
            	sourceNodesSet,
                true,
                options.getLengthLimit(),
                false);
        }

        //View result of query and Highlight it
        viewAndHighlightResult(poi.run(),
        	options.isCurrentView(),
        	"Common Stream");
	}
}
