package org.patika.mada.util;

/**
 * This class is used for labeling patika objects with microarray data. This
 * object is added to the custom data space of the objects.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class MutationData extends ExperimentData
{
	private static SignificanceFilter sigFilt;

	public MutationData(double value)
	{
		super(value);
	}

	public MutationData(double value1, double value2)
	{
		super(value1, value2);
	}

	public double getMaxValue()
	{
		return MUTATED;
	}

	public double getMinValue()
	{
		return NORMAL;
	}

	protected SignificanceFilter getSignificanceFilter()
	{
		return sigFilt;
	}

	public static void setSignificanceFilter(SignificanceFilter sigFilt)
	{
		MutationData.sigFilt = sigFilt;
	}

	public Object getKey()
	{
		return MUTATION_DATA;
	}

	public final double MUTATED = 2D;
	public final double NORMAL = 0D;

}