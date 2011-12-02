package org.gvt.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.gvt.ChisioMain;
import org.gvt.GraphAnimation;
import org.gvt.editpart.ChsScalableRootEditPart;
import org.gvt.figure.HighlightLayer;
import org.gvt.model.*;
import org.gvt.util.ChsTransform;

import java.util.*;

/**
 * This class lays out the graph model rooted at the Chisio model passed into
 * the constructor by first constructing an l-level model (LGraphManager,
 * LGraph, LNode and LEdge) and copying the results back to the input Chisio
 * model when finished.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public abstract class AbstractLayout
{
// -----------------------------------------------------------------------------
// Section: Instance Variables
// -----------------------------------------------------------------------------

	ChisioMain main;
	protected ScrollingGraphicalViewer viewer;

	/**
	 * Geometric abstraction of the compound graph
	 */
	protected LGraphManager lGraphManager;

	/**
	 * Nesting levels of the compound structure
	 */
	protected Vector inclusionLevels;

	/**
	 * Hash maps to store the mapping of layout objects (l-level) and their
	 * model level objects
	 */
	protected HashMap viewMapVtoL;
	protected HashMap viewMapLtoV;

	/**
	 * Indicates whether the topology will be created from Chisio model or not
	 */
	protected boolean fromChisioModel;
	/**
	 * Whether layout should create bendpoints as needed or not
	 */
	protected boolean createBendsAsNeeded;

	/**
	 * Whether layout should be incremental or not
	 */
	protected boolean incremental;

	/**
	 * Do we animate the layout process?
	 */
	protected boolean animationDuringLayout;

	/**
	 * Number iterations that should be done between two successive animations
	 */
	protected int animationPeriod;

	/**
	 * This is used for creation of bendpoints by using dummy nodes and edges.
	 * Maps an LEdge to its dummy bendpoint path.
	 */
	protected HashMap edgeToDummyNodes = new HashMap();

	/**
	 * Layout Quality: 0:proof, 1:default, 2:draft
	 */
	protected int layoutQuality;

	/**
	 * Holds given root chisio model in constructor in order to use it in
	 * createTopology method, later.
	 */
	CompoundModel rootModel;

	/**
	 * Holds given list of edges and nodes in constructor in order to use it in
	 * createTopology method, later.
	 */
	private List<LNode> V;
	private List<LEdge> E;

