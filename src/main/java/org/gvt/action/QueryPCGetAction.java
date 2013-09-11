package org.gvt.action;

import cpath.client.CPathClient;
import cpath.client.util.CPathException;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.eclipse.jface.dialogs.MessageDialog;
import org.gvt.ChisioMain;
import org.gvt.gui.AbstractQueryParamDialog;
import org.gvt.gui.StringInputDialog;
import org.gvt.util.Conf;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ozgun Babur
 *
 */
public class QueryPCGetAction extends QueryPCAction
{
	protected Set<String> id;

	public QueryPCGetAction(ChisioMain main, boolean useSelected)
	{
		super(main, "Object With Database ID ...", useSelected);
	}

	public void run()
	{
        if(main.getOwlModel() == null || main.getOwlModel().getLevel().equals(BioPAXLevel.L3))
        {
            if (!useSelected)
            {
				String ids = null;
				if (id != null && !id.isEmpty())
				{
					if (id.size() == 1) ids = id.iterator().next();
					else
					{
						ids = " ";
						for (String s : id)
						{
							ids += s;
						}
						ids = ids.trim();
					}
				}

                StringInputDialog dialog = new StringInputDialog(main.getShell(), "Get Objects",
                    "Enter Pathway Commons ID of database object", ids, "Find the specified object");

                ids = dialog.open();

				if (id == null) id = new HashSet<String>();
				Collections.addAll(id, ids.split("\\s+"));
				if (id.isEmpty()) id = null;
            }

            execute();
            id = null;
        }
        else
        {
            MessageDialog.openError(main.getShell(), "Incompatible Levels","This query is only applicable to Level 3 models.");
        }
    }

	public void setIDs(Set<String> ids)
	{
		this.id = ids;
	}

	@Override
	protected Model doQuery() throws CPathException
	{
		CPathClient pc2 = getPCClient();

		if (useSelected && !options.getSourceList().isEmpty())
		{
			id = new HashSet<String>(options.getSourceList());
		}

		return getPCGetQuery().sources(id).result();
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
