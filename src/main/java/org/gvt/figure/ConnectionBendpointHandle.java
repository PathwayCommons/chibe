package org.gvt.figure;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.*;
import org.eclipse.gef.handles.BendpointHandle;
import org.eclipse.gef.tools.ConnectionBendpointTracker;

/**
 * A BendpointHandle that is used to create a new bendpoint.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class ConnectionBendpointHandle
	extends BendpointHandle
{
	// type of the bendpoint, "create" or "move"
	String type;
	{
		setCursor(SharedCursors.SIZEALL);
	}

	/**
	 * Creates a new ConnectionBendpointHandle, sets its owner and its index,
	 * and sets its locator to a new {@link MidpointLocator} with the given
	 * locatorIndex.
	 *
	 * @param owner		the ConnectionEditPart owner
	 * @param index		the index
	 * @param locatorIndex the locator index
	 */
	public ConnectionBendpointHandle(ConnectionEditPart owner,
		int index,
		int locatorIndex,
		String type)
	{
		setOwner(owner);
		setIndex(index);

		if (type.equals("create"))
		{
			setLocator(new MidpointLocator(getConnection(), locatorIndex));
			setPreferredSize(new Dimension(DEFAULT_HANDLE_SIZE - 2,
				DEFAULT_HANDLE_SIZE - 2));

		}
		else if (type.equals("move"))
		{
			setLocator(new BendpointLocator(getConnection(), locatorIndex));
		}

		this.type = type;
	}

	/**
	 * Creates and returns a new ConnectionBendpointTracker.
	 */
	protected DragTracker createDragTracker()
	{
		ConnectionBendpointTracker tracker;
		tracker = new ConnectionBendpointTracker(
			(ConnectionEditPart) getOwner(),
			getIndex());

		if (type.equals("create"))
		{
			tracker.setType(RequestConstants.REQ_CREATE_BENDPOINT);
		}
		else if (type.equals("move"))
		{
			tracker.setType(RequestConstants.REQ_MOVE_BENDPOINT);
		}
		tracker.setDefaultCursor(getCursor());
		return tracker;

	}

	/**
	 * draws the bendpoint handles with blue color
	 * @param g
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
		{ 	//We don't really own rect 'r', so fix it.
			r.expand(1, 1);
		}
	}
}