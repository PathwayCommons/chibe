package org.gvt.model.biopaxl2;

import org.gvt.command.CreateConnectionCommand;
import org.gvt.model.EdgeModel;
import org.gvt.model.NodeModel;
import org.gvt.model.IBioPAXEdge;
import org.patika.mada.graph.Edge;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;

import java.util.*;

/**
 * Any edge that is used in BioPAX visual graph.
 * 
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public abstract class BioPAXEdge extends EdgeModel implements IBioPAXEdge
{
	protected int id;

	protected boolean transcription;

	public BioPAXEdge(NodeModel source, NodeModel target)
	{
		assert source != null;
		assert target != null;
		
		CreateConnectionCommand ccc = new CreateConnectionCommand();
		ccc.setSource(source);
		ccc.setTarget(target);
		ccc.setConnection(this);
		ccc.execute();
	}

	public BioPAXEdge(BioPAXEdge excised, Map<NodeModel, NodeModel> map)
	{
		this(map.get(excised.getSource()), map.get(excised.getTarget()));
		this.id = excised.getId();
		this.transcription = excised.isTranscription();
		this.setArrow(excised.getArrow());
		this.setColor(excised.getColor());
		this.setStyle(excised.getStyle());

		for (Object key : excised.getAllLabels())
		{
			this.putLabel(key, excised.getLabel(key));
		}

		((BioPAXL2Graph) ((Node) getSource()).getGraph()).putInExcisionMap(excised, this);
		this.putLabel(BioPAXL2Graph.EXCISED_FROM, excised);
	}

	public boolean isTranscription()
	{
		return transcription;
	}

	public void setTranscription(boolean transcription)
	{
		this.transcription = transcription;
	}

	public boolean isBreadthEdge()
	{
		return true;
	}

	public boolean isCausative()
	{
		return isDirected() && getSign() != 0;
	}

	public Node getSourceNode()
	{
		return (Node) this.getSource();
	}

	public Node getTargetNode()
	{
		return (Node) this.getTarget();
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public boolean isPositive()
	{
		return this.getSign() == Edge.POSITIVE;
	}

	public boolean isNegative()
	{
		return this.getSign() == Edge.NEGATIVE;
	}

	@Override
	public boolean isEquivalenceEdge()
	{
		return false;
	}

	public boolean isPTM()
	{
		return !isTranscription();
	}

	/**
	 * Directed by default. Is overwritten in some undirected edges.
	 */
	public boolean isDirected()
	{
		return true;
	}

	public boolean isHighlighted()
	{
		return this.isHighlight();
	}

	public Set<GraphObject> getRequisites()
	{
		HashSet<GraphObject> reqs = new HashSet<GraphObject>();

		reqs.add((GraphObject) this.getSource());
		reqs.add((GraphObject) this.getTarget());

		return reqs;
	}

	public List<String[]> getInspectable()
	{
		List<String[]> list = new ArrayList<String[]>();

		return list;
	}

	/**
	 * Used for differentiating and matching between chisio objects and based biopax model.
	 * @return
	 */
	public abstract String getIDHash();
}
