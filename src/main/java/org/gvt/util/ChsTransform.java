package org.gvt.util;

import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.PrecisionDimension;
import org.eclipse.draw2d.geometry.PrecisionRectangle;

/**
 * This class is for transforming certain world coordinates to device ones.
 *  
 * Following example transformation translates (shifts) world coordinates by
 * (10,20), scales objects in the world to be twice as tall but half as wide
 * in device coordinates. In addition it flips the y coordinates.
 * 
 *			(wox,woy): world origin (x,y)
 *			(wex,wey): world extension x and y
 *			(dox,doy): device origin (x,y)
 *			(dex,dey): device extension x and y
 *
 *										(dox,doy)=(10,20)
 *											*--------- dex=50
 *											|
 *			 wey=50							|
 *				|							|
 *				|							|
 *				|							|
 *				*------------- wex=100		|
 *			(wox,woy)=(0,0)					dey=-100
 *
 * In most cases, we will set all values to 1.0 except dey=-1.0 to flip the y
 * axis.
 * 
 * @author Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class ChsTransform {
// ---------------------------------------------------------------------
// Section: Instance variables.
// ---------------------------------------------------------------------
	
	/* World origin and extension */
	private double lworldOrgX;
	private double lworldOrgY;
	private double lworldExtX;
	private double lworldExtY;

	/* Device origin and extension */
	private double ldeviceOrgX;
	private double ldeviceOrgY;
	private double ldeviceExtX;
	private double ldeviceExtY;

// ---------------------------------------------------------------------
// Section: Constructors and initialization.
// ---------------------------------------------------------------------

	/**
	 * Default constructor.
	 */
	public ChsTransform()
	{
		this.init();
	}

	/**
	 * This method initializes an object of this class.
	 */
	void init()
	{
		lworldOrgX = 0.0;
		lworldOrgY = 0.0;
		ldeviceOrgX = 0.0;
		ldeviceOrgY = 0.0;
		lworldExtX = 1.0;
		lworldExtY = 1.0;
		ldeviceExtX = 1.0;
		ldeviceExtY = 1.0;
	}

// ---------------------------------------------------------------------
// Section: Get/set methods for instance variables.
// ---------------------------------------------------------------------
	
	/* World related */
	
	public double getWorldOrgX()
	{
		return this.lworldOrgX;
	}

	public void setWorldOrgX(double wox)
	{
		this.lworldOrgX = wox;
	}
	
	public double getWorldOrgY()
	{
		return this.lworldOrgY;
	}

	public void setWorldOrgY(double woy)
	{
		this.lworldOrgY = woy;
	}
	
	public double getWorldExtX()
	{
		return this.lworldExtX;
	}

	public void setWorldExtX(double wex)
	{
		this.lworldExtX = wex;
	}
	
	public double getWorldExtY()
	{
		return this.lworldExtY;
	}

	public void setWorldExtY(double wey)
	{
		this.lworldExtY = wey;
	}

	/* Device related */
	
	public double getDeviceOrgX()
	{
		return this.ldeviceOrgX;
	}

	public void setDeviceOrgX(double dox)
	{
		this.ldeviceOrgX = dox;
	}
	
	public double getDeviceOrgY()
	{
		return this.ldeviceOrgY;
	}

	public void setDeviceOrgY(double doy)
	{
		this.ldeviceOrgY = doy;
	}
	
	public double getDeviceExtX()
	{
		return this.ldeviceExtX;
	}

	public void setDeviceExtX(double dex)
	{
		this.ldeviceExtX = dex;
	}
	
	public double getDeviceExtY()
	{
		return this.ldeviceExtY;
	}

	public void setDeviceExtY(double dey)
	{
		this.ldeviceExtY = dey;
	}	

