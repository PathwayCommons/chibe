package org.gvt.model.biopaxl2;

import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.*;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.gvt.model.CompoundModel;
import org.gvt.model.EntityAssociated;
import org.gvt.util.EntityHolder;
import org.patika.mada.graph.Edge;
import org.patika.mada.graph.Node;

import java.util.*;

/**
 * Corresponds to inferred states in BioPAX file.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class Actor extends BioPAXNode implements EntityAssociated
{
	/**
	 * Related physical entity.
	 */
	protected physicalEntity entity;

	/**
	 * List of related physical entity participants.
	 */
	protected List<physicalEntityParticipant> participants;

	/**
	 * Constructor.
	 * @param root container node
	 */
	public Actor(CompoundModel root)
	{
		super(root);

		setColor(new Color(null, 150, 150, 150));
		setText("Actor");
		setSize(new Dimension(50, 20));
		setShape("RoundRect");
	}

	/**
	 * YAC
	 * @param root container node
	 * @param entity biopax physical entity that this class is based
	 * @param participants group of PEPs that indicates this state
	 */
	public Actor(CompoundModel root, physicalEntity entity,
		List<physicalEntityParticipant> participants)
	{
		this(root);

		this.entity = entity;
		this.participants = participants;
		sortParticipants();
		configFromModel();
	}

	public Actor(Actor excised, CompoundModel root)
	{
		super(excised, root);
		this.entity = excised.getEntity().l2pe;
		this.participants = excised.getParticipants();
		sortParticipants();
		getReferences().clear();
		configFromModel();
	}

	protected void sortParticipants()
	{
		Collections.sort(participants, new Comparator<physicalEntityParticipant>()
		{
			@Override
			public int compare(physicalEntityParticipant p1, physicalEntityParticipant p2)
			{
				return p1.getRDFId().compareTo(p2.getRDFId());
			}
		});
	}

	public void configFromModel()
	{
		// Extract references from entity
		String names = extractReferences(entity);

		String name = getDisplayName(entity);
		if (name == null)
		{
			name = "noname";
		}
		setText(name);

		setTooltipText(names);

		List<String> infos = getInfoStrings();

		int width, height;

		if (isUbique())
		{
			height = infos.isEmpty() ? DEFAULT_UBIQUE_HEIGHT :
				DEFAULT_UBIQUE_HEIGHT + 2 * DEFAULT_INFO_BULB;

			FontData data = getTextFont().getFontData()[0];
			data.setHeight(6);
			setTextFont(new Font(null, data));

			if (getName().startsWith("Phosphate") || getName().startsWith("phosphate") ||
				getName().startsWith("Orthophosphate") || getName().startsWith("orthophosphate") ||
				getName().startsWith("PPi") || getName().equals("Pi"))
			{
				setText("P");
			}
			else if (getName().startsWith("NTP ["))
			{
				setText("NTP");
			}

			width = Math.max(suggestInitialWidth(), 15);
		}
		else
		{
			width = suggestInitialWidth();

			if (!(entity instanceof smallMolecule) && width < MIN_INITIAL_WIDTH)
			{
				width = MIN_INITIAL_WIDTH;
			}

			height = infos.isEmpty() ? DEFAULT_HEIGHT : DEFAULT_HEIGHT + 2 * DEFAULT_INFO_BULB;
		}

		setSize(new Dimension(width, height));

		if (entity instanceof smallMolecule)
		{
			setColor(SMALL_MOL_BG_COLOR);
		}
		else
		{
//		    setColor(getEntitySpecificColor());
			setColor(getStringSpecificColor(getText()));
		}

		String shp = "RoundRect";

		if (!infos.isEmpty())
		{
			shp += "WithInfo";
			for (String info : infos)
			{
				shp += ";" + info;
			}
		}
		this.setShape(shp);

//		if (entity instanceof smallMolecule)
//		{
//			this.setSize(new Dimension(30, 15));
//		}
	}

	public EntityHolder getEntity()
	{
		return new EntityHolder(entity);
	}

	public List<physicalEntityParticipant> getParticipants()
	{
		return participants;
	}

	public Collection<? extends Level2Element> getRelatedModelElements()
	{
		return this.participants;
	}

	/**
	 * Reactome proteins have very long names which contain some short synomyms in paranthesis in
	 * the name. This method is a work around for creating short names from those long names. This
	 * method returns the shortest string found in paranthesis.
	 *
	 * @param longName long name
	 * @return some short name that is found in paranthesis.
	 */
	public static String getShortestNameInParanthesis(String longName)
	{
		int mark = 0;
		int i;
		String shortName = longName;

		while ((i = longName.indexOf("(", mark)) > 0 && longName.indexOf(")", i) > i)
		{
			String s = longName.substring(longName.indexOf("(", mark)+1,
				longName.indexOf(")", i));

			mark = longName.indexOf(")", i);

			if (s.length() < shortName.length())
			{
				shortName = s;
			}
		}
		return shortName;
	}

	public List<String> getInfoStrings()
	{
		List<String> list = new ArrayList<String>();

		if (!participants.isEmpty())
		{
			physicalEntityParticipant par = participants.get(0);
			if (par instanceof sequenceParticipant)
			{
				sequenceParticipant seq = (sequenceParticipant) par;
				Set<sequenceFeature> set = seq.getSEQUENCE_FEATURE_LIST();

				if (set != null)
				{
					for (sequenceFeature feat : set)
					{
						String featStr = feat.getSHORT_NAME();

						if (featStr == null || featStr.length() == 0)
						{
							openControlledVocabulary ocv = feat.getFEATURE_TYPE();

							if (ocv == null)
							{
								System.err.println("Has sequence feature with no feature short name" +
									" or feature type: " + this);
							}
							else if (ocv.getTERM() == null || ocv.getTERM().isEmpty())
							{
								System.err.println("Encountered an OCV without any term: " +
									this);
							}
							else
							{
								featStr = ocv.getTERM().iterator().next();
							}
						}

						Set<sequenceLocation> locSet = feat.getFEATURE_LOCATION();

						if (locSet == null)
						{
							System.err.println("sequence feature has a null feature location: " +
								this);
							continue;
						}

						for (sequenceLocation loc : locSet)
						{
							if (loc instanceof sequenceSite)
							{
								featStr += " @" + ((sequenceSite) loc).getSEQUENCE_POSITION();
							}
							else if (loc instanceof sequenceInterval)
							{
								sequenceInterval interval = (sequenceInterval) loc;

								if (interval.getSEQUENCE_INTERVAL_BEGIN() != null &&
									interval.getSEQUENCE_INTERVAL_END() != null)
								{
									featStr += " [" +
										interval.getSEQUENCE_INTERVAL_BEGIN().getSEQUENCE_POSITION() +
										"-" +
										interval.getSEQUENCE_INTERVAL_END().getSEQUENCE_POSITION() +
										"]";
								}
							}
						}
						list.add(featStr);
					}
				}
			}
		}

		return list;
	}

	public boolean hasInfoString()
	{
		if (!participants.isEmpty())
		{
			physicalEntityParticipant par = participants.get(0);
			if (par instanceof sequenceParticipant)
			{
				sequenceParticipant seq = (sequenceParticipant) par;
				Set<sequenceFeature> set = seq.getSEQUENCE_FEATURE_LIST();

				if (set != null && !set.isEmpty()) return true;
			}
		}
		return false;
	}

	/**
	 * Each RDF id is associated with a color.
	 * @return a color specific to physical entity
	 */
	public Color getEntitySpecificColor()
	{
		if (entity == null) return null;

		return super.getStringSpecificColor(entity.getRDFId());
	}

	public boolean isEvent()
	{
		return false;
	}

	public boolean sameEntity(Node n)
	{
		return n instanceof Actor &&
			this.entity.equals(((Actor) n).getEntity());
	}

	/**
	 * Actors and subclasses are breadth nodes.
	 * @return true
	 */
	public boolean isBreadthNode()
	{
		return true;
	}

	public String getIDHash()
	{
		return entity.getRDFId() + edgeHash();
	}

	public String edgeHash()
	{
		if (participants.isEmpty())
		{
			return "";
		}
		else
		{
			String hash = null;
			for (physicalEntityParticipant p : participants)
			{
				if (hash == null || p.getRDFId().compareTo(hash) < 0) hash = p.getRDFId(); 
			}
			return hash;
		}
	}

	public physicalEntityParticipant createNewPEP(Model model, String rdfid)
	{
		if (participants.isEmpty())
		{
			physicalEntityParticipant pepNew = model.addNew(physicalEntityParticipant.class, rdfid);

			pepNew.setPHYSICAL_ENTITY(entity);
			return pepNew;
		}
		else
		{
			physicalEntityParticipant pep = participants.get(0);

			physicalEntityParticipant pepNew = (physicalEntityParticipant)
				model.addNew(pep.getModelInterface(), rdfid);

			pepNew.setCELLULAR_LOCATION(pep.getCELLULAR_LOCATION());

			if (pep instanceof sequenceParticipant)
			{
				for (sequenceFeature feat : ((sequenceParticipant) pep).getSEQUENCE_FEATURE_LIST())
				{
					((sequenceParticipant) pepNew).addSEQUENCE_FEATURE_LIST(feat);
				}
			}
			pepNew.setPHYSICAL_ENTITY(entity);
			return pepNew;
		}
	}

	public boolean isUbique()
	{
		return entity instanceof smallMolecule && isUbiqueName(entity.getNAME());
	}

	public static boolean isUbiqueName(String name)
	{
		return
			name.startsWith("ATP") ||
			name.startsWith("ADP") ||
			name.startsWith("AMP") ||
			name.startsWith("adenosine 5'-monophosphate") ||
			name.startsWith("H2O") ||
			name.startsWith("H+") ||
			name.startsWith("Oxygen") ||
			name.startsWith("O2") ||
			name.startsWith("CO2") ||
			name.startsWith("GDP") ||
			name.startsWith("GTP") ||
			name.startsWith("PPi") ||
			name.equals("Pi") ||
			name.startsWith("Phosphate") ||
			name.startsWith("phosphate") ||
			name.startsWith("Orthophosphate") ||
			name.startsWith("orthophosphate") ||
			name.startsWith("NTP");
	}

	private boolean isEffector()
	{
		for (Edge edge : getDownstream())
		{
			if (edge instanceof EffectorFirstHalf || edge instanceof NonModulatedEffector)
			{
				return true;
			}
		}
		return false;
	}

	public List<String[]> getInspectable()
	{
		List<String[]> list = super.getInspectable();

		addNamesAndTypeAndID(list, entity);

		if (participants != null && !participants.isEmpty())
		{
			physicalEntityParticipant pep = participants.get(0);
			openControlledVocabulary loc = pep.getCELLULAR_LOCATION();
			if (loc != null)
			{
				list.add(new String[]{"Location", loc.toString()});
			}
		}

		for (String info : getInfoStrings())
		{
			String type = Character.isDigit(info.charAt(0)) ? "Stochiometry" : "Modification";
			list.add(new String[]{type, info});
		}

		addDataSourceAndXrefAndComments(list, entity);

		return list;
	}

	public static final int DEFAULT_HEIGHT = 20;
	public static final int DEFAULT_UBIQUE_HEIGHT = 15;
	public static final int DEFAULT_INFO_BULB = 6;
	public static final int MIN_INITIAL_WIDTH = 40;
	public static final Color SMALL_MOL_BG_COLOR = new Color(null, 255, 255, 255);
}
