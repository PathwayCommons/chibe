package org.gvt.model.biopaxl3;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.gvt.model.CompoundModel;
import org.gvt.model.EntityAssociated;
import org.gvt.util.Conf;
import org.gvt.util.EntityHolder;
import org.patika.mada.graph.Edge;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Corresponds to inferred states in BioPAX file.
 *
 * @author Ozgun Babur
 */
public class Actor extends BioPAXNode implements EntityAssociated
{
	/**
	 * Related physical entity.
	 */
	protected EntityHolder entity;

	/**
	 * A related physical entity. This field is used for ubiques. Ubiques are identified with their
	 * neighbor.
	 */
	protected Entity related;

	int multimerNo;

	/**
	 * This is the list of IDs of ubiquotous small molecules in Pathway Commons 2.
	 */
	private static Blacklist blacklist;

	/**
	 * Constructor.
	 *
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
	 *
	 * @param root   container node
	 * @param entity biopax physical entity that this class is based
	 */
	public Actor(CompoundModel root, PhysicalEntity entity, Entity related)
	{
		this(root);

		this.entity = new EntityHolder(entity);
		this.related = related;
		this.multimerNo = 1;
		configFromModel();
	}

	public Actor(CompoundModel root, EntityReference entity, Entity related)
	{
		this(root);

		this.entity = new EntityHolder(entity);
		this.related = related;
		this.multimerNo = 1;
		configFromModel();
	}

	public Actor(Actor excised, CompoundModel root)
	{
		super(excised, root);
		this.entity = excised.getEntity();
		this.related = excised.getRelated();
		this.multimerNo = excised.multimerNo;
		getReferences().clear();
		configFromModel();
	}

	public int getMultimerNo()
	{
		return multimerNo;
	}

	public void setMultimerNo(int multimerNo)
	{
		this.multimerNo = multimerNo;
	}

	public boolean isMultimer()
	{
		return multimerNo > 1;
	}

	public void configFromModel()
	{
		// Extract references from entity
		String names = extractReferences(entity.getNamed());

		String name = getDisplayName(entity.getNamed());
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
			} else if (getName().startsWith("NTP ["))
			{
				setText("NTP");
			}

