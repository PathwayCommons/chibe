package org.gvt.action;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level3.EntityReference;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.gvt.ChisioMain;
import org.gvt.gui.ItemSelectionDialog;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.EntityAssociated;
import org.gvt.model.biopaxl2.Complex;
import org.gvt.model.biopaxl2.ComplexMember;
import org.gvt.util.BioPAXUtil;
import org.gvt.util.EntityHolder;
import org.gvt.util.PathwayHolder;
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
		if (main.getBioPAXModel() == null)
		{
			MessageDialog.openError(main.getShell(), "Error!", "No BioPAX model.");
			return;
		}

		// If no enitty list is given, then fetch from selected graph

		if (entities == null)
		{
			entities = BioPAXUtil.getEntities(main.getBioPAXModel());
		}

		Model model = main.getBioPAXModel();

		// Still no entity? Then open the list of entities and make user to select

		if (entities.isEmpty())
		{
			List<String> allEntityNames = new ArrayList<String>();
			Map<String, EntityHolder> nametoEntityMap = new HashMap<String, EntityHolder>();

			if (model.getLevel() == BioPAXLevel.L3)
			{
				for (EntityReference er : model.getObjects(EntityReference.class))
				{
					EntityHolder eh = new EntityHolder(er);
					nametoEntityMap.put(eh.getName(), eh);
				}
			}
			else if (model.getLevel() == BioPAXLevel.L2)
			{
				for (physicalEntity pe : model.getObjects(physicalEntity.class))
				{
					EntityHolder eh = new EntityHolder(pe);
					nametoEntityMap.put(eh.getName(), eh);
				}
			}

			allEntityNames.addAll(nametoEntityMap.keySet());

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
			String name = "Neighborhood of";

			int i = 0;
			for (EntityHolder entity : entities)
			{
				name += (i == 0 ? " " : ", ") + entity.getName();
				i++;
				if (i == 3) break;
			}
			if (entities.size() > i) name += ", et al.";

			PathwayHolder ph = BioPAXUtil.getPathwayOfNeighbors(entities, model, name);

			new OpenPathwaysAction(main, Collections.singletonList(ph.getName())).run();

			// Highlight the source set of nodes
			BioPAXGraph graph = main.getPathwayGraph();
			graph.hihglightRelatedNodes(entities);
		}

		this.entities = null;
	}
}