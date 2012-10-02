package org.gvt.action;

import cpath.client.CPath2Client;
import cpath.client.util.CPathException;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.eclipse.jface.dialogs.MessageDialog;
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
		super(main, "Object With RDF ID ...", useSelected);
	}

	public void run()
	{
        if(main.getOwlModel() == null || main.getOwlModel().getLevel().equals(BioPAXLevel.L3))
        {
            if (!useSelected)
            {
                StringInputDialog dialog = new StringInputDialog(main.getShell(), "Get Objects",
                    "Enter RDF ID of database object", id,"Find the specified object");

                id = dialog.open();
                if (id != null)
                {
                    id = id.trim();
                    if (id.length() == 0) id = null;
                }
            }

            execute();
            id = null;
        }
        else
        {
            MessageDialog.openError(main.getShell(), "Incompatible Levels","This query is only applicable to Level 3 models.");
        }
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
