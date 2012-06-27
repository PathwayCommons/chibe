package org.ivis.layout.cise;

import java.util.*;

import org.ivis.layout.LNode;
import org.ivis.util.*;

/**
 * This class implements data and functionality required for CiSE layout per
 * on-circle node. In other words, it is an extension to CiSENode class for
 * on-circle nodes.
 *
 * @author Esat Belviranli
 * @author Alptug Dilek
 * @author Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class CiSEOnCircleNodeExt
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/*
	 * Associated CiSE node
	 */
	private CiSENode ciseNode;

	/*
	 * Holds the intra-cluster edges of this node, initially it is null. It
	 * will be calculated and stored when getIntraClusterEdges method is first
	 * called.
	 */
	private List<CiSEEdge> intraClusterEdges = null;

	/*
	 * Holds the inter-cluster edges of this node, initially it is null. It
	 * will be calculated and stored when getInterClusterEdges method is first
	 * called.
	 */
	private List<CiSEEdge> interClusterEdges = null;

	/*
	 * Holds relative position of this node with respect to its owner circle
	 * It is less than 0 (=-1) only if not assigned. Its unit is radian.
	 */
	private double angle = -1;

	/*
	 * Holds current index of this node within its owner circle; it is -1 if not
	 * assigned.
	 */
	private int orderIndex = -1;

	/*
	 * Indicates whether a swapping with next node in the owner circle order
	 * will cause no additional crossings or not.
	 */
	private boolean canSwapWithNext;

	/*
	 * Indicates whether a swapping with previous node in the owner circle order
	 * will cause no additional crossings or not.
	 */
	private boolean canSwapWithPrevious;

	/*
	 * Holds the total weighted displacement value calculated over a constant
	 * number of iterations used for deciding whether two nodes should be
	 * swapped.
	 */
	private double displacementForSwap;

// -----------------------------------------------------------------------------
// Section: Constructors and initializations
// -----------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	public CiSEOnCircleNodeExt(CiSENode ciseNode)
	{
		this.ciseNode = ciseNode;
	}

// -----------------------------------------------------------------------------
// Section: Accessors and mutators
// -----------------------------------------------------------------------------
	/**
	 * This method returns the associated CiSE node.
	 */
	public CiSENode getCiSENode()
	{
		return this.ciseNode;
	}

	/**
	 * This method returns the relative position of this node within its owner
	 * circle.
	 */
	public double getAngle()
	{
		return angle;
	}

	/**
	 * This method sets the relative position of this node within its owner
	 * circle. We keep the angle positive for easy debugging.
	 */
	public void setAngle(double angle)
	{
		this.angle = angle % IGeometry.TWO_PI;

		if (this.angle < 0)
		{
			this.angle += IGeometry.TWO_PI;
		}
	}

	/**
	 * This method returns current index of this node in its owner circle.
	 */
	public int getIndex()
	{
		return orderIndex;
	}

	/**
	 * This method sets current index of this node in its owner circle.
	 */
	public void setIndex(int index)
	{
		orderIndex = index;
	}

	/**
	 * This method returns the char code of this node based on the node index.
	 * First node of the cluster is 'a', second one is 'b", and so on. We only
	 * guarentee a unique char code up to 52 nodes in a cluster.
	 */
	public char getCharCode()
	{
		assert this.orderIndex >= 0;

		char charCode;

		if (this.orderIndex < 26)
		{
			charCode = (char)('a' + this.orderIndex);
		}
		else if (this.orderIndex < 52)
		{
			charCode = (char)('A' + this.orderIndex - 26);
		}
		else
		{
			charCode = '?';
		}

		return charCode;
	}

	/**
	 * This method returns the next node according to current ordering of the
	 * owner circle.
	 */
	public CiSENode getNextNode()
	{
		CiSECircle circle = (CiSECircle)this.ciseNode.getOwner();
		
		int totalNodes = circle.getOnCircleNodes().size();

		int nextNodeIndex = orderIndex + 1;

		if (nextNodeIndex == totalNodes)
		{
			nextNodeIndex = 0;
		}

		return circle.getOnCircleNodes().get(nextNodeIndex);
	}

	/**
	 * This method returns the previous node according to current ordering of
	 * the owner circle.
	 */
	public CiSENode getPrevNode()
	{
		CiSECircle circle = (CiSECircle)this.ciseNode.getOwner();

		int nextNodeIndex = orderIndex - 1;

		if (nextNodeIndex == -1)
		{
			nextNodeIndex = circle.getOnCircleNodes().size() - 1;
		}

		return circle.getOnCircleNodes().get(nextNodeIndex);
	}

	/**
	 * This method returns the extension of the next node according to current
	 * ordering of the owner circle.
	 */
	public CiSEOnCircleNodeExt getNextNodeExt()
	{
		return this.getNextNode().getOnCircleNodeExt();
	}

	/**
	 * This method returns the extension of the previous node according to
	 * current ordering of the owner circle.
	 */
	public CiSEOnCircleNodeExt getPrevNodeExt()
	{
		return this.getPrevNode().getOnCircleNodeExt();
	}

	public boolean canSwapWithNext()
	{
		return canSwapWithNext;
	}

	public boolean canSwapWithPrev()
	{
		return canSwapWithPrevious;
	}

	public double getDisplacementForSwap()
	{
		return displacementForSwap;
	}

	public void setDisplacementForSwap(double displacementForSwap)
	{
		this.displacementForSwap = displacementForSwap;
	}

	public void addDisplacementForSwap(double displacementIncrForSwap)
	{
		this.displacementForSwap = displacementIncrForSwap;
// This is what we intended (but above seems to work better):
//		this.displacementForSwap = (this.displacementForSwap +
//			displacementIncrForSwap) / 2.0;
	}

