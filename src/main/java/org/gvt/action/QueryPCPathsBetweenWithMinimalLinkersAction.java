package org.gvt.action;

import cpath.client.util.CPathException;
import cpath.service.GraphType;
import org.biopax.paxtools.model.BioPAXElement;
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
import java.util.Set;

/**
 * @author Ozgun Babur
 *
 */
public class QueryPCPathsBetweenWithMinimalLinkersAction extends QueryPCAction
{
	private String[] symbols;

	public QueryPCPathsBetweenWithMinimalLinkersAction(ChisioMain main, QueryLocation qLoc, String... symbols)
	{
		super(main, "Paths Between With Minimal Linkers...", false, qLoc);

		if (qLoc == QueryLocation.FILE_MECH || qLoc == QueryLocation.PC_MECH)
			throw new IllegalArgumentException("Cannot perform action on mechanistic graphs.");

		if (symbols.length > 1)
		{
			this.symbols = symbols;
			String text = "Graph around " + symbols[0];
			for (int i = 1; i < symbols.length; i++)
			{
				text += " and " + symbols[i];
			}
			setText(text);
		}
	}

	public QueryPCPathsBetweenWithMinimalLinkersAction(ChisioMain main, boolean useSelected, QueryLocation qLoc)
	{
		super(main, "Paths Between With Minimal Linkers...", useSelected, qLoc);
	}

	public void run()
	{
		execute();
	}

	@Override
	protected Model doQuery() throws CPathException
	{
		throw new RuntimeException("This method should not be called!");
	}

	@Override
	protected Set<BioPAXElement> doFileQuery(Model model)
	{
		throw new RuntimeException("This method should not be called!");
	}

	@Override
	protected Collection<GraphObject> doSIFQuery(BasicSIFGraph graph) throws CPathException
	{
		return AlgoRunner.searchPathsBetweenSIFWithMinimalLinkers(
			getSeed(graph, options.getConvertedSourceList()));
	}

	@Override
	protected AbstractQueryParamDialog getDialog()
	{
		if (symbols == null || symbols.length < 2)
			return new GoIQueryParamWithEntitiesDialog(main, queryLoc.isSIF());
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

	@Override
	protected String getSIFQueryType()
	{
		throw new RuntimeException("Code should not reach here!");
	}

	@Override
	protected boolean highlightSeed()
	{
		return true;
	}
}
