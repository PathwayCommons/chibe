package org.gvt.model.biopaxl3;

import org.biopax.paxtools.model.level3.*;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;
import org.gvt.util.ID;
import org.gvt.util.NodeProvider;
import org.ivis.layout.Cluster;
import org.patika.mada.graph.GraphObject;

import java.util.*;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class Hub extends BioPAXNode
{
	private Interaction inter;

	public Hub(CompoundModel root)
	{
		super(root);
		
		setShape("Ellipse");
		setColor(COLOR);
		setText("");
		setSize(new Dimension(9, 9));
	}
	
	public Hub(CompoundModel root, Interaction inter, NodeProvider prov)
	{
		this(root);
		this.inter = inter;

		this.setTooltipText(inter.getStandardName());

		extractReferences(inter);
		buildConnections(root, prov);
	}

	public Hub(Hub excised, CompoundModel root)
	{
		super(excised, root);
		this.inter = excised.getInteraction();
	}

	public Interaction getInteraction()
	{
		return inter;
	}

	public boolean isEvent()
	{
		return true;
	}

	public Collection<? extends Level3Element> getRelatedModelElements()
	{
		return Arrays.asList(this.inter);
	}

	private void buildConnections(CompoundModel root, NodeProvider prov)
	{
		for (Entity ent : inter.getParticipant())
		{
			NodeModel node = prov.getNode(ID.get(ent), root);
			if (node != null)
			{
				new MultiTouch(node, this);
			}
		}

		createControlOverInteraction(root, inter, prov);
	}

	private static final Color COLOR = new Color(null, 100, 100, 100);

	public static String getPossibleCompartmentName(Interaction inter)
	{
		Set<String> names = new HashSet<String>();

		for (Entity ent : inter.getParticipant())
		{
			if (ent instanceof PhysicalEntity)
			{
				PhysicalEntity pe = (PhysicalEntity) ent;
				if (pe.getCellularLocation() != null)
				{
					for (String loc : pe.getCellularLocation().getTerm())
					{
						names.add(loc);
						break;
					}
				}
			}
		}
		if (names.size() == 1) return names.iterator().next();
		else return null;
	}

	public static boolean needsToRepresentedWithANode(MolecularInteraction mi, NodeProvider prov)
	{
		for (Control control : mi.getControlledOf())
		{
			if (prov.needsToBeDisplayed(ID.get(control))) return true;
		}
		int cnt = 0;
		for (Entity entity : mi.getParticipant())
		{
			if (prov.needsToBeDisplayed(ID.get(entity)))
			{
				cnt++;
			}
		}
		return cnt != 2;
	}

	public Set<GraphObject> getRequisites()
	{
		Set<GraphObject> reqs = super.getRequisites();
		reqs.addAll(this.getSourceConnections());
		reqs.addAll(this.getTargetConnections());
		return reqs;
	}

	public String getIDHash()
	{
		return ID.get(inter);
	}

	public List<String[]> getInspectable()
	{
		List<String[]> list = super.getInspectable();

		addNamesAndTypeAndID(list, inter);

		for (Evidence ev : inter.getEvidence())
		{
			list.add(new String[]{"Evidence", ev.toString()});
		}

		addDataSourceAndXrefAndComments(list, inter);

		return list;
	}
}
