package org.gvt;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.ivis.layout.LayoutOptionsPack;

/**
 * This class extends the XYLayout class to implement XY Layout algorithm to
 * enable the animation ability in Chisio.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */

public class ChsXYLayout extends XYLayout
{
	public void layout(IFigure container)
	{
		if(!LayoutOptionsPack.getInstance().getGeneral().
			isAnimationDuringLayout() ||
			LayoutOptionsPack.getInstance().getGeneral().
				isAnimationOnLayout())
		{
			GraphAnimation.recordInitialState(container);
			if (GraphAnimation.playbackState(container))
				return;
		}
		super.layout(container);
	}

	protected Dimension calculatePreferredSize(IFigure f, int wHint, int hHint)
	{
		Dimension dim = super.calculatePreferredSize(f, wHint, hHint);
		// Size is tripled to fix the cropping in bendpoints and node moves
		return dim.scale(3);
	}
}
