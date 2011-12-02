package org.gvt.model.biopaxl2;

import org.biopax.paxtools.model.level2.*;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;
import org.patika.mada.graph.GraphObject;

import java.util.*;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class Hub extends BioPAXNode
{
	private interaction inter;
	private List<physicalEntityParticipant> peps;

	public Hub(CompoundModel root)
	{
		super(root);
		
		setShape("Ellipse");
		setColor(COLOR);
		setText("");
		setSize(new Dimension(9, 9));
	}
	
	public Hub(CompoundModel root, interaction inter, List<physicalEntityParticipant> peps,
		Map<String, NodeModel> map)
	{
		this(root);
		this.inter = inter;
		this.peps = peps;

		this.setTooltipText(inter.getNAME());

		extractReferences(inter);
		buildConnections(root, map);
	}

	public Hub(Hub excised, CompoundModel root)
	{
		super(excised, root);
		this.inter = excised.getInteraction();
		this.peps = excised.getParticipants();
	}

	public interaction getInteraction()
	{
		return inter;
	}

	public boolean isEvent()
	{
		return true;
	}

	public List<physicalEntityParticipant> getParticipants()
	{
		return peps;
	}

	public Collection<? extends Level2Element> getRelatedModelElements()
	{
		return Arrays.asList(this.inter);
	}

	private void buildConnections(CompoundModel root, Map<String, NodeModel> map)
	{
		for (control c : this.inter.isCONTROLLEDOf())
		{
			if (map.containsKey(c.getRDFId()))
			{
				Control mod = (Control) map.get(c.getRDFId());
				new EffectorSecondHalf(mod, this, mod.getControl());
			}
			else if (c.isCONTROLLEDOf().isEmpty() && c.getCONTROLLER().size() == 1)
			{
				NodeModel source = map.get(c.getCONTROLLER().iterator().next().getRDFId());
				new NonModulatedEffector(source, this, c,
					c.getCONTROLLER().iterator().next(), inter);
			}
			else
			{
				Control ctrl = new Control(root, c, this, map);
				map.put(c.getRDFId(), ctrl);
			}
		}
	}

	private static final Color COLOR = new Color(null, 100, 100, 100);

	public static String getPossibleCompartmentName(Collection<physicalEntityParticipant> peps)
	{
		Set<String> names = new HashSet<String>();

		for (physicalEntityParticipant par : peps)
		{
			if (par.getCELLULAR_LOCATION() != null)
			{
				names.add(par.getCELLULAR_LOCATION().getTERM().iterator().next());
			}
		}
		if (names.size() == 1) return names.iterator().next();
		else return null;
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
		return inter.getRDFId();
	}

	public List<String[]> getInspectable()
	{
		List<String[]> list = super.getInspectable();

		addNamesAndTypeAndID(list, inter);

		for (evidence ev : inter.getEVIDENCE())
		{
			list.add(new String[]{"Evidence", ev.toString()});
		}

		addDataSourceAndXrefAndComments(list, inter);

		return list;
	}
}
