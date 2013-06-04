package org.gvt.model.biopaxl3;

import org.biopax.paxtools.model.level3.*;
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

		assert root instanceof ChbComplex;
	}

	public ComplexMember(ChbComplex root, PhysicalEntity entity, Entity related)
	{
		super(root, entity, related);
	}

	public ComplexMember(ComplexMember excised, ChbComplex root)
	{
		super(excised, root);
	}

	public boolean isComplexMember()
	{
		return true;
	}

	public ChbComplex getParentComplex()
	{
		return (ChbComplex) this.getParentModel();
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
		return 1;
	}

	public boolean hasInfoString()
	{
		if (super.hasInfoString()) return true;
		return extractStochiometry() > 1;
	}

	public String getIDHash()
	{
		String hash = entity.getRDFId();
		if (related != null) hash += related.getRDFId();
		return hash;
	}

	public Set<GraphObject> getRequisites()
	{
		Set<GraphObject> reqs = super.getRequisites();
		reqs.add((GraphObject) this.getParentModel());
		return reqs;
	}

	
}
