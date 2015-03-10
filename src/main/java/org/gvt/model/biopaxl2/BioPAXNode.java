package org.gvt.model.biopaxl2;

import org.biopax.paxtools.model.level2.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.TextUtilities;
import org.gvt.command.CreateCommand;
import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;
import org.patika.mada.graph.Edge;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;
import org.patika.mada.util.ExperimentData;
import org.patika.mada.util.Representable;
import org.patika.mada.util.XRef;

import java.util.*;

/**
 * Any node to use in BioPAX visual graph.
 * 
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public abstract class BioPAXNode extends NodeModel implements IBioPAXL2Node
{
	/**
	 * Unique ID of this node.
	 */
	private int id;

	private List<XRef> references;

	protected NodeUtil util;

	public BioPAXNode(CompoundModel root)
	{
		CreateCommand command = new CreateCommand(root, this);
		command.execute();

		this.references = new ArrayList<XRef>();
		this.util = new NodeUtil(this);
	}

	/**
	 * Constructor for excising.
	 * @param excised original graph member
	 */
	public BioPAXNode(BioPAXNode excised, CompoundModel root)
	{
		this(root);
		this.id = excised.id;
		this.references.addAll(excised.getReferences());
		this.setShape(excised.getShape());
		this.setSize(excised.getSize());
		this.setColor(excised.getColor());
		this.setText(excised.getText());
		this.setTextColor(excised.getTextColor());
		this.setTooltipText(excised.getTooltipText());

		for (Object key : excised.getAllLabels())
		{
			this.putLabel(key, excised.getLabel(key));
		}

		getGraph().putInExcisionMap(excised, this);
		this.putLabel(BioPAXL2Graph.EXCISED_FROM, excised);
	}

	public boolean isComplexMember()
	{
		return false;
	}

	/**
	 * Empty method will be overwritten in children when the node needes to be configured by the
	 * properties of its corresponding biopax model obejects.
	 */
	public void configFromModel()
	{
		// Assume there is no configuration needed.
	}

	public BioPAXL2Graph getGraph()
	{
		return util.getGraph();
	}

	public Compartment getCompartment()
	{
		return util.getCompartment();
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getName()
	{
		return this.getText();
	}

	public NodeUtil getUtil()
	{
		return util;
	}

	public List<XRef> getReferences()
	{
		return this.references;
	}

	public void addReference(XRef ref)
	{
		if (!this.references.contains(ref))
		{
			this.references.add(ref);
		}
	}

	@Override
	public List<XRef> getSecondaryReferences()
	{
		return Collections.emptyList();
	}

	/**
	 * Extract cross-references from the based entity.
	 * @return list of possible names
	 */
	protected String extractReferences(entity ent)
	{
		Set<String> nameSet = new HashSet<String>();

		if (ent.getSHORT_NAME() != null && ent.getSHORT_NAME().length() > 0)
		{
			this.addReference(new XRef(NAME_REF, ent.getSHORT_NAME()));
			if(ent.getSHORT_NAME() != null) nameSet.add(ent.getSHORT_NAME());
		}

		if (ent.getNAME() != null && ent.getNAME().length() > 0)
		{
			this.addReference(new XRef(NAME_REF, ent.getNAME()));
			nameSet.add(ent.getNAME());
		}

		for (String syn : ent.getSYNONYMS())
		{
			if (syn != null)
			{
				XRef ref = new XRef(NAME_REF, syn);

				if (!this.getReferences().contains(ref))
				{
					this.addReference(ref);
					nameSet.add(syn);
				}
			}
		}

//		for (xref xr : new ClassFilterSet<unificationXref>(ent.getXREF(), unificationXref.class))
		for (xref xr : ent.getXREF())
		{
			if (xr != null)
			{
				this.addReference(new XRef(xr));
			}
		}
		String names = "";

		Iterator<String> iter = nameSet.iterator();
		while (iter.hasNext())
		{
			String nm = iter.next();
			names += nm;
			if (iter.hasNext()) nm += "\n";
		}

		return names;
	}

	protected int suggestInitialWidth()
	{
		Dimension dim = TextUtilities.INSTANCE.getStringExtents(getText(), getTextFont());
		int width = Math.min(dim.width + 4, MAX_INITIAL_WIDTH);
		return width;
	}

	/**
	 * Searches references and finds the shortest name reference.
	 * @return first shortest name reference
	 */
	public String getShortestNameRef()
	{
		int shortest = Integer.MAX_VALUE;
		String name = null;

		for (XRef ref : references)
		{
			if (ref.getDb().equals(NAME_REF))
			{
				String value = ref.getRef();

				if (value.length() < shortest)
				{
					name = value;
					shortest = value.length();
				}
			}
		}
		return name;
	}

	public static String getDisplayName(entity ent)
	{
		String txt = extractGeneSymbol(ent);

		if (txt == null && !(ent instanceof smallMolecule))
		{
			txt = ent.getSHORT_NAME();
		}

		if (txt == null)
		{
			txt = ent.getNAME();

			for (String syn : ent.getSYNONYMS())
			{
				if (txt == null || syn.length() < txt.length())
				{
					txt = syn;
				}
			}

			if (ent.getSHORT_NAME() != null &&
				(txt == null || txt.length() > ent.getSHORT_NAME().length()))
			{
				txt = ent.getSHORT_NAME();
			}
		}

		return txt;
	}

	protected static String extractGeneSymbol(entity ent)
	{
		String sym = null;

		for (xref ref : ent.getXREF())
		{
			String db = ref.getDB();

			if (db == null) continue;
			
			if (db.equalsIgnoreCase("GENE_SYMBOL") ||
				db.equalsIgnoreCase("HGNC SYMBOL") ||
				db.equalsIgnoreCase("GENESYMBOL") ||
				db.equalsIgnoreCase("GENE SYMBOL") ||
				db.equalsIgnoreCase("GENE-SYMBOL") ||
				db.equalsIgnoreCase("SYMBOL"))
			{
				sym = ref.getID();
				break;
			}
		}
		return sym;
	}

	//----------------------------------------------------------------------------------------------
	// Section: Location related
	//----------------------------------------------------------------------------------------------

	public boolean fetchLocation(String pathwayRDFID)
	{
		return util.fetchLocation(pathwayRDFID);
	}

	/**
	 * Records location of this node in to the related biopax elemnts.
	 */
	public void recordLocation()
	{
		util.recordLocation();
	}

	/**
	 * Records location of this node in to the related biopax elemnts.
	 */
	public void eraseLocation()
	{
		util.eraseLocation();
	}

	/**
	 * A node is assumed to map unique biopax model element by default and id hash is not used. This
	 * method must be overwritten in children when mapping clashes occur, e.g. when drawing two
	 * conversions for in chisio for representing a reversible conversion in biopax.
	 *
	 * This method should not depend on Java's hashing Strings.
	 */
	public String getIDHash()
	{
		return util.getIDHash();
	}

	//----------------------------------------------------------------------------------------------
	// Section: Model tagging
	//----------------------------------------------------------------------------------------------

	public boolean hasModelTag(String tag)
	{
		return util.hasModelTag(tag);
	}

	public String fetchModelTag(String tag)
	{
		return util.fetchModelTag(tag);
	}

	//----------------------------------------------------------------------------------------------
	// Section: Experiment data related
	//----------------------------------------------------------------------------------------------

	public Representable getRepresentableData(Object key)
	{
		return (Representable) this.getLabel(key);
	}

	public ExperimentData getExperimentData(String type)
	{
		return (ExperimentData) this.getLabel(type);
	}

	public void setExperimentData(ExperimentData data)
	{
		this.putLabel(data.getKey(), data);
	}

	public boolean hasExperimentData(Object key)
	{
		return this.hasLabel(key);
	}

	public boolean hasSignificantExperimentalChange(String type)
	{
		return this.hasExperimentData(type) && this.getExperimentData(type).isSignificant();
	}

	public int getExperimentDataSign(String type)
	{
		return this.getExperimentData(type).getSign();
	}

	//----------------------------------------------------------------------------------------------
	// Section: Traversing
	//----------------------------------------------------------------------------------------------

	public Collection<? extends Node> getParents()
	{
		CompoundModel p = this.getParentModel();
		Collection<Node> col = new ArrayList<Node>(1);

		if (p == null || p.isRoot())
		{
			return col;
		}
		else
		{
			col.add((BioPAXCompoundNode) p);
			return col;
		}
	}

	public Collection<? extends Edge> getUpstream()
	{
		return this.getTargetConnections();
	}

	public Collection<? extends Edge> getDownstream()
	{
		return this.getSourceConnections();
	}

	public Collection<? extends Node> getChildren()
	{
		return new ArrayList<Node>(0);
	}

	public boolean sameEntity(Node n)
	{
		return n.equals(this);
	}

	/**
	 * No node is a breadth node by default.
	 * @return false by default
	 */
	public boolean isBreadthNode()
	{
		return false;
	}

	public String toString()
	{
		return this.getName();
	}

	public boolean isHighlighted()
	{
		return this.isHighlight();
	}

	/**
	 * Nothing is a transcriptional event by default. Will be overwritten in some children.
	 * @return false by default
	 */
	public boolean isTranscriptionEvent()
	{
		return false;
	}

	public Set<Node> getTabuNodes()
	{
		return Collections.<Node>emptySet();
	}

	public Set<GraphObject> getRequisites()
	{
		HashSet<GraphObject> reqs = new HashSet<GraphObject>();

		if (this.getParentModel() instanceof Compartment)
		{
			reqs.add((GraphObject) this.getParentModel());
		}

		return reqs;
	}

	public List<String[]> getInspectable()
	{
		List<String[]> list = new ArrayList<String[]>();

		return list;
	}

	public Color getStringSpecificColor(String name)
	{
		int id = Math.abs(name.hashCode());

		int number1 = Math.abs((name + name).hashCode() * 97) % 1000;
		int number2 = Math.abs((name + name).hashCode() * 59) % 1000;

		int t = 350;
		int c[] = new int[3];
		c[0] = number1 % Math.min(255, t);
		c[1] = number2 % Math.min(255, t - c[0]);
		c[2] = Math.min(254, t - c[0] - c[1]);

		int p = 255;

		int r = c[0] + (id % (p - c[0]));
		int g = c[1] + (((id % p) * 97) % (p - c[1]));
		int b = c[2] + (((id % p) * 59) % (p - c[2]));

		return new Color(null, r, g, b);
	}

	//----------------------------------------------------------------------------------------------
	// Section: Inspector related
	//----------------------------------------------------------------------------------------------

	/**
	 * Writes the set of open controlled vocabulary in one line of a string.
	 * @param set
	 * @return
	 */
	public static String formatInString(Set<openControlledVocabulary> set)
	{
		String s = "";

		for (openControlledVocabulary voc : set)
		{
			if (s.length() > 0) s += ", ";
			s += voc;
		}
		return s;
	}

	/**
	 * Converts the class name to a prontable string in the inspector.
	 * @param clsName
	 * @return
	 */
	public static String classNameToString(String clsName)
	{
		clsName = clsName.substring(clsName.lastIndexOf(".")+1, clsName.lastIndexOf("I"));
		clsName = clsName.substring(0, 1).toUpperCase() + clsName.substring(1);

		for (int i = 3; i < clsName.length(); i++)
		{
			if (Character.isLowerCase(clsName.charAt(i - 1)) &&
				Character.isUpperCase(clsName.charAt(i)))
			{
				clsName = clsName.substring(0, i) + " " + clsName.substring(i);
			}
		}

		return clsName;
	}

	/**
	 * Parses the type, name and synomyms of the given entity into the properties list, which can be
	 * displayed in the inspector.
	 * @param list
	 * @param ent
	 */
	public static void addNamesAndTypeAndID(List<String[]> list, entity ent)
	{
		String type = BioPAXNode.classNameToString(ent.getClass().getName());
		list.add(new String[]{"Type", type});

		String s = ent.getNAME();
		if (s != null && s.length() > 0)
		{
			list.add(new String[]{"Name", s});
		}

		s = ent.getSHORT_NAME();

		if (s != null && s.length() > 0)
		{
			list.add(new String[]{"Short Name", s});
		}

		for (String synon : ent.getSYNONYMS())
		{
			list.add(new String[]{"Synonym", synon});
		}

		list.add(new String[]{"ID", ent.getRDFId()});
	}

	/**
	 * Parses the data source, xref, commecnt and organism of the given entity into the properties
	 * list, which can be displayed in the inspector.
	 * @param list
	 * @param ent
	 */
	public static void addDataSourceAndXrefAndComments(List<String[]> list, entity ent)
	{
		for (dataSource ds : ent.getDATA_SOURCE())
		{
			if (!ds.getNAME().isEmpty())
			{
				list.add(new String[]{"Data Source", ds.toString()});
			}
		}

		for (xref xr : ent.getXREF())
		{
			list.add(new String[]{"Reference", xr.toString()});
		}

		if (ent instanceof sequenceEntity)
		{
			bioSource src = ((sequenceEntity) ent).getORGANISM();
			if (src != null)
			{
				list.add(new String[]{"Organism", src.toString()});
			}
		}

		for (String comment : ent.getCOMMENT())
		{
			if (!comment.contains("@Layout"))
			{
				String c;

				boolean first = true;

				while (comment.length() > PROPERTY_CHAR_LIMIT)
				{
					int cutIndex = comment.lastIndexOf(" ", PROPERTY_CHAR_LIMIT);
					if (cutIndex < 0) cutIndex = PROPERTY_CHAR_LIMIT;

					c = comment.substring(0, cutIndex);
					comment = comment.substring(cutIndex).trim();

					String prop = first ? "Comment" : "";
					list.add(new String[]{prop, c});
					first = false;
				}

				if (comment.trim().length() > 0)
				{
					String prop = first ? "Comment" : "";
					list.add(new String[]{prop, comment.trim()});
				}
			}
		}
	}


	public static final String NAME_REF = "Name";
	public static final int PROPERTY_CHAR_LIMIT = 50;
	public static final int MAX_INITIAL_WIDTH = 100;
}
