package org.gvt.model.basicsif;

import org.biopax.paxtools.pattern.miner.SIFType;
import org.gvt.model.NodeModel;
import org.gvt.model.biopaxl3.BioPAXL3Graph;
import org.patika.mada.graph.GraphObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class BasicSIFGraph extends BioPAXL3Graph
{
	public BasicSIFGraph()
	{
		super(null);
		this.setGraphType(BASIC_SIF);
	}


	public void write(OutputStream os)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));

			for (Object o : getEdges())
			{
				BasicSIFEdge edge = (BasicSIFEdge) o;
				for (BasicSIFNode source : getNonGroupNodes(edge.getSource()))
				{
					for (BasicSIFNode target : getNonGroupNodes(edge.getTarget()))
					{
						writer.write(source.getText() + "\t");
						writer.write(edge.getType().getTag() + "\t");
						writer.write(target.getText() + "\n");
					}
				}
			}

			for (Object o : getChildren())
			{
				if (o instanceof BasicSIFGroup)
				{
					String text = ((BasicSIFGroup) o).getText();

					if (text != null && !text.isEmpty())
					{
						for (String type : text.split(","))
						{
							type = type.trim();

							SIFType sifType = SIFType.typeOf(type);
							if (sifType != null)
							{
								BasicSIFNode[] members = getNonGroupNodes((BasicSIFGroup) o);

								for (int i = 0; i < members.length; i++)
								{
									for (int j = 0; j < members.length; j++)
									{
										if (i == j || (!sifType.isDirected() &&
											members[i].getText().compareTo(members[i].getText()) >= 0))
											continue;

										writer.write(members[i].getText() + "\t");
										writer.write(type + "\t");
										writer.write(members[j].getText() + "\n");
									}
								}
							}
						}
					}
				}
			}

			writer.close();
		}
		catch (IOException e){e.printStackTrace();}
	}

	private BasicSIFNode[] getNonGroupNodes(NodeModel node)
	{
		if (node instanceof BasicSIFGroup)
		{
			return (BasicSIFNode[]) ((BasicSIFGroup) node).getChildren().toArray(
				new BasicSIFNode[((BasicSIFGroup) node).getChildren().size()]);
		}
		else return new BasicSIFNode[]{(BasicSIFNode) node};
	}

	public BioPAXL3Graph excise(Collection<GraphObject> objects, boolean keepHighlights)
	{
		BasicSIFGraph graph = new BasicSIFGraph();

		Map<NodeModel, NodeModel> map = new HashMap<NodeModel, NodeModel>();

		for (GraphObject object : objects)
		{
			if (object instanceof BasicSIFNode)
			{
				BasicSIFNode orig = (BasicSIFNode) object;
				BasicSIFNode ex = new BasicSIFNode(orig, graph);
				map.put(orig, ex);
			}
		}
		
		for (GraphObject object : objects)
		{
			if (object instanceof BasicSIFEdge)
			{
				BasicSIFEdge orig = (BasicSIFEdge) object;
				new BasicSIFEdge(orig, map);
			}
		}

		graph.groupSimilarNodes();
		return graph;
	}


	public void groupSimilarNodes()
	{
		Map<NodeModel, Set<String>> incomingMap = new HashMap<NodeModel, Set<String>>();
		Map<NodeModel, Set<String>> outgoingMap = new HashMap<NodeModel, Set<String>>();

		for (Object o : getNodes())
		{
			NodeModel node = (NodeModel) o;

			if (!incomingMap.containsKey(node)) incomingMap.put(node, new HashSet<String>());
			if (!outgoingMap.containsKey(node)) outgoingMap.put(node, new HashSet<String>());

			for (Object oo : node.getTargetConnections())
			{
				BasicSIFEdge edge = (BasicSIFEdge) oo;
				assert edge.getTarget() == node;
				String key = edge.getType().getTag() + " " + edge.getSource().getText();
				incomingMap.get(node).add(key);
				if (!edge.isDirected()) outgoingMap.get(node).add(key);
			}
			for (Object oo : node.getSourceConnections())
			{
				BasicSIFEdge edge = (BasicSIFEdge) oo;
				assert edge.getSource() == node;
				String key = edge.getType().getTag() + " " + edge.getTarget().getText();
				outgoingMap.get(node).add(key);
				if (!edge.isDirected()) incomingMap.get(node).add(key);
			}
		}

		for (Set<NodeModel> group : findGroups(getNodes(), incomingMap, outgoingMap))
		{
			HashSet<BasicSIFNode> members = new HashSet<BasicSIFNode>();
			for (NodeModel m : group)
			{
				members.add((BasicSIFNode) m);
			}
			new BasicSIFGroup(this, members);
		}
	}

	private Set<Set<NodeModel>> findGroups(Collection<NodeModel> nodes,
		Map<NodeModel, Set<String>> incomingMap, Map<NodeModel, Set<String>> outgoingMap)
	{
		Set<Set<NodeModel>> groups = new HashSet<Set<NodeModel>>();

		for (NodeModel node : nodes)
		{
			Set<NodeModel> group = getSimilarNodes(node, nodes, incomingMap, outgoingMap);
			if (!group.isEmpty() && !contains(groups, group)) groups.add(group);
		}
		return groups;
	}

	private boolean contains(Set<Set<NodeModel>> groups, Set<NodeModel> group)
	{
		for (Set<NodeModel> g : groups)
		{
			if (g.size() == group.size() && g.containsAll(group)) return true;
		}
		return false;
	}

	private Set<NodeModel> getSimilarNodes(NodeModel node, Collection<NodeModel> nodes,
		Map<NodeModel, Set<String>> incomingMap, Map<NodeModel, Set<String>> outgoingMap)
	{
		if (incomingMap.get(node).isEmpty() && outgoingMap.get(node).isEmpty())
			return Collections.emptySet();

		Set<NodeModel> sim = new HashSet<NodeModel>();

		for (NodeModel n : nodes)
		{
			if (similar(n, node, incomingMap, outgoingMap)) sim.add(n);
		}
		if (sim.size() > 1) return sim;
		else return Collections.emptySet();
	}

	private boolean similar(NodeModel n1, NodeModel n2, Map<NodeModel, Set<String>> incomingMap,
		Map<NodeModel, Set<String>> outgoingMap)
	{
		if (incomingMap.get(n1).size() != incomingMap.get(n2).size() ||
			outgoingMap.get(n1).size() != outgoingMap.get(n2).size())
		{
			return false;
		}
		if (incomingMap.get(n1).containsAll(incomingMap.get(n2)) &&
			outgoingMap.get(n1).containsAll(outgoingMap.get(n2)))
		{
			return true;
		}

		Set<String> n1_in = new HashSet<String>(incomingMap.get(n1));
		Set<String> n2_in = new HashSet<String>(incomingMap.get(n2));
		Set<String> n1_out = new HashSet<String>(outgoingMap.get(n1));
		Set<String> n2_out = new HashSet<String>(outgoingMap.get(n2));

		removeCommon(n1_in, n2_in);
		removeCommon(n1_out, n2_out);

		return containssOnlyInterEdges(n1.getText(), n2.getText(), getParsed(n1_in), getParsed(n2_in)) &&
			containssOnlyInterEdges(n1.getText(), n2.getText(), getParsed(n1_out), getParsed(n2_out));
	}

	private void removeCommon(Set<String> set1, Set<String> set2)
	{
		Set<String> temp = new HashSet<String>(set1);
		set1.removeAll(set2);
		set2.removeAll(temp);
	}

	private boolean containssOnlyInterEdges(String name1, String name2,
		Map<String, Set<String>> edges1, Map<String, Set<String>> edges2)
	{
		for (String type : edges1.keySet())
		{
			if (!edges2.containsKey(type)) return false;

			assert !edges1.get(type).isEmpty();

			if (edges1.get(type).size() != 1 || !edges1.get(type).iterator().next().equals(name2))
				return false;
		}
		for (String type : edges2.keySet())
		{
			if (!edges1.containsKey(type)) return false;

			assert !edges2.get(type).isEmpty();

			if (edges2.get(type).size() != 1 || !edges2.get(type).iterator().next().equals(name1))
				return false;
		}
		return true;
	}

	private Map<String, Set<String>> getParsed(Set<String> edges)
	{
		Map<String, Set<String>> parsed = new HashMap<String, Set<String>>();

		for (String edge : edges)
		{
			String[] tok = edge.split(" ");
			if (!parsed.containsKey(tok[0])) parsed.put(tok[0], new HashSet<String>());
			parsed.get(tok[0]).add(tok[1]);
		}
		return parsed;
	}
}