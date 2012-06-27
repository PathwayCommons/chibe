package org.gvt.layout;

import java.util.*;

import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.gvt.util.ChsGeometry;

/**
 * This class represents an edge (l-level) for layout purposes.
 *
 * @author Erhan Giral
 * @author Ugur Dogrusoz
 * @author Cihan Kucukkececi
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class LEdge
{
// -----------------------------------------------------------------------------
// Section: Instance Variables
// -----------------------------------------------------------------------------
	/**
	 * Source and target nodes of this edge
	 */
	public LNode source;
	public LNode target;

	/**
	 * Whether this edge is an intergraph one
	 */
	protected boolean isInterGraph;

	/**
	 * The length of this edge ( l = sqrt(x^2 + y^2) )
	 */
	public double length;
	public double lengthx;
	public double lengthy;

	/**
	 * Whether an end of this edge is of non-uniform size requiring special edge
	 * length calculations
	 */
	public boolean overlapingTargetAndSource = false;

	/**
	 * Benpoints for this edge. List items' type is EdgeBendpoint 
	 */
	public List bendpoints;

// -----------------------------------------------------------------------------
// Section: Constructors and Initializations
// -----------------------------------------------------------------------------
	public LEdge(LNode source, LNode target)
	{
		this.bendpoints = new ArrayList();

		this.source = source;
		this.target = target;

		// add created edge to the edge lists of the source and target nodes
		this.source.edges.add(this);
		this.target.edges.add(this);

		if (this.source.getOwner() != this.target.getOwner())
		{
			this.isInterGraph = true;
		}
		else
		{
			this.isInterGraph = false;
		}
	}

// -----------------------------------------------------------------------------
// Section: Remaining Methods
// -----------------------------------------------------------------------------
	/**
	 * This method returns the end of this node different from the input one.
	 */
	public LNode getOtherEnd(LNode node)
	{
		if (this.source.equals(node))
		{
			return this.target;
		}
		else if (this.target.equals(node))
		{
			return this.source;
		}
		else
		{
			throw new IllegalArgumentException(
				"Node is not incident " + "with this edge");
		}
	}

	/**
	 * This method updates the temporal attributes of this edge.
	 */
	public void recalculate()
	{
		double[] clipPointCoordinates;
		
		clipPointCoordinates = ChsGeometry.getIntersection(this.target.getRect(), 
															this.source.getRect());
		
		PrecisionPoint clipPoint1 = new PrecisionPoint(clipPointCoordinates[0], 
														clipPointCoordinates[1]);
		PrecisionPoint clipPoint2 = new PrecisionPoint(clipPointCoordinates[2], 
														clipPointCoordinates[3]);

		// Edge is clipped on both nodes' boundaries
		if (clipPoint1 != null && clipPoint2 != null)
		{
			this.lengthx = clipPoint1.x - clipPoint2.x;
			this.lengthy = clipPoint1.y - clipPoint2.y;

			// However we have an overlap so negate the distance vector
			if (this.source.getRect().intersects(this.target.getRect()))
			{
				this.lengthx = -this.lengthx;
				this.lengthy = -this.lengthy;
			}
		}
		else
		{
			this.lengthx =
				this.target.getCenterX() - this.source.getCenterX();
			this.lengthy =
				this.target.getCenterY() - this.source.getCenterY();

			// However we have an overlap, so apply full force
			if (this.source.getRect().intersects(this.target.getRect()))
			{
				this.overlapingTargetAndSource = true;
			}
		}

		this.length = Math.sqrt(
			this.lengthx * this.lengthx + this.lengthy * this.lengthy);

		if (this.length == 0)
		{
			this.length = CoSELayout.MIN_EDGE_LENGTH;
		}
	}
}