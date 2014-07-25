package org.gvt.action;

import cpath.client.util.CPathException;
import cpath.service.GraphType;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.query.QueryExecuter;
import org.biopax.paxtools.query.algorithm.Direction;
import org.biopax.paxtools.query.algorithm.LimitType;
import org.gvt.ChisioMain;
import org.gvt.gui.AbstractQueryParamDialog;
import org.gvt.gui.PoIQueryParamWithEntitiesDialog;
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
public class QueryPCPathsFromToAction extends QueryPCAction
{
	private String fromSymbol;
	private String toSymbol;

	public QueryPCPathsFromToAction(ChisioMain main, QueryLocation qLoc)
	{
		super(main, "Paths From To ...", false, qLoc);
	}

	public QueryPCPathsFromToAction(ChisioMain main, String fromSymbol, String toSymbol,
		QueryLocation qLoc)
	{
		super(main, "Paths from " + fromSymbol + " to " + toSymbol, false, qLoc);
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

		return getPCGraphQuery().limit(options.getLengthLimit()).
			kind(GraphType.PATHSFROMTO).
			sources(sourceSymbols).
			targets(targetSymbols).
			result();
	}

	@Override
	protected Set<BioPAXElement> doFileQuery(Model model)
	{
		List<String> sourceSym = options.getConvertedSourceList();
		Set<BioPAXElement> source = findRelatedReferences(model, sourceSym);

		List<String> targSym = options.getConvertedSourceList();
		Set<BioPAXElement> target = findRelatedReferences(model, targSym);

		return QueryExecuter.runPathsFromTo(source, target, model, options.getLimitType() ?
			LimitType.NORMAL : LimitType.SHORTEST_PLUS_K, options.getLengthLimit());
	}

	@Override
	protected Collection<GraphObject> doSIFQuery(BasicSIFGraph graph) throws CPathException
	{
		return AlgoRunner.searchPathsFromTo(getSeed(graph, options.getConvertedSourceList()),
			getSeed(graph, options.getConvertedTargetList()), options.getLengthLimit(),
			!options.undirectedSIFTypeSelected(),
			options.getLimitType() ? -1 : options.getShortestPlusKLimit(), options.isStrict());
	}

	@Override
	protected AbstractQueryParamDialog getDialog()
	{
		if (fromSymbol == null)
		{
			assert toSymbol == null;
			return new PoIQueryParamWithEntitiesDialog(main, queryLoc.isSIF());
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
		if (newPathwayName != null) return super.getNewPathwayName();

		if (fromSymbol != null)
		{
			assert toSymbol != null;
			return fromSymbol + " -> " + toSymbol;
		}
		else return super.getNewPathwayName();
	}
}
