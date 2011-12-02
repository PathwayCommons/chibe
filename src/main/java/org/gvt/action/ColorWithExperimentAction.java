package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.gui.ItemSelectionDialog;
import org.gvt.gui.ItemSelectionRunnable;
import org.gvt.model.BioPAXGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class ColorWithExperimentAction extends Action
{
	private ChisioMain main;

	private String type;

	private BioPAXGraph graph;

	public ColorWithExperimentAction(ChisioMain main)
	{
		super("Use Data Colors");
		setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class, "icon/color-experiment.png"));
		setToolTipText(getText());
		this.main = main;
	}

	public ColorWithExperimentAction(ChisioMain main, BioPAXGraph graph, String type)
	{
		this(main);
		this.type = type;
		this.graph = graph;
	}

	public void run()
	{
		if (graph == null)
		{
			graph = main.getPathwayGraph();

			if (graph == null)
			{
				return;
			}
		}
		if (type == null)
		{
			Set<String> types = main.getLoadedExperimentTypes();

			if (types.isEmpty())
			{
				MessageDialog.openError(main.getShell(),
					"Error!", "Load experiment data first.");

				return;
			}

			if (types.size() == 1)
			{
				type = types.iterator().next();
			}
			else
			{
				ArrayList<String> selectedItems = new ArrayList<String>();

				if (graph.getLastAppliedColoring() != null)
				{
					selectedItems.add(graph.getLastAppliedColoring());
				}

				ItemSelectionDialog dialog = new ItemSelectionDialog(main.getShell(),
					200,
					"Experiment Type Selection Dialog",
					"Select experiment type",
					new ArrayList<String>(types),
					selectedItems,
					false,
					false,
					new ColoringSelectionRunnable());

				dialog.setUpdateUponSelection(true);
				dialog.open();
			}
		}

		if (type != null)
		{
			ArrayList<String> typeInList = new ArrayList<String>();
			typeInList.add(type);
			new ColoringSelectionRunnable().run(typeInList);
			graph.setLastAppliedColoring(type);
		}

		// We do not want to remember this in the next run
		type = null;
		graph = null;
	}

	private class ColoringSelectionRunnable implements ItemSelectionRunnable
	{
		public void run(Collection<String> selectedTerms)
		{
			assert selectedTerms.size() == 1;

			String type = selectedTerms.iterator().next();

			if (type.equals(ItemSelectionDialog.NONE))
			{
				// Check for unnecessary un-coloring
				if (graph.getLastAppliedColoring() != null)
				{
					new RemoveExperimentColorAction(main, graph).run();
					return;
				}
			}

			graph.representDataOnActors(type);
			graph.setLastAppliedColoring(type);
		}
	}
}
