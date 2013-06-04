package org.gvt.figure;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.swt.graphics.Color;
import org.gvt.model.CompoundModel;

/**
 * This class draws the border of Compound nodes. Also drawing the bottom part
 * which is colorful and includes the label of compound node is implemented
 * in this class.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class CompoundBorder extends LineBorder
{
	protected int labelHeight;
    protected Dimension labelSize;
	Color color;

	public CompoundBorder(Color c, int lblHeight)
	{
		color = c;
		labelHeight = lblHeight;
	}

	public CompoundBorder()
	{
		labelHeight = CompoundModel.LABEL_HEIGHT;
	}

	public void setColor(Color c)
	{
		color = c;
	}

	public CompoundBorder(int width)
	{
		setGrabBarWidth(width);
		labelSize = new Dimension(width, 4);
	}

	public Insets getInsets(IFigure figure)
	{
		return new Insets(getWidth() + 2,
			labelHeight + 2,
			getWidth() + 2,
			getWidth() + 2);
	}

	public Dimension getPreferredSize()
	{
		return labelSize;
	}

	public void paint(IFigure figure, Graphics graphics, Insets insets)
	{

		super.paint(figure, graphics, insets);
	}

	public void setGrabBarWidth(int width)
	{
		labelHeight = width;
	}
}