package org.gvt.figure;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

/**
 * Format for info:
 *
 * tooltip|letter|bg-color|bord-color
 *
 * @author Ozgun Babur
 */
public class RPPASiteFigure extends InfoFigure
{
	protected static final Color FORE = new Color(null, 0 , 0 , 0);

	public RPPASiteFigure(String info, int i, Rectangle parentBounds, Dimension dim)
	{
		super(info, i, parentBounds, dim);
	}

	@Override
	protected String getLetter(String info)
	{
		String[] t = info.split("\\|");
		if (t.length > 1)
		{
			return t[1];
		}
		else
		{
			System.err.println("Invalid format for rppasite = " + info);
			return "";
		}
	}

	@Override
	protected Color getBackColor(String info)
	{
		return getColor(info.split("\\|")[2]);
	}

	@Override
	protected Color getForeColor(String info)
	{
		return FORE;
	}

	@Override
	protected Color getBordColor(String info)
	{
		return getColor(info.split("\\|")[3]);
	}

	protected Color getColor(String s)
	{
		String[] v = s.split(" ");
		return new Color(null,
			Integer.parseInt(v[0]), Integer.parseInt(v[1]), Integer.parseInt(v[2]));
	}

	@Override
	protected String getTooltipText(String info)
	{
		String[] t = info.split("\\|");
		if (t.length > 4) return t[0] + ", " + t[4];
		else return t[0];
	}
}
