package org.ivis.layout.avsdf;

import java.util.*;
import java.awt.Point;
import java.awt.Dimension;

import org.ivis.layout.*;

/**
 * This class implements the overall layout process for the AVSDF algorithm
 * (Circular Drawing Algorithm by He and Sykora).
 *
 * @author Esat Belviranli
 * @author Alptug Dilek
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class AVSDFLayout extends Layout
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/**
	 * Separation of the nodes on the circle customizable by the user
	 */
	private int nodeSeparation = AVSDFConstants.DEFAULT_NODE_SEPARATION;

// -----------------------------------------------------------------------------
// Section: Constructors and initializations
// -----------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	public AVSDFLayout()
	{
		super();
	}

	/**
	 * This method creates a new circle associated with the input view object.
	 */
	public LGraph newGraph(Object vObject)
	{
		return new AVSDFCircle(null, this.graphManager, vObject);
	}

	/**
	 * This method creates a new node associated with the input view node.
	 */
	public LNode newNode(Object vNode)
	{
		return new AVSDFNode(this.graphManager, vNode);
	}

	/**
	 * This method creates a new edge associated with the input view edge.
	 */
	public LEdge newEdge(Object vEdge)
	{
		return new AVSDFEdge(null, null, vEdge);
	}

	/**
	 * This method is used to initialize AVSDF layout parameters
	 */
	public void initParameters()
	{
		super.initParameters();

		if (!this.isSubLayout)
		{
			LayoutOptionsPack.AVSDF layoutOptionsPack =
				LayoutOptionsPack.getInstance().getAVSDF();

			this.nodeSeparation = layoutOptionsPack.getNodeSeparation();
		}
	}

// -----------------------------------------------------------------------------
// Section: Accessors
// -----------------------------------------------------------------------------
	/**
	 * This method returns the node seperation value.
	 */
	public int getNodeSeparation()
	{
		return nodeSeparation;
	}

	/**
	 * This method sets the node seperation value.
	 */
	public void setNodeSeparation(int nodeSeparation)
	{
		this.nodeSeparation = nodeSeparation;
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
		// Check if the graph contains any compound structures
		if (this.graphManager.getGraphs().size() > 1)
		{
			return false;
		}

		AVSDFCircle clusterGraph = (AVSDFCircle) this.graphManager.getRoot();

		clusterGraph.setNodeSeperation(this.nodeSeparation);
		clusterGraph.calculateRadius();
		clusterGraph.initOrdering();

		while (!clusterGraph.hasFinishedOrdering())
		{
			AVSDFNode node = clusterGraph.findNodeToPlace();
			clusterGraph.putInOrder(node);
		}

		postProcess(clusterGraph);
		clusterGraph.correctAngles();

		Iterator iter = clusterGraph.getNodes().iterator();

		while (iter.hasNext())
		{
			AVSDFNode node = (AVSDFNode)iter.next();
			node.setCenter(clusterGraph.getCenterX() +
					clusterGraph.getRadius() * Math.cos(node.getAngle()),
				clusterGraph.getCenterY() +
					clusterGraph.getRadius() * Math.sin(node.getAngle()));
		}

		return true;
	}

	/**
	 * This method implements the post processing step of the algorithm, which
	 * tries to minimize the number of edges further with respect to the local
	 * adjusting algorithm described by He and Sykora.
	 */
	public void postProcess(AVSDFCircle circle)
	{
		circle.calculateEdgeCrossingsOfNodes();
		Object[] array = circle.getNodes().toArray();
		AVSDFNodesEdgeCrossingSort sort = new AVSDFNodesEdgeCrossingSort(array);
		sort.quicksort();

		for (int i = array.length - 1; i >= 0; i--)
		{
			AVSDFNode node = (AVSDFNode)(array[i]);
			int currentCrossingNumber = node.getTotalCrossingOfEdges();
			int newCrossingNumber;
			Set neighbors = node.getNeighborsList();
			Iterator iter = neighbors.iterator();

			while (iter.hasNext())
			{
				AVSDFNode tempNode = (AVSDFNode)iter.next();
				int newIndex = (tempNode.getIndex() + 1) % circle.getSize();
				int oldIndex = node.getIndex();

				if (oldIndex != newIndex)
				{
					node.setIndex(newIndex);

					if (oldIndex < node.getIndex())
					{
						oldIndex += circle.getSize();
					}

					for (int k = node.getIndex(); k < oldIndex; k++)
					{
						AVSDFNode temp =
							circle.getOrder()[k % circle.getSize()];
						temp.setIndex((temp.getIndex() + 1) % circle.getSize());
					}

					node.calculateTotalCrossing();
					newCrossingNumber = node.getTotalCrossingOfEdges();

					if (newCrossingNumber >= currentCrossingNumber)
					{
						circle.loadOldIndicesOfNodes();
					}
					else
					{
						circle.reOrderVertices();
						currentCrossingNumber = newCrossingNumber;
					}
				}
			}
		}
	}
}