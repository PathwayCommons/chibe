package org.gvt.layout;

import java.util.*;

import org.eclipse.draw2d.geometry.*;
import org.gvt.ChisioMain;
import org.gvt.command.CreateCommand;
import org.gvt.command.CreateConnectionCommand;
import org.gvt.model.*;

/**
 * This class represents a graph manager (l-level) for layout purposes. A graph
 * manager maintains a collection of graphs, forming a compound graph structure
 * through inclusion and inter-graph edges.
 *
 * @author Erhan Giral
 * @author Ugur Dogrusoz
 * @author Cihan Kucukkececi
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class LGraphManager
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/**
	 * Graphs maintained by this graph, including the root of nesting hierachcy
	 */
	private List graphs;

	/**
	 * All nodes and edges in this graph manager. For efficiency purposes we
	 * hold references of all layout objects that we operate on in arrays.
	 */
	private Object[] nodes;
	private Object[] edges;

	/**
	 * These should not be needed/used after construction of l-level, once
	 * contents are transferred to the corresponding arrays for efficiency.
	 */
	private List nodeList;
	private List edgeList;

	/**
	 * The root of the inclusion/nesting hierarchy of this compound structure
	 */
	private LGraph rootGraph;

	/**
	 * Layout object using this graph manager
	 */
	private AbstractLayout layout;

// -----------------------------------------------------------------------------
// Section: Constructors and Initialization
// -----------------------------------------------------------------------------
	/**
	 * The constructor
	 */
	public LGraphManager(AbstractLayout layout)
	{
		this.graphs = new ArrayList();
		this.nodes = null;
		this.edges = null;
		this.nodeList = null;
		this.edgeList = new ArrayList();
		this.rootGraph = null;
		this.layout = layout;
	}

// -----------------------------------------------------------------------------
// Section: Accessors
// -----------------------------------------------------------------------------
	public List getGraphs()
	{
		return graphs;
	}

	public void setGraphs(List graphs)
	{
		this.graphs = graphs;
	}

	public Object[] getNodes()
	{
		if (this.nodes == null)
		{
			this.nodes = this.getNodeList().toArray();
		}

		return this.nodes;
	}

	public Object[] getEdges()
	{
		if (this.edges == null)
		{
			this.edges = this.edgeList.toArray();
		}

		return this.edges;
	}

	public List getNodeList()
	{
		if (this.nodeList == null)
		{
			this.nodeList = new LinkedList();

			for (Iterator iterator = this.getGraphs().iterator();
				 iterator.hasNext();)
			{
				this.nodeList.addAll(((LGraph) iterator.next()).getNodes());
			}
		}

		return this.nodeList;
	}

	public List getEdgeList()
	{
		// should have been constructed during l-level construction!
		return edgeList;
	}

	/**
	 * This method return the node list of this graph.
	 */
	public LGraph getRoot()
	{
		return this.rootGraph;
	}

	public void setRootGraph(LGraph graph)
	{
		this.rootGraph = graph;
	}

	/**
	 * This method returns the associated layout object, which uses this graph
	 * manager.
	 */
	public AbstractLayout getLayout()
	{
		return this.layout;
	}

