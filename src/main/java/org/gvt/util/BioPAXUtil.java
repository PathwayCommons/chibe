package org.gvt.util;

import org.biopax.paxtools.controller.Cloner;
import org.biopax.paxtools.controller.Completer;
import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.interaction;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.xref;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.query.QueryExecuter;
import org.biopax.paxtools.query.algorithm.Direction;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.biopaxl2.BioPAXL2Graph;
import org.gvt.model.biopaxl3.BioPAXL3Graph;
import org.patika.mada.util.XRef;

import java.util.*;

/**
 * This class contains some common operations related to BioPAX model.
 * @author Ozgun Babur
 */
public class BioPAXUtil
{
	public static List<String> namesOfUnemptyPathways(Model model)
	{
		List<String> list = new ArrayList<String>();

		if (model.getLevel() == BioPAXLevel.L3)
		{
			for (Pathway p : model.getObjects(Pathway.class))
			{
				PathwayHolder ph = new PathwayHolder(p);
				if (!p.getPathwayComponent().isEmpty()) list.add(ph.getName());
			}
		}
		else if (model.getLevel() == BioPAXLevel.L2)
		{
			for (pathway p : model.getObjects(pathway.class))
			{
				PathwayHolder ph = new PathwayHolder(p);
				if (!p.getPATHWAY_COMPONENTS().isEmpty()) list.add(ph.getName());
			}
		}

		return list;
	}

	public static int numberOfUnemptyPathways(Model model)
	{
		List<String> names = namesOfUnemptyPathways(model);
		if (names == null) return 0;
		return names.size();
	}


	public static PathwayHolder createGlobalPathway(Model model, String name)
	{
		PathwayHolder h = new PathwayHolder(model, name);

		if (model.getLevel() == BioPAXLevel.L3)
		{
			Pathway p = h.l3p;

			for (Interaction inter : model.getObjects(Interaction.class))
			{
				p.addPathwayComponent(inter);
			}
		}
		else if (model.getLevel() == BioPAXLevel.L2)
		{
			pathway p = h.l2p;
			for (interaction inter : model.getObjects(interaction.class))
			{
				p.addPATHWAY_COMPONENTS(inter);
			}
		}

		return h;
	}

	public static Set<String> collectIDs(Collection<BioPAXElement> eles)
	{
		Set<String> set = new HashSet<String>();
		for (BioPAXElement ele : eles)
		{
			set.add(ele.getRDFId());
		}
		return set;
	}

	public static PathwayHolder createPathway(Model model, String name, Collection<String> intids)
	{
		PathwayHolder h = new PathwayHolder(model, name);

		if (model.getLevel() == BioPAXLevel.L3)
		{
			Pathway p = h.l3p;

			for (String id : intids)
			{
				BioPAXElement ele = model.getByID(id);

				if (ele instanceof Interaction)
				{
					p.addPathwayComponent((Interaction) ele);
				}
			}

			// If any object is not covered by the pathway, add their ID to comments.

			Completer c = new Completer(SimpleEditorMap.L3);

			Set<BioPAXElement> set = new HashSet<BioPAXElement>();
			set.add(p);

			set = c.complete(set, model);

			Set<String> ids = new HashSet<String>();
			for (BioPAXElement ele : set)
			{
				ids.add(ele.getRDFId());
			}

			ids.remove(p.getRDFId());

			for (String id : intids)
			{
				if (!ids.contains(id))
				{
					p.addComment(BioPAXGraph.PATHWAY_CONTENT_TAG + BioPAXGraph.MODEL_TAG_SEPARATOR +
						id);
				}
			}
		}
		else
		{
			pathway p = h.l2p;

			for (interaction inter : model.getObjects(interaction.class))
			{
				if (intids.contains(inter.getRDFId()))
				{
					p.addPATHWAY_COMPONENTS(inter);
				}
			}
		}

		return h;
	}

	public static String makeUniquePathwayName(Model model, String name)
	{
		if (name == null) name = "unnamed pathway";
		List<String> names = getPathwayNames(model);

		if (!names.contains(name)) return name;
		String offer;

		int i = 2;
		do
		{
			offer = name + " (" + i + ")";
			i++;
		}
		while(names.contains(offer));

		return offer;
	}


	public static List<String> getPathwayNames(Model model)
	{
		if (model == null) return Collections.emptyList();

		List<String> names = new ArrayList<String>();

		if (model.getLevel() == BioPAXLevel.L3)
		{
			for (Pathway p : model.getObjects(Pathway.class))
			{
				if (p.getDisplayName() == null)
				{
					if (p.getStandardName() != null)
					{
						p.setDisplayName(p.getStandardName());
					}
					else if (!p.getName().isEmpty())
					{
						p.setDisplayName(p.getName().iterator().next());
					}
				}
				names.add(p.getDisplayName());
			}
		}
		else if (model.getLevel() == BioPAXLevel.L2)
		{
			for (pathway p : model.getObjects(pathway.class))
			{
				names.add(p.getNAME());
			}
		}

		return names;
	}

	/**
	 * Gets pathways mapped to their names.
	 * @return name -> pathway map
	 */
	public static Map<String, PathwayHolder> getNameToPathwayMap(Model model)
	{
		Map<String, PathwayHolder> map = new HashMap<String, PathwayHolder>();

		if (model.getLevel() == BioPAXLevel.L3)
		{
			for (Pathway p : model.getObjects(Pathway.class))
			{
				PathwayHolder ph = new PathwayHolder(p);
				map.put(ph.getName(), ph);
			}
		}
		else if (model.getLevel() == BioPAXLevel.L2)
		{
			for (pathway p : model.getObjects(pathway.class))
			{
				PathwayHolder ph = new PathwayHolder(p);
				map.put(ph.getName(), ph);
			}
		}
		return map;
	}


