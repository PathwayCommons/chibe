package org.gvt.figure;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import java.util.List;

/**
 * @author Ozgun Babur
*/
public class RoundRectWithInfo extends Figure
{
	/**
	 * List of information two show.
 	 */
	List<String> infos;
	boolean multimer;

	public RoundRectWithInfo(Rectangle bounds, List<String> infos, Label label, boolean multimer)
	{
		setBounds(bounds);
		this.infos = infos;
		this.multimer = multimer;

		this.add(label);

		if (infos != null)
		{
			bounds = bounds.getCopy();
			bounds.x += 2;
			bounds.width -= 4;

			for (int i = 0; i < infos.size(); i++)
			{
				String info = infos.get(i);
				InfoFigure fig = new InfoFigure(info, i, bounds, new Dimension(SPAN, SPAN));
				this.add(fig);
			}
		}
	}

	protected void paintFigure(Graphics g)
	{
		g.setAntialias(SWT.ON);
		Rectangle bounds = this.getParent().getBounds().getCopy();
		setBounds(bounds);

		Rectangle rect = bounds.getCopy();
		rect.y += SPAN/2;
		rect.height -= SPAN;

		int rounding = 10;

		if (multimer)
		{
			int shift = 2;
			rect.height -= shift;
			rect.width -= shift;
			rect.x += shift;
			rect.y += shift;
			g.fillRoundRectangle(rect, rounding, rounding);
			rect.height--;
			rect.width--;
			g.drawRoundRectangle(rect, rounding, rounding);
			rect.height++;
			rect.width++;
			rect.x -= shift;
			rect.y -= shift;
			g.fillRoundRectangle(rect, rounding, rounding);
			rect.height--;
			rect.width--;
			g.drawRoundRectangle(rect, rounding, rounding);
		}
		else
		{
			g.fillRoundRectangle(rect, rounding, rounding);
			rect.height--;
			rect.width--;
			g.drawRoundRectangle(rect, rounding, rounding);
		}
	}

	protected static final int SPAN = 12;
}
