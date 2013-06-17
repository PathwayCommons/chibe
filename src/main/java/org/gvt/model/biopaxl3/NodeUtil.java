package org.gvt.model.biopaxl3;

import org.gvt.model.NodeModel;
import org.biopax.paxtools.model.level3.Level3Element;
import org.eclipse.draw2d.geometry.Point;

import java.util.ArrayList;

/**
 * If something should be implemented in both BioPAXNode and BioPAXCompoundNode, it is implemented
 * here for avoiding duplication.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class NodeUtil
{
	private IBioPAXL3Node node;

	public NodeUtil(IBioPAXL3Node node)
	{
		this.node = node;
	}

	/**
	 * Gets the graph that this node is inserted.
	 */
	public BioPAXL3Graph getGraph()
	{
		NodeModel parent = (NodeModel) node;

		do
		{
			parent = parent.getParentModel();
		}
		while (parent != null && !(parent instanceof BioPAXL3Graph));

		return (BioPAXL3Graph) parent;
	}

	/**
	 * Gets the compartment that this node is in.
	 */
	public Compartment getCompartment()
	{
		NodeModel parent = (NodeModel) node;

		do
		{
			parent = parent.getParentModel();
		}
		while (parent != null && !(parent instanceof Compartment));

		return (Compartment) parent;
	}

	//----------------------------------------------------------------------------------------------
	// Section: Tagging biopax model with information
	//----------------------------------------------------------------------------------------------

	public String fetchModelTag(String type)
	{
		String prefix  = node.getIDHash() + BioPAXL3Graph.MODEL_TAG_SEPARATOR + type;
		for (Level3Element element : node.getRelatedModelElements())
		{
			for (String comment : element.getComment())
			{
				if (comment.startsWith(prefix))
				{
					return comment.substring(prefix.length()+1);
				}
			}
		}
		return null;
	}

	public boolean hasModelTag(String type)
	{
		String prefix  = node.getIDHash() + BioPAXL3Graph.MODEL_TAG_SEPARATOR + type;
		for (Level3Element element : node.getRelatedModelElements())
		{
			for (String comment : element.getComment())
			{
				if (comment.startsWith(prefix))
				{
					return true;
				}
			}
		}
		return false;
	}

	public void recordModelTag(String type, String tag)
	{
		String prefix  = node.getIDHash() + BioPAXL3Graph.MODEL_TAG_SEPARATOR + type;
		tag = prefix + BioPAXL3Graph.MODEL_TAG_SEPARATOR + tag;

		for (Level3Element element : node.getRelatedModelElements())
		{
			removeCommentStartsWith(element, prefix);
			element.addComment(tag);
		}
	}

	public void removeModelTag(String type)
	{
		String prefix  = node.getIDHash() + BioPAXL3Graph.MODEL_TAG_SEPARATOR + type;

		for (Level3Element element : node.getRelatedModelElements())
		{
			removeCommentStartsWith(element, prefix);
		}
	}

	private void removeCommentStartsWith(Level3Element element, String prefix)
	{
		for (String com : new ArrayList<String>(element.getComment()))
		{
			if (com.startsWith(prefix))
			{
				element.removeComment(com);
			}
		}
	}

	/**
	 * Simplest implementation.
	 */
	public String getIDHash()
	{
		return node.getName() != null ? node.getName() : "";
	}

	//----------------------------------------------------------------------------------------------
	// Section: Layout tagging
	//----------------------------------------------------------------------------------------------

	public boolean fetchLocation(String pathwayRDFID)
	{
		// Get the rdfid of the related pathway if not given
		if (pathwayRDFID == null) pathwayRDFID = getGraph().getPathway().getRDFID();

		String type = BioPAXL3Graph.LAYOUT_TAG + BioPAXL3Graph.MODEL_TAG_SEPARATOR + pathwayRDFID;

		String tag = fetchModelTag(type);

		if (tag == null) return false;

		String[] terms = tag.split(BioPAXL3Graph.MODEL_TAG_SEPARATOR);

		assert terms.length == 2 : "terms length: " + terms.length + "\n" + tag;

		node.setLocationAbs(new Point(Integer.parseInt(terms[0]), Integer.parseInt(terms[1])));

		return true;
	}

	/**
	 * Records location of this node in to the related biopax elemnts.
	 */
	public void recordLocation()
	{
		// Get the rdfid of the related pathway
		String pathwayRDFID = getGraph().getPathway().getRDFID();

		String type = BioPAXL3Graph.LAYOUT_TAG + BioPAXL3Graph.MODEL_TAG_SEPARATOR + pathwayRDFID;

		String tag = node.getLocationAbs().x + BioPAXL3Graph.MODEL_TAG_SEPARATOR +
			node.getLocationAbs().y;

		recordModelTag(type, tag);
	}

	public void eraseLocation()
	{
		// Get the rdfid of the related pathway
		String pathwayRDFID = getGraph().getPathway().getRDFID();

		String type = BioPAXL3Graph.LAYOUT_TAG + BioPAXL3Graph.MODEL_TAG_SEPARATOR + pathwayRDFID;

		removeModelTag(type);
	}
		

}
