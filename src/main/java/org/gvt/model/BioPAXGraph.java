package org.gvt.model;

import org.biopax.paxtools.model.Model;
import org.eclipse.swt.graphics.Color;
import org.gvt.util.EntityHolder;
import org.gvt.util.PathwayHolder;
import org.patika.mada.graph.Graph;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;
import org.patika.mada.util.Path;
import org.patika.mada.util.XRef;

import java.util.*;

/**
 * @author Ozgun Babur
 */
public abstract class BioPAXGraph extends CompoundModel implements Graph
{
	/**
	 * The original BioPAX graph.
	 */
	protected Model biopaxModel;

	/**
	 * MECHANISTIC or SIMPLE_INTERACTION
	 */
	protected String graphType;

	/**
	 * This map is present when this graph is created by excision. It is used for keeping a link
	 * from original graph to the objects in this graph.
	 */
	protected Map<GraphObject, GraphObject> excisionMapOrigToThis;

	/**
	 * This map is present when this graph is created by excision. It is used for keeping a link
	 * from this graph objects to the objects in original graph.
	 */
	protected Map<GraphObject, GraphObject> excisionMapThisToOrig;

	protected Map<EntityHolder, List<Node>> entityToNodeMap;

	/**
	 * Transient property for editor usage.
	 */
	protected String lastAppliedColoring;

	public boolean isMechanistic()
	{
		return this.graphType.equals(PROCESS_DIAGRAM);
	}

	public Model getBiopaxModel()
	{
		return biopaxModel;
	}

	public String getGraphType()
	{
		return graphType;
	}

	public void setGraphType(String graphType)
	{
		this.graphType = graphType;
	}

	public void setBiopaxModel(Model biopaxModel)
	{
		this.biopaxModel = biopaxModel;
	}

	public String getName()
	{
		return getText();
	}

	public void setName(String name)
	{
		this.setText(name);
	}

	public String getLastAppliedColoring()
	{
		return lastAppliedColoring;
	}

	public void setLastAppliedColoring(String lastAppliedColoring)
	{
		this.lastAppliedColoring = lastAppliedColoring;
	}

	public void removeLabels(Collection labels)
	{
		for (Object o : getNodes())
		{
			Node node = (Node) o;

			for (Object label : labels)
			{
				node.removeLabel(label);
			}
		}
	}

