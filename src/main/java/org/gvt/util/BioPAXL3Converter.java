package org.gvt.util;

import org.biopax.paxtools.controller.Completer;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.gvt.command.DeleteCommand;
import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;
import org.gvt.model.biopaxl3.*;

import java.util.*;

/**
 * GraphML reader class for loading graphml files
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class BioPAXL3Converter implements NodeProvider
{
	public static boolean nestCompartments = true;

	Map<String, NodeModel> nodeMap;
	Map<String, Set<Actor>> nodeSetMap;

	Model model;
	Pathway pathway;
	BioPAXL3Graph root;
	Set<String> idsOfInterest;

	public BioPAXL3Converter(BioPAXL3Graph root)
	{
		this.root = root;
		nodeMap = new HashMap<String, NodeModel>();
		nodeSetMap = new HashMap<String, Set<Actor>>();
		model = root.getBiopaxModel();
		pathway = root.getPathway().l3p;
		populateEntitiesOfFocus();
	}

	/**
	 * This method uses the Model object embedded in the root to create elements of
	 * BioPAX graph.
	 */
	public void convert()
	{
		if (nestCompartments) createAndNestCompartments();

		// Create events

		for (String id : idsOfInterest)
		{
			BioPAXElement ele = model.getByID(id);
			if (ele instanceof Conversion ||
				ele instanceof TemplateReaction ||
				(ele instanceof MolecularInteraction && Conf.drawPPI()))
			{
				getNode(ID.get(ele), root);
			}
		}

		// Create controls that control nothing

		for (String id : idsOfInterest)
		{
			BioPAXElement ele = model.getByID(id);
			if (ele instanceof Control)
			{
				if (ChbControl.controlNeedsToBeANode((Control) ele, this))
					getNode(ID.get(ele), root);
			}
		}

		// Create homologies

		Set<PhysicalEntity> reiter = new HashSet<PhysicalEntity>();
		Map<NodeModel, Set<NodeModel>> existing = new HashMap<NodeModel, Set<NodeModel>>();

		for (String id : idsOfInterest)
		{
			NodeModel node1 = getAnExistingNode(id);

			if (node1 != null && model.getByID(id) instanceof PhysicalEntity)
			{
				reiter.add((PhysicalEntity) model.getByID(id));
			}
		}
		while(!reiter.isEmpty())
		{
			Set<PhysicalEntity> newEnts = new HashSet<PhysicalEntity>();

			for (PhysicalEntity pe : reiter)
			{
				NodeModel node1 = getAnExistingNode(ID.get(pe));
				for (PhysicalEntity mem : pe.getMemberPhysicalEntity())
				{
					if (needsToBeDisplayed(ID.get(mem)))
					{
						NodeModel node2 = getAnExistingNode(ID.get(mem));
						if (node2 == null)
						{
							node2 = getNode(ID.get(mem), root);
							newEnts.add(mem);
						}

						if (existing.containsKey(node1) && existing.get(node1).contains(node2))
							continue;

						new Member(node1, node2);

						if (!existing.containsKey(node1))
							existing.put(node1, new HashSet<NodeModel>());
						existing.get(node1).add(node2);
					}
				}
				if (pe instanceof SimplePhysicalEntity)
				{
					EntityReference er = ((SimplePhysicalEntity) pe).getEntityReference();
					if (er != null && pe.getMemberPhysicalEntity().isEmpty())
					{
						for (EntityReference mem : er.getMemberEntityReference())
						{
							if (needsToBeDisplayed(ID.get(mem)))
							{
								NodeModel node2 = getAnExistingNode(ID.get(mem));
								if (node2 == null)
								{
									node2 = getNode(ID.get(mem), root);
									// todo check if we don't really need the below line
//									newEnts.add(mem);
								}

								if (existing.containsKey(node1) && existing.get(node1).contains(node2))
									continue;

								new Member(node1, node2);

								if (!existing.containsKey(node1))
									existing.put(node1, new HashSet<NodeModel>());
								existing.get(node1).add(node2);
							}
						}
					}
				}
				for (PhysicalEntity parent : pe.getMemberPhysicalEntityOf())
				{
					if (needsToBeDisplayed(ID.get(parent)))
					{
						NodeModel node2 = getAnExistingNode(ID.get(parent));
						if (node2 == null)
						{
							node2 = getNode(ID.get(parent), root);
							newEnts.add(parent);
						}

						if (existing.containsKey(node2) && existing.get(node2).contains(node1))
							continue;

						new Member(node2, node1);

						if (!existing.containsKey(node2))
							existing.put(node2, new HashSet<NodeModel>());
						existing.get(node2).add(node1);
					}
				}
			}
			reiter = newEnts;
		}

		// remove empty compartments

		List<Compartment> remove = new ArrayList<Compartment>();
		for (Object o : root.getNodes())
		{
			if (o instanceof Compartment)
			{
				Compartment com = (Compartment) o;
				if (com.getChildren().isEmpty())
				{
					remove.add(com);
				}
			}
		}
		for (Compartment com : remove)
		{
			DeleteCommand command = new DeleteCommand();
			command.setChild(com);
			command.setParent(com.getParentModel());
			command.execute();
		}


		// Pathways should have a display name in ChiBE
		for (Pathway p : model.getObjects(Pathway.class))
		{
			if (p.getDisplayName() == null)
			{
				if (p.getStandardName() != null) p.setDisplayName(p.getStandardName());
				else if (!p.getName().isEmpty()) p.setDisplayName(p.getName().iterator().next());
				else p.setDisplayName(ID.get(p));
			}
		}
	}

	private Hub createMolecularInteraction(MolecularInteraction inter)
	{
		if (Hub.needsToRepresentedWithANode(inter, this))
		{
			String compName = Hub.getPossibleCompartmentName(inter);

			if (nestCompartments) compName = CompartmentManager.getUnifiedName(compName);

			CompoundModel compart = compName == null ? root :
				(CompoundModel) nodeMap.get(compName);

			return new Hub(compart, inter, this);
		}
		else
		{
			List<NodeModel> interactors = new ArrayList<NodeModel>(2);

			for (Entity ent : inter.getParticipant())
			{
				NodeModel node = this.getNode(ID.get(ent), root);
				if (node != null) interactors.add(node);
			}

			assert interactors.size() == 2 :
				"There are not 2 interactors. Size is " + interactors.size();

			new Pairing(inter, interactors.get(0), interactors.get(1));
			return null;
		}
	}

	private NodeModel getAnExistingNode(String id)
	{
		if (nodeMap.containsKey(id)) return nodeMap.get(id);
		else if (nodeSetMap.containsKey(id)) return chooseOneOfMultipleNodes(id);
		else return null;
	}

	private NodeModel chooseOneOfMultipleNodes(String id)
	{
		List<Actor> nodes = new ArrayList<Actor>(nodeSetMap.get(id));
		Collections.sort(nodes, new Comparator<Actor>()
		{
			@Override
			public int compare(Actor o1, Actor o2)
			{
				return o1.getIDHash().compareTo(o2.getIDHash());
			}
		});
		return nodes.get(0);
	}

	private ChbTempReac createTemplateReaction(TemplateReaction reac)
	{
		String compName = ChbTempReac.getPossibleCompartmentName(reac);

		if (compName != null && nestCompartments)
			compName = CompartmentManager.getUnifiedName(compName);

		CompoundModel compart = compName == null ? root : (CompoundModel) nodeMap.get(compName);
		ChbTempReac tempReac = new ChbTempReac(compart, reac, this);
		tempReac.selectBestCompartment();
		return tempReac;
	}

	private ChbConversion createConversion(Conversion conv)
	{
		String compName = ChbConversion.getPossibleCompartmentName(conv);

		if (compName != null && nestCompartments)
			compName = CompartmentManager.getUnifiedName(compName);

		CompoundModel compart = compName == null ? root : (CompoundModel) nodeMap.get(compName);

		assert compart != null;

		ChbConversion forwd = null, rever = null;

		if (evidenceExists(conv, ChbConversion.LEFT_TO_RIGHT))
		{
			forwd = new ChbConversion(compart, conv, ChbConversion.LEFT_TO_RIGHT, this);
			forwd.selectBestCompartment();
		}
		if (evidenceExists(conv, ChbConversion.RIGHT_TO_LEFT))
		{
			rever = new ChbConversion(compart, conv, ChbConversion.RIGHT_TO_LEFT, this);
			rever.selectBestCompartment();
		}
		if (forwd == null && rever == null)
		{
			forwd = new ChbConversion(compart, conv, ChbConversion.LEFT_TO_RIGHT, this);
			forwd.selectBestCompartment();
		}
		if (forwd != null) return forwd;
		else return rever;
	}

	private CompoundModel getCompartment(PhysicalEntity pe)
	{
		if (pe.getCellularLocation() != null &&
			!pe.getCellularLocation().getTerm().isEmpty())
		{
			String compName = pe.getCellularLocation().getTerm().iterator().next();
			if (nestCompartments) compName = CompartmentManager.getUnifiedName(compName);

			Compartment comp = (Compartment) nodeMap.get(compName);

			if (comp == null)
			{
				comp = new Compartment(root, compName);
				nodeMap.put(compName, comp);
			}

			return comp;
		}
		else
		{
			return root;
		}
	}

	private CompoundModel getCompartment(String compName)
	{
		if (nestCompartments) compName = CompartmentManager.getUnifiedName(compName);

		Compartment comp = (Compartment) nodeMap.get(compName);

		if (comp == null)
		{
			comp = new Compartment(root, compName);
			nodeMap.put(compName, comp);
		}

		return comp;
	}

	private void populateEntitiesOfFocus()
	{
		idsOfInterest = new HashSet<String>();

		if (pathway != null)
		{
			Completer c = new Completer(SimpleEditorMap.L3);
			Set<BioPAXElement> s = new HashSet<BioPAXElement>();
			s.add(pathway);
			Set<BioPAXElement> completed = c.complete(s, model);
			for (BioPAXElement ele : completed)
			{
				idsOfInterest.add(ID.get(ele));
			}
		}
		else
		{
			for (BioPAXElement ele : model.getObjects())
			{
				idsOfInterest.add(ID.get(ele));
			}
		}
	}

	private void createAndNestCompartments()
	{
		assert nestCompartments;

		Map<String, CompoundModel> compMap = new HashMap<String, CompoundModel>();

		for (String id : idsOfInterest)
		{
			BioPAXElement ele = model.getByID(id);

			if (ele instanceof PhysicalEntity)
			{
				PhysicalEntity pe = (PhysicalEntity) ele;
				String compName = extractCellularLoc(pe);
				if (compName != null)
				{
					compName = CompartmentManager.getUnifiedName(compName);

					Compartment comp = (Compartment) nodeMap.get(compName);

					if (comp == null)
					{
						comp = new Compartment(root, compName);
						nodeMap.put(compName, comp);
						compMap.put(compName, comp);
					}
				}
			}
		}

		CompartmentManager.nestCompartments(compMap);
	}

	private String extractCellularLoc(PhysicalEntity pe)
	{
		if (pe.getCellularLocation() != null &&
			!pe.getCellularLocation().getTerm().isEmpty())
		{
			return pe.getCellularLocation().getTerm().iterator().next();
		}
		return null;
	}

	private Compartment extractCompartment(PhysicalEntity pe)
	{
		String compName = extractCellularLoc(pe);
		if (compName != null)
		{
			if (nestCompartments) compName = CompartmentManager.getUnifiedName(compName);
			Compartment cmp = (Compartment) nodeMap.get(compName);
			assert cmp != null;
			return cmp;
		}
		return null;
	}

	private NodeModel createSimplePhysicalEntity(Named peOrEr, CompoundModel relatedRoot)
	{
		assert !(peOrEr instanceof Complex);
		assert peOrEr instanceof EntityReference || peOrEr instanceof PhysicalEntity;

		// Fix naming irregularities of the entity

		if (peOrEr.getStandardName() == null)
		{
			if (peOrEr.getDisplayName() != null)
			{
				peOrEr.setStandardName(peOrEr.getDisplayName());
			}
			else if (!peOrEr.getName().isEmpty())
			{
				peOrEr.setStandardName(peOrEr.getName().iterator().next());
			}
			else if (peOrEr instanceof SimplePhysicalEntity)
			{
				EntityReference er = ((SimplePhysicalEntity) peOrEr).getEntityReference();
				if (er != null)
				{
					peOrEr.setStandardName(er.getStandardName());
					if (peOrEr.getStandardName() == null &&
						er.getName() != null && !er.getName().isEmpty())
					{
						peOrEr.setStandardName(er.getName().iterator().next());
					}
				}
			}
		}
		if (peOrEr.getStandardName() == null)
		{
			peOrEr.setStandardName("noname");
		}

		// Create simple states (Actors)

		CompoundModel parent = null;
		if (peOrEr instanceof PhysicalEntity) parent = extractCompartment((PhysicalEntity) peOrEr);
		if (parent == null) parent = root;

		Actor actor;
		if (peOrEr instanceof PhysicalEntity)
		{
			if (Actor.isUbique((PhysicalEntity) peOrEr))
				actor = new Actor(relatedRoot, (PhysicalEntity) peOrEr, null);
			else actor = new Actor(parent, (PhysicalEntity) peOrEr, null);
		}
		else actor = new Actor(parent, (EntityReference) peOrEr, null);


		if (actor.isUbique())
		{
			if (!nodeSetMap.containsKey(ID.get(peOrEr)))
				nodeSetMap.put(ID.get(peOrEr), new HashSet<Actor>());

			nodeSetMap.get(ID.get(peOrEr)).add(actor);
		}
		else nodeMap.put(ID.get(peOrEr), actor);

		return actor;
	}

	/**
	 * Fills in the complex with members.
	 */
	private void createComplexContent(ChbComplex c, Complex current_cmp, Complex top_cmp)
	{
		for (PhysicalEntity pe : current_cmp.getComponent())
		{
			if (pe instanceof Complex && !((Complex) pe).getComponent().isEmpty())
			{
				createComplexContent(c, (Complex) pe, top_cmp);
			}
			else
			{
				ComplexMember cm = new ComplexMember(c, pe, top_cmp);
				cm.setMultimerNo(getStoichiometry(current_cmp, pe));
				cm.setRelated(top_cmp);

				if (!nodeSetMap.containsKey(ID.get(pe)))
					nodeSetMap.put(ID.get(pe), new HashSet<Actor>());

				nodeSetMap.get(ID.get(pe)).add(cm);
			}
		}
	}

	private int getStoichiometry(Complex c, PhysicalEntity mem)
	{
		for (Stoichiometry st : c.getComponentStoichiometry())
		{
			if (st.getPhysicalEntity().equals(mem)) return (int) st.getStoichiometricCoefficient();
		}
		return 1;
	}
	
	private boolean hasSingleMultimerMember(Complex c)
	{
		return c.getComponent().size() == 1 &&
			c.getComponent().iterator().next() instanceof SimplePhysicalEntity &&
			c.getComponentStoichiometry().size() == 1 &&
			c.getComponentStoichiometry().iterator().next().getStoichiometricCoefficient() > 1;
	}
	
	/**
	 * Checks if there is an evidence that the conversion works in the specified
	 * direction.
	 */
	private boolean evidenceExists(Conversion conv, boolean direction)
	{
		// Try to find evidence in direction filed of controls.

		for (Control cont : conv.getControlledOf())
		{
			if (cont instanceof Catalysis)
			{
				CatalysisDirectionType dir = ((Catalysis) cont).getCatalysisDirection();

				if (dir != null)
				{
					if (direction == ChbConversion.LEFT_TO_RIGHT)
					{
						if (dir == CatalysisDirectionType.LEFT_TO_RIGHT)
						{
							return true;
						}
					}
					else
					{
						if (dir == CatalysisDirectionType.RIGHT_TO_LEFT)
						{
							return true;
						}
					}
				}
			}
		}

		// Try to understand from spontaneous field of conversion

		ConversionDirectionType cd = conv.getConversionDirection();

		if (cd == ConversionDirectionType.REVERSIBLE)
		{
			return true;
		}
		else if (direction == ChbConversion.LEFT_TO_RIGHT)
		{
			if (cd == ConversionDirectionType.LEFT_TO_RIGHT)
			{
				return true;
			}
		}
		else
		{
			if (cd == ConversionDirectionType.RIGHT_TO_LEFT)
			{
				return true;
			}
		}

		return false;
	}

	private NodeModel createComplex(Complex cmp)
	{

		CompoundModel compart = getCompartment(cmp);

		if (compart == root)
		{
			String nm = ChbComplex.suggestCompartmentNameUsingMembers(cmp.getComponent());

			if (nm != null)
			{
				if (nestCompartments) nm = CompartmentManager.getUnifiedName(nm);
				compart = getCompartment(nm);
			}
		}

		NodeModel nd;

		if (cmp.getComponent().isEmpty())
		{
			nd = new Actor(compart, cmp, null);
		}
		else if (hasSingleMultimerMember(cmp))
		{
			PhysicalEntity mem = cmp.getComponent().iterator().next();
			nd = new Actor(compart, mem, cmp);
			((Actor) nd).setMultimerNo(getStoichiometry(cmp, mem));
		}
		else
		{
			ChbComplex c = new ChbComplex(compart, cmp);
			createComplexContent(c, cmp, cmp);
			nd = c;
		}

		nodeMap.put(ID.get(cmp), nd);
		return nd;
	}

	@Override
	public NodeModel getNode(String id, CompoundModel relatedRoot)
	{
		if (nodeMap.containsKey(id))
		{
			return nodeMap.get(id);
		}

		if (!needsToBeDisplayed(id)) return null;

		BioPAXElement ele = model.getByID(id);

		if (ele instanceof Complex)
		{
			return createComplex((Complex) ele);
		}
		else if (ele instanceof PhysicalEntity || ele instanceof EntityReference)
		{
			return createSimplePhysicalEntity((Named) ele, relatedRoot);
		}
		else if (ele instanceof Control)
		{
			return new ChbControl(relatedRoot, (Control) ele, this);
		}
		else if (ele instanceof Pathway)
		{
			return new ChbPathway(relatedRoot, (Pathway) ele, this);
		}
		else if (ele instanceof Conversion)
		{
			return createConversion((Conversion) ele);
		}
		else if (ele instanceof TemplateReaction)
		{
			return createTemplateReaction((TemplateReaction) ele);
		}
		else if (ele instanceof MolecularInteraction && Conf.drawPPI())
		{
			return createMolecularInteraction((MolecularInteraction) ele);
		}
		else return null;
	}

	@Override
	public boolean needsToBeDisplayed(String id)
	{
		return idsOfInterest.contains(id);
	}

	@Override
	public void register(String id, NodeModel node)
	{
		nodeMap.put(id, node);
	}
}