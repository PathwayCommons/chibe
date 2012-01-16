package org.gvt.action;

import org.biopax.paxtools.io.pathwayCommons.PathwayCommons2Client;
import org.biopax.paxtools.model.Model;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.gui.GoIQueryParamWithEntitiesDialog;
import org.gvt.gui.NeighborhoodQueryParamWithEntitiesDialog;
import org.gvt.util.QueryOptionsPack;

import java.util.List;

/**
 * @author Ozgun Babur
 *
 */
public class QueryPCPathsBetweenAction extends Action
{
	private ChisioMain main;

	/**
	 * Dialog options are stored, in order to use next time dialog is opened.
	 */
	QueryOptionsPack options;

	public QueryPCPathsBetweenAction(ChisioMain main)
	{
		super("Query Paths Between");
		setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class, "icon/query-neighbors.png"));
		setToolTipText(getText());
		this.main = main;
		options = new QueryOptionsPack();
	}

	public void run()
	{
		try
		{
			//open dialog
			GoIQueryParamWithEntitiesDialog dialog = new GoIQueryParamWithEntitiesDialog(main, null);

			options = dialog.open(options);

			if (!options.isCancel())
			{
				options.setCancel(true);
			}
			else
			{
				return;
			}

			List<String> sourceSymbols = options.getFormattedSourceList();

			if (sourceSymbols.isEmpty()) return;

			main.lockWithMessage("Querying Pathway Commons ...");
			PathwayCommons2Client pc2 = new PathwayCommons2Client();
			pc2.setGraphQueryLimit(options.getLengthLimit());
			Model model = pc2.getPathsBetween(sourceSymbols);
			main.unlock();

			if (model != null && !model.getObjects().isEmpty())
			{
				if (main.getOwlModel() != null)
				{
					MergeAction merge = new MergeAction(main, model);
					merge.setOpenPathways(true);
					merge.setCreateNewPathway(true);
					merge.setNewPathwayName("Neighborhood");
					merge.run();
				}
				else
				{
					LoadBioPaxModelAction load = new LoadBioPaxModelAction(main, model);
					load.setOpenPathways(true);

					load.setPathwayName("Neighborhood");
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
}
