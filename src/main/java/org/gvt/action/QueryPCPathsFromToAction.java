package org.gvt.action;

import cpath.client.CPath2Client;
import cpath.client.util.CPathException;
import org.biopax.paxtools.model.Model;
import org.gvt.ChisioMain;
import org.gvt.gui.AbstractQueryParamDialog;
import org.gvt.gui.PoIQueryParamWithEntitiesDialog;

import java.util.Arrays;
import java.util.List;

/**
 * @author Ozgun Babur
 *
 */
public class QueryPCPathsFromToAction extends QueryPCAction
{
	private String fromSymbol;
	private String toSymbol;

	public QueryPCPathsFromToAction(ChisioMain main)
	{
		super(main, "Paths From To ...", false);
	}

	public QueryPCPathsFromToAction(ChisioMain main, String fromSymbol, String toSymbol)
	{
		super(main, "Paths from " + fromSymbol + " to " + toSymbol, false);
		this.fromSymbol = fromSymbol;
		this.toSymbol = toSymbol;
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
		if (fromSymbol == null)
		{
			assert toSymbol == null;
			return new PoIQueryParamWithEntitiesDialog(main, null);
		}
		else
		{
			options.setSourceList(Arrays.asList(fromSymbol));
			options.setTargetList(Arrays.asList(toSymbol));
			options.setLengthLimit(1);

			return null;
		}
	}

	@Override
	protected boolean canQuery()
	{
		List<String> sourceSymbols = options.getConvertedSourceList();
		List<String> targetSymbols = options.getConvertedTargetList();
		warnForUnknownSymbols(options.getUnknownSymbols());
		return !sourceSymbols.isEmpty() && !targetSymbols.isEmpty();
	}

	@Override
	protected String getNewPathwayName()
	{
		if (fromSymbol != null)
		{
			assert toSymbol != null;
			return fromSymbol + " -> " + toSymbol;
		}
		else return super.getNewPathwayName();
	}
}