// -----------------------------------------------------------------------------
// Section: Remaining methods
// -----------------------------------------------------------------------------
	/**
	 * This method updates the absolute position of this node with respect to
	 * its angle and the position of node that owns the owner circle.
	 */
	public void updatePosition()
	{
		CiSECircle ownerGraph = (CiSECircle) this.ciseNode.getOwner();
		LNode parentNode = ownerGraph.getParent();

		double parentX = parentNode.getCenterX();
		double parentY = parentNode.getCenterY();

		double xDifference = ownerGraph.getRadius() * Math.cos(this.angle);
		double yDifference = ownerGraph.getRadius() * Math.sin(this.angle);

//		System.out.printf("upb %s:(%5.1f,%5.1f)\n",
//			new Object [] {this.getText(),
//				parentX + xDifference - this.x,
//				parentY + yDifference - this.y});

		this.ciseNode.setCenter(parentX + xDifference , parentY + yDifference);
	}

	/**
	 * This method returns the index difference of this node with the input
	 * node. Note that the index difference cannot be negative if both nodes are
	 * placed on the circle. Here -1 means at least one of the nodes are not yet
	 * placed on the circle.
	 */
	public int getCircDistWithTheNode(CiSEOnCircleNodeExt refNode)
	{
		int otherIndex = refNode.getIndex();

		if (otherIndex == -1 || this.getIndex() == -1)
		{
			return -1;
		}

		int diff = this.getIndex() - otherIndex;

		if (diff < 0)
		{
			diff += 
				((CiSECircle)this.ciseNode.getOwner()).getOnCircleNodes().size();
		}

		return diff;
	}

	/**
	 * This method calculates the total number of crossings the edges of this
	 * node cause.
	 */
	public int calculateTotalCrossing()
	{
		Iterator iter = getIntraClusterEdges().iterator();
		int count = 0;
		ArrayList temp = new ArrayList();

		temp.addAll(((CiSECircle) this.ciseNode.getOwner()).getIntraClusterEdges());
		temp.removeAll(this.ciseNode.getEdges());

		while (iter.hasNext())
		{
			CiSEEdge edge = (CiSEEdge)iter.next();
			count += edge.calculateTotalCrossingWithList(temp);
		}

		return count;
	}

	/*
	 * This method updates the conditions for swapping of this node with its
	 * previous and next neighbors on the associated circle.
	 */
	public void updateSwappingConditions()
	{
		int currentCrossingNumber = this.calculateTotalCrossing();
		int currentNodeIndex = this.orderIndex;

		CiSEOnCircleNodeExt nextNodeExt = this.getNextNode().getOnCircleNodeExt();
		this.orderIndex = nextNodeExt.getIndex();
		nextNodeExt.setIndex(currentNodeIndex);

		int tempCrossingNumber = this.calculateTotalCrossing();

		if (tempCrossingNumber > currentCrossingNumber)
		{
			canSwapWithNext = false;
		}
		else
		{
			canSwapWithNext = true;
		}

		nextNodeExt.setIndex(this.orderIndex);

		this.setIndex(currentNodeIndex);

		CiSEOnCircleNodeExt prevNodeExt = this.getPrevNode().getOnCircleNodeExt();
		this.orderIndex = prevNodeExt.getIndex();
		prevNodeExt.setIndex(currentNodeIndex);

		tempCrossingNumber = this.calculateTotalCrossing();

		if (tempCrossingNumber > currentCrossingNumber)
		{
			canSwapWithPrevious = false;
		}
		else
		{
			canSwapWithPrevious = true;
		}

		prevNodeExt.setIndex(this.orderIndex);

		this.setIndex(currentNodeIndex);
	}

	/**
	 * This method swaps this node with the specified neighbor (prev or next).
	 */
	public void swapWith(CiSEOnCircleNodeExt neighborExt)
	{
		assert this.getNextNode().getOnCircleNodeExt() == neighborExt ||
			this.getPrevNode().getOnCircleNodeExt() == neighborExt;

		((CiSECircle )this.ciseNode.getOwner()).
			swapNodes(this.ciseNode, neighborExt.ciseNode);
	}

	/**
	 * This method finds the number of crossings of inter cluster edges of this
	 * node with the inyer cluster edges of the other node.
	 */
	public int getInterClusterIntersections(CiSEOnCircleNodeExt other)
	{
		int count = 0;

		List<CiSEEdge> thisInterClusterEdges = this.getInterClusterEdges();
		List<CiSEEdge> otherInterClusterEdges = other.getInterClusterEdges();

		Iterator<CiSEEdge> iter1 = thisInterClusterEdges.iterator();

		while (iter1.hasNext())
		{
			CiSEEdge edge = iter1.next();
			PointD point1 = this.ciseNode.getCenter();
			PointD point2 = edge.getOtherEnd(this.ciseNode).getCenter();

			Iterator<CiSEEdge> iter2 = otherInterClusterEdges.iterator();

			while(iter2.hasNext())
			{
				CiSEEdge otherEdge = iter2.next();
				PointD point3 = other.ciseNode.getCenter();
				PointD point4 = otherEdge.getOtherEnd(other.ciseNode).getCenter();

				if (edge.getOtherEnd(this.ciseNode) !=
					otherEdge.getOtherEnd(other.ciseNode))
				{
					boolean result =
						IGeometry.doIntersect(point1, point2, point3, point4);

					if (result)
					{
						count++;
					}
				}
			}
		}

		return count;
	}

	/**
	 * This method returns the inter cluster edges of the associated node.
	 */
	public List<CiSEEdge> getInterClusterEdges()
	{
		if (interClusterEdges == null)
		{
			interClusterEdges = new ArrayList<CiSEEdge>();

			Iterator<CiSEEdge> iterator = this.ciseNode.getEdges().iterator();

			while (iterator.hasNext())
			{
				CiSEEdge edge = iterator.next();

				if (!edge.isIntraCluster)
				{
					interClusterEdges.add(edge);
				}
			}
		}

		return interClusterEdges;
	}

	/**
	 * This method returns the intra cluster edges of the associated node.
	 */
	public List<CiSEEdge> getIntraClusterEdges()
	{
		if (intraClusterEdges == null)
		{
			intraClusterEdges = new ArrayList<CiSEEdge>();

			Iterator<CiSEEdge> iterator = this.ciseNode.getEdges().iterator();

			while (iterator.hasNext())
			{
				CiSEEdge edge = iterator.next();

				if (edge.isIntraCluster)
				{
					intraClusterEdges.add(edge);
				}
			}
		}

		return intraClusterEdges;
	}
}