package org.ivis.layout.six;

import java.util.Iterator;

import org.ivis.layout.cise.*;
import org.ivis.layout.*;
import org.ivis.layout.fd.*;

/**
 * This class provides a "rough" implementation of the circular layout algorithm
 * by Six & Tollis (J. M. Six and I. G. Tollis, "A framework for user-grouped
 * circular drawings," in GD '03, ser. LNCS, G. Liotta, Ed., vol. 2912, 2004,
 * pp. 135-146.)
 *
 * @author: Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class SixCircularLayout extends CiSELayout
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
	public SixCircularLayout()
	{
		super();
	}

	/**
	 * This method creates a new graph associated with the input view graph.
	 */
	public LGraph newGraph(Object vGraph)
	{
		return new SixCircularCircle(null, this.graphManager, vGraph);
	}

	/**
	 * This method creates a new node associated with the input view node.
	 */
	public LNode newNode(Object vNode)
	{
		return new SixCircularNode(this.graphManager, vNode);
	}

	/**
	 * This method is used to set CoSE layout parameters that are specific to
	 * Cluster layout.
	 */
	public void initParameters()
	{
		super.initParameters();

		if (!this.isSubLayout)
		{
			this.repulsionConstant *= 3.5;
		}
	}
// -----------------------------------------------------------------------------
// Section: Layout related
// -----------------------------------------------------------------------------
	/**
	 * This method performs layout on constructed l-level graph. It returns true
	 * on success, false otherwise.
	 */
	public boolean layout()
	{
		if (!this.convertToClusteredGraph())
		{
			return false;
		}

		this.graphManager.getRoot().calcEstimatedSize();

		this.doStep1(); // layout individual circles (same as CiSE)
		this.doStep2(); // layout the cluster / quotient graph (same as CiSE)
		this.doStep3();
		this.doStep4();

//		System.out.println("Six circular layout finished after " +
//			this.totalIterations + " iterations");

		return true;
	}

	/**
	 * This method corresponds to step 5 of Algorithm 1 in their paper. Here we
	 * keep the location of each clustered unchanged and rotate each cluster to
	 * optimally orient it.
	 */
	public void doStep3()
	{
//		System.out.println("Six Phase 3 started...");

		// No special order is specified by the algorithm, so we do it in no
		// particular order!
		
		CiSEGraphManager gm = (CiSEGraphManager)this.getGraphManager();
		Iterator iterator = gm.getRoot().getNodes().iterator();
		LNode node;
		SixCircularCircle circle;

		while (iterator.hasNext())
		{
			node = (LNode)iterator.next();

			if (node.getChild() == null)
			{
				continue;
			}

			circle = (SixCircularCircle) node.getChild();
			circle.calculateOptimalOrientation();
		}
	}

	/**
	 * This method corresponds to step 6 of Algorithm 1 in their paper. Here we
	 * run force-directed layout making sure on-circle nodes stay on their
	 * "tracks". Since circle positions are already determined and fixed, no
	 * gravitational forces are considered either.
	 */
	public void doStep4()
	{
//		System.out.println("Six Phase 4 started...");

		this.initSpringEmbedder();
		this.runSpringEmbedder();
	}

	/*
	 * This method implements a spring embedder needed by the last step of this
	 * algorithm.
	 */
	private void runSpringEmbedder()
	{
		this.totalDisplacement = 1000;
		this.totalIterations = 0;

		do
		{
			this.totalIterations++;

			if (this.totalIterations % FDLayoutConstants.CONVERGENCE_CHECK_PERIOD == 0)
			{
				if (this.isConverged())
				{
					break;
				}

				this.coolingFactor = this.initialCoolingFactor *
					((this.maxIterations - this.totalIterations) / (double)this.maxIterations);
			}

			this.totalDisplacement = 0;

			this.calcSpringForces();
			this.calcRepulsionForces();
			this.moveNodes();

			this.animate();
		}
		while (this.totalIterations < this.maxIterations);
	}

	/**
	 * This method calculates the spring forces for the ends of each node.
	 */
	public void calcSpringForces()
	{
		Object[] lEdges = this.getAllEdges();
		CiSEEdge edge;

		for (int i = 0; i < lEdges.length; i++)
		{
			edge = (CiSEEdge) lEdges[i];

			this.calcSpringForce(edge, edge.idealLength);
		}
	}

	/**
	 * This method calculates the repulsion forces for each pair of nodes.
	 */
	public void calcRepulsionForces()
	{
		int i, j;
		SixCircularNode nodeA, nodeB;
		Object[] lNodes = this.getOnCircleNodes();

		for (i = 0; i < lNodes.length; i++)
		{
			nodeA = (SixCircularNode) lNodes[i];

			for (j = i + 1; j < lNodes.length; j++)
			{
				nodeB = (SixCircularNode) lNodes[j];

				assert nodeA.getOnCircleNodeExt() != null;
				assert nodeB.getOnCircleNodeExt() != null;

				// If both nodes are not members of the same graph, skip.
				if (nodeA.getOwner() != nodeB.getOwner())
				{
					continue;
				}

				this.calcRepulsionForce(nodeA, nodeB);
			}
		}

		// We should not have any in-nodes!
		assert this.getInCircleNodes().length == 0;
	}

	/**
	 * This method updates positions of each node at the end of an iteration.
	 */
	public void moveNodes()
	{
		Object[] lNodes = this.getOnCircleNodes();
		SixCircularNode node;

		for (int i = 0; i < lNodes.length; i++)
		{
			node = (SixCircularNode) lNodes[i];
			node.move();
		}
	}
}