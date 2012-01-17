package org.gvt.action;

import org.biopax.paxtools.io.pathwayCommons.PathwayCommons2Client;
import org.biopax.paxtools.io.pathwayCommons.util.PathwayCommonsException;
import org.biopax.paxtools.model.Model;
import org.gvt.ChisioMain;
import org.gvt.gui.AbstractQueryParamDialog;
import org.gvt.gui.PoIQueryParamWithEntitiesDialog;

import java.util.List;

/**
 * @author Ozgun Babur
 *
 */
public class QueryPCPathsFromToAction extends QueryPCAction
{
	public QueryPCPathsFromToAction(ChisioMain main)
	{
		super(main, "Query Paths From--To--", false);
	}

	public void run()
	{
		execute();
	}

	@Override
	protected Model doQuery() throws PathwayCommonsException
	{
		List<String> sourceSymbols = options.getFormattedSourceList();
		List<String> targetSymbols = options.getFormattedTargetList();

		PathwayCommons2Client pc2 = new PathwayCommons2Client();
		pc2.setGraphQueryLimit(options.getLengthLimit());
		return pc2.getPathsFromTo(sourceSymbols, targetSymbols);
	}

	@Override
	protected AbstractQueryParamDialog getDialog()
	{
		return new PoIQueryParamWithEntitiesDialog(main, null);
	}

	@Override
	protected boolean canQuery()
	{
		return !options.getFormattedSourceList().isEmpty() &&
			!options.getFormattedTargetList().isEmpty();
	}
}