			width = Math.max(suggestInitialWidth(), 15);
			setBorderColor(UBIQUE_BORDER_COLOR);
		} else
		{
			width = suggestInitialWidth();

			if (!(entity.getNamed() instanceof SmallMolecule) && width < MIN_INITIAL_WIDTH)
			{
				width = MIN_INITIAL_WIDTH;
			}

			height = infos.isEmpty() ? DEFAULT_HEIGHT : DEFAULT_HEIGHT + 2 * DEFAULT_INFO_BULB;
		}

		setSize(new Dimension(width, height));

		if (entity.getNamed() instanceof SmallMolecule)
		{
			setColor(SMALL_MOL_BG_COLOR);
		} else
		{
			setColor(getEntitySpecificColor());
//			setColor(getStringSpecificColor(getText()));
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
	}

	public EntityHolder getEntity()
	{
		return entity;
	}

	public Entity getRelated()
	{
		return related;
	}

	public void setRelated(Entity related)
	{
		this.related = related;
	}

	public Collection<? extends Level3Element> getRelatedModelElements()
	{
		Collection<Level3Element> col = new HashSet<Level3Element>();
		col.add(entity.getNamed());
		return col;
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
			String s = longName.substring(longName.indexOf("(", mark) + 1,
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

		if (entity.l3pe != null)
		{
			extractFeatures(list, entity.l3pe.getFeature(), false);
			extractFeatures(list, entity.l3pe.getNotFeature(), true);
		}

		return list;
	}

	static boolean showFragmentFeature = Conf.getBoolean(Conf.DISPLAY_FRAGMENT_FEATURE);

	private void extractFeatures(List<String> list, Set<EntityFeature> feats, boolean not)
	{
		for (EntityFeature feat : feats)
		{
			String featStr = null;

			if (feat instanceof ModificationFeature)
			{
				ModificationFeature mf = (ModificationFeature) feat;
				SequenceModificationVocabulary voc = mf.getModificationType();

				if (voc != null)
				{
					Set<String> terms = voc.getTerm();

					if (!terms.isEmpty())
					{
						if (terms.size() > 1)
						{
							System.err.print("Terms has more than one term. First: ");
							Iterator<String> iter = terms.iterator();
							System.err.print(iter.next() + "  Second:");
							System.err.println(iter.next());
						}

						featStr = terms.iterator().next();
					}
				} else featStr = "?";
			} else if (showFragmentFeature && feat instanceof FragmentFeature)
			{
				featStr = "fragment";
			}

			if (featStr != null)
			{
				SequenceLocation loc = feat.getFeatureLocation();
				if (loc instanceof SequenceSite)
				{
					SequenceSite ss = (SequenceSite) loc;

					featStr += " @" + ss.getSequencePosition();
				}

				if (not) featStr = "-" + featStr;

				list.add(featStr);
			}
		}
	}

	public boolean hasInfoString()
	{
		if (entity.l3pe != null)
		{
			for (EntityFeature feat : entity.l3pe.getFeature())
			{
				if (feat instanceof ModificationFeature ||
					feat instanceof FragmentFeature)
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Each RDF id is associated with a color.
	 *
	 * @return a color specific to physical entity
	 */
	public Color getEntitySpecificColor()
	{
		EntityHolder ent = getEntity();
		if (ent.l3er != null) return super.getStringSpecificColor(ent.l3er.getRDFId());
		else if (ent.l3pe != null)
		{
			List<String> memErs = getMemberEntityIDs(ent.l3pe, new ArrayList<String>());

			if (!memErs.isEmpty())
			{
				Collections.sort(memErs);

				String s = "";
				for (String memEr : memErs)
				{
					s += memEr;
				}
				return super.getStringSpecificColor(s);
			} else return super.getStringSpecificColor(ent.l3pe.getRDFId());
		} else return null;
	}

	protected List<String> getMemberEntityIDs(PhysicalEntity pe, List<String> list)
	{
		for (PhysicalEntity mem : pe.getMemberPhysicalEntity())
		{
			if (mem instanceof SimplePhysicalEntity)
			{
				SimplePhysicalEntity spe = (SimplePhysicalEntity) mem;
				EntityReference er = spe.getEntityReference();

				if (er != null)
				{
					if (!list.contains(er.getRDFId())) list.add(er.getRDFId());
				} else
				{
					getMemberEntityIDs(spe, list);
				}
			}
		}
		return list;
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
	 *
	 * @return true
	 */
	public boolean isBreadthNode()
	{
		return true;
	}

	public String getIDHash()
	{
		return entity.getID() + (related != null ? related.getRDFId() : "");
	}

	public boolean isUbique()
	{
		return entity.l3pe != null && (isIDUbique(entity.l3pe) ||
			entity.l3pe instanceof SmallMolecule && isUbiqueName(entity.l3pe.getDisplayName()));

	}

	public static boolean isUbique(PhysicalEntity pe)
	{
		return isIDUbique(pe) || (pe instanceof SmallMolecule && isUbiqueName(pe.getDisplayName()));
	}

	public static boolean isIDUbique(PhysicalEntity pe)
	{
		if (blacklist == null) blacklist = fetchBlackListFromPC();
		return blacklist.isUbique(pe);
	}

	public static Blacklist getBlackList()
	{
		if (blacklist == null) blacklist = fetchBlackListFromPC();
		return blacklist;
	}

	public static boolean isUbiqueName(String name)
	{
		return name != null &&
			(name.startsWith("ATP") ||
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
				name.equals("NAD") ||
				name.equals("NADH") ||
				name.equals("NAD+") ||
				name.equals("NADP") ||
				name.equals("NADPH") ||
				name.equals("NADP+") ||
				name.equals("FAD") ||
				name.equals("FADH") ||
				name.equals("FADH2") ||
				name.startsWith("Phosphate") ||
				name.startsWith("phosphate") ||
				name.startsWith("Orthophosphate") ||
				name.startsWith("orthophosphate") ||
				name.startsWith("NTP"));

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

		addNamesAndTypeAndID(list, entity.getNamed());

		CellularLocationVocabulary voc = null;
		if (entity.l3pe != null)
		{
			voc = entity.l3pe.getCellularLocation();
		}

		if (voc != null && !voc.getTerm().isEmpty())
		{
			list.add(new String[]{"Location", voc.getTerm().iterator().next()});
		}

		for (String info : getInfoStrings())
		{
			String type = Character.isDigit(info.charAt(0)) ? "Stochiometry" : "Modification";
			list.add(new String[]{type, info});
		}

		addDataSourceAndXrefAndComments(list, entity.getNamed());

		EntityHolder ent = getEntity();
		if (ent.l3er != null)
		{
			addNamesAndTypeAndID(list, ent.l3er);

			for (Xref xr : ent.l3er.getXref())
			{
				list.add(new String[]{"Reference", xr.toString()});
			}
		}

		return list;
	}

	public Set<GraphObject> getRequisites()
	{
		Set<GraphObject> reqs = super.getRequisites();
//		for (Object o : getSourceConnections())
//		{
//			if (o instanceof Member) reqs.add((Member) o);
//		}
		for (Object o : getTargetConnections())
		{
			if (o instanceof Member) reqs.add((Member) o);
		}
		return reqs;
	}

	private static Blacklist fetchBlackListFromPC()
	{
		try
		{
//			URL url = new URL(Conf.get(Conf.PATHWAY_COMMONS_URL) + "downloads/blacklist.txt");
			URL url = new URL(Conf.getBlacklistURL());
			URLConnection con = url.openConnection();
			return new Blacklist((InputStream) con.getContent());
		}
		catch (Exception e)
		{
			System.out.println("Cannot fetch blacklist from Pathway Commons.");
			return new Blacklist();
		}
	}

	public static final int DEFAULT_HEIGHT = 20;
	public static final int DEFAULT_UBIQUE_HEIGHT = 15;
	public static final int DEFAULT_INFO_BULB = 6;
	public static final int MIN_INITIAL_WIDTH = 40;
	public static final Color SMALL_MOL_BG_COLOR = new Color(null, 255, 255, 255);
	public static final Color UBIQUE_BORDER_COLOR = new Color(null, 130, 130, 130);
}