// -----------------------------------------------------------------------------
// Section: Remaining Methods
// -----------------------------------------------------------------------------
	/**
	 * This method returns the rightmost point of the nodes in the given list.
	 */
	public double getRightBorderOfNodeGroup(LinkedList nodeList)
	{
		if (nodeList == null)
		{
			return -Double.MAX_VALUE;
		}

		Iterator iter = nodeList.iterator();
		double right = -Double.MAX_VALUE;
		double temp;

		while (iter.hasNext())
		{
			temp = ((LNode) (iter.next())).getRight();

			if (right < temp)
			{
				right = temp;
			}
		}

		return right;
	}

	/**
	 * This method returns the leftmost point of the nodes in the given list.
	 */
	public double getLeftBorderOfNodeGroup(LinkedList nodeList)
	{
		if (nodeList == null)
		{
			return Double.MAX_VALUE;
		}

		Iterator iter = nodeList.iterator();
		double left = Double.MAX_VALUE;
		double temp;

		while (iter.hasNext())
		{
			temp = ((LNode) (iter.next())).getLeft();

			if (left > temp)
			{
				left = temp;
			}
		}

		return left;
	}

	/**
	 * This method returns the bottommost point of the nodes in the given list.
	 */
	public double getBottomBorderOfNodeGroup(LinkedList nodeList)
	{
		if (nodeList == null)
		{
			return -Double.MAX_VALUE;
		}

		Iterator iter = nodeList.iterator();
		double bottom = -Double.MAX_VALUE;
		double temp;

		while (iter.hasNext())
		{
			temp = ((LNode) (iter.next())).getBottom();

			if (bottom < temp)
			{
				bottom = temp;
			}
		}

		return bottom;
	}

	/**
	 * This method returns the topmost point of the nodes in the given list.
	 */
	public double getTopBorderOfNodeGroup(LinkedList nodeList)
	{
		if (nodeList == null)
		{
			return Double.MAX_VALUE;
		}

		Iterator iter = nodeList.iterator();
		double top = Double.MAX_VALUE;
		double temp;

		while (iter.hasNext())
		{
			temp = ((LNode) (iter.next())).getTop();

			if (top > temp)
			{
				top = temp;
			}
		}

		return top;
	}

	/**
	 * This method checks whether one of the input nodes is an ancestor of the
	 * other one (and vice versa) in the nesting tree. Such pairs of nodes
	 * should not be allowed to be joined by edges.
	 */
	public static boolean isOneAncestorOfOther(LNode firstNode,
		LNode secondNode)
	{
		assert firstNode != null && secondNode != null;

		if (firstNode == secondNode)
		{
			return true;
		}

		// Is second node an ancestor of the first one?

		LGraph ownerGraph = firstNode.getOwner();
		LNode parentNode;

		do
		{
			parentNode = ownerGraph.getParent();

			if (parentNode == null)
			{
				break;
			}

			if (parentNode == secondNode)
			{
				return true;
			}

			ownerGraph = parentNode.getOwner();
			if(ownerGraph == null)
			{
				break;
			}
		} while (true);

		// Is first node an ancestor of the second one?

		ownerGraph = secondNode.getOwner();

		do
		{
			parentNode = ownerGraph.getParent();

			if (parentNode == null)
			{
				break;
			}

			if (parentNode == firstNode)
			{
				return true;
			}

			ownerGraph = parentNode.getOwner();
			if(ownerGraph == null)
			{
				break;
			}
		} while (true);

		return false;
	}
	
	/**
	 * This method finds the lowest common ancestor of given two nodes.
	 * 
	 * @param firstNode
	 * @param secondNode
	 * @return lowest common ancestor
	 */
	public LGraph findLowestCommonAncestor(LNode firstNode, LNode secondNode)
	{
		if (firstNode == secondNode)
		{
			return firstNode.getOwner();
		}

		LGraph firstOwnerGraph = firstNode.getOwner();

		do
		{
			if (firstOwnerGraph == null)
			{
				break;
			}

			LGraph secondOwnerGraph = secondNode.getOwner();
		
			do
			{			
				if (secondOwnerGraph == null)
				{
					break;
				}

				if (secondOwnerGraph == firstOwnerGraph)
				{
					return secondOwnerGraph;
				}
				
				secondOwnerGraph = secondOwnerGraph.getParent().getOwner();
			} while (true);

			firstOwnerGraph = firstOwnerGraph.getParent().getOwner();
		} while (true);

		return firstOwnerGraph;
	}

	/**
	 * This method creates a chisio model for current l structure stored in 
	 * this graph manager. The resulting model is returned inside a root
	 * CompoundModel. 
	 */
	public CompoundModel createL2ChisioModel()
	{
		// Used for later accessing to source and target nodes of an edge.
		HashMap<LNode, NodeModel> lToChisio = new HashMap<LNode, NodeModel>();

		// Holds the LNode s that are not converted to chisio model yet. The 
		// stack will grow when more nodes are encountered during depth-first 
		// traversal.
		Stack<LNode> remainingLNodes = new Stack<LNode>();

		// The chisio root model that will represent current instance of 
		// lGraphManager.
		CompoundModel chisioRoot = new CompoundModel();
		chisioRoot.setAsRoot();
		lToChisio.put(this.rootGraph.getParent(), chisioRoot);
		remainingLNodes.addAll(this.rootGraph.getNodes());

		// Start a top-down depth-first traversal and convert each LNode to 
		// corresponding chisio model : either NodeModel or CompoundModel.
		while (!remainingLNodes.empty())
		{
			NodeModel currentChisioModel = null;
			LNode currentLNode = remainingLNodes.pop();
			
			// Current LNode has a child lGraph
			if (currentLNode.child != null)
			{	
				currentChisioModel = new CompoundModel();
				remainingLNodes.addAll(currentLNode.child.getNodes());
			}
			// Current node is a simple node
			else
			{
				currentChisioModel = new NodeModel();
			}

			// Copy position and size information.
			CompoundModel parentChisioModel = (CompoundModel)lToChisio.get(
					currentLNode.getOwner().getParent());

			currentChisioModel.setLocation(
					new PrecisionPoint(currentLNode.rect.x, currentLNode.rect.y));
			currentChisioModel.setSize(new PrecisionDimension(
					currentLNode.rect.width, currentLNode.rect.height));

			CreateCommand cmd = 
				new CreateCommand(parentChisioModel, currentChisioModel);
			cmd.execute();

			// Save the mapping between l and chisio level.
			lToChisio.put(currentLNode, currentChisioModel);
		}

		// Create Edges
		for (int i = 0; i < edges.length; i++)
		{
			LEdge currentEdge = (LEdge) edges[i];
			NodeModel chisioSource = lToChisio.get(currentEdge.source);
			NodeModel chisioTarget = lToChisio.get(currentEdge.target);

			assert chisioSource != null && chisioTarget != null;

			// Create the edge as chisio does.
			EdgeModel chisioEdge = new EdgeModel();
			CreateConnectionCommand command = new CreateConnectionCommand();
			command.setSource(chisioSource);
			command.setTarget(chisioTarget);
			command.setConnection(chisioEdge);
			command.execute();
		}

		return chisioRoot;
	}
}
