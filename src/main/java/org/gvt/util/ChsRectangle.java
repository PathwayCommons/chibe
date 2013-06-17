package org.gvt.util;


/**
 * This class represents an alternative for PrecisionRectangle for performance
 * reasons.
 *
 * @author Ugur Dogrusoz
 * @author Shatlyk Ashyralyev
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class ChsRectangle
{
// -----------------------------------------------------------------------------
// Section: Instance Variables
// -----------------------------------------------------------------------------
	/**
	 * Geometry of rectangle
	 */
	public double x;
	public double y;
	public double width;
	public double height;
	
// -----------------------------------------------------------------------------
// Section: Constructors and Initialization
// -----------------------------------------------------------------------------
	/**
	 * Empty constructor
	 */	
	public ChsRectangle()
	{
		this.x = 0;
		this.y = 0;		
		this.height = 0;
		this.width = 0;
	}

	/**
	 * Constructor
	 */	
	public ChsRectangle(double x, double y, double height, double width)
	{
		this.x = x;
		this.y = y;		
		this.height = height;
		this.width = width;
	}

// -----------------------------------------------------------------------------
// Section: Accessors
// -----------------------------------------------------------------------------
	public double getX() 
	{
		return x;
	}

	public void setX(double x) 
	{
		this.x = x;
	}

	public double getY() 
	{
		return y;
	}

	public void setY(double y) 
	{
		this.y = y;
	}

	public double getWidth() 
	{
		return width;
	}

	public void setWidth(double width) 
	{
		this.width = width;
	}

	public double getHeight() 
	{
		return height;
	}

	public void setHeight(double height) 
	{
		this.height = height;
	}
	
// -----------------------------------------------------------------------------
// Section: Remaining Methods
// -----------------------------------------------------------------------------

	public double getRight()
	{
		return this.x + this.width;
	}
	
	public double getBottom()
	{
		return this.y + this.height;
	}
	
	public boolean intersects(ChsRectangle a)
	{
		if (this.getRight() <= a.x)
		{
			return false;
		}

		if (this.getBottom() <= a.y)
		{
			return false;
		}

		if(a.getRight() <= this.x)
		{
			return false;
		}

		if (a.getBottom() <= this.y)
		{
			return false;
		}

		return true;
	}
	
	public double getCenterX()
	{
		return this.x + this.width / 2;
	}
	
	public double getCenterY()
	{
		return this.y + this.height / 2;
	}
	
	public double getWidthHalf()
	{
		return this.width / 2;
	}
	
	public double getHeightHalf()
	{
		return this.height / 2;
	}
}