	public static PathwayHolder getPathwayOfNeighbors(Collection<EntityHolder> entHolds,
		Model model, String name)
	{
		PathwayHolder h = new PathwayHolder(model, name);

		if (h.isLevel3())
		{
			Set<BioPAXElement> eles = new HashSet<BioPAXElement>();
			for (EntityHolder entHold : entHolds)
			{
				eles.add(entHold.l3er != null ? entHold.l3er : entHold.l3pe);
			}
			Set<BioPAXElement> result =
				QueryExecuter.runNeighborhood(eles, model, 1, Direction.BOTHSTREAM);

			for (BioPAXElement ele : result)
			{
				if (ele instanceof Interaction) h.l3p.addPathwayComponent((Interaction) ele);
			}
		}
		else
		{
			for (EntityHolder entHold : entHolds)
			{
				for (interaction inter : entHold.l2pe.getAllInteractions())
				{
					h.l2p.addPATHWAY_COMPONENTS(inter);
				}
			}
		}

		return h;
	}

	/**
	 * Get the entities in the parameter biopax model.
	 */
	public static Set<EntityHolder> getEntities(Model model)
	{
		if (model == null) return Collections.<EntityHolder>emptySet();

		Set<EntityHolder> set = new HashSet<EntityHolder>();

		if (model.getLevel().equals(BioPAXLevel.L3))
		{
			for (EntityReference er : model.getObjects(EntityReference.class))
			{
				set.add(new EntityHolder(er));
			}
			if (set.isEmpty())
			{
				for (PhysicalEntity pe : model.getObjects(PhysicalEntity.class))
				{
					set.add(new EntityHolder(pe));
				}
			}
		}
		if (model.getLevel().equals(BioPAXLevel.L2))
		{
			for (physicalEntity pe : model.getObjects(physicalEntity.class))
			{
				set.add(new EntityHolder(pe));
			}
		}
		return set;
	}

	public static Model excise(Model model, PathwayHolder ph)
	{
		SimpleEditorMap editorMap = SimpleEditorMap.get(model.getLevel());
		Completer c = new Completer(editorMap);

		Set<BioPAXElement> set = new HashSet<BioPAXElement>();
		set.add(ph.getPathway());
		set = c.complete(set, model);

		Cloner cloner = new Cloner(editorMap, model.getLevel().getDefaultFactory());
		return cloner.clone(model, set);
	}

	public static Set<BioPAXElement> getContent(Collection<EntityHolder> ehs)
	{
		Set<BioPAXElement> set = new HashSet<BioPAXElement>();
		for (EntityHolder eh : ehs)
		{
			set.add(eh.getEntity());
		}
		return set;
	}

	private static final PathAccessor locAcc =
		new PathAccessor("PhysicalEntity/cellularLocation/term");

	public static List<String> getCellularLocations(Model model)
	{
		if (model.getLevel() != BioPAXLevel.L3)
			throw new IllegalArgumentException("Only level 3 is supported");

		List<String> locs = new ArrayList<String>();

		for (Object o : locAcc.getValueFromModel(model))
		{
			locs.add(o.toString());
		}

		Collections.sort(locs);
		return locs;
	}

	public static Set<BioPAXElement> getElementsAtLocations(Model model, Set<String> locs)
	{
		if (model.getLevel() != BioPAXLevel.L3)
			throw new IllegalArgumentException("Only level 3 is supported");

		Set<BioPAXElement> result = new HashSet<BioPAXElement>();
		for (PhysicalEntity pe : model.getObjects(PhysicalEntity.class))
		{
			Set found = locAcc.getValueFromBean(pe);
			found.retainAll(locs);
			if (!found.isEmpty()) result.add(pe);
		}
		return result;
	}

	public static Set<EntityHolder> getRelatedEntities(Model model, Collection<XRef> refs)
	{
		Set<String> vals = new HashSet<String>();
		for (XRef ref : refs)
		{
			vals.add(ref.getRef());
		}

		Set<EntityHolder> ehs = new HashSet<EntityHolder>();

		if (model.getLevel() == BioPAXLevel.L3)
		{
			for (EntityReference er : model.getObjects(EntityReference.class))
			{
				for (Xref xref : er.getXref())
				{
					if (vals.contains(xref.getId())) ehs.add(new EntityHolder(er));
				}
			}
		}
		else if (model.getLevel() == BioPAXLevel.L2)
		{
			for (physicalEntity pe : model.getObjects(physicalEntity.class))
			{
				for (xref ref : pe.getXREF())
				{
					if (vals.contains(ref.getID())) ehs.add(new EntityHolder(pe));
				}
			}
		}
		return ehs;
	}

	public static Set<String> getInteractionIDs(Model model)
	{
		Set<String> ids = new HashSet<String>();

		if (model.getLevel() == BioPAXLevel.L2)
		{
			for (interaction inter : model.getObjects(interaction.class))
			{
				ids.add(inter.getRDFId());
			}
		} else if (model.getLevel() == BioPAXLevel.L3)
		{
			for (Interaction inter : model.getObjects(Interaction.class))
			{
				ids.add(inter.getRDFId());
			}
		}
		return ids;
	}

}
