package org.gvt.model.basicsif;

import org.biopax.paxtools.pattern.miner.SIFType;
import org.gvt.model.GraphObject;
import org.gvt.model.biopaxl3.BioPAXEdge;
import org.gvt.model.NodeModel;
import org.gvt.model.sifl3.SIFEdge;

import java.util.*;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class BasicSIFEdge extends BioPAXEdge
{
	protected SIFEdge.EdgeType type;

	protected Set<String> mediators;

	protected Map<GraphObject, BasicSIFEdge> substitutionMap;

	protected String key;

	public BasicSIFEdge(NodeModel source, NodeModel target, String tag, String mediators)
	{
		super(source, target);

		this.type = SIFEdge.typeMap.get(tag);

		setTooltipText(tag);

		setColor(type.getColor());

		if (type.isDirected())
		{
			setArrow("Target");
		}

		if (!type.isSolid())
		{
			setStyle("Dashed");
		}

		this.mediators = new HashSet<String>();
		if (mediators != null)
		{
			Collections.addAll(this.mediators, mediators.split(" "));
		}

		setKey(source.getText() + " " + tag + " " + target.getText());
	}

	public BasicSIFEdge(BioPAXEdge excised, Map<NodeModel, NodeModel> map)
	{
		super(excised, map);
		this.type = ((BasicSIFEdge) excised).type;
		this.mediators = ((BasicSIFEdge) excised).mediators;
	}

	public int getSign()
	{
		return 0;
	}

	public boolean isDirected()
	{
		return type.getIntType().isDirected();
	}

	public boolean isBreadthEdge()
	{
		return true;
	}

	public String getIDHash()
	{
		return "";
	}

	public SIFType getType()
	{
		return type.getIntType();
	}

	public Set<String> getMediators()
	{
		return mediators;
	}

	public Set<String> getMediators(Set<GraphObject> nodes)
	{
		if (substitutionMap == null) return mediators;
		if (nodes.isEmpty()) return mediators;

		Set<String> meds = new HashSet<String>();
		for (GraphObject node : nodes)
		{
			if (substitutionMap.containsKey(node))
				meds.addAll(substitutionMap.get(node).getMediators(nodes));
		}

		if (!meds.isEmpty()) return meds;
		else return mediators;
	}

	public void addSubstitution(BasicSIFEdge edge, NodeModel node)
	{
		if (substitutionMap == null)
			substitutionMap = new HashMap<GraphObject, BasicSIFEdge>();

		substitutionMap.put(node, edge);
		mediators.addAll(edge.getMediators());
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public List<String[]> getInspectable()
	{
		List<String[]> list = super.getInspectable();
		list.add(new String[]{"Type", type.getIntType().getTag()});
		return list;
	}
}