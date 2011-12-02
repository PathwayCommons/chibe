package org.gvt.layout;

import java.util.*;

import org.eclipse.draw2d.geometry.*;
import org.gvt.model.CompoundModel;

/**
 * This class represents a graph (l-level) for layout purposes. An l-level
 * graph is either the root of a compound graph structure or child of an
 * l-level compound node.
 *
 * @author Erhan Giral
 * @author Ugur Dogrusoz
 * @author Cihan Kucukkececi
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class LGraph
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/**
	 * Nodes maintained by this graph
	 */
	private List nodes;

	/**
	 * Edges whose source nodes are in this graph including inter-graph edges.
	 */
	private List edges;

	/**
	 * Owner graph manager
	 */
	private LGraphManager graphManager;

	/**
	 * Parent node of this graph, in case this graph is a child graph of an
	 * expanded node
	 */
	private LNode parent;

	/**
	 * Geometry of this graph (i.e. that of its bounding rectangle)
	 */
	private int top;
	private int left;
	private int bottom;
	private int right;

	protected int estimatedSize = 0;

// -----------------------------------------------------------------------------
// Section: Constructors and Initialization
// -----------------------------------------------------------------------------
	public LGraph(LNode parent, LGraphManager graphMgr)
	{
		this.edges = new ArrayList();
		this.nodes = new ArrayList();
		this.graphManager = graphMgr;
		this.parent = parent;
	}

// -----------------------------------------------------------------------------
// Section: Accessors
// -----------------------------------------------------------------------------
	public List getNodes()
	{
		return nodes;
	}

	public void setNodes(List nodes)
	{
		this.nodes = nodes;
	}

	public List getEdges()
	{
		return edges;
	}

	public void setEdges(List edges)
	{
		this.edges = edges;
	}

	public LGraphManager getGraphManager()
	{
		return graphManager;
	}

	public void setGraphManager(LGraphManager graphManager)
	{
		this.graphManager = graphManager;
	}

	public LNode getParent()
	{
		return parent;
	}

	public void setParent(LNode parent)
	{
		this.parent = parent;
	}

	public int getLeft()
	{
		return this.left;
	}

	public int getRight()
	{
		return this.right;
	}

	public int getTop()
	{
		return this.top;
	}

	public int getBottom()
	{
		return this.bottom;
	}

// -----------------------------------------------------------------------------
// Section: Remaining Methods
// -----------------------------------------------------------------------------
	/**
	 * This method calculates, updates and returns the left-top point of this
	 * graph including margins.
	 */
	public Point updateLeftTop()
	{
		int top = Integer.MAX_VALUE;
		int left = Integer.MAX_VALUE;
		int nodeTop;
		int nodeLeft;

		Iterator itr = this.getNodes().iterator();

		while (itr.hasNext())
		{
			LNode lNode = (LNode) itr.next();
			nodeTop = (int)(lNode.getTop());
			nodeLeft = (int)(lNode.getLeft());

			if (top > nodeTop)
			{
				top = nodeTop;
			}

			if (left > nodeLeft)
			{
				left = nodeLeft;
			}
		}

		// Do we have any nodes in this graph?
		if (top == Integer.MAX_VALUE)
		{
			return null;
		}

		this.left = left - graphMargin;
		this.top =  top - graphMargin;

		// Apply the margins and return the result
		return new Point(this.left, this.top);
	}

	/**
	 * This method calculates and updates the bounds of this graph including
	 * margins.
	 */
	public void updateBounds()
	{
		Rectangle boundingRect = this.calculateBounds(this.getNodes());

		// Do we have any nodes in this graph?
		if (left == Integer.MAX_VALUE)
		{
			this.left =  (int)(this.parent.getLeft());
			this.right = (int)(this.parent.getRight());
			this.top =  (int)(this.parent.getTop());
			this.bottom = (int)(this.parent.getBottom());
		}

		this.left = boundingRect.x - graphMargin;
		this.right = boundingRect.right() + graphMargin;
		this.top =  boundingRect.y - graphMargin;
		// Label text dimensions are to be added for the bottom of the compound!
		this.bottom = boundingRect.bottom() + graphMargin;
	}

	/**
	 * This method returns the bounding rectangle of the given list of nodes. No
	 * margins are accounted for, and it returns a rectangle with top-left set
	 * to Integer.MAX_VALUE if the list is empty.
	 */
	public static Rectangle calculateBounds(List<LNode> nodes)
	{
		int left = Integer.MAX_VALUE;
		int right = -Integer.MAX_VALUE;
		int top = Integer.MAX_VALUE;
		int bottom = -Integer.MAX_VALUE;
		int nodeLeft;
		int nodeRight;
		int nodeTop;
		int nodeBottom;

		Iterator<LNode> itr = nodes.iterator();

		while (itr.hasNext())
		{
			LNode lNode = itr.next();
			nodeLeft = (int)(lNode.getLeft());
			nodeRight = (int)(lNode.getRight());
			nodeTop = (int)(lNode.getTop());
			nodeBottom = (int)(lNode.getBottom());

			if (left > nodeLeft)
			{
				left = nodeLeft;
			}

			if (right < nodeRight)
			{
				right = nodeRight;
			}

			if (top > nodeTop)
			{
				top = nodeTop;
			}

			if (bottom < nodeBottom)
			{
				bottom = nodeBottom;
			}
		}

		Rectangle boundingRect =
			new Rectangle(left, top, right - left, bottom - top);

		return boundingRect;
	}

	/**
	 * This method returns an estimation on the total sizes of the contained
	 * nodes.
	 */
	public int getEstimatedSize()
	{
		int size = 0;
		Iterator itr = this.nodes.iterator();

		while (itr.hasNext())
		{
			LNode lNode = (LNode) itr.next();
			size += lNode.getEstimatedSize();
		}

		this.estimatedSize = size;

		if (this.estimatedSize == 0)
		{
			this.estimatedSize = EMPTY_COMPOUND_NODE_SIZE;
		}

		return this.estimatedSize;
	}

	/**
	 * This method scatters the non-compound nodes in this graph for random
	 * initial positioning before layout is performed.
	 */
	public void scatterNodes()
	{
		Iterator itr = this.nodes.iterator();

		while (itr.hasNext())
		{
			LNode lNode = (LNode) itr.next();

			// We only scatter leaf nodes, compound ones get their geometry from
			// their content

			if (lNode.getChild() == null)
			{
				lNode.scatter();
			}
		}
	}

// -----------------------------------------------------------------------------
// Section: Class Methods
// -----------------------------------------------------------------------------
	/**
	 * This method returns the margins of l-level graphs to be applied on the
	 * bounding rectangle of its contents.
	 */
	public static int getGraphMargin()
	{
		return LGraph.graphMargin;
	}

	/**
	 * This method sets the margins of l-level graphs to be applied on the
	 * bounding rectangle of its contents.
	 */
	public static void setGraphMargin(int margin)
	{
		LGraph.graphMargin = margin;
	}

// -----------------------------------------------------------------------------
// Section: Class Variables
// -----------------------------------------------------------------------------
	/**
	 * Margins of this graph to be applied on bouding rectangle of its contents
	 */
	protected static int graphMargin = CompoundModel.MARGIN_SIZE;

	/**
	 * Dimensions of an empty compound node
	 */
	private static final int EMPTY_COMPOUND_NODE_SIZE =
		CompoundModel.DEFAULT_SIZE.width;
}