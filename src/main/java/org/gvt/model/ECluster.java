package org.gvt.model;

import java.util.ArrayList;
import java.util.Random;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.swt.graphics.Color;
import org.ivis.layout.Cluster;
import org.ivis.layout.ClusterManager;
import org.ivis.util.PointD;

/**
 * This class represents a cluster for editor purpose. It extendas the Cluster
 * class for layout purposes.
 *
 * @author Shatlyk Ashyralyyev
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class ECluster extends Cluster {
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/*
	 * Gap is used for drawing polygon around cluster.
	 */
	private double gapPercentage;
	
	/*
	 * Is polygon highlighted
	 */
	private boolean highlight;	
	
	/*
	 * HighlightColor
	 */
	private Color highlightColor;

// -----------------------------------------------------------------------------
// Section: Constructor
// -----------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	public ECluster(ClusterManager clusterManager, 
			int clusterID,
			String clusterName) {
		super(clusterManager, clusterID, clusterName);

		this.highlightColor = randomColor();
	}
	/**
	 * Another constructor
	 */
	public ECluster(ClusterManager clusterManager, String clusterName) 
	{
		super(clusterManager, clusterName);
		
		this.highlightColor = randomColor();
	}

// -----------------------------------------------------------------------------
// Section: Getters and Setters
// -----------------------------------------------------------------------------
	/**
	 * This method returns the gap percentage of this cluster.
	 */
	public double getGapPercentage() 
	{
		return gapPercentage;
	}

	/**
	 * This method sets the gap percentage of this cluster.
	 */
	public void setGapPercentage(double gapPercentage) 
	{
		this.gapPercentage = gapPercentage;
	}

	/**
	 * This method return true if highlight is true, and false otherwise.
	 */
	public boolean isHighlight() 
	{
		return highlight;
	}

	/**
	 * This method sets the highlight boolean.
	 */
	public void setHighlight(boolean highlight) 
	{
		this.highlight = highlight;
	}

	/**
	 * This method returns the color of highlight.
	 */
	public Color getHighlightColor() 
	{
		return highlightColor;
	}

	/**
	 * This method sets the highlight color.
	 */
	public void setHighlightColor(Color highlightColor) 
	{
		this.highlightColor = highlightColor;
	}
	
	/**
	 * This method get the list of the points in PointList type
	 */
	public PointList getPointList()
	{
		ArrayList<PointD> polygon = this.getPolygon();
		PointList pl = new PointList();
		for ( PointD ptD : polygon )
		{
			pl.addPoint(new Point(ptD.getX(), ptD.getY()));
		}
		
		return pl;
	}

// -----------------------------------------------------------------------------
// Section: Other methods
// -----------------------------------------------------------------------------
	private Color randomColor()
	{
		Random rand = new Random();
		int r = Math.abs(rand.nextInt()) % 256;
		int g = Math.abs(rand.nextInt()) % 256;
		int b = Math.abs(rand.nextInt()) % 256;
		
		return new Color(null, r, g, b);
	}

}
