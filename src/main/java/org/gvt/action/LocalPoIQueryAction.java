package org.gvt.action;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.query.QueryExecuter;
import org.biopax.paxtools.query.algorithm.LimitType;
import org.gvt.gui.PoIQueryParamWithEntitiesDialog;
import org.gvt.util.BioPAXUtil;
import org.gvt.util.EntityHolder;
import org.gvt.ChisioMain;
import org.eclipse.jface.dialogs.MessageDialog;
import org.gvt.util.QueryOptionsPack;
import org.patika.mada.graph.Node;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.algorithm.LocalPoIQuery;
import org.biopax.paxtools.model.Model;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * This class creates the action for opening query properties window.
 *
 * @author Merve Cakir
 */
public class LocalPoIQueryAction extends AbstractLocalQueryAction
{
	ChisioMain main;

	/**
	 * Dialog options are stored, in order to use next time dialog is opened.
	 */
	QueryOptionsPack options;

	/**
	 * Constructor
	 */
	public LocalPoIQueryAction(ChisioMain main)
	{
		super(main, "Paths From To ...");
		setToolTipText(getText());
		options = new QueryOptionsPack();
		this.main = main;
	}

	public void run()
	{
		LocalPoI();
	}

	public void LocalPoI()
	{
		if (main.getBioPAXModel() == null)
		{
			MessageDialog.openError(main.getShell(), "Error!",
				"Load or query a BioPAX model first!");

			return;
		}

		//Source and target node sets

		Set<BioPAXElement> source;
		Set<BioPAXElement> target;

		//open dialog
		PoIQueryParamWithEntitiesDialog dialog = new PoIQueryParamWithEntitiesDialog(
			main, BioPAXUtil.getEntities(main.getBioPAXModel()));

		options = dialog.open(options);

		if (!options.isCancel())
		{
			options.setCancel(true);
		}
		else
		{
			return;
		}

		//Get added entities.
		List<EntityHolder> sourceAddedEntities = dialog.getAddedSourceEntities();
		List<EntityHolder> targetAddedEntities = dialog.getAddedTargetEntities();

		source = BioPAXUtil.getContent(sourceAddedEntities);
		target = BioPAXUtil.getContent(targetAddedEntities);


		Set<BioPAXElement> result = QueryExecuter.runPathsFromTo(source, target,
			main.getBioPAXModel(), options.getLimitType() ? LimitType.NORMAL :
			LimitType.SHORTEST_PLUS_K, options.getLengthLimit());

		viewAndHighlightResult(result, options.isCurrentView(), "Query Result");
	}
}





	

