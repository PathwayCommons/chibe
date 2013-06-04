/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.gvt.figure;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.handles.RelativeHandleLocator;
import org.eclipse.gef.handles.SquareHandle;
import org.eclipse.gef.tools.ResizeTracker;
import org.eclipse.swt.graphics.Cursor;

/**
 * A Handle used to resize a GraphicalEditPart.
 */
public class ResizeHandle extends SquareHandle
{
	private int cursorDirection = 0;
	private boolean isResizable;

	/**
	 * Creates a new ResizeHandle for the given GraphicalEditPart.
	 * <code>direction</code> is the relative direction from the
	 * center of the owner figure.  For example, <code>SOUTH_EAST</code>
	 * would place the handle in the lower-right corner of its
	 * owner figure.  These direction constants can be found in
	 * {@link org.eclipse.draw2d.PositionConstants}.
	 *
	 * @param owner	 owner of the ResizeHandle
	 * @param direction relative direction from the center of the owner figure
	 */
	public ResizeHandle(GraphicalEditPart owner,
		int direction,
		boolean isResizable)
	{
		setOwner(owner);
		setLocator(new RelativeHandleLocator(owner.getFigure(), direction));
		setCursor(Cursors.getDirectionalCursor(direction,
			owner.getFigure().isMirrored()));
		cursorDirection = direction;
		this.isResizable = isResizable;
	}

	/**
	 * Creates a new ResizeHandle for the given GraphicalEditPart.
	 *
	 * @see org.eclipse.gef.handles.SquareHandle#SquareHandle(org.eclipse.gef.GraphicalEditPart, org.eclipse.draw2d.Locator, org.eclipse.swt.graphics.Cursor)
	 */
	public ResizeHandle(GraphicalEditPart owner, Locator loc, Cursor c)
	{
		super(owner, loc, c);
	}

	/**
	 * Returns <code>null</code> for the DragTracker.
	 *
	 * @return returns <code>null</code>
	 */
	protected DragTracker createDragTracker()
	{
		return new ResizeTracker(getOwner(), cursorDirection);
	}

	public void paintFigure(Graphics g)
	{
		Rectangle r = getBounds();
		r.shrink(1, 1);

		try
		{
			if(isResizable)
				g.setBackgroundColor(ColorConstants.cyan);
			else
			    g.setBackgroundColor(ColorConstants.white);
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