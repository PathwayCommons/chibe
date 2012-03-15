package org.gvt.model.biopaxl3;

import org.biopax.paxtools.model.level3.*;
import org.eclipse.swt.graphics.Color;
import org.gvt.model.CompoundModel;
import org.gvt.model.EntityAssociated;
import org.gvt.util.EntityHolder;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.util.XRef;

import java.util.*;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class ChbComplex extends BioPAXCompoundNode implements EntityAssociated
{
	/**
	 * BioPAX complex.
	 */
	private Complex cmp;

	public ChbComplex(CompoundModel root)
	{
		super(root);
		
		setText("Complex");
	}

	public Collection<? extends Level3Element> getRelatedModelElements()
	{
		return Arrays.asList(cmp);
	}

	public ChbComplex(CompoundModel root, Complex cmp)
	{
		this(root);

		this.cmp = cmp;

		extractReferences();
		
		setText((cmp.getDisplayName() != null && cmp.getDisplayName().length() > 0) ?
			cmp.getDisplayName() : cmp.getStandardName());

		setTooltipText(getText());
		
		setColor(COLOR);
	}

	/**
	 * Constructor for excising.
	 * @param toexcise
	 * @param root
	 */
	public ChbComplex(ChbComplex toexcise, CompoundModel root)
	{
		super(toexcise, root);
		this.cmp = toexcise.getComplex();
	}

	/**
	 * Extract cross-references from the based complex.
	 */
	private void extractReferences()
	{
		for (Xref xr : cmp.getXref())
		{
			this.addReference(new XRef(xr));
		}
	}

	public Complex getComplex()
	{
		return cmp;
	}

	public EntityHolder getEntity()
	{
		return new EntityHolder(cmp);
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

		CellularLocationVocabulary voc = cmp.getCellularLocation();
		if (voc != null && !voc.getTerm().isEmpty())
		{
			list.add(new String[]{"Location", voc.getTerm().iterator().next()});
		}

		BioPAXNode.addDataSourceAndXrefAndComments(list, cmp);

		return list;
	}

	public String getIDHash()
	{
		return cmp.getRDFId();
	}

	public String suggestCompartmentNameUsingMembers()
	{
		return suggestCompartmentNameUsingMembers(cmp.getComponent());
	}

	public static String suggestCompartmentNameUsingMembers(
		Set<PhysicalEntity> members)
	{
		List<String> names = new ArrayList<String>();

		for (PhysicalEntity mem : members)
		{
			CellularLocationVocabulary voc = mem.getCellularLocation();
			if (voc != null && !voc.getTerm().isEmpty())
			{
				names.add(voc.getTerm().iterator().next());
			}
		}
		if (!names.isEmpty()) return names.get(0);
		return null;
	}

	public List<ComplexMember> getRelatedMembers(PhysicalEntity pe)
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
