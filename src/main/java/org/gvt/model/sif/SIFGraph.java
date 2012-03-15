package org.gvt.model.sif;

import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.io.sif.InteractionRule;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.io.sif.SimpleInteractionConverter;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.gvt.model.BioPAXGraph;
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
	private List<BinaryInteractionType> ruleTypes;

	public SIFGraph(Model biopaxModel, List<BinaryInteractionType> ruleTypes)
	{
		setBiopaxModel(biopaxModel);
		setGraphType(SIF);
		this.ruleTypes = ruleTypes;

		createContents();
	}

	private void createContents()
	{
		Set<SimpleInteraction> simpleInts = getSimpleInteractions(biopaxModel.getLevel());

		// Map to remember created nodes
		Map<EntityHolder, SIFNode> map = new HashMap<EntityHolder, SIFNode>();

		// Encountered rules. For avoiding duplicate edges.
		Set<String> encountered = new HashSet<String>();

		for (SimpleInteraction simpleInt : simpleInts)
		{
			EntityHolder source = new EntityHolder(simpleInt.getSource());
			EntityHolder target = new EntityHolder(simpleInt.getTarget());

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

			new SIFEdge(sourceNode, targetNode, simpleInt.getType());

			encountered.add(id);

			if (!simpleInt.getType().isDirected())
			{
				encountered.add(target.getID() + " - " + source.getID());
			}
		}
	}

	private Set<SimpleInteraction> getSimpleInteractions(BioPAXLevel level)
	{
		Map<BinaryInteractionType, InteractionRule> ruleMap =
			new HashMap<BinaryInteractionType, InteractionRule>();

		for (InteractionRule rule : SimpleInteractionConverter.getRules(level))
		{
			for (BinaryInteractionType ruleType : rule.getRuleTypes())
			{
				if (ruleTypes.contains(ruleType))
				{
					ruleMap.put(ruleType, rule);
				}
			}
		}

		Set<InteractionRule> ruleSet = new HashSet<InteractionRule>();

		for (BinaryInteractionType ruleType : ruleTypes)
		{
			ruleSet.add(ruleMap.get(ruleType));
		}

		InteractionRule[] rules = ruleSet.toArray(new InteractionRule[ruleSet.size()]);

		// Prepare options map

		Map<BinaryInteractionType, Boolean> options = new HashMap<BinaryInteractionType, Boolean>();

		for (BinaryInteractionType ruleType : getPossibleRuleTypes(biopaxModel.getLevel()))
		{
			options.put(ruleType, ruleTypes.contains(ruleType));
		}

		SimpleInteractionConverter converter = new SimpleInteractionConverter(options, rules);
		return converter.inferInteractions(this.getBiopaxModel());
	}

	/**
	 * Extracts rule types from possible rule classes.
	 * @return possible rule types
	 */
	public static List<BinaryInteractionType> getPossibleRuleTypes(BioPAXLevel level)
	{
		List<BinaryInteractionType> rules = new ArrayList<BinaryInteractionType>();

		for (InteractionRule rule : SimpleInteractionConverter.getRules(level))
		{
			for (BinaryInteractionType ruleType : rule.getRuleTypes())
			{
				rules.add(ruleType);
			}
		}

		return rules;
	}

	public List<BinaryInteractionType> getRuleTypes()
	{
		return ruleTypes;
	}

	public void write(OutputStream os)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));

			for (Object o : getEdges())
			{
				SIFEdge edge = (SIFEdge) o;
				SIFNode source = (SIFNode) edge.getSource();
				SIFNode target = (SIFNode) edge.getTarget();

				writer.write(source.getText() + "\t");
				writer.write(edge.getType().getTag() + "\t");
				writer.write(target.getText() + "\n");
			}

			writer.close();
		}
		catch (IOException e){e.printStackTrace();}
	}

	// Had to implement these methods to make SIF graph a BioPAX graph

	public List<String> getPathwayNames()
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public String getPathwayRDFID()
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public int numberOfUnemptyPathways()
	{
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public List<String> namesOfUnemptyPathways() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public String createGlobalPathway(String name)
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public String createPathway(String name, List<String> intids)
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

	public Map<String, PathwayHolder> getNameToPathwayMap()
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
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
