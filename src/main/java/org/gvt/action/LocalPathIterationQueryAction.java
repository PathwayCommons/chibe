package org.gvt.action;

import org.biopax.paxtools.model.Model;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.gvt.ChisioMain;
import org.gvt.gui.ItemSelectionDialog;
import org.gvt.model.BioPAXGraph;
import org.patika.mada.algorithm.LocalPathIterationQuery;
import org.patika.mada.algorithm.LocalPoIQuery;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;

import java.util.*;

/**
 * This class creates the action for opening path selection dialog after
 * running LocalPathIterationQuery and then upon selecting a path displaying
 * the result by running PoI.
 *
 * @author Merve Cakir
 */
public class LocalPathIterationQueryAction extends Action
{
	ChisioMain main;

	/**
	 * Constructor
	 */
	public LocalPathIterationQueryAction(ChisioMain main)
	{
		super("Path Iteration ...");
		setToolTipText(getText());

		this.main = main;
	}

	public void run()
	{
		LocalPathIteration();
	}

	public void LocalPathIteration()
	{
		Model owlModel = this.main.getBioPAXModel();

		if (owlModel == null)
		{
			MessageDialog.openError(main.getShell(), "Error!",
				"Load or query a BioPAX model first!");

			return;
		}

		BioPAXGraph rootGraph = BioPAXGraph.newInstance(main.getBioPAXModel());

		LocalPathIterationQuery lpi = new LocalPathIterationQuery(rootGraph);

		// Run BFS from all nodes in the graph and store the result in a set.
		Set<LocalPathIterationQuery.NodePair> result = lpi.run();

		// Convert NodePair set into a sorted array
		LocalPathIterationQuery.NodePair[] resultArray =
			new LocalPathIterationQuery.NodePair[result.size()];
		result.toArray(resultArray);
		Arrays.sort(resultArray);

		// This set will store NodePairs that will be displayed in the dialog
		List<LocalPathIterationQuery.NodePair> realResult =
			new ArrayList<LocalPathIterationQuery.NodePair>();

		/**
		 * Selection of pairs that will be displayed in the dialog. First
		 * criteria is showing all longest paths. If the number of longest
		 * paths is smaller than 10, then the following paths will also be added
		 * till reaching size of 10.
		 */

		int max = 0;

		for (LocalPathIterationQuery.NodePair nodePair : resultArray)
		{
			if (nodePair.getCurrentShortestPath() > max)
			{
				max = nodePair.getCurrentShortestPath();
			}
		}

		int i = resultArray.length - 1;
		while (resultArray[i].getCurrentShortestPath() == max)
		{
			realResult.add(resultArray[i]);
			i--;
		}

		int j = realResult.size() + 1;
		while (realResult.size() < 10)
		{
			realResult.add(resultArray[resultArray.length - j]);
			j++;
		}

		// Store the NodePair's string types
		ArrayList<String> stringResult = new ArrayList<String>();

		// Map between NodePair and its corresponding string
		Map<String, LocalPathIterationQuery.NodePair> nodeToString =
			new HashMap<String, LocalPathIterationQuery.NodePair>();

		// Convert NodePair into string
		for (LocalPathIterationQuery.NodePair nodePair : realResult)
		{
			String key = nodePair.toString();
			if (!stringResult.contains(key))
			{
				stringResult.add(key);
				nodeToString.put(key, nodePair);
			}
		}

		// Open the dialog which will display the paths obtained from query
		ItemSelectionDialog dialog = new ItemSelectionDialog(main.getShell(),
			300,
			"Path Selection Dialog",
			"Select one of the longest paths to visualize",
			stringResult,
			new ArrayList<String>(),
			false,
			true,
			null);

		dialog.setDoSort(false);
		dialog.open();

		if (dialog.isCancelled())
		{
			return;
		}

		List<String> selectedString = dialog.getSelectedItems();

		// Find the NodePair selected from the path selection dialog
		LocalPathIterationQuery.NodePair selectedNodePair =
			nodeToString.get(selectedString.get(0));

		// Get the source node of PoI
		Set<Node> sourceSet = new HashSet<Node>();
		sourceSet.addAll(rootGraph.getRelatedStates(selectedNodePair.getNodeA()));

		// Get the target node of PoI
		Set<Node> targetSet = new HashSet<Node>();
		targetSet.addAll(rootGraph.getRelatedStates(selectedNodePair.getNodeB()));

		// PoI will be run from the first node in NodePair to the second one
		// with shortest+0 length limit and not strict type
		LocalPoIQuery poi = new LocalPoIQuery(sourceSet, targetSet, LocalPoIQuery.SHORTEST_PLUS_K,
			0, false);
		Set<GraphObject> resultSet = poi.run();

		if (resultSet.size() == 0)
		{
			MessageDialog.openWarning(main.getShell(), "No result!",
				"No path can be found with specified parameters");
		}
		// Result of PoI will be displayed in new niew
		else
		{
			BioPAXGraph pathwayGraph = rootGraph.excise(resultSet);
			pathwayGraph.setName("Path Iteration");
			main.createNewTab(pathwayGraph);
			new CoSELayoutAction(main).run();

			resultSet = pathwayGraph.getCorrespMember(resultSet);

			for (GraphObject go : resultSet)
			{
				go.setHighlight(true);
			}
		}
	}
}
