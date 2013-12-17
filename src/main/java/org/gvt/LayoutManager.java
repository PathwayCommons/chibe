package org.gvt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.gvt.action.ZoomAction;
import org.gvt.model.CompoundModel;
import org.gvt.model.EdgeModel;
import org.gvt.model.NodeModel;
import org.ivis.layout.*;
import org.ivis.util.PointD;

import java.util.*;

/**
 * This class manages the operations related to layout. Methods related to
 * topology creation (i.e. transforming from v-level to l-level), layout
 * animation handling reside in this class. 
 * 
 * @author Selcuk Onur Sumer
 *
 */
public class LayoutManager
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------

	private Layout layout;
	private CompoundModel root;
	private ChisioMain main;
	private HashMap<NodeModel, LNode> gvtToLayout;
	private HashMap<LNode, NodeModel> layoutToGvt;

// -----------------------------------------------------------------------------
// Section: Constructors and initialization
// -----------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public LayoutManager()
	{
		super();
		this.gvtToLayout = new HashMap<NodeModel, LNode>();
		this.layoutToGvt = new HashMap<LNode, NodeModel>();
	}

// -----------------------------------------------------------------------------
// Section: Accessors
// -----------------------------------------------------------------------------

	public void setLayout(Layout layout)
	{
		this.layout = layout;
	}

	public void setRoot(CompoundModel root)
	{
		this.root = root;
	}

	public void setMain(ChisioMain main)
	{
		this.main = main;
	}

