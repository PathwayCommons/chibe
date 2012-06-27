package org.ivis.layout.avsdf;

import java.util.*;
import java.awt.Dimension;
import java.awt.Point;

import org.ivis.layout.LNode;
import org.ivis.layout.LGraphManager;
import org.ivis.layout.LNodeDegreeSort;

/**
 * This class implements data and functionality required for AVSDF layout per
 * node.
 *
 * @author Esat Belviranli
 * @author Alptug Dilek
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class AVSDFNode extends LNode
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/*
	 * Angle of this node on the owner circle in radians
	 */
	private double angle;

	/*
	 * Index of this node on the owner circle
	 */
	private int circleIndex = -1;

	/*
	 * Total number of crossings of the edges this node is incident to
	 */
	private int totalCrossingOfEdges = -1;

	/*
	 * Whether the current edge crossing nunber is valid or it needs to be
	 * recalculted
	 */
	private boolean isCrossingNumberValid = false;

// -----------------------------------------------------------------------------
// Section: Constructors and initialization
// -----------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	public AVSDFNode(LGraphManager gm, Object vNode)
	{
		super(gm, vNode);
	}

	/**
	 * Alternative constructor
	 */
	public AVSDFNode(LGraphManager gm, Point loc, Dimension size, Object vNode)
	{
		super(gm, loc, size, vNode);
	}

// -----------------------------------------------------------------------------
// Section: Accessor methods
// -----------------------------------------------------------------------------
	/**
	 * This method returns the circle this node is owned by.
	 */
	public AVSDFCircle getCircle()
	{
		return (AVSDFCircle)(this.getOwner());
	}

	/**
	 * This method sets the index of this node on the circle, and sets the
	 * crossing number invalid. Due to the index change of the node; it needs to
	 * be recalculated.
	 */
	public void setIndex(int circleIndex)
	{
		this.circleIndex = circleIndex;
		isCrossingNumberValid = false;
	}

	/**
	 * This method returns the index of this node in the ordering of its owner
	 * circle. Here -1 means that the vertex is not yet placed on its owner
	 * circle.
	 */
	public int getIndex()
	{
		return this.circleIndex;
	}

	/**
	 * This method returns the array of the neigbors of this node sorted in
	 * ascending order.
	 */
	public AVSDFNode[] getNeigborsSortedByDegree()
	{

		Iterator iterator = this.getEdges().iterator();
		ArrayList list = new ArrayList<AVSDFNode>();

		while (iterator.hasNext())
		{
			AVSDFEdge edge = (AVSDFEdge)iterator.next();
			AVSDFNode node = edge.getOtherEnd(this);

			if (node.getIndex() == -1)
			{
				list.add(node);
			}
		}

		Object[] temp = (list.toArray());
		AVSDFNode[] array = new AVSDFNode[temp.length];

		for (int i = 0; i < array.length; i++)
		{
			array[i] = (AVSDFNode)temp[i];
		}

		LNodeDegreeSort sort = new LNodeDegreeSort(array);
		if (array.length > 0)
		{
			sort.quicksort();
		}

		return array;
	}

	/**
	 * This method returns the degree of this node.
	 */
	public int getDegree()
	{
		return this.getEdges().size();
	}

	/**
	 * This method returns whether or not this node is currently placed on its
	 * owner circle.
	 */
	public boolean isOrdered()
	{
		return (this.circleIndex > -1);
	}

	/**
	 * This method sets the angle of this node w.r.t. its owner circle. Here
	 * the angle value is in radian.
	 */
	public void setAngle(double angle)
	{
		this.angle = angle;
	}

	/**
	 * This method returns the angle of this node w.r.t. its owner circle. Here
	 * the angle value is in radian.
	 */
	public double getAngle()
	{
		return this.angle;
	}

	/**
	 * This method returns the index difference of this node with the input
	 * node. Note that the index difference cannot be negative if both nodes are
	 * placed on the circle. Here -1 means at least one of the nodes are not yet
	 * placed on the circle.
	 */
	public int getCircDistWithTheNode(AVSDFNode refNode)
	{
		int otherIndex = refNode.getIndex();

		if (otherIndex == -1 || this.getIndex() == -1)
		{
			return -1;
		}

		int diff = this.getIndex() - otherIndex;

		if (diff < 0)
		{
			diff += this.getCircle().getSize();
		}

		return diff;
	}

	/**
	 * This method finds the number of edge crossings between the edges of
	 * this node and the edges of the input one.
	 */
	public int getCrossingNumberWithNode(AVSDFNode otherNode)
	{
		Iterator iter1 = this.getEdges().iterator();
		int totalCrossing = 0;

		while (iter1.hasNext())
		{
			Iterator iter2 = otherNode.getEdges().iterator();
			AVSDFEdge edge = (AVSDFEdge)iter1.next();

			while (iter2.hasNext())
			{
				AVSDFEdge otherEdge = (AVSDFEdge)iter2.next();
				totalCrossing += edge.crossingWithEdge(otherEdge);
			}
		}
		return totalCrossing;
	}

	/**
	 * This method returns the total number of edge crossings. If the previously
	 * calculated value is not valid due to an index change on the circle, then
	 * a recalculation is performed.
	 */
	public int getTotalCrossingOfEdges()
	{
		if (!isCrossingNumberValid)
		{
			calculateTotalCrossing();
			isCrossingNumberValid = true;
		}

		return totalCrossingOfEdges;
	}

// -----------------------------------------------------------------------------
// Section: Remaining methods
// -----------------------------------------------------------------------------
	/**
	 * This method calculates the total number of crossings the edges of this
	 * node cause.
	 */
	public void calculateTotalCrossing()
	{
		Iterator iter = this.edges.iterator();
		int count = 0;
		ArrayList temp = new ArrayList();

		temp.addAll(getCircle().getEdges());
		temp.removeAll(this.edges);

		while (iter.hasNext())
		{
			AVSDFEdge edge = (AVSDFEdge)iter.next();
			count += edge.calculateTotalCrossingWithList(temp);
		}

		totalCrossingOfEdges = count;
	}
}