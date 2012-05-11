package org.gvt.action;

import org.biopax.paxtools.model.Model;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.MessageBox;
import org.gvt.ChisioMain;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.CompoundModel;
import org.gvt.util.PathwayHolder;

import java.util.ArrayList;

/**
 * Action for closing the currently open tab.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class DeletePathwayAction extends Action
{
	private ChisioMain main;
	private boolean allOpenPathways;

	/**
	 * Constructor
	 */
	public DeletePathwayAction(ChisioMain main, boolean allOpenPathways)
	{
		super(allOpenPathways ? "Delete All Open Pathways" : "Delete Pathway");

		if (allOpenPathways)
		{
			setImageDescriptor(ImageDescriptor.createFromFile(getClass(), "../icon/pathway-delete-all.png"));
		}
		else
		{
			setImageDescriptor(ImageDescriptor.createFromFile(getClass(), "../icon/pathway-delete.png"));
		}

		this.setToolTipText(getText());
		this.main = main;
		this.allOpenPathways = allOpenPathways;
	}

	public DeletePathwayAction(ChisioMain main)
	{
		this(main, false);
	}

	public void run()
	{
		CTabItem tab = main.getSelectedTab();

		if (tab != null)
		{
			CompoundModel root = (CompoundModel) main.getTabToViewerMap().get(tab).getContents().
				getModel();

			if (root instanceof BioPAXGraph)
			{
				BioPAXGraph graph = (BioPAXGraph) root;

				if (graph.getPathway().hasEdge())
				{
					MessageDialog.openError(main.getShell(), "Cannot delete pathway",
						"Pathway is either a participant of an interaction,\nor controller of, " +
							"or controlled by a control.\nNot safe to delete.");
					return;
				}
			}

			String s = allOpenPathways ? "all open pathways" : "the pathway";
			MessageBox box = new MessageBox(main.getShell(), SWT.YES | SWT.NO | SWT.CANCEL);
			box.setText("Confirm");
			box.setMessage("Delete "+ s +"?\n" +
				"Contents will not be deleted from the model!");

			int answer = box.open();
			
			if (answer == SWT.YES)
			{
				if (allOpenPathways)
				{
					for (CTabItem tabItem : new ArrayList<CTabItem>(
						main.getTabToViewerMap().keySet()))
					{
						deletePathway(tabItem);
					}
				}
				else
				{
					deletePathway(tab);
				}
			}
		}
	}

	private void deletePathway(CTabItem tab)
	{

		ScrollingGraphicalViewer viewer = main.getTabToViewerMap().get(tab);

		main.closeTab(tab, false);

		CompoundModel root = (CompoundModel) viewer.getContents().getModel();

		if (root instanceof BioPAXGraph)
		{
			BioPAXGraph graph = (BioPAXGraph) root;

			if (graph.isMechanistic())
			{
				graph.forgetLayout();
				Model model = graph.getBiopaxModel();
				PathwayHolder p = graph.getPathway();
				p.removeFromModel(model);

				main.getAllPathwayNames().remove(p.getName());
			}
		}
	}
}