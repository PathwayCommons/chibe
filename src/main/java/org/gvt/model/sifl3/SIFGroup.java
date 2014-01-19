package org.gvt.model.sifl3;

import org.biopax.paxtools.model.level3.*;
import org.eclipse.swt.graphics.Color;
import org.gvt.command.AddCommand;
import org.gvt.command.DeleteConnectionCommand;
import org.gvt.command.OrphanChildCommand;
import org.gvt.model.CompoundModel;
import org.gvt.model.EntityAssociated;
import org.gvt.model.NodeModel;
import org.gvt.model.biopaxl3.BioPAXCompoundNode;
import org.gvt.util.EntityHolder;
import org.patika.mada.graph.Edge;
import org.patika.mada.graph.GraphObject;

import java.util.*;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class SIFGroup extends BioPAXCompoundNode implements EntityAssociated
{
	private Set<String> mediators;

	private Map<org.gvt.model.GraphObject, Set<SIFEdge>> substitutionMap;

	public SIFGroup(CompoundModel root)
	{
		super(root);
		setLabelHeight(0);

		setText("");
	}

	public Collection<? extends Level3Element> getRelatedModelElements()
	{
		return Collections.emptySet();
	}

	public SIFGroup(CompoundModel root, Collection<SIFNode> members)
	{
		this(root);

		// Add members as child node

		for (SIFNode member : members)
		{
			OrphanChildCommand occ = new OrphanChildCommand();
			occ.setChild(member);
			occ.setParent(member.getParentModel());
			occ.execute();

			AddCommand add = new AddCommand();
			add.setParent(this);
			add.setChild(member);
			add.execute();
		}

		// If the members all have the same edge to the same object, merge them
		mergeEdges(members, true);
		mergeEdges(members, false);
		mergeEdges(members);

		this.mediators = new HashSet<String>();
		deleteInnerClique(members);

		setBorderColor(BORDER_COLOR);
		setColor(BG_COLOR);
		setTextColor(TEXT_COLOR);
	}

	protected void mergeEdges(Collection<SIFNode> members, boolean incoming)
	{
		Map<String, Set<SIFEdge>> map = new HashMap<String, Set<SIFEdge>>();

		for (SIFNode member : members)
		{
			for (Edge e : incoming ? member.getUpstream() : member.getDownstream())
			{
				SIFEdge edge = (SIFEdge) e;

				if (!edge.isDirected()) continue;

				NodeModel neigh = incoming ? edge.getSource() : edge.getTarget();

				String key = getKey(edge, neigh);

				if (!map.containsKey(key)) map.put(key, new HashSet<SIFEdge>());
				map.get(key).add(edge);
			}
		}

		for (String key : map.keySet())
		{
			if (map.get(key).size() == members.size())
			{
				createMergedEdge(map.get(key), incoming);
			}
		}
	}

	protected void mergeEdges(Collection<SIFNode> members)
	{
		Map<String, Set<SIFEdge>> map = new HashMap<String, Set<SIFEdge>>();

		for (SIFNode member : members)
		{
			for (Edge e : member.getUpstream())
			{
				SIFEdge edge = (SIFEdge) e;

				if (edge.isDirected()) continue;

				NodeModel neigh = edge.getSource();

				String key = getKey(edge, neigh);

				if (!map.containsKey(key)) map.put(key, new HashSet<SIFEdge>());
				map.get(key).add(edge);
			}
			for (Edge e : member.getDownstream())
			{
				SIFEdge edge = (SIFEdge) e;

				if (edge.isDirected()) continue;

				NodeModel neigh = edge.getTarget();

				String key = getKey(edge, neigh);

				if (!map.containsKey(key)) map.put(key, new HashSet<SIFEdge>());
				map.get(key).add(edge);
			}
		}

		for (String key : map.keySet())
		{
			if (map.get(key).size() == members.size())
			{
				createMergedEdge(map.get(key), key.split(" ")[1]);
			}
		}
	}

	private String getKey(SIFEdge edge, NodeModel node)
	{
		return edge.getTag() + " " + getNodeName(node);
	}

	private String getNodeName(NodeModel node)
	{
		if (node instanceof SIFGroup) return ((SIFGroup) node).getMemberNames();
		return node.getText();
	}

	private void createMergedEdge(Collection<SIFEdge> edges, boolean incoming)
	{
		SIFEdge sample = edges.iterator().next();
		SIFEdge merged = new SIFEdge(incoming ? sample.getSource() : this,
			incoming ? this : sample.getTarget(), sample.getTag(), null);

		for (SIFEdge edge : edges)
		{
			merged.addSubstitution(edge, incoming ? edge.getTarget() : edge.getSource());

			DeleteConnectionCommand command = new DeleteConnectionCommand();
			command.setConnectionModel(edge);
			command.execute();
		}
	}

	private void createMergedEdge(Collection<SIFEdge> edges, String commonNodeName)
	{
		SIFEdge sample = edges.iterator().next();
		boolean incoming = getNodeName(sample.getSource()).equals(commonNodeName);

		if (!incoming) assert getNodeName(sample.getTarget()).equals(commonNodeName);

		SIFEdge merged = new SIFEdge(
			incoming ? sample.getSource() : this,
			incoming ? this : sample.getTarget(),
			sample.getTag(), null);

		for (SIFEdge edge : edges)
		{
			merged.addSubstitution(edge, incoming ? edge.getTarget() : edge.getSource());
			DeleteConnectionCommand command = new DeleteConnectionCommand();
			command.setConnectionModel(edge);
			command.execute();
		}
	}


	protected void deleteInnerClique(Collection<? extends NodeModel> members)
	{
		Map<NodeModel, Map<String, Set<SIFEdge>>> inMap =
			new HashMap<NodeModel, Map<String, Set<SIFEdge>>>();

		Map<NodeModel, Map<String, Set<SIFEdge>>> outMap =
			new HashMap<NodeModel, Map<String, Set<SIFEdge>>>();

		for (NodeModel member : members)
		{
			if (!inMap.containsKey(member)) inMap.put(member, new HashMap<String, Set<SIFEdge>>());
			if (!outMap.containsKey(member)) outMap.put(member, new HashMap<String, Set<SIFEdge>>());

			for (Object o : member.getTargetConnections())
			{
				SIFEdge edge = (SIFEdge) o;
				putInMap(inMap, member, edge, members);
				if (!edge.isDirected()) putInMap(outMap, member, edge, members);

			}
			for (Object o : member.getSourceConnections())
			{
				SIFEdge edge = (SIFEdge) o;
				putInMap(outMap, member, edge, members);
				if (!edge.isDirected()) putInMap(inMap, member, edge, members);
			}
		}

		Set<Set<SIFEdge>> edgeSets = new HashSet<Set<SIFEdge>>();

		collectEdgeSets(members, inMap, edgeSets);
		collectEdgeSets(members, outMap, edgeSets);

		for (Set<SIFEdge> edges : edgeSets)
		{
			deleteEdges(edges);
		}
	}

	private void collectEdgeSets(Collection<? extends NodeModel> members, Map<NodeModel,
		Map<String, Set<SIFEdge>>> map, Set<Set<SIFEdge>> edgeSets)
	{
		for (String type : map.values().iterator().next().keySet())
		{
			Set<SIFEdge> collectedEdges = new HashSet<SIFEdge>();
			boolean isAClique = true;

			for (NodeModel member : map.keySet())
			{
				if (!map.get(member).containsKey(type)) break;
				Set<SIFEdge> edges = map.get(member).get(type);
				if (edges.size() == members.size() - 1 && !contains(edgeSets, edges))
				{
					collectedEdges.addAll(edges);
				}
				else
				{
					isAClique = false;
					break;
				}
			}

			if (isAClique) edgeSets.add(collectedEdges);
		}
	}

	private boolean contains(Set<Set<SIFEdge>> edgeSets, Set<SIFEdge> edges)
	{
		for (Set<SIFEdge> set : edgeSets)
		{
			if (set.size() == edges.size() && set.containsAll(edges)) return true;
		}
		return false;
	}

	private void putInMap(Map<NodeModel, Map<String, Set<SIFEdge>>> map, NodeModel member,
		SIFEdge edge, Collection<? extends NodeModel> members)
	{
		if (!members.contains(edge.getSource()) || !members.contains(edge.getTarget())) return;

		if (!map.get(member).containsKey(edge.getTag()))
			map.get(member).put(edge.getTag(), new HashSet<SIFEdge>());
		map.get(member).get(edge.getTag()).add(edge);
	}

	private void deleteEdges(Set<SIFEdge> edges)
	{
		String type = edges.iterator().next().getTag();

		for (SIFEdge edge : edges)
		{
			this.mediators.addAll(edge.getMediators());

			DeleteConnectionCommand command = new DeleteConnectionCommand();
			command.setConnectionModel(edge);
			command.execute();
		}

		String text = this.getText().isEmpty() ? "" : ", ";
		text += type;
		this.setText(this.getText() + text);
		this.setLabelHeight(CompoundModel.LABEL_HEIGHT);
	}

	public String getMemberNames()
	{
		List<String> names = new ArrayList<String>();
		for (Object o : children)
		{
			if (o instanceof NodeModel) names.add(((NodeModel) o).getText());
		}
		Collections.sort(names);
		String s = "";
		for (String name : names)
		{
			s += name;
		}
		return s;
	}

	public Set<String> getMediators()
	{
		return mediators;
	}

	public Set<String> getMediators(Set<org.gvt.model.GraphObject> nodes)
	{
		if (nodes.isEmpty()) return mediators;
		if (substitutionMap == null) return mediators;

		Set<SIFEdge> edges = new HashSet<SIFEdge>();
		for (org.gvt.model.GraphObject node : nodes)
		{
			if (substitutionMap.containsKey(node)) edges.addAll(substitutionMap.get(node));
		}

		Set<String> meds = new HashSet<String>();
		for (SIFEdge edge : edges)
		{
			meds.addAll(edge.getMediators());
		}
		return meds;
	}

	public void addSubstitution(SIFEdge edge)
	{
		mediators.addAll(edge.getMediators());

		if (substitutionMap == null) substitutionMap =
			new HashMap<org.gvt.model.GraphObject, Set<SIFEdge>>();

		if (!substitutionMap.containsKey(edge.getSource()))
			substitutionMap.put(edge.getSource(), new HashSet<SIFEdge>());
		if (!substitutionMap.containsKey(edge.getTarget()))
			substitutionMap.put(edge.getTarget(), new HashSet<SIFEdge>());

		// The edge won't remember its source and target after being removed, so we need to record
		// them here. That's why only recording the edge is not enough.
		substitutionMap.get(edge.getSource()).add(edge);
		substitutionMap.get(edge.getTarget()).add(edge);
	}


	/**
	 * Constructor for excising.
	 * @param toexcise
	 * @param root
	 */
	public SIFGroup(SIFGroup toexcise, CompoundModel root)
	{
		super(toexcise, root);
	}

	/**
	 * Complexes are breadth nodes.
	 * @return true
	 */
	public boolean isBreadthNode()
	{
		return true;
	}

	public Set<GraphObject> getRequisites()
	{
		Set<GraphObject> reqs = super.getRequisites();
		reqs.addAll(this.getChildren());
		return reqs;
	}

	public List<String[]> getInspectable()
	{
		return Collections.emptyList();
	}

	public String getIDHash()
	{
		String hash = "";

		for (Object o : children)
		{
			hash += ((SIFNode) o).getIDHash();
		}
		return hash;
	}

	@Override
	public EntityHolder getEntity()
	{
		return null;
	}

	private static final Color BG_COLOR = new Color(null, 255, 255, 255);
	private static final Color BORDER_COLOR = new Color(null, 150, 150, 150);
	private static final Color TEXT_COLOR = new Color(null, 0, 0, 0);

}
