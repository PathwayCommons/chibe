package org.gvt.figure;

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

import java.util.List;

/**
 * @author Ozgun Babur
 */
public class RPPANodeFigure extends RoundRectWithInfo
{
	public RPPANodeFigure(Rectangle bounds, List<String> infos, Label label, int borderWidth)
	{
		super(bounds, null, label, false, borderWidth);
		this.infos = infos;

		bounds = bounds.getCopy();
		bounds.x += 2;
		bounds.width -= 4;

		for (int i = 0; i < infos.size(); i++)
		{
			String info = infos.get(i);
			InfoFigure fig = new RPPASiteFigure(info, i, bounds, new Dimension(SPAN, SPAN));
			this.add(fig);
		}
	}
}
