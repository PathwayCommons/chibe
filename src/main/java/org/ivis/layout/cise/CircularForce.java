package org.ivis.layout.cise;

/**
 * This class is a utility class that is used to store the rotation amount,
 * x-axis displacement and y-axis displacement components of a force that act
 * upon an on-circle node. The calculation for this is done in CiSECircle for
 * specified on-circle node. Here we assume that forces on on-circle nodes can
 * be modelled with forces acting upon the perimeter of a circular flat, rigid
 * object sitting on a 2-dimensional surface, free to move in a direction
 * without any friction. Thus, such an object is assumed to move and rotate on
 * this force in amounts proportional to the total force (not just the vertical
 * component of the force!) and the component of the force that is tangential to
 * the circular shape of the object, respectively.
 *
 * @author Alptug Dilek
 * @author Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class CircularForce
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/**
	 * This is the rotation amount that is to be assigned to the rotationAmount
	 * of a CiSENode.
	 */
	private double rotationAmount;

	/**
	 * This is the x-axis displacement value that is to be assigned to the
	 * displacementX of a CiSENode.
	 */
	private double displacementX;

	/**
	 * This is the y-axis displacement value that is to be assigned to the
	 * displacementY of a CiSENode.
	 */
	private double displacementY;

// -----------------------------------------------------------------------------
// Section: Constructors and initializations
// -----------------------------------------------------------------------------
	/**
	 * Default constructor
	 */
	public CircularForce()
	{
	}

	/**
	 * Constructor with parameters
	 */
	public CircularForce(
		double rotationAmount,
		double displacementX,
		double displacementY)
	{
		this.rotationAmount = rotationAmount;
		this.displacementX = displacementX;
		this.displacementY = displacementY;
	}

// -----------------------------------------------------------------------------
// Section: Accessors and mutators
// -----------------------------------------------------------------------------
	public double getRotationAmount()
	{
		return rotationAmount;
	}

	public void setRotationAmount(double rotationAmount)
	{
		this.rotationAmount = rotationAmount;
	}

	public double getDisplacementX()
	{
		return displacementX;
	}

	public void setDisplacementX(double displacementX)
	{
		this.displacementX = displacementX;
	}

	public double getDisplacementY()
	{
		return displacementY;
	}

	public void setDisplacementY(double displacementY)
	{
		this.displacementY = displacementY;
	}
}