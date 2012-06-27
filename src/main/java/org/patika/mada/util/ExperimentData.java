package org.patika.mada.util;

import org.eclipse.swt.graphics.Color;
import org.gvt.util.Conf;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

/**
 * This class is used for labeling patika objects with microarray data. This
 * object is added to the custom data space of the objects.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public abstract class ExperimentData implements Representable
{
	private double value1;

	/**
	 * Used if this info is used to label with two microarray data, i.e.
	 * comparison.
	 */
	private double value2;

	/**
	 * Specifies whether a single value or a comparison is stored.
	 */
	private boolean type;

	/**
	 * If detected as present in experiment group 1
	 */
	private int presInExp1;

	/**
	 * If detected as present in experiment group 2
	 */
	private int presInExp2;

	/**
	 * Constructor with one value.
	 * @param value
	 */
	public ExperimentData(double value)
	{
		this.value1 = value;
		this.presInExp1 = UNKNOWN;
		this.type = SINGLE;
	}

	/**
	 * Constructor with two values.
	 * @param value1
	 * @param value2
	 */
	public ExperimentData(double value1, double value2)
	{
		this.value1 = value1;
		this.value2 = value2;
		this.presInExp1 = UNKNOWN;
		this.presInExp2 = UNKNOWN;
		this.type = DOUBLE;
	}

	public abstract double getMaxValue();
	public abstract double getMinValue();
	protected abstract SignificanceFilter getSignificanceFilter();

	public double getValue()
	{
		if (this.isSingle())
		{
			return this.getValue1();
		}
		else
		{
			if (getMinValue() < 0)
			{
				if (getValue1() > getValue2())
					return -(this.getValue1() - getMinValue()) /
						(this.getValue2() - getMinValue());
				else
					return (this.getValue2() - getMinValue()) /
						(this.getValue1() - getMinValue());
			}
			else
			{
				if (getValue1() > getValue2())
					return -(this.getValue1() / this.getValue2());
				else
					return this.getValue2() / this.getValue1();
			}
		}
	}

	public double getValue1()
	{
		return value1;
	}

	public void setValue1(double value1)
	{
		this.value1 = value1;
	}

	public double getValue2()
	{
		return value2;
	}

	public void setValue2(double value2)
	{
		this.value2 = value2;
	}

	public boolean isSingle()
	{
		return type == SINGLE;
	}

	public boolean isDouble()
	{
		return type == DOUBLE;
	}

	public void setType(boolean type)
	{
		this.type = type;
	}

	public boolean isSignificant()
	{
		return this.getSignificanceFilter().isSignificant(this);
//		return Math.log(Math.abs(this.value1 - this.value2)) / Math.log(getMaxValue() - getMinValue()) > 0.7;
	}

	public boolean isUpRegulated()
	{
		return value2 - value1 > 0;
	}

	public boolean isDownRegulated()
	{
		return value2 - value1 < 0;
	}

	public int getSign()
	{
		return (this.isUpRegulated()) ? UPREGULATION : DOWNREGULATION;
	}

	public boolean alterNodeColor()
	{
		return true;
	}

	public boolean alterToolTipText()
	{
		return true;
	}

	public boolean alterTextColor()
	{
		return true;
	}

	public Color getNodeColor()
	{
		double v = this.getValue();

 		if (v >= high)
		{
			return highC;
		}
		else if (v > mid_h)
		{
			return new Color (null,
				getValueByRatio(v, mid_h, high, midC.getRed(), highC.getRed()),
				getValueByRatio(v, mid_h, high, midC.getGreen(), highC.getGreen()),
				getValueByRatio(v, mid_h, high, midC.getBlue(), highC.getBlue()));
		}
		else if (v <= mid_h && v >= mid_l)
		{
			return midC;
		}
		else if (v < mid_l && v > low)
		{
			return new Color (null,
				getValueByRatio(v, low, mid_l, lowC.getRed(), midC.getRed()),
				getValueByRatio(v, low, mid_l, lowC.getGreen(), midC.getGreen()),
				getValueByRatio(v, low, mid_l, lowC.getBlue(), midC.getBlue()));
		}
		else
		{
			return lowC;
		}		
	}

	private int getValueByRatio(double num,
		double numLow, double numHigh, int colorNum1, int colorNum2)
	{
		return (int)
			((((num - numLow) / (numHigh - numLow)) * (colorNum2 - colorNum1)) + colorNum1);
	}

	static final DecimalFormat fmt = new DecimalFormat("0.##");

	public String getToolTipText()
	{
		return fmt.format(getValue());
	}

	public Color getTextColor()
	{
		return DEFAULT_TEXT_COLOR;
	}

	/**
	 * Key for storing this type of experiment data in the experiment hashmaps of nodes.
	 * @return key for hashmap
	 */
	public abstract Object getKey();

	public static final int UPREGULATION = 1;
	public static final int DOWNREGULATION = -1;

	public static final boolean SINGLE = false;
	public static final boolean DOUBLE = true;

	public static final double LOG2 = Math.log(2);

	// Experiment data types for using as constant somewhere

	public static List<String> getDataTypes()
	{
		return Arrays.asList(
            ALTERATION_DATA,
			EXPRESSION_DATA,
			MASS_SPEC_DATA, 
			COPY_NUMBER_VARIATION,
			MUTATION_DATA);
	}

	// Experiment data type constants

	public static final String EXPRESSION_DATA = "Expression Data";
	public static final String MASS_SPEC_DATA = "Mass Spectrometry Data";
	public static final String COPY_NUMBER_VARIATION = "Copy Number Variation Data";
	public static final String MUTATION_DATA = "Mutation Data";
    public static final String ALTERATION_DATA = "Alteration Data";

	// Presence - absence constants

	public static final int PRESENT = 1;
	public static final int ABSENT = -1;
	public static final int UNKNOWN = 0;

	private static final Color highC = Conf.getColor(Conf.EXPERIMENT_UP_COLOR);
	private static final Color midC = Conf.getColor(Conf.EXPERIMENT_MIDDLE_COLOR);
	private static final Color lowC = Conf.getColor(Conf.EXPERIMENT_DOWN_COLOR);
	
	/*	UK: Modified high, mid and low to fit better for proteomics use, where regulation values are often represented as 
	 * 	fold change in the range (-inf, -1] U [1, inf)	*/
	private static final double high = Conf.getNumber(Conf.EXPERIMENT_MAX_UPREGULATION);
	private static final double mid_h = Conf.getNumber(Conf.EXPERIMENT_NO_CHANGE_UPPER_BOUND);
	private static final double mid_l = Conf.getNumber(Conf.EXPERIMENT_NO_CHANGE_LOWER_BOUND);
	private static final double low = Conf.getNumber(Conf.EXPERIMENT_MAX_DOWNREGULATION);

	private static final Color DEFAULT_TEXT_COLOR = new Color(null, 0, 0, 0);
}
