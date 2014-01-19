package org.gvt.model.sifl3;

import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.pattern.miner.SIFInteraction;
import org.biopax.paxtools.pattern.miner.SIFSearcher;
import org.biopax.paxtools.pattern.miner.SIFType;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.NodeModel;
import org.gvt.util.EntityHolder;
import org.gvt.util.PathwayHolder;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * SIF graphs are not excisable.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class SIFGraph extends BioPAXGraph
{
	private List<SIFType> ruleTypes;
	private Blacklist blacklist;

	public SIFGraph(Model biopaxModel, List<SIFType> ruleTypes, Blacklist blacklist)
	{
		setBiopaxModel(biopaxModel);
		setGraphType(SIF_LEVEL3);
		this.ruleTypes = ruleTypes;
		this.blacklist = blacklist;

		createContents();
	}

	private void createContents()
	{
		Set<SIFInteraction> sifInts = getSimpleInteractions();

		// Map to remember created nodes
		Map<EntityHolder, SIFNode> map = new HashMap<EntityHolder, SIFNode>();

		// Encountered rules. For avoiding duplicate edges.
		Set<String> encountered = new HashSet<String>();

		for (SIFInteraction simpleInt : sifInts)
		{
            if(simpleInt.type != null)
            {
                EntityHolder source = new EntityHolder(simpleInt.source.iterator().next());
                EntityHolder target = new EntityHolder(simpleInt.target.iterator().next());

                if (!map.containsKey(source))
                {
                    map.put(source, new SIFNode(this, source));
                }
                if (!map.containsKey(target))
                {
                    map.put(target, new SIFNode(this, target));
                }
                SIFNode sourceNode = map.get(source);
                SIFNode targetNode = map.get(target);

                String id = source.getID() + " - " + target.getID();

                if (encountered.contains(id))
                {
                    continue;
                }

                new SIFEdge(sourceNode, targetNode, simpleInt.type.getTag(),
					new HashSet<String>(Arrays.asList(simpleInt.getMediatorsInString().split(" "))));

                encountered.add(id);

                if (!simpleInt.type.isDirected())
                {
                    encountered.add(target.getID() + " - " + source.getID());
                }
            }
		}

		groupSimilarNodes();
	}

	private Set<SIFInteraction> getSimpleInteractions()
	{
		SIFSearcher searcher = new SIFSearcher(ruleTypes.toArray(new SIFType[ruleTypes.size()]));
		searcher.setBlacklist(blacklist);
		return searcher.searchSIF(biopaxModel);
	}

	/**
	 * Extracts rule types from possible rule classes.
	 * @return possible rule types
	 */
	public static List<SIFType> getPossibleRuleTypes()
	{
		return Arrays.asList(SIFType.values());
	}

	public void write(OutputStream os)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));

			for (Object o : getEdges())
			{
				SIFEdge edge = (SIFEdge) o;

				for (SIFNode source : getNonGroupNodes(edge.getSource()))
				{
					for (SIFNode target : getNonGroupNodes(edge.getTarget()))
					{
						writer.write(source.getText() + "\t");
						writer.write(edge.getTag() + "\t");
						writer.write(target.getText() + "\n");
					}
				}
			}

			for (Object o : getChildren())
			{
				if (o instanceof SIFGroup)
				{
					String text = ((SIFGroup) o).getText();

					if (text != null && !text.isEmpty())
					{
						for (String type : text.split(","))
						{
							type = type.trim();

							SIFType sifType = SIFType.typeOf(type);
							if (sifType != null)
							{
								SIFNode[] members = getNonGroupNodes((SIFGroup) o);

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

	private SIFNode[] getNonGroupNodes(NodeModel node)
	{
		if (node instanceof SIFGroup)
		{
			return (SIFNode[]) ((SIFGroup) node).getChildren().toArray(
				new SIFNode[((SIFGroup) node).getChildren().size()]);
		}
		else return new SIFNode[]{(SIFNode) node};
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
				SIFEdge edge = (SIFEdge) oo;
				assert edge.getTarget() == node;
				String key = edge.getTag() + " " + edge.getSource().getText(); //
				incomingMap.get(node).add(key);
				if (!edge.isDirected()) outgoingMap.get(node).add(key); //
			}
			for (Object oo : node.getSourceConnections())
			{
				SIFEdge edge = (SIFEdge) oo;
				assert edge.getSource() == node;
				String key = edge.getTag() + " " + edge.getTarget().getText(); //
				outgoingMap.get(node).add(key);
				if (!edge.isDirected()) incomingMap.get(node).add(key); //
			}
		}

		for (Set<NodeModel> group : findGroups(getNodes(), incomingMap, outgoingMap))
		{
			HashSet<SIFNode> members = new HashSet<SIFNode>();
			for (NodeModel m : group)
			{
				members.add((SIFNode) m);
			}
			new SIFGroup(this, members);
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
	
	
	// Had to implement these methods to make SIF graph a BioPAX graph

	public String getPathwayRDFID()
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public List<String[]> getInspectable()
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public BioPAXGraph excise(Collection<GraphObject> objects, boolean keepHighlights)
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	protected void prepareEntityToNodeMap()
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void representDataOnActors(String type)
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void removeRepresentations()
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public boolean fetchLayout()
	{
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public boolean fetchLayout(String pathwayRDFID)
	{
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public void recordLayout()
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void forgetLayout()
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public Set<Node> getRelatedStates(EntityHolder pe)
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public Set<Node> getRelatedStates(Collection<EntityHolder> entities)
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public void replaceComplexMembersWithComplexes(Collection<Node> objects)
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public BioPAXGraph excise(PathwayHolder p)
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public PathwayHolder getPathway()
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public void setPathway(PathwayHolder p)
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void registerContentsToPathway()
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