// -----------------------------------------------------------------------------
// Section: Constructors and Initializations
// -----------------------------------------------------------------------------
	/**
	 *  Initializes fields and creates topology according to given chisio root
	 *  model.
	 */
	public AbstractLayout(CompoundModel rootModel)
	{
		assert (rootModel != null): "Root chisio model cannot be null";

		this.init();
		this.fromChisioModel = true;
		this.rootModel = rootModel;
	}

	/**
	 *  Initialize field and creates topoly according to given LNodes and LEdges
	 */
	public AbstractLayout(List<LNode> V, List<LEdge> E)
	{
		assert (V != null): "Node list cannot be null";
		assert (E != null): "Edge list cannot be null";

		this.init();
		this.fromChisioModel = false;
		this.V = V;
		this.E = E;
	}

	private void init()
	{
		this.lGraphManager = this.createNewLGraphManager();
		this.inclusionLevels = new Vector();
		this.viewMapVtoL = new HashMap();
		this.viewMapLtoV = new HashMap();
	}

	/**
	 * This method creates light-weight abstractions (layout level objects) of
	 * the given Chisio compound graph model. It returns false if the provided
	 * topology is unexpected (e.g. compound structures, where they are not
	 * supported).
	 */
	protected boolean createTopology(CompoundModel rootModel)
	{
		LNode rootNode = this.createLGraphs(rootModel, 0);
		this.createLEdges(rootModel);
		this.lGraphManager.setRootGraph(rootNode.getChild());
		this.initialize();

		return true;
	}

	/**
	 * This method sets required fields of this class according to already
	 * created l-level topology. These fields are:
	 *  - Owner graph of given nodes.
	 *  - LGraphManager of given nodes.
	 *  - Inclusion levels for the given graph structure (currently flat)
	 * It returns false if the provided topology is unexpected (e.g. compound
	 * structures, where they are not supported).
	 */
	protected boolean createTopology(List<LNode> lNodes,List<LEdge> lEdges)
	{
		// Create root lNode and root lGraph
		LNode rootNode = this.createNewLNode(this.lGraphManager, null);
		LGraph rootGraph = this.createNewLGraph(rootNode, this.lGraphManager);
		rootNode.setChild(rootGraph);
		this.lGraphManager.getGraphs().add(rootGraph);

		// Adjust levels
		Vector currentLevel = new Vector(1);
		this.inclusionLevels.add(0,currentLevel);

		// Process all nodes and make necessary settings.
		for (LNode currentNode : lNodes)
		{
			currentNode.setOwner(rootGraph);
			currentNode.setChild(null);
			currentNode.graphManager = this.lGraphManager;
			rootGraph.getNodes().add(currentNode);
			currentLevel.add(currentNode);
		}

		// Process all edges and make necessary settings.
		for (LEdge currentEdge : lEdges)
		{
			assert currentEdge.source != null;
			assert currentEdge.target != null;

			assert rootGraph.getNodes().contains(currentEdge.source);
			assert rootGraph.getNodes().contains(currentEdge.target);

			this.lGraphManager.getEdgeList().add(currentEdge);
			rootGraph.getEdges().add(currentEdge);
		}

		this.lGraphManager.setRootGraph(rootNode.getChild());
		this.initialize();

		return true;
	}

	/**
	 * This method initializes layout parameters.
	 */
	public void initialize()
	{
		this.initParameters();
	}

	public LGraphManager createNewLGraphManager()
	{
		return new LGraphManager(this);
	}

	public LNode createNewLNode(LGraphManager gm, NodeModel model)
	{
		return new LNode(gm);
	}

	public LNode createNewLNode(LGraphManager gm,
		NodeModel model,
		Point loc,
		Dimension size)
	{
		return new LNode(gm, loc, size);
	}

	public LEdge createNewLEdge(LNode source, LNode target)
	{
		return new LEdge(source, target);
	}

	public LGraph createNewLGraph(LNode rootNode, LGraphManager lGraphManager)
	{
		return new LGraph(rootNode, lGraphManager);
	}

	protected LNode createLGraphs(CompoundModel model, int level)
	{
		LNode rootNode;
		LNode lNode;
		Vector currentLevel;

		if (this.inclusionLevels.size() < level + 1)
		{
			currentLevel = new Vector();
			this.inclusionLevels.add(level, currentLevel);
		}
		else
		{
			currentLevel = (Vector) this.inclusionLevels.elementAt(level);
		}

		LGraph lGraph;

		if (model.getParentModel() == null)
		// root of Chisio compound structure
		{
			rootNode = this.createNewLNode(this.lGraphManager, null);
		}
		else
		{
			rootNode =
				this.createNewLNode(this.lGraphManager,
					model,
					model.getLocationAbs(),
					model.getSize());
		}

		// instantiate an owner graph
		ArrayList lNodes = new ArrayList();
		lGraph = this.createNewLGraph(rootNode, this.lGraphManager);

		// get the nodes of the graph
		Iterator iter = model.getChildren().iterator();
		Object currentObject;
		boolean isCompound;

		while (iter.hasNext())
		{
			currentObject = iter.next();
			isCompound = currentObject instanceof CompoundModel;

			if (!isCompound)
//				// needed when compounds are in collapsed form!
//				|| !currentNode.getChildGraph().isViewable())
			{
				NodeModel mdl = (NodeModel) currentObject;
				lNode =
					this.createNewLNode(this.lGraphManager,
						mdl,
						mdl.getLocationAbs(),
						mdl.getSize());
				lNode.setChild(null);
			}
			else
			{
				// recurse on this node
				// also set Chisio model node appropriately
				lNode =
					this.createLGraphs((CompoundModel) currentObject,
						level + 1);
				lNode.setRect(((NodeModel) currentObject).getLocationAbs(),
					((NodeModel) currentObject).getSize());
			}

			lNode.setOwner(lGraph);
			lNodes.add(lNode);

			// Since (in the createTopology method) the edges are traversed
			// after all the l-level nodes are created, they must be kept in a
			// hash map in which they are bound to v-level nodes
			currentLevel.add(lNode);
			this.viewMapVtoL.put((GraphObject) currentObject, lNode);
			this.viewMapLtoV.put(lNode, (GraphObject) currentObject);
		}

		// set the nodes of the LGraph and return the node that owns this graph;
		// also make sure that this graph is added to the graphs list of
		// LGraphManager
		lGraph.setNodes(lNodes);
		this.lGraphManager.getGraphs().add(lGraph);
		rootNode.setChild(lGraph);

		return rootNode;
	}

	/**
	 * This method creates l-level edges for all Chisio edges.
	 */
	protected void createLEdges(CompoundModel root)
	{
		// iterate through the list of all edges in this graph and create
		// LEdge's reading the information from the hash map created before
		// according to the source and target nodes
		Iterator iter = root.getEdgeIterator(CompoundModel.ALL_EDGES, true, false);
		EdgeModel vEdge;

		while (iter.hasNext())
		{
			vEdge = (EdgeModel) iter.next();

			NodeModel vSource = vEdge.getSource();
			NodeModel vTarget = vEdge.getTarget();

			// get corresponding l-level nodes from the map
			LNode lSource = (LNode) this.viewMapVtoL.get(vSource);
			LNode lTarget = (LNode) this.viewMapVtoL.get(vTarget);

			// create the LEdge
			LEdge lEdge = this.createNewLEdge(lSource, lTarget);

			Assert.isTrue((vSource.getParentModel() != vTarget.getParentModel())
				== lEdge.isInterGraph);

			// add the newly created edge to the owner of the source (or target)
			// node, also add it to the LGraphManager
			LGraph lGraph = lSource.getOwner();
			lGraph.getEdges().add(lEdge);
			this.lGraphManager.getEdgeList().add(lEdge);

			this.viewMapVtoL.put(vEdge, lEdge);
			this.viewMapLtoV.put(lEdge, vEdge);
		}
	}

