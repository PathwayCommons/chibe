package org.ivis.layout.avsdf;

import java.util.List;

import org.ivis.util.QuickSort;

/**
 * This class implements sorting with respect to the crossing number of nodes.
 *
 * @author Alptug Dilek
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class AVSDFNodesEdgeCrossingSort extends QuickSort
{
	public AVSDFNodesEdgeCrossingSort(List<Object> objectList)
	{
		super(objectList);
	}

	public AVSDFNodesEdgeCrossingSort(Object[] objectArray)
	{
		super(objectArray);
	}

	// must return true if b is greater than a in terms of comparison criteraia
	public boolean compare(Object a, Object b)
	{
		AVSDFNode node1 = (AVSDFNode)a;
		AVSDFNode node2 = (AVSDFNode)b;

		return
			node2.getTotalCrossingOfEdges() > node1.getTotalCrossingOfEdges();
	}
}