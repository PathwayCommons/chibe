package org.gvt.util;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.xref;
import org.biopax.paxtools.model.level3.*;

import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class EntityHolder
{
	public physicalEntity l2pe;
	public PhysicalEntity l3pe;
	public EntityReference l3er;

	public EntityHolder(BioPAXElement element)
	{
		if (element instanceof physicalEntity)
		{
			this.l2pe = (physicalEntity) element;
		}
		else if (element instanceof PhysicalEntity)
		{
			this.l3pe = (PhysicalEntity) element;

			setReferenceFromPE();
		}
		else if (element instanceof EntityReference)
		{
			this.l3er = (EntityReference) element;
		}
		else
		{
			throw new IllegalArgumentException("Invalid object: " + element);
		}
	}

	private void setReferenceFromPE()
	{
		if (l3pe instanceof SimplePhysicalEntity)
		{
			l3er = ((SimplePhysicalEntity) l3pe).getEntityReference();
		}
	}

	public EntityHolder(physicalEntity l2pe)
	{
		this.l2pe = l2pe;
	}

	public EntityHolder(PhysicalEntity l3pe)
	{
		this.l3pe = l3pe;
		setReferenceFromPE();
	}

	public EntityHolder(EntityReference l3er)
	{
		this.l3er = l3er;
	}

	public int hashCode()
	{
		if (l2pe != null) return l2pe.hashCode();
		if (l3er != null) return l3er.hashCode();
		if (l3pe != null) return l3pe.hashCode();
		return 0;
	}

	public boolean equals(Object obj)
	{
		if (obj instanceof EntityHolder)
		{
			EntityHolder eh = (EntityHolder) obj;

			if (l2pe != null)
			{
				return eh.l2pe != null && l2pe.equals(eh.l2pe);
			}

			if (l3er != null)
			{
				return eh.l3er != null && l3er.equals(eh.l3er);
			}

			if (l3pe != null)
			{
				return eh.l3pe != null && l3pe.equals(eh.l3pe);
			}
		}
		throw new RuntimeException("comparison with: " + obj);
//		return false;
	}

	public String getName()
	{
		if (l2pe != null)
		{
			String txt = org.gvt.model.biopaxl2.BioPAXNode.getDisplayName(l2pe);
			if (txt != null) return txt;
			else return ID.get(l2pe);
		}
		else if (l3pe != null)
		{
			if (l3pe.getDisplayName() != null) return l3pe.getDisplayName();
			else if (l3pe.getStandardName() != null) return l3pe.getStandardName();
			else if (!l3pe.getName().isEmpty()) return l3pe.getName().iterator().next();
			else return ID.get(l3pe);
		}
		else if (l3er != null)
		{
			if (l3er.getDisplayName() != null) return l3er.getDisplayName();
			else if (l3er.getStandardName() != null) return l3er.getStandardName();
			else if (!l3er.getName().isEmpty()) return l3er.getName().iterator().next();
			else return ID.get(l3er);
		}
		else return null;
	}

	public String getID()
	{
		if (l2pe != null)
		{
			return ID.get(l2pe);
		}
		else if (l3pe != null)
		{
			return ID.get(l3pe);
		}
		else if (l3er != null)
		{
			return ID.get(l3er);
		}
		else return null;
	}

	public boolean containsWord(String word)
	{
		word = word.toLowerCase();

		if (l2pe != null)
		{
			if (l2pe.getNAME() != null && l2pe.getNAME().toLowerCase().contains(word)) return true;

			if (l2pe.getSHORT_NAME() != null &&
				l2pe.getSHORT_NAME().toLowerCase().contains(word)) return true;

			for (String s : l2pe.getSYNONYMS())
			{
				if (s.toLowerCase().contains(word)) return true;
			}

			for (xref ref : l2pe.getXREF())
			{
				if (ref.getID().toLowerCase().contains(word)) return true;
			}
		}
		else if (l3pe != null)
		{
			if (l3pe.getStandardName() != null &&
				l3pe.getStandardName().toLowerCase().contains(word)) return true;

			if (l3pe.getDisplayName() != null &&
				l3pe.getDisplayName().toLowerCase().contains(word)) return true;

			for (String s : l3pe.getName())
			{
				if (s.toLowerCase().contains(word)) return true;
			}

			for (Xref ref : l3pe.getXref())
			{
				if (ref.getId().toLowerCase().contains(word)) return true;
			}
		}
		else if (l3er != null)
		{
			if (l3er.getStandardName() != null &&
				l3er.getStandardName().toLowerCase().contains(word)) return true;

			if (l3er.getDisplayName() != null &&
				l3er.getDisplayName().toLowerCase().contains(word)) return true;

			for (String s : l3er.getName())
			{
				if (s.toLowerCase().contains(word)) return true;
			}

			for (Xref ref : l3er.getXref())
			{
				if (ref.getId() != null && ref.getId().toLowerCase().contains(word)) return true;
			}
		}

		return false;
	}

	public BioPAXElement getEntity()
	{
		if (l3er != null) return l3er;
		if (l3pe != null) return l3pe;
		return l2pe;
	}

	public Named getNamed()
	{
		if (l3pe != null) return l3pe;
		if (l3er != null) return l3er;
		return null;
	}
}