// -----------------------------------------------------------------------------
// Section: Accessor Methods
// -----------------------------------------------------------------------------
	/**
	 * This method returns the associated layout graph manager.
	 */
	public LGraphManager getLGraphManager()
	{
		return this.lGraphManager;
	}

	/**
	 * This method returns the array of all nodes.
	 */
	public Object[] getNodes()
	{
		return this.lGraphManager.getNodes();
	}

	/**
	 * This method returns the array of all edges.
	 */
	public Object[] getEdges()
	{
		return this.lGraphManager.getEdges();
	}

	public Dimension getCurrentSize()
	{
		return new Dimension(viewer.getControl().getShell().getSize());
	}

	public void setViewer(ScrollingGraphicalViewer viewer)
	{
		this.viewer = viewer;
	}

// -----------------------------------------------------------------------------
// Section: Remaining Methods
// -----------------------------------------------------------------------------
	/**
	 *  This method shows busy cursor while layout is runnning.
	 */
	public final void runLayout()
	{
		viewer.getControl().getShell().setEnabled(false);
		viewer.getControl().getShell().setCursor(new Cursor(null, SWT.CURSOR_WAIT));
		//disable higlight and handles
		IFigure highlightLayer = ((ChsScalableRootEditPart) viewer.getRootEditPart()).getLayer(
			HighlightLayer.HIGHLIGHT_LAYER);

		IFigure handleLayer = ((ChsScalableRootEditPart) viewer.getRootEditPart()).getLayer(
			LayerConstants.HANDLE_LAYER);

		highlightLayer.setVisible(false);
		handleLayer.setVisible(false);

		boolean isSuccessful;

		if (fromChisioModel)
		{
			isSuccessful = createTopology(rootModel);
		}
		else
		{
			isSuccessful = createTopology(V, E);
		}

		if (isSuccessful)
		{
			this.layout();

			this.transform();

			// Propagate geometric changes to v-level objects if we have created
			// l-graph objects from chisio model.
			if (fromChisioModel)
			{
				this.update();
			}
		}
		else
		{
			MessageBox mb = new MessageBox(new Shell(), SWT.OK);
			mb.setMessage("Compound structures are not supported by this"
				+ " layout style!");
			mb.setText(ChisioMain.TOOL_NAME);
			mb.open();
		}

		//enable highlight and handles
		highlightLayer.setVisible(true);
		handleLayer.setVisible(true);

		viewer.getControl().getShell().setCursor(new Cursor(null, SWT.CURSOR_ARROW));
		viewer.getControl().getShell().setEnabled(true);
	}

	/**
	 * This method is the main method of the layout algorithm; each new layout
	 * algoritm must implement this method.
	 */
	public abstract void layout();

	/**
	 * This method is used to set all layout parameters to default values
	 * determined at compile time.
	 */
	public void initParameters()
	{
		LayoutOptionsPack.General layoutOptionsPack =
			LayoutOptionsPack.getInstance().getGeneral();

		this.animationDuringLayout =
			layoutOptionsPack.isAnimationDuringLayout();
		this.animationPeriod = (int) transform(
			layoutOptionsPack.getAnimationPeriod(), DEFAULT_ANIMATION_PERIOD);
		animationOnLayout = layoutOptionsPack.isAnimationOnLayout();

		this.layoutQuality = layoutOptionsPack.getLayoutQuality();
		this.incremental = layoutOptionsPack.isIncremental();
		this.createBendsAsNeeded = layoutOptionsPack.isCreateBendsAsNeeded();

		LGraph.setGraphMargin(CompoundModel.MARGIN_SIZE);
	}

	/**
	 * This method transforms the LNodes in the associated LGraphManager so that
	 * upper-left corner of the drawing is (0, 0). The goal is to avoid negative
	 * coordinates that are not allowed when displaying by shifting the drawing
	 * as necessary.
	 */
	public void transform()
	{
		this.transform(new PrecisionPoint(0, 0));
	}

	/**
	 * This method transforms the LNodes in the associated LGraphManager so that
	 * upper-left corner of the drawing starts at the input coordinate.
	 */
	public void transform(PrecisionPoint newLeftTop)
	{
		// create a transformation object (from Eclipse to layout). When an
		// inverse transform is applied, we get upper-left coordinate of the
		// drawing or the root graph at given input coordinate (some margins
		// already included in calculation of left-top).

		ChsTransform trans = new ChsTransform();
		Point leftTop = this.lGraphManager.getRoot().updateLeftTop();

		if (leftTop != null)
		{
			trans.setWorldOrgX(newLeftTop.preciseX);
			trans.setWorldOrgY(newLeftTop.preciseY);

			trans.setDeviceOrgX(leftTop.x);
			trans.setDeviceOrgY(leftTop.y);

			List nodes = this.lGraphManager.getNodeList();
			Iterator iter = nodes.iterator();

			while (iter.hasNext())
			{
				LNode lNode = (LNode) iter.next();
				lNode.transform(trans);
			}
		}
	}


	/**
	 * This method updates the geometry of the target graph according to
	 * calculated layout.
	 */
	public void update()
	{
		createBendpointsFromDummyNodes();

		List nodes = lGraphManager.getNodeList();
		Iterator iter = nodes.iterator();

		while (iter.hasNext())
		{
			LNode lNode = (LNode) iter.next();

			NodeModel vNode = (NodeModel) viewMapLtoV.get(lNode);

			if (vNode == null)
			{
				continue;
			}

			if (lNode.getChild() == null ||
				lNode.getChild().getNodes().size() == 0)
			{
				vNode.setLocationAbs(new Point(lNode.getLocation().preciseX,
					lNode.getLocation().preciseY));
			}
			else
			{
				((CompoundModel)vNode).calculateSize();
			}
		}

		if (animationOnLayout || animationDuringLayout)
		{
			GraphAnimation.run(viewer);
		}
	}

	/**
	 * This method creates dummy nodes (an l-level node with minimal dimensions)
	 * for the given edge (one per bendpoint). The existing l-level structure
	 * is updated accordingly.
	 */
	public List createDummyNodesForBendpoints(LEdge edge)
	{
		List dummyNodes = new ArrayList();
		LNode prev = edge.source;
		int level = prev.getInclusionTreeDepth()-1;
		LGraph graph =
			this.lGraphManager.findLowestCommonAncestor(edge.source, edge.target);

		for (int i = 0; i < edge.bendpoints.size(); i++)
		{
			// create new dummy node
			LNode dummyNode =
				this.createNewLNode(this.lGraphManager,
					null,
					new Point(0,0),
					new Dimension(1,1));
			dummyNode.setOwner(graph);
			dummyNode.setChild(null);
			graph.getNodes().add(dummyNode);
			((Vector)this.inclusionLevels.get(level)).add(dummyNode);

			// create new dummy edge between prev and dummy node
			LEdge dummyEdge = this.createNewLEdge(prev, dummyNode);
			graph.getEdges().add(dummyEdge);
			this.lGraphManager.getEdgeList().add(dummyEdge);

			dummyNodes.add(dummyNode);
			prev = dummyNode;
		}

		LEdge dummyEdge = this.createNewLEdge(prev, edge.target);
		graph.getEdges().add(dummyEdge);
		this.lGraphManager.getEdgeList().add(dummyEdge);

		// remove real edge from graph
		this.edgeToDummyNodes.put(edge, dummyNodes);
		graph.getEdges().remove(edge);
		this.lGraphManager.getEdgeList().remove(edge);

		return dummyNodes;
	}

	/**
	 * This method creates bendpoints for edges in Chisio from the dummy nodes
	 * at l-level.
	 */
	public void createBendpointsFromDummyNodes()
	{
		List edges = this.lGraphManager.getEdgeList();
		edges.addAll(0, this.edgeToDummyNodes.keySet());

		for (int k = 0 ; k < edges.size(); k++)
		{
			LEdge lEdge = (LEdge) edges.get(k);

			if (lEdge.bendpoints.size() > 0)
			{
				List path = (List) this.edgeToDummyNodes.get(lEdge);

				Point sourceLoc =
					new Point(lEdge.source.getCenterX(),
						lEdge.source.getCenterY());
				Point targetLoc =
					new Point(lEdge.target.getCenterX(),
						lEdge.target.getCenterY());
				int level = lEdge.source.getInclusionTreeDepth()-1;
				LGraph graph = this.lGraphManager.findLowestCommonAncestor(
					lEdge.source, lEdge.target);

				for (int i = 0; i < path.size(); i++)
				{
					LNode dummyNode = ((LNode)path.get(i));
					Point p =
						new PrecisionPoint(dummyNode.getCenterX(),
							dummyNode.getCenterY());

					// update bendpoint's location according to dummy node
					EdgeBendpoint ebp =
						(EdgeBendpoint) lEdge.bendpoints.get(i);
					ebp.setRelativeDimensions(
						p.getDifference(sourceLoc),
						p.getDifference(targetLoc));

					// remove created dummy node and dummy edges
					graph.getNodes().remove(dummyNode);
					((Vector)this.inclusionLevels.get(level)).remove(dummyNode);

					graph.getEdges().removeAll(dummyNode.edges);
					this.lGraphManager.getEdgeList().removeAll(dummyNode.edges);

					this.lGraphManager.getNodeList().remove(dummyNode);
				}

				// add the real edge to graph list
				graph.getEdges().add(lEdge);
			}

			// transfer bendpoints information to v-level
			EdgeModel edge = (EdgeModel) this.viewMapLtoV.get(lEdge);
			edge.setBendpoints(lEdge.bendpoints);
		}
	}

	/**
	 * This method determines the initial positions of leaf nodes in the
	 * associated l-level compound graph structure randomly. Non-empty compound
	 * nodes get their initial positions (and dimensions) from their contents,
	 * thus no calculations should be done for them!
	 */
	protected void positionNodesRandomly()
	{
		assert !this.incremental;

		Object[] lNodes = this.lGraphManager.getNodes();
		LNode lNode;

		for (int i = 0; i < lNodes.length; i++)
		{
			lNode = (LNode) lNodes[i];

			if (lNode.getChild() == null)
			{
				lNode.scatter();
			}
			else if (lNode.getChild().getNodes().size() == 0)
			{
				lNode.scatter();
			}
			else
			{
				lNode.updateBounds();
			}
		}
	}

	/**
	 * This method returns a list of trees where each tree is represented as a
	 * list of l-nodes. The method returns a list of size 0 when:
	 * - The graph is not flat or
	 * - One of the component(s) of the graph is not a tree.
	 */
	protected ArrayList<ArrayList<LNode>> getFlatForest()
	{
		ArrayList<ArrayList<LNode>> flatForest =
			new ArrayList<ArrayList<LNode>>();
		boolean isForest = true;

		// Quick reference for all nodes in the graph manager associated with
		// this layout. The list should not be changed.
		final List<LNode> allNodes = this.lGraphManager.getRoot().getNodes();

		// First be sure that the graph is flat
		boolean isFlat = true;

		for (int i = 0; i < allNodes.size(); i++)
		{
			if (allNodes.get(i).getChild() != null)
			{
				isFlat = false;
			}
		}

		// Return empty forest if the graph is not flat.
		if (!isFlat)
		{
			return flatForest;
		}

		// Run BFS for each component of the graph.

		Set<LNode> visited = new HashSet<LNode>();
		LinkedList<LNode> toBeVisited = new LinkedList<LNode>();
		HashMap<LNode, LNode> parents = new HashMap<LNode, LNode>();
		LinkedList<LNode> unProcessedNodes = new LinkedList<LNode>();

		unProcessedNodes.addAll(allNodes);

		// Each iteration of this loop finds a component of the graph and
		// decides whether it is a tree or not. If it is a tree, adds it to the
		// forest and continued with the next component.

		while (unProcessedNodes.size() > 0 && isForest)
		{
			toBeVisited.add(unProcessedNodes.getFirst());

			// Start the BFS. Each iteration of this loop visits a node in a
			// BFS manner.
			while (!toBeVisited.isEmpty() && isForest)
			{
				LNode currentNode = toBeVisited.poll();
				visited.add(currentNode);

				// Traverse all neighbors of this node.
				List<LEdge> neighborEdges = currentNode.getEdges();

				for (int i = 0; i < neighborEdges.size(); i++)
				{
					LNode currentNeighbor =
						neighborEdges.get(i).getOtherEnd(currentNode);

					// If BFS is not growing from this neighbor.
					if (parents.get(currentNode) != currentNeighbor)
					{
						// We haven't previously visited this neighbor.
						if (!visited.contains(currentNeighbor))
						{
							toBeVisited.addLast(currentNeighbor);
							parents.put(currentNeighbor, currentNode);
						}
						// Since we have previously visited this neighbor and
						// this neighbor is not parent of currentNode, given
						// graph contains a component that is not tree, hence
						// it is not a forest.
						else
						{
							isForest = false;
							break;
						}
					}
				}
			}

			if (!isForest)
			// The graph contains a component that is not a tree. Empty
			// previously found trees. The method will end.
			{
				flatForest.clear();
			}
			else
			// Save currently visited nodes as a tree in our forest. Reset
			// visited and parents lists. Continue with the next component of
			// the graph, if any.
			{
				flatForest.add(new ArrayList<LNode>(visited));
				unProcessedNodes.removeAll(visited);
				visited.clear();
				parents.clear();
			}
		}

		return flatForest;
	}

	/**
	 * This method moves the l-nodes towards the middle of the screen for better
	 * visibility during animation.
	 */
	protected void moveTowardsScreenCenter()
	{
		Object[] lNodes = this.getNodes();

		for (int i = 0; i < lNodes.length; i++)
		{
			LNode node = (LNode) lNodes[i];

			node.moveBy(500, 500);
		}
	}

