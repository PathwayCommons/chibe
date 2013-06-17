package org.gvt.editpolicy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Bendpoint;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.BendpointEditPolicy;
import org.eclipse.gef.requests.BendpointRequest;
import org.gvt.command.*;
import org.gvt.figure.ConnectionBendpointHandle;
import org.gvt.model.EdgeModel;

/**
 * This class is an editpolicy for Bendpoint support of edges in a graph.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class ChsBendpointEditPolicy extends BendpointEditPolicy
{
	protected List createSelectionHandles()
	{
		List<ConnectionBendpointHandle> list = new ArrayList<ConnectionBendpointHandle>();
		ConnectionEditPart connEP = (ConnectionEditPart) getHost();
		PointList points = getConnection().getPoints();
		List<Bendpoint> bendPoints = (List<Bendpoint>) getConnection().getRoutingConstraint();
		int bendPointIndex = 0;
		Point currBendPoint = null;

		if (bendPoints == null)
		{
			bendPoints = new ArrayList<Bendpoint>();
		}
		else if (!bendPoints.isEmpty())
		{
			currBendPoint = (bendPoints.get(0)).getLocation();
		}

		for (int i = 0; i < points.size() - 1; i++)
		{
			//Put a create handle on the middle of every segment
			list.add(new ConnectionBendpointHandle(connEP,
				bendPointIndex,
				i,
				"create"));

			//If the current user bendpoint matches a bend location, show a move handle
			if (i < points.size() - 1
				&& bendPointIndex < bendPoints.size()
				&& currBendPoint.equals(points.getPoint(i + 1)))
			{
				list.add(new ConnectionBendpointHandle(connEP,
					bendPointIndex,
					i + 1,
					"move"));

				//Go to the next user bendpoint
				bendPointIndex++;
				if (bendPointIndex < bendPoints.size())
				{
					currBendPoint = (bendPoints.get(bendPointIndex))
						.getLocation();
				}
			}
		}
		return list;
	}

	protected Command getCreateBendpointCommand(BendpointRequest request)
	{
		CreateBendpointCommand com = new CreateBendpointCommand();
		Point p = request.getLocation();
		Connection conn = getConnection();

		conn.translateToRelative(p);

		com.setLocation(p);
		Point ref1 = getConnection().getSourceAnchor().getReferencePoint();
		Point ref2 = getConnection().getTargetAnchor().getReferencePoint();

		conn.translateToRelative(ref1);
		conn.translateToRelative(ref2);


		com.setRelativeDimensions(p.getDifference(ref1),
			p.getDifference(ref2));
		com.setWire((EdgeModel) request.getSource().getModel());
		com.setIndex(request.getIndex());
		return com;
	}

	protected Command getMoveBendpointCommand(BendpointRequest request)
	{
		MoveBendpointCommand com = new MoveBendpointCommand();
		Point p = request.getLocation();
		Connection conn = getConnection();

		conn.translateToRelative(p);

		com.setLocation(p);

		Point ref1 = getConnection().getSourceAnchor().getReferencePoint();
		Point ref2 = getConnection().getTargetAnchor().getReferencePoint();

		conn.translateToRelative(ref1);
		conn.translateToRelative(ref2);

		com.setRelativeDimensions(p.getDifference(ref1),
			p.getDifference(ref2));
		com.setWire((EdgeModel) request.getSource().getModel());
		com.setIndex(request.getIndex());
		return com;
	}

	protected Command getDeleteBendpointCommand(BendpointRequest request)
	{
		BendpointCommand com = new DeleteBendpointCommand();
		Point p = request.getLocation();
		com.setLocation(p);
		com.setWire((EdgeModel) request.getSource().getModel());
		com.setIndex(request.getIndex());
		return com;
	}
}