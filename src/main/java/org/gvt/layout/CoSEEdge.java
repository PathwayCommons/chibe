package org.gvt.layout;

import java.util.Vector;
import java.util.Iterator;

/**
 * This class implements CoSE specific data and functionality for edges.
 *
 * @author Erhan Giral
 * @author Ugur Dogrusoz
 * @author Cihan Kucukkececi
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class CoSEEdge extends LEdge
{
// -----------------------------------------------------------------------------
// Section: Instance Variables
// -----------------------------------------------------------------------------
	/**
	 * Desired length of this edge after layout
	 */
	public double idealLength = Double.MAX_VALUE;

	/**
	 * Tree reduction related vaiables. Trees are reduced temporarily during
	 * layout for efficiency purposes.
	 */
	protected boolean reduced;
	protected CoSENode growedFrom = null;

// -----------------------------------------------------------------------------
// Section: Constructors and Initializations
// -----------------------------------------------------------------------------
	public CoSEEdge(CoSENode source, CoSENode target)
	{
		super(source, target);
	}

// -----------------------------------------------------------------------------
// Section: Remaining methods
// -----------------------------------------------------------------------------
	/**
	 * This method returns the ideal length of this edge.
	 */
	public double getIdealLengthModifier()
	{
		// We need to calculate the ideal length
		if (this.idealLength == Double.MAX_VALUE)
		{
			if (this.isInterGraph)
			{
				Vector allParentsOfTarget = target.getAllParents();
				Vector allParentsOfSource = source.getAllParents();
				CoSENode commonAncestor = null;

				Iterator itr = allParentsOfSource.iterator();

				while (itr.hasNext())
				{
					CoSENode parent = (CoSENode) itr.next();

					if (allParentsOfTarget.contains(parent))
					{
						commonAncestor = parent;
						break;
					}
				}

				int commonAncestorDepth;

				if (commonAncestor == this.source.getOwner().
					getGraphManager().getRoot().getParent())
				{
					commonAncestorDepth = 0;
				}
				else
				{
					commonAncestorDepth =
						commonAncestor.getInclusionTreeDepth();
				}

				int levels = this.source.getInclusionTreeDepth() +
					this.target.getInclusionTreeDepth() -
					2 * commonAncestorDepth;
				this.idealLength = 1 +
					CoSELayout.perLevelIdealEdgeLengthFactor * levels;
			}
			else
			{
				this.idealLength = 1;
			}
		}

		return this.idealLength;
	}
}