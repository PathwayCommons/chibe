package org.gvt.model.biopaxl2;

import org.gvt.command.CreateCommand;
import org.gvt.model.CompoundModel;
import org.patika.mada.graph.Edge;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;
import org.patika.mada.util.ExperimentData;
import org.patika.mada.util.Representable;
import org.patika.mada.util.XRef;

import java.util.*;

/**
 * Base class for complexes and abstractions.
 * 
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public abstract class BioPAXCompoundNode extends CompoundModel implements IBioPAXL2Node
{
	/**
	 * Unique id of this object.
	 */
	private int id;

	/**
	 * External database references for this node.
	 */
	private List<XRef> references;

	protected NodeUtil util;

	public BioPAXCompoundNode(CompoundModel root)
	{
		CreateCommand command = new CreateCommand(root, this);
		command.execute();

		this.references = new ArrayList<XRef>();
		this.util = new NodeUtil(this);
	}

	/**
	 * Constructor for excising.
	 * @param excised original graph member
	 */
	public BioPAXCompoundNode(BioPAXCompoundNode excised, CompoundModel root)
	{
		this(root);
		this.id = excised.id;
		this.references.addAll(excised.getReferences());
		this.setShape(excised.getShape());
		this.setSize(excised.getSize());
		this.setColor(excised.getColor());
		this.setText(excised.getText());
		this.setTextColor(excised.getTextColor());
		this.setTooltipText(excised.getTooltipText());
		this.setBorderColor(excised.getBorderColor());

		for (Object key : excised.getAllLabels())
		{
			this.putLabel(key, excised.getLabel(key));
		}

		getGraph().putInExcisionMap(excised, this);
		this.putLabel(BioPAXL2Graph.EXCISED_FROM, excised);
	}

	public boolean isComplexMember()
	{
		return false;
	}

	/**
	 * Empty method will be overwritten in children when the node needes to be configured by the
	 * properties of its corresponding biopax model obejects.
	 */
	public void configFromModel()
	{
		// Assume there is no configuration needed.
	}

	public BioPAXL2Graph getGraph()
	{
		return util.getGraph();
	}

	public Compartment getCompartment()
	{
		return util.getCompartment();
	}

	public boolean isEvent()
	{
		return false;
	}

	public boolean isTranscriptionEvent()
	{
		return false;
	}

	public List<XRef> getReferences()
	{
		return this.references;
	}

	public void addReference(XRef ref)
	{
		if (!this.references.contains(ref))
		{
			this.references.add(ref);
		}
	}

	public String getName()
	{
		return this.getText();
	}

	//----------------------------------------------------------------------------------------------
	// Section: Experiment data related
	//----------------------------------------------------------------------------------------------

	public Representable getRepresentableData(Object key)
	{
		return (Representable) this.getLabel(key);
	}

	public ExperimentData getExperimentData(String type)
	{
		return (ExperimentData) this.getLabel(type);
	}

	public void setExperimentData(ExperimentData data)
	{
		this.putLabel(data.getKey(), data);
	}

	public boolean hasExperimentData(Object key)
	{
		return this.hasLabel(key);
	}

	public boolean hasSignificantExperimentalChange(String type)
	{
		return this.hasExperimentData(type) && this.getExperimentData(type).isSignificant();
	}

	public int getExperimentDataSign(String type)
	{
		return this.getExperimentData(type).getSign();
	}

	//----------------------------------------------------------------------------------------------
	// Section: Location related
	//----------------------------------------------------------------------------------------------

	public boolean fetchLocation(String pathwayRDFID)
	{
		return util.fetchLocation(pathwayRDFID);
	}

	/**
	 * Records location of this node in to the related biopax elemnts.
	 */
	public void recordLocation()
	{
		util.recordLocation();
	}

	/**
	 * Records location of this node in to the related biopax elemnts.
	 */
	public void eraseLocation()
	{
		util.eraseLocation();
	}

	/**
	 * A node is assumed to map unique biopax model element by default and id hash is not used. This
	 * method must be overwritten in children when mapping clashes occur, e.g. when drawing two
	 * conversions for in chisio for representing a reversible conversion in biopax.
	 */
	public String getIDHash()
	{
		return util.getIDHash();
	}

	//----------------------------------------------------------------------------------------------
	// Section: Model tagging
	//----------------------------------------------------------------------------------------------

	public boolean hasModelTag(String tag)
	{
		return util.hasModelTag(tag);
	}

	public String fetchModelTag(String tag)
	{
		return util.fetchModelTag(tag);
	}

	//----------------------------------------------------------------------------------------------
	// Section: Traversing
	//----------------------------------------------------------------------------------------------

	public Collection<? extends Node> getParents()
	{
		CompoundModel p = this.getParentModel();
		Collection<Node> col = new ArrayList<Node>(1);

		if (p == null || p.isRoot())
		{
			return col;
		}
		else
		{
			col.add((BioPAXCompoundNode) p);
			return col;
		}
	}

	public Collection<? extends Edge> getUpstream()
	{
		return this.getTargetConnections();
	}

	public Collection<? extends Edge> getDownstream()
	{
		return this.getSourceConnections();
	}

	public Set<Node> getTabuNodes()
	{
		return Collections.emptySet();
	}
	
	//----------------------------------------------------------------------------------------------
	// Section: Identity
	//----------------------------------------------------------------------------------------------

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public boolean sameEntity(Node n)
	{
		return n.equals(this);
	}

	/**
	 * No node is a breadth node by default.
	 * @return false by default
	 */
	public boolean isBreadthNode()
	{
		return false;
	}

	public String toString()
	{
		return this.getName();
	}

	public boolean isHighlighted()
	{
		return this.isHighlight();
	}

	public Set<GraphObject> getRequisites()
	{
		HashSet<GraphObject> reqs = new HashSet<GraphObject>();

		if (this.getParentModel() instanceof Compartment)
		{
			reqs.add((GraphObject) this.getParentModel());
		}

		return reqs;
	}

	public List<String[]> getInspectable()
	{
		List<String[]> list = new ArrayList<String[]>();

		return list;
	}
}