// ---------------------------------------------------------------------
// Section: x or y coordinate transformation
// ---------------------------------------------------------------------

	/**
	 * This method transforms an x position in world coordinates to an x
	 * position in device coordinates.
	 */
	public double transformX(double x)
	{
		double xDevice;
		double worldExtX = this.lworldExtX;

		if (worldExtX != 0.0)
		{
			xDevice = this.ldeviceOrgX +
				((x - this.lworldOrgX) * this.ldeviceExtX / worldExtX);
		}
		else
		{
			xDevice = 0.0;
		}

		return(xDevice);
	}

	/**
	 * This method transforms a y position in world coordinates to a y
	 * position in device coordinates.
	 */
	public double transformY(double y)
	{
		double yDevice;
		double worldExtY = this.lworldExtY;

		if (worldExtY != 0.0)
		{
			yDevice = this.ldeviceOrgY +
				((y - this.lworldOrgY) * this.ldeviceExtY / worldExtY);
		}
		else
		{
			yDevice = 0.0;
		}

		return(yDevice);
	}

	/**
	 * This method transforms an x position in device coordinates to an x
	 * position in world coordinates.
	 */
	public double inverseTransformX(double x)
	{
		double xWorld;
		double deviceExtX = this.ldeviceExtX;

		if (deviceExtX != 0.0)
		{
			xWorld = this.lworldOrgX +
				((x - this.ldeviceOrgX) * this.lworldExtX / deviceExtX);
		}
		else
		{
			xWorld = 0.0;
		}

		return(xWorld);
	}

	/**
	 * This method transforms a y position in device coordinates to a y
	 * position in world coordinates.
	 */
	public double inverseTransformY(double y)
	{
		double yWorld;
		double deviceExtY = this.ldeviceExtY;

		if (deviceExtY != 0.0)
		{
			yWorld = this.lworldOrgY +
				((y - this.ldeviceOrgY) * this.lworldExtY / deviceExtY);
		}
		else
		{
			yWorld = 0.0;
		}

		return(yWorld);
	}

// ---------------------------------------------------------------------
// Section: point, dimension and rectagle transformation
// ---------------------------------------------------------------------

	/**
	 * This method transforms the input point from the world coordinate system
	 * to the device coordinate system.
	 */
	public PrecisionPoint transformPoint(PrecisionPoint inPoint)
	{
		PrecisionPoint outPoint = 
			new PrecisionPoint(this.transformX(inPoint.preciseX),
				this.transformY(inPoint.preciseY));
		
		return(outPoint);
	}

	/**
	 * This method transforms the input dimension from the world coordinate 
	 * system to the device coordinate system.
	 */
	public PrecisionDimension transformDimension(PrecisionDimension inDimension)
	{
		PrecisionDimension outDimension =
			new PrecisionDimension(
				this.transformX(inDimension.preciseWidth) -
					this.transformX(0.0),
				this.transformY(inDimension.preciseHeight) -
					this.transformY(0.0));
		
		return outDimension;
	}

	/**
	 * This method transforms the input rectangle from the world coordinate
	 * system to the device coordinate system.
	 */
	public PrecisionRectangle transformRect(PrecisionRectangle inRect)
	{
		PrecisionRectangle outRect = new PrecisionRectangle();
		
		PrecisionDimension inRectDim = 
			new PrecisionDimension(inRect.preciseWidth, inRect.preciseHeight);
		PrecisionDimension outRectDim = this.transformDimension(inRectDim);
		outRect.setWidth(outRectDim.preciseWidth);
		outRect.setHeight(outRectDim.preciseHeight);
		
		outRect.setX(this.transformX(inRect.preciseX));
		outRect.setY(this.transformY(inRect.preciseY));
		
		return(outRect);
	}

	/**
	 * This method transforms the input point from the device coordinate system
	 * to the world coordinate system.
	 */
	public PrecisionPoint inverseTransformPoint(PrecisionPoint inPoint)
	{
		PrecisionPoint outPoint = 
			new PrecisionPoint(this.inverseTransformX(inPoint.preciseX),
				this.inverseTransformY(inPoint.preciseY));
		
		return(outPoint);
	}

	/** 
	 * This method transforms the input dimension from the device coordinate 
	 * system to the world coordinate system.
	 */
	public PrecisionDimension inverseTransformDimension(PrecisionDimension inDimension)
	{ 
		PrecisionDimension outDimension = 
			new PrecisionDimension(
				this.inverseTransformX(inDimension.preciseWidth - 
					this.inverseTransformX(0.0)),
				this.inverseTransformY(inDimension.preciseHeight - 
					this.inverseTransformY(0.0)));
		
		return(outDimension);
	}

	/**
	 * This method transforms the input rectangle from the device coordinate
	 * system to the world coordinate system. The result is in the passed 
	 * output rectangle object.
	 */
	public PrecisionRectangle inverseTransformRect(PrecisionRectangle inRect) 
	{
		PrecisionRectangle outRect = new PrecisionRectangle();
		
		PrecisionDimension inRectDim = 
			new PrecisionDimension(inRect.preciseWidth, inRect.preciseHeight);
		PrecisionDimension outRectDim = 
			this.inverseTransformDimension(inRectDim);
		outRect.setWidth(outRectDim.preciseWidth);
		outRect.setHeight(outRectDim.preciseHeight);
		
		outRect.setX(this.inverseTransformX(inRect.preciseX));
		outRect.setY(this.inverseTransformY(inRect.preciseY));
		
		return(outRect);
	}

