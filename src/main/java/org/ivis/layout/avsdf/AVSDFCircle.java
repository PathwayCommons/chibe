package org.ivis.layout.avsdf;

import java.util.*;

import org.ivis.layout.*;

/**
 * This class implements data and functionality required for AVSDF layout per
 * circle.
 *
 * @author Alptug Dilek
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class AVSDFCircle extends LGraph
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/*
	 * Holds the ordering of the nodes on this circle
	 */
	private AVSDFNode[] inOrder;

	/*
	 * Index that determines the vertex to be placed on this circle
	 */
	private int currentIndex;

	/*
	 * Node spacing in between the nodes on this circle
 	 */
	private int nodeSeperation;

	/*
	 * Stack used in determining the order of the nodes on this circle
 	 */
	private Stack<AVSDFNode> stack;

	/*
	 * Parameters that determine the size and position of this circle
	 */
	private double perimeter = 0;
	private double centerX = 0;
	private double centerY = 0;
	private double radius = 0;

// -----------------------------------------------------------------------------
// Section: Constructors and initializations
// -----------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	public AVSDFCircle(LNode parent, LGraphManager graphMgr, Object vObject)
	{
		super(parent, graphMgr, vObject);
		currentIndex = 0;
		stack = new Stack<AVSDFNode>();
	}

	public void initOrdering()
	{
		inOrder = new AVSDFNode[getNodes().size()];
	}

// -----------------------------------------------------------------------------
// Section: Accessors
// -----------------------------------------------------------------------------
	/**
	 * This method returns the array in which the nodes of this circle are kept
	 * in order.
	 */
	public AVSDFNode[] getOrder()
	{
		return this.inOrder;
	}

	/**
	 * This method returns the x-coordinate of the center of this circle.
	 */
	public double getCenterX()
	{
		return centerX;
	}

	/**
	 * This method returns the y-coordinate of the center of this circle.
	 */
	public double getCenterY()
	{
		return centerY;
	}

	/**
	 * This method returns the radius of this circle.
	 */
	public double getRadius()
	{
		return radius;
	}

	/**
	 * This method returns the total number of vertices of this circle.
	 */
	public int getSize()
	{
		return this.getNodes().size();
	}

	/**
	 * This method calculates and returns the total number of crossings in this
	 * circle by adding up the crossing number of individual nodes on it.
	 */
	public int getTotalCrossingOfCircle()
	{
		int crossingNumber = 0;

		for (int i = 0; i < inOrder.length; i++)
		{
			int nodeCrossing = inOrder[i].getTotalCrossingOfEdges();

			if (nodeCrossing == -1)
			{
				return -1;
			}

			crossingNumber += nodeCrossing;
		}

		return crossingNumber / 4;
	}

	/**
	 * This method checks whether or not all of the vertices of this circle are
	 * assigned an index on the circle.
	 */
	public boolean hasFinishedOrdering()
	{
		return (this.currentIndex == this.getNodes().size());
	}

	/**
	 * This method returns the node seperation of this circle.
	 */
	public int getNodeSeperation()
	{
		return nodeSeperation;
	}

	/**
	 * This method sets the node seperation of this circle.
	 */
	public void setNodeSeperation(int nodeSeperation)
	{
		this.nodeSeperation = nodeSeperation;
	}

