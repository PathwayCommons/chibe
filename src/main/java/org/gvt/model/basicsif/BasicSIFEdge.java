package org.gvt.model.basicsif;

import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.pattern.miner.SIFType;
import org.gvt.model.biopaxl3.BioPAXEdge;
import org.gvt.model.NodeModel;
import org.gvt.model.sifl3.SIFEdge;

import java.util.Map;
import java.util.List;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class BasicSIFEdge extends BioPAXEdge
{
	protected SIFEdge.EdgeType type;

	public BasicSIFEdge(BasicSIFNode source, BasicSIFNode target, String tag)
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
	}

	public BasicSIFEdge(BioPAXEdge excised, Map<NodeModel, NodeModel> map)
	{
		super(excised, map);
		this.type = ((BasicSIFEdge) excised).type;
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
	
	public List<String[]> getInspectable()
	{
		List<String[]> list = super.getInspectable();
		list.add(new String[]{"Type", type.getIntType().getTag()});
		return list;
	}
}