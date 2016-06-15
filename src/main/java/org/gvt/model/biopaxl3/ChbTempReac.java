package org.gvt.model.biopaxl3;

import org.biopax.paxtools.model.level3.*;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;
import org.gvt.util.ID;
import org.gvt.util.NodeProvider;
import org.patika.mada.graph.Edge;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;
import org.patika.mada.util.Ranker;

import java.util.*;

/**
 * Conversions in BioPax file.
 * 
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class ChbTempReac extends BioPAXNode
{
	private TemplateReaction tr;

	public ChbTempReac(CompoundModel root)
	{
		super(root);

		setColor(COLOR);
		setText("t");
		setSize(new Dimension(8, 12));
		setShape("Rectangle");
	}

	public ChbTempReac(CompoundModel root, TemplateReaction tr, NodeProvider prov)
	{
		this(root);
		this.tr = tr;
		configFromModel();
		buildConnections(root, tr, prov);
	}

	public ChbTempReac(ChbTempReac excised, CompoundModel root)
	{
		super(excised, root);
		this.tr = excised.getTemplateReaction();
		configFromModel();
	}

	public void configFromModel()
	{
		extractReferences(tr);
		setTooltipText(tr.getDisplayName());
	}

	public TemplateReaction getTemplateReaction()
	{
		return tr;
	}

	public Collection<? extends Level3Element> getRelatedModelElements()
	{
		return Arrays.asList(tr);
	}

	private void buildConnections(CompoundModel root, TemplateReaction tr, NodeProvider prov)
	{
		// CreateTemplate

		NucleicAcid template = tr.getTemplate();
		if (template != null)
		{
			NodeModel tmp = prov.getNode(ID.get(template), root);
			new Template(tmp, this);
		}

		// Create products.

		for (PhysicalEntity ent : tr.getProduct())
		{
			NodeModel prd = prov.getNode(ID.get(ent), root);
			new Product(this, prd);
		}

		createControlOverInteraction(root, tr, prov);
	}

	private NodeModel mapLookup(PhysicalEntity pe, TemplateReaction tr, Map<String, NodeModel> map)
	{
		NodeModel nm = map.get(ID.get(pe));
		if (nm == null) nm = map.get(ID.get(pe) + ID.get(tr));
		return nm;
	}
	public static String getPossibleCompartmentName(TemplateReaction tr)
	{
		Set<String> names = new HashSet<String>();
		
		if (tr.getTemplate() != null) getCompartmentName(names, tr.getTemplate());

		for (PhysicalEntity ent : tr.getProduct())
		{
			getCompartmentName(names, ent);
		}
		
		if (names.size() == 1) return names.iterator().next();
		else return null;
	}

	private static void getCompartmentName(Set<String> names, PhysicalEntity ent)
	{
		if (ent.getCellularLocation() != null &&
			!ent.getCellularLocation().getTerm().isEmpty())
		{
			names.add(ent.getCellularLocation().getTerm().iterator().next());
		}
	}

	public static List<String> getPossibleCompartmentNames(Conversion conv)
	{
		List<String> names = new ArrayList<String>();

		for (PhysicalEntity ent : conv.getLeft())
		{
			if (ent.getCellularLocation() != null)
			{
				names.add(ent.getCellularLocation().getTerm().iterator().next());
			}
		}
		for (PhysicalEntity ent : conv.getRight())
		{
			if (ent.getCellularLocation() != null)
			{
				names.add(ent.getCellularLocation().getTerm().iterator().next());
			}
		}
		return names;
	}

	/**
	 * Select the comparment that contain max number of neighbors. if there are more than one
	 * compartment of this type, then consider parent compartments of these compartments,
	 * re-calculate the score.
	 */
	public void selectBestCompartment()
	{
		List<NodeModel> neighbors = getNeighborsList();

		Ranker<CompoundModel> r = new Ranker<CompoundModel>();

		for (NodeModel neigh : neighbors)
		{
			if (neigh instanceof ComplexMember) neigh = neigh.getParentModel();
			CompoundModel cm = neigh.getParentModel();
			if (cm != null) r.count(cm);
		}

		List<List<CompoundModel>> list = r.getRankedList();
		if (list.isEmpty()) return;

		List<CompoundModel> firstGroup = list.get(0);

		CompoundModel comp;

		if (firstGroup.size() == 1)
		{
			comp = firstGroup.get(0);
		}
		else
		{
			for (NodeModel neigh : neighbors)
			{
				if (neigh instanceof ComplexMember) neigh = neigh.getParentModel();
				CompoundModel cm = neigh.getParentModel();
				if (cm != null) cm = cm.getParentModel();
				if (cm != null) r.count(cm);
			}

			list = r.getRankedList();
			List<CompoundModel> secondGroup = list.get(0);

			assert !secondGroup.isEmpty();

			if (secondGroup.size() == 1)
			{
				comp = secondGroup.get(0);
			}
			else
			{
				ArrayList<CompoundModel> copySec = new ArrayList<CompoundModel>(secondGroup);
				copySec.retainAll(firstGroup);

				if (!copySec.isEmpty())
				{
					secondGroup = copySec;
				}

				comp = secondGroup.get(0);

				if (!(comp instanceof Compartment) && secondGroup.size() > 1)
				{
					comp = secondGroup.get(1);
				}
			}
		}
		this.getParentModel().removeChild(this);
		comp.addChild(this);
		this.setParentModel(comp);
	}

	public boolean isEvent()
	{
		return true;
	}

	public boolean isTranscriptionEvent()
	{
		return true;
	}

	private Set<Node> tabu;
	public Set<Node> getTabuNodes()
	{
		// Return substrates

		if (tabu == null)
		{
			tabu = new HashSet<Node>();

			for (Edge edge : getUpstream())
			{
				if (edge instanceof Substrate)
				{
					tabu.add(edge.getSourceNode());
				}
			}
		}
		return new HashSet<Node>(tabu);
	}

	public Set<GraphObject> getRequisites()
	{
		Set<GraphObject> reqs = super.getRequisites();
		reqs.addAll(this.getSourceConnections());
		reqs.addAll(this.getTargetConnections());
		return reqs;
	}

	public List<String[]> getInspectable()
	{
		List<String[]> list = super.getInspectable();

		addNamesAndTypeAndID(list, tr);

		for (Evidence ev : tr.getEvidence())
		{
			list.add(new String[]{"Evidence", ev.toString()});
		}

		if (!tr.getInteractionType().isEmpty())
		{
			String s = formatInString(tr.getInteractionType());
			list.add(new String[]{"Interaction Type", s});
		}

		addDataSourceAndXrefAndComments(list, tr);

		return list;
	}

	public String getIDHash()
	{
		return ID.get(tr);
	}

	private static final Color COLOR = new Color(null, 230, 230, 230);
}
