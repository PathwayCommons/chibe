package org.gvt.figure;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.*;
import org.gvt.util.ChsGeometry;

/**
 * The ChopboxAnchor's location is found by calculating the intersection of a
 * line drawn from the center point of its owner's box to a reference point on
 * that box. Thus using the ChopBoxAnchor will be oriented such that they point
 * to their owner's center.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class ChsChopboxAnchor extends AbstractConnectionAnchor
{
	/**
	 * Constructs a new ChopboxAnchor.
	 */
	protected ChsChopboxAnchor()
	{
	}

	/**
	 * Constructs a ChopboxAnchor with the given <i>owner</i> figure.
	 *
	 * @param owner The owner figure
	 * @since 2.0
	 */
	public ChsChopboxAnchor(IFigure owner)
	{
		super(owner);
	}

	/**
	 * Gets a Rectangle from and returns the Point where a line from the
	 * center of the Rectangle to the Point <i>reference</i> intersects the 
	 * Rectangle.
	 *
	 * @param reference The reference point
	 * @return The anchor location
	 */
	public Point getLocation(Point reference)
	{
		if (!(getOwner() instanceof CompoundFigure))
		{
			if (((NodeFigure) getOwner()).shape.equals("Ellipse"))
			{
				return getLocationEllipse(reference);
			}
			else if (((NodeFigure) getOwner()).shape.equals("Triangle"))
			{
				return getLocationTriangle(reference);
			}
		}
		
		Rectangle r = Rectangle.SINGLETON;
		r.setBounds(getOwner().getBounds());
		r.translate(-1, -1);
		r.resize(1, 1);

		getOwner().translateToAbsolute(r);
		float centerX = r.x + 0.5f * r.width;
		float centerY = r.y + 0.5f * r.height;

		if (r.isEmpty() ||
			(reference.x == (int) centerX && reference.y == (int) centerY))
		{
			return new Point((int) centerX,
				(int) centerY);  //This avoids divide-by-zero
		}

		float dx = reference.x - centerX;
		float dy = reference.y - centerY;

		// r.width, r.height, dx, and dy are guaranteed to be non-zero.
		float scale = 0.5f /
			Math.max(Math.abs(dx) / r.width, Math.abs(dy) / r.height);

		dx *= scale;
		dy *= scale;
		centerX += dx;
		centerY += dy;

		return new Point(Math.round(centerX), Math.round(centerY));
	}

	public Point getLocationEllipse(Point reference)
	{
		Rectangle r = Rectangle.SINGLETON;
		r.setBounds(getOwner().getBounds());
		r.translate(-1, -1);
		r.resize(1, 1);
		getOwner().translateToAbsolute(r);

		Point ref = r.getCenter().negate().translate(reference);

		if (ref.x == 0)
		{
			return new Point(reference.x, (ref.y > 0) ? r.bottom() : r.y);
		}

		if (ref.y == 0)
		{
			return new Point((ref.x > 0) ? r.right() : r.x, reference.y);
		}

		float dx = (ref.x > 0) ? 0.5f : -0.5f;
		float dy = (ref.y > 0) ? 0.5f : -0.5f;

		float k = (float) (ref.y * r.width) / (ref.x * r.height);
		k = k * k;

		return r.getCenter().translate((int) (r.width * dx / Math.sqrt(1 + k)),
			(int) (r.height * dy / Math.sqrt(1 + 1 / k)));
	}

	public Point getLocationTriangle(Point reference)
	{
		Rectangle r = Rectangle.SINGLETON;
		r.setBounds(getOwner().getBounds());
		r.translate(-1, -1);
		r.resize(1, 1);

		getOwner().translateToAbsolute(r);
		float centerX = r.x + 0.5f * r.width;
		float centerY = r.y + (2 * r.height) / 3;

		PointList pl = ((NodeFigure) getOwner()).calculateTrianglePoints(r);

		Point p1 = ChsGeometry.getIntersection(reference,
			new Point(centerX, centerY),
			pl.getFirstPoint(),
			pl.getLastPoint());
		Point p2 = ChsGeometry.getIntersection(reference,
			new Point(centerX, centerY),
			pl.getFirstPoint(),
			pl.getMidpoint());
		Point p3 = ChsGeometry.getIntersection(reference,
			new Point(centerX, centerY),
			pl.getMidpoint(),
			pl.getLastPoint());

		Dimension diff = reference.getDifference(pl.getLastPoint());
		Point intPoint;
		
		if (diff.height >= 0)
		{
			Point fixedP = pl.getLastPoint();
			boolean otherSide = false;
		
			if (centerX > reference.x)
			{
				fixedP = pl.getMidpoint();
				otherSide = true;
			}

			int xx1 = (int) (fixedP.x - centerX);
			int xx2 = reference.x - fixedP.x;
			double yy1 = (fixedP.y - centerY);
			double yy2 = (yy1 * ((double) xx2 / xx1));

			intPoint = p3;
			
			if (reference.y - fixedP.y <= yy2)
			{
				intPoint = p1;
			
				if (otherSide)
				{
					intPoint = p2;
				}
			}
		}
		else
		{
			diff = reference.getDifference(pl.getFirstPoint());
			
			if (diff.width >= 0)
			{
				return p1;
			}
			else
			{
				return p2;
			}
		}
		
		return intPoint;
	}
}