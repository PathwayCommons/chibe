package org.gvt.model.biopaxl2;

import org.biopax.paxtools.model.level2.*;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;
import org.gvt.util.ID;
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
public class Conversion extends BioPAXNode
{
	private conversion conv;
	private boolean direction;

	/**
	 * Transcriptional relation.
	 */
	private boolean t;
	
	public Conversion(CompoundModel root)
	{
		super(root);
		
		setColor(COLOR);
		setText("");
		setSize(new Dimension(12, 12));
		setShape("Rectangle");
	}

	public Conversion(CompoundModel root, conversion conv, boolean direction,
		Map<String, NodeModel> map)
	{
		this(root);
		this.conv = conv;
		this.direction = direction;
		configFromModel();
		buildConnections(root, conv, direction, map);
	}

	public Conversion(Conversion excised, CompoundModel root)
	{
		super(excised, root);
		this.conv = excised.getConversion();
		this.direction = excised.getDirection();
		configFromModel();
	}

	public void configFromModel()
	{
		extractReferences(conv);
		setTooltipText(conv.getNAME());

		if (util.hasModelTag(BioPAXL2Graph.DEPLETING_REACTION_TAG))
		{
			setText("d");
		}
		if (util.hasModelTag(BioPAXL2Graph.TRANSCRIPTION_TAG))
		{
			t = true;
//			setText("t");
		}
	}

	public boolean isT()
	{
		return t;
	}

	public conversion getConversion()
	{
		return conv;
	}

	public boolean getDirection()
	{
		return direction;
	}

	public Collection<? extends Level2Element> getRelatedModelElements()
	{
		return Arrays.asList(conv);
	}

	private void buildConnections(CompoundModel root, conversion conv, boolean direction,
		Map<String, NodeModel> map)
	{
		// Will be used when inferring if this conversion is a transcription
//		boolean prodIsActor = false;

		// Create substrate and products.
		
		Set<physicalEntityParticipant> subsSet = direction == LEFT_TO_RIGHT ?
			conv.getLEFT() : conv.getRIGHT();

		Set<physicalEntityParticipant> prodSet = direction == RIGHT_TO_LEFT ?
			conv.getLEFT() : conv.getRIGHT();
		
		for (physicalEntityParticipant par : subsSet)
		{
			NodeModel sub = map.get(ID.get(par));
			new Substrate(sub, this, par);
		}
		for (physicalEntityParticipant par : prodSet)
		{
			NodeModel prod = map.get(ID.get(par));
			new Product(this, prod, par);
		}

		// Infer if this conversion is a transcription

		if (!t && subsSet.isEmpty() && prodSet.size() == 1)
		{
			t = true;
//			this.setText("t");
			util.recordModelTag(BioPAXL2Graph.TRANSCRIPTION_TAG, "");
		}

		// Create effectors.
		
		for (control con : conv.isCONTROLLEDOf())
		{
			if (map.containsKey(ID.get(con)))
			{
				Control cont = (Control) map.get(ID.get(con));
				new EffectorSecondHalf(cont, this, cont.getControl());
			}
			else if (con.isCONTROLLEDOf().isEmpty() && con.getCONTROLLER().size() == 1)
			{
				NodeModel source = map.get(ID.get(con.getCONTROLLER().iterator().next()));
				new NonModulatedEffector(source, this, con,
					con.getCONTROLLER().iterator().next(), conv);
			}
			else
			{
				Control ctrl = new Control(root, con, this, map);
				map.put(ID.get(con), ctrl);
			}
		}
	}
	
	public static String getPossibleCompartmentName(conversion conv)
	{
		Set<String> names = new HashSet<String>();
		
		for (physicalEntityParticipant par : conv.getLEFT())
		{
			if (par.getCELLULAR_LOCATION() != null &&
				!par.getCELLULAR_LOCATION().getTERM().isEmpty())
			{
				names.add(par.getCELLULAR_LOCATION().getTERM().iterator().next());
			}
		}
		for (physicalEntityParticipant par : conv.getRIGHT())
		{
			if (par.getCELLULAR_LOCATION() != null &&
				!par.getCELLULAR_LOCATION().getTERM().isEmpty())
			{
				names.add(par.getCELLULAR_LOCATION().getTERM().iterator().next());
			}
		}
		
		if (names.size() == 1) return names.iterator().next();
		else return null;
	}
	
	public static List<String> getPossibleCompartmentNames(conversion conv)
	{
		List<String> names = new ArrayList<String>();

		for (physicalEntityParticipant par : conv.getLEFT())
		{
			if (par.getCELLULAR_LOCATION() != null)
			{
				names.add(par.getCELLULAR_LOCATION().getTERM().iterator().next());
			}
		}
		for (physicalEntityParticipant par : conv.getRIGHT())
		{
			if (par.getCELLULAR_LOCATION() != null)
			{
				names.add(par.getCELLULAR_LOCATION().getTERM().iterator().next());
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

		for (evidence ev : conv.getEVIDENCE())
		{
			list.add(new String[]{"Evidence", ev.toString()});
		}

		if (!conv.getINTERACTION_TYPE().isEmpty())
		{
			String s = formatInString(conv.getINTERACTION_TYPE());
			list.add(new String[]{"Interaction Type", s});
		}

		SpontaneousType spo = conv.getSPONTANEOUS();

		if (spo != null)
		{
			list.add(new String[]{"Spontaneous", spo.toString()});
		}

		addDataSourceAndXrefAndComments(list, conv);

		return list;
	}

	public String getIDHash()
	{
		return ID.get(conv) + direction;
	}

	public boolean isDepleting()
	{
		return util.hasModelTag(BioPAXL2Graph.DEPLETING_REACTION_TAG);
	}

	private static final Color COLOR = new Color(null, 170, 170, 170);

	public static final boolean LEFT_TO_RIGHT = true;
	public static final boolean RIGHT_TO_LEFT = false;
}
