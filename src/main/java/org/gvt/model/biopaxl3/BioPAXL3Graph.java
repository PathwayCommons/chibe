package org.gvt.model.biopaxl3;

import org.apache.xmlbeans.impl.jam.mutable.MMember;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.Process;
import org.eclipse.swt.graphics.Color;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;
import org.gvt.util.EntityHolder;
import org.gvt.util.PathwayHolder;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;
import org.patika.mada.util.Representable;

import java.util.*;

/**
 * Top level compound node for biopax graph.
 * 
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class BioPAXL3Graph extends BioPAXGraph
{
	private Pathway pathway;

	private Map<Interaction, Collection<GraphObject>> interactionMap;

	/**
	 * Empty constructor.
	 */
	public BioPAXL3Graph()
	{
		this.graphType = PROCESS_DIAGRAM;
	}
	
	/**
	 * @param biopaxModel biopax graph
	 */
	public BioPAXL3Graph(Model biopaxModel)
	{
		this();
		this.biopaxModel = biopaxModel;
		this.setAsRoot();
	}

	public PathwayHolder getPathway()
	{
		if (pathway == null) return null;
		return new PathwayHolder(pathway);
	}

	public void setPathway(PathwayHolder pathway)
	{
		this.pathway = pathway.l3p;
	}

	public void setName(String name)
	{
		super.setName(name);

		if (this.pathway != null)
		{
			this.pathway.setDisplayName(name);
		}
	}

	public Map<Interaction, Collection<GraphObject>> getInteractionMap()
	{
		if (interactionMap == null)
		{
			prepareInteractionMap();
		}
		return interactionMap;
	}

	/**
	 * Maps all interactions to their related conversions, controls, hubs or pairings in this graph.
	 * this is used for finding member of a pathway.
	 */
	private void prepareInteractionMap()
	{
		interactionMap = new HashMap<Interaction, Collection<GraphObject>>();

		for (Object o : getNodes())
		{
			Node node = (Node) o;

			Interaction inter = null;

			if (node instanceof ChbConversion)
			{
				inter = ((ChbConversion) node).getConversion();
			}
			else if (node instanceof ChbControl)
			{
				inter = ((ChbControl) node).getControl();
			}
			else if (node instanceof ChbTempReac)
			{
				inter = ((ChbTempReac) node).getTemplateReaction();
			}
			else if (node instanceof Hub)
			{
				inter = ((Hub) node).getInteraction();
			}

			if (inter != null)
			{
				if (!interactionMap.containsKey(inter))
				{
					interactionMap.put(inter, new ArrayList<GraphObject>());
				}
				interactionMap.get(inter).add(node);
			}
		}

		for (Object o : getEdges())
		{
			Interaction inter = null;

			if (o instanceof NonModulatedEffector)
			{
				inter = ((NonModulatedEffector) o).getControl();
			}
			else if (o instanceof Pairing)
			{
				inter = ((Pairing) o).getInteraction();
			}

			if (inter != null)
			{
				if (!interactionMap.containsKey(inter))
				{
					interactionMap.put(inter, new ArrayList<GraphObject>());
				}
				interactionMap.get(inter).add((GraphObject) o);
			}
		}
	}

	protected void prepareEntityToNodeMap()
	{
		entityToNodeMap = new HashMap<EntityHolder, List<Node>>();

		for (Object o : getNodes())
		{
			Node node = (Node) o;

			 if (node instanceof Actor)
			{
				getRelatedNodeList(((Actor) node).getEntity()).add(node);
			}
			else if (node instanceof ChbComplex)
			{
				getRelatedNodeList(((ChbComplex) node).getEntity()).add(node);
			}
		}
	}

	private List<Node> getRelatedNodeList(EntityHolder he)
	{
		if (!entityToNodeMap.containsKey(he))
		{
			entityToNodeMap.put(he, new ArrayList<Node>());
		}
		return entityToNodeMap.get(he);
	}

	//----------------------------------------------------------------------------------------------
	// Section: Location related
	//----------------------------------------------------------------------------------------------

	public boolean fetchLayout()
	{
		assert this.pathway != null;

		return fetchLayout(this.pathway.getRDFId());
	}

	public boolean fetchLayout(String pathwayRDFID)
	{
		assert biopaxModel != null;

		boolean allLayedOut = true;

		for (Object o : getNodes())
		{
			if (o instanceof BioPAXNode)
			{
				BioPAXNode node = (BioPAXNode) o;
				allLayedOut = node.fetchLocation(pathwayRDFID) && allLayedOut;
			}
			else if (o instanceof BioPAXCompoundNode &&
				((BioPAXCompoundNode) o).getChildren().isEmpty())
			{
				BioPAXCompoundNode node = (BioPAXCompoundNode) o;
				allLayedOut = node.fetchLocation(pathwayRDFID) && allLayedOut;
			}
		}
		for (Object o : getNodes())
		{
			if (o instanceof CompoundModel)
			{
				((CompoundModel) o).calculateSizeUp();
			}
		}
		return allLayedOut;
	}

	public void recordLayout()
	{
		assert biopaxModel != null;
		assert this.pathway != null;

		for (Object o : getNodes())
		{
			if (o instanceof BioPAXNode)
			{
				BioPAXNode node = (BioPAXNode) o;
				node.recordLocation();
			}
			else if (o instanceof BioPAXCompoundNode &&
				((BioPAXCompoundNode) o).getChildren().isEmpty())
			{
				BioPAXCompoundNode node = (BioPAXCompoundNode) o;
				node.recordLocation();
			}
		}
	}

	public void forgetLayout()
	{
		assert biopaxModel != null;
		assert this.pathway != null;

		for (Object o : getNodes())
		{
			if (o instanceof BioPAXNode)
			{
				BioPAXNode node = (BioPAXNode) o;
				node.eraseLocation();
			}
			else if (o instanceof BioPAXCompoundNode &&
				((BioPAXCompoundNode) o).getChildren().isEmpty())
			{
				BioPAXCompoundNode node = (BioPAXCompoundNode) o;
				node.eraseLocation();
			}
		}
	}

	//----------------------------------------------------------------------------------------------
	// Section: Some other methods
	//----------------------------------------------------------------------------------------------

	/**
	 * Gets pathways mapped to their names.
	 * @return name -> pathway map
	 */
	public Map<String, PathwayHolder> getNameToPathwayMap()
	{
		if (biopaxModel != null)
		{
			Map<String, PathwayHolder> map = new HashMap<String, PathwayHolder>();

			for (Pathway p : biopaxModel.getObjects(Pathway.class))
			{
				PathwayHolder ph = new PathwayHolder(p);
				map.put(ph.getName(), ph);
			}
			return map;
		}
		return null;
	}
	/**
	 * Fills in the empty pathway with the biopax interactions in the graph.
	 */
	public void registerContentsToPathway()
	{
		assert this.pathway != null;

		assert pathway.getPathwayComponent().isEmpty() : "Pathway already constains something";

		for (Object o : getNodes())
		{
			if (o instanceof ChbConversion)
			{
				pathway.addPathwayComponent(((ChbConversion) o).getConversion());
			}
			else if (o instanceof ChbControl)
			{
				pathway.addPathwayComponent(((ChbControl) o).getControl());
			}
			else if (o instanceof ChbTempReac)
			{
				pathway.addPathwayComponent(((ChbTempReac) o).getTemplateReaction());
			}
			else if (o instanceof Hub)
			{
				pathway.addPathwayComponent(((Hub) o).getInteraction());
			}
		}
		for (Object o : getEdges())
		{
			if (o instanceof Pairing)
			{
				pathway.addPathwayComponent(((Pairing) o).getInteraction());
			}
			if (o instanceof NonModulatedEffector)
			{
				pathway.addPathwayComponent(((NonModulatedEffector) o).getControl());
			}
		}
	}

	public int numberOfUnemptyPathways()
	{
		int count = 0;
		for (Pathway p : biopaxModel.getObjects(org.biopax.paxtools.model.level3.Pathway.class))
		{
			if (!p.getPathwayComponent().isEmpty()) count++;
		}
		return count;
	}

	public List<String> namesOfUnemptyPathways()
	{
		List<String> list = new ArrayList<String>();
		for (Pathway p : biopaxModel.getObjects(Pathway.class))
		{
			if (!p.getPathwayComponent().isEmpty()) list.add(p.getDisplayName());
		}
		return list;
	}

	public String createGlobalPathway(String name)
	{
		Pathway p = biopaxModel.addNew(Pathway.class,
			"http://chisiobiopaxeditor/#" + System.currentTimeMillis());

		for (Interaction inter : biopaxModel.getObjects(Interaction.class))
		{
			p.addPathwayComponent(inter);
		}
		p.setDisplayName(makeUniquePathwayName(name));
		return p.getDisplayName();
	}

	public String createPathway(String name, List<String> intids)
	{
		Pathway p = biopaxModel.addNew(Pathway.class,
			"http://chisiobiopaxeditor/#" + System.currentTimeMillis());

		for (Interaction inter : biopaxModel.getObjects(Interaction.class))
		{
			if (intids.contains(inter.getRDFId()))
			{
				p.addPathwayComponent(inter);
			}
		}
		p.setDisplayName(makeUniquePathwayName(name));
		return p.getDisplayName();
	}

	public List<String> getPathwayNames()
	{
		List<String> names = new ArrayList<String>();

		for (Pathway p : biopaxModel.getObjects(Pathway.class))
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
		return names;
	}

	public String getPathwayRDFID()
	{
		if (pathway != null) return pathway.getRDFId();
		return null;
	}

	//----------------------------------------------------------------------------------------------
	// Section: Excision related
	//----------------------------------------------------------------------------------------------


	public BioPAXGraph excise(Collection<GraphObject> objects, boolean keepHighlights)
	{
		BioPAXL3Graph exGraph = new BioPAXL3Graph(this.getBiopaxModel());

		Set<GraphObject> toExcise = new HashSet<GraphObject>(objects);

		Set<GraphObject> newObjs = new HashSet<GraphObject>(toExcise);
		Set<GraphObject> processInTurn = new HashSet<GraphObject>();

		Set<String> compartmentsToExcise = new HashSet<String>();

		do
		{
			processInTurn.clear();
			processInTurn.addAll(newObjs);
			newObjs.clear();

			for (GraphObject go : new ArrayList<GraphObject>(processInTurn))
			{
				Set<GraphObject> reqs = go.getRequisites();

				for (GraphObject req : reqs)
				{
					if (!toExcise.contains(req))
					{
						newObjs.add(req);
						toExcise.add(req);

						if (req instanceof Compartment)
						{
							compartmentsToExcise.add(((Compartment) req).getName());
						}
					}
				}
			}
		}
		while(!newObjs.isEmpty());

		Map<NodeModel, NodeModel> map = new HashMap<NodeModel, NodeModel>();

		map.put(this, exGraph);

		Set<Node> toProcess = new HashSet<Node>();

		for (GraphObject go : toExcise)
		{
			if (go instanceof Node)
			{
				toProcess.add((Node) go);
			}
		}

		while(!toProcess.isEmpty())
		{
			Set<Node> unfinished = new HashSet<Node>();

			for (Node node : toProcess)
			{
				CompoundModel compound = ((NodeModel) node).getParentModel();

				if (map.containsKey(compound))
				{
					CompoundModel newRoot = (CompoundModel) map.get(compound);

					if (node instanceof Compartment)
					{
						Compartment orig = (Compartment) node;
						map.put(orig, new Compartment(orig, newRoot));
					}
					else if (node instanceof ChbComplex)
					{
						ChbComplex orig = (ChbComplex) node;
						map.put(orig, new ChbComplex(orig, newRoot));
					}
					else if (node instanceof ComplexMember)
					{
						ComplexMember orig = (ComplexMember) node;
						map.put(orig, new ComplexMember(orig, (ChbComplex) newRoot));
					}
					else if (node instanceof Actor)
					{
						Actor orig = (Actor) node;
						map.put(orig, new Actor(orig, newRoot));
					}
					else if (node instanceof ChbConversion)
					{
						ChbConversion orig = (ChbConversion) node;
						map.put(orig, new ChbConversion(orig, newRoot));
					}
					else if (node instanceof ChbTempReac)
					{
						ChbTempReac orig = (ChbTempReac) node;
						map.put(orig, new ChbTempReac(orig, newRoot));
					}
					else if (node instanceof ChbControl)
					{
						ChbControl orig = (ChbControl) node;
						map.put(orig, new ChbControl(orig, newRoot));
					}
					else if (node instanceof Hub)
					{
						Hub orig = (Hub) node;
						map.put(orig, new Hub(orig, newRoot));
					}
					else if (node instanceof ChbPathway)
					{
						ChbPathway orig = (ChbPathway) node;
						map.put(orig, new ChbPathway(orig, newRoot));
					}
					else
					{
						System.err.println("Missing: " + node);
					}

					if (keepHighlights)
					{
						map.get(node).setHighlight(node.isHighlighted());
					}
				}
				else if (compound instanceof Compartment &&
					!compartmentsToExcise.contains(((Compartment)compound).getName()))
				{
					Compartment orig = (Compartment) node;
					map.put(orig, new Compartment(orig, exGraph));

					if (keepHighlights)
					{
						map.get(node).setHighlight(node.isHighlighted());
					}
				}
				else
				{
					unfinished.add(node);
				}
			}
			toProcess = unfinished;
		}

		for (GraphObject go : toExcise)
		{
			if (go instanceof BioPAXEdge)
			{
				BioPAXEdge edge = (BioPAXEdge) go;

				assert map.containsKey(edge.getSource()) : "No source. s: " + edge.getSource() + " t: " + edge.getTarget();
				assert map.containsKey(edge.getTarget()) : "No target. s: " + edge.getSource() + " t: " + edge.getTarget();
			}

			BioPAXEdge exEdge = null;

			if (go instanceof Substrate)
			{
				exEdge = new Substrate((Substrate) go, map);
			}
			else if (go instanceof Template)
			{
				exEdge = new Template((Template) go, map);
			}
			else if (go instanceof Product)
			{
				exEdge = new Product((Product) go, map);
			}
			else if (go instanceof NonModulatedEffector)
			{
				exEdge = new NonModulatedEffector((NonModulatedEffector) go, map);
			}
			else if (go instanceof EffectorFirstHalf)
			{
				exEdge = new EffectorFirstHalf((EffectorFirstHalf) go, map);
			}
			else if (go instanceof EffectorSecondHalf)
			{
				exEdge = new EffectorSecondHalf((EffectorSecondHalf) go, map);
			}
			else if (go instanceof Pairing)
			{
				exEdge = new Pairing((Pairing) go, map);
			}
			else if (go instanceof MultiTouch)
			{
				exEdge = new MultiTouch((MultiTouch) go, map);
			}
			else if (go instanceof Member)
			{
				exEdge = new Member((Member) go, map);
			}

			if (keepHighlights && exEdge != null)
			{
				exEdge.setHighlight(go.isHighlighted());
			}
		}

		exGraph.setGraphType(PROCESS_DIAGRAM);
		return exGraph;
	}

	public BioPAXGraph excise(PathwayHolder p)
	{
		Map<Interaction, Collection<GraphObject>> intMap = getInteractionMap();

		Set<GraphObject> set = new HashSet<GraphObject>();

		for (Interaction inter : getMemberInteractions(p.l3p, null))
		{
			Collection<GraphObject> graphObjects = intMap.get(inter);
			if (graphObjects != null) // Can be null if we have omitted PPI
			{
				for (GraphObject graphObject : graphObjects)
				{
					// graphObject can be null when we ignore PPI
					if (graphObject != null)
					{
						set.add(graphObject);
					}
				}
			}
		}

		BioPAXGraph excised = excise(set);
		excised.setName(p.getName());
		excised.setPathway(p);

		return excised;
	}

	public List<String[]> getInspectable()
	{
		List<String[]> list = new ArrayList<String[]>();

		if (pathway != null)
		{
			BioPAXNode.addNamesAndTypeAndID(list, pathway);
			BioPAXNode.addDataSourceAndXrefAndComments(list, pathway);
		}

		return list;
	}

	//----------------------------------------------------------------------------------------------
	// Section: Some mining
	//----------------------------------------------------------------------------------------------

	/**
	 * Collect all interactions related to the parameter pathway.
	 * @param p pathway
	 * @return covered interactions
	 */
	private Set<Interaction> getMemberInteractions(Pathway p, Set<Pathway> pset)
	{
		Set<Interaction> set = new HashSet<Interaction>();
		if (pset == null) pset = new HashSet<Pathway>();

		for (Process comp : p.getPathwayComponent())
		{
			if (comp instanceof Interaction)
			{
				set.add((Interaction) comp);
			}
			else if (comp instanceof Pathway)
			{
				if (comp == p)
				{
					System.err.println("Pathway includes itself: " + p.getDisplayName());
				}
				else if (pset.contains(comp))
				{
					System.err.println("Pathway has cyclic reference: " + p.getDisplayName());
				}
				else
				{
					pset.add((Pathway) comp);
					set.addAll(getMemberInteractions((Pathway) comp, pset));
				}
			}
		}

		return set;
	}

	/**
	 * Gets the related states (Actors and Complexes) of the given physical entity.
	 * @param pe pysical entity
	 * @return related states
	 */
	public Set<Node> getRelatedStates(EntityHolder pe)
	{
		Set<Node> states = new HashSet<Node>();

		for (Object o : getNodes())
		{
			if (o instanceof Actor)
			{
				Actor act = (Actor) o;
				if (act.getEntity().equals(pe))
				{
					states.add(act);
				}
			}
			else if (o instanceof ChbComplex)
			{
				ChbComplex com = (ChbComplex) o;
				if (com.getEntity().equals(pe))
				{
					states.add(com);
				}
			}
		}

		return states;
	}

	/**
	 * Gets the related states (Actors and Complexes) of the given physical entities.
	 * @param entities pysical entities
	 * @return related states
	 */
	public Set<Node> getRelatedStates(Collection<EntityHolder> entities)
	{
		Set<Node> states = new HashSet<Node>();

		for (Object o : getNodes())
		{
			if (o instanceof Actor)
			{
				Actor act = (Actor) o;
				if (entities.contains(act.getEntity()))
				{
					states.add(act);
				}
			}
			else if (o instanceof ChbComplex)
			{
				ChbComplex com = (ChbComplex) o;
				if (entities.contains(com.getEntity()))
				{
					states.add(com);
				}
			}
		}

		return states;
	}

	/**
	 * Replaces complex member nodes with owner complexes in the given collection.
	 * @param objects
	 */
	public void replaceComplexMembersWithComplexes(Collection<Node> objects)
	{
		for (GraphObject go : new HashSet<GraphObject>(objects))
		{
			if (go instanceof ComplexMember)
			{
				objects.remove(go);

				ChbComplex cmp = ((ComplexMember) go).getParentComplex();
				if (!objects.contains(cmp))
				{
					objects.add(cmp);
				}
			}
		}
	}

	//----------------------------------------------------------------------------------------------
	// Section: Data representation
	//----------------------------------------------------------------------------------------------

	public void representDataOnActors(String type)
	{
		if (type == null)
		{
			return;
		}

		for (Object nm : getNodes())
		{
			if (nm instanceof BioPAXNode)
			{
				BioPAXNode node = (BioPAXNode) nm;

				Representable data = node.getRepresentableData(type);
				
				
								
				/* UK: in-house fix for differentiating proteins from other physicalEntities. 
				 * Could, and perhaps should, be done in a better way... 
				 * */
				boolean isProt = false;
				if (node instanceof Actor){
					Actor a = (Actor) node;
					EntityHolder holder = a.getEntity();
					isProt = holder.l3pe.getClass().toString().contains("tein");
					
				}

				if (data == null)
				{
					/* UK: "If entity is a protein and has no experimental data: use light blue." 
					 * This is interesting in terms of proteomics as we can not get any data on other types of molecules, 
					 * and thus what's interesting to see, immediately, is which proteins are there and which ones are modified. 
					 * 
					 * A good way to implement this feature "cleanly" would be to add a visibility filter for different types of 
					 * physicalEntities, such as proteins.	*/
					if(isProt)
						node.setColor(noDataC_Protein);
					else
						node.setColor(noDataC);
				
					node.setTooltipText(noDataText);
					node.setTextColor(Actor.DEFAULT_TEXT_COLOR);
				}
				else
				{
					if (data.alterNodeColor()) node.setColor(data.getNodeColor());
					if (data.alterTextColor()) node.setTextColor(data.getTextColor());
					if (data.alterToolTipText()) node.setTooltipText(data.getToolTipText());
				}
			}
		}
	}

	public void removeRepresentations()
	{
		for (Object nm : getNodes())
		{
			if (nm instanceof BioPAXNode)
			{
				BioPAXNode node = (BioPAXNode) nm;
				node.configFromModel();
			}
		}
	}
}
