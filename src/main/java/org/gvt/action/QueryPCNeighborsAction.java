package org.gvt.action;

import cpath.client.CPathClient;
import cpath.client.util.CPathException;
import cpath.service.GraphType;
import org.biopax.paxtools.model.Model;
import org.gvt.ChisioMain;
import org.gvt.gui.AbstractQueryParamDialog;
import org.gvt.gui.NeighborhoodQueryParamWithEntitiesDialog;

import java.util.List;

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
	public QueryPCNeighborsAction(ChisioMain main, boolean upstream, boolean downstream)
	{
		super(main, upstream && downstream ? "Both Streams" : upstream ? "Upstream" : "Downstream",
			true);

		assert upstream || downstream;

		options.setUpstream(upstream);
		options.setDownstream(downstream);
	}

	public QueryPCNeighborsAction(ChisioMain main)
	{
		super(main, "Neighborhood ...", false);
	}

	public void run()
	{
		execute();
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
	protected AbstractQueryParamDialog getDialog()
	{
		return new NeighborhoodQueryParamWithEntitiesDialog(main, null);
	}

	@Override
	protected boolean canQuery()
	{
		List<String> symbols = options.getConvertedSourceList();
		warnForUnknownSymbols(options.getUnknownSymbols());
		return !symbols.isEmpty();
	}
}
