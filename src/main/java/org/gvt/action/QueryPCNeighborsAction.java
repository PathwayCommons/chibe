package org.gvt.action;

import org.biopax.paxtools.io.pathwayCommons.PathwayCommons2Client;
import org.biopax.paxtools.io.pathwayCommons.util.PathwayCommonsException;
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
	public QueryPCNeighborsAction(ChisioMain main, boolean useSelected)
	{
		super(main, "Query Neighbors", useSelected);
	}

	public void run()
	{
		execute();
	}

	@Override
	protected Model doQuery() throws PathwayCommonsException
	{
		PathwayCommons2Client pc2 = getPCClient();
		pc2.setGraphQueryLimit(options.getLengthLimit());
		List<String> symbols = options.getConvertedSourceList();

		PathwayCommons2Client.STREAM_DIRECTION dir =
			options.isUpstream() && options.isDownstream() ?
				PathwayCommons2Client.STREAM_DIRECTION.BOTHSTREAM :
			options.isUpstream() ? PathwayCommons2Client.STREAM_DIRECTION.UPSTREAM :
				PathwayCommons2Client.STREAM_DIRECTION.DOWNSTREAM;

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
		return !options.getConvertedSourceList().isEmpty();
	}
}
