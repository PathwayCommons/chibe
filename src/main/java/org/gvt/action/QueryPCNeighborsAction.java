package org.gvt.action;

import cpath.client.CPath2Client;
import cpath.client.util.CPathException;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.query.algorithm.Direction;
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
		super(main, upstream && downstream ? "Bothstream" : upstream ? "Upstream" : "Downstream", 
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

		CPath2Client pc2 = getPCClient();
		pc2.setGraphQueryLimit(options.getLengthLimit());

		Direction dir = options.isUpstream() && options.isDownstream() ? Direction.BOTHSTREAM :
			options.isUpstream() ? Direction.UPSTREAM : Direction.DOWNSTREAM;

		return pc2.getNeighborhood(symbols, dir);
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
