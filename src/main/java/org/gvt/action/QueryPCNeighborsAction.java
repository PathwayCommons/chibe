package org.gvt.action;

import cpath.client.CPathClient;
import cpath.client.util.CPathException;
import cpath.service.GraphType;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.query.QueryExecuter;
import org.biopax.paxtools.query.algorithm.Direction;
import org.gvt.ChisioMain;
import org.gvt.gui.AbstractQueryParamDialog;
import org.gvt.gui.NeighborhoodQueryParamWithEntitiesDialog;
import org.gvt.model.basicsif.BasicSIFGraph;
import org.patika.mada.algorithm.AlgoRunner;
import org.patika.mada.graph.GraphObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Ozgun Babur
 *
 */
public class QueryPCNeighborsAction extends QueryPCAction
{
	/**
	 * If you use this constructor, then useSelected is true. This means no dialog appears.
	 *
	 * @param main
	 * @param upstream
	 * @param downstream
	 */
	public QueryPCNeighborsAction(ChisioMain main, boolean upstream, boolean downstream,
		QueryLocation qLoc)
	{
		super(main, upstream && downstream ? "Both Streams" : upstream ? "Upstream" : "Downstream",
			true, qLoc);

		assert upstream || downstream;

		options.setUpstream(upstream);
		options.setDownstream(downstream);
	}

	public QueryPCNeighborsAction(ChisioMain main, QueryLocation qLoc)
	{
		super(main, "Neighborhood ...", false, qLoc);
	}

	public void run()
	{
		execute();
	}

	@Override
	protected String getSIFQueryType()
	{
		return "neighborhood";
	}

	@Override
	protected Model doQuery() throws CPathException
	{
		List<String> symbols = options.getConvertedSourceList();

		try
		{
			return getPCGraphQuery().
				kind(GraphType.NEIGHBORHOOD).
				sources(symbols).
				limit(options.getLengthLimit()).
				direction(options.isUpstream() && options.isDownstream() ?
					CPathClient.Direction.BOTHSTREAM: options.isUpstream() ?
					CPathClient.Direction.UPSTREAM : CPathClient.Direction.DOWNSTREAM).
				result();
		}
		catch (Exception e)
		{
			//todo better handle empty results
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected Set<BioPAXElement> doFileQuery(Model model)
	{
		List<String> symbols = options.getConvertedSourceList();
		Set<BioPAXElement> source = findRelatedReferences(model, symbols);

		return QueryExecuter.runNeighborhood(source, model, options.getLengthLimit(),
			options.isUpstream() && options.isDownstream() ?
				Direction.BOTHSTREAM : options.isUpstream() ?
				Direction.UPSTREAM : Direction.DOWNSTREAM);
	}

	@Override
	protected Collection<GraphObject> doSIFQuery(BasicSIFGraph graph) throws CPathException
	{
		return AlgoRunner.searchNeighborhood(getSeed(graph, options.getConvertedSourceList()),
			options.getLengthLimit(), options.isUpstream(), options.isDownstream());
	}

	@Override
	protected AbstractQueryParamDialog getDialog()
	{
		return new NeighborhoodQueryParamWithEntitiesDialog(main, queryLoc.isSIF());
	}

	@Override
	protected boolean canQuery()
	{
		List<String> symbols = options.getConvertedSourceList();
		warnForUnknownSymbols(options.getUnknownSymbols());
		return !symbols.isEmpty();
	}
}
