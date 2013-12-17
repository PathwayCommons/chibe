package org.gvt.model;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

import org.gvt.LayoutManager;
import org.ivis.layout.*;

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

	protected List<NodeModel> children;

	int labelHeight;

	/*
	 * Cluster Manager of all graphs managed by this graph manager
	 */
	private EClusterManager eClusterManager;

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
		this.children = new ArrayList<NodeModel>();
		this.labelHeight = LABEL_HEIGHT;
	}

	public CompoundModel(Point pt, Color c, String lbl)
	{
		this(new Rectangle(pt, CompoundModel.DEFAULT_SIZE));
		setColor(c);
		setText(lbl);
	}

	public int getLabelHeight()
	{
		return labelHeight;
	}

	public void setLabelHeight(int labelHeight)
	{
		this.labelHeight = labelHeight;
	}

	/**
	 * This method returns the cluster manager of all graphs managed by this
	 * graph manager.
	 */
	public EClusterManager getClusterManager()
	{
		return this.eClusterManager;
	}

	/**
	 * This method sets the cluster manager of all graphs managed by this
	 * graph manager.
	 */
	public void setClusterManager(EClusterManager eClusterManager)
	{
		this.eClusterManager = eClusterManager;
	}

	public void update(LGraphObject lGraphObj)
	{
		// this node is a compound, so calculate its size
		this.calculateSizeUp();

		// if this is the root model then animate the graph
		if (this.isRoot())
		{
			LayoutManager.getInstance().animate();
		}
		else
		// otherwise input graph object must be an LNode
		{
			LNode lNode = (LNode) lGraphObj;

			// in case it's an empty compound, update its location as well
			if (lNode.getChild() == null || lNode.getChild().getNodes().size() == 0)
			{
				this.setLocationAbs(
					new Point(lNode.getRect().x,  lNode.getRect().y));
			}
		}
	}

	public void addChild(NodeModel node)
	{
		this.children.add(node);
		firePropertyChange(P_CHILDREN, -1, node);
	}

	public List getChildren()
	{
		return children;
	}

	public void removeChild(Object o)
	{
		if (o instanceof NodeModel)
		{
			NodeModel nodeModel = (NodeModel) o;
			nodeModel.resetClusters();
		}

		this.children.remove(o);
		firePropertyChange(P_CHILDREN, o, null);
	}

	public void setParentModel(CompoundModel parent)
	{
		super.setParentModel(parent);

		// if a parent of a model is set as null, do not update ClusterManager
		if (parent != null)
		{
			setClusterManager(parent.getClusterManager());
		}
	}

	/**
	 *  This method calculates sizes of children recursively and then
	 *  calculates its own size.
	 */
	public void calculateSizeDown()
	{

		// First, recursively calculate sizes of children compounds
		Iterator iter = this.children.iterator();

		while (iter.hasNext())
		{
			NodeModel child = (NodeModel) iter.next();

			if (child instanceof CompoundModel)
			{
				((CompoundModel) child).calculateSizeDown();
			}
		}

		if (getParentModel() != null && !isRoot)
		{
			// Second, calculate size of this compound model
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
					bound.height + (2 * this.MARGIN_SIZE) + this.labelHeight));

				iter = this.children.iterator();

				while (iter.hasNext())
				{
					NodeModel child = (NodeModel) iter.next();
					child.setLocationAbs(child.getLocationAbs().translate(
						diff.width + this.MARGIN_SIZE,
						diff.height + this.MARGIN_SIZE));
				}
			}
		}
	}

	/**
	 * This method calculates the size of this compound model and then
	 * recursively calculates the size of its parent, which continues
	 * until root.
	 */
	public void calculateSizeUp()
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
					bound.height + (2 * this.MARGIN_SIZE) + this.labelHeight));

				Iterator iter = this.children.iterator();

				while (iter.hasNext())
				{
					NodeModel child = (NodeModel) iter.next();
					child.setLocationAbs(child.getLocationAbs().translate(
						diff.width + this.MARGIN_SIZE,
						diff.height + this.MARGIN_SIZE));
				}
			}

			(getParentModel()).calculateSizeUp();
		}

		updatePolygonsOfChildren();
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

		root.calculateSizeUp();
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
		if(this.eClusterManager == null )
		{
			this.eClusterManager = new EClusterManager();
		}
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
// Section: Implementation of Clustered Interface
// -----------------------------------------------------------------------------
	/**
	 * This method add this compound model into a cluster with given cluster ID.
	 * If such cluster doesn't exist in ClusterManager, it creates a new cluster.
	 * This is done by calling super method. Since this is a compound model,
	 * this operation is done recursively for every child.
	 */
	public void addCluster(int clusterID)
	{
		super.addCluster(clusterID);

		// It is guaranteed here that a cluster with clusterID exists.
		// Therefore, instead of iterating for each child, use the other
		// addCluster(cluster) method for recursion.
		Cluster cluster = this.eClusterManager.getClusterByID(clusterID);

		//get all children node models
		List childrenNodes = this.children;

		Iterator itr = childrenNodes.iterator();

		// iterate over each child node model
		while (itr.hasNext() )
		{
			NodeModel childModel = (NodeModel) itr.next();

			// recursively add children node models to the cluster
			childModel.addCluster(cluster);
		}
	}

	/**
	 * This method adds the given cluster into cluster list of this compound
	 * model, and moreover it adds this compound model into set of clustered
	 * nodes of the given cluster. This is done by calling super method. Since
	 * this is a compound model, this operation is done recursively.
	 */
	public void addCluster(Cluster cluster)
	{
		// call super method
		super.addCluster(cluster);

		// get all children node models
		List childrenNodes = this.children;

		Iterator itr = childrenNodes.iterator();

		// iterate over each child node model
		while ( itr.hasNext() )
		{
			NodeModel childModel = (NodeModel) itr.next();

			// recursively add children node models to the cluster
			childModel.addCluster(cluster);
		}
	}

	/**
	 * This method removes the given cluster from the cluster list of this
	 * compound model, and moreover it removes this compound model from the
	 * set of clustered nodes of the given cluster. This is done by calling
	 * super method. Since this is a compound model, this operation is done
	 * recursively.
	 */
	public void removeCluster(Cluster cluster)
	{
		// call super method
		super.removeCluster(cluster);

		// get all children node models
		List childrenNodes = this.children;

		Iterator itr = childrenNodes.iterator();

		// iterate over each child node model
		while ( itr.hasNext() )
		{
			NodeModel childModel = (NodeModel) itr.next();

			// recursively remove children node models from the cluster
			childModel.removeCluster(cluster);
		}
	}

	public void resetClusters()
	{
		List<Cluster> clusters = new ArrayList<Cluster>();
		clusters.addAll(this.clusters);

		super.resetClusters();

		List childrenNodes = this.children;

		Iterator itr = childrenNodes.iterator();

		while ( itr.hasNext() )
		{
			NodeModel childModel = (NodeModel) itr.next();

			for (Cluster cluster : clusters)
			{
				childModel.removeCluster(cluster);
			}
		}
	}

	protected void updatePolygonsOfChildren()
	{
		super.updatePolygonsOfChildren();

		// Call recursively all children
		Iterator<NodeModel> childrenIter = this.children.iterator();

		while ( childrenIter.hasNext() )
		{
			NodeModel childModel = childrenIter.next();

			childModel.updatePolygonsOfChildren();
		}
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