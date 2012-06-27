package org.ivis.layout.cise;

import org.ivis.layout.LGraphManager;
import org.ivis.layout.Layout;

/**
 * This class implements a graph-manager for CiSE layout specific data and
 * functionality.
 *
 * @author Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class CiSEGraphManager extends LGraphManager
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/**
	 * All on-circle and other nodes (unclustered nodes and nodes representing
	 * each cluster/circle) in this graph manager. For efficiency purposes we
	 * hold references of these nodes that we operate on in arrays.
	 */
	private CiSENode[] onCircleNodes;
	private CiSENode[] nonOnCircleNodes;
	private CiSENode[] inCircleNodes;
	
// -----------------------------------------------------------------------------
// Section: Constructors and initialization
// -----------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	public CiSEGraphManager(Layout layout)
	{
		super(layout);
		this.onCircleNodes = null;
		this.inCircleNodes = null;
		this.nonOnCircleNodes = null;
	}

// -----------------------------------------------------------------------------
// Section: Accessors
// -----------------------------------------------------------------------------
	/**
	 * This method returns an array of all on-circle nodes.
	 */
	public CiSENode[] getOnCircleNodes()
	{
		assert this.onCircleNodes != null;

		return this.onCircleNodes;
	}

	/**
	 * This method returns an array of all in-circle nodes.
	 */
	public CiSENode[] getInCircleNodes()
	{
		assert this.inCircleNodes != null;

		return this.inCircleNodes;
	}

	/**
	 * This method returns an array of all nodes other than on-circle nodes.
	 */
	public CiSENode[] getNonOnCircleNodes()
	{
		assert this.nonOnCircleNodes != null;

		return this.nonOnCircleNodes;
	}

	/**
	 * This method sets the array of all on-circle nodes.
	 */
	public void setOnCircleNodes(CiSENode[] nodes)
	{
		this.onCircleNodes = nodes;
	}

	/**
	 * This method sets the array of all in-circle nodes.
	 */
	public void setInCircleNodes(CiSENode[] nodes)
	{
		this.inCircleNodes = nodes;
	}

	/**
	 * This method sets the array of all nodes other than on-circle nodes.
	 */
	public void setNonOnCircleNodes(CiSENode[] nodes)
	{
		this.nonOnCircleNodes = nodes;
	}
}