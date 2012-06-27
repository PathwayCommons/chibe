package org.ivis.layout.six;

import org.ivis.layout.cise.*;
import org.ivis.layout.LGraphManager;
import org.ivis.util.IMath;

/**
 * This class implements Six circular layout specific data and functionality
 * required for nodes.
 *
 * @author: Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class SixCircularNode extends CiSENode
{
// -----------------------------------------------------------------------------
// Section: Constructors and initialization
// -----------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	public SixCircularNode(LGraphManager gm, Object vNode)
	{
		super(gm, vNode);
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
		SixCircularLayout layout = (SixCircularLayout) this.graphManager.getLayout();
		this.displacementX = layout.coolingFactor * (this.springForceX + this.repulsionForceX);
		this.displacementY = layout.coolingFactor * (this.springForceY + this.repulsionForceY);

		if (Math.abs(this.displacementX) > layout.maxNodeDisplacement)
		{
			this.displacementX = layout.maxNodeDisplacement *
				IMath.sign(this.displacementX);
		}

		if (Math.abs(this.displacementY) > layout.maxNodeDisplacement)
		{
			this.displacementY = layout.maxNodeDisplacement *
				IMath.sign(this.displacementY);
		}

		assert this.child == null;

		// Since circles do not move during this iteration, we only take the
		// rotational amount for each on-circle node and move the on-circle node
		// by this amount

		SixCircularCircle circle = (SixCircularCircle)this.owner;
		CircularForce circularForce = circle.decomposeForce(this);
		CiSEOnCircleNodeExt onCircleNodeExt = this.getOnCircleNodeExt();

		// Moving on-circle nodes by this calculated amount is too drastic;
		// taking a small portion of it seems to work fine!
		
		double theta = circularForce.getRotationAmount() / circle.getRadius() / 15.0;
		onCircleNodeExt.setAngle(onCircleNodeExt.getAngle() + theta);
		onCircleNodeExt.updatePosition();
		layout.totalDisplacement += circularForce.getRotationAmount();

		this.springForceX = 0;
		this.springForceY = 0;
		this.repulsionForceX = 0;
		this.repulsionForceY = 0;
		this.displacementX = 0;
		this.displacementY = 0;
	}
}