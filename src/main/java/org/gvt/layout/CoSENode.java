package org.gvt.layout;

import java.util.Iterator;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

/**
 * This class implements CoSE specific data and functionality for nodes.
 *
 * @author Erhan Giral
 * @author Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class CoSENode extends LNode
{
// -----------------------------------------------------------------------------
// Section: Instance Variables
// -----------------------------------------------------------------------------
	/**
	 * Spring, repulsion and gravitational forces acting on this node
	 */
	protected double springForceX;
	protected double springForceY;
	protected double repulsionForceX;
	protected double repulsionForceY;
	protected double gravitationForceX;
	protected double gravitationForceY;

	/**
	 * Amount by which this node is to be moved in this iteration
	 */
	private double displacementX;
	private double displacementY;

	/**
	 * Reduction related variables; subgraphs that form trees are reduced during
	 * initialization and grown back gradually.
	 */
	private boolean reduced;
	protected boolean reducedTreeRoot;

// -----------------------------------------------------------------------------
// Section: Constructors and Initialization
// -----------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	public CoSENode(LGraphManager gm)
	{
		super(gm);
	}

	/**
	 * Alternative constructor
	 */
	public CoSENode(LGraphManager gm, Point loc, Dimension size)
	{
		super(gm, loc, size);
	}

	public void initialize()
	{
		super.initialize();

		this.reduced = false;
		this.reducedTreeRoot = false;
	}

// -----------------------------------------------------------------------------
// Section: Accessors
// -----------------------------------------------------------------------------
	public boolean isReduced()
	{
		return this.reduced;
	}

	public void setReduced(boolean reduced)
	{
		// Note that for accurate accounting of the number of reduced nodes, we
		// assume: 1) this is called only when reversing the reduced status; 2)
		// for efficiency accounting is not done here but at invocation point.

		this.reduced = reduced;
	}

// -----------------------------------------------------------------------------
// Section: Remaining Methods
// -----------------------------------------------------------------------------
	/**
	 * This method positions this node randomly in both x and y dimensions.
	 */
	protected void scatter()
	{
		if (this.isReduced() && !this.reducedTreeRoot)
		{
			// So reduced nodes do not clutter the drawing centered at
			// (GRAVITY_CENTER_X,GRAVITY_CENTER_Y)

			this.setLocation(AbstractLayout.GRAVITY_CENTER_X + 1000,
				AbstractLayout.GRAVITY_CENTER_Y + 500);
		}
		else
		{
			// Scatter around (GRAVITY_CENTER_X,GRAVITY_CENTER_Y)
			super.scatter();
		}
	}

	/**
	 * This method returns number of unmarked (reduced) neighbors of this node.
	 */
	public int getNumberOFUnmarkedNeigbors()
	{
		Iterator itr = this.edges.iterator();
		int count = 0;

		while (itr.hasNext())
		{
			CoSEEdge lEdge = (CoSEEdge) itr.next();
			CoSENode otherEnd = (CoSENode) lEdge.getOtherEnd(this);

			if (!otherEnd.isReduced())
			{
				count++;
			}
		}

		return count;
	}

	/**
	 * This method recalculates the displacement related attributes of this
	 * object. These attributes are calculated at each layout iteration once,
	 * for increasing the speed of the layout.
	 */
	public void move()
	{
		this.displacementX = CoSELayout.coolingFactor * (this.springForceX +
			this.repulsionForceX +
			this.gravitationForceX);
//			+ this.oldDisplacementX * LNode.MOMENTUM_CONSTANT;

		this.displacementY = CoSELayout.coolingFactor * (this.springForceY +
			this.repulsionForceY +
			this.gravitationForceY);
//			+ this.oldDisplacementY * LNode.MOMENTUM_CONSTANT;

		if (Math.abs(this.displacementX) > MAX_DISPLACEMENT)
		{
			this.displacementX =
				MAX_DISPLACEMENT * CoSELayout.sign(this.displacementX);
		}

		if (Math.abs(this.displacementY) > MAX_DISPLACEMENT)
		{
			this.displacementY =
				MAX_DISPLACEMENT * CoSELayout.sign(this.displacementY);
		}

		if (this.child == null)
		// a simple node, just move it
		{
			this.moveBy(this.displacementX, this.displacementY);
		}
		else if (this.child.getNodes().size() == 0)
		// an empty compound node, again just move it
		{
			this.moveBy(this.displacementX, this.displacementY);
		}
		// non-empty compound node, propogate movement to children as well
		else
		{
			this.propogateDisplacementToChildren(this.displacementX,
				this.displacementY);
		}

//		System.out.printf("\t%s:s=(%5.1f,%5.1f) r=(%5.1f,%5.1f) g=(%5.1f,%5.1f)\n",
//			new Object [] {this.getText(),
// 			this.springForceX, this.springForceY,
//			this.repulsionForceX, this.repulsionForceY,
//			this.gravitationForceX, this.gravitationForceY});

		CoSELayout.totalDisplacement +=
			Math.abs(this.displacementX) + Math.abs(this.displacementY);

		this.springForceX = 0;
		this.springForceY = 0;
		this.repulsionForceX = 0;
		this.repulsionForceY = 0;
		this.gravitationForceX = 0;
		this.gravitationForceY = 0;
		this.displacementX = 0;
		this.displacementY = 0;
	}

	/**
	 * This method applies the transformation of a compound node (denoted as
	 * root) to all the nodes in its children graph
	 */
	public void propogateDisplacementToChildren(double dX, double dY)
	{
		Iterator nodeIter = this.getChild().getNodes().iterator();

		while (nodeIter.hasNext())
		{
			CoSENode lNode = (CoSENode) nodeIter.next();

			lNode.moveBy(dX, dY);
			lNode.displacementX += dX;
			lNode.displacementY += dY;

			if (lNode.getChild() != null)
			{
				lNode.propogateDisplacementToChildren(dX, dY);
			}
		}

		this.updateBounds();
	}

 	/**
	 * This method migrates this node some place near the input node, used when
	 * growing nodes in reduced trees back into the drawing.
	 */
	protected void migrateTo(LNode node)
	{
		this.rect.x = node.rect.x + 5;
		this.rect.y = node.rect.y + 5;
	}

	/**
	 * Given a pair of projected spring forces, this method propagates them to
	 * parents of this node.
	 */
	public boolean propogateIGEFtoParents(double springForceX,
		double springForceY)
	{
		// Root node does not need this
		if (this.getOwner() == null)
		{
			return false;
		}

		if (((CoSENode)this.getOwner().getParent()).
			propogateIGEFtoParents(springForceX / 2, springForceY / 2))
		{
			this.springForceX += springForceX;
			this.springForceY += springForceY;
		}
		else
		{
			this.springForceX += springForceX / 2;
			this.springForceY += springForceY / 2;
		}

		return true;
	}

// -----------------------------------------------------------------------------
// Section: Class Constants
// -----------------------------------------------------------------------------

	/**
	 * Maximum amount by which a node can be moved per iteration
	 */
	private static final double MAX_DISPLACEMENT = 100;

	/**
	 * Constant used for momentum calculations
	 */
	private static final double MOMENTUM_CONSTANT = 0.75;
}