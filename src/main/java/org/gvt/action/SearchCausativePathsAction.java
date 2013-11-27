package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.gvt.ChisioMain;
import org.gvt.gui.ItemSelectionRunnable;
import org.gvt.util.EntityHolder;
import org.gvt.gui.ItemSelectionDialog;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.EntityAssociated;
import org.patika.mada.algorithm.AlgoRunner;
import org.patika.mada.graph.Node;
import org.patika.mada.util.CausativePath;
import org.patika.mada.util.ExperimentData;
import org.patika.mada.util.Path;

import java.util.*;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class SearchCausativePathsAction extends Action
{
	private ChisioMain main;

	private Set<Node> targets;

	private BioPAXGraph pathwayGraph;

	/**
	 * specifies if the causality search will be performed on the subject graph or current view
	 * graph.
	 */
	private boolean globalSearch;

	/**
	 * Path ids to real paths.
	 */
	private Map<String, CausativePath> idMap;

	/**
	 * Constructor
	 * @param main main application
	 * @param globalSearch search in the global (background) graph or only in the current view
	 */
	public SearchCausativePathsAction(ChisioMain main, boolean globalSearch)
	{
		super("Search Causative Paths" + (globalSearch ? " In Entire Model" : ""));
		this.setToolTipText(this.getText());
		this.main = main;
		this.globalSearch = globalSearch;
	}

	public SearchCausativePathsAction(ChisioMain main, Set<Node> targets, boolean globalSearch)
	{
		this(main, globalSearch);
		this.targets = targets;
	}

	public void run()
	{
		if (globalSearch && main.getBioPAXModel() == null) return;

		if (!globalSearch && main.getPathwayGraph() == null) return;

		if (main.getExperimentDataManager(ExperimentData.EXPRESSION_DATA) == null)
		{
			MessageDialog.openError(main.getShell(), "No Expression Data Loaded!",
				"Load experiment data to use this feature.");

			return;
		}

		if (!main.getExperimentDataManager(ExperimentData.EXPRESSION_DATA).isInCompareMode())
		{
			MessageDialog.openError(main.getShell(), "Wrong Data Selection!",
				"Causative paths can only be searched in data comparison mode.\n" +
				"Go to \"Data Selection\" and compare some of the loaded experiments.");

			return;
		}

		BioPAXGraph graph = globalSearch ? BioPAXGraph.newInstance(main.getBioPAXModel()) :
			main.getPathwayGraph();

		if (targets == null)
		{
			targets = new HashSet<Node>();
			List selectedModel = main.getSelectedModel();

			if (selectedModel != null)
			{
				for (Object o : selectedModel)
				{
					if (o instanceof Node)
					{
						targets.add((Node) o);
					}
				}

				if (!targets.isEmpty() && globalSearch)
				{
					Map<EntityHolder, List<Node>> entityToNodeMap = graph.getEntityToNodeMap();

					ArrayList<Node> actors = new ArrayList<Node>(targets);
					targets.clear();

					for (Node actor : actors)
					{
						Node corresponding = (Node) actor.getLabel(BioPAXGraph.EXCISED_FROM);

						if (corresponding instanceof EntityAssociated)
						{
							EntityHolder entity = ((EntityAssociated) corresponding).getEntity();
							List<Node> nodeList = entityToNodeMap.get(entity);

							for (Node node : nodeList)
							{
								targets.add(node);
							}
						}
					}
				}
			}
		}


		// Prepare and run the algorithm

		int limit = 10;
		int k = 0;
		int t = 0;

		if (targets.isEmpty()) targets = null;

		Map<Node, Map<Integer, List<CausativePath>>> allMap =
			AlgoRunner.searchCausativePaths(graph, targets, limit, t, k);

		// Search the paths map for each target and
		// prepare the result paths to visualize

		List<String> pathIDs = new ArrayList<String>();
		idMap = new HashMap<String, CausativePath>();

		// Collect all paths in a list to be used in excising the original graph and displaying the
		// merge graph

		List<Path> paths = new ArrayList<Path>();

		for (Map<Integer, List<CausativePath>> integerListMap : allMap.values())
		{
			for (List<CausativePath> list : integerListMap.values())
			{
				paths.addAll(list);
			}
		}

		// Open merge graph of result pathways in a new view

		if (!paths.isEmpty() && globalSearch)
		{
			pathwayGraph = graph.excise(paths, false, true);
			pathwayGraph.setName("Causative Paths");
			main.createNewTab(pathwayGraph);
			new CoSELayoutAction(main).run();
			
			new ColorWithExperimentAction(main, pathwayGraph, ExperimentData.EXPRESSION_DATA).run();

			for (Path path : paths)
			{
				path.replaceElements(pathwayGraph);
			}
		}

		// If this is a local search then we will show result on the same result

		if (!globalSearch)
		{
			pathwayGraph = graph;
		}

		for (Node target : allMap.keySet())
		{
			Map<Integer, List<CausativePath>> pathsMap = allMap.get(target);

			if (pathsMap != null)
			{
				for (int i = 1; i <= limit; i++)
				{
					List<CausativePath> pathList = pathsMap.get(i);

					if (pathList != null)
					{
						for (CausativePath path : pathList)
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
			dialog.setDoSort(false);
			Object lastItem = dialog.open();

			if (lastItem == null || !lastItem.equals(ItemSelectionDialog.NONE))
			{
				dialog.runAsIfSelected(ItemSelectionDialog.NONE);
			}
		}
		else
		{
			MessageDialog.openInformation(main.getShell(), "No results!",
				"No causative paths found.");
		}

		targets = null;
		pathwayGraph = null;
	}

	private class Runner implements ItemSelectionRunnable
	{
		private String lastID;

		public void run(Collection selectedTerms)
		{
			if (selectedTerms.isEmpty())
			{
				return;
			}

			String id = selectedTerms.iterator().next().toString();

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
				idMap.get(lastID).removeInferenceData();
				idMap.get(lastID).highlight(false);
			}

			if (id.equals(ItemSelectionDialog.NONE))
			{
				if (pathwayGraph.getLastAppliedColoring() != null)
				{
					pathwayGraph.representDataOnActors(pathwayGraph.getLastAppliedColoring());
				}
				else
				{
					pathwayGraph.removeRepresentations();
				}
				lastID = null;
				return;
			}

			idMap.get(id).associateInferenceData();
			idMap.get(id).highlight(true);

			pathwayGraph.representDataOnActors(CausativePath.INFERENCE_DATA_KEY);

			lastID = id;
		}
	}
}