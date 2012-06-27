package org.gvt.model.biopaxl2;

import org.gvt.model.NodeModel;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.openControlledVocabulary;

import java.util.Map;
import java.util.List;

/**
 * Edges that are pased on a physical entity participant.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public abstract class PEPBasedEdge extends BioPAXEdge
{
	/**
	 * PEP that this edge is based on.
	 */
	private physicalEntityParticipant pep;

	public PEPBasedEdge(NodeModel source, NodeModel target, physicalEntityParticipant pep)
	{
		super(source, target);
		this.pep = pep;
	}

	public PEPBasedEdge(BioPAXEdge excised, Map<NodeModel, NodeModel> map)
	{
		super(excised, map);
		this.pep = ((PEPBasedEdge) excised).getPep();
	}

	public physicalEntityParticipant getPep()
	{
		return pep;
	}

	public String getIDHash()
	{
		return pep.getRDFId();
	}

	public List<String[]> getInspectable()
	{
		List<String[]> list = super.getInspectable();

		list.add(new String[]{"ID", pep.getRDFId()});

		list.add(new String[]{"Stochiometry", "" + pep.getSTOICHIOMETRIC_COEFFICIENT()});

		for (String comment : pep.getCOMMENT())
		{
			if (!comment.contains("@Layout"))
			{
				list.add(new String[]{"Comment", comment});
			}
		}
		return list;
	}
}
