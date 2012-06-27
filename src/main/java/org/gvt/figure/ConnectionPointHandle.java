package org.gvt.figure;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.*;
import org.eclipse.gef.handles.ConnectionHandle;
import org.eclipse.gef.tools.ConnectionEndpointTracker;

/**
 * This class maintains selevtion handles for edges.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class ConnectionPointHandle extends ConnectionHandle
{
	String point;

	/**
	 * Constructor
	 */
	public ConnectionPointHandle(ConnectionEditPart owner, String point)
	{
		setOwner(owner);
		this.point = point;

		if (point.equals("END"))
		{
			setLocator(new ConnectionLocator(getConnection(),
				ConnectionLocator.TARGET));
		}
		else if (point.equals("START"))
		{
			setLocator(new ConnectionLocator(getConnection(),
				ConnectionLocator.SOURCE));
		}
	}

	protected DragTracker createDragTracker()
	{
		if (isFixed())
		{
			return null;
		}

		ConnectionEndpointTracker tracker;
		tracker =
			new ConnectionEndpointTracker((ConnectionEditPart) getOwner());

		if (point.equals("END"))
		{
			tracker.setCommandName(RequestConstants.REQ_RECONNECT_TARGET);
		}
		else if (point.equals("START"))
		{
			tracker.setCommandName(RequestConstants.REQ_RECONNECT_SOURCE);
		}

		tracker.setDefaultCursor(getCursor());

		return tracker;
	}

	/**
	 * Draws the selection points with Blue Color
	 */
	public void paintFigure(Graphics g)
	{
		Rectangle r = getBounds();
		r.shrink(1, 1);

		try
		{
			g.setBackgroundColor(ColorConstants.cyan);
			g.fillRectangle(r.x, r.y, r.width, r.height);
			g.setForegroundColor(ColorConstants.cyan);
			g.drawRectangle(r.x, r.y, r.width, r.height);
		}
		finally
		{
			//We don't really own rect 'r', so fix it.
			r.expand(1, 1);
		}
	}
}