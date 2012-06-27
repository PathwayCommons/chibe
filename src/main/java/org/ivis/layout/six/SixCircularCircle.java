package org.ivis.layout.six;

import java.util.Iterator;

import org.ivis.layout.cise.*;
import org.ivis.layout.LGraphManager;
import org.ivis.layout.LNode;

/**
 * This class implements data and functionality required for Six circular layout
 * per cluster.
 *
 * @author: Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class SixCircularCircle extends CiSECircle
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------

// -----------------------------------------------------------------------------
// Section: Constructors and initialization
// -----------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	public SixCircularCircle(LNode parent, LGraphManager graphMgr, Object vNode)
	{
		super(parent, graphMgr, vNode);
	}

// -----------------------------------------------------------------------------
// Section: Remaining methods
// -----------------------------------------------------------------------------
	/**
	 * This method calculates the optimal orientation of this circle by rotating
	 * its nodes in an exhaustive fashion as described in the paper.
	 */
	public void calculateOptimalOrientation()
	{
		int noOfNodesOnCircle = this.getOnCircleNodes().size();

		// First rotate w/ the current ordering and find the minimum

		double minimalPotentialEnergy = this.calcPotentialEnergy();
		int minimalPotentialIndex = 0;
		double potentialEnergy;

		for (int i = 1; i < noOfNodesOnCircle; i++)
		{
			this.rotateNodes();
			potentialEnergy = this.calcPotentialEnergy();

			if (potentialEnergy < minimalPotentialEnergy)
			{
				minimalPotentialEnergy = potentialEnergy;
				minimalPotentialIndex = i;
			}
		}

		// To get back to original orientation
		this.rotateNodes();

		// Now reverse the order of the nodes on the circle
		this.reverseNodes();

		// Do the rotation again w/ reversed order to see if we can improve

		boolean reverseOrderIsBetter = false;

		for (int i = 0; i < noOfNodesOnCircle; i++)
		{
			potentialEnergy = this.calcPotentialEnergy();

			if (potentialEnergy < minimalPotentialEnergy)
			{
				minimalPotentialEnergy = potentialEnergy;
				minimalPotentialIndex = i;
				reverseOrderIsBetter = true;
			}

			this.rotateNodes();
		}

		if (!reverseOrderIsBetter)
		{
			// Revert the order
			this.reverseNodes();
		}

		this.rotateNodesToOptimal(minimalPotentialIndex);
	}

	/**
	 * This method rotates the nodes of this circle clockwise by one.
	 */
	private void rotateNodes()
	{
		Iterator iterator = this.getOnCircleNodes().iterator();
		int noOfNodesOnCircle = this.getOnCircleNodes().size();
		CiSENode node;
		int index;

		while (iterator.hasNext())
		{
			node = (CiSENode)iterator.next();
			index = node.getOnCircleNodeExt().getIndex();
			node.getOnCircleNodeExt().setIndex((index + 1) % noOfNodesOnCircle);
		}

		this.reCalculateNodeAnglesAndPositions();
	}

	/*
	 * This method rotates nodes back to the orientation where optimal was
	 * achieved.
	 */
	private void rotateNodesToOptimal(int minimalPotentialIndex)
	{
		int noOfNodesOnCircle = this.getOnCircleNodes().size();

		if (minimalPotentialIndex == 0)
		// Current orientation is optimal
		{
			return;
		}

		// Rotate each node to increase its index by the calculated amount

		Iterator iterator = this.getOnCircleNodes().iterator();
		CiSENode node;
		CiSEOnCircleNodeExt nodeExt;

		while (iterator.hasNext())
		{
			node = (CiSENode)iterator.next();
			nodeExt = node.getOnCircleNodeExt();

			nodeExt.setIndex((nodeExt.getIndex() + minimalPotentialIndex) %
				noOfNodesOnCircle);
		}

		this.reCalculateNodeAnglesAndPositions();
	}

	/**
	 * This method calculates the potential energy of this cluster w.r.t. its
	 * inter-cluster edges. Typically the goal is to find an orientation of this
	 * circle for which this potential is minimized.
	 */
	public double calcPotentialEnergy()
	{
		double potentialEnergy = 0.0;

		Iterator nodeIterator = this.getOutNodes().iterator();
		CiSENode node;
		Iterator edgeIterator;
		CiSEEdge edge;

		while (nodeIterator.hasNext())
		{
			node = (CiSENode)nodeIterator.next();
			edgeIterator = node.getEdges().iterator();

			while (edgeIterator.hasNext())
			{
				edge = (CiSEEdge)edgeIterator.next();

				if (edge.isInterGraph())
				{
					edge.updateLength();
					assert !edge.isOverlapingSourceAndTarget();
					potentialEnergy += edge.getLength();
				}
			}
		}

		return potentialEnergy;
	}

// -----------------------------------------------------------------------------
// Section: Class variables/constants
// -----------------------------------------------------------------------------

// -----------------------------------------------------------------------------
// Section: Class methods
// -----------------------------------------------------------------------------
}