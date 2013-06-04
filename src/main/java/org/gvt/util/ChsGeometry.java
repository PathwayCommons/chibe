package org.gvt.util;

import java.awt.geom.Line2D;
import java.util.*;

import org.eclipse.draw2d.geometry.*;
import org.gvt.util.ChsRectangle;

/**
 * This class maintains a list of static geometry related utility methods.
 *
 * @author Ugur Dogrusoz
 * @author Esat Belviranli
 * @author Shatlyk Ashyralyev
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
abstract public class ChsGeometry
{
// -----------------------------------------------------------------------------
// Section: Class Methods
// -----------------------------------------------------------------------------
	/**
	 * This method calculates the intersection (clipping) points for
	 * two rectangles with line defined by the centers of these two rectangles.
	 */
	public static double[] getIntersection(ChsRectangle rectA, ChsRectangle rectB)
	{
		//result[0-1] will contain clipPoint of rectA, result[2-3] will contain clipPoint of rectB
		double[] result = new double[4];
		
		double p1x = rectA.getCenterX();
		double p1y = rectA.getCenterY();		
		double p2x = rectB.getCenterX();
		double p2y = rectB.getCenterY();
		
		//if two rectangles intersects each other, then clipping points are centers
		if (rectA.intersects(rectB))
		{
			result[0] = p1x;
			result[1] = p1y;
			result[2] = p2x;
			result[3] = p2y;
			return result;
		}
		
		//variables for rectA
		double topLeftAx = rectA.getX();
		double topLeftAy = rectA.getY();
		double topRightAx = rectA.getRight();
		double bottomLeftAx = rectA.getX();
		double bottomLeftAy = rectA.getBottom();
		double bottomRightAx = rectA.getRight();
		double halfWidthA = rectA.getWidthHalf();
		double halfHeightA = rectA.getHeightHalf();
		
		//variables for rectB
		double topLeftBx = rectB.getX();
		double topLeftBy = rectB.getY();
		double topRightBx = rectB.getRight();
		double bottomLeftBx = rectB.getX();
		double bottomLeftBy = rectB.getBottom();
		double bottomRightBx = rectB.getRight();
		double halfWidthB = rectB.getWidthHalf();
		double halfHeightB = rectB.getHeightHalf();

		//flag whether clipping points are found
		boolean clipPointAFound = false;
		boolean clipPointBFound = false;
		

		// line is vertical
		if (p1x == p2x)
		{
			if(p1y > p2y)
			{
				result[0] = p1x;
				result[1] = topLeftAy;
				result[2] = p2x;
				result[3] = bottomLeftBy;
				return result; 
			}
			else if(p1y < p2y)
			{
				result[0] = p1x;
				result[1] = bottomLeftAy;
				result[2] = p2x;
				result[3] = topLeftBy;
				return result;
			}
			else
			{
				//not line, return null;
			}
		}
		// line is horizontal
		else if (p1y == p2y)
		{
			if(p1x > p2x)
			{
				result[0] = topLeftAx;
				result[1] = p1y;
				result[2] = topRightBx;
				result[3] = p2y;
				return result;
			}
			else if(p1x < p2x)
			{
				result[0] = topRightAx;
				result[1] = p1y;
				result[2] = topLeftBx;
				result[3] = p2y;
				return result;
			}
			else
			{
				//not valid line, return null;
			}
		}
		else
		{
			//slopes of rectA's and rectB's diagonals
			double slopeA = rectA.height / rectA.width;
			double slopeB = rectB.height / rectB.width;
			
			//slope of line between center of rectA and center of rectB
			double slopePrime = (p2y - p1y) / (p2x - p1x);
			int cardinalDirectionA;
			int cardinalDirectionB;
			double tempPointAx;
			double tempPointAy;
			double tempPointBx;
			double tempPointBy;
			
			//determine whether clipping point is the corner of nodeA
			if((-slopeA) == slopePrime)
			{
				if(p1x > p2x)
				{
					result[0] = bottomLeftAx;
					result[1] = bottomLeftAy;
					clipPointAFound = true;
				}
				else
				{
					result[0] = topRightAx;
					result[1] = topLeftAy;
					clipPointAFound = true;
				}
			}
			else if(slopeA == slopePrime)
			{
				if(p1x > p2x)
				{
					result[0] = topLeftAx;
					result[1] = topLeftAy;
					clipPointAFound = true;
				}
				else
				{
					result[0] = bottomRightAx;
					result[1] = bottomLeftAy;
					clipPointAFound = true;
				}
			}
			
			//determine whether clipping point is the corner of nodeB
			if((-slopeB) == slopePrime)
			{
				if(p2x > p1x)
				{
					result[2] = bottomLeftBx;
					result[3] = bottomLeftBy;
					clipPointBFound = true;
				}
				else
				{
					result[2] = topRightBx;
					result[3] = topLeftBy;
					clipPointBFound = true;
				}
			}
			else if(slopeB == slopePrime)
			{
				if(p2x > p1x)
				{
					result[2] = topLeftBx;
					result[3] = topLeftBy;
					clipPointBFound = true;
				}
				else
				{
					result[2] = bottomRightBx;
					result[3] = bottomLeftBy;
					clipPointBFound = true;
				}
			}
			
			//if both clipping points are corners
			if(clipPointAFound && clipPointBFound)
			{
				return result;
			}
			
			//determine Cardinal Direction of rectangles
			if(p1x > p2x)
			{
				if(p1y > p2y)
				{
					cardinalDirectionA = getCardinalDirection(slopeA, slopePrime, 4);
					cardinalDirectionB = getCardinalDirection(slopeB, slopePrime, 2);
				}
				else
				{
					cardinalDirectionA = getCardinalDirection(-slopeA, slopePrime, 3);
					cardinalDirectionB = getCardinalDirection(-slopeB, slopePrime, 1);
				}
			}
			else
			{
				if(p1y > p2y)
				{
					cardinalDirectionA = getCardinalDirection(-slopeA, slopePrime, 1);
					cardinalDirectionB = getCardinalDirection(-slopeB, slopePrime, 3);
				}
				else
				{
					cardinalDirectionA = getCardinalDirection(slopeA, slopePrime, 2);
					cardinalDirectionB = getCardinalDirection(slopeB, slopePrime, 4);
				}
			}
			//calculate clipping Point if it is not found before
			if(!clipPointAFound)
			{
				switch(cardinalDirectionA)
				{
					case 1:
						tempPointAy = topLeftAy;
						tempPointAx = p1x + ( -halfHeightA ) / slopePrime;
						result[0] = tempPointAx;
						result[1] = tempPointAy;
						break;
					case 2:
						tempPointAx = bottomRightAx;
						tempPointAy = p1y + halfWidthA * slopePrime;
						result[0] = tempPointAx;
						result[1] = tempPointAy;
						break;
					case 3:
						tempPointAy = bottomLeftAy;
						tempPointAx = p1x + halfHeightA / slopePrime;
						result[0] = tempPointAx;
						result[1] = tempPointAy;
						break;
					case 4:
						tempPointAx = bottomLeftAx;
						tempPointAy = p1y + ( -halfWidthA ) * slopePrime;
						result[0] = tempPointAx;
						result[1] = tempPointAy;
						break;
				}
			}
			if(!clipPointBFound)
			{
				switch(cardinalDirectionB)
				{
					case 1:
						tempPointBy = topLeftBy;
						tempPointBx = p2x + ( -halfHeightB ) / slopePrime;
						result[2] = tempPointBx;
						result[3] = tempPointBy;
						break;
					case 2:
						tempPointBx = bottomRightBx;
						tempPointBy = p2y + halfWidthB * slopePrime;
						result[2] = tempPointBx;
						result[3] = tempPointBy;
						break;
					case 3:
						tempPointBy = bottomLeftBy;
						tempPointBx = p2x + halfHeightB / slopePrime;
						result[2] = tempPointBx;
						result[3] = tempPointBy;
						break;
					case 4:
						tempPointBx = bottomLeftBx;
						tempPointBy = p2y + ( -halfWidthB ) * slopePrime;
						result[2] = tempPointBx;
						result[3] = tempPointBy;
						break;
				}
			}
			
		}
		return result;		//return result array of coordinates
	}
	
	/**
	 * This method returns in which cardinal direction does input point stays
	 * 1: North
	 * 2: East
	 * 3: South
	 * 4: West
	 */
	private static int getCardinalDirection(double slope,
		double slopePrime,
		int line)
	{
		if (slope > slopePrime)
		{
			return line;
		}
		else
		{
			return 1 + line % 4;
		}
	}
	
	/**
	 * This method calculates the intersection (clipping) point of the input
	 * rectangle with the line defined by the input point pair.
	 *
	 * Note: This method and other methods used by only this method are no
	 * longer used for efficiency reasons!
	 */
	public static PrecisionPoint getIntersection(ChsRectangle rect,
		double p1x, double p1y,
		double p2x, double p2y)
	{
		double ip1x = 0;
		double ip1y = 0;
		double ip2x = 0;
		double ip2y = 0;

		// Since we are not using objects for performance constraints,
		// we should have booleans indicating whether we found some
		// intersection point.
		boolean ip1Null = true;
		boolean ip2Null = true;

		double topLeftX = rect.x;
		double topLeftY = rect.y;
		double topRightX = rect.getRight();
		double bottomLeftX = rect.x;
		double bottomLeftY = rect.getBottom();
		double bottomRightX = rect.getRight();

		// Calculate top line
		if (p1x == p2x)
		{
			if (p1y == p2y)
			{
				// not a valid line, return null
			}
			else if (p1x >= bottomLeftX &&
				 p1x <= bottomRightX)
			{
				// line vertical
				if (topLeftY != bottomLeftY)
				{
					ip1x = p1x;
					ip1y = bottomLeftY;
					ip2x = p1x;
					ip2y = topLeftY;

					ip1Null = ip2Null = false;
				}
				else
				{
					// Rectangle is in fact a line
					ip1x = p1x;
					ip1y = topLeftY;
					ip1Null = false;
				}
			}
		}
		else if (p1y == p2y)
		{
			// line horizantal
			if (p1y >= bottomLeftY &&
				p1y <= topLeftY)
			{
				if (topLeftX != topRightX)
				{
					ip1x = topRightX;
					ip1y = p1y;
					ip2x = p1x;
					ip2y = topLeftY;
					ip1Null = ip2Null = false;
				}
				else
				{
					// Rectangle is in fact a line
					ip1x = topLeftX;
					ip1y = p1y;
					ip1Null = false;
				}
			}
		}
		else
		{
			double tempPointX;
			double tempPointY;

			// General case, the line is not vertical or horizantal

			// Test hitting the top of the ractangle
			tempPointY = topLeftY;
			tempPointX = intersectsAtY(p1x, p2x,
				p1y, p2y, tempPointY);

			if (tempPointX != Double.NaN &&
				tempPointX <= bottomRightX &&
				tempPointX >= bottomLeftX)
			{
				ip1x = tempPointX;
				ip1y = topLeftY;
				ip1Null = false;
			}

			// Test hitting the bottom of the ractangle
			tempPointY = bottomLeftY;
			tempPointX = intersectsAtY(p1x, p2x,
				p1y, p2y, tempPointY);

			if (tempPointX != Double.NaN &&
				tempPointX <= bottomRightX &&
				tempPointX >= bottomLeftX)
			{
				if (ip1Null)
				{
					ip1x = tempPointX;
					ip1y = bottomLeftY;
					ip1Null = false;
				}
				else
				{
					ip2x = tempPointX;
					ip2y = bottomLeftY;
					ip2Null = false;
				}
			}

			// When hitting left or right, we exclude the corner points,
			// such that we don't get them twice.

			// Test hitting left of rectangle
			tempPointX = bottomLeftX;
			tempPointY = intersectsAtX(p1x, p2x,
				p1y, p2y, tempPointX);

			if (tempPointY != Double.NaN &&
				topLeftY < tempPointY &&
				tempPointY < bottomLeftY)
			{
				if (ip1Null)
				{
					ip1x = bottomLeftX;
					ip1y = tempPointY;
					ip1Null = false;
				}
				else
				{
					ip2x = bottomLeftX;
					ip2y = tempPointY;
					ip2Null = false;
				}
			}

			// Test hitting right of rectangle
			tempPointX = bottomRightX;
			tempPointY = intersectsAtX(p1x, p2x,
				p1y, p2y, tempPointX);

			if (tempPointY != Double.NaN &&
				topLeftY < tempPointY &&
				tempPointY < bottomLeftY)
			{
				if (ip1Null)
				{
					ip1x = bottomRightX;
					ip1y = tempPointY;
					ip1Null = false;
				}
				else
				{
					ip2x = bottomRightX;
					ip2y = tempPointY;
					ip2Null = false;
				}
			}
		}

		PrecisionPoint ip1 = null;
		PrecisionPoint ip2 = null;

		if (!ip1Null)
		{
			ip1 = new PrecisionPoint(ip1x,ip1y);
		}
		if (!ip2Null)
		{
			ip2 = new PrecisionPoint(ip2x,ip2y);
		}

		return findTheClosestPoint(p2x, p2y, ip1, ip2);
	}

	/**
	 * This method returns the closest point, among the first and seconds point,
	 * to the reference point
	 */
	private static PrecisionPoint findTheClosestPoint(double refPointX,
		double refPointY,
		PrecisionPoint first,
		PrecisionPoint second)
	{
		PrecisionPoint temp = null;

		if (second == null && first != null)
		{
			temp = first;
		}
		else if (second != null && first == null)
		{
			temp = second;
		}
		else if (second != null && first != null)
		{
			double term1 = refPointX - first.preciseX;
			double term2 = refPointY - first.preciseY;

			double distanceFirst = term1 * term1 + term2 * term2;

			term1 = refPointX - second.preciseX;
			term2 = refPointY - second.preciseY;

			double distanceSecond = term1 * term1 + term2 * term2;

			if (distanceFirst >= distanceSecond)
			{
				temp = second;
			}
			else
			{
				temp = first;
			}
		}

		return temp;
	}

	/**
	 * This method finds the y coordinate of a line at a specified x coordinate
	 * returns a valid point if the line exists at x point and the line is not
	 * vertical.
	 */
	private static double intersectsAtX(double x1, double x2,
		double y1, double y2, double x)
	{
		double y = Double.NaN;

		if (x1 != x2 )
		{
			if (x == x1)
			{
				y = y1;
			}
			else if (x == x2)
			{
				y = y2;
			}
			else
			{
				double tempY = y1 + ((y2 - y1) * (x - x1)) / (x2 - x1);

				// Check whether found y is lying on the rectangle edge.
				if (((tempY <= y1 && tempY >= y2)||
					(tempY >= y1 && tempY <= y2)))
				{
					y = tempY;
				}
			}
		}

		return y;
	}

	/**
	 * This method finds the x coordinate of a line at a specified y coordinate
	 * returns a valid point if the line exists at y point and the line is not
	 * horizantal.
	 */
	private static double intersectsAtY(double x1, double x2,
		double y1, double y2, double y)
	{
		double x = Double.NaN;

		if (y1 != y2 )
		{
			if (y == y1)
			{
				x = x1;
			}
			else if (y == y2)
			{
				x = x2;
			}
			else
			{
				double tempX = x1 + ((x2 - x1) * (y - y1)) / (y2 - y1);

				// Check whether found x is lying on the rectangle edge.
				if (((tempX <= x1 && tempX >= x2)||
					(tempX >= x1 && tempX <= x2)))
				{
					x = tempX;
				}

			}
		}

		return x;
	}

	/**
	 * This method calculates the intersection of the two lines defined by
	 * point pairs (s1,s2) and (f1,f2).
	 */
	public static Point getIntersection(Point s1, Point s2, Point f1, Point f2)
	{
		int x1 = s1.x;
		int y1 = s1.y;
		int x2 = s2.x;
		int y2 = s2.y;
		int x3 = f1.x;
		int y3 = f1.y;
		int x4 = f2.x;
		int y4 = f2.y;

		int x, y; // intersection point

		int a1, a2, b1, b2, c1, c2; // coefficients of line eqns.

		int denom;

		a1 = y2 - y1;
		b1 = x1 - x2;
		c1 = x2 * y1 - x1 * y2;  // { a1*x + b1*y + c1 = 0 is line 1 }

		a2 = y4 - y3;
		b2 = x3 - x4;
		c2 = x4 * y3 - x3 * y4;  // { a2*x + b2*y + c2 = 0 is line 2 }

		denom = a1 * b2 - a2 * b1;

		if (denom == 0)
		{
			return null;
		}

		x = (b1 * c2 - b2 * c1) / denom;
		y = (a2 * c1 - a1 * c2) / denom;

		return new Point(x, y);
	}

	/**
	 * This method finds and returns the angle of the vector from the + x-axis
	 * in clockwise direction (compatible w/ Java coordinate system!).
	 */
	public static double angleOfVector(double Cx, double Cy,
		double Nx, double Ny)
	{
		double C_angle;

		if (Cx != Nx)
		{
			C_angle = Math.atan((Ny - Cy) / (Nx - Cx));

			if (Nx < Cx)
			{
				C_angle += Math.PI;
			}
			else if (Ny < Cy)
			{
				C_angle += TWO_PI;
			}
		}
		else if (Ny < Cy)
		{
			C_angle = ONE_AND_HALF_PI; // 270 degrees
		}
		else
		{
			C_angle = HALF_PI; // 90 degrees
		}

//		assert 0.0 <= C_angle && C_angle < TWO_PI;

		return C_angle;
	}

	/**
	 * This method converts the given angle in radians to degrees.
	 */
	public static double radian2degree(double rad)
	{
		return 180.0 * rad / Math.PI;
	}

	/**
	 * This method checks whether the given two line segments (one with point
	 * p1 and p2, the other with point p3 and p4) intersect at a point other
	 * than these points.
	 */
	public static boolean doIntersect(PrecisionPoint p1, PrecisionPoint p2,
		PrecisionPoint p3, PrecisionPoint p4)
	{
		boolean result = Line2D.linesIntersect(p1.preciseX, p1.preciseY,
			p2.preciseX, p2.preciseY, p3.preciseX, p3.preciseY,
			p4.preciseX, p4.preciseY);

		return result;
	}

	private static void testClippingPoints()
	{
		ChsRectangle rectA = new ChsRectangle(5, 6, 2, 4);
		ChsRectangle rectB;
		
		rectB = new ChsRectangle(0, 4, 1, 4);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new ChsRectangle(1, 4, 1, 2);
		findAndPrintClipPoints(rectA, rectB);
	
		rectB = new ChsRectangle(1, 3, 3, 2);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------		
		rectB = new ChsRectangle(2, 3, 2, 4);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new ChsRectangle(3, 3, 2, 2);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new ChsRectangle(3, 2, 4, 2);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------		
		rectB = new ChsRectangle(6, 3, 2, 2);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------		
		rectB = new ChsRectangle(9, 2, 4, 2);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new ChsRectangle(9, 3, 2, 2);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new ChsRectangle(8, 3, 2, 4);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------
		rectB = new ChsRectangle(11, 3, 3, 2);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new ChsRectangle(11, 4, 1, 2);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new ChsRectangle(10, 4, 1, 4);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------
		rectB = new ChsRectangle(10, 5, 2, 2);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new ChsRectangle(9, 4.5, 2, 4);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new ChsRectangle(10, 5.8, 0.4, 2);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------		
		rectB = new ChsRectangle(11, 6, 2, 2);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------
		rectB = new ChsRectangle(10, 7.8, 0.4, 2);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new ChsRectangle(9, 7.5, 1, 4);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new ChsRectangle(10, 7, 2, 2);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------
		rectB = new ChsRectangle(10, 9, 2, 6);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new ChsRectangle(11, 9, 2, 4);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new ChsRectangle(12, 8, 4, 2);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------
		rectB = new ChsRectangle(7, 9, 2, 4);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new ChsRectangle(8, 9, 4, 2);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new ChsRectangle(10, 9, 2, 2);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------
		rectB = new ChsRectangle(6, 10, 2, 2);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------
		rectB = new ChsRectangle(3, 8, 4, 2);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new ChsRectangle(3, 9, 2, 2);
		findAndPrintClipPoints(rectA, rectB);

		rectB = new ChsRectangle(2, 8, 4, 4);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------
		rectB = new ChsRectangle(2, 8, 2, 2);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new ChsRectangle(1, 8, 2, 4);
		findAndPrintClipPoints(rectA, rectB);
	
		rectB = new ChsRectangle(1, 8.5, 1, 4);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------
		rectB = new ChsRectangle(3, 7, 2, 2);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new ChsRectangle(1, 7.5, 1, 4);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new ChsRectangle(3, 7.8, 0.4, 2);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------
		rectB = new ChsRectangle(1, 6, 2, 2);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------		
		rectB = new ChsRectangle(3, 5.8, 0.4, 2);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new ChsRectangle(1, 5, 1, 3);
		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new ChsRectangle(1, 4, 3, 3);
		findAndPrintClipPoints(rectA, rectB);
//----------------------------------------------
		rectB = new ChsRectangle(4, 4, 3, 3);
//		findAndPrintClipPoints(rectA, rectB);
		
		rectB = new ChsRectangle(5, 6, 2, 4);
//		findAndPrintClipPoints(rectA, rectB);
	}
	
	private static void findAndPrintClipPoints(ChsRectangle rectA, ChsRectangle rectB)
	{
		System.out.println("---------------------");
		double[] clipPoints = new double[4];
		
		System.out.println("RectangleA  X: " + rectA.x + "  Y: " + rectA.y + "  Width: " + rectA.width + "  Height: " + rectA.height);
		System.out.println("RectangleB  X: " + rectB.x + "  Y: " + rectB.y + "  Width: " + rectB.width + "  Height: " + rectB.height);
		clipPoints = ChsGeometry.getIntersection(rectA, rectB);

		System.out.println("Clip Point of RectA X:" + clipPoints[0] + " Y: " + clipPoints[1]);
		System.out.println("Clip Point of RectB X:" + clipPoints[2] + " Y: " + clipPoints[3]);	
	}
	/*
	 * Main method for testing purposes.
	 */
	public static void main(String [] args)
	{
		testClippingPoints();	
	}

// -----------------------------------------------------------------------------
// Section: Class Constants
// -----------------------------------------------------------------------------
	/**
	 * Some useful pre-calculated constants
	 */
	public static final double HALF_PI = 0.5 * Math.PI;
	public static final double ONE_AND_HALF_PI = 1.5 * Math.PI;
	public static final double TWO_PI = 2.0 * Math.PI;
	public static final double THREE_PI = 3.0 * Math.PI;
}