// -----------------------------------------------------------------------------
// Section: Remaining methods
// -----------------------------------------------------------------------------
	/**
	 * This method traverses the vertices of this circle and corrects the angle
	 * of the vertices with respect to their circle indices.
	 */
	public void correctAngles()
	{
		currentIndex = 0;

		for (int i=0; i<inOrder.length; i++)
		{
			putInOrder(inOrder[i]);
		}
	}

	/**
	 * This method puts the given node on the circle in the current order and
	 * sets its angle appropriately.
	 */
	public void putInOrder(AVSDFNode node)
	{
		assert getNodes().contains(node): "The node must be a member of LGraph";

		inOrder[currentIndex] = node;
		node.setIndex(currentIndex);

		if (currentIndex == 0)
		{
			node.setAngle(0.0);
		}
		else
		{
			node.setAngle(inOrder[currentIndex - 1].getAngle() + 2 * Math.PI *
				(node.getHalfTheDiagonal() + nodeSeperation +
					inOrder[currentIndex - 1].getHalfTheDiagonal()) /
				perimeter);
		}

		currentIndex++;
	}

	/**
	 * This method returns the next node to be placed on this circle with
	 * respect to the AVSDF algorithm.
	 */
	public AVSDFNode findNodeToPlace()
	{
		// find the smallest degree vertex if the stack is empty
		AVSDFNode sDegreeNode = null;

		if (stack.empty())
		{
			sDegreeNode = findUnorderedSmallestDegreeNode();
		}
		// find the first vertex in the stack not yet placed
		else
		{
			boolean foundUnorderNode = false;

			while (!foundUnorderNode && !stack.isEmpty())
			{
				sDegreeNode = stack.pop();
				foundUnorderNode = !sDegreeNode.isOrdered();
			}

			if (!foundUnorderNode)
			{
				sDegreeNode = null;
			}
		}

		// If no unordered vertex is found in the stack, find one from the
		// remaining ones
 		if (sDegreeNode == null)
		{
			sDegreeNode = findUnorderedSmallestDegreeNode();
		}

		// Add the unorderd neigbors of this node to the stack
		if (sDegreeNode != null)
		{
			AVSDFNode[] neighbors = sDegreeNode.getNeigborsSortedByDegree();

			for (int i = neighbors.length-1; i >= 0; i--)
			{
				if (!neighbors[i].isOrdered())
				{
					stack.push(neighbors[i]);
				}
			}
		}

		return sDegreeNode;
	}

	/**
	 * This method calculates the radius of this circle with respect to the
	 * sizes of the vertices and the node separation parameter.
	 */
	public void calculateRadius()
	{
		double totalDiagonal = 0;
		Iterator iterator = getNodes().iterator();
		double temp;

		while (iterator.hasNext())
		{
			LNode node = (LNode)iterator.next();

			temp = node.getWidth() * node.getWidth() +
				node.getHeight() * node.getHeight();
			totalDiagonal += Math.sqrt(temp);
		}

		perimeter = totalDiagonal + this.getNodes().size() * nodeSeperation;
		double a = perimeter / (2 * Math.PI);
		this.getParent().setWidth(2 * a);
		this.getParent().setHeight(2 * a);
		this.getParent().setCenter(this.getParent().getWidth(),
			this.getParent().getHeight());

		centerX = getParent().getCenterX();
		centerY = getParent().getCenterY();
		radius = getParent().getHeight() / 2;
	}

	/**
	 * This method calculates the total number of crossings of all vertices of
	 * this circle.
	 */
	public void calculateEdgeCrossingsOfNodes()
	{
		Iterator iter = this.getNodes().iterator();

		while (iter.hasNext())
		{
			AVSDFNode node = (AVSDFNode) iter.next();

			node.calculateTotalCrossing();
		}
	}

	/**
	 * This method sets the index of each vertex to its position in inOrder
	 * array. Note that index of a node can be different from its place in the
	 * array due to crossing reduction phase of the AVSDF algorithm. It loads
	 * old index values to vertices due to an increase in the number of
	 * crossings with the new indices.
	 */
	public void loadOldIndicesOfNodes()
	{
		for (int i = 0; i < this.getSize(); i++)
		{
			inOrder[i].setIndex(i);
		}
	}

	/**
	 * This method sets the position of each node in inOrder array to its index.
	 * Note that index of a node can be different from its place in the inOrder
	 * array due to crossing reduction phase of the AVSDF algorithm. This method
	 * puts the nodes to their new index values in inOrder array due to a
	 * decrease in the number of crossings with the new indices.
	 */
	public void reOrderVertices()
	{
		Iterator iter = getNodes().iterator();

		while (iter.hasNext())
		{
			AVSDFNode node = (AVSDFNode) iter.next();
			inOrder[node.getIndex()] = node;
		}
	}

	/**
	 * This method finds and returns the unordered smallest degree vertex on
	 * this circle.
	 */
	private AVSDFNode findUnorderedSmallestDegreeNode()
	{
		int minDegree = Integer.MAX_VALUE;
		AVSDFNode sDegreeNode = null;

		Iterator iterator = this.getNodes().iterator();

		while (iterator.hasNext())
		{
			AVSDFNode node = (AVSDFNode)iterator.next();

			if (node.getDegree() < minDegree && !node.isOrdered())
			{
				minDegree = node.getDegree();
				sDegreeNode = node;
			}
		}

		return sDegreeNode;
	}
}