package org.gvt.layout;

import java.util.*;

import org.eclipse.draw2d.geometry.*;
import org.eclipse.jface.util.Assert;
import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;
import org.gvt.util.ChsTransform;
import org.gvt.util.ChsRectangle;

/**
 * This class represents a node (l-level) for layout purposes.
 *
 * @author Erhan Giral
 * @author Ugur Dogrusoz
 * @author Cihan Kucukkececi
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class LNode
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/**
	 * Owner graph manager of this node
	 */
	protected LGraphManager graphManager;

	/**
	 * Possibly null child graph of this node
	 */
	protected LGraph child;

	/**
	 * Owner graph of this node; cannot be null
	 */
	private LGraph owner;

	/**
	 * List of edges incident with this node
	 */
	protected List edges;

	/**
	 * Geometry of this node
	 */
	public ChsRectangle rect;

	protected int estimatedInitialSize = Integer.MIN_VALUE;

	private int inclusionTreeDepth = Integer.MAX_VALUE;

	/**
	 * Used by hashing operations in createTopology method of LGraphManager
	 */
	protected int LPID;

// -----------------------------------------------------------------------------
// Section: Constructors and Initialization
// -----------------------------------------------------------------------------
	/**
	 * Empty constructor
	 */
	public LNode()
	{
		this.initialize();
		this.graphManager = null;
		rect = new ChsRectangle();
	}

	/**
	 * Constructor
	 */
	public LNode(LGraphManager gm)
	{
		this.initialize();
		this.graphManager = gm;
		rect = new ChsRectangle();
	}

	/**
	 * Alternative constructor
	 */
	public LNode(LGraphManager gm, Point loc, Dimension size)
	{
		this.initialize();

		this.graphManager = gm;
		rect = new ChsRectangle(loc.x, loc.y, size.height, size.width);
	}

	public void initialize()
	{
		this.edges = new LinkedList();
	}

	public List getEdges()
	{
		return this.edges;
	}

	public void setEdges(List edges)
	{
		this.edges = edges;
	}

// -----------------------------------------------------------------------------
// Section: Accessors
// -----------------------------------------------------------------------------
	public LGraph getChild()
	{
		return child;
	}

	public void setChild(LGraph child)
	{
		this.child = child;
	}

	public LGraph getOwner()
	{
		return owner;
	}

	public void setOwner(LGraph owner)
	{
		this.owner = owner;
	}

	public double getWidth()
	{
		return this.rect.width;
	}

	public void setWidth(double width)
	{
		this.rect.width = width;
	}

	public double getHeight()
	{
		return this.rect.height;
	}

	public void setHeight(double height)
	{
		this.rect.height = height;
	}

	public double getLeft()
	{
		return this.rect.x;
	}

	public double getRight()
	{
		return this.rect.x + this.rect.width;
	}

	public double getTop()
	{
		return this.rect.y;
	}

	public double getBottom()
	{
		return this.rect.y + this.rect.height;
	}

	public double getCenterX()
	{
		return this.rect.x + this.rect.width / 2;
	}

	public double getCenterY()
	{
		return this.rect.y + this.rect.height / 2;
	}

	public PrecisionPoint getLocation()
	{
		return new PrecisionPoint(this.rect.x, this.rect.y);
	}

	public ChsRectangle getRect()
	{
		return this.rect;
	}

	public double getDiagonal()
	{
		return(Math.sqrt(this.rect.width * this.rect.width + this.rect.height * this.rect.height));
	}

	public void setRect(Point upperLeft, Dimension dimension)
	{
		this.rect.x = upperLeft.x;
		this.rect.y = upperLeft.y;
		this.rect.width = dimension.width;
		this.rect.height = dimension.height;
	}

	public void setCenter(double cx, double cy)
	{
		this.rect.x = cx - this.rect.width / 2;
		this.rect.y = cy - this.rect.height / 2;
	}

	public void setLocation(double x, double y)
	{
		this.rect.x = x;
		this.rect.y = y;
	}

	public void moveBy(double dx, double dy)
	{
//		if (! (this instanceof CiSEOnCircleNode))
//		System.out.printf("mb\t%s:(%5.1f,%5.1f)\n", new Object [] {this.getText(), dx, dy});

		this.rect.x += dx;
		this.rect.y += dy;
	}

	/*
	 * This method returns the name of the corresponding v-level node of this
	 * node, if any.
	 */
	public String getText()
	{
		NodeModel vNode =
			(NodeModel)this.graphManager.getLayout().viewMapLtoV.get(this);

		if (vNode != null)
		{
			return vNode.getText();
		}
		else
		{
			return null;
		}
	}

