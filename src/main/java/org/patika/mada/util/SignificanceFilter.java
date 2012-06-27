package org.patika.mada.util;

/**
 * This class is a user defined criteria about the significance of the experiment data.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public interface SignificanceFilter
{
	public boolean isSignificant(ExperimentData data);
}
