package org.gvt.figure;

import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.RotatableDecoration;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * @author Ozgun Babur
 */
public class CircleDecoration extends Ellipse implements RotatableDecoration
{
	private int radius = 4;

	private Point center = new Point();
	private Point ref = new Point();
	private Point locToSet = new Point();

	public void setRadius(int radius)
	{
		erase();
		this.radius = Math.abs(radius);
		bounds = null;
		repaint();
	}

	public void setLineWidth(int width)
	{
		super.setLineWidth(width);
	}

	public Rectangle getBounds()
	{
		if (bounds == null)
		{
			int diameter = radius * 2;
			bounds = new Rectangle(center.x - radius, center.y - radius, diameter, diameter);
			bounds.expand(lineWidth / 2, lineWidth / 2);
		}
		return bounds;
	}

	public void setLocation(Point p)
	{
		locToSet = p;
	}

	private void updateLocation(Point p)
	{
		if (center.equals(p))
		{
			return;
		}
		center.setLocation(p);
		bounds = null;
	}

	public void setReferencePoint(Point p)
	{
		this.ref = p;
		updateLocation(findBetterLocation(locToSet));
	}

	private Point findBetterLocation(Point loc)
	{
		Point p = new Point(loc);

		if (p.y == ref.y)
		{
			p.x += ref.x > p.x ? radius - 1 : -radius + 1;
		} else
		{
			double r = Math.abs((p.x - ref.x) / (double) (p.y - ref.y));
			double dy = (radius - 1) / Math.sqrt((r * r) + 1);
			double dx = dy * r;

			if (ref.x < p.x) dx *= -1;
			if (ref.y < p.y) dy *= -1;

			p.x = (int) Math.round(p.x + dx);
			p.y = (int) Math.round(p.y + dy);
		}

		return p;
	}
}
