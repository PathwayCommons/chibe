package org.gvt.model.sifl3;

import org.biopax.paxtools.pattern.miner.SIFType;
import org.eclipse.swt.graphics.Color;
import org.gvt.model.biopaxl3.BioPAXEdge;
import org.gvt.model.biopaxl3.IBioPAXL3Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class SIFEdge extends BioPAXEdge
{
	private String tag;
	private boolean breadthEdge;

	public static Map<String, EdgeType> typeMap;

	public SIFEdge(SIFNode source, SIFNode target, String tag)
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

	private static final boolean SOLID = true;
	private static final boolean DASHED = false;

	static
	{
		typeMap = new HashMap<String, EdgeType>();
		
		addType(new EdgeType(SIFType.INTERACTS_WITH,
			new Color(null, 100, 150, 100), SOLID, NO_SIGN, false));
		addType(new EdgeType(SIFType.IN_SAME_COMPLEX,
			new Color(null, 150, 150, 150), SOLID, NO_SIGN, false));
		addType(new EdgeType(SIFType.CONTROLS_STATE_CHANGE,
			new Color(null, 50, 100, 150), SOLID, NO_SIGN, false));
		addType(new EdgeType(SIFType.CONSEQITIVE_CATALYSIS,
			new Color(null, 150, 50, 150), SOLID, NO_SIGN, false));
        addType(new EdgeType(SIFType.CONTROLS_EXPRESSION,
                new Color(null, 50, 150, 50), DASHED, NO_SIGN, false));
        addType(new EdgeType(SIFType.CONTROLS_DEGRADATION,
                new Color(null, 150, 50, 50), SOLID, NO_SIGN, false));


        // Non-Paxtools SIF edges

		addType(new EdgeType("TRANSCRIPTION", true,
			new Color(null, 150, 150, 0), DASHED, NO_SIGN, false));
		addType(new EdgeType("DEGRADATION", true,
			new Color(null, 150, 0, 150), SOLID, NO_SIGN, false));
		addType(new EdgeType("BINDS_TO", false,
			new Color(null, 100, 100, 100), SOLID, NO_SIGN, true));
	}
}
