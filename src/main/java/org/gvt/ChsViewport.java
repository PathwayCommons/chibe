package org.gvt;

import org.eclipse.draw2d.Viewport;

/**
 * A Viewport is a flexible window onto a {@link org.eclipse.draw2d.ScrollPane}
 * and represents the visible portion of the ScrollPane.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class ChsViewport extends Viewport
{
	/**
	 * Constructs a new Viewport.  If <i>setting</i> is <code>true</code>,
	 * the viewport will use graphics translation to paint.
	 *
	 * @param setting whether to use graphics translation
	 */
	public ChsViewport(boolean setting)
	{
		super(setting);
	}

	/**
	 * Sets extents of {@link org.eclipse.draw2d.RangeModel RangeModels} to the
	 * client area of this Viewport. Sets RangeModel minimums to zero. Sets
	 * RangeModel maximums to this Viewport's height/width.
	 */
	protected void readjustScrollBars()
	{
		if (getContents() == null)
			return;
		// I have extended the scrollbars to handle marquee zoom in borders.
		int height = getContents().getBounds().height * 3 / 2;
		int width = getContents().getBounds().width * 3 / 2;

		getVerticalRangeModel().setAll(0, getClientArea().height, height);
		getHorizontalRangeModel().setAll(0, getClientArea().width, width);
	}
}
