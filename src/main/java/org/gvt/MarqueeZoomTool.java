package org.gvt;

import org.eclipse.gef.tools.MarqueeSelectionTool;
import org.eclipse.draw2d.geometry.Rectangle;
import org.gvt.editpart.ChsScalableRootEditPart;
import org.gvt.figure.HighlightLayer;

/**
 * A tool which zooms to a rectangular area of a Graphical Viewer.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */

public class MarqueeZoomTool extends MarqueeSelectionTool
{
	public Rectangle getMarqueeSelectionRectangle()
	{
		return new Rectangle(getStartLocation(), getLocation());
	}

	protected boolean handleButtonUp(int button)
	{
		if (button == 1 &&
			!getMarqueeSelectionRectangle().getSize().equals(1, 1))
		{
			if (stateTransition(STATE_DRAG_IN_PROGRESS, STATE_TERMINAL))
			{
				handleInvalidInput();
			}

			handleFinished();

			((ChsScalableRootEditPart) getCurrentViewer().getRootEditPart()).
				getZoomManager().zoomTo(getMarqueeSelectionRectangle());

			// Highlights are refreshed after marquee zoom
			((HighlightLayer) ((ChsScalableRootEditPart)getCurrentViewer().
				getRootEditPart()).getLayer(HighlightLayer.HIGHLIGHT_LAYER)).
					refreshHighlights();

			// Selection Tool is selected
			getDomain().setActiveTool(getDomain().getDefaultTool());

			return true;
		}

		return false;
	}
}