// -----------------------------------------------------------------------------
// Section: Remaining Methods
// -----------------------------------------------------------------------------
	public List getEdgeListToNode(LNode to)
	{
		List<LEdge> edgeList = new ArrayList();

		if (edges != null)
		{
			for (int i = 0; i < edges.size(); i++)
			{
				LEdge edge = (LEdge) edges.get(i);

				if (edge.source == this && edge.target == to)
				{
					edgeList.add(edge);
				}
			}
		}

		return edgeList;
	}

	/**
	 *	This method returns all edges between this node and the given node. 
	 */
	public List getEdgesBetween(LNode other)
	{
		List<LEdge> edgeList = new ArrayList();

		if (edges != null)
		{
			for (int i = 0; i < edges.size(); i++)
			{
				LEdge edge = (LEdge) edges.get(i);

				if ((edge.source == this && edge.target == other) ||
						(edge.target == this && edge.source == other))
				{
					edgeList.add(edge);
				}
			}
		}

		return edgeList;
	}

	public boolean isNeighbor(LNode node)
	{
		if (edges != null)
		{
			for (int i = 0; i < edges.size(); i++)
			{
				LEdge edge = (LEdge) edges.get(i);

				if (edge.source == node || edge.target == node)
				{
					return true;
				}
			}
		}

		return false;
	}

	public Set getNeighborsList()
	{
		Set<LNode> neighbors = new HashSet();

		for (int i = 0; i < edges.size(); i++)
		{
			LEdge edge = (LEdge) edges.get(i);

			if (edge.source.equals(this))
			{
				neighbors.add(edge.target);
			}
			else
			{
				neighbors.add(edge.source);
			}
		}

		return neighbors;
	}

	public Set getSuccessors()
	{
		Set<LNode> neighbors = new HashSet();

		for (int i = 0; i < edges.size(); i++)
		{
			LEdge edge = (LEdge) edges.get(i);

			if (edge.source.equals(this))
			{
				neighbors.add(edge.target);
			}
		}

		return neighbors;
	}

	/**
	 * This method returns the estimated size of this node, taking into account
	 * node margins and whether this node is a compound one containing others.
	 */
	public int getEstimatedSize()
	{
		if (this.estimatedInitialSize != Integer.MIN_VALUE)
		{
			return this.estimatedInitialSize;
		}
		if (this.child == null)
		{
			this.estimatedInitialSize = (int)this.rect.width;
			return this.estimatedInitialSize;
		}
		else
		{
			this.estimatedInitialSize = this.child.getEstimatedSize();
			this.rect.width = this.estimatedInitialSize;
			this.rect.height = this.estimatedInitialSize;

			return this.estimatedInitialSize;
		}
	}

	/**
	 * This method positions this node randomly in both x and y dimensions.
	 */
	protected void scatter()
	{
		// We assume the center to be at (GRAVITY_CENTER_X,GRAVITY_CENTER_Y).

		double randomCenterX;
		double randomCenterY;

		double minX = -AbstractLayout.WORLD_BOUNDARY / 10000;
		double maxX = AbstractLayout.WORLD_BOUNDARY / 10000;
		randomCenterX = AbstractLayout.GRAVITY_CENTER_X +
			(LNode.random.nextDouble() * (maxX - minX)) + minX;

		double minY = -AbstractLayout.WORLD_BOUNDARY / 10000;
		double maxY = AbstractLayout.WORLD_BOUNDARY / 10000;
		randomCenterY = AbstractLayout.GRAVITY_CENTER_Y +
			(LNode.random.nextDouble() * (maxY - minY)) + minY;

		this.rect.x = randomCenterX;
		this.rect.y = randomCenterY;
	}

	/**
	 * This method updates the bounds of this compound node.
	 */
	public void updateBounds()
	{
		assert this.getChild() != null;

		if (this.getChild().getNodes().size() != 0)
		{
			// wrap the children nodes by re-arranging the boundaries
			LGraph childGraph = this.getChild();
			childGraph.updateBounds();

			this.rect.x =  childGraph.getLeft();
			this.rect.y =  childGraph.getTop();

			this.setWidth(childGraph.getRight() - childGraph.getLeft() +
				2 * compoundNodeMargin);
			this.setHeight(childGraph.getBottom() - childGraph.getTop() +
				2 * compoundNodeMargin + CompoundModel.LABEL_HEIGHT);
		}
	}

	/**
	 * This method returns the maximum depth of this node in the inclusion tree.
	 */
	public int getInclusionTreeDepth()
	{
		if (this.inclusionTreeDepth == Integer.MAX_VALUE)
		{
			int depth = 0;
			LGraph root = this.owner.getGraphManager().getRoot();
			LGraph parent = this.owner;

			while (true)
			{
				if (parent != root)
				{
					depth++;
				}
				else
				{
					break;
				}

				parent = parent.getParent().getOwner();
			}

			depth++;
			this.inclusionTreeDepth = depth;
		}

		return this.inclusionTreeDepth;
	}

	/**
	 * This method returns all parents of this node.
	 */
	public Vector getAllParents()
	{
		Vector parents = new Vector();
		LNode rootNode = this.owner.getGraphManager().getRoot().getParent();
		LNode parent = this.owner.getParent();

		while (true)
		{
			if (parent != rootNode)
			{
				parents.add(parent);
			}
			else
			{
				break;
			}

			parent = parent.getOwner().getParent();
		}

		parents.add(rootNode);

		return parents;
	}

	public int getRootGraphIndex()
	{
		return owner.getNodes().indexOf(this);
	}

	/**
	 * This method transforms the layout coordinates of this node using input
	 * transform.
	 */
	public void transform(ChsTransform trans)
	{
		double left = this.rect.x;

		if (left > AbstractLayout.WORLD_BOUNDARY)
		{
			left = AbstractLayout.WORLD_BOUNDARY;
		}
		else if (left < -AbstractLayout.WORLD_BOUNDARY)
		{
			left = -AbstractLayout.WORLD_BOUNDARY;
		}

		double top = this.rect.y;

		if (top > AbstractLayout.WORLD_BOUNDARY)
		{
			top = AbstractLayout.WORLD_BOUNDARY;
		}
		else if (top < -AbstractLayout.WORLD_BOUNDARY)
		{
			top = -AbstractLayout.WORLD_BOUNDARY;
		}

		PrecisionPoint leftTop = new PrecisionPoint(left, top);
		PrecisionPoint vLeftTop = trans.inverseTransformPoint(leftTop);

		this.setLocation(vLeftTop.preciseX, vLeftTop.preciseY);
	}

// -----------------------------------------------------------------------------
// Section: Class Variables
// -----------------------------------------------------------------------------
	/**
	 * Compound nodes normally have no margins in Chisio but we'll maintain some
	 * safety buffer for node-node overlaps. Labels are accounted for
	 * separately. Notice that graph margins are also handled separately!
	 */
	public static int compoundNodeMargin  = 5;

	/**
	 * Nodes are assumed to have this margin when calculating sizes.
	 */
	public static final int NODE_MARGIN = 4;

	/**
	 * Used for random initial positioning
	 */
	private static Random random = new Random();
}