// -----------------------------------------------------------------------------
// Section: Remaining methods
// -----------------------------------------------------------------------------

	/**
	 * Creates l-level the topology of the graph from the given compound model.
	 */
	public void createTopology()
	{	
		// create initial topology: a graph manager associated with the layout,
		// containing an empty root graph as its only graph

		LGraphManager graphMgr = this.layout.getGraphManager();
		LGraph lroot = graphMgr.addRoot();
		lroot.vGraphObject = this.root;

		// for each NodeModel in the root model create an LNode

		for (Object o : root.getChildren())
		{
			createNode((NodeModel) o, null, this.layout);
		}

		Map<EdgeModel, Integer> multiEdges = findMultiEdges(root.getEdges());

		// for each EdgeModel in the edge set create an LEdge

		for (Object o : root.getEdges())
		{
			EdgeModel edge = (EdgeModel) o;
			createEdge(edge, multiEdges.get(edge));
		}

		graphMgr.updateBounds();
	}

	private Map<EdgeModel, Integer> findMultiEdges(Set<EdgeModel> edges)
	{
		Map<EdgeModel, Set<EdgeModel>> map = new HashMap<EdgeModel, Set<EdgeModel>>();

		for (EdgeModel edge1 : edges)
		{
			for (EdgeModel edge2 : edges)
			{
				if (edge1 == edge2) continue;
				if (edge1.hashCode() < edge2.hashCode()) continue;

				if ((edge1.getSource() == edge2.getSource() && edge1.getTarget() == edge2.getTarget()) ||
					(edge1.getSource() == edge2.getTarget() && edge1.getTarget() == edge2.getSource()))
				{

					Set<EdgeModel> set = map.get(edge1);
					if (set == null) set = map.get(edge2);
					if (set == null) set = new HashSet<EdgeModel>();

					if (!map.containsKey(edge1)) map.put(edge1, set);
					if (!map.containsKey(edge2)) map.put(edge2, set);

					set.add(edge1);
					set.add(edge2);
				}
			}
		}

		Map<EdgeModel, Integer> multi = new HashMap<EdgeModel, Integer>();

		for (Set<EdgeModel> edgeSet : map.values())
		{
			int i = 0;
			NodeModel ref = null;
			for (EdgeModel edge : edgeSet)
			{
				int index = i++;

				if (ref == null) ref = edge.getSource();
				else if (edge.getTarget() == ref) index *= -1;

				multi.put(edge, index);
			}
		}

		return multi;
	}

	/**
	 * Creates an LNode for the given NodeModel object.
	 * 
	 * @param node		NodeModel object representing the node
	 * @param parent	parent node of the given node
	 * @param layout	layout of the graph
	 */
	private void createNode(NodeModel node,
		NodeModel parent,
		Layout layout)
	{
		LNode lNode = layout.newNode(node);
		LGraph rootGraph = layout.getGraphManager().getRoot(); 
		
		this.gvtToLayout.put(node, lNode);
		this.layoutToGvt.put(lNode, node);
		
		// get iterator for clusters of the node model
		Iterator<Cluster> clusterItr = node.getClusters().iterator();
		// copy cluster information
		while(clusterItr.hasNext())
		{
			int clusterID = clusterItr.next().getClusterID();
			lNode.addCluster(clusterID);
		}
		
		// if the node has a parent add the l-node as a child of the parent
		// l-node. Otherwise add the node to the root graph.
		
		if (parent != null)
		{
			LNode parentLNode = this.gvtToLayout.get(parent);
			assert parentLNode.getChild() != null : 
				"Parent node doesn't have child graph.";
			parentLNode.getChild().add(lNode);
			lNode.label = new String(node.getText());
		}
		else
		{
			rootGraph.add(lNode);
			lNode.label = new String(node.getText());
		}
		
		// copy geometry

		lNode.setLocation(node.getLocationAbs().x, node.getLocationAbs().y);

		// copy cluster IDs (zero means unclustered)
		
		for ( Cluster cluster : node.getClusters())
		{
			int clusterID = cluster.getClusterID();

			if (clusterID != 0)
			{
				assert clusterID > 0;
				lNode.addCluster(clusterID);
			}
		}

		// if node is a compound, recursively create child nodes

		if (node instanceof CompoundModel)
		{
			CompoundModel compoundNode = (CompoundModel)node;
			Iterator nodeIter = compoundNode.getChildren().iterator();
		
			// add new LGraph to the graph manager for the compound node
			layout.getGraphManager().add(layout.newGraph(null), lNode);
			
			// for each NodeModel in the node set create an LNode
			while (nodeIter.hasNext())
			{
				this.createNode((NodeModel)nodeIter.next(),
					compoundNode,
					layout);
			}

			lNode.updateBounds();
		}
		else
		{
			lNode.setWidth(node.getSize().width);
			lNode.setHeight(node.getSize().height);
		}
	}
	
	/**
	 * Creates an LEdge for the given EdgeModel object.
	 * 
	 * @param edge		source edge 
	 */
	private void createEdge(EdgeModel edge, Integer bendpointIndex)
	{
		LEdge lEdge = this.layout.newEdge(edge);
		
		LNode sourceLNode = this.gvtToLayout.get(edge.getSource());
		LNode targetLNode = this.gvtToLayout.get(edge.getTarget());
		
		this.layout.getGraphManager().add(lEdge, sourceLNode, targetLNode);
		
		if (bendpointIndex != null) lEdge.getBendpoints().add(
			new PointD(bendpointIndex, bendpointIndex));
	}
	
	/**
	 * This method is designed to run before performing layout.
	 */
	public void preRun()
	{
		Runnable preRun = new PreRun(this.main);
		this.main.getShell().getDisplay().syncExec(preRun);
	}

	/**
	 * This method is designed to run after the layout.
	 */
	public void postRun()
	{
		Runnable postRun = new PostRun(this.main);
		this.main.getShell().getDisplay().syncExec(postRun);
	}

	/**
	 * This method performs animation during layout if the corresponding flag is
	 * set to true.
	 */
	public void animate()
	{
		// if animation during layout is selected then animate,
		// otherwise do nothing

		if (LayoutOptionsPack.getInstance().getGeneral().animationDuringLayout)
		{
			Runnable inRun = new DuringRun(this.main);
			this.main.getShell().getDisplay().syncExec(inRun);
		}
	}
	
	/**
	 * Performs the layout by directly calling the runLayout method of the
	 * current layout.
	 */
	public void runLayout()
	{
		this.layout.runLayout();
	}

	
	/**
	 * Inner runnable class which is intended to be run before the execution of
	 * the layout operation.
	 */
	private class PreRun implements Runnable
	{
		private ChisioMain main;
		
		public PreRun(ChisioMain main)
		{
			this.main = main;
		}

		public void run()
		{
			this.main.getShell().setCursor(new Cursor(null, SWT.CURSOR_WAIT));
			
			// disable highlight and handles
			this.main.getHighlightLayer().setVisible(false);
			this.main.getHandleLayer().setVisible(false);
		}
	}
	
	/**
	 * Inner runnable class which is intended to be run before the execution of
	 * the layout operation.
	 */
	private class DuringRun implements Runnable
	{
		private ChisioMain main;
		
		public DuringRun(ChisioMain main)
		{
			this.main = main;
		}

		public void run()
		{
			new ZoomAction(main, 0, null).run();
			GraphAnimation.run(this.main.getViewer());
		}
	}
	
	/**
	 * Inner runnable class which is intended to be run before the execution of
	 * the layout operation.
	 */
	private class PostRun implements Runnable
	{
		private ChisioMain main;
		
		public PostRun(ChisioMain main)
		{
			this.main = main;
		}

		public void run()
		{
			if (LayoutOptionsPack.getInstance().getGeneral().animationOnLayout)
			{
				GraphAnimation.run(this.main.getViewer());
			}
			
			// zoom to the final layout
//			new ZoomAction(main, 0, null).run();
			
			// enable highlight and handles
			this.main.getHighlightLayer().setVisible(true);
			this.main.getHandleLayer().setVisible(true);

			this.main.getShell().setCursor(new Cursor(null, SWT.CURSOR_ARROW));
		}
	}
	
	/**
	 * Returns the singleton instance of the class.
	 * 
	 * @return	the singleton instance
	 */
	public static LayoutManager getInstance()
	{
		return LayoutManager.instance;
	}
	
// -----------------------------------------------------------------------------
// Section: Class variables
// -----------------------------------------------------------------------------
	/*
	 *  Singleton object for this class.
	 */
	private static LayoutManager instance = new LayoutManager();
}