package org.gvt.action;

import org.biopax.paxtools.io.pathwayCommons.PathwayCommons2Client;
import org.biopax.paxtools.io.pathwayCommons.util.PathwayCommonsException;
import org.biopax.paxtools.model.Model;
import org.gvt.ChisioMain;
import org.gvt.gui.AbstractQueryParamDialog;
import org.gvt.gui.StringInputDialog;

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
	protected Model doQuery() throws PathwayCommonsException
	{
		PathwayCommons2Client pc2 = new PathwayCommons2Client();

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
		return !(id == null && options.getSourceList().isEmpty());
	}
}
