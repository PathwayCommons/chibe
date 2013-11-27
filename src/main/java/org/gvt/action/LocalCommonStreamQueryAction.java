package org.gvt.action;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.query.QueryExecuter;
import org.biopax.paxtools.query.algorithm.Direction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.gvt.ChisioMain;
import org.gvt.gui.CommonStreamQueryParamDialog;
import org.gvt.gui.CommonStreamQueryParamWithEntitiesDialog;
import org.gvt.util.BioPAXUtil;
import org.gvt.util.EntityHolder;
import org.gvt.util.QueryOptionsPack;

import java.util.List;
import java.util.Set;

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
	QueryOptionsPack options;

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
		super(main, "Common Stream ...");
		setToolTipText(getText());
		options = new QueryOptionsPack();
		this.main = main;
		this.useSelection = useSelection;	
	}

	public void run()
	{	
		if (main.getBioPAXModel() == null)
		{
			MessageDialog.openError(main.getShell(), "Error!",
				"Load or query a BioPAX model first!");

			return;
		}
		
		//remove previous highlights
		RemoveHighlightsAction rha = new RemoveHighlightsAction(this.main);
		rha.run();
		
		Set<BioPAXElement> source;

		//if action is called from PopupMenu
		if (useSelection)
		{
			if (main.getPathwayGraph() == null)
			{
				MessageDialog.openError(main.getShell(), "Error!",
					"This feature works only for BioPAX graphs");

				return;
			}

			//open dialog
			CommonStreamQueryParamDialog dialog = new CommonStreamQueryParamDialog(this.main);
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
			source = main.getSelectedBioPAXElements();
		}
		//if action is called from TopMenuBar
		else
		{
			//open dialog
			CommonStreamQueryParamWithEntitiesDialog dialog =
				new CommonStreamQueryParamWithEntitiesDialog(
					main, BioPAXUtil.getEntities(main.getBioPAXModel()));

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
			List<EntityHolder> addedEntities = dialog.getAddedSourceEntities();
			source = BioPAXUtil.getContent(addedEntities);
		}

		Set<BioPAXElement> result = QueryExecuter.runCommonStreamWithPOI(source, main.getBioPAXModel(),
			options.isDownstream() ? Direction.DOWNSTREAM : Direction.UPSTREAM,
			options.getLengthLimit());

        //View result of query and Highlight it
        viewAndHighlightResult(result, options.isCurrentView(), "Query Result");
	}
}
