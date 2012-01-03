package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.gvt.ChisioMain;
import org.gvt.gui.ItemSelectionDialog;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.EntityAssociated;
import org.gvt.model.biopaxl2.Complex;
import org.gvt.model.biopaxl2.ComplexMember;
import org.gvt.util.EntityHolder;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;

import java.util.*;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class GetNeighborhoodOfSelectedEntityAction extends Action
{
	private ChisioMain main;

	/**
	 * Reference set to search. Only the first one that matches the db will be searched.
	 */
	private Set<EntityHolder> entities;

	public GetNeighborhoodOfSelectedEntityAction(ChisioMain main)
	{
		super("Create Pathway With Neighbors Of Selected");
		this.main = main;
	}

	public GetNeighborhoodOfSelectedEntityAction(ChisioMain main,
		Collection<EntityHolder> entities)
	{
		this(main);
		this.entities = new HashSet<EntityHolder>(entities);
	}

	public void run()
	{
		BioPAXGraph root = main.getRootGraph();

		if (root == null)
		{
			MessageDialog.openError(main.getShell(), "Error!", "No BioPAX model.");
			return;
		}

		// If no enitty list is given, then fetch from selected graph

		if (entities == null)
		{
			entities = new HashSet<EntityHolder>();

			if (main.getViewer() != null)
			{
				for (Object o : main.getSelectedModel())
				{
					if (o instanceof EntityAssociated)
					{
						entities.add(((EntityAssociated) o).getEntity());
					}
				}
			}
		}

		// Still no entity? Then open the list of entities and make user to select

		// Prepare data structures to access entities

		Map<EntityHolder, List<Node>> entityToNodeMap = root.getEntityToNodeMap();

		if (entities.isEmpty())
		{
			List<String> allEntityNames = new ArrayList<String>();
			Map<String, EntityHolder> nametoEntityMap = new HashMap<String, EntityHolder>();

			for (EntityHolder eh : entityToNodeMap.keySet())
			{
				String name = eh.getName();

				assert name != null;

				nametoEntityMap.put(name, eh);
				allEntityNames.add(name);
			}

			List<String> userSelection = new ArrayList<String>();

			ItemSelectionDialog dialog = new ItemSelectionDialog(main.getShell(), 350,
				"Entity Selection Dialog",
				"Select entities whose neighborhood is to be found",
				allEntityNames,
				userSelection,
				true, true, null);

			dialog.setMinValidSelect(1);
			dialog.setDoSort(true);
			dialog.open();

			if (!dialog.isCancelled())
			{
				for (String s : userSelection)
				{
					entities.add(nametoEntityMap.get(s));
				}
			}
		}

		if (!entities.isEmpty())
		{
			List<GraphObject> related = new ArrayList<GraphObject>();

			for (EntityHolder pe : entities)
			{
				for (Node node : entityToNodeMap.get(pe))
				{
					related.add(node);

					related.addAll(node.getUpstream());
					related.addAll(node.getDownstream());

					if (node instanceof ComplexMember)
					{
						Complex cmp = ((ComplexMember) node).getParentComplex();
						related.addAll(cmp.getUpstream());
						related.addAll(cmp.getDownstream());
					}
				}
			}

			BioPAXGraph graph = main.getRootGraph().excise(related);

			String name = "Neighborhood of";

			int i = 0;
			for (EntityHolder entity : entities)
			{
				name += (i == 0 ? " " : ", ") + entity.getName();
				i++;
				if (i == 3) break;
			}
			if (entities.size() > i) name += ", et al.";

			graph.setName(name);

			main.createNewTab(graph);

			new CoSELayoutAction(main).run();

			// Highlight the source set of nodes
			graph.hihglightRelatedNodes(entities);
		}

		this.entities = null;
	}
}