	public String makeUniquePathwayName(String name)
	{
		List<String> names = getPathwayNames();

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

	public abstract List<String> getPathwayNames();

	public abstract String getPathwayRDFID();
	
	public abstract int numberOfUnemptyPathways();

	public abstract List<String> namesOfUnemptyPathways();

	public abstract String createGlobalPathway(String name);

	public abstract String createPathway(String name, List<String> intids);

	public abstract List<String[]> getInspectable();

	//----------------------------------------------------------------------------------------------
	// Section: Excision related
	//----------------------------------------------------------------------------------------------

	/**
	 * This is method should only be used by the excision mechanism.
	 * @param original
	 * @param member
	 */
	public void putInExcisionMap(GraphObject original, GraphObject member)
	{
		if (excisionMapOrigToThis == null)
		{
			excisionMapOrigToThis = new HashMap<GraphObject, GraphObject>();
			excisionMapThisToOrig = new HashMap<GraphObject, GraphObject>();
		}
		excisionMapOrigToThis.put(original, member);
		excisionMapThisToOrig.put(member, original);
	}

	/**
	 * This method is used when we want to find the correstponding object in this graph. The
	 * parameter is an object form the original graph. The returned object is a member of this
	 * graph.
	 * @param orig original object in the orginal (root) graph
	 * @return corresponding member in this graph
	 */
	public GraphObject getCorrespMember(GraphObject orig)
	{
		return this.excisionMapOrigToThis.get(orig);
	}

	/**
	 * Compiles a list of original graph objects from the given collection that contains members
	 * of this graph.
	 * @param origs members of original graph
	 * @return set of member objects
	 */
	public Set<GraphObject> getCorrespMember(Collection<GraphObject> origs)
	{
		Set<GraphObject> members = new HashSet<GraphObject>();

		for (GraphObject orig : origs)
		{
			GraphObject mem = getCorrespMember(orig);
			if (mem != null)
			{
				members.add(mem);
			}
		}

		return members;
	}
	/**
	 * This method is used when we want to find the correstponding object in the original graph.
	 * The parameter is an object form the this graph. The returned object is a member of the
	 * original graph.
	 * @param member member object in this graph
	 * @return corresponding original object in the root graph
	 */
	public GraphObject getCorrespOrig(GraphObject member)
	{
		return this.excisionMapThisToOrig.get(member);
	}

	/**
	 * Compiles a list of original graph objects from the given collection that contains members
	 * of this graph.
	 * @param members members of this graph
	 * @return set of original objects
	 */
	public Set<GraphObject> getCorrespOrig(Collection<GraphObject> members)
	{
		Set<GraphObject> origs = new HashSet<GraphObject>();

		for (GraphObject member : members)
		{
			GraphObject orig = getCorrespOrig(member);
			if (orig != null)
			{
				origs.add(orig);
			}
		}

		return origs;
	}

	public BioPAXGraph excise(Collection<GraphObject> objects)
	{
		return this.excise(objects, false);
	}

	public abstract BioPAXGraph excise(Collection<GraphObject> objects, boolean keepHighlights);

	/**
	 * Istead of a collection of graph objects user can pass a list of paths to get an excised merge
	 * graph.
	 * @param paths list of paths
	 * @param keepHighlights command to preserve hihglighted nodes
	 * @param whatever this parameter is used for not clashing with the other call. It is useless
	 * otherwise
	 * @return excised merge graph
	 */
	public BioPAXGraph excise(Collection<Path> paths, boolean keepHighlights, boolean whatever)
	{
		Set<GraphObject> set = new HashSet<GraphObject>();

		for (Path path : paths)
		{
			set.addAll(path.getObjects());
		}

		return excise(set, keepHighlights);
	}

	public Map<EntityHolder, List<Node>> getEntityToNodeMap()
	{
		prepareEntityToNodeMap();
		return this.entityToNodeMap;
	}

	protected abstract void prepareEntityToNodeMap();

	public Set<EntityHolder> getAllEntities()
	{
		prepareEntityToNodeMap();
		return this.entityToNodeMap.keySet();
	}

	//----------------------------------------------------------------------------------------------
	// Section: Data representation
	//----------------------------------------------------------------------------------------------

	public abstract void representDataOnActors(String type);
	
	public abstract void removeRepresentations();

	//----------------------------------------------------------------------------------------------
	// Section: Layout persistence
	//----------------------------------------------------------------------------------------------

	public abstract boolean fetchLayout();

	public abstract boolean fetchLayout(String pathwayRDFID);

	public abstract void recordLayout();

	public abstract void forgetLayout();

	//----------------------------------------------------------------------------------------------
	// Section: Entity related
	//----------------------------------------------------------------------------------------------

	/**
	 * Highlights nodes related to the given entiites.
	 * @param entities to relate nodes
	 */
	public void hihglightRelatedNodes(Collection<EntityHolder> entities)
	{
		Map<EntityHolder, List<Node>> map = getEntityToNodeMap();

		for (EntityHolder entity : entities)
		{
			if (map.containsKey(entity))
			{
				List<Node> nodes = map.get(entity);

				for (Node node : nodes)
				{
					assert this.getNodes().contains(node);
					node.setHighlight(true);
				}
			}
		}
	}

	/**
	 * Gets the physical entities related to the given xrefs.
	 * @param refs to search
	 * @return related physical entities
	 */
	public Set<EntityHolder> getRelatedEntities(Collection<XRef> refs)
	{
		Set<EntityHolder> entities = new HashSet<EntityHolder>();

		for (Object o : getNodes())
		{
			if (o instanceof EntityAssociated)
			{
				EntityAssociated node = (EntityAssociated) o;

				for (XRef xRef : node.getReferences())
				{
					if (refs.contains(xRef))
					{
						entities.add(node.getEntity());
						break;
					}
				}
			}
		}
		return entities;
	}

	/**
	 * Gets the related states (Actors and Complexes) of the given physical entity.
	 * @param pe pysical entity
	 * @return related states
	 */
	public abstract Set<Node> getRelatedStates(EntityHolder pe);
	
	/**
	 * Gets the related states (Actors and Complexes) of the given physical entities.
	 * @param entities pysical entities
	 * @return related states
	 */
	public abstract Set<Node> getRelatedStates(Collection<EntityHolder> entities);

	/**
	 * Replaces complex member nodes with owner complexes in the given collection.
	 * @param objects to find and replace complex members
	 */
	public abstract void replaceComplexMembersWithComplexes(Collection<Node> objects);

	//----------------------------------------------------------------------------------------------
	// Section: Pathway related
	//----------------------------------------------------------------------------------------------

	/**
	 * Gets pathways mapped to their names.
	 * @return name -> pathway map
	 */
	public abstract Map<String, PathwayHolder> getNameToPathwayMap();

	public abstract BioPAXGraph excise(PathwayHolder p);

	public abstract PathwayHolder getPathway();

	public abstract void setPathway(PathwayHolder p);
	
	/**
	 * Fills in the empty pathway with the biopax interactions in the graph.
	 */
	public abstract void registerContentsToPathway();

	//----------------------------------------------------------------------------------------------
	// Section: Class constants
	//----------------------------------------------------------------------------------------------

	public static final Color noDataC = new Color(null, 255, 255, 255);
	public static final Color noDataC_Protein = new Color(null, 255, 255, 200);

	public static final String noDataText = "No Data";

	/**
	 * Used for mapping the objects to their original correspondings.
	 */
	public static final String EXCISED_FROM = "EXCISED_FROM";

	// Model Tag related

	/**
	 * Used for storing layout information in the BioPAX model.
	 */
	public static final String LAYOUT_TAG = "Layout";

	public static final String MODEL_TAG_SEPARATOR = "@";

	public static final String DEPLETING_REACTION_TAG = "DEPLETING_REACTION";
	public static final String TRANSCRIPTION_TAG = "TRANSCRIPTION_TAG";

	// Graph types

	public static final String PROCESS_DIAGRAM = "PROCESS_DIAGRAM";
	public static final String SIF = "SIF";
	public static final String BASIC_SIF = "BASIC_SIF";

}
