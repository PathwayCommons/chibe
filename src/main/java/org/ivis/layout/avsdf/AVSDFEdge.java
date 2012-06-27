package org.ivis.layout.avsdf;

import java.util.List;
import java.util.Iterator;

import org.ivis.layout.LEdge;

/**
 * This class implements data and functionality required for AVSDF layout per
 * edge.
 *
 * @author Esat Belviranli
 * @author Alptug Dilek
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class AVSDFEdge extends LEdge
{
// -----------------------------------------------------------------------------
// Section: Constructors and initializations
// -----------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	public AVSDFEdge(AVSDFNode source, AVSDFNode target, Object vEdge)
	{
		super(source, target, vEdge);
	}

// -----------------------------------------------------------------------------
// Section: Accessor methods
// -----------------------------------------------------------------------------
	/**
	 * This method returns the other end of this edge.
	 */
	public AVSDFNode getOtherEnd(AVSDFNode node)
	{
		return (AVSDFNode)(super.getOtherEnd(node));
	}

// -----------------------------------------------------------------------------
// Section: Remaining methods
// -----------------------------------------------------------------------------
	/**
	 * This method checks whether this edge crosses with the input edge. It
	 * returns false, if any of the vertices those edges are incident to are not
	 * yet placed on the circle.
	 */
	public boolean crossesWithEdge(AVSDFEdge other)
	{
		int sourcePos = ((AVSDFNode)this.source).getIndex();
		int targetPos = ((AVSDFNode)this.target).getIndex();
		int otherSourcePos = ((AVSDFNode)other.source).getIndex();
		int otherTargetPos = ((AVSDFNode)other.target).getIndex();

		// if any of the vertices those two edges are not yet placed
		if (sourcePos == -1 || targetPos == -1 ||
			otherSourcePos == -1 || otherTargetPos == -1)
		{
			return false;
		}

		int otherSourceDist = ((AVSDFNode)other.source).
			getCircDistWithTheNode(((AVSDFNode)this.source));
		int otherTargetDist = ((AVSDFNode)other.target).
			getCircDistWithTheNode(((AVSDFNode)this.source));
		int thisTargetDist =  ((AVSDFNode)this.target).
			getCircDistWithTheNode(((AVSDFNode)this.source));

		if (thisTargetDist < Math.max(otherSourceDist, otherTargetDist) &&
			thisTargetDist > Math.min(otherSourceDist, otherTargetDist) &&
			otherTargetDist != 0 && otherSourceDist != 0)
		{
			return true;
		}

		return false;
	}

	/**
	 * This method returns 1 if this edge crosses with the input edge, 0
	 * otherwise.
	 */
	public int crossingWithEdge(AVSDFEdge other)
	{
		boolean result = this.crossesWithEdge(other);
		int res = 0;

		if (result)
		{
			res = 1;
		}

		return res;
	}

	/**
	 * This method calculates the total number of crossings of this edge with
	 * all the edges given in the input list.
	 */
	public int calculateTotalCrossingWithList(List edgeList)
	{
		int totalCrossing = 0;
		Iterator iter = edgeList.iterator();

		while (iter.hasNext())
		{
			AVSDFEdge edge = (AVSDFEdge)iter.next();
			totalCrossing += crossingWithEdge(edge);
		}

		return totalCrossing;
	}
}