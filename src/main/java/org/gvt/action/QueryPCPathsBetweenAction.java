package org.gvt.action;

import cpath.client.CPathClient;
import cpath.client.util.CPathException;
import cpath.service.GraphType;
import org.biopax.paxtools.model.Model;
import org.gvt.ChisioMain;
import org.gvt.gui.AbstractQueryParamDialog;
import org.gvt.gui.GoIQueryParamWithEntitiesDialog;
import org.gvt.model.basicsif.BasicSIFGraph;
import org.patika.mada.algorithm.AlgoRunner;
import org.patika.mada.graph.GraphObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Ozgun Babur
 *
 */
public class QueryPCPathsBetweenAction extends QueryPCAction
{
	private String[] symbols;

	public QueryPCPathsBetweenAction(ChisioMain main, boolean querySIF, String... symbols)
	{
		super(main, "Paths Between ...", false, querySIF);
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

	public QueryPCPathsBetweenAction(ChisioMain main, boolean useSelected, boolean querySIF)
	{
		super(main, "Paths Between ...", useSelected, querySIF);
	}

	public void run()
	{
		execute();
	}

	@Override
	protected Model doQuery() throws CPathException
	{
		List<String> sourceSymbols = options.getConvertedSourceList();
		return getPCGraphQuery().
			kind(GraphType.PATHSBETWEEN).
			sources(sourceSymbols).
			limit(options.getLengthLimit()).
			result();
	}

	@Override
	protected Collection<GraphObject> doSIFQuery(BasicSIFGraph graph) throws CPathException
	{
		return AlgoRunner.searchGraphOfInterest(graph,
			getSeed(graph, options.getConvertedSourceList()), options.getLengthLimit(),
			!options.undirectedSIFTypeSelected());
	}

	@Override
	protected AbstractQueryParamDialog getDialog()
	{
		if (symbols == null || symbols.length < 2)
			return new GoIQueryParamWithEntitiesDialog(main, querySIF);
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
		if (newPathwayName != null) return super.getNewPathwayName();

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
