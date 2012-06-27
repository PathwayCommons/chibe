package org.ivis.layout.cise;

import org.ivis.util.QuickSort;

/**
 * This class sorts the array of input edges based on the associated angles. If
 * angles turn out to be the same, then we sort the edges based on their
 * in-cluster end nodes' orders in clockwise direction. This information is
 * calculated beforehand and stored in a matrix in each associated circle.
 *
 * @author Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class CiSEInterClusterEdgeSort extends QuickSort
{
	/**
	 * Owner circle whose inter-cluster edges are to be sorted
	 */
	private CiSECircle ownerCircle;

	/**
	 * Constructor
	 */
	public CiSEInterClusterEdgeSort(CiSECircle ownerCircle, Object[] objectArray)
	{
		super(objectArray);
		this.ownerCircle = ownerCircle;
	}

	// must return true if b is greater than a in terms of comparison criteria
	public boolean compare(Object a, Object b)
	{
		CiSEInterClusterEdgeInfo edgeInfoA = (CiSEInterClusterEdgeInfo) a;
		CiSEInterClusterEdgeInfo edgeInfoB = (CiSEInterClusterEdgeInfo) b;

		if (edgeInfoB.getAngle() > edgeInfoA.getAngle())
		{
			return true;
		}
		else if (edgeInfoB.getAngle() == edgeInfoA.getAngle())
		{
			if (edgeInfoA == edgeInfoB)
			{
				return false;
			}
			else
			{
				return this.ownerCircle.getOrder(
					this.ownerCircle.getThisEnd(edgeInfoA.getEdge()),
					this.ownerCircle.getThisEnd(edgeInfoB.getEdge()));
			}
		}
		else
		{
			return false;
		}
	}
}