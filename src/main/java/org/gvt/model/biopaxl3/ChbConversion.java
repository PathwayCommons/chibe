package org.gvt.model.biopaxl3;

import org.biopax.paxtools.model.level3.*;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;
import org.patika.mada.graph.Edge;
import org.patika.mada.util.Ranker;

import java.util.*;

/**
 * Conversions in BioPax file.
 * 
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class ChbConversion extends BioPAXNode
{
	private Conversion conv;
	private boolean direction;

	/**
	 * Transcriptional relation.
	 */
	private boolean t;
	
	public ChbConversion(CompoundModel root)
	{
		super(root);
		
		setColor(COLOR);
		setText("");
		setSize(new Dimension(12, 12));
		setShape("Rectangle");
	}

	public ChbConversion(CompoundModel root, Conversion conv, boolean direction,
		Map<String, NodeModel> map)
	{
		this(root);
		this.conv = conv;
		this.direction = direction;
		configFromModel();
		buildConnections(root, conv, direction, map);
	}

	public ChbConversion(ChbConversion excised, CompoundModel root)
	{
		super(excised, root);
		this.conv = excised.getConversion();
		this.direction = excised.getDirection();
		configFromModel();
	}

	public void configFromModel()
	{
		extractReferences(conv);
		setTooltipText(conv.getStandardName());

		if (util.hasModelTag(BioPAXL3Graph.DEPLETING_REACTION_TAG))
		{
			setText("d");
		}
		if (util.hasModelTag(BioPAXL3Graph.TRANSCRIPTION_TAG))
		{
			t = true;
//			setText("t");
		}
	}

	public boolean isT()
	{
		return t;
	}

	public Conversion getConversion()
	{
		return conv;
	}

	public boolean getDirection()
	{
		return direction;
	}

	public Collection<? extends Level3Element> getRelatedModelElements()
	{
		return Arrays.asList(conv);
	}

	private void buildConnections(CompoundModel root, Conversion conv, boolean direction,
		Map<String, NodeModel> map)
	{
		// Will be used when inferring if this conversion is a transcription
//		boolean prodIsActor = false;

		// Create substrate and products.
		
		Set<PhysicalEntity> subsSet = direction == LEFT_TO_RIGHT ?
			conv.getLeft() : conv.getRight();

		Set<PhysicalEntity> prodSet = direction == RIGHT_TO_LEFT ?
			conv.getLeft() : conv.getRight();
		
		for (PhysicalEntity ent : subsSet)
		{
			NodeModel sub = mapLookup(ent, conv, map);
			new Substrate(sub, this);
		}
		for (PhysicalEntity par : prodSet)
		{
			NodeModel prod = mapLookup(par, conv, map);
			new Product(this, prod);
		}

		// Infer if this conversion is a transcription

		if (!t && subsSet.isEmpty() && prodSet.size() == 1)
		{
			t = true;
//			this.setText("t");
			util.recordModelTag(BioPAXL3Graph.TRANSCRIPTION_TAG, "");
		}

		// Create effectors.
		
		for (Control con : conv.getControlledOf())
		{
			if (map.containsKey(con.getRDFId()))
			{
				ChbControl cont = (ChbControl) map.get(con.getRDFId());
				new EffectorSecondHalf(cont, this, cont.getControl());
			}
			else if (con.getControlledOf().isEmpty() && con.getController().size() == 1)
			{
				NodeModel source = map.get(con.getController().iterator().next().getRDFId());
				new NonModulatedEffector(source, this, con, conv);
			}
			else
			{
				ChbControl ctrl = new ChbControl(root, con, this, map);
				map.put(con.getRDFId(), ctrl);
			}
		}
	}

	private NodeModel mapLookup(PhysicalEntity pe, Conversion conv, Map<String, NodeModel> map)
	{
		NodeModel nm = map.get(pe.getRDFId());
		if (nm == null) nm = map.get(pe.getRDFId() + conv.getRDFId());
		return nm;
	}
	public static String getPossibleCompartmentName(Conversion conv)
	{
		Set<String> names = new HashSet<String>();
		
		for (PhysicalEntity ent : conv.getLeft())
		{
			getCompartmentName(names, ent);
		}
		for (PhysicalEntity ent : conv.getRight())
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
		return isT();
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

		addNamesAndTypeAndID(list, conv);

		for (Evidence ev : conv.getEvidence())
		{
			list.add(new String[]{"Evidence", ev.toString()});
		}

		if (!conv.getInteractionType().isEmpty())
		{
			String s = formatInString(conv.getInteractionType());
			list.add(new String[]{"Interaction Type", s});
		}

		Boolean spo = conv.getSpontaneous();

		if (spo != null)
		{
			list.add(new String[]{"Spontaneous", spo.toString()});
		}

		addDataSourceAndXrefAndComments(list, conv);

		return list;
	}

	public String getIDHash()
	{
		return conv.getRDFId() + direction;
	}

	public boolean isDepleting()
	{
		return util.hasModelTag(BioPAXL3Graph.DEPLETING_REACTION_TAG);
	}

	private static final Color COLOR = new Color(null, 170, 170, 170);

	public static final boolean LEFT_TO_RIGHT = true;
	public static final boolean RIGHT_TO_LEFT = false;
}
