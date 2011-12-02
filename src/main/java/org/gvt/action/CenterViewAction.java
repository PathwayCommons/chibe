package org.gvt.action;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.Action;
import org.gvt.ChisioMain;
import org.gvt.editpart.ChsScalableRootEditPart;

/**
 * Action for centering the visible rectangle to the given center point.
 * Note that the point might not be centered if it is too close to left or top.
 *
 * @author Esat Belviranli
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007 - present
 */
public class CenterViewAction extends Action
{
	private ChisioMain main;

	private Point centerPoint;

	/**
	 * Constructor
	 *
	 * @param main		  Main application
	 * @param centerPoint The point where the view will be centered to.
	 */
	public CenterViewAction(ChisioMain main, Point centerPoint)
	{
		this.main = main;
		this.centerPoint = centerPoint;
	}

	public void run()
	{
		Rectangle visibleRect = ((ChsScalableRootEditPart) this.main.getViewer().getRootEditPart()).
			getZoomManager().getViewport().getClientArea();

		int viewLocationX = visibleRect.x - (visibleRect.width/2 - this.centerPoint.x);
		int viewLocationY = visibleRect.y - (visibleRect.height/2 - this.centerPoint.y);

		((ChsScalableRootEditPart) this.main.getViewer().getRootEditPart()).
			getZoomManager().setViewLocation(
				new Point(viewLocationX, viewLocationY));
	}
}