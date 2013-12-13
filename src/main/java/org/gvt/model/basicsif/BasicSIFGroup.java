package org.gvt.model.basicsif;

import org.biopax.paxtools.model.level3.Level3Element;
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
public class BasicSIFGroup extends BioPAXCompoundNode implements EntityAssociated
{
	private Set<String> mediators;

	/**
	 * Constructor for excising.
	 * @param toexcise
	 * @param root
	 */
	public BasicSIFGroup(BasicSIFGroup toexcise, CompoundModel root)
	{
		super(toexcise, root);
	}

	public BasicSIFGroup(CompoundModel root)
	{
		super(root);
		setLabelHeight(0);

		setText("");
	}

	public Collection<? extends Level3Element> getRelatedModelElements()
	{
		return Collections.emptySet();
	}

	public BasicSIFGroup(CompoundModel root, Collection<BasicSIFNode> members)
	{
		this(root);

		// Add members as child node

		for (BasicSIFNode member : members)
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

	protected void mergeEdges(Collection<BasicSIFNode> members, boolean incoming)
	{
		Map<String, Set<BasicSIFEdge>> map = new HashMap<String, Set<BasicSIFEdge>>();

		for (BasicSIFNode member : members)
		{
			for (Edge e : incoming ? member.getUpstream() : member.getDownstream())
			{
				BasicSIFEdge edge = (BasicSIFEdge) e;

				if (!edge.isDirected()) continue;

				NodeModel neigh = incoming ? edge.getSource() : edge.getTarget();

				String key = getKey(edge, neigh);

				if (!map.containsKey(key)) map.put(key, new HashSet<BasicSIFEdge>());
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

	protected void mergeEdges(Collection<BasicSIFNode> members)
	{
		Map<String, Set<BasicSIFEdge>> map = new HashMap<String, Set<BasicSIFEdge>>();

		for (BasicSIFNode member : members)
		{
			for (Edge e : member.getUpstream())
			{
				BasicSIFEdge edge = (BasicSIFEdge) e;

				if (edge.isDirected()) continue;

				NodeModel neigh = edge.getSource();

				String key = getKey(edge, neigh);

				if (!map.containsKey(key)) map.put(key, new HashSet<BasicSIFEdge>());
				map.get(key).add(edge);
			}
			for (Edge e : member.getDownstream())
			{
				BasicSIFEdge edge = (BasicSIFEdge) e;

				if (edge.isDirected()) continue;

				NodeModel neigh = edge.getTarget();

				String key = getKey(edge, neigh);

				if (!map.containsKey(key)) map.put(key, new HashSet<BasicSIFEdge>());
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

	private String getKey(BasicSIFEdge edge, NodeModel node)
	{
		return edge.getType().getTag() + " " + getNodeName(node);
	}

	private String getNodeName(NodeModel node)
	{
		if (node instanceof BasicSIFGroup) return ((BasicSIFGroup) node).getMemberNames();
		return node.getText();
	}


	private void createMergedEdge(Collection<BasicSIFEdge> edges, boolean incoming)
	{
		BasicSIFEdge sample = edges.iterator().next();
		BasicSIFEdge merged = new BasicSIFEdge(incoming ? sample.getSource() : this,
			incoming ? this : sample.getTarget(), sample.getType().getTag(), null);

		for (BasicSIFEdge edge : edges)
		{
			merged.addMediators(edge.getMediators());

			DeleteConnectionCommand command = new DeleteConnectionCommand();
			command.setConnectionModel(edge);
			command.execute();
		}
	}

	private void createMergedEdge(Collection<BasicSIFEdge> edges, String commonNodeName)
	{
		BasicSIFEdge sample = edges.iterator().next();
		BasicSIFEdge merged = new BasicSIFEdge(
			getNodeName(sample.getSource()).equals(commonNodeName) ? sample.getSource() : this,
			getNodeName(sample.getTarget()).equals(commonNodeName) ? sample.getTarget() : this,
			sample.getType().getTag(), null);

		for (BasicSIFEdge edge : edges)
		{
			merged.addMediators(edge.getMediators());

			DeleteConnectionCommand command = new DeleteConnectionCommand();
			command.setConnectionModel(edge);
			command.execute();
		}
	}

	protected void deleteInnerClique(Collection<? extends NodeModel> members)
	{
		Map<NodeModel, Map<String, Set<BasicSIFEdge>>> inMap =
			new HashMap<NodeModel, Map<String, Set<BasicSIFEdge>>>();

		Map<NodeModel, Map<String, Set<BasicSIFEdge>>> outMap =
			new HashMap<NodeModel, Map<String, Set<BasicSIFEdge>>>();

		for (NodeModel member : members)
		{
			if (!inMap.containsKey(member)) inMap.put(member, new HashMap<String, Set<BasicSIFEdge>>());
			if (!outMap.containsKey(member)) outMap.put(member, new HashMap<String, Set<BasicSIFEdge>>());

			for (Object o : member.getTargetConnections())
			{
				BasicSIFEdge edge = (BasicSIFEdge) o;
				putInMap(inMap, member, edge, members);
				if (!edge.isDirected()) putInMap(outMap, member, edge, members);

			}
			for (Object o : member.getSourceConnections())
			{
				BasicSIFEdge edge = (BasicSIFEdge) o;
				putInMap(outMap, member, edge, members);
				if (!edge.isDirected()) putInMap(inMap, member, edge, members);
			}
		}

		Set<Set<BasicSIFEdge>> edgeSets = new HashSet<Set<BasicSIFEdge>>();

		collectEdgeSets(members, inMap, edgeSets);
		collectEdgeSets(members, outMap, edgeSets);

		for (Set<BasicSIFEdge> edges : edgeSets)
		{
			deleteEdges(edges);
		}
	}

	private void collectEdgeSets(Collection<? extends NodeModel> members, Map<NodeModel,
		Map<String, Set<BasicSIFEdge>>> map, Set<Set<BasicSIFEdge>> edgeSets)
	{
		for (String type : map.values().iterator().next().keySet())
		{
			Set<BasicSIFEdge> collectedEdges = new HashSet<BasicSIFEdge>();
			boolean isAClique = true;

			for (NodeModel member : map.keySet())
			{
				if (!map.get(member).containsKey(type)) break;
				Set<BasicSIFEdge> edges = map.get(member).get(type);
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

	private boolean contains(Set<Set<BasicSIFEdge>> edgeSets, Set<BasicSIFEdge> edges)
	{
		for (Set<BasicSIFEdge> edgeSet : edgeSets)
		{
			if (edgeSet.size() == edges.size() && edgeSet.containsAll(edges)) return true;
		}
		return false;
	}

	private void putInMap(Map<NodeModel, Map<String, Set<BasicSIFEdge>>> map, NodeModel member,
		BasicSIFEdge edge, Collection<? extends NodeModel> members)
	{
		if (!members.contains(edge.getSource()) || !members.contains(edge.getTarget())) return;

		if (!map.get(member).containsKey(edge.getType().getTag()))
			map.get(member).put(edge.getType().getTag(), new HashSet<BasicSIFEdge>());
		map.get(member).get(edge.getType().getTag()).add(edge);
	}

	private void deleteEdges(Set<BasicSIFEdge> edges)
	{
		String type = edges.iterator().next().getType().getTag();

		for (BasicSIFEdge edge : edges)
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
			hash += ((BasicSIFNode) o).getIDHash();
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
