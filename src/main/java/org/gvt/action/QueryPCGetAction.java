package org.gvt.action;

import cpath.client.util.CPathException;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.eclipse.jface.dialogs.MessageDialog;
import org.gvt.ChisioMain;
import org.gvt.gui.AbstractQueryParamDialog;
import org.gvt.gui.StringInputDialog;
import org.gvt.model.basicsif.BasicSIFGraph;
import org.patika.mada.graph.GraphObject;

import java.util.Collection;
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
		super(main, "Object With Database ID ...", useSelected, false);
	}

	public void run()
	{
        if(main.getBioPAXModel() == null || main.getBioPAXModel().getLevel().equals(BioPAXLevel.L3))
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
		if (useSelected && !options.getSourceList().isEmpty())
		{
			id = new HashSet<String>(options.getSourceList());
		}

		return getPCGetQuery().sources(id).result();
	}

	@Override
	protected Collection<GraphObject> doSIFQuery(BasicSIFGraph graph) throws CPathException
	{
		throw new UnsupportedOperationException("Cannot get from SIF");
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
