package org.gvt.model.biopaxl2;

import org.biopax.paxtools.model.level2.*;
import org.biopax.paxtools.model.Model;
import org.eclipse.swt.graphics.Color;
import org.gvt.model.CompoundModel;
import org.gvt.model.EntityAssociated;
import org.gvt.util.EntityHolder;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.util.XRef;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class Complex extends BioPAXCompoundNode implements EntityAssociated
{
	/**
	 * BioPAX complex.
	 */
	private complex cmp;

	/**
	 * List of related physical entity participants.
	 */
	List<physicalEntityParticipant> participants;
	
	public Complex(CompoundModel root)
	{
		super(root);
		
		setText("Complex");
	}

	public Collection<? extends Level2Element> getRelatedModelElements()
	{
		return this.participants;
	}

	public Complex(CompoundModel root, complex cmp, List<physicalEntityParticipant> participants)
	{
		this(root);

		this.cmp = cmp;
		this.participants = participants;

		extractReferences();
		
		setText((cmp.getSHORT_NAME() != null && cmp.getSHORT_NAME().length() > 0) ?
			cmp.getSHORT_NAME() : cmp.getNAME());

		setTooltipText(getText());
		
		setColor(COLOR);
	}

	/**
	 * Constructor for excising.
	 * @param toexcise
	 * @param root
	 */
	public Complex(Complex toexcise, CompoundModel root)
	{
		super(toexcise, root);
		this.cmp = toexcise.getComplex();
		this.participants = toexcise.getParticipants();
	}

	/**
	 * Extract cross-references from the based complex.
	 */
	private void extractReferences()
	{
		for (xref xr : cmp.getXREF())
		{
			this.addReference(new XRef(xr));
		}
	}

	public complex getComplex()
	{
		return cmp;
	}

	public EntityHolder getEntity()
	{
		return new EntityHolder(cmp);
	}

	public List<physicalEntityParticipant> getParticipants()
	{
		return participants;
	}

	/**
	 * Complexes are breadth nodes.
	 * @return true
	 */
	public boolean isBreadthNode()
	{
		return true;
	}

	public Set<GraphObject> getRequisites()
	{
		Set<GraphObject> reqs = super.getRequisites();
		reqs.addAll(this.getChildren());
		return reqs;
	}

	public List<String[]> getInspectable()
	{
		List<String[]> list = new ArrayList<String[]>();

		BioPAXNode.addNamesAndTypeAndID(list, cmp);

		if (participants != null && !participants.isEmpty())
		{
			physicalEntityParticipant pep = participants.get(0);
			openControlledVocabulary loc = pep.getCELLULAR_LOCATION();
			if (loc != null)
			{
				list.add(new String[]{"Location", loc.toString()});
			}
		}

		BioPAXNode.addDataSourceAndXrefAndComments(list, cmp);

		return list;
	}

	public String getIDHash()
	{
		String h = cmp.getRDFId();

		int c = 0;
		if (!participants.isEmpty())
		{
			c += participants.iterator().next().stateCode();
		}
		return h + c;
	}

	public physicalEntityParticipant createNewPEP(Model model, String rdfid)
	{
		if (participants.isEmpty())
		{
			physicalEntityParticipant pepNew = (physicalEntityParticipant)
				model.addNew(physicalEntityParticipant.class, rdfid);

			pepNew.setPHYSICAL_ENTITY(cmp);
			return pepNew;
		}
		else
		{
			physicalEntityParticipant pep = participants.get(0);

			physicalEntityParticipant pepNew = (physicalEntityParticipant)
				model.addNew(pep.getModelInterface(), rdfid);

			pepNew.setCELLULAR_LOCATION(pep.getCELLULAR_LOCATION());

			if (pep instanceof sequenceParticipant)
			{
				for (sequenceFeature feat : ((sequenceParticipant) pep).getSEQUENCE_FEATURE_LIST())
				{
					((sequenceParticipant) pepNew).addSEQUENCE_FEATURE_LIST(feat);
				}
			}
			pepNew.setPHYSICAL_ENTITY(cmp);
			return pepNew;
		}
	}

	public String suggestCompartmentNameUsingMembers()
	{
		return suggestCompartmentNameUsingMembers(cmp.getCOMPONENTS());
	}

	public static String suggestCompartmentNameUsingMembers(
		Set<physicalEntityParticipant> memberParts)
	{
		List<String> names = new ArrayList<String>();

		for (physicalEntityParticipant pep : memberParts)
		{
			if (pep.getCELLULAR_LOCATION() != null)
			{
				names.add(pep.getCELLULAR_LOCATION().getTERM().iterator().next());
			}
		}
		if (!names.isEmpty()) return names.get(0);
		return null;
	}

	public List<ComplexMember> getRelatedMembers(physicalEntity pe)
	{
		List<ComplexMember> list = new ArrayList<ComplexMember>();

		for (Object o : getChildren())
		{
			ComplexMember mem = (ComplexMember) o;

			if (mem.getEntity() == pe)
			{
				list.add(mem);
			}
		}
		return list;
	}

	private static final Color COLOR = new Color(null, 140, 140, 140);
}
