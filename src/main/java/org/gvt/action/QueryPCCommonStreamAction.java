package org.gvt.action;

import org.biopax.paxtools.io.pathwayCommons.PathwayCommons2Client;
import org.biopax.paxtools.io.pathwayCommons.util.PathwayCommonsException;
import org.biopax.paxtools.model.Model;
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

		PathwayCommons2Client.STREAM_DIRECTION direction = options.isDownstream() ?
			PathwayCommons2Client.STREAM_DIRECTION.DOWNSTREAM :
			PathwayCommons2Client.STREAM_DIRECTION.UPSTREAM;

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
