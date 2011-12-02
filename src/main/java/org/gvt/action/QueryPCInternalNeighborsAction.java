package org.gvt.action;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.io.pathwayCommons.PathwayCommonsIOHandler;
import org.biopax.paxtools.model.Model;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.gui.StringInputDialog;
import org.gvt.model.biopaxl2.Actor;
import org.gvt.model.biopaxl2.Complex;

import java.io.IOException;

/**
 * @author Ozgun Babur
 *
 */
public class QueryPCInternalNeighborsAction extends Action
{
	private ChisioMain main;

	public QueryPCInternalNeighborsAction(ChisioMain main)
	{
		super("Query Neighbors");
		setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class, "icon/query-neighbors.png"));
		setToolTipText(getText());
		this.main = main;
	}

	public void run()
	{

		String id = null;

		if (main.getViewer() != null)
		{
			for (Object o : main.getSelectedModel())
			{
				if (o instanceof Actor)
				{
					id = ((Actor) o).getEntity().getID();
				}
				else if (o instanceof Complex)
				{
					id = ((Complex) o).getEntity().getID();
				}
				if (id != null) break;
			}
		}

		if (id == null)
		{
			StringInputDialog dialog = new StringInputDialog(main.getShell(), "ID Dialog",
				"Enter BioPAX ID of PhysicalEntity", null);

			id = dialog.open().trim();
		}

		if (id != null)
		{
			try
			{
				main.lockWithMessage("Querying Internal PC Database ...");
				PathwayCommonsIOHandler ioHandler = new PathwayCommonsIOHandler(new SimpleIOHandler());

				System.out.println("Querying neighbors for " + id);
				Model resultModel = ioHandler.getNeighbors(id); // TODO: implement to query internal PC hibernate DB

				main.unlock();

				if (resultModel != null && !resultModel.getObjects().isEmpty())
				{
					String pname = id;

					if (main.getOwlModel() != null)
					{
						MergeAction merge = new MergeAction(main, resultModel);
						merge.setOpenPathways(true);
						merge.setCreateNewPathway(true);
						merge.setNewPathwayName(pname);
						merge.run();
					}
					else
					{
						LoadBioPaxModelAction load = new LoadBioPaxModelAction(main, resultModel);
						load.setOpenPathways(true);

						load.setPathwayName(pname);
						load.run();
					}
				}
				else
				{
					MessageDialog.openInformation(main.getShell(), "Not found!",
						"Nothing found!");
				}

				assert main.getAllPathwayNames().contains(id) :
					"New pathway name is not in allPathwayNames";

			}
			catch (IOException e)
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
}