// ---------------------------------------------------------------------
// Section: Remaining methods.
// ---------------------------------------------------------------------

	/**
	 * This method adjusts the world extensions of this transform object
	 * such that transformations based on this transform object will 
	 * preserve the aspect ratio of objects as much as possible.
	 */
	public void adjustExtToPreserveAspectRatio()
	{
		double deviceExtX = this.ldeviceExtX;
		double deviceExtY = this.ldeviceExtY;

		if (deviceExtY != 0.0 &&
			deviceExtX != 0.0)
		{
			double worldExtX = this.lworldExtX;
			double worldExtY = this.lworldExtY;

			if (deviceExtY * worldExtX < deviceExtX * worldExtY)
			{
				this.setWorldExtX((deviceExtY > 0.0) ?
					deviceExtX * worldExtY / deviceExtY :
					0.0);
			}
			else
			{
				this.setWorldExtY((deviceExtX > 0.0) ?
					deviceExtY * worldExtX / deviceExtX :
					0.0);
			}
		}
	}

	/**
	 * This method is for testing purposes only!
	 */
	public static void main(String[] args)
	{
		ChsTransform trans = new ChsTransform();
		
		trans.setWorldOrgX(0.0);
		trans.setWorldOrgY(0.0);
		trans.setWorldExtX(100.0);
		trans.setWorldExtY(50.0);
		
		trans.setDeviceOrgX(10.0);
		trans.setDeviceOrgY(20.0);
		trans.setDeviceExtX(50.0);
		trans.setDeviceExtY(-100.0);
		
		PrecisionRectangle rectWorld = new PrecisionRectangle();
		
		rectWorld.preciseX = 12.0;
		rectWorld.preciseY = -25.0;
		rectWorld.preciseWidth = 150.0;
		rectWorld.preciseHeight = 150.0;

		PrecisionPoint pointWorld = 
			new PrecisionPoint(rectWorld.preciseX, rectWorld.preciseY);
		PrecisionDimension dimWorld =
			new PrecisionDimension(rectWorld.preciseWidth, rectWorld.preciseHeight);
		
		PrecisionPoint pointDevice = trans.transformPoint(pointWorld);
		PrecisionDimension dimDevice = trans.transformDimension(dimWorld);
		PrecisionRectangle rectDevice = trans.transformRect(rectWorld);
		
		// The transformed location & dimension should be (16,70) & (75,-300)
	}
}