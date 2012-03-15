package org.gvt.layout;

/**
 * This class implements CoSE specific data and functionality for graphs.
 *
 * @author Erhan Giral
 * @author Ugur Dogrusoz
 * @author Cihan Kucukkececi
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class CoSEGraph extends LGraph
{
// -----------------------------------------------------------------------------
// Section: Constructors and Initialization
// -----------------------------------------------------------------------------
	public CoSEGraph(CoSENode parent, LGraphManager graphMgr)
	{
		super(parent, graphMgr);
	}

// -----------------------------------------------------------------------------
// Section: Class Methods
// -----------------------------------------------------------------------------
	/**
	 * This method sets the margins of l-level graphs to be applied on the
	 * bounding rectangle of its contents. For CoSE layout we need to adjust the
	 * ideal edge length modifier as well!
	 */
	public static void setGraphMargin(int margin)
	{
		LGraph.setGraphMargin(margin);

		// we loosen up the factor since ideal edge length doesn't seem to scale
		// up properly (i.e. when you increase the iel to be twice as much, it
		// ends up to be three times the original).
		CoSELayout.perLevelIdealEdgeLengthFactor =
			CoSELayout.DEFAULT_PER_LEVEL_IDEAL_EDGE_LENGTH_FACTOR +
			(double)LGraph.graphMargin / CoSELayout.DEFAULT_EDGE_LENGTH / 1.5;
	}
}