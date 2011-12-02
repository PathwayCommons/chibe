package org.gvt.util;

import org.gvt.model.NodeModel;
import org.gvt.model.CompoundModel;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * Used for unifying compartment names in BioPAX model and for building nesting relationships
 * between compartments.
 *
 * @author Ozgun Babur
 */
public class CompartmentManager
{
	private static final String CYTOPLASM = "Cytoplasm";
	private static final String NUCLEUS = "Nucleus";
	private static final String CELL_MEMBRANE = "Cytoplasmic membrane";
	private static final String NUCLEAR_MEM = "Nuclear membrane";
	private static final String EXTRACELLULAR = "Extracellular";
	private static final String MITOCH_OUTER_MEM = "Mitochondrial Outer Membrane";
	private static final String MITOCH_INNER_MEM = "Mitochondrial Inner Membrane";
	private static final String MITOCH_INTER_MEM = "Mitochondrial Intermembrane Space";
	private static final String MITOCHONDRIA = "Mitochondria";
	private static final String ER_MEM = "Endoplasmic Reticulum Membrane";
	private static final String ER_LUM = "Endoplasmic Reticulum Lumen";
	private static final String ENDOSOME = "Endosome";
	private static final String ENDOSOME_MEM = "Endosome Membrane";
	private static final String GOLGI_MEM = "Golgi Membrane";
	private static final String GOLGI_LUM = "Golgi Lumen";
	private static final String CALCIUM_STORE = "Calcium Store";
	private static final String UNKNOWN = "Unknown Compartment";

	private static Map<String, String> unifierMap;

	/**
	 * Nests the first compartment under in the second cmpartment
	 * @param node to nest
	 * @param comp container
	 */
	public static void moveCompartmentIndside(NodeModel node, CompoundModel comp)
	{
		node.getParentModel().removeChild(node);
		comp.addChild(node);
		node.setParentModel(comp);
	}

	/**
	 * Creates a heuristic nesting for the given compartments. IF there is a cytoplasm, assumes any
	 * unknown compartment is nested under cytoplasm.
	 * @param map compartment map
	 */
	public static void nestCompartments(Map<String, CompoundModel> map)
	{
		CompoundModel cytoplasm = map.get(CYTOPLASM);

		if (cytoplasm != null)
		{
			CompoundModel cell_mem = map.get(CELL_MEMBRANE);
			CompoundModel extracel = map.get(EXTRACELLULAR);

			Set<CompoundModel> processed = new HashSet<CompoundModel>();

			if (extracel != null) processed.add(extracel);

			if (cell_mem !=  null)
			{
				moveCompartmentIndside(cytoplasm, cell_mem);
				processed.add(cell_mem);
			}

			processed.add(cytoplasm);

			for (CompoundModel cm : map.values())
			{
				if (!processed.contains(cm))
				{
					moveCompartmentIndside(cm, cytoplasm);
				}
			}
		}

		CompoundModel nuc_mem = map.get(NUCLEAR_MEM);
		CompoundModel nucleus = map.get(NUCLEUS);

		if (nuc_mem != null && nucleus != null) moveCompartmentIndside(nucleus, nuc_mem);

		CompoundModel mitoc = map.get(MITOCHONDRIA);
		CompoundModel mit_inn_mem = map.get(MITOCH_INNER_MEM);
		CompoundModel mit_out_mem = map.get(MITOCH_OUTER_MEM);
		CompoundModel mit_int_mem = map.get(MITOCH_INTER_MEM);

		if (mitoc != null)
		{
			if (mit_inn_mem != null) moveCompartmentIndside(mitoc, mit_inn_mem);
			else if (mit_int_mem != null) moveCompartmentIndside(mitoc, mit_int_mem);
			else if (mit_out_mem != null) moveCompartmentIndside(mitoc, mit_out_mem);
		}
		if (mit_inn_mem != null)
		{
			if (mit_int_mem != null) moveCompartmentIndside(mit_inn_mem, mit_int_mem);
			else if (mit_out_mem != null) moveCompartmentIndside(mit_inn_mem, mit_out_mem);
		}
		if (mit_int_mem != null)
		{
			if (mit_out_mem != null) moveCompartmentIndside(mit_int_mem, mit_out_mem);
		}

		CompoundModel endo = map.get(ENDOSOME);
		CompoundModel endo_mem = map.get(ENDOSOME_MEM);

		if (endo != null && endo_mem != null) moveCompartmentIndside(endo, endo_mem);

		CompoundModel gol_lum = map.get(GOLGI_LUM);
		CompoundModel gol_mem = map.get(GOLGI_MEM);

		if (gol_lum != null && gol_mem != null) moveCompartmentIndside(gol_lum, gol_mem);

		CompoundModel er_lum = map.get(ER_LUM);
		CompoundModel er_mem = map.get(ER_MEM);

		if (er_lum != null && er_mem != null) moveCompartmentIndside(er_lum, er_mem);
	}