// -----------------------------------------------------------------------------
// Section: Class Methods
// -----------------------------------------------------------------------------
	/**
	 * This method returns the sign of the input value.
	 */
	static public int sign(double value)
	{
		if (value > 0)
		{
			return 1;
		}
		else if (value < 0)
		{
			return -1;
		}
		else
		{
			return 0;
		}
	}

	/**
	 * This method transforms the input slider value into an actual parameter
	 * value using two separate linear functions (one from 0 to 50, other from
	 * 50 to 100), where default slider value (50) maps to the default value of
	 * the associated actual parameter. Minimum and maximum slider values map to
	 * 1/10 and 10 fold of this default value, respectively.
	 */
	public static double transform(int sliderValue, double defaultValue)
	{
		double a, b;

		if (sliderValue <= 50)
		{
			a = 9.0 * defaultValue / 500.0;
			b = defaultValue / 10.0;
		}
		else
		{
			a = 9.0 * defaultValue / 50.0;
			b = -8 * defaultValue;
		}

		return (a * sliderValue + b);
	}

	/**
	 * This method takes a list of lists, where each list contains l-nodes of a
	 * tree. Center of each tree is return as a list of.
	 */
	public static List<LNode> findCenterOfEachTree(List<List> listofLists)
	{
		ArrayList<LNode> centers = new ArrayList<LNode>();

		for (int i = 0; i < listofLists.size(); i++)
		{
			List<LNode> list = listofLists.get(i);
			LNode center = findCenterOfTree(list);
			centers.add(i, center);
		}

		return centers;
	}

	/**
	 * This method finds and returns the center of the given nodes, assuming
	 * that the given nodes form a tree in themselves.
	 */
	public static LNode findCenterOfTree(List<LNode> nodes)
	{
		ArrayList<LNode> list = new ArrayList<LNode>();
		list.addAll(nodes);

		ArrayList<LNode> removedNodes = new ArrayList<LNode>();
		HashMap<LNode, Integer> remainingDegrees =
			new HashMap<LNode, Integer>();
		boolean foundCenter = false;
		LNode centerNode = null;

		if (list.size() == 1 || list.size() == 2)
		{
			foundCenter = true;
			centerNode = list.get(0);
		}

		Iterator<LNode> iter = list.iterator();

		while (iter.hasNext())
		{
			LNode node = iter.next();
			Integer degree = new Integer(node.getNeighborsList().size());
			remainingDegrees.put(node , degree);

			if (degree.intValue() == 1)
			{
				removedNodes.add(node);
			}
		}

		ArrayList<LNode> tempList = new ArrayList<LNode>();
		tempList.addAll(removedNodes);

		while (!foundCenter)
		{
			ArrayList<LNode> tempList2 = new ArrayList<LNode>();
			tempList2.addAll(tempList);
			tempList.removeAll(tempList);
			iter = tempList2.iterator();

			while (iter.hasNext())
			{
				LNode node = iter.next();
				list.remove(node);

				Set<LNode> neighbours = node.getNeighborsList();

				for (LNode neighbor: neighbours)
				{
					if (!removedNodes.contains(neighbor))
					{
						Integer otherDegree = remainingDegrees.get(neighbor);
						Integer newDegree =
							new Integer(otherDegree.intValue() - 1);

						if (newDegree.intValue() == 1)
						{
							tempList.add(neighbor);
						}

						remainingDegrees.put(neighbor, newDegree);
					}
				}
			}

			removedNodes.addAll(tempList);

			if (list.size() == 1 || list.size() == 2)
			{
				foundCenter = true;
				centerNode = list.get(0);
			}
		}

		return centerNode;
	}

// -----------------------------------------------------------------------------
// Section: Class Variables
// -----------------------------------------------------------------------------
	/**
	 * Do we animate the before and after layout node replacements?
	 */
	public static boolean animationOnLayout;

	/**
	 * This variable stores the world boundaries that this manager operates on.
	 */
	public static final int WORLD_BOUNDARY = 1000000;

	/**
	 * This variable stores the coordinates of the gravity center used by some
	 * layout algorithms.
	 */
	public static final int GRAVITY_CENTER_X = 1200;
	public static final int GRAVITY_CENTER_Y = 900;

	/**
	 * Layout Quality
	 */
	public static final int PROOF_QUALITY = 0;
	public static final int DEFAULT_QUALITY = 1;
	public static final int DRAFT_QUALITY = 2;

	/**
	 * Default parameters
	 */
	public static final boolean DEFAULT_CREATE_BENDS_AS_NEEDED = false;
	public static final boolean DEFAULT_INCREMENTAL = false;
	public static final boolean DEFAULT_ANIMATION_ON_LAYOUT = true;
	public static final boolean DEFAULT_ANIMATION_DURING_LAYOUT = false;
	public static final int DEFAULT_ANIMATION_PERIOD = 50;
}