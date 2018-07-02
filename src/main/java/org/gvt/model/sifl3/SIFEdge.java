package org.gvt.model.sifl3;

import org.biopax.paxtools.pattern.miner.SIFEnum;
import org.biopax.paxtools.pattern.miner.SIFType;
import org.cbio.causality.signednetwork.SignedType;
import org.eclipse.swt.graphics.Color;
import org.gvt.model.GraphObject;
import org.gvt.model.NodeModel;
import org.gvt.model.biopaxl3.BioPAXEdge;
import org.gvt.model.biopaxl3.IBioPAXL3Node;

import java.util.*;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class SIFEdge extends BioPAXEdge
{
	private String tag;
	private boolean breadthEdge;
	private Set<String> mediators;
	protected Map<GraphObject, SIFEdge> substitutionMap;


	public static Map<String, EdgeType> typeMap;

	public SIFEdge(NodeModel source, NodeModel target, String tag, Set<String> mediators)
	{
		super(source, target);

		this.tag = tag;

		setTooltipText(tag);

		EdgeType et = getType(tag);

		setColor(et.color);

		if (et.directed)
		{
			setArrow("Target");
		}

		if (!et.solid)
		{
			setStyle("Dashed");
		}

		breadthEdge = !getType(tag).noDistance;

		this.mediators = new HashSet<String>();
		if (mediators != null) this.mediators.addAll(mediators);
	}

	public String getTag()
	{
		return tag;
	}

	public int getSign()
	{
		return getType(tag).sign;
	}

	public boolean isDirected()
	{
		return getType(tag).directed;
	}

	public boolean isBreadthEdge()
	{
		return breadthEdge;
	}

	public List<String[]> getInspectable()
	{
		List<String[]> list = super.getInspectable();
		list.add(new String[]{"Type", tag});
		if (getTooltipText() != null && !getTooltipText().isEmpty())
		{
			list.add(new String[]{"Tooltip", getTooltipText()});
		}
		return list;
	}

	public static class EdgeType
	{
		SIFType intType;
		String tag;
		Color color;
		boolean solid;
		boolean directed;
		int sign;
		boolean noDistance;

		private EdgeType(SIFType intType, Color color, boolean solid, int sign,
			boolean noDistance)
		{
			this.intType = intType;
			this.tag = intType.getTag();
			this.directed = intType.isDirected();
			this.color = color;
			this.solid = solid;
			this.sign = sign;
			this.noDistance = noDistance;
		}

		private EdgeType(String tag, boolean directed, Color color, boolean solid, int sign,
			boolean noDistance)
		{
			this.tag = tag;
			this.directed = directed;
			this.color = color;
			this.solid = solid;
			this.sign = sign;
			this.noDistance = noDistance;
		}

		public String getTag()
		{
			return tag;
		}

		public boolean isDirected()
		{
			return directed;
		}

		public SIFType getIntType()
		{
			return intType;
		}

		public Color getColor()
		{
			return color;
		}

		public boolean isSolid()
		{
			return solid;
		}

		public int getSign()
		{
			return sign;
		}

		public boolean isNoDistance()
		{
			return noDistance;
		}
	}

	private static void addType(EdgeType type)
	{
		typeMap.put(type.tag, type);
	}

	public static EdgeType getType(String tag)
	{
		return typeMap.get(tag);
	}
	
	public static boolean isDirected(String tag)
	{
		return getType(tag).intType.isDirected();
	}

	public String getIDHash()
	{
		return ((IBioPAXL3Node) getSourceNode()).getIDHash() +
			((IBioPAXL3Node) getTargetNode()).getIDHash() + tag;
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

	public void addSubstitution(SIFEdge edge, NodeModel node)
	{
		if (substitutionMap == null)
			substitutionMap = new HashMap<GraphObject, SIFEdge>();

		substitutionMap.put(node, edge);
		mediators.addAll(edge.getMediators());
	}

	private static final boolean SOLID = true;
	private static final boolean DASHED = false;

	static
	{
		typeMap = new HashMap<String, EdgeType>();
		
		addType(new EdgeType(SIFEnum.NEIGHBOR_OF,
			new Color(null, 100, 120, 100), SOLID, NO_SIGN, false));
		addType(new EdgeType(SIFEnum.INTERACTS_WITH,
			new Color(null, 255, 192, 203), SOLID, NO_SIGN, false));
		addType(new EdgeType(SIFEnum.IN_COMPLEX_WITH,
			new Color(null, 150, 150, 150), SOLID, NO_SIGN, false));
		addType(new EdgeType(SIFEnum.CONTROLS_STATE_CHANGE_OF,
			new Color(null, 50, 100, 150), SOLID, NO_SIGN, false));
		addType(new EdgeType(SIFEnum.CONTROLS_TRANSPORT_OF,
			new Color(null, 100, 100, 150), SOLID, NO_SIGN, false));
		addType(new EdgeType(SIFEnum.CONTROLS_PHOSPHORYLATION_OF,
			new Color(null, 100, 150, 100), SOLID, NO_SIGN, false));
		addType(new EdgeType(SIFEnum.CATALYSIS_PRECEDES,
			new Color(null, 150, 50, 150), SOLID, NO_SIGN, false));
        addType(new EdgeType(SIFEnum.CONTROLS_EXPRESSION_OF,
                new Color(null, 50, 150, 50), DASHED, NO_SIGN, false));
        addType(new EdgeType(SIFEnum.CONSUMPTION_CONTROLLED_BY,
                new Color(null, 100, 120, 80), SOLID, NO_SIGN, false));
        addType(new EdgeType(SIFEnum.CONTROLS_PRODUCTION_OF,
                new Color(null, 50, 120, 100), SOLID, NO_SIGN, false));
        addType(new EdgeType(SIFEnum.CONTROLS_TRANSPORT_OF_CHEMICAL,
                new Color(null, 80, 130, 100), SOLID, NO_SIGN, false));
        addType(new EdgeType(SIFEnum.CHEMICAL_AFFECTS,
                new Color(null, 100, 80, 80), SOLID, NO_SIGN, false));
        addType(new EdgeType(SIFEnum.REACTS_WITH,
                new Color(null, 70, 120, 80), SOLID, NO_SIGN, true));
        addType(new EdgeType(SIFEnum.USED_TO_PRODUCE,
                new Color(null, 70, 80, 120), SOLID, NO_SIGN, false));

        addType(new EdgeType(SignedType.PHOSPHORYLATES,
                new Color(null, 0, 150, 0), SOLID, POSITIVE, false));
        addType(new EdgeType(SignedType.DEPHOSPHORYLATES,
                new Color(null, 150, 0, 0), SOLID, NEGATIVE, false));
        addType(new EdgeType(SignedType.UPREGULATES_EXPRESSION,
                new Color(null, 0, 150, 0), DASHED, POSITIVE, false));
        addType(new EdgeType(SignedType.DOWNREGULATES_EXPRESSION,
                new Color(null, 150, 0, 0), DASHED, NEGATIVE, false));
        addType(new EdgeType("activates-gtpase", true,
                new Color(null, 0, 150, 200), SOLID, POSITIVE, false));
        addType(new EdgeType("inhibits-gtpase", true,
                new Color(null, 200, 0, 170), SOLID, NEGATIVE, false));

        // Non-Paxtools SIF edges

		addType(new EdgeType("TRANSCRIPTION", true,
			new Color(null, 150, 150, 0), DASHED, NO_SIGN, false));
		addType(new EdgeType("DEGRADATION", true,
			new Color(null, 150, 0, 150), SOLID, NO_SIGN, false));
		addType(new EdgeType("BINDS_TO", false,
			new Color(null, 100, 100, 100), SOLID, NO_SIGN, true));
		addType(new EdgeType("in-same-group", false,
			new Color(null, 100, 100, 100), SOLID, NO_SIGN, false));
		addType(new EdgeType("correlates-with", false,
			new Color(null, 100, 100, 100), SOLID, NO_SIGN, false));
		addType(new EdgeType("activates", true,
			new Color(null, 50, 180, 50), SOLID, POSITIVE, false));
		addType(new EdgeType("inhibits", true,
			new Color(null, 180, 50, 50), SOLID, NEGATIVE, false));
	}
}
