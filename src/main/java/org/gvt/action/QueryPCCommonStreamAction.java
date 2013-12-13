package org.gvt.action;

import cpath.client.CPathClient;
import cpath.client.util.CPathException;
import cpath.service.GraphType;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.query.algorithm.Direction;
import org.gvt.ChisioMain;
import org.gvt.gui.AbstractQueryParamDialog;
import org.gvt.gui.CommonStreamQueryParamWithEntitiesDialog;
import org.gvt.model.basicsif.BasicSIFGraph;
import org.patika.mada.algorithm.AlgoRunner;
import org.patika.mada.graph.GraphObject;

import java.util.Collection;
import java.util.List;

/**
 * @author Ozgun Babur
 *
 */
public class QueryPCCommonStreamAction extends QueryPCAction
{
	public QueryPCCommonStreamAction(ChisioMain main, boolean querySIF)
	{
		super(main, "Common Stream ...", false, querySIF);
	}

	public QueryPCCommonStreamAction(ChisioMain main, boolean downstream, boolean querySIF)
	{
		this(main, querySIF);
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
	protected Collection<GraphObject> doSIFQuery(BasicSIFGraph graph) throws CPathException
	{
		return AlgoRunner.searchCommonStream(getSeed(graph, options.getConvertedSourceList()),
			options.isDownstream(), options.getLengthLimit());
	}

	@Override
	protected AbstractQueryParamDialog getDialog()
	{
		return new CommonStreamQueryParamWithEntitiesDialog(main, querySIF);
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
