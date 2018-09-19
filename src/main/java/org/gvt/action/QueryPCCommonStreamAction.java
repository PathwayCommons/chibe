package org.gvt.action;

import cpath.client.CPathClient;
import cpath.client.util.CPathException;
import cpath.service.GraphType;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.query.QueryExecuter;
import org.biopax.paxtools.query.algorithm.Direction;
import org.gvt.ChisioMain;
import org.gvt.gui.AbstractQueryParamDialog;
import org.gvt.gui.CommonStreamQueryParamWithEntitiesDialog;
import org.gvt.model.basicsif.BasicSIFGraph;
import org.patika.mada.algorithm.AlgoRunner;
import org.patika.mada.graph.GraphObject;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Ozgun Babur
 *
 */
public class QueryPCCommonStreamAction extends QueryPCAction
{
	public QueryPCCommonStreamAction(ChisioMain main, QueryLocation qLoc)
	{
		super(main, "Common Stream ...", false, qLoc);
	}

	public QueryPCCommonStreamAction(ChisioMain main, boolean downstream, QueryLocation qLoc)
	{
		this(main, qLoc);
		super.useSelected = true;
		setText(downstream ? "Downstream" : "Upstream");

		options.setUpstream(!downstream);
		options.setDownstream(downstream);
		options.setLengthLimit(2);
	}

	public void run()
	{
		execute();
	}

	@Override
	protected String getSIFQueryType()
	{
		return "commonstream";
	}

	@Override
	protected Model doQuery() throws CPathException
	{
		List<String> sourceSymbols = options.getConvertedSourceList();

		return getPCGraphQuery().
			kind(GraphType.COMMONSTREAM).
			sources(sourceSymbols).
			limit(options.getLengthLimit()).direction(options.isDownstream() ?
				CPathClient.Direction.DOWNSTREAM : CPathClient.Direction.UPSTREAM).
			result();
	}

	@Override
	protected Set<BioPAXElement> doFileQuery(Model model)
	{
		List<String> symbols = options.getConvertedSourceList();
		Set<BioPAXElement> source = findRelatedReferences(model, symbols);

		return QueryExecuter.runCommonStreamWithPOI(source, model, options.isUpstream() ?
				Direction.UPSTREAM : Direction.DOWNSTREAM,  options.getLengthLimit());
	}

	@Override
	protected Collection<GraphObject> doSIFQuery(BasicSIFGraph graph) throws CPathException
	{
		return AlgoRunner.searchCommonStream(getSeed(graph, options.getConvertedSourceList()),
			options.isDownstream(), options.getLengthLimit(), !options.undirectedSIFTypeSelected());
	}

	@Override
	protected AbstractQueryParamDialog getDialog()
	{
		return new CommonStreamQueryParamWithEntitiesDialog(main, queryLoc.isSIF());
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
