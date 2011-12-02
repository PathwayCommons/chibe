package org.gvt.model.biopaxl3;

import org.biopax.paxtools.model.level3.*;
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
	private Interaction inter;

	public Hub(CompoundModel root)
	{
		super(root);
		
		setShape("Ellipse");
		setColor(COLOR);
		setText("");
		setSize(new Dimension(9, 9));
	}
	
	public Hub(CompoundModel root, Interaction inter, Map<String, NodeModel> map)
	{
		this(root);
		this.inter = inter;

		this.setTooltipText(inter.getStandardName());

		extractReferences(inter);
		buildConnections(root, map);
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

	private void buildConnections(CompoundModel root, Map<String, NodeModel> map)
	{
		for (Control c : this.inter.getControlledOf())
		{
			if (map.containsKey(c.getRDFId()))
			{
				ChbControl mod = (ChbControl) map.get(c.getRDFId());
				new EffectorSecondHalf(mod, this, mod.getControl());
			}
			else if (c.getControlledOf().isEmpty() && c.getController().size() == 1)
			{
				NodeModel source = map.get(c.getController().iterator().next().getRDFId());
				new NonModulatedEffector(source, this, c, inter);
			}
			else
			{
				ChbControl ctrl = new ChbControl(root, c, this, map);
				map.put(c.getRDFId(), ctrl);
			}
		}
	}

	private static final Color COLOR = new Color(null, 100, 100, 100);

	public static String getPossibleCompartmentName(Collection<PhysicalEntity> pes)
	{
		Set<String> names = new HashSet<String>();

		for (PhysicalEntity pe : pes)
		{
			if (pe.getCellularLocation() != null)
			{
				for (String loc : pe.getCellularLocation().getTerm())
				{
					names.add(loc);
					break;
				}
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

		for (Evidence ev : inter.getEvidence())
		{
			list.add(new String[]{"Evidence", ev.toString()});
		}

		addDataSourceAndXrefAndComments(list, inter);

		return list;
	}
}
