package org.ivis.layout.cluster;

import org.ivis.layout.fd.FDLayoutConstants;
import org.ivis.layout.cose.CoSEConstants;

/**
 * This class maintains the constants used by Cluster layout.
 *
 * @author: Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class ClusterConstants extends CoSEConstants
{
// -----------------------------------------------------------------------------
// Section: Cluster layout user options
// -----------------------------------------------------------------------------
	/*
	 * Default cluster gravity
	 */
	public static final double DEFAULT_COMPOUND_GRAVITY_STRENGTH = 6.0 *
		FDLayoutConstants.DEFAULT_COMPOUND_GRAVITY_STRENGTH;

	/**
	 * Default margins of the dummy compounds corresponding to clusters;
	 * determines how much the clusters should be separated
	 */
	public static final int DEFAULT_CLUSTER_SEPARATION = 5;

// -----------------------------------------------------------------------------
// Section: Cluster layout remaining contants
// -----------------------------------------------------------------------------
}