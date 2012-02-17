package org.gvt.model.sif;

import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.eclipse.swt.graphics.Color;
import org.gvt.model.biopaxl2.BioPAXEdge;
import org.gvt.model.biopaxl2.IBioPAXL2Node;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class SIFEdge extends BioPAXEdge
{
	private BinaryInteractionType type;
	private boolean breadthEdge;

	public static Map<String, EdgeType> typeMap;

	public SIFEdge(SIFNode source, SIFNode target, String tag)
	{
		this(source, target, getType(tag).intType);
	}

	public SIFEdge(SIFNode source, SIFNode target, BinaryInteractionType type)
	{
		super(source, target);

		this.type = type;

		setTooltipText(type.getTag());

		EdgeType et = getType(type.getTag());

		setColor(et.color);

		if (et.intType.isDirected())
		{
			setArrow("Target");
		}

		if (!et.solid)
		{
			setStyle("Dashed");
		}

		breadthEdge = !getType(type.getTag()).noDistance;
	}

	public BinaryInteractionType getType()
	{
		return type;
	}

	public int getSign()
	{
		return getType(type.getTag()).sign;
	}

	public boolean isDirected()
	{
		return type.isDirected();
	}

	public boolean isBreadthEdge()
	{
		return breadthEdge;
	}

	public List<String[]> getInspectable()
	{
		List<String[]> list = super.getInspectable();
		list.add(new String[]{"Type", type.getTag()});
		return list;
	}

	public static class EdgeType
	{
		BinaryInteractionType intType;
		Color color;
		boolean solid;
		int sign;
		boolean noDistance;

		private EdgeType(BinaryInteractionType intType, Color color, boolean solid, int sign,
			boolean noDistance)
		{
			this.intType = intType;
			this.color = color;
			this.solid = solid;
			this.sign = sign;
			this.noDistance = noDistance;
		}

		public BinaryInteractionType getIntType()
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
		typeMap.put(type.intType.getTag(), type);
	}

	private static EdgeType getType(String typeName)
	{
		return typeMap.get(typeName);
	}
	
	public static boolean isDirected(String typeName)
	{
		return getType(typeName).intType.isDirected();
	}

	public String getIDHash()
	{
		return ((IBioPAXL2Node) getSourceNode()).getIDHash() +
			((IBioPAXL2Node) getTargetNode()).getIDHash() +
			type.getTag();
	}

	private static final boolean SOLID = true;
	private static final boolean DASHED = false;

	static
	{
		typeMap = new HashMap<String, EdgeType>();
		
		addType(new EdgeType(BinaryInteractionType.INTERACTS_WITH,
			new Color(null, 200, 0, 0), SOLID, NO_SIGN, false));
		addType(new EdgeType(BinaryInteractionType.REACTS_WITH,
			new Color(null, 0, 150, 0), SOLID, NO_SIGN, false));
		addType(new EdgeType(BinaryInteractionType.IN_SAME_COMPONENT,
			new Color(null, 0, 0, 250), SOLID, NO_SIGN, false));
		addType(new EdgeType(BinaryInteractionType.COMPONENT_OF,
			new Color(null, 100, 100, 100), DASHED, POSITIVE, true));
		addType(new EdgeType(BinaryInteractionType.STATE_CHANGE,
			new Color(null, 0, 150, 150), SOLID, NO_SIGN, false));
		addType(new EdgeType(BinaryInteractionType.METABOLIC_CATALYSIS,
			new Color(null, 250, 0, 250), SOLID, NO_SIGN, false));
		addType(new EdgeType(BinaryInteractionType.SEQUENTIAL_CATALYSIS,
			new Color(null, 150, 50, 150), SOLID, NO_SIGN, false));
		addType(new EdgeType(BinaryInteractionType.CO_CONTROL,
			new Color(null, 100, 100, 0), SOLID, POSITIVE, false));
		addType(new EdgeType(BinaryInteractionType.ACTIVATES,
			new Color(null, 100, 200, 0), SOLID, POSITIVE, false));
		addType(new EdgeType(BinaryInteractionType.INACTIVATES,
			new Color(null, 200, 100, 0), SOLID, NEGATIVE, false));
		addType(new EdgeType(BinaryInteractionType.GENERIC_OF,
			new Color(null, 150, 150, 0), SOLID, POSITIVE, true));
	}

}
