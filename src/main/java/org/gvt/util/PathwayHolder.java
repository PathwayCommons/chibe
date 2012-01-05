package org.gvt.util;

import org.biopax.paxtools.model.level2.pathway;
import org.biopax.paxtools.model.level2.pathwayComponent;
import org.biopax.paxtools.model.level2.conversion;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.BioPAXLevel;
import org.patika.mada.graph.GraphObject;
import org.gvt.model.biopaxl2.Conversion;
import org.gvt.model.biopaxl3.ChbConversion;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class PathwayHolder
{
	public pathway l2p;
	public Pathway l3p;

	public PathwayHolder(pathway l2p)
	{
		this.l2p = l2p;
	}

	public PathwayHolder(Pathway l3p)
	{
		if (l3p.getDisplayName() == null && l3p.getStandardName() != null)
		{
			l3p.setDisplayName(l3p.getStandardName());
		}

		this.l3p = l3p;
	}

	public PathwayHolder(Model model)
	{
		String id = "http://www.chisio.org/user#" + System.currentTimeMillis();

		if (model.getLevel() == BioPAXLevel.L2)
		{
			l2p = BioPAXLevel.L2.getDefaultFactory().create(pathway.class, id);
			model.add(l2p);
		}
		else if (model.getLevel() == BioPAXLevel.L3)
		{
			l3p = BioPAXLevel.L3.getDefaultFactory().create(Pathway.class, id);
			model.add(l3p);
		}
	}

	public String getRDFID()
	{
		if (l2p != null)
		{
			return l2p.getRDFId();
		}
		else if (l3p != null)
		{
			return l3p.getRDFId();
		}
		else return null;
	}

	public String getName()
	{
		if (l2p != null)
		{
			return l2p.getNAME();
		}
		else if (l3p != null)
		{
			return l3p.getDisplayName();
		}
		else return null;
	}

	public void setName(String name)
	{
		if (l2p != null)
		{
			l2p.setNAME(name);
		}
		else if (l3p != null)
		{
			l3p.setDisplayName(name);
		}
	}

	public void removeFromModel(Model model)
	{
		if (l2p != null)
		{
			model.remove(l2p);
		}
		else if (l3p != null)
		{
			model.remove(l3p);
		}
	}

	public void updateContentWith(Collection<GraphObject> content)
	{
		if (l2p != null)
		{
			for (pathwayComponent pc : new HashSet<pathwayComponent>(l2p.getPATHWAY_COMPONENTS()))
			{
				l2p.removePATHWAY_COMPONENTS(pc);
			}

			Set<conversion> convSet = new HashSet<conversion>();

			for (GraphObject go : content)
			{
				if (go instanceof Conversion)
				{
					Conversion conv = (Conversion) go;
					conversion cnv = conv.getConversion();
					convSet.add(cnv);
				}
			}

			Set<pathwayComponent> components = l2p.getPATHWAY_COMPONENTS();

			for (conversion cnv : convSet)
			{
				if (!components.contains(cnv))
				{
					l2p.addPATHWAY_COMPONENTS(cnv);
				}
			}
		}
		else if (l3p != null)
		{
			for (Process pc : new HashSet<Process>(l3p.getPathwayComponent()))
			{
				l3p.removePathwayComponent(pc);
			}

			Set<org.biopax.paxtools.model.level3.Conversion> convSet =
				new HashSet<org.biopax.paxtools.model.level3.Conversion>();

			for (GraphObject go : content)
			{
				if (go instanceof ChbConversion)
				{
					ChbConversion conv = (ChbConversion) go;
					org.biopax.paxtools.model.level3.Conversion cnv = conv.getConversion();
					convSet.add(cnv);
				}
			}

			Set<Process> components = l3p.getPathwayComponent();

			for (org.biopax.paxtools.model.level3.Conversion cnv : convSet)
			{
				if (!components.contains(cnv))
				{
					l3p.addPathwayComponent(cnv);
				}
			}
		}
	}

	public boolean isEmpty()
	{
		if (l2p != null)
		{
			return l2p.getPATHWAY_COMPONENTS().isEmpty();
		}
		else if (l3p != null)
		{
			return l3p.getPathwayComponent().isEmpty();
		}
		else return true;
	}

	public boolean hasEdge()
	{
		if (l2p != null)
		{
			return !l2p.isPARTICIPANTSof().isEmpty() || !l2p.isCONTROLLEDOf().isEmpty();
		}
		else if (l3p != null)
		{
			return !l3p.getParticipantOf().isEmpty() || !l3p.getControlledOf().isEmpty() ||
				l3p.getControllerOf().isEmpty();
		}
		else return false;
	}
}
