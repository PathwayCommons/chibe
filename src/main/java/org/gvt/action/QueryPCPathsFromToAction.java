package org.gvt.action;

import cpath.client.CPath2Client;
import cpath.client.util.CPathException;
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
		super(main, "Paths Of Interest ...", false);
	}

	public void run()
	{
		execute();
	}

	@Override
	protected Model doQuery() throws CPathException
	{
		List<String> sourceSymbols = options.getConvertedSourceList();
		List<String> targetSymbols = options.getConvertedTargetList();

		CPath2Client pc2 = getPCClient();
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
		List<String> sourceSymbols = options.getConvertedSourceList();
		List<String> targetSymbols = options.getConvertedTargetList();
		warnForUnknownSymbols(options.getUnknownSymbols());
		return !sourceSymbols.isEmpty() && !targetSymbols.isEmpty();
	}
}
