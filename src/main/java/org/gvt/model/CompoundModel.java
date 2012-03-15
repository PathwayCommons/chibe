package org.gvt.model;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This class implements a compound structure which can include a list of
 * children (nodes and edges). Each compound node maintains a label and margins.
 *
 * @author Cihan Kucukkececi
 * 
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class CompoundModel extends NodeModel
{
	private boolean isRoot = false;

	private boolean isDirected = false;

	protected List children;

	public CompoundModel()
	{
		this(new Rectangle(new Point(0, 0), CompoundModel.DEFAULT_SIZE));
	}

	public CompoundModel(Point pt)
	{
		this(new Rectangle(pt, CompoundModel.DEFAULT_SIZE));
	}

	public CompoundModel(Rectangle rect)
	{
		setConstraint(rect);
		this.text = DEFAULT_TEXT;
		this.textFont = DEFAULT_TEXT_FONT;
		this.textColor = DEFAULT_TEXT_COLOR;
		this.color = DEFAULT_COLOR;
		this.borderColor = DEFAULT_BORDER_COLOR;
		this.shape = DEFAULT_SHAPE;
		this.children = new ArrayList();
	}

	public CompoundModel(Point pt, Color c, String lbl)
	{
		this(new Rectangle(pt, CompoundModel.DEFAULT_SIZE));
		setColor(c);
		setText(lbl);
	}

	public void addChild(Object o)
	{
		this.children.add(o);
		firePropertyChange(P_CHILDREN, -1, o);
	}

	public List getChildren()
	{
		return children;
	}

	public void removeChild(Object o)
	{
		this.children.remove(o);
		firePropertyChange(P_CHILDREN, o, null);
	}

	public void calculateSize()
	{
		if (getParentModel() != null && !isRoot)
		{
			if (this.children.size() == 0)
			{
				setSize(CompoundModel.DEFAULT_SIZE);
			}
			else
			{
				Rectangle bound = calculateBounds();
				Dimension diff =
					getLocationAbs().getDifference(bound.getLocation());

				setLocationAbs(new Point(bound.x - this.MARGIN_SIZE,
					bound.y - this.MARGIN_SIZE));
				setSize(new Dimension(bound.width + (2 * this.MARGIN_SIZE),
					bound.height + (2 * this.MARGIN_SIZE) + this.LABEL_HEIGHT));

				Iterator iter = this.children.iterator();

				while (iter.hasNext())
				{
					NodeModel child = (NodeModel) iter.next();
					child.setLocationAbs(child.getLocationAbs().translate(
						diff.width + this.MARGIN_SIZE,
						diff.height + this.MARGIN_SIZE));
				}
			}

			(getParentModel()).calculateSize();
		}
	}

	public void setMarginSize(int margin)
	{
		this.MARGIN_SIZE = margin;
		CompoundModel root = this;

		while (root.getParentModel() != null)
		{
			root = root.getParentModel();
		}

		for (Object o : root.getChildren())
		{
			if (o instanceof CompoundModel)
			{
				updateMarginSize((CompoundModel) o);
			}
		}
	}

	private void updateMarginSize(CompoundModel root)
	{
		for (Object o : root.getChildren())
		{
			GraphObject child = (GraphObject) o;

			if (child instanceof CompoundModel)
			{
				updateMarginSize((CompoundModel) child);
			}
		}

		root.calculateSize();
	}

	public Iterator getEdgeIterator(int edgeType,
		boolean isRecursive,
		boolean onlyEndsWithinRoot)
	{
		return
			new EdgeIterator(this, edgeType, isRecursive, onlyEndsWithinRoot);
	}

	public void setAsRoot()
	{
		this.isRoot = true;
	}

	public boolean isRoot()
	{
		return this.isRoot;
	}

	public boolean isDirected()
	{
		return this.isDirected;
	}

	public void setDirected(boolean directed)
	{
		this.isDirected = directed;
	}

	public Set getEdges()
	{
		return ((EdgeIterator)
			getEdgeIterator(CompoundModel.ALL_EDGES, true, false)).getEdges();
	}

	public Set getNodes()
	{
		return ((NodeIterator) getNodeIterator(true)).getNodes();
	}

	public Iterator getNodeIterator(boolean isRecursive)
	{
		return (new NodeIterator(this, isRecursive));
	}

	public boolean isAncestorofNode(NodeModel node)
	{
		CompoundModel root = node.getParentModel();

		while (root != null)
		{
			if(root == this)
			{
				return true;
			}
			else
			{
				root = root.getParentModel();
			}
		}

		return false;
	}

	/**
	 * This method finds the boundaries of this compound node.
	 *
	 */
	public Rectangle calculateBounds()
	{
		int top = Integer.MAX_VALUE;
		int left = Integer.MAX_VALUE;
		int right = 0;
		int bottom = 0;
		int nodeTop;
		int nodeLeft;
		int nodeRight;
		int nodeBottom;

		/* Checks the nodes' locations which are children of
		 * this compound node.
		 */
		Iterator itr = this.getChildren().iterator();

		while (itr.hasNext())
		{
			NodeModel node = (NodeModel) itr.next();

			Point locAbs = node.getLocationAbs();
			Dimension size = node.getSize();
			nodeTop = locAbs.y;
			nodeLeft = locAbs.x;
			nodeRight = locAbs.x + size.width;
			nodeBottom = locAbs.y + size.height;

			if (top > nodeTop)
			{
				top = nodeTop;
			}

			if (left > nodeLeft)
			{
				left = nodeLeft;
			}

			if (right < nodeRight)
			{
				right = nodeRight;
			}

			if (bottom < nodeBottom)
			{
				bottom = nodeBottom;
			}
		}

		// Checks the bendpoints' locations.
		if (this.isRoot)
		{
			itr = this.getEdges().iterator();
		}
		else
		{
			itr = this.getEdgeIterator(ALL_EDGES, true, true);
		}

		while (itr.hasNext())
		{
			EdgeModel edge = (EdgeModel) itr.next();
			edge.updateBendpoints();

			for (int i = 0; i < edge.bendpoints.size(); i++)
			{
				EdgeBendpoint eb = (EdgeBendpoint) edge.bendpoints.get(i);
				Point loc = eb.getLocationFromModel(edge);

				int x = loc.x;
				int y = loc.y;

				if (top > y)
				{
					top = y;
				}

				if (left > x)
				{
					left = x;
				}

				if (right < x)
				{
					right = x;
				}

				if (bottom < y)
				{
					bottom = y;
				}
			}
		}

		// Do we have any nodes in this graph?
		if (top == Double.MAX_VALUE)
		{
			return null;
		}

		return new Rectangle(left, top, right - left, bottom - top);
	}

// -----------------------------------------------------------------------------
// Section: Class Variables
// -----------------------------------------------------------------------------
	public static Dimension DEFAULT_SIZE = new Dimension(40, 40);

	public static String DEFAULT_TEXT = "Compound";

	public static Font DEFAULT_TEXT_FONT =
		new Font(null, new FontData("Arial", 8, SWT.NORMAL));

	public static Color DEFAULT_TEXT_COLOR = ColorConstants.white;

	public static Color DEFAULT_COLOR = new Color(null, 177, 83, 112);

	public static Color DEFAULT_BORDER_COLOR = new Color(null, 177, 83, 112);

	public static int MARGIN_SIZE = 10;

	public static int LABEL_HEIGHT = 20;

	public static int DEFAULT_CLUSTER_ID = 0;

// -----------------------------------------------------------------------------
// Section: Class Constants
// -----------------------------------------------------------------------------
	public static final int ALL_EDGES = 0;

	public static final int INTRA_GRAPH_EDGES = 1;

	public static final int INTER_GRAPH_EDGES = 2;

	public static final String P_CHILDREN = "_children";
}