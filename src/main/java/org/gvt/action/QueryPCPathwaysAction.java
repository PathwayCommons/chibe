package org.gvt.action;

import cpath.service.jaxb.ServiceResponse;
import org.biopax.paxtools.io.pathwayCommons.PathwayCommons2Client;
import org.biopax.paxtools.model.Model;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.gui.ItemSelectionDialog;
import org.gvt.gui.StringInputDialog;
import org.gvt.util.QueryOptionsPack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Ozgun Babur
 *
 */
public class QueryPCPathwaysAction extends Action
{
	private ChisioMain main;

	/**
	 * Dialog options are stored, in order to use next time dialog is opened.
	 */
	QueryOptionsPack options;

	public QueryPCPathwaysAction(ChisioMain main)
	{
		super("Query Pathways");
		setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class, "icon/query-neighbors.png"));
		setToolTipText(getText());
		this.main = main;
		options = new QueryOptionsPack();
	}

	public void run()
	{
		try
		{
			StringInputDialog dialog = new StringInputDialog(main.getShell(), "Search Pathways",
				"Enter a keyword for pathway name", "");

			String keyword = dialog.open();

			if (keyword == null || keyword.trim().length() == 0)
			{
				return;
			}

			keyword = keyword.trim();

			main.lockWithMessage("Querying Pathway Commons ...");
			PathwayCommons2Client pc2 = new PathwayCommons2Client();
			pc2.setType("Pathway");
			ServiceResponse resp = pc2.find(keyword);
			main.unlock();

			List<Holder> holders = extractResultFromServResp(resp);
			Collections.sort(holders);

			List<String> names = getNamesList(holders);
			ItemSelectionDialog isd = new ItemSelectionDialog(main.getShell(),
				500, "Result Pathways", "Select Pathway to Get", names, null,
				false, true, null);

			String selected = isd.open();

			if (selected == null) return;
			
			String id = getIDList(holders).get(names.indexOf(selected));

			main.lockWithMessage("Querying Pathway Commons ...");
			pc2 = new PathwayCommons2Client();
			Model model = pc2.get(id);
			main.unlock();

			if (model != null && !model.getObjects().isEmpty())
			{
				if (main.getOwlModel() != null)
				{
					MergeAction merge = new MergeAction(main, model);
					merge.setOpenPathways(true);
					merge.run();
				}
				else
				{
					LoadBioPaxModelAction load = new LoadBioPaxModelAction(main, model);
					load.setOpenPathways(true);
					load.run();
				}
			}
			else
			{
				MessageDialog.openInformation(main.getShell(), "Not found!",
					"Nothing found!");
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
		}

	}
	
	private List<Holder> extractResultFromServResp(ServiceResponse resp)
	{
		return null; //todo
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
