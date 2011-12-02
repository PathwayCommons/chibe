package org.gvt.util;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.gvt.ChisioMain;
import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;
import org.gvt.model.biopaxl3.*;
import org.patika.mada.util.XRef;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * GraphML reader class for loading graphml files
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class BioPAXL3Reader
{
	public static boolean nestCompartments = true;

	Model model;

	private BioPAXIOHandler reader;

	public BioPAXL3Reader()
	{
		this.reader = new SimpleIOHandler(BioPAXLevel.L3);
	}

	public BioPAXL3Reader(Model model)
	{
		this.model = model;
	}

    public Model getModel()
	{
        return this.model;
    }

    public CompoundModel readXMLFile(File xmlFile)
	{
		BioPAXL3Graph root = null;

		XRef.clearDBSet();

		try
		{
			if (model == null)
			{
				model = reader.convertFromOWL(new FileInputStream(xmlFile));
			}

			if (model != null)
			{
				root = new BioPAXL3Graph(model);

				createGraph(root);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			MessageBox messageBox = new MessageBox(
				new Shell(),
				SWT.ERROR_UNSUPPORTED_FORMAT);
			messageBox.setMessage("File cannot be loaded!");
			messageBox.setText(ChisioMain.TOOL_NAME);
			messageBox.open();

			return null;
		}

		return root;
	}

	/**tr
	 * This method uses the Model object embedded in the root to create elements of
	 * BioPAX graph.
	 */
	protected void createGraph(BioPAXL3Graph root)
	{
		Map<String, NodeModel> map = new HashMap<String, NodeModel>();

		Model model = root.getBiopaxModel();

//		for (xref xr : model.getObjects(xref.class))
//		{
//			UniquePrinter.print("xref ["+xr.getID()+"] = ", xr.getDB());
//		}

		if (nestCompartments) createAndNestCompartments(model, map, root);

		// Create actors

		for (PhysicalEntity pe : model.getObjects(PhysicalEntity.class))
		{
			if (pe.getStandardName() == null)
			{
				if (pe.getDisplayName() != null)
				{
					pe.setStandardName(pe.getDisplayName());
				}
				else if (!pe.getName().isEmpty())
				{
					pe.setStandardName(pe.getName().iterator().next());
				}
				else if (pe instanceof SimplePhysicalEntity)
				{
					EntityReference er = ((SimplePhysicalEntity) pe).getEntityReference();
					if (er != null)
					{
						pe.setStandardName(er.getStandardName());
						if (pe.getStandardName() == null &&
							er.getName() != null && !er.getName().isEmpty())
						{
							pe.setStandardName(er.getName().iterator().next());
						}
					}
				}
			}
			if (pe.getStandardName() == null)
			{
				pe.setStandardName("noname");
			}

			// Complex will be processed later.
			if (pe instanceof Complex) continue;

			// Create simple states (Actors)

			CompoundModel parent = extractCompartment(pe, map);
			if (parent == null) parent = root;

			if (!isUbique(pe))
			{
				if (pe.getComponentOf().isEmpty() || !pe.getParticipantOf().isEmpty())
				{
					Actor actor = new Actor(parent, pe, null);
					map.put(pe.getRDFId(), actor);
				}
			}
			else
			{
				for (Interaction inter : pe.getParticipantOf())
				{
					Actor actor = new Actor(parent, pe, inter);
					map.put(pe.getRDFId() + inter.getRDFId(), actor);
				}
			}
		}

		// Create complexes

		for (Complex cmp : model.getObjects(Complex.class))
		{
			if (cmp.getParticipantOf().isEmpty())
			{
				ChbComplex c = new ChbComplex(root, cmp);
				createComplexContent(c, cmp, cmp, map);
			}
			else
			{
				CompoundModel compart = getCompartment(cmp, map, root);

				if (compart == root)
				{
					String nm = ChbComplex.suggestCompartmentNameUsingMembers(cmp.getComponent());

					if (nm != null)
					{
						if (nestCompartments) nm = CompartmentManager.getUnifiedName(nm);
						compart = getCompartment(nm, map, root);
					}
				}

				NodeModel nd;

				if (cmp.getComponent().isEmpty())
				{
					nd = new Actor(compart, cmp, null);
				}
				else
				{
					ChbComplex c = new ChbComplex(compart, cmp);
					createComplexContent(c, cmp, cmp, map);
					nd = c;
				}

				map.put(cmp.getRDFId(), nd);
			}
		}

		// Create events

		for (Conversion conv : model.getObjects(Conversion.class))
		{
			String compName = ChbConversion.getPossibleCompartmentName(conv);

			if (compName != null && nestCompartments)
				compName = CompartmentManager.getUnifiedName(compName);

			CompoundModel compart = compName == null ? root : (CompoundModel) map.get(compName);

//			if (compart == null)
//			{
//				System.out.println("");
//			}

			assert compart != null;
			
			ChbConversion forwd = null, rever = null;

			if (evidenceExists(conv, ChbConversion.LEFT_TO_RIGHT))
			{
				forwd = new ChbConversion(compart, conv, ChbConversion.LEFT_TO_RIGHT, map);
				forwd.selectBestCompartment();
			}
			if (evidenceExists(conv, ChbConversion.RIGHT_TO_LEFT))
			{
				rever = new ChbConversion(compart, conv, ChbConversion.RIGHT_TO_LEFT, map);
				rever.selectBestCompartment();
			}
			if (forwd == null && rever == null)
			{
				forwd = new ChbConversion(compart, conv, ChbConversion.LEFT_TO_RIGHT, map);
				forwd.selectBestCompartment();
			}
		}

//		for (TemplateReaction reac : model.getObjects(TemplateReaction.class))
//		{
//			String compName = ChbConversion.getPossibleCompartmentName(reac);
//
//			if (compName != null && nestCompartments)
//				compName = CompartmentManager.getUnifiedName(compName);
//
//			CompoundModel compart = compName == null ? root : (CompoundModel) map.get(compName);
//
//		}

		for (Interaction inter : model.getObjects(Interaction.class))
		{
			boolean drawPPI = true;
			if (!drawPPI) break;

			if (!(inter instanceof Conversion) && !(inter instanceof Control))
			{
				// We constraint the participant list to Physical Entities only
				Set<Entity> interPartic = inter.getParticipant();
				Set<PhysicalEntity> pes = new HashSet<PhysicalEntity>();
				for (Entity e : interPartic)
				{
					if (e instanceof PhysicalEntity) pes.add((PhysicalEntity) e);
				}
				if (pes.isEmpty()) continue;

				boolean hasControl = !inter.getControlledOf().isEmpty();

				if (hasControl || pes.size() != 2)
				{
					String compName = Hub.getPossibleCompartmentName(pes);

					if (nestCompartments) compName = CompartmentManager.getUnifiedName(compName);

					CompoundModel compart = compName == null ? root :
						(CompoundModel) map.get(compName);

					Hub hub = new Hub(compart, inter, map);

					for (PhysicalEntity pe : pes)
					{
						NodeModel node = map.get(pe.getRDFId());

						assert node != null;

						new MultiTouch(node, hub);
					}
				}
				else // peps.size() == 2
				{
					NodeModel[] ends = new NodeModel[2];

					int i = 0;
					for (PhysicalEntity pe : pes)
					{
						ends[i++] = map.get(pe.getRDFId());
					}

					// TEMPORARY CODE -- REMOVE WHEN BUG IS FIXED
					if (ends[0] == null || ends[1] == null)
					{
						System.err.println("PE not converted!");
						continue;
					}

					assert ends[0] != null : "Source of a Pairing is null";
					assert ends[1] != null : "Target of a Pairing is null";

					new Pairing(inter, ends[0], ends[1]);
				}
			}
		}
	}

	private CompoundModel getCompartment(PhysicalEntity pe, Map<String, NodeModel> map,
		CompoundModel root)
	{
		if (pe.getCellularLocation() != null &&
			!pe.getCellularLocation().getTerm().isEmpty())
		{
			String compName = pe.getCellularLocation().getTerm().iterator().next();
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

		for (PhysicalEntity par : model.getObjects(PhysicalEntity.class))
		{
			String compName = extractCellularLoc(par);
			if (compName != null)
			{
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

	private String extractCellularLoc(PhysicalEntity pe)
	{
		if (pe.getCellularLocation() != null &&
			!pe.getCellularLocation().getTerm().isEmpty())
		{
			return pe.getCellularLocation().getTerm().iterator().next();
		}
		return null;
	}

	private Compartment extractCompartment(PhysicalEntity pe, Map<String, NodeModel> map)
	{
		String compName = extractCellularLoc(pe);
		if (compName != null)
		{
			if (nestCompartments) compName = CompartmentManager.getUnifiedName(compName);
			Compartment cmp = (Compartment) map.get(compName);
			assert cmp != null;
			return cmp;
		}
		return null;
	}

	/**
	 * Fills in the complex with members.
	 */
	private void createComplexContent(ChbComplex c, Complex current_cmp, Complex top_cmp,
		Map<String, NodeModel> map)
	{
		for (PhysicalEntity pe : current_cmp.getComponent())
		{
			if (pe instanceof Complex && !((Complex) pe).getComponent().isEmpty())
			{
				createComplexContent(c, (Complex) pe, top_cmp, map);
			}
			else
			{
				ComplexMember cm = new ComplexMember(c, pe, top_cmp);
				if (!map.containsKey(pe.getRDFId())) map.put(pe.getRDFId(), cm);
			}
		}
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


	private boolean isUbique(PhysicalEntity pe)
	{
		return pe instanceof SmallMolecule && Actor.isUbiqueName(pe.getStandardName());
	}
}