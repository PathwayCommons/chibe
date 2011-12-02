package org.gvt.action;


import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.gvt.ChisioMain;
import org.gvt.gui.ItemSelectionDialog;
import org.gvt.gui.ItemSelectionRunnable;
import org.gvt.model.BioPAXGraph;
import org.patika.mada.algorithm.AlgoRunner;
import org.patika.mada.graph.Node;
import org.patika.mada.util.Path;

import java.util.*;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class HighlightPathsBetweenSelectedAction extends Action
{
	private ChisioMain main;

	/**
	 * Path ids to real paths.
	 */
	private Map<String, Path> idMap;

	/**
	 * Constructor
	 */
	public HighlightPathsBetweenSelectedAction(ChisioMain main)
	{
		super("Highlight Paths Between Selected");
		setToolTipText(getText());
		this.main = main;
	}

	public void run()
	{
		if (main.getViewer() == null)
		{
			return;
		}

		BioPAXGraph graph = main.getPathwayGraph();

		if (graph == null)
		{
			MessageDialog.openError(main.getShell(), "Not applicable!",
				"This feature works only for process views.");

			return;
		}

		List<Node> selectedNodes = new ArrayList<Node>();

		ScrollingGraphicalViewer viewer = main.getViewer();

		if (viewer == null) return;

		// Iterates selected objects
		Iterator selectedObjects = ((IStructuredSelection) viewer.getSelection()).iterator();

		while (selectedObjects.hasNext())
		{
			Object model = ((EditPart)selectedObjects.next()).getModel();

			if (model instanceof Node)
			{
				selectedNodes.add((Node) model);
			}
		}

		// Prepare and run the algorithm

		int limit = 10;

		Map<Node, Map<Integer, List<Path>>> allMap =
			AlgoRunner.searchPathsBetween(graph, selectedNodes, limit);

		// Search the paths map for each target and
		// prepare the result paths to visualize

		List<String> pathIDs = new ArrayList<String>();
		idMap = new HashMap<String, Path>();

		for (Node target : allMap.keySet())
		{
			Map<Integer, List<Path>> pathsMap = allMap.get(target);

			if (pathsMap != null)
			{
				for (int i = 1; i <= limit; i++)
				{
					List<Path> pathList = pathsMap.get(i);

					if (pathList != null)
					{
						for (Path path : pathList)
						{
							String id = path.toString();

							if (pathIDs.contains(id))
							{
								int j = 2;
								String nextID;
								do
								{
									nextID = id + " (" + (j++) + ")";
								}
								while (pathIDs.contains(nextID));
								id = nextID;
							}

							pathIDs.add(id);
							idMap.put(id, path);
						}
					}
				}
			}
		}

		if (!pathIDs.isEmpty())
		{
			// Open a dialog so that user selected the specific path to visualize

			ItemSelectionDialog dialog = new ItemSelectionDialog(main.getShell(),
				250,
				"Path Selection Dialog",
				"Select path to visualize",
				pathIDs, new ArrayList<String>(), false, false, new Runner());

			dialog.setUpdateUponSelection(true);
			String lastItem = dialog.open();

			// Below lines are responsible for clearing any highlighting after closing the dialog.
			// But we do not want it to disappear. So this is disabled.
//			if (!lastItem.equals(ItemSelectionDialog.NONE))
//			{
//				dialog.runAsIfSelected(ItemSelectionDialog.NONE);
//			}
		}
		else
		{
			MessageDialog.openInformation(main.getShell(), "No results!",
				"No directed paths found between selected nodes.");
		}
	}

	private class Runner implements ItemSelectionRunnable
	{
		private String lastID;

		public void run(Collection<String> selectedTerms)
		{
			if (selectedTerms.isEmpty())
			{
				return;
			}

			String id = selectedTerms.iterator().next();

			run(id);
		}

		public void run(String id)
		{
			if (id.equals(lastID))
			{
				return;
			}

			if (lastID != null)
			{
				idMap.get(lastID).highlight(false);
			}

			if (id.equals(ItemSelectionDialog.NONE))
			{
				lastID = null;
				return;
			}

			idMap.get(id).highlight(true);

			lastID = id;
		}
	}
}