	public static String getUnifiedName(String name)
	{
		if (name == null) return null;

		if (unifierMap.containsKey(name.toLowerCase()))
		{
			return unifierMap.get(name.toLowerCase());
		}
		return name;
	}

	static
	{
		unifierMap = new HashMap<String, String>();
		unifierMap.put(CYTOPLASM.toLowerCase(), CYTOPLASM);
		unifierMap.put("cytosol", CYTOPLASM);
		unifierMap.put("CCO-CYTOPLASM".toLowerCase(), CYTOPLASM);
		unifierMap.put(CELL_MEMBRANE.toLowerCase(), CELL_MEMBRANE);
		unifierMap.put("lipid raft", CELL_MEMBRANE);
		unifierMap.put("plasma membrane", CELL_MEMBRANE);
		unifierMap.put("integral to membrane", CELL_MEMBRANE);
		unifierMap.put("transmembrane", CELL_MEMBRANE);
		unifierMap.put(NUCLEAR_MEM.toLowerCase(), NUCLEAR_MEM);
		unifierMap.put("nuclear envelope", NUCLEAR_MEM);
		unifierMap.put(EXTRACELLULAR.toLowerCase(), EXTRACELLULAR);
		unifierMap.put("extracellular region", EXTRACELLULAR);
		unifierMap.put("tight junction", EXTRACELLULAR);
		unifierMap.put(MITOCH_OUTER_MEM.toLowerCase(), MITOCH_OUTER_MEM);
		unifierMap.put(MITOCH_INNER_MEM.toLowerCase(), MITOCH_INNER_MEM);
		unifierMap.put(MITOCH_INTER_MEM.toLowerCase(), MITOCH_INTER_MEM);
		unifierMap.put(MITOCHONDRIA.toLowerCase(), MITOCHONDRIA);
		unifierMap.put("mitochondrial matrix", MITOCHONDRIA);
		unifierMap.put(ER_MEM.toLowerCase(), ER_MEM);
		unifierMap.put(ER_LUM.toLowerCase(), ER_LUM);
		unifierMap.put(NUCLEUS.toLowerCase(), NUCLEUS);
		unifierMap.put("nucleoplasm", NUCLEUS);
		unifierMap.put(ENDOSOME.toLowerCase(), ENDOSOME);
		unifierMap.put("early endosome", ENDOSOME);
		unifierMap.put(ENDOSOME_MEM.toLowerCase(), ENDOSOME_MEM);
		unifierMap.put("early endosome membrane", ENDOSOME_MEM);
		unifierMap.put(GOLGI_MEM.toLowerCase(), GOLGI_MEM);
		unifierMap.put(GOLGI_LUM.toLowerCase(), GOLGI_LUM);
		unifierMap.put(CALCIUM_STORE.toLowerCase(), CALCIUM_STORE);
		unifierMap.put(UNKNOWN.toLowerCase(), UNKNOWN);
		unifierMap.put("cellular component unknown", UNKNOWN);
		unifierMap.put("cellular_component unknown", UNKNOWN);
	}
}

/*
	public static void moveCompartmentIndside(EditPart node, EditPart comp)
	{
		ChangeBoundsRequest req = new ChangeBoundsRequest(RequestConstants.REQ_ORPHAN);
		req.getEditParts().add(node);

		Command orphanCmd = node.getCommand(req);

		req.setType(RequestConstants.REQ_ADD);
		Command addCmd = comp.getCommand(req);

		orphanCmd.execute();
		addCmd.execute();
	}
*/

