package org.gvt.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Bendpoint;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.SWT;

import org.ivis.layout.LEdge;
import org.ivis.layout.LGraphObject;
import org.ivis.layout.Updatable;
import org.ivis.util.DimensionD;
import org.ivis.util.PointD;

/**
 * This class implements an edge in a graph. An edge can have its own color,
 * label/text, a style (solid/dashed) and an arrow(none/source/target/both).
 * 
 * @author Cihan Kucukkececi
 * 
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class EdgeModel extends GraphObject implements Updatable
{
	private NodeModel sourceNode;

	private NodeModel targetNode;

	protected String style;

	protected String arrow;

	protected int width;

	protected List bendpoints;

	/**
	 * Constructor
	 */
	public EdgeModel()
	{
		super();
		this.text = DEFAULT_TEXT;
		this.textFont = DEFAULT_TEXT_FONT;
		this.textColor = DEFAULT_TEXT_COLOR;
		this.color = DEFAULT_COLOR;
		this.style = DEFAULT_STYLE;
		this.arrow = DEFAULT_ARROW;
		this.width = DEFAULT_WIDTH;
		this.bendpoints = new ArrayList();
	}

	public void update(LGraphObject lGraphObj)
	{
		// Since this is the update method of a v-level edge, it is assumed
		// that the given LGraphObject is an instance of LEdge. So, cast
		// operation is performed without type checking.
		LEdge lEdge = (LEdge) lGraphObj;

		// update bendpoints of the edge by using the l-level edge's
		// bendpoint information

		List bendpoints = new ArrayList();

		for(PointD p: lEdge.getBendpoints())
		{
			PointD sourceLoc = new PointD(lEdge.getSource().getCenterX(),
				lEdge.getSource().getCenterY());

			PointD targetLoc = new PointD(lEdge.getTarget().getCenterX(),
				lEdge.getTarget().getCenterY());

			// When x == y this means that the layout does not support bendpoints, and the
			// bendpoints still have the indexes we originally assigned. Here we handle them.

			if (p.getX() == p.getY())
			{
				double difX = sourceLoc.getX() - targetLoc.getX();
				double difY = sourceLoc.getY() - targetLoc.getY();

				double lengthSq = (difX * difX) + (difY * difY);

				double xShare = (difX * difX) / lengthSq;
				double yShare = 1 - xShare;

				double xx = Math.sqrt((lengthSq / 800) * yShare) * Math.signum(difY);
				double yy = Math.sqrt((lengthSq / 800) * xShare) * Math.signum(difX);

				int index = (int) p.getX();

				if (index < 0)
				{
					xx *= -1;
					yy *= -1;
					index *= -1;
				}

				if (index % 2 == 1)
				{
					xx *= -1;
					yy *= -1;
				}

				// don't forget below are integer divisions
				xx *= ((index / 2) * 2) + 1;
				yy *= ((index / 2) * 2) + 1;

				double centerX = sourceLoc.getX() + targetLoc.getX();
				double centerY = sourceLoc.getY() + targetLoc.getY();

				p.setX((centerX / 2) - xx);
				p.setY((centerY / 2) + yy);
			}

			DimensionD dim1 = p.getDifference(sourceLoc);
			DimensionD dim2 = p.getDifference(targetLoc);

			EdgeBendpoint ebp = new EdgeBendpoint();
			ebp.setRelativeDimensions(
				new Dimension((int)(dim1.width), (int)(dim1.height)),
				new Dimension((int)(dim2.width), (int)(dim2.height)));

			bendpoints.add(ebp);
		}

		this.setBendpoints(bendpoints);
	}

	public String getStyle()
	{
		return this.style;
	}

	public void setStyle(String s)
	{
		this.style = s;
		firePropertyChange(P_STYLE, null, style);
	}

	public String getArrow()
	{
		return this.arrow;
	}

	public void setArrow(String a)
	{
		this.arrow = a;
		firePropertyChange(P_ARROW, null, arrow);
	}

	public int getWidth()
	{
		return this.width;
	}

	public void setWidth(int w)
	{
		this.width = w;
		firePropertyChange(P_WIDTH, null, width);
	}

	public NodeModel getSource()
	{
		return this.sourceNode;
	}

	public void setSource(NodeModel source)
	{
		this.sourceNode = source;
	}

	public NodeModel getTarget()
	{
		return this.targetNode;
	}

	public void setTarget(NodeModel target)
	{
		this.targetNode = target;
	}

	public void detachSource()
	{
		this.sourceNode.removeSourceConnection(this);
	}

	public void detachTarget()
	{
		this.targetNode.removeTargetConnection(this);
	}

	public void attachSource()
	{
		if (!this.sourceNode.getSourceConnections().contains(this))
			this.sourceNode.addSourceConnection(this);
	}

	public void attachTarget()
	{
		if (!this.targetNode.getTargetConnections().contains(this))
			this.targetNode.addTargetConnection(this);
	}

	public boolean isIntragraph()
	{
		return getSource().getParentModel() == getTarget().getParentModel();
	}

	public List getBendpoints()
	{
		return this.bendpoints;
	}

	public void insertBendpoint(int index, Bendpoint point)
	{
		getBendpoints().add(index, point);
		firePropertyChange(P_BENDPOINT, null, null);
	}

	public void insertBendpoint(Bendpoint point)
	{
		getBendpoints().add(point);
		firePropertyChange(P_BENDPOINT, null, null);
	}

	public void removeBendpoint(int index)
	{
		getBendpoints().remove(index);
		firePropertyChange(P_BENDPOINT, null, null);
	}

	public void setBendpoint(int index, Bendpoint point)
	{
		getBendpoints().set(index, point);
		firePropertyChange(P_BENDPOINT, null, null);
	}

	public void setBendpoints(List points)
	{
		this.bendpoints = points;
		firePropertyChange(P_BENDPOINT, null, null);
	}

	public void removeAllBendpoints()
	{
		this.bendpoints.clear();
		firePropertyChange(P_BENDPOINT, null, null);
	}
	
	public void updateBendpoints()
	{
		firePropertyChange(P_BENDPOINT, null, null);
	}

// -----------------------------------------------------------------------------
// Section: Class Variables
// -----------------------------------------------------------------------------
	public static String DEFAULT_TEXT = "";

	public static Font DEFAULT_TEXT_FONT =
		new Font(null, new FontData("Arial", 8, SWT.NORMAL));

	public static Color DEFAULT_TEXT_COLOR = ColorConstants.black;

	public static Color DEFAULT_COLOR = ColorConstants.black;

	public static String DEFAULT_ARROW = "None";

	public static String DEFAULT_STYLE = "Solid";

	public static int DEFAULT_WIDTH = 1;

// -----------------------------------------------------------------------------
// Section: Class Constants
// -----------------------------------------------------------------------------
	public static final String P_STYLE = "_style";

	public static final String P_ARROW = "_arrow";

	public static final String P_WIDTH = "_width";

	public static final String P_BENDPOINT = "_bendpoint";
}