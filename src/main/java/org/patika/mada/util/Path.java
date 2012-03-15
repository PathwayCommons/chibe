package org.patika.mada.util;

import org.gvt.model.BioPAXGraph;
import org.patika.mada.graph.Edge;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;

import java.util.LinkedList;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class Path 
{
	//==============================================================================================
	// Section: Instance variables
	//==============================================================================================

	/**
	 * Nodes of the path
	 */
	private LinkedList<Node> nodes;

	/**
	 * Edges of the path
	 */
	private LinkedList<Edge> edges;

	/**
	 * All graph objects, i.e. nodes + edges
	 */
	private LinkedList<GraphObject> objects;

	/**
	 * Path's being positive (1) or negative (-1)
	 */
	private int sign;

	/**
	 * The breadth length of this path. this would be the number of transition in the graph, but
	 * it is more complicated in BioPAX.
	 */
	private int length;

	/**
	 * The length is only increased when we add a breadth-node after an edge. This flag is turned on
	 * after adding an edge, and turned off after adding a node.
	 */
	private boolean alertForLengthUpdate;



	//==============================================================================================
	// Section: Constructor
	//==============================================================================================

	public Path()
	{
		nodes = new LinkedList<Node>();
		edges = new LinkedList<Edge>();
		objects = new LinkedList<GraphObject>();
		sign = 1;
		length = 0;
	}
	
	//==============================================================================================
	// Section: Methods
	//==============================================================================================

	public LinkedList<Node> getNodes()
	{
		return nodes;
	}

	public LinkedList<Edge> getEdges()
	{
		return edges;
	}

	public LinkedList<GraphObject> getObjects()
	{
		return objects;
	}

	public int getSign()
	{
		return sign;
	}

	/**
	 * Tells whether the object is on the path.
	 * @param obj to ask
	 * @return true if on path
	 */
	public boolean contains(GraphObject obj)
	{
		return this.objects.contains(obj);
	}

	/**
	 * Adds a node to the path.
	 */
	public void addNode(Node node)
	{
		assert edges.isEmpty() || 
			edges.getLast().getTargetNode() == node || 
			nodes.getLast().getParents().contains(node) || 
			nodes.getLast().getChildren().contains(node);

		nodes.add(node);
		objects.add(node);

		if (alertForLengthUpdate)
		{
			if (node.isBreadthNode())
			{
				length++;
			}
			alertForLengthUpdate = false;
		}
	}
	
	/**
	 * Adds an edge to the path.
	 */
	public void addEdge(Edge edge)
	{
		assert edge.getSourceNode() == nodes.getLast();
		
		edges.add(edge);
		objects.add(edge);

		sign *= edge.getSign();
		alertForLengthUpdate = true;
	}

	/**
	 * Gets the length of the path, i.e. number of edges.
	 */
	public int getLength()
	{
		return length;
	}

	public void highlight(boolean on)
	{
		for (Node node : nodes)
		{
			node.setHighlight(on);
		}
		for (Edge edge : edges)
		{
			edge.setHighlight(on);
		}
	}

	/**
	 * This method is used when we need to replace the contents of this Path with the complementary
	 * objects in the parameter graph.
	 * @param graph to switch to
	 */
	public void replaceElements(BioPAXGraph graph)
	{
		LinkedList<Node> tempNodeList = new LinkedList<Node>();

		for (Node node : nodes)
		{
			Node n = (Node) graph.getCorrespMember(node);
			assert n != null;
			tempNodeList.add(n);
		}
		nodes = tempNodeList;

		LinkedList<Edge> tempEdgeList = new LinkedList<Edge>();

		for (Edge edge : edges)
		{
			Edge e = (Edge) graph.getCorrespMember(edge);
			assert e != null;
			tempEdgeList.add(e);
		}
		edges = tempEdgeList;

		LinkedList<GraphObject> tempObjectList = new LinkedList<GraphObject>();

		for (GraphObject object : objects)
		{
			GraphObject o = graph.getCorrespMember(object);
			assert o != null;
			tempObjectList.add(o);
		}
		objects = tempObjectList;
	}

	public String toString()
	{
		return nodes.get(0).getName() +
			" --" + getLength() + "-" + (sign > 0 ? "> " : "| ") +
			nodes.get(nodes.size()-1).getName();
	}
}
