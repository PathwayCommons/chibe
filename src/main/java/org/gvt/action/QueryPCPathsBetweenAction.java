package org.gvt.action;

import cpath.client.CPath2Client;
import cpath.client.util.CPathException;
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
		super(main, "Paths Between ...", useSelected);
	}

	public void run()
	{
		execute();
	}

	@Override
	protected Model doQuery() throws CPathException
	{
		List<String> sourceSymbols = options.getConvertedSourceList();
		CPath2Client pc2 = getPCClient();
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
		List<String> sourceSymbols = options.getConvertedSourceList();
		warnForUnknownSymbols(options.getUnknownSymbols());
		if (sourceSymbols.size() < 2)
		{
			warnForLowInput(2, sourceSymbols.size());
		}
		return sourceSymbols.size() > 1;
	}
}
