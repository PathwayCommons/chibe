package org.gvt.action;

import org.biopax.paxtools.io.pathwayCommons.PathwayCommons2Client;
import org.biopax.paxtools.io.pathwayCommons.util.PathwayCommonsException;
import org.biopax.paxtools.model.Model;
import org.gvt.ChisioMain;
import org.gvt.gui.AbstractQueryParamDialog;
import org.gvt.gui.GoIQueryParamWithEntitiesDialog;

import java.util.List;

/**
 * @author Ozgun Babur
 *
 */
public class QueryPCPathsBetweenAction extends QueryPCAction
{
	public QueryPCPathsBetweenAction(ChisioMain main, boolean useSelected)
	{
		super(main, "Query Paths Between", useSelected);
	}

	public void run()
	{
		execute();
	}

	@Override
	protected Model doQuery() throws PathwayCommonsException
	{
		List<String> sourceSymbols = options.getFormattedSourceList();
		PathwayCommons2Client pc2 = new PathwayCommons2Client();
		pc2.setGraphQueryLimit(options.getLengthLimit());
		return pc2.getPathsBetween(sourceSymbols);
	}

	@Override
	protected AbstractQueryParamDialog getDialog()
	{
		return new GoIQueryParamWithEntitiesDialog(main, null);
	}

	@Override
	protected boolean canQuery()
	{
		return options.getFormattedSourceList().size() > 1;
	}
}
