package org.gvt.action;

import cpath.client.CPath2Client;
import cpath.client.util.CPathException;
import org.biopax.paxtools.model.Model;
import org.gvt.ChisioMain;
import org.gvt.gui.AbstractQueryParamDialog;
import org.gvt.gui.StringInputDialog;
import org.gvt.util.Conf;

/**
 * @author Ozgun Babur
 *
 */
public class QueryPCGetAction extends QueryPCAction
{
	String id;

	public QueryPCGetAction(ChisioMain main, boolean useSelected)
	{
		super(main, "Get with RDF ID", useSelected);
	}

	public void run()
	{
		if (!useSelected)
		{
			StringInputDialog dialog = new StringInputDialog(main.getShell(), "Get Objects",
				"Enter RDF ID of databse object", id);

			id = dialog.open();
			if (id != null)
			{
				id = id.trim();
				if (id.length() == 0) id = null;
			}
		}

		execute();
	}

	@Override
	protected Model doQuery() throws CPathException
	{
		CPath2Client pc2 = getPCClient();

		if (useSelected && !options.getSourceList().isEmpty())
		{
			id = options.getSourceList().iterator().next();
		}

		return pc2.get(id);
	}

	@Override
	protected AbstractQueryParamDialog getDialog()
	{
		return null;
	}

	@Override
	protected boolean canQuery()
	{
		return !(id == null &&
			(options.getSourceList() == null || options.getSourceList().isEmpty()));
	}
}
