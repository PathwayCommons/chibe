package org.gvt.action;

import cpath.service.jaxb.SearchHitType;
import cpath.service.jaxb.SearchResponseType;
import org.biopax.paxtools.io.pathwayCommons.PathwayCommons2Client;
import org.biopax.paxtools.io.pathwayCommons.util.PathwayCommonsException;
import org.biopax.paxtools.model.Model;
import org.eclipse.jface.dialogs.MessageDialog;
import org.gvt.ChisioMain;
import org.gvt.gui.AbstractQueryParamDialog;
import org.gvt.gui.ItemSelectionDialog;
import org.gvt.gui.StringInputDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Ozgun Babur
 *
 */
public class QueryPCPathwaysAction extends QueryPCAction
{
	String keyword;
	
	String pathwayID;

	public QueryPCPathwaysAction(ChisioMain main)
	{
		super(main, "Query Pathways", false);
	}

	public void run()
	{
		try
		{
			StringInputDialog dialog = new StringInputDialog(main.getShell(), "Search Pathways",
				"Enter a keyword for pathway name", keyword);

			keyword = dialog.open();

			if (keyword == null || keyword.trim().length() == 0)
			{
				return;
			}

			keyword = keyword.trim();

			main.lockWithMessage("Querying Pathway Commons ...");
			PathwayCommons2Client pc2 = new PathwayCommons2Client();
			pc2.setType("Pathway");
			SearchResponseType resp = pc2.find(keyword);
			main.unlock();

			List<Holder> holders = extractResultFromServResp(resp);
			Collections.sort(holders);

			List<String> names = getNamesList(holders);
			ItemSelectionDialog isd = new ItemSelectionDialog(main.getShell(),
				500, "Result Pathways", "Select Pathway to Get", names, null,
				false, true, null);

			String selected = isd.open();

			if (selected == null) return;
			
			pathwayID = getIDList(holders).get(names.indexOf(selected));

			execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			MessageDialog.openError(main.getShell(), "Error",
				"An error occured during querying:\n" + e.getMessage());
		}
		finally
		{
			main.unlock();
			pathwayID = null;
		}

	}
	
	private List<Holder> extractResultFromServResp(SearchResponseType resp)
	{
		List<Holder> holders = new ArrayList<Holder>();
		for (SearchHitType hit : resp.getSearchHit())
		{
			holders.add(new Holder(hit.getName().iterator().next(), hit.getUri()));
		}
		return holders;
	}
	
	private List<String> getNamesList(List<Holder> holders)
	{
		List<String> names = new ArrayList<String>(holders.size());
		for (Holder holder : holders)
		{
			names.add(holder.name);
		}
		return names;
	}
	
	private List<String> getIDList(List<Holder> holders)
	{
		List<String> ids = new ArrayList<String>(holders.size());
		for (Holder holder : holders)
		{
			ids.add(holder.id);
		}
		return ids;
	}

	@Override
	protected Model doQuery() throws PathwayCommonsException
	{
		PathwayCommons2Client pc2 = new PathwayCommons2Client();
		return pc2.get(pathwayID);
	}

	@Override
	protected AbstractQueryParamDialog getDialog()
	{
		return null;
	}

	@Override
	protected boolean canQuery()
	{
		return pathwayID != null && pathwayID.length() > 0;
	}

	private class Holder implements Comparable
	{
		String name;
		String id;

		private Holder(String name, String id)
		{
			this.name = name;
			this.id = id;
		}

		@Override
		public int compareTo(Object o)
		{
			return name.compareTo(((Holder) o).name);
		}
	}
}
