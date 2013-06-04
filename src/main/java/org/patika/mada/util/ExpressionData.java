package org.patika.mada.util;

/**
 * This class is used for labeling patika objects with microarray data. This
 * object is added to the custom data space of the objects.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class ExpressionData extends ExperimentData
{
	public static double minValue;
	public static double maxValue;

	private static SignificanceFilter sigFilt;


	public ExpressionData(double value)
	{
		super(value);
	}

	public ExpressionData(double value1, double value2)
	{
		super(value1, value2);
	}

	public double getMaxValue()
	{
		return maxValue;
	}

	public double getMinValue()
	{
		return minValue;
	}
	
	protected SignificanceFilter getSignificanceFilter()
	{
		return sigFilt;
	}

	public static void setSignificanceFilter(SignificanceFilter sigFilt)
	{
		ExpressionData.sigFilt = sigFilt;
	}

	public Object getKey()
	{
		return EXPRESSION_DATA;
	}

	static
	{
		sigFilt = new SignificanceFilter()
		{
			public boolean isSignificant(ExperimentData data)
			{
				// This is a temporary code. Significance check should be made properly.
				return data.getValue() >= 0.1 || data.getValue() <= -0.1;
			}
		};
	}
}