package org.ivis.layout.cise;

import java.util.Iterator;
import java.util.Set;
import java.awt.Point;
import java.awt.Dimension;

import org.ivis.layout.*;
import org.ivis.layout.fd.FDLayoutNode;
import org.ivis.util.IMath;

/**
 * This class implements data and functionality required for CiSE layout per
 * node.
 *
 * @author Esat Belviranli
 * @author Alptug Dilek
 * @author Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class CiSENode extends FDLayoutNode
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/**
	 * Amount by which this node will be rotated in this iteration. Note that
	 * clockwise rotation is positive and counter-clockwise is negative.
	 */
	public double rotationAmount;

	/*
	 * Extension for on-circle nodes
	 */
	private CiSEOnCircleNodeExt onCircleNodeExt;

// -----------------------------------------------------------------------------
// Section: Constructors and initializations
// -----------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	public CiSENode(LGraphManager gm, Object vNode)
	{
		super(gm, vNode);
		this.onCircleNodeExt = null;
	}

	/**
	 * Alternative constructor
	 */
	public CiSENode(LGraphManager gm, Point loc, Dimension size, Object vNode)
	{
		super(gm, loc, size, vNode);
		this.onCircleNodeExt = null;
	}

// -----------------------------------------------------------------------------
// Section: Accessors and mutators
// -----------------------------------------------------------------------------
	/**
	 * This method limits the input displacement with the maximum possible.
	 */
	public double getLimitedDisplacement(double displacement)
	{
		if (Math.abs(displacement) > CiSEConstants.MAX_NODE_DISPLACEMENT)
		{
			displacement = CiSEConstants.MAX_NODE_DISPLACEMENT * IMath.sign(displacement);
		}

		return displacement;
	}

	/**
	 * This method returns the extension of this node for on-circle nodes. This
	 * extension is null if this node is a non-on-circle node.
	 */
	public CiSEOnCircleNodeExt getOnCircleNodeExt()
	{
		return this.onCircleNodeExt;
	}

	/**
	 * This method returns neighbors of this node which are on-circle, not 
	 * in-circle.
	 */
	public Set<CiSENode> getOnCircleNeighbors()
	{
		Set<CiSENode> neighbors = this.getNeighborsList();
		
		Iterator<CiSENode> neighborIterator = neighbors.iterator();
		
		while (neighborIterator.hasNext()) 
		{
			CiSENode neighbor = neighborIterator.next();

			if (neighbor.getOnCircleNodeExt() == null ||
				!neighbor.getClusterID().equals(this.getClusterID()))
			{
				neighborIterator.remove();
			}
		}
		return neighbors;
	}

	/**
	 * This method sets this node as an on-circle node by creating an extension
	 * for it.
	 */
	public CiSEOnCircleNodeExt setAsOnCircleNode()
	{
		assert this.onCircleNodeExt == null;
		this.onCircleNodeExt = new CiSEOnCircleNodeExt(this);
		return this.onCircleNodeExt;
	}

	/**
	 * This method sets this node as an non on-circle node by deleting the 
	 * extension for it.
	 */
	public void setAsNonOnCircleNode()
	{
		assert this.onCircleNodeExt != null;
		this.onCircleNodeExt = null;
	}
// -----------------------------------------------------------------------------
// Section: Remaining methods
// -----------------------------------------------------------------------------
	/**
	 * This method moves this node as a result of the computations at the end of
	 * this iteration.
	 */
	public void move()
	{
		CiSELayout layout =
			(CiSELayout)this.getOwner().getGraphManager().getLayout();

		this.displacementX = getLimitedDisplacement(this.displacementX);
		this.displacementY = getLimitedDisplacement(this.displacementY);

		// First propogate movement to children if it's a circle
		if (this.getChild() != null)
		{
			// Take size into account when reflecting total force into movement!
			int noOfNodesOnCircle = this.getChild().getNodes().size();
			this.displacementX /= noOfNodesOnCircle;
			this.displacementY /= noOfNodesOnCircle;
			assert noOfNodesOnCircle >= 2;

			Iterator iter = this.getChild().getNodes().iterator();

			while (iter.hasNext())
			{
				CiSENode node = (CiSENode)iter.next();
				node.moveBy(this.displacementX, this.displacementY);
				layout.totalDisplacement +=
					Math.abs(displacementX) + Math.abs(displacementY);
			}
		}

		this.moveBy(this.displacementX, this.displacementY);
		layout.totalDisplacement +=
			Math.abs(displacementX) + Math.abs(displacementY);

		if (this.getChild() != null)
		{
			this.getChild().updateBounds(true);
		}

		this.displacementX = 0.0;
		this.displacementY = 0.0;
	}
}