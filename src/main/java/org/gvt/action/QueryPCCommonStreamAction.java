package org.gvt.action;

import cpath.client.internal.PathwayCommons2Client;
import cpath.client.internal.util.PathwayCommonsException;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.query.algorithm.Direction;
import org.gvt.ChisioMain;
import org.gvt.gui.AbstractQueryParamDialog;
import org.gvt.gui.CommonStreamQueryParamWithEntitiesDialog;

import java.util.List;

/**
 * @author Ozgun Babur
 *
 */
public class QueryPCCommonStreamAction extends QueryPCAction
{
	boolean downstream;

	public QueryPCCommonStreamAction(ChisioMain main)
	{
		super(main, "Query Common Stream", false);
	}

	public QueryPCCommonStreamAction(ChisioMain main, boolean useSelected, boolean downstream)
	{
		this(main);
		super.useSelected = useSelected;
		this.downstream = downstream;

		if (useSelected)
		{
			setText(downstream ? "Query Common Downstream" : "Query Common Upstream");

			options.setUpstream(!downstream);
			options.setDownstream(downstream);
			options.setLengthLimit(2);
		}
	}

	public void run()
	{
		execute();
	}

	@Override
	protected Model doQuery() throws PathwayCommonsException
	{
		List<String> sourceSymbols = options.getConvertedSourceList();

		PathwayCommons2Client pc2 = getPCClient();
		pc2.setGraphQueryLimit(options.getLengthLimit());

		Direction direction = options.isDownstream() ? Direction.DOWNSTREAM : Direction.UPSTREAM;

		return pc2.getCommonStream(sourceSymbols, direction);
	}

	@Override
	protected AbstractQueryParamDialog getDialog()
	{
		return new CommonStreamQueryParamWithEntitiesDialog(main, null);
	}

	@Override
	protected boolean canQuery()
	{
		return !options.getConvertedSourceList().isEmpty();
	}
}
