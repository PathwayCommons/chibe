package org.gvt.figure;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.gvt.model.CompoundModel;

/**
 * This class is the UI of Compound nodes.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class CompoundFigure extends NodeFigure
{
	int labelHeight;

	public CompoundFigure(Point locationAbs,
		Dimension size,
		String text,
		String toolTipText,
		Font textFont,
		Color textColor,
		Color color,
		Color borderColor,
		Color highlightColor,
		boolean highlight)
	{
		super(locationAbs,
			size,
			text,
			toolTipText,
			textFont,
			textColor,
			color,
			borderColor,
			"Rectangle",
			highlightColor,
			highlight);

		this.labelHeight = CompoundModel.LABEL_HEIGHT;
		this.label.setOpaque(true);
		this.label.setBackgroundColor(color);
		this.label.setBounds(new Rectangle(
			bounds.x,
			bounds.y + bounds.height - labelHeight,
			bounds.width,
			labelHeight));

		add(label);
		setBackgroundColor(ColorConstants.white);
		LineBorder border = new LineBorder(1);
		border.setColor(getForegroundColor());
		label.setBorder(border);		
	}

	public void updateColor(Color color)
	{
		label.setBackgroundColor(color);
	}

	public void updateShape(String s)
	{
		// Do nothing. Compounds have only one shape.
	}

	protected void paintFigure(Graphics graphics)
	{
		label.setBounds(new Rectangle(
			bounds.x,
			bounds.y + bounds.height - labelHeight,
			bounds.width,
			labelHeight));

		Rectangle rect = getBounds().getCopy();
		rect.height--;
		rect.width--;
		graphics.drawRectangle(rect);
	}
}