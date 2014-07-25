package org.gvt.action;

import cpath.client.util.CPathException;
import cpath.service.jaxb.SearchHit;
import cpath.service.jaxb.SearchResponse;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.eclipse.jface.dialogs.MessageDialog;
import org.gvt.ChisioMain;
import org.gvt.gui.AbstractQueryParamDialog;
import org.gvt.gui.ItemSelectionDialog;
import org.gvt.gui.StringInputDialog;
import org.gvt.model.basicsif.BasicSIFGraph;
import org.patika.mada.graph.GraphObject;

import java.util.*;

/**
 * @author Ozgun Babur
 *
 */
public class QueryPCPathwaysAction extends QueryPCAction
{
	String keyword;
	String latestKeyword;
	
	String pathwayID;

	public QueryPCPathwaysAction(ChisioMain main, QueryLocation qLoc)
	{
		super(main, "Pathways With Keyword ...", false, qLoc);
	}

	public void run()
	{
        if(main.getBioPAXModel() == null || main.getBioPAXModel().getLevel().equals(BioPAXLevel.L3))
        {
            try
            {
                StringInputDialog dialog = new StringInputDialog(main.getShell(), "Query Pathways",
                    "Enter a keyword for pathway name", keyword != null ? keyword : latestKeyword,
                    "Find pathways related to the specified keyword");

                keyword = dialog.open();

                if (keyword == null || keyword.trim().length() == 0)
                {
                    return;
                }

				latestKeyword = keyword;

                keyword = keyword.trim().toLowerCase();

                main.lockWithMessage("Querying Pathway Commons ...");

				SearchResponse resp = getPCSearchQuery().
					typeFilter("Pathway").
					queryString("name:" + keyword).
					result();

                main.unlock();

				if (resp != null)
				{
					List<Holder> holders = extractResultFromServResp(resp, keyword);

					ItemSelectionDialog isd = new ItemSelectionDialog(main.getShell(),
						500, "Result Pathways", "Select Pathway to Get", holders, null,
						false, true, null);
					isd.setDoSort(false);

					Object selected = isd.open();

					if (selected == null) return;

					pathwayID = ((Holder) selected).getID();

					execute();
				}
				else
				{
					MessageDialog.openInformation(main.getShell(), "No result",
						"Could not find any match.\n");
				}
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
                keyword = null;
            }
        }
        else
        {
            MessageDialog.openError(main.getShell(), "Incompatible Levels","This query is only applicable to Level 3 models.");
        }
	}
	
	private List<Holder> extractResultFromServResp(SearchResponse resp, String keyword)
	{
		List<Holder> holders = new ArrayList<Holder>();

		for (SearchHit hit : resp.getSearchHit())
		{
			Holder h = new Holder(hit);
			if (h.getID() != null && h.getName() != null) holders.add(h);
		}
		return holders;
	}
	
	private List<String> getIDList(List<Holder> holders)
	{
		List<String> ids = new ArrayList<String>(holders.size());
		for (Holder holder : holders)
		{
			ids.add(holder.getID());
		}
		return ids;
	}

	private Set<String> getKeywords(String keyword)
	{
		Set<String> set = new HashSet<String>();
		for (String s : keyword.split(" "))
		{
			s = s.trim();
			if (s.length() > 1) set.add(s);
		}
		return set;
	}
	
	@Override
	protected Model doQuery() throws CPathException
	{
		return getPCGetQuery().sources(new String[]{pathwayID}).result();
	}

	@Override
	protected Set<BioPAXElement> doFileQuery(Model model)
	{
		return findInFile(model, Collections.singleton(pathwayID));
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
		return pathwayID != null && pathwayID.length() > 0;
	}

	private class Holder implements Comparable
	{
		SearchHit hit;

		private Holder(SearchHit hit)
		{
			this.hit = hit;
		}

		@Override
		public int compareTo(Object o)
		{
			return hit.getName().compareTo(((Holder) o).hit.getName());
		}

		@Override
		public int hashCode()
		{
			return hit.getUri().hashCode();
		}

		@Override
		public boolean equals(Object o)
		{
			if (o instanceof Holder)
			{
				Holder h = (Holder) o;
				return hit.getUri().equals(h.hit.getUri());
			}
			return false;
		}

		public String getDataSource()
		{
			if (!hit.getDataSource().isEmpty())
			{
				String s = hit.getDataSource().iterator().next();
				if (s.contains("/") && !s.endsWith("/")) s = s.substring(s.lastIndexOf("/") + 1);
				return s;
			}
			return null;
		}

		public String getOrganism()
		{
			if (!hit.getOrganism().isEmpty())
			{
				String s = hit.getOrganism().iterator().next();
				if (s.contains("/") && !s.endsWith("/")) s = s.substring(s.lastIndexOf("/") + 1);
				if (s.equals("9606")) s = "Human";
				return s;
			}
			return null;
		}

		public String getID()
		{
			return hit.getUri();
		}

		public String getName()
		{
			return hit.getName();
		}

		@Override
		public String toString()
		{
			String s = hit.getName();

			String d = getDataSource();
			if (d != null) s += " [" + d + "]";

			String o = getOrganism();
			if (o != null) s += " [" + o + "]";

			return s;
		}
	}
}
