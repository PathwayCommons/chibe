package org.gvt.model;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.gvt.ChisioMain;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a node in a graph.  A node can have its own color,
 * border color, label/text, source and target edges.
 *
 * @author Cihan Kucukkececi
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class NodeModel extends GraphObject
{
	private CompoundModel parentModel = null;

	protected Rectangle constraint = new Rectangle();

	protected String shape;

	protected Color borderColor;

	protected List sourceConnections = new ArrayList();

	protected List targetConnections = new ArrayList();
	
	public static String[] shapes = {
		"Rectangle",
		"RoundRect",
		"Ellipse",
		"Triangle",
		"Diamond",
		"RoundRectWithInfo"};

	public NodeModel()
	{
		this(new Rectangle(new Point(0, 0), NodeModel.DEFAULT_SIZE));
	}

	public NodeModel(Point pt)
	{
		this(new Rectangle(pt, NodeModel.DEFAULT_SIZE));
	}

	public NodeModel(Rectangle rect)
	{
		this.setConstraint(rect);
		this.text = DEFAULT_TEXT;
		this.textFont = DEFAULT_TEXT_FONT;
		this.textColor = DEFAULT_TEXT_COLOR;
		this.color = DEFAULT_COLOR;
		this.borderColor = DEFAULT_BORDER_COLOR;
		this.shape = DEFAULT_SHAPE;
	}

	public void setConstraint(Rectangle rectangle)
	{
		this.constraint.setLocation(rectangle.getLocation());
		this.constraint.setSize(rectangle.getSize());
		firePropertyChange(P_CONSTRAINT, null, this.constraint);
	}

	public Rectangle getConstraint()
	{
		return this.constraint;
	}

	public void addSourceConnection(EdgeModel connx)
	{
		this.sourceConnections.add(connx);
		firePropertyChange(P_CONNX_SOURCE, null, null);
	}

	public void addTargetConnection(EdgeModel connx)
	{
		this.targetConnections.add(connx);
		firePropertyChange(P_CONNX_TARGET, null, null);
	}

	public void removeSourceConnection(EdgeModel connx)
	{
		this.sourceConnections.remove(connx);
		firePropertyChange(P_CONNX_SOURCE, null, null);
	}

	public void removeTargetConnection(EdgeModel connx)
	{
		this.targetConnections.remove(connx);
		firePropertyChange(P_CONNX_TARGET, null, null);
	}

	public List getSourceConnections()
	{
		return this.sourceConnections;
	}

	public List getTargetConnections()
	{
		return this.targetConnections;
	}

	public String getShape()
	{
		return this.shape;
	}

	public void setShape(String s)
	{
		this.shape = s;
		firePropertyChange(P_SHAPE, null, this.shape);
	}

	public void setBorderColor(Color c)
	{
		this.borderColor = c;
		firePropertyChange(P_BORDERCOLOR, null, this.borderColor);
	}

	public Color getBorderColor()
	{
		return this.borderColor;
	}
	
	public NodeModel(Point pt, String str, Color c, String lbl)
	{
		this(new Rectangle(pt, NodeModel.DEFAULT_SIZE));

		this.setShape(str);
		setColor(c);
		setText(lbl);
	}

	public CompoundModel getParentModel()
	{
		return this.parentModel;
	}

	public void setParentModel(CompoundModel parent)
	{
		this.parentModel = parent;
	}

	/**
	 * This method sets the location of this graph object relative to its
	 * container, if any.
	 *
	 * @param p the new location of this graph object
	 */
	public void setLocation(Point p)
	{
		this.constraint.setLocation(p);
		firePropertyChange(P_CONSTRAINT, null, constraint);
	}

	/**
	 * This method sets the absolute location of this graph object; that is,
	 * w.r.t the root container as opposed to the parent container.
	 *
	 * @param p the new location of this graph object
	 */
	public void setLocationAbs(Point p)
	{
		CompoundModel parent = this.getParentModel();

		if (parent != null && parent instanceof CompoundModel)
		{
			Point parentLocation = parent.getLocationAbs();
			p.translate(-parentLocation.x, -parentLocation.y);
		}

		this.setLocation(p);
	}

	/**
	 * This method gets the current location of this graph object relative to
	 * its parent container.
	 *
	 * @return current location relative to its parent
	 */
	public Point getLocation()
	{
		return this.constraint.getLocation();
	}

	/**
	 * This method gets the current absolute location of this graph object; that
	 * is, w.r.t. the root container as opposed to the parent container.
	 *
	 * @return current absolute location
	 */
	public Point getLocationAbs()
	{
		Point location = this.constraint.getLocation();
		CompoundModel parent = this.getParentModel();

		if (parent != null && parent instanceof CompoundModel)
		{
			location.translate(parent.getLocationAbs());
		}

		return location;
	}

	public void setSize(Dimension d)
	{
		this.constraint.setSize(d);
		firePropertyChange(P_CONSTRAINT, null, constraint);
	}

	public Dimension getSize()
	{
		return this.constraint.getSize();
	}

	public List getEdgeListToNode(NodeModel to)
	{
		List<EdgeModel> edgeList = new ArrayList();

		if (sourceConnections != null)
		{
			int i = 0;

			while (i < sourceConnections.size())
			{
				EdgeModel edge = (EdgeModel) sourceConnections.get(i++);

				if (edge.getTarget() == to)
				{
					edgeList.add(edge);
				}
			}
		}

		return edgeList;
	}

	public boolean isNeighbor(NodeModel node)
	{
		if(sourceConnections.contains(node) || targetConnections.contains(node))
		{
			return true;
		}

		return false;
	}
	public List<NodeModel> getNeighborsList()
	{
		List<NodeModel> neigbors = new ArrayList();

		for (int i = 0 ; i < this.sourceConnections.size() ; i++)
		{
			EdgeModel edge = (EdgeModel) this.sourceConnections.get(i);
			neigbors.add(edge.getTarget());
		}

		for (int i = 0 ; i < this.targetConnections.size() ; i++)
		{
			EdgeModel edge = (EdgeModel) this.targetConnections.get(i);
			neigbors.add(edge.getSource());
		}

		return neigbors;
	}

	public int getLeft()
	{
		return this.constraint.x;
	}

	public int getRight()
	{
		return this.constraint.x + this.constraint.width;
	}

	public int getTop()
	{
		return this.constraint.y;
	}

	public int getBottom()
	{
		return this.constraint.y + this.constraint.height;
	}

	public void setPositiveLocation(Rectangle r)
	{
		this.setConstraint(r);
		Point loc = this.getLocationAbs();

		if (loc.x < 0)
		{
			loc.x = 0;
		}

		if (loc.y < 0)
		{
			loc.y = 0;
		}

		this.setLocationAbs(loc);
	}

// -----------------------------------------------------------------------------
// Section: Class Variables
// -----------------------------------------------------------------------------
	public static Dimension DEFAULT_SIZE = new Dimension(40, 40);

	public static String DEFAULT_TEXT = "Node";

	public static Font DEFAULT_TEXT_FONT =
		new Font(null, new FontData("Arial", 8, SWT.NORMAL));

	public static Color DEFAULT_TEXT_COLOR = ColorConstants.black;

	public static Color DEFAULT_COLOR = new Color(null, 14, 112, 130);

	public static Color DEFAULT_BORDER_COLOR = new Color(null, 14, 112, 130);

	public static String DEFAULT_SHAPE = shapes[0];

// -----------------------------------------------------------------------------
// Section: Class Constants
// -----------------------------------------------------------------------------
	public static final String P_CONSTRAINT = "_constraint";

	public static final String P_SHAPE = "_shape";

	public static final String P_BORDERCOLOR = "_borderColor";

	public static final String P_CONNX_SOURCE = "_connx_source";

	public static final String P_CONNX_TARGET = "_connx_target";
}