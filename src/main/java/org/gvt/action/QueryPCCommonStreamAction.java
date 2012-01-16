package org.gvt.action;

import org.biopax.paxtools.io.pathwayCommons.PathwayCommons2Client;
import org.biopax.paxtools.model.Model;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.gui.AbstractQueryParamDialog;
import org.gvt.gui.CommonStreamQueryParamDialog;
import org.gvt.gui.CommonStreamQueryParamWithEntitiesDialog;
import org.gvt.gui.GoIQueryParamWithEntitiesDialog;
import org.gvt.util.QueryOptionsPack;

import java.io.IOException;
import java.util.List;

/**
 * @author Ozgun Babur
 *
 */
public class QueryPCCommonStreamAction extends Action
{
	private ChisioMain main;

	/**
	 * Dialog options are stored, in order to use next time dialog is opened.
	 */
	QueryOptionsPack options;

	public QueryPCCommonStreamAction(ChisioMain main)
	{
		super("Query Common Stream");
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
			CommonStreamQueryParamWithEntitiesDialog dialog =
				new CommonStreamQueryParamWithEntitiesDialog(main, null);

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
			Model model = pc2.getCommonStream(sourceSymbols, (options.isDirection() ==
				AbstractQueryParamDialog.DOWNSTREAM ?
					PathwayCommons2Client.STREAM_DIRECTION.DOWNSTREAM :
					PathwayCommons2Client.STREAM_DIRECTION.UPSTREAM));
			main.unlock();

			if (model != null && !model.getObjects().isEmpty())
			{
				if (main.getOwlModel() != null)
				{
					MergeAction merge = new MergeAction(main, model);
					merge.setOpenPathways(true);
					merge.setCreateNewPathway(true);
					merge.setNewPathwayName("Common stream");
					merge.run();
				}
				else
				{
					LoadBioPaxModelAction load = new LoadBioPaxModelAction(main, model);
					load.setOpenPathways(true);

					load.setPathwayName("Common stream");
					load.run();
				}
			}
			else
			{
				MessageDialog.openInformation(main.getShell(), "Not found!", "Nothing found!");
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
