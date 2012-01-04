package org.gvt.model.biopaxl2;

import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.*;
import org.eclipse.swt.graphics.Color;
import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.EntityAssociated;
import org.gvt.util.EntityHolder;
import org.gvt.util.PathwayHolder;
import org.patika.mada.graph.Graph;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;
import org.patika.mada.util.Path;
import org.patika.mada.util.Representable;
import org.patika.mada.util.XRef;

import java.util.*;

/**
 * Top level compound node for biopax graph.
 * 
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class BioPAXL2Graph extends BioPAXGraph
{
	private pathway pathway;

	private Map<interaction, Collection<GraphObject>> interactionMap;

	/**
	 * Empty constructor.
	 */
	public BioPAXL2Graph()
	{
		this.graphType = PROCESS_DIAGRAM;
	}
	
	/**
	 * @param biopaxModel biopax graph
	 */
	public BioPAXL2Graph(Model biopaxModel)
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
		this.pathway = pathway.l2p;
	}

	public void setName(String name)
	{
		super.setName(name);

		if (this.pathway != null) pathway.setNAME(name);
	}

	public Map<interaction, Collection<GraphObject>> getInteractionMap()
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
		interactionMap = new HashMap<interaction, Collection<GraphObject>>();

		for (Object o : getNodes())
		{
			Node node = (Node) o;

			interaction inter = null;

			if (node instanceof Conversion)
			{
				inter = ((Conversion) node).getConversion();
			}
			else if (node instanceof Control)
			{
				inter = ((Control) node).getControl();
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
			interaction inter = null;

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
			else if (node instanceof Complex)
			{
				getRelatedNodeList(((Complex) node).getEntity()).add(node);
			}
		}
	}

	private List<Node> getRelatedNodeList(EntityHolder eh)
	{
		if (!entityToNodeMap.containsKey(eh))
		{
			entityToNodeMap.put(eh, new ArrayList<Node>());
		}
		return entityToNodeMap.get(eh);
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
				((CompoundModel) o).calculateSize();
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

			for (pathway p : biopaxModel.getObjects(pathway.class))
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

		assert pathway.getPATHWAY_COMPONENTS().isEmpty() : "Pathway already constains something";

		for (Object o : getNodes())
		{
			if (o instanceof Conversion)
			{
				pathway.addPATHWAY_COMPONENTS(((Conversion) o).getConversion());
			}
			else if (o instanceof Control)
			{
				pathway.addPATHWAY_COMPONENTS(((Control) o).getControl());
			}
			else if (o instanceof Hub)
			{
				pathway.addPATHWAY_COMPONENTS(((Hub) o).getInteraction());
			}
		}
		for (Object o : getEdges())
		{
			if (o instanceof Pairing)
			{
				pathway.addPATHWAY_COMPONENTS(((Pairing) o).getInteraction());
			}
		}	
	}

	public boolean modelConstainsUnemptyPathway()
	{
		for (pathway p : biopaxModel.getObjects(pathway.class))
		{
			if (!pathway.getPATHWAY_COMPONENTS().isEmpty()) return true;
		}
		return false;
	}

	public String createGlobalPathway(String name)
	{
		pathway p = biopaxModel.addNew(pathway.class,
			"http://chisiobiopaxeditor/#" + System.currentTimeMillis());

		for (interaction inter : biopaxModel.getObjects(interaction.class))
		{
			p.addPATHWAY_COMPONENTS(inter);
		}
		p.setNAME(makeUniquePathwayName(name));
		return p.getNAME();
	}

	public String createPathway(String name, List<String> intids)
	{
		pathway p = biopaxModel.addNew(pathway.class,
			"http://chisiobiopaxeditor/#" + System.currentTimeMillis());

		for (interaction inter : biopaxModel.getObjects(interaction.class))
		{
			if (intids.contains(inter.getRDFId()))
			{
				p.addPATHWAY_COMPONENTS(inter);
			}
		}
		p.setNAME(makeUniquePathwayName(name));
		return p.getNAME();
	}

	public List<String> getPathwayNames()
	{
		List<String> names = new ArrayList<String>();

		for (pathway p : biopaxModel.getObjects(pathway.class))
		{
			names.add(p.getNAME());
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
		BioPAXL2Graph exGraph = new BioPAXL2Graph(this.getBiopaxModel());

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
					else if (node instanceof Complex)
					{
						Complex orig = (Complex) node;
						map.put(orig, new Complex(orig, newRoot));
					}
					else if (node instanceof ComplexMember)
					{
						ComplexMember orig = (ComplexMember) node;
						map.put(orig, new ComplexMember(orig, (Complex) newRoot));
					}
					else if (node instanceof Actor)
					{
						Actor orig = (Actor) node;
						map.put(orig, new Actor(orig, newRoot));
					}
					else if (node instanceof Conversion)
					{
						Conversion orig = (Conversion) node;
						map.put(orig, new Conversion(orig, newRoot));
					}
					else if (node instanceof Control)
					{
						Control orig = (Control) node;
						map.put(orig, new Control(orig, newRoot));
					}
					else if (node instanceof Hub)
					{
						Hub orig = (Hub) node;
						map.put(orig, new Hub(orig, newRoot));
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
		Map<interaction, Collection<GraphObject>> intMap = getInteractionMap();

		Set<GraphObject> set = new HashSet<GraphObject>();

		for (interaction inter : getMemberInteractions(p.l2p, null))
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
	private Set<interaction> getMemberInteractions(pathway p, Set<pathway> pset)
	{
		Set<interaction> set = new HashSet<interaction>();
		if (pset == null) pset = new HashSet<pathway>();

		for (pathwayComponent comp : p.getPATHWAY_COMPONENTS())
		{
			if (comp instanceof interaction)
			{
				set.add((interaction) comp);
			}
			else if (comp instanceof pathwayStep)
			{
				for (process proc : ((pathwayStep) comp).getSTEP_INTERACTIONS())
				{
					if (proc instanceof interaction)
					{
						set.add((interaction) proc);
					}
					else if (proc instanceof pathway)
					{
						if (proc == p)
						{
							System.err.println("Pathway includes itself: " + p.getNAME());
						}
						else if (pset.contains(proc))
						{
							System.err.println("Pathway has cyclic reference: " + p.getNAME());
						}
						else
						{
							pset.add((pathway) proc);
							set.addAll(getMemberInteractions((pathway) proc, pset));
						}
					}
				}
			}
			else if (comp instanceof pathway)
			{
				if (comp == p)
				{
					System.err.println("Pathway includes itself: " + p.getNAME());
				}
				else if (pset.contains(comp))
				{
					System.err.println("Pathway has cyclic reference: " + p.getNAME());
				}
				else
				{
					pset.add((pathway) comp);
					set.addAll(getMemberInteractions((pathway) comp, pset));
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
			else if (o instanceof Complex)
			{
				Complex com = (Complex) o;
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
			else if (o instanceof Complex)
			{
				Complex com = (Complex) o;
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
	 * @param objects to find and replace complex members
	 */
	public void replaceComplexMembersWithComplexes(Collection<Node> objects)
	{
		for (GraphObject go : new HashSet<GraphObject>(objects))
		{
			if (go instanceof ComplexMember)
			{
				objects.remove(go);

				Complex cmp = ((ComplexMember) go).getParentComplex();
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
					isProt = holder.l2pe.getClass().toString().contains("tein");
					
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
