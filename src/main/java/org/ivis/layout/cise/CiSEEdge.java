package org.ivis.layout.cise;

import java.util.List;
import java.util.Iterator;

import org.ivis.layout.LNode;
import org.ivis.layout.fd.FDLayoutEdge;

/**
 * This class implements data and functionality required for CiSE layout per
 * edge.
 *
 * @author Esat Belviranli
 * @author Alptug Dilek
 * @author Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class CiSEEdge extends FDLayoutEdge
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/**
	 * Flag for inter-graph edges in the base is not good enough. So we define
	 * this one to mean: a CiSE edge is intra-cluster only if both its ends are
	 * on a common circle; not intra-cluster, otherwise!
	 */
	protected boolean isIntraCluster;

// -----------------------------------------------------------------------------
// Section: Constructors and initializations
// -----------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	public CiSEEdge(LNode source, LNode target, Object vEdge)
	{
		super(source, target, vEdge);
		this.isIntraCluster = true;
	}

// -----------------------------------------------------------------------------
// Section: Remaining methods
// -----------------------------------------------------------------------------
	/**
	 * This method checks whether this edge crosses with the input edge. It
	 * returns false, if any of the vertices those edges are incident to are
	 * not yet placed on the circle.
	 */
	public boolean crossesWithEdge(CiSEEdge other)
	{
		boolean result = false;
		CiSEOnCircleNodeExt sourceExt =
			((CiSENode)this.source).getOnCircleNodeExt();
		CiSEOnCircleNodeExt targetExt =
			((CiSENode)this.target).getOnCircleNodeExt();
		CiSEOnCircleNodeExt otherSourceExt =
			((CiSENode)other.source).getOnCircleNodeExt();
		CiSEOnCircleNodeExt otherTargetExt =
			((CiSENode)other.target).getOnCircleNodeExt();
		int sourcePos = -1;
		int targetPos = -1;
		int otherSourcePos = -1;
		int otherTargetPos = -1;

		if (sourceExt != null)
		{
			sourcePos = sourceExt.getIndex();
		}

		if (targetExt != null)
		{
			targetPos = targetExt.getIndex();
		}

		if (otherSourceExt != null)
		{
			otherSourcePos = otherSourceExt.getIndex();
		}

		if (otherTargetExt != null)
		{
			otherTargetPos = otherTargetExt.getIndex();
		}

		if (!this.isInterGraph && !other.isInterGraph)
		{
			if (this.source.getOwner() != this.target.getOwner())
			{
				result = false;
			}
			else
			{
				// if any of the vertices those two edges are not yet placed
				if (sourcePos == -1 || targetPos == -1 ||
					otherSourcePos == -1 || otherTargetPos == -1)
				{
					result = false;
				}

				int otherSourceDist = otherSourceExt.getCircDistWithTheNode(sourceExt);
				int otherTargetDist = otherTargetExt.getCircDistWithTheNode(sourceExt);
				int thisTargetDist = targetExt.getCircDistWithTheNode(sourceExt);

				if (thisTargetDist < Math.max(otherSourceDist, otherTargetDist) &&
					thisTargetDist > Math.min(otherSourceDist, otherTargetDist) &&
					otherTargetDist != 0 && otherSourceDist != 0)
				{
					result = true;
				}
			}
		}
		else
		{
			result = true;
		}

		return result;
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
			CiSEEdge edge = (CiSEEdge)iter.next();
			totalCrossing += crossingWithEdge(edge);
		}

		return totalCrossing;
	}

	/**
	 * This method returns 1 if this edge crosses with the input edge, 0
	 * otherwise.
	 */
	public int crossingWithEdge(CiSEEdge other)
	{
		boolean crosses = this.crossesWithEdge(other);
		int result = 0;

		if (crosses)
		{
			result = 1;
		}
		
		return result;
	}
}