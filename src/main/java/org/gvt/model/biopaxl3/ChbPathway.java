package org.gvt.model.biopaxl3;

import org.biopax.paxtools.model.level3.*;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;
import org.patika.mada.graph.GraphObject;

import java.util.*;

/**
 * Control in BioPAX file.
 * 
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class ChbPathway extends BioPAXNode
{
	/**
	 * BioPAX control node.
	 */
	Pathway pathway;

	public ChbPathway(CompoundModel root)
	{
		super(root);

		setColor(COLOR);
		setText("P");
		setSize(new Dimension(20, 20));
		setShape("Diamond");
	}

	public ChbPathway(CompoundModel root, Pathway pathway, Map<String, NodeModel> map)
	{
		this(root);

		// Remember this control to prevent duplication.
		map.put(pathway.getRDFId(), this);

		this.pathway = pathway;
		configFromModel();
	}

	public void configFromModel()
	{
		super.configFromModel();
		setTooltipText(pathway.getStandardName());
	}

	public ChbPathway(ChbPathway excised, CompoundModel root)
	{
		super(excised, root);
		this.pathway = excised.getPathway();
	}

	public Pathway getPathway()
	{
		return pathway;
	}

	public Collection<? extends Level3Element> getRelatedModelElements()
	{
		return Arrays.asList(pathway);
	}

	public boolean isEvent()
	{
		return false;
	}

	public Set<GraphObject> getRequisites()
	{
		Set<GraphObject> reqs = super.getRequisites();
		return reqs;
	}

	public List<String[]> getInspectable()
	{
		List<String[]> list = super.getInspectable();

		addNamesAndTypeAndID(list, pathway);

		for (Evidence ev : pathway.getEvidence())
		{
			list.add(new String[]{"Evidence", ev.toString()});
		}

		addDataSourceAndXrefAndComments(list, pathway);

		return list;
	}
	
	public String getIDHash()
	{
		return pathway.getRDFId();
	}

	private static final Color COLOR = new Color(null, 220, 210, 200);
}
