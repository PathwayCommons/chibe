package org.gvt.model.biopaxl2;

import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.BioPAXElement;
import org.gvt.model.CompoundModel;
import org.patika.mada.graph.GraphObject;

import java.util.List;
import java.util.Set;

/**
 * Complex members in BioPAX complexes.
 * 
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class ComplexMember extends Actor
{
	public ComplexMember(CompoundModel root)
	{
		super(root);

		assert root instanceof Complex;
	}

	public ComplexMember(Complex root, physicalEntity entity,
		List<physicalEntityParticipant> participants)
	{
		super(root, entity, participants);
	}

	public ComplexMember(ComplexMember excised, Complex root)
	{
		super(excised, root);
	}

	public boolean isComplexMember()
	{
		return true;
	}
		
	public Complex getParentComplex()
	{
		return (Complex) this.getParentModel();
	}

	public List<String> getInfoStrings()
	{
		List<String> infos = super.getInfoStrings();

		int st = extractStochiometry();

		if (st > 1)
		{
			infos.add("" + st);
		}

		return infos;
	}

	protected int extractStochiometry()
	{
		double s = participants.get(0).getSTOICHIOMETRIC_COEFFICIENT();
		if (s != BioPAXElement.UNKNOWN_DOUBLE)
		{
			int i = (int) Math.round(s);

			if (i > 1)
			{
				return i;
			}
		}
		return 1;
	}

	public boolean hasInfoString()
	{
		if (super.hasInfoString()) return true;
		return extractStochiometry() > 1;
	}

	public String getIDHash()
	{
		int hash = 0;
		for (physicalEntityParticipant pep : participants)
		{
			if (pep.isPARTICIPANTSof().isEmpty())
			{
				hash += pep.getRDFId().hashCode();
			}
			else
			{
				hash += pep.stateCode();
				break;
			}
		}
		return entity.getRDFId() + hash;
	}

	public Set<GraphObject> getRequisites()
	{
		Set<GraphObject> reqs = super.getRequisites();
		reqs.add((GraphObject) this.getParentModel());
		return reqs;
	}

	
}
