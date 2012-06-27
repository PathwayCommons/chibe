package org.gvt.editpolicy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.*;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.gvt.figure.ConnectionPointHandle;
import org.gvt.model.EdgeModel;


/**
 * This class is an editpolicy for handling the selection of edges.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class ChsConnectionEndpointEditPolicy
	extends ConnectionEndpointEditPolicy
{
// -----------------------------------------------------------------------------
// Section: Overriden methods.
// -----------------------------------------------------------------------------
	/**
	 * Overriden method for making the line's color cyan when selected
	 */
	protected void addSelectionHandles()
	{
		super.addSelectionHandles();
		getConnectionFigure().setForegroundColor(ColorConstants.cyan);
	}

	/**
	 * Returns and casts the associated figure for the hosted edit part
	 */
	protected PolylineConnection getConnectionFigure()
	{
		return (PolylineConnection) ((GraphicalEditPart) getHost()).getFigure();
	}

	/**
	 * Overriden method for making the line's color to original color when
	 * deselected
	 */
	protected void removeSelectionHandles()
	{
		super.removeSelectionHandles();
		getConnectionFigure().setForegroundColor(
			((EdgeModel)this.getHost().getModel()).getColor());
	}

	protected List createSelectionHandles()
	{
		List<ConnectionPointHandle> list =
			new ArrayList<ConnectionPointHandle>();
		list.add(new ConnectionPointHandle((ConnectionEditPart) getHost(),
			"END"));
		list.add(new ConnectionPointHandle((ConnectionEditPart) getHost(),
			"START"));
		return list;
	}
}