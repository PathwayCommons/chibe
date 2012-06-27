package org.ivis.layout.cise;

import java.util.List;

import org.ivis.util.QuickSort;

/**
 * This class is used for sorting on-circle nodes by their indexes.
 * 
 * @author Esat Belviranli
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class CiSENodeSort extends QuickSort
{
	/**
	 * Constructor
	 */
	public CiSENodeSort(List<Object> objectList)
	{
		super(objectList);
	}

	/**
	 * This method is required by QuickSort. In this case, comparison is based
	 * on indexes of onCircleNodes on the circle.
	 */
	public boolean compare(Object a, Object b)
	{
		return ((CiSENode)b).getOnCircleNodeExt().getIndex() >
			((CiSENode)a).getOnCircleNodeExt().getIndex();
	}
}