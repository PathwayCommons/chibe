package org.gvt;

import java.util.ListIterator;

import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.*;
import org.gvt.layout.LayoutOptionsPack;
import org.gvt.layout.AbstractLayout;

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
			AbstractLayout.animationOnLayout)
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
