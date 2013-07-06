package org.gvt.action;

import cpath.client.CPath2Client;
import cpath.client.util.CPathException;
import org.biopax.paxtools.model.Model;
import org.gvt.ChisioMain;
import org.gvt.gui.AbstractQueryParamDialog;
import org.gvt.gui.GoIQueryParamWithEntitiesDialog;

import java.util.Arrays;
import java.util.List;

/**
 * @author Ozgun Babur
 *
 */
public class QueryPCPathsBetweenAction extends QueryPCAction
{
	private String[] symbols;

	public QueryPCPathsBetweenAction(ChisioMain main, String... symbols)
	{
		super(main, "Paths Between ...", false);
		if (symbols.length > 1)
		{
			this.symbols = symbols;
			String text = "Paths Between " + symbols[0];
			for (int i = 1; i < symbols.length; i++)
			{
				text += " and " + symbols[i];
			}
			setText(text);
		}
	}

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
		if (symbols == null || symbols.length < 2)
			return new GoIQueryParamWithEntitiesDialog(main, null);
		else
		{
			options.setSourceList(Arrays.asList(symbols));
			options.setLengthLimit(1);
			return null;
		}
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

	@Override
	protected String getNewPathwayName()
	{
		if (symbols != null)
		{
			assert symbols.length > 1;
			String s = symbols[0];
			for (int i = 1; i < symbols.length; i++)
			{
				s += " -- " + symbols[i];
			}
			return s;
		}
		else return super.getNewPathwayName();
	}

}
