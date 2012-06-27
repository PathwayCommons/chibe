package org.ivis.layout.cise;

/**
 * This class keeps the information of each inter-cluster edge of the associated
 * circle. It is to be used for sorting inter-cluster edges based on this info.
 *
 * @author Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class CiSEInterClusterEdgeInfo
{
	/**
	 * Inter-cluster edge
	 */
	private CiSEEdge edge;

	/**
	 * Angle in radians (in clockwise direction from the positive x-axis) that
	 * is computed for this inter-cluster edge based on the line segment with
	 * one end as the center of the associated cluster and the other end being
	 * the center of the source/target node of this inter-cluster edge that is
	 * not in this cluster.
	 */
	private double angle;

	public CiSEInterClusterEdgeInfo(CiSEEdge edge, double angle)
	{
		this.edge = edge;
		this.angle = angle;
	}

	public CiSEEdge getEdge()
	{
		return this.edge;
	}

	public double getAngle()
	{
		return this.angle;
	}
}