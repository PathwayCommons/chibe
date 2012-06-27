package org.gvt.model;

import org.eclipse.draw2d.Bendpoint;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.gvt.figure.EdgeFigure;

/**
 * EdgeBendpoint is a Bendpoint that calculates its location based on its
 * distance from the start and end points of the EdgeFigure, as well as its
 * weight. Detailed information can be found in setWeight() method.
 * 
 * Bendpoints are also used in the calculation of its owners (CompoundModel)
 * size. Hence getLocationFromModel() method is implemented to calculate the
 * location of bendpoints by using model information instead of UI information.
 * This is needed because UI is updated after operations according to model are
 * done.
 *
 * @author Cihan Kucukkececi
 * 
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class EdgeBendpoint implements Bendpoint
{
	private Connection connection;

	private float weight = 0.5f;

	private Dimension d1, d2;

	/**
	 * Constructs a new Bendpoint.
	 * 
	 */
	public EdgeBendpoint()
	{
	}

	/**
	 * Constructs a new Bendpoint and associates it with the given Connection.
	 */
	public EdgeBendpoint(Connection conn)
	{
		setConnection(conn);
	}

	public EdgeBendpoint(int dim1w, int dim1h, int dim2w, int dim2h)
	{
		d1 = new Dimension(dim1w, dim1h);
		d2 = new Dimension(dim2w, dim2h);
	}

	/**
	 * Returns the Connection this Bendpoint is associated with.
	 */
	protected Connection getConnection()
	{
		return connection;
	}

	/**
	 * Calculates and returns this Bendpoint's new location.
	 */
	public Point getLocation()
	{
		Point a1 = getConnection().getSourceAnchor().getReferencePoint();
		Point a2 = getConnection().getTargetAnchor().getReferencePoint();

		Point p = new Point();
		Dimension dim1 = d1.getCopy(), dim2 = d2.getCopy();

		getConnection().translateToAbsolute(dim1);
		getConnection().translateToAbsolute(dim2);

		p.x = (int) ((a1.x + dim1.width) * (1f - weight) + weight
			* (a2.x + dim2.width));
		p.y = (int) ((a1.y + dim1.height) * (1f - weight) + weight
			* (a2.y + dim2.height));
		getConnection().translateToRelative(p);

		return p;
	}

	/**
	 * Calculates and returns this Bendpoint's new location by using the model
	 * structure not UI. This methos is used to calculate bounds of compound
	 * nodes by using bendpoints before updating the UI.
	 */
	public Point getLocationFromModel(EdgeModel model)
	{
		Dimension dim1 = model.getSource().getSize();
		Dimension dim2 = model.getTarget().getSize();

		Point a1 = model.getSource().getLocationAbs().
			translate(dim1.getScaled(0.5));
		Point a2 = model.getTarget().getLocationAbs().
			translate(dim2.getScaled(0.5));

		Point p = new Point();
		dim1 = d1.getCopy();
		dim2 = d2.getCopy();

		p.x = (int) ((a1.x + dim1.width) * (1f - weight) + weight
			* (a2.x + dim2.width));
		p.y = (int) ((a1.y + dim1.height) * (1f - weight) + weight
			* (a2.y + dim2.height));

		return p;
	}

	/**
	 * Sets the Connection this bendpoint should be associated with.
	 */
	public void setConnection(Connection conn)
	{
		connection = conn;
	}

	/**
	 * Sets the Dimensions representing the X and Y distances this Bendpoint is
	 * from the start and end points of the Connection. These Dimensions are
	 * generally set once and are used in calculating the Bendpoint's location.
	 */
	public void setRelativeDimensions(Dimension dim1, Dimension dim2)
	{
		d1 = dim1;
		d2 = dim2;
	}

	/**
	 * Sets the weight this Bendpoint should use to calculate its location. The
	 * weight should be between 0.0 and 1.0. A weight of 0.0 will cause the
	 * Bendpoint to follow the start point, while a weight of 1.0 will cause the
	 * Bendpoint to follow the end point. A weight of 0.5 (the default) will
	 * cause the Bendpoint to maintain its original aspect ratio between the
	 * start and end points.
	 */
	public void setWeight(float w)
	{
		weight = w;
	}

	public Dimension getFirstRelativeDimension()
	{
		return d1;
	}

	public Dimension getSecondRelativeDimension()
	{
		return d2;
	}

	public float getWeight()
	{
		return weight;
	}

	public String toString()
	{
		return d1.width + "," + d1.height + "," + d2.width + "," + d2.height;
	}
}