package org.gvt.util;

import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.*;
import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;
import org.gvt.model.biopaxl2.*;

import java.util.*;

/**
 * GraphML reader class for loading graphml files
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class BioPAXL2Reader
{
	public static boolean nestCompartments = true;

	Model model;

	public BioPAXL2Reader(Model model)
	{
		this.model = model;
	}
    
    public Model getModel()
	{
        return this.model;
    }

	/**tr
	 * This method uses the Model object embedded in the root to create elements of
	 * BioPAX graph.
	 */
	public void createGraph(BioPAXL2Graph root)
	{
		Map<String, NodeModel> map = new HashMap<String, NodeModel>();
		
		Model model = root.getBiopaxModel();

//		for (xref xr : model.getObjects(xref.class))
//		{
//			UniquePrinter.print("xref ["+xr.getID()+"] = ", xr.getDB());
//		}

		if (nestCompartments) createAndNestCompartments(model, map, root);
		
		// Create actors
		
		for (physicalEntity pe : model.getObjects(physicalEntity.class))
		{
			// Complex will be processed later.
			if (pe instanceof complex) continue;

			// Create simple states (Actors)
			
			if (pe.isPHYSICAL_ENTITYof().isEmpty())
			{
				new Actor(root, pe, new ArrayList<physicalEntityParticipant>());
			}
			else
			{
				Collection<List<physicalEntityParticipant>> coll = groupParticipants(pe);
				
				for (List<physicalEntityParticipant> list : coll)
				{
					CompoundModel compart = getCompartment(list.get(0), map, root);
					
					Actor actor = new Actor(compart, pe, list);
					
					for (physicalEntityParticipant par : list)
					{
						map.put(par.getRDFId(), actor);
					}
				}
			}
		}
		
		// Create complexes

		for (complex cmp : model.getObjects(complex.class))
		{
			if (cmp.isPHYSICAL_ENTITYof().isEmpty())
			{
				Complex c = new Complex(root, cmp, new ArrayList<physicalEntityParticipant>(0));
				createComplexContent(c, cmp, new ArrayList<physicalEntityParticipant>());
			}
			else
			{
				Collection<List<physicalEntityParticipant>> coll = groupParticipants(cmp);
				
				for (List<physicalEntityParticipant> list : coll)
				{
					CompoundModel compart = getCompartment(list.get(0), map, root);

					if (compart == root)
					{
						String nm = Complex.suggestCompartmentNameUsingMembers(cmp.getCOMPONENTS());

						if (nm != null)
						{
							if (nestCompartments) nm = CompartmentManager.getUnifiedName(nm);
							compart = getCompartment(nm, map, root);
						}
					}

					NodeModel nd;

					if (cmp.getCOMPONENTS().isEmpty())
					{
						nd = new Actor(compart, cmp, list);
					}
					else
					{
						Complex c = new Complex(compart, cmp, list);
						createComplexContent(c, cmp, list);
						nd = c;
					}

					for (physicalEntityParticipant par : list)
					{
						map.put(par.getRDFId(), nd);
					}
				}
			}
		}		
		
		// Create events
		
		for (conversion conv : model.getObjects(conversion.class))
		{
			String compName = Conversion.getPossibleCompartmentName(conv);

			if (compName != null && nestCompartments)
				compName = CompartmentManager.getUnifiedName(compName);
			
			CompoundModel compart = compName == null ? root : (CompoundModel) map.get(compName);
			
			Conversion forwd = null, rever = null;
			
			if (evidenceExists(conv, Conversion.LEFT_TO_RIGHT))
			{
				forwd = new Conversion(compart, conv, Conversion.LEFT_TO_RIGHT, map);
				forwd.selectBestCompartment();
			}			
			if (evidenceExists(conv, Conversion.RIGHT_TO_LEFT))
			{
				rever = new Conversion(compart, conv, Conversion.RIGHT_TO_LEFT, map);
				rever.selectBestCompartment();
			}
			if (forwd == null && rever == null)
			{
				forwd = new Conversion(compart, conv, Conversion.LEFT_TO_RIGHT, map);
				forwd.selectBestCompartment();
			}
		}

		for (interaction inter : model.getObjects(interaction.class))
		{
			boolean drawPPI = true;
			if (!drawPPI) break;

			if (!(inter instanceof conversion) && !(inter instanceof control))
			{
				Set<InteractionParticipant> interPartic = inter.getPARTICIPANTS();
				Set<physicalEntityParticipant> peps = new HashSet<physicalEntityParticipant>();

				boolean hasControl = !inter.isCONTROLLEDOf().isEmpty();

				for (InteractionParticipant partic : interPartic)
				{
					if (partic instanceof physicalEntityParticipant)
					{
						peps.add((physicalEntityParticipant) partic);
					}
				}

				if (hasControl || peps.size() != 2)
				{
					String compName = Hub.getPossibleCompartmentName(peps);

					if (nestCompartments) compName = CompartmentManager.getUnifiedName(compName);

					CompoundModel compart = compName == null ? root :
						(CompoundModel) map.get(compName);

					Hub hub = new Hub(compart, inter,
						new ArrayList<physicalEntityParticipant>(peps), map);

					for (physicalEntityParticipant pep : peps)
					{
						NodeModel node = map.get(pep.getRDFId());

						// TEMPORARY CODE -- REMOVE WHEN BUG IS FIXED
						if (node == null)
						{
							System.err.println("PEP without PE");
							continue;
						}

						assert node != null;

						new MultiTouch(node, hub, pep);
					}
				}
				else // peps.size() == 2
				{
					NodeModel[] ends = new NodeModel[2];

					int i = 0;
					for (physicalEntityParticipant pep : peps)
					{
						ends[i++] = map.get(pep.getRDFId());
					}

					// TEMPORARY CODE -- REMOVE WHEN BUG IS FIXED
					if (ends[0] == null || ends[1] == null)
					{
						System.err.println("PEP without PE");
						continue;
					}

					assert ends[0] != null : "Source of a Pairing is null";
					assert ends[1] != null : "Target of a Pairing is null";

					new Pairing(inter, peps.iterator().next(), ends[0], ends[1]);
				}
			}
		}
	}

	private CompoundModel getCompartment(physicalEntityParticipant par, Map<String, NodeModel> map, 
		CompoundModel root)
	{
		if (par.getCELLULAR_LOCATION() != null &&
			!par.getCELLULAR_LOCATION().getTERM().isEmpty())
		{
			String compName = par.getCELLULAR_LOCATION().getTERM().iterator().next();
			if (nestCompartments) compName = CompartmentManager.getUnifiedName(compName);

			Compartment comp = (Compartment) map.get(compName);
			
			if (comp == null)
			{
				comp = new Compartment(root, compName);
				map.put(compName, comp);
			}
			
			return comp;
		}
		else
		{
			return root;
		}
	}

	private CompoundModel getCompartment(String compName, Map<String, NodeModel> map,
		CompoundModel root)
	{
		if (nestCompartments) compName = CompartmentManager.getUnifiedName(compName);

		Compartment comp = (Compartment) map.get(compName);

		if (comp == null)
		{
			comp = new Compartment(root, compName);
			map.put(compName, comp);
		}

		return comp;
	}

	private void createAndNestCompartments(Model model, Map<String, NodeModel> map, CompoundModel root)
	{
		assert nestCompartments;

		Map<String, CompoundModel> compMap = new HashMap<String, CompoundModel>();

		for (physicalEntityParticipant par : model.getObjects(physicalEntityParticipant.class))
		{
			if (par.getCELLULAR_LOCATION() != null &&
				!par.getCELLULAR_LOCATION().getTERM().isEmpty())
			{
				String compName = par.getCELLULAR_LOCATION().getTERM().iterator().next();
				compName = CompartmentManager.getUnifiedName(compName);

				Compartment comp = (Compartment) map.get(compName);

				if (comp == null)
				{
					comp = new Compartment(root, compName);
					map.put(compName, comp);
					compMap.put(compName, comp);
				}
			}
		}

		CompartmentManager.nestCompartments(compMap);
	}

	/**
	 * Fills in the complex with members.
	 */
	private void createComplexContent(Complex c, complex cmp, List<physicalEntityParticipant> upper)
	{
		for (physicalEntityParticipant par : cmp.getCOMPONENTS())
		{
			assert par.getPHYSICAL_ENTITY() != null : "physicalEntity of PEP is null";
			upper.add(0, par);

			physicalEntity pe = par.getPHYSICAL_ENTITY();

			if (pe instanceof complex && !((complex) pe).getCOMPONENTS().isEmpty())
			{
				createComplexContent(c, (complex) pe, upper);
			}
			else
			{
				new ComplexMember(c, pe, new ArrayList<physicalEntityParticipant>(upper));
			}

			upper.remove(0);
		}
	}
	
	/**
	 * Checks if there is an evidence that the conversion works in the specified
	 * direction.
	 */
	private boolean evidenceExists(conversion conv, boolean direction)
	{
		// Try to find evidence in direction filed of controls.
		
		for (control cont : conv.isCONTROLLEDOf())
		{
			if (cont instanceof catalysis)
			{
				Direction dir = ((catalysis) cont).getDIRECTION();
				
				if (dir != null)
				{
					if (dir == Direction.REVERSIBLE)
					{
						return true;
					}
					
					if (direction == Conversion.LEFT_TO_RIGHT)
					{
						if (dir == Direction.IRREVERSIBLE_LEFT_TO_RIGHT ||
							dir == Direction.PHYSIOL_LEFT_TO_RIGHT)
						{
							return true;
						}
					}
					else
					{
						if (dir == Direction.IRREVERSIBLE_RIGHT_TO_LEFT ||
							dir == Direction.PHYSIOL_RIGHT_TO_LEFT)
						{
							return true;
						}
					}
				}
			}
		}
		
		// Try to understand from spontaneous field of conversion
		
		SpontaneousType spon = conv.getSPONTANEOUS();
		
		boolean evident = spon != null && spon != SpontaneousType.NOT_SPONTANEOUS &&
			((direction == Conversion.LEFT_TO_RIGHT && spon == SpontaneousType.L_R) ||
			(direction == Conversion.RIGHT_TO_LEFT && spon == SpontaneousType.R_L));

		if (!evident) evident = conv.getCOMMENT().contains("reversible");
		return evident;
	}
	
	/**
	 * Iterates over participants and groups them according to their inferred states.
	 * Participants that point to a complex are ignored.
	 */
	private Collection<List<physicalEntityParticipant>> groupParticipants(physicalEntity pe)
	{
		Collection<List<physicalEntityParticipant>> set = 
			new ArrayList<List<physicalEntityParticipant>>();
		
		for (physicalEntityParticipant par : pe.isPHYSICAL_ENTITYof())
		{
			if (par.isCOMPONENTof() != null) continue;
			
			List<physicalEntityParticipant> parts = null;
			
			for (List<physicalEntityParticipant> e : set)
			{
				if (e.get(0).isInEquivalentState(par))
				{
					parts = e;
				}
			}
			
			if (isUbique(pe) || parts == null)
			{
				parts = new ArrayList<physicalEntityParticipant>();
				set.add(parts);
			}
			parts.add(par);
		}
		
		return set;
	}

	private boolean isUbique(physicalEntity pe)
	{
		return pe instanceof smallMolecule && Actor.isUbiqueName(pe.getNAME());
	}

	/**
	 * Checks if a PEP points to a PE which is null or not in model.
	 * @param model
	 */
	private void PEPCheck(Model model)
	{
		Map<String, physicalEntity> rdfToPE = new HashMap<String, physicalEntity>();
		for (physicalEntity pe : model.getObjects(physicalEntity.class))
		{
			rdfToPE.put(pe.getRDFId(), pe);
		}
		Set<physicalEntityParticipant> peps = model.getObjects(physicalEntityParticipant.class);

		for (physicalEntityParticipant pep : peps)
		{
			physicalEntity pe = pep.getPHYSICAL_ENTITY();

			if (pe == null)
			{
				System.err.println("PE of PEP is null. PEP id: " + pep.getRDFId());
			}
			else if (!rdfToPE.containsKey(pe.getRDFId()) || rdfToPE.get(pe.getRDFId()) != pe)
			{
				System.err.println("PE of PEP is not in model.\nPEP id: " + pep.getRDFId() +
					"\nPE id: " + pe.getRDFId());
			}
		}
	}
}
