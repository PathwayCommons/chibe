package org.gvt.model.basicsif;

import org.biopax.paxtools.pattern.miner.SIFEnum;
import org.biopax.paxtools.pattern.miner.SIFType;
import org.cbio.causality.analysis.Graph;
import org.cbio.causality.analysis.GraphList;
import org.cbio.causality.signednetwork.SignedType;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.gvt.model.NodeModel;
import org.gvt.model.biopaxl3.BioPAXL3Graph;
import org.gvt.util.Conf;
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

	public BasicSIFGraph(Graph graph)
	{
		this();

		Map<String, BasicSIFNode> nodeMap = new HashMap<String, BasicSIFNode>();

		if (graph instanceof GraphList)
		{
			for (Graph g : ((GraphList) graph).getGraphs())
			{
				loadFromGraph(g, nodeMap);
			}
		}
		else
		{
			loadFromGraph(graph, nodeMap);
		}
	}

	private void loadFromGraph(Graph graph, Map<String, BasicSIFNode> nodeMap)
	{
		boolean directed = graph.isDirected();
		Set<String> memory = new HashSet<String>();

		for (String gene : graph.getSymbols())
		{
			if (!nodeMap.containsKey(gene))
			{
				nodeMap.put(gene, new BasicSIFNode(this, gene, gene));
			}
			for (String neigh : directed ? graph.getDownstream(gene) : graph.getNeighbors(gene))
			{
				if (!nodeMap.containsKey(neigh))
				{
					nodeMap.put(neigh, new BasicSIFNode(this, neigh, neigh));
				}

				String key = gene + "\t" + neigh;
				String rev = neigh + "\t" + gene;

				if (memory.contains(key) || (!directed && memory.contains(rev))) continue;

				new BasicSIFEdge(nodeMap.get(gene), nodeMap.get(neigh), graph.getEdgeType(),
					graph.getMediatorsInString(gene, neigh));

				memory.add(key);
			}
		}
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

							SIFType sifType = SIFEnum.typeOf(type);
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

		if (Conf.getBoolean(Conf.USE_SIF_GROUPING)) graph.groupSimilarNodes();

		return graph;
	}

	//--- Section: Grouping -----------------------------------------------------------------------|

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

		for (Set<NodeModel> group : findGroups((Set<NodeModel>) getNodes(), incomingMap, outgoingMap))
		{
			HashSet<BasicSIFNode> members = new HashSet<BasicSIFNode>();
			for (NodeModel m : group)
			{
				members.add((BasicSIFNode) m);
			}
			new BasicSIFGroup(this, members);
		}

		for (Object o : getEdges())
		{
			BasicSIFEdge edge = (BasicSIFEdge) o;
			if (edge.substitutionMap != null && !edge.substitutionMap.isEmpty())
			{
				BasicSIFEdge hidden = edge.substitutionMap.values().iterator().next();

				while (hidden.substitutionMap != null && !hidden.substitutionMap.isEmpty())
				{
					hidden = hidden.substitutionMap.values().iterator().next();
				}

				edge.setColor(hidden.getColor());
				edge.setHighlight(hidden.isHighlight());
				edge.setWidth(hidden.getWidth());

				String tooltip = "";
				for (BasicSIFEdge leaf : edge.getLeaf())
				{
					if (leaf.getText() != null && !leaf.getText().isEmpty())
					{
						tooltip += leaf.getTooltipText() + "\n";
					}
				}
				if (!tooltip.isEmpty())
				{
					edge.setTooltipText(tooltip.trim());
					edge.setText("o");
				}
			}
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
//		if (incomingMap.get(node).isEmpty() && outgoingMap.get(node).isEmpty())
//			return Collections.emptySet();

		Set<NodeModel> sim = new HashSet<NodeModel>();

		for (NodeModel n : nodes)
		{
			if (similar(n, node, incomingMap, outgoingMap)) sim.add(n);
		}
		if (sim.size() > 1) return sim;
		else return Collections.<NodeModel>emptySet();
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

	//--- Section: Formatting ---------------------------------------------------------------------|

	public void format(List<String> lines)
	{
		Map<String, BasicSIFEdge> edgeMap = getEdgeMap();
		Map<String, BasicSIFNode> nodeMap = getNodeMap();

		for (BasicSIFNode node : nodeMap.values())
		{
			if (node.getShape().startsWith("RPPA")) node.setShape("RoundRect");
		}

		for (String line : lines)
		{
			String[] token = line.split("\t");

			if (token.length < 2) continue;

			if (token[0].equals("node"))
			{
				for (BasicSIFNode node : findNodes(token[1], nodeMap))
				{
					if (token[2].equals("color"))
					{
						node.setColor(stringToColor(token[3]));
					}
					else if (token[2].equals("bordercolor"))
					{
						node.setBorderColor(stringToColor(token[3]));
					}
					else if (token[2].equals("borderwidth"))
					{
						node.setBorderWidth(Integer.parseInt(token[3]));
					}
					else if (token[2].equals("highlight"))
					{
						node.setHighlight(token[3].equals("on"));
					}
					else if (token[2].equals("highlightcolor"))
					{
						node.setHighlightColor(stringToColor(token[3]));
					}
					else if (token[2].equals("textcolor"))
					{
						node.setTextColor(stringToColor(token[3]));
					}
					else if (token[2].equals("tooltip"))
					{
						node.setTooltipText((token[3]).replaceAll("\\\\n", "\n"));
					}
					else if (token[2].equals("shape"))
					{
						node.setShape(token[3]);
					}
					else if (token[2].equals("rppasite"))
					{
						if (!node.getShape().startsWith("RPPA"))
						{
							Dimension size = node.getSize();
							size.height = 32;
							node.setSize(size);
						}
						String s = !node.getShape().startsWith("RPPA") ? "RPPA" : node.getShape();
						node.setShape(s + ";" + token[3]);
					}
				}
			}
			else if (token[0].equals("edge"))
			{
				for (BasicSIFEdge edge : findEdges(token[1], edgeMap))
				{
					if (token[2].equals("color"))
					{
						edge.setColor(stringToColor(token[3]));
					}
					else if (token[2].equals("width"))
					{
						edge.setWidth(Integer.parseInt(token[3]));
					}
					else if (token[2].equals("highlight"))
					{
						edge.setHighlight(token[3].equals("on"));
					}
					else if (token[2].equals("highlightcolor"))
					{
						edge.setHighlightColor(stringToColor(token[3]));
					}
				}
			}
		}
	}

	public List<String> getCurrentFormat()
	{
		List<String> lines = new ArrayList<String>();

		Map<String, BasicSIFEdge> edgeMap = getEdgeMap();
		Map<String, BasicSIFNode> nodeMap = getNodeMap();

		for (BasicSIFNode node : nodeMap.values())
		{
			String pre = "node\t" + node.getText() + "\t";
			lines.add(pre + "color\t" + colorToString(node.getColor()));
			lines.add(pre + "bordercolor\t" + colorToString(node.getBorderColor()));
			lines.add(pre + "borderwidth\t" + node.getBorderWidth());
			lines.add(pre + "highlight\t" + node.isHighlight());
			if (node.getHighlightColor() != null)
				lines.add(pre + "highlightcolor\t" + colorToString(node.getHighlightColor()));
			lines.add(pre + "textcolor\t" + colorToString(node.getTextColor()));
			if (node.getTooltipText() != null) lines.add(pre + "tooltip\t" + node.getTooltipText());
		}
		Set<BasicSIFEdge> mem = new HashSet<BasicSIFEdge>();
		for (String key : edgeMap.keySet())
		{
			BasicSIFEdge edge = edgeMap.get(key);
			if (!mem.contains(edge))
			{
				mem.add(edge);
				String pre = "edge\t" + key + "\t";
				lines.add(pre + "color\t" + colorToString(edge.getColor()));
				lines.add(pre + "width\t" + edge.getWidth());
				lines.add(pre + "highlight\t" + edge.isHighlight());
				if (edge.getHighlightColor() != null)
					lines.add(pre + "highlightcolor\t" + colorToString(edge.getHighlightColor()));
			}
		}
		return lines;
	}

	private Map<String, BasicSIFEdge> getEdgeMap()
	{
		Map<String, BasicSIFEdge> map = new HashMap<String, BasicSIFEdge>();

		for (Object o : getEdges())
		{
			if (o instanceof BasicSIFEdge)
			{
				BasicSIFEdge edge = (BasicSIFEdge) o;

				for (String key : getSubstitutionKeys(edge, null))
				{
					map.put(key, edge);
				}
			}
		}
		return map;
	}

	private Set<String> getSubstitutionKeys(BasicSIFEdge edge, Set<String> keys)
	{
		 if (keys == null) keys = new HashSet<String>();

		if (edge.substitutionMap == null) keys.add(edge.getKey());
		else
		{
			for (BasicSIFEdge e : edge.substitutionMap.values())
			{
				getSubstitutionKeys(e, keys);
			}
		}
		return keys;
	}

	private Color stringToColor(String s)
	{
		String[] c = s.split(" ");
		try
		{
			return new Color(null,
				Integer.parseInt(c[0]), Integer.parseInt(c[1]), Integer.parseInt(c[2]));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return new Color(null, 0, 0, 0);
		}
	}

	private String colorToString(Color c)
	{
		return c.getRed() + " " + c.getGreen() + " " + c.getBlue();
	}

	private Map<String, BasicSIFNode> getNodeMap()
	{
		Map<String, BasicSIFNode> map = new HashMap<String, BasicSIFNode>();
		for (Object o : getNodes())
		{
			if (o instanceof BasicSIFNode)
			{
				BasicSIFNode node = (BasicSIFNode) o;
				map.put(node.getText(), node);
			}
		}
		return map;
	}

	private Collection<BasicSIFNode> findNodes(String s, Map<String, BasicSIFNode> nodeMap)
	{
		if (s.equals("all-nodes")) return nodeMap.values();

		Set<BasicSIFNode> set = new HashSet<BasicSIFNode>();
		for (String name : s.split(";"))
		{
			if (nodeMap.containsKey(name)) set.add(nodeMap.get(name));
		}
		return set;
	}

	private Collection<BasicSIFEdge> findEdges(String s, Map<String, BasicSIFEdge> edgeMap)
	{
		if (s.equals("all-edges")) return edgeMap.values();

		Set<BasicSIFEdge> set = new HashSet<BasicSIFEdge>();
		for (String name : s.split(";"))
		{
			if (edgeMap.containsKey(name)) set.add(edgeMap.get(name));
		}
		return set;
	}
}
