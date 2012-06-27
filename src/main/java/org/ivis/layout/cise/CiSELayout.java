package org.ivis.layout.cise;

import java.util.*;

import org.ivis.layout.*;
import org.ivis.layout.fd.FDLayout;
import org.ivis.layout.fd.FDLayoutConstants;
import org.ivis.layout.avsdf.*;
import org.ivis.layout.cose.*;
import org.ivis.util.*;

/**
 * This class implements a Circular Spring Embedder (CiSE) layout algortithm.
 * The algorithm is used for layout of clustered nodes where nodes in each
 * cluster is drawn around a circle. The basic steps of the algorithm follows:
 * - Step 1: each cluster is laid out with AVSDF circular layout algorithm;
 * - Step 2: cluster graph (quotient graph of the clustered graph, where nodes
 *   correspond to clusters and edges correspond to inter-cluster edges) is laid
 *   out with a spring embedder to determine the initial layout;
 * - Steps 3-5: the cluster graph is laid out with a modified spring embedder,
 *   where the nodes corresponding to clusters are also allowed to rotate,
 *   indirectly affecting the layout of the nodes inside the clusters. In Step
 *   3, we allow flipping of clusters, whereas in Step 4, we allow swapping of
 *   neighboring node pairs in a cluster to improve inter-cluster edge crossings
 *   without increasing intra-cluster crossings.
 *
 * @author Esat Belviranli
 * @author Alptug Dilek
 * @author Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class CiSELayout extends FDLayout
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/**
	 * Separation of the nodes on each circle customizable by the user
	 */
	public int nodeSeperation = CiSEConstants.DEFAULT_NODE_SEPARATION;

	/**
	 * Ideal edge length coefficient for inter-cluster edges
	 */
	public double idealInterClusterEdgeLengthCoefficient =
		CiSEConstants.DEFAULT_IDEAL_INTER_CLUSTER_EDGE_LENGTH_COEFF;

	/**
	 * Decides whether pull on-circle nodes inside of the circle.
	 */
	public boolean allowNodesInsideCircle;

	/**
	 * Max percentage of the nodes in a circle that can move inside the circle
	 */
	public double maxRatioOfNodesInsideCircle;

	/**
	 * Current step of the layout process
	 */
	private int step;

	/**
	 * Current phase of current step
	 */
	private int phase;

	/**
	 * Holds the set of pairs swapped in the last swap phase.
	 */
	private Set<CiSEOnCircleNodePair> swappedPairsInLastIteration;

// -----------------------------------------------------------------------------
// Section: Constructors and initializations
// -----------------------------------------------------------------------------
	/**
	 * The constructor creates and associates with this layout a new graph
	 * manager as well.
	 */
	public CiSELayout()
	{
		super();

		this.step = CiSELayout.STEP_NOT_STARTED;
		this.phase = CiSELayout.PHASE_NOT_STARTED;
		this.swappedPairsInLastIteration = new HashSet<CiSEOnCircleNodePair>();

		this.oldTotalDisplacement = 0.0;
	}

	/*
	 * This method creates a new graph manager associated with this layout.
	 */
	protected LGraphManager newGraphManager()
	{
		LGraphManager gm = new CiSEGraphManager(this);
		this.graphManager = gm;
		return gm;
	}

	/**
	 * This method creates a new graph associated with the input view graph.
	 */
	public LGraph newGraph(Object vGraph)
	{
		return new CiSECircle(null, this.graphManager, vGraph);
	}

	/**
	 * This method creates a new node associated with the input view node.
	 */
	public LNode newNode(Object vNode)
	{
		return new CiSENode(this.graphManager, vNode);
	}

	/**
	 * This method creates a new on-circle CiSE node associated with the input
	 * view node.
	 */
	public LNode newCiSEOnCircleNode(Object vNode)
	{
		CiSENode newNode = (CiSENode) this.newNode(vNode);
		newNode.setAsOnCircleNode();

		return newNode;
	}

	/**
	 * This method creates a new edge associated with the input view edge.
	 */
	public LEdge newEdge(Object vEdge)
	{
		return new CiSEEdge(null, null, vEdge);
	}

	/**
	 * This method converts the "flat" l-level topology into one with a graph
	 * per cluster before layout starts.
	 */
	protected boolean convertToClusteredGraph()
	{
		// Check if the graph contains any compound structures; should be flat.
		if (this.graphManager.getGraphs().size() > 1)
		{
			return false;
		}

		LGraph rootGraph = this.graphManager.getRoot();

		// Following hash map stores clusters along with their nodes and
		// edges in following format
		//   <cluster ID, [nodes in this cluster, edges of this cluster]>
		// as key-value pairs.
		// Notice that edges can be intra-cluster edges or inter-graph edges
		// with exactly one end in this cluster. Such inter-graph edges are
		// always kept in the source's associated edge list (not in target's,
		// just to avoid redundancy).
		
		HashMap<String,Object[]> clusterMap = new HashMap();
		String clusterID;
		LinkedList[] nodeAndEdgeLists;

		// Go over all nodes and form clusters and store associated node lists

		for (Object obj : this.getAllNodes())
		{
			clusterID = ((LNode) obj).getClusterID();

			if (clusterID != null)
			{
				nodeAndEdgeLists = (LinkedList[]) clusterMap.get(clusterID);

				if (nodeAndEdgeLists == null)
				{
					nodeAndEdgeLists = new LinkedList[2];
					nodeAndEdgeLists[0] = new LinkedList(); // node list
					nodeAndEdgeLists[1] = new LinkedList(); // edge list

					clusterMap.put(clusterID, nodeAndEdgeLists);
				}

				nodeAndEdgeLists[0].add(obj);
			}
		}

		// Now go over all clusters and determine clusters with a single node,
		// and reset their cluster IDs

		Iterator<String> iter = clusterMap.keySet().iterator();
		LinkedList nodeList;
		LinkedList singleNodeClusterList = new LinkedList();

		while (iter.hasNext())
		{
			clusterID = iter.next();
			nodeAndEdgeLists = (LinkedList[]) clusterMap.get(clusterID);
			nodeList = nodeAndEdgeLists[0];

			if (nodeList.size() < 2)
			{
				singleNodeClusterList.add(clusterID);
				((LNode) nodeList.getFirst()).resetClusters();
			}
		}

		// Remove any single node clusters from the map as their nodes are to be
		// treated as unclustered
		
		for (Object obj : singleNodeClusterList)
		{
			clusterMap.remove(obj);
		}

		// Go over all edges to construct edge lists associated with each
		// circle (either intra-cluster edge or inter-graph edge with exactly
		// one end in this circle). Remove these edges from the root graph as
		// well. They will be inserted back after the clustered nodes have been
		// moved to their circles. Finally set each edge's intra-cluster flag
		// properly. The inter-graph flag is handled by insert methods.

		CiSEEdge edge;
		CiSENode sourceNode;
		CiSENode targetNode;
		String sourceClusterID;
		String targetClusterID;
		LinkedList edgeList;

		for (Object obj : this.getAllEdges())
		{
			edge = (CiSEEdge) obj;
			sourceNode = (CiSENode)edge.getSource();
			targetNode = (CiSENode)edge.getTarget();
			sourceClusterID = sourceNode.getClusterID();
			targetClusterID = targetNode.getClusterID();

			// Assume it is not an intra-cluster edge
			edge.isIntraCluster = false;

			if (sourceClusterID != null && targetClusterID != null)
			{
				if (sourceClusterID.equals(targetClusterID))
				// both ends are in the same cluster
				{
					edge.isIntraCluster = true;
				}

				nodeAndEdgeLists =
					(LinkedList[]) clusterMap.get(sourceClusterID);
				nodeAndEdgeLists[1].add(edge);
				rootGraph.remove(edge);
			}
			else if (sourceClusterID != null && targetClusterID == null)
			// source is in a cluster but target is not
			{
				nodeAndEdgeLists =
					(LinkedList[]) clusterMap.get(sourceClusterID);
				nodeAndEdgeLists[1].add(edge);
				rootGraph.remove(edge);
			}
			else if (sourceClusterID == null && targetClusterID != null)
			// target is in a cluster but source is not
			{
				nodeAndEdgeLists =
					(LinkedList[]) clusterMap.get(targetClusterID);
				nodeAndEdgeLists[1].add(edge);
				rootGraph.remove(edge);
			}
		}

		// Create a circle (l-graph) for each cluster and move nodes in that
		// cluster onto this circle. After we add associated removed edges as
		// well later on, we will be forming a compound structure reflecting the
		// clustered nature of this graph.

		iter = clusterMap.keySet().iterator();
		CiSECircle circle;
		CiSENode clusterNode;
		CiSENode node;

		while (iter.hasNext())
		{
			clusterID = iter.next();
			nodeAndEdgeLists = (LinkedList[]) clusterMap.get(clusterID);
			nodeList = nodeAndEdgeLists[0];

			// Create a cluster node and associated circle graph
			clusterNode = (CiSENode) this.newNode(null);
			this.graphManager.getRoot().add(clusterNode);
			circle = (CiSECircle) this.newGraph(null);
			this.graphManager.add(circle, clusterNode);

			// Set bigger margins so clusters are spaced out nicely
			circle.setMargin(circle.getMargin() + 15);

			// Move each node of the cluster into this circle
			for (Object obj : nodeList)
			{
				node = (CiSENode) obj;

				// At this point nodes should be free of any incident edges
				assert node.getEdges().size() == 0;

				node.getOwner().remove(node);
				node.setAsOnCircleNode();
				circle.add(node);

				// Initially all on-circle nodes are assumed to be in-nodes
				circle.getInNodes().add(node);
			}
		}

		// Now insert all temporarily removed edges back. Notice that some of
		// these edges are intra-cluster edges while others are inter-graph
		// edges.

		iter = clusterMap.keySet().iterator();

		while (iter.hasNext())
		{
			clusterID = iter.next();
			nodeAndEdgeLists = (LinkedList[]) clusterMap.get(clusterID);
			edgeList = nodeAndEdgeLists[1];

			for (Object obj : edgeList)
			{
				edge = (CiSEEdge) obj;

				if (edge.isIntraCluster)
				{
					edge.getSource().getOwner().
						add(edge, edge.getSource(), edge.getTarget());
					assert !edge.isInterGraph() : "Must not be an inter-graph edge!";
				}
				else
				{
					this.graphManager.
						add(edge, edge.getSource(), edge.getTarget());
					assert edge.isInterGraph() : "Must be an inter-graph edge!";
				}
			}
		}

		// Reset all nodes array of the associated graph manager so it gets
		// recalculated when inquired again.
		this.graphManager.resetAllNodes();

		// Count on- and non-on-circle nodes
		int onCircleNodeCount = 0;

		for (Object lGraph : this.graphManager.getGraphs())
		{
			if (lGraph != rootGraph)
			{
				onCircleNodeCount += ((LGraph)lGraph).getNodes().size();
			}
		}

		int nonOnCircleNodeCount = rootGraph.getNodes().size();

		// Populate and set the two arrays used for fast iteration
		CiSENode[] nonOnCircleNodes = new CiSENode[nonOnCircleNodeCount];
		CiSENode[] onCircleNodes = new CiSENode[onCircleNodeCount];
		int onCircleIndex = 0;
		int nonOnCircleIndex = 0;

		for (Object obj : this.graphManager.getAllNodes())
		{
			node = (CiSENode) obj;

			if (node.getOnCircleNodeExt() != null)
			{
				onCircleNodes[onCircleIndex] = node;
				onCircleIndex++;
			}
			else
			{
				nonOnCircleNodes[nonOnCircleIndex] = node;
				nonOnCircleIndex++;
			}
		}

		this.getCiSEGraphManager().setOnCircleNodes(onCircleNodes);
		this.getCiSEGraphManager().setNonOnCircleNodes(nonOnCircleNodes);

		// Also initialize in-circle nodes to empty array. They will be 
		// found after step 4.
		this.getCiSEGraphManager().setInCircleNodes(new CiSENode[0]);

		// Determine out-nodes of each circle

		for (Object obj : this.getGraphManager().getInterGraphEdges())
		{
			edge = (CiSEEdge)obj;
			assert edge.isInterGraph();
			sourceNode = (CiSENode)edge.getSource();
			targetNode = (CiSENode)edge.getTarget();
			sourceClusterID = sourceNode.getClusterID();
			targetClusterID = targetNode.getClusterID();
			assert sourceClusterID == null || targetClusterID == null ||
				!sourceClusterID.equals(targetClusterID);

			// If an on-circle node is an out-node, then remove it from the
			// in-node list and add it to out-node list of the associated
			// circle. Notice that one or two ends of an inter-graph edge will
			// be out-node(s).

			if (sourceClusterID != null)
			{
				circle = (CiSECircle)sourceNode.getOwner();

				// Make sure it has not been already moved to the out node list
				if (circle.getInNodes().remove(sourceNode))
				{
					circle.getOutNodes().add(sourceNode);
				}
			}

			if (targetClusterID != null)
			{
				circle = (CiSECircle)targetNode.getOwner();

				// Make sure it has not been already moved to the out node list
				if (circle.getInNodes().remove(targetNode))
				{
					circle.getOutNodes().add(targetNode);
				}
			}
		}

		return true;
	}

	/**
	 * This method is used to set all layout parameters to default values.
	 */
	public void initParameters()
	{
		super.initParameters();

		if (!this.isSubLayout)
		{
			LayoutOptionsPack.CiSE layoutOptionsPack =
				LayoutOptionsPack.getInstance().getCiSE();

			this.nodeSeperation = layoutOptionsPack.getNodeSeparation();

			this.idealEdgeLength = layoutOptionsPack.getDesiredEdgeLength();

			this.idealInterClusterEdgeLengthCoefficient =
				transform(layoutOptionsPack.getInterClusterEdgeLengthFactor(),
					CiSEConstants.DEFAULT_IDEAL_INTER_CLUSTER_EDGE_LENGTH_COEFF);
			
			this.allowNodesInsideCircle =
				layoutOptionsPack.isAllowNodesInsideCircle();

			this.maxRatioOfNodesInsideCircle =
				layoutOptionsPack.getMaxRatioOfNodesInsideCircle();
		}

		this.springConstant = CiSEConstants.DEFAULT_SPRING_STRENGTH;
		this.repulsionConstant = FDLayoutConstants.DEFAULT_REPULSION_STRENGTH;
		this.gravityConstant = FDLayoutConstants.DEFAULT_GRAVITY_STRENGTH;
		this.incremental = true;
	}

// -----------------------------------------------------------------------------
// Section: Accessors
// -----------------------------------------------------------------------------
	/**
	 * This method returns the array of all on-circle nodes.
	 */
	public CiSENode[] getOnCircleNodes()
	{
		return this.getCiSEGraphManager().getOnCircleNodes();
	}

	/**
	 * This method returns the array of all nodes other than on-circle nodes.
	 */
	public CiSENode[] getNonOnCircleNodes()
	{
		return this.getCiSEGraphManager().getNonOnCircleNodes();
	}

	/**
	 * This method returns the array of all nodes other than on-circle nodes.
	 */
	public CiSENode[] getInCircleNodes()
	{
		return this.getCiSEGraphManager().getInCircleNodes();
	}

	/**
	 * This method downcasts and returns associated graph manager.
	 */
	public CiSEGraphManager getCiSEGraphManager()
	{
		return (CiSEGraphManager)this.graphManager;
	}

	/**
	 * This method returns the node separation amount for this layout.
	 */
	public int getNodeSeparation()
	{
		return this.nodeSeperation;
	}

// -----------------------------------------------------------------------------
// Section: Layout related
// -----------------------------------------------------------------------------
	/**
	 * This method performs layout on constructed l-level graph. It returns true
	 * on success, false otherwise.
	 */
	public boolean layout()
	{
		LGraph root = this.graphManager.getRoot();

		if (!this.convertToClusteredGraph())
		{
			return false;
		}

		root.updateConnected();
		root.calcEstimatedSize();

		this.doStep1();
		this.doStep2();

		root.setEstimatedSize(root.getBiggerDimension());
		this.prepareCirclesForReversal();
		this.calcIdealEdgeLengths(false);

		this.doStep5(); //stabilize before reversals take place
		this.doStep3();
		this.doStep5(); //stabilize before swaps take place
		this.doStep4();

		this.findAndMoveInnerNodes();
		this.calcIdealEdgeLengths(true);

		this.doStep5();

		System.out.println("CiSE layout finished after " +
			this.totalIterations + " iterations");

		return true;
	}

	/**
	 * This method runs AVSDF layout for each cluster.
	 */
	public void doStep1()
	{
		this.step = CiSELayout.STEP_1;
		this.phase = CiSELayout.PHASE_OTHER;
		List clusteredNodes;
		AVSDFNode avsdfNode;
		AVSDFEdge avsdfEdge;
		
		// We need this mapping for transferring positions and dimensions back 
		HashMap<CiSENode, AVSDFNode> ciseToAvsdf = 
			new HashMap<CiSENode, AVSDFNode> ();
		
		for (Object graph : this.graphManager.getGraphs())
		{
			LGraph lgraph = (LGraph)graph;

			if (lgraph == this.graphManager.getRoot())
			{
				assert lgraph.getParent().getOwner() == null;
				continue;
			}

			// Create an AVSDF layout object
			AVSDFLayout avsdfLayout = new AVSDFLayout();
			avsdfLayout.isSubLayout = true;
			avsdfLayout.setNodeSeparation(this.nodeSeperation);
			AVSDFCircle avsdfCircle = 
				(AVSDFCircle) avsdfLayout.getGraphManager().addRoot();

			CiSECircle ciseCircle = (CiSECircle)lgraph;
			clusteredNodes = ciseCircle.getOnCircleNodes();

			// Create corresponding AVSDF nodes in current cluster
			PointD loc;

			for (Object node : clusteredNodes)
			{
				CiSENode ciseOnCircleNode = (CiSENode) node;
				avsdfNode = (AVSDFNode)
					avsdfLayout.newNode(ciseOnCircleNode.vGraphObject);
				loc = ciseOnCircleNode.getLocation();
				avsdfNode.setLocation(loc.x, loc.y);
				avsdfNode.setWidth(ciseOnCircleNode.getWidth());
				avsdfNode.setHeight(ciseOnCircleNode.getHeight());

				avsdfCircle.add(avsdfNode);
				ciseToAvsdf.put(ciseOnCircleNode, avsdfNode);
			}

			// For each edge, create a corresponding AVSDF edge if its both ends
			// are in this cluster.

			for (Object edge : this.getAllEdges())
			{
				CiSEEdge ciseEdge = (CiSEEdge) edge;

				if (clusteredNodes.contains(ciseEdge.getSource()) &&
					clusteredNodes.contains(ciseEdge.getTarget()))
				{
					AVSDFNode avsdfSource =
						(AVSDFNode)ciseToAvsdf.get(ciseEdge.getSource());
					AVSDFNode avsdfTarget =
						(AVSDFNode)ciseToAvsdf.get(ciseEdge.getTarget());
					avsdfEdge = (AVSDFEdge) avsdfLayout.newEdge("");

					avsdfCircle.add(avsdfEdge, avsdfSource, avsdfTarget);
				}
			}

			// Call AVSDF layout for this cluster
			avsdfLayout.runLayout();

			// Reflect changes back to CiSENode's

			for (Object node : clusteredNodes)
			{
				CiSENode ciseOnCircleNode = (CiSENode) node;
				avsdfNode = (AVSDFNode)ciseToAvsdf.get(ciseOnCircleNode);
				loc = avsdfNode.getLocation();
				ciseOnCircleNode.setLocation(loc.x, loc.y);
				ciseOnCircleNode.getOnCircleNodeExt().setIndex(avsdfNode.getIndex());
				ciseOnCircleNode.getOnCircleNodeExt().setAngle(avsdfNode.getAngle());
			}

			// Sort nodes of this ciseCircle according to circle indexes of
			// ciseOnCircleNodes.
			CiSENodeSort sorter = new CiSENodeSort(clusteredNodes);
			sorter.quicksort();

			// Assign width and height of the AVSDF circle containing the nodes
			// above to the corresponding cise-circle.
			if (avsdfCircle.getNodes().size() > 0)
			{
				LNode parentCiSE = ciseCircle.getParent();
				LNode parentAVSDF = avsdfCircle.getParent();
				parentCiSE.setLocation(parentAVSDF.getLocation().x,
					parentAVSDF.getLocation().y);
				ciseCircle.setRadius(avsdfCircle.getRadius());
				ciseCircle.calculateParentNodeDimension();
			}
		}
	}

	/**
	 * This method runs a spring embedder on the cluster-graph (quotient graph
	 * of the clustered graph) to determine initial layout.
	 */
	public void doStep2()
	{
		this.step = CiSELayout.STEP_2;
		this.phase = CiSELayout.PHASE_OTHER;
		List<CoSENode> newCoSENodes = new ArrayList<CoSENode>();
		List<CoSEEdge> newCoSEEdges = new ArrayList<CoSEEdge>();
		CoSENode newNode;
		CoSEEdge newEdge;

		// Used for holding conversion mapping between cise and cose nodes.
		HashMap<CiSENode, CoSENode> ciseNodeToCoseNode =
			new HashMap<CiSENode, CoSENode>();

		// Used for reverse mapping between cose and cise edges while sorting
		// incident edges.
		HashMap<CoSEEdge, Set<CiSEEdge>> coseEdgeToCiseEdges =
			new HashMap<CoSEEdge, Set<CiSEEdge>>();

		// Create a CoSE layout object
		CoSELayout coseLayout = new CoSELayout();
		coseLayout.isSubLayout = true;
		coseLayout.useMultiLevelScaling = false;
		coseLayout.useFRGridVariant = true;
		coseLayout.springConstant *= 1.5;
		LGraph coseRoot = coseLayout.getGraphManager().addRoot();

		// Traverse through all nodes and create new CoSENode's.
		CiSENode[] nonOnCircleNodes = this.getNonOnCircleNodes();
		PointD loc;

		for (int i = 0; i < nonOnCircleNodes.length; i++)
		{
			// Copy required fields of this node.
			CiSENode ciseNode = nonOnCircleNodes[i];

			newNode = (CoSENode) coseLayout.newNode(ciseNode.vGraphObject);
			loc = ciseNode.getLocation();
			newNode.setLocation(loc.x, loc.y);
			newNode.setWidth(ciseNode.getWidth());
			newNode.setHeight(ciseNode.getHeight());

			// Set nodes corresponding to circles to be larger than original, so
			// inter-cluster edges end up longer.

			if (ciseNode.getChild() != null)
			{
				newNode.setWidth(1.2 * newNode.getWidth());
				newNode.setHeight(1.2 * newNode.getHeight());
			}

			coseRoot.add(newNode);
			newCoSENodes.add(newNode);
			ciseNodeToCoseNode.put(ciseNode, newNode);
		}

		// Used for preventing duplicate edge creation between two cose nodes
		CoSEEdge[][] nodePairs =
			new CoSEEdge[newCoSENodes.size()][newCoSENodes.size()];

		// Traverse through edges and create cose edges for inter-cluster ones.

		Object[] allEdges = this.graphManager.getAllEdges();

		for (int i = 0; i < allEdges.length; i++ )
		{
			CiSEEdge ciseEdge = (CiSEEdge) allEdges[i];
			CiSENode sourceCise = (CiSENode) ciseEdge.getSource();
			CiSENode targetCise = (CiSENode) ciseEdge.getTarget();

			// Determine source and target nodes for current edge

			if (sourceCise.getOnCircleNodeExt() != null)
			// Source node is an on-circle node, take its parent as source node
			{
				sourceCise =
					(CiSENode)ciseEdge.getSource().getOwner().getParent();
			}

			if (targetCise.getOnCircleNodeExt() != null)
			// Target node is an on-circle node, take its parent as target node
			{
				targetCise =
					(CiSENode)ciseEdge.getTarget().getOwner().getParent();
			}

			CoSENode sourceCose = ciseNodeToCoseNode.get(sourceCise);
			CoSENode targetCose = ciseNodeToCoseNode.get(targetCise);

			assert (sourceCose != null) && (targetCose != null);

			int sourceIndex = newCoSENodes.indexOf(sourceCose);
			int targetIndex = newCoSENodes.indexOf(targetCose);

			if (sourceIndex != targetIndex)
			// Make sure it's an inter-cluster edge
			{
				if (nodePairs[sourceIndex][targetIndex] == null &&
					nodePairs[targetIndex][sourceIndex] == null)
				{
					newEdge = (CoSEEdge) coseLayout.newEdge("");
					coseRoot.add(newEdge, sourceCose, targetCose);
					newCoSEEdges.add(newEdge);

					coseEdgeToCiseEdges.put(newEdge, new HashSet<CiSEEdge>());

					nodePairs[sourceIndex][targetIndex] = newEdge;
					nodePairs[targetIndex][sourceIndex] = newEdge;
				}
				else
				{
					assert nodePairs[sourceIndex][targetIndex] ==
						nodePairs[targetIndex][sourceIndex];

					newEdge =  nodePairs[sourceIndex][targetIndex];
				}

				coseEdgeToCiseEdges.get(newEdge).add(ciseEdge);
			}
		}

		this.reorderIncidentEdges(ciseNodeToCoseNode, coseEdgeToCiseEdges);

		// Call CoSE layout
		coseLayout.runLayout();

//		coseLayout.transform();
//		GraphMLWriter graphMLWriter = new GraphMLWriter("E:\\cluster.graphml");
//		graphMLWriter.saveGraph(coseLayout.getGraphManager());

		// Reflect changes back to cise nodes

		// First update all non-on-circle nodes.
		nonOnCircleNodes = this.getNonOnCircleNodes();

		for (int i = 0; i < nonOnCircleNodes.length; i++)
		{
			CiSENode ciseNode = nonOnCircleNodes[i];

			CoSENode coseNode = ciseNodeToCoseNode.get(ciseNode);
			loc = coseNode.getLocation();
			ciseNode.setLocation(loc.x, loc.y);
		}

		// Then update all cise on-circle nodes, since their parents have
		// changed location.

		CiSENode[] onCircleNodes = this.getOnCircleNodes();
		CiSENode ciseNode;
		PointD parentLoc;

		for (int i = 0; i < onCircleNodes.length; i++)
		{
			ciseNode = onCircleNodes[i];
			loc = ciseNode.getLocation();
			parentLoc = ciseNode.getOwner().getParent().getLocation();
			ciseNode.setLocation(loc.x + parentLoc.x, loc.y + parentLoc.y);
		}
	}

	/**
	 * This method sorts incident lists of cose nodes created earlier according
	 * to node ordering inside corresponding cise circles, if any. For each cose
	 * edge we have one or possibly more cise edges. Let's look up their indices
	 * and somehow do a smart calculation of their average. So if this cluster A
	 * is connected to cluster B via on-circle nodes indexed at 3, 6, and 12,
	 * then we may imagine that cluster B should be aligned with the node
	 * indexed at 7 [=(3+6+12)/3]. The input parameters reference the hash maps
	 * maintaining correspondence between cise and cose nodes (1-1) and cose and
	 * cise edges (1-many), respectively.
	 **/
	private void reorderIncidentEdges(
		HashMap<CiSENode, CoSENode> ciseNodeToCoseNode,
		HashMap<CoSEEdge, Set<CiSEEdge>> coseEdgeToCiseEdges)
	{
		CiSENode[] nonOnCircleNodes = this.getNonOnCircleNodes();

		for (int i = 0; i < nonOnCircleNodes.length; i++)
		{
			if (nonOnCircleNodes[i].getChild() == null)
			{
				continue;
			}

			CiSECircle ciseCircle = (CiSECircle) nonOnCircleNodes[i].getChild();
			int mod = ciseCircle.getOnCircleNodes().size();
			CoSENode coseNode = ciseNodeToCoseNode.get(ciseCircle.getParent());
			List incidentCoseEdges =  coseNode.getEdges();
			Map<Object, Double> indexMapping = new HashMap<Object, Double>();

			for (int j = 0; j < incidentCoseEdges.size(); j++)
			{
				CoSEEdge coseEdge = (CoSEEdge) incidentCoseEdges.get(j);

				List<Object> edgeIndices = new ArrayList<Object>();
				Set<CiSEEdge> ciseEdges = coseEdgeToCiseEdges.get(coseEdge);
				Iterator<CiSEEdge> edgeIter = ciseEdges.iterator();

				while (edgeIter.hasNext())
				{
					CiSEEdge ciseEdge = edgeIter.next();
					int edgeIndex = -1;

					if (ciseEdge.getSource().getOwner() == ciseCircle)
					{
						edgeIndex = ((CiSENode)	ciseEdge.getSource()).
							getOnCircleNodeExt().getIndex();
					}
					else if (ciseEdge.getTarget().getOwner() == ciseCircle)
					{
						edgeIndex = ((CiSENode) ciseEdge.getTarget()).
							getOnCircleNodeExt().getIndex();
					}

					assert edgeIndex != -1;

					edgeIndices.add(new Integer(edgeIndex));
				}

				IntegerQuickSort intSort = new IntegerQuickSort(edgeIndices);
				intSort.quicksort();

				// When averaging indices, we need to make sure it falls to the
				// correct side, simple averaging will not always work. For
				// instance, if indices are 0, 1, and 5 for a 6 node circle /
				// cluster, we want the average to be 0 [=(0+1+(-1))/3] as
				// opposed to 2 [=(0+1+5)/3]. We need to calculate the largest
				// gap between adjacent indices (1 to 5 in this case) here.
				// Indices after the start of the largest gap are to be adjusted
				// (by subtracting mod from each), so the average falls into the
				// correct side.

				int indexLargestGapStart = -1;
				int largestGap = -1;
				int gap;

				// calculate largest gap and its starting index

				Iterator indexIter = edgeIndices.iterator();
				Integer edgeIndex = null;
				Integer prevEdgeIndex;
				Integer firstEdgeIndex = -1;
				int edgeIndexPos = -1;

				while (indexIter.hasNext())
				{
					prevEdgeIndex = edgeIndex;
					edgeIndex = (Integer) indexIter.next();
					edgeIndexPos++;

					if (prevEdgeIndex != null)
					{
						gap = edgeIndex - prevEdgeIndex;

						assert gap >= 0;

						if (gap > largestGap)
						{
							largestGap = gap;
							indexLargestGapStart = edgeIndexPos - 1;
						}
					}
					else
					{
						firstEdgeIndex = edgeIndex;
					}
				}

				if (firstEdgeIndex != -1 &&
					(firstEdgeIndex + mod - edgeIndex) > largestGap)
				{
					largestGap = firstEdgeIndex + mod - edgeIndex;
					indexLargestGapStart = edgeIndexPos;
					assert indexLargestGapStart == edgeIndices.size() - 1;
				}

				// adjust indices after the start of the gap (beginning with the
				// index that marks the end of the largest gap)

				int edgeCount = edgeIndices.size();

				assert edgeCount != 0;

				if (largestGap > 0)
				{
					Integer index;

					for (int k = indexLargestGapStart + 1; k < edgeCount; k++)
					{
						index = (Integer) edgeIndices.get(k);
						edgeIndices.set(k, index - mod);
					}
				}

				// Sum up indices

				double averageIndex;
				double totalIndex = 0;
				indexIter = edgeIndices.iterator();

				while (indexIter.hasNext())
				{
					edgeIndex = (Integer) indexIter.next();
					totalIndex += edgeIndex;
				}

				averageIndex = totalIndex / edgeCount;

				if (averageIndex < 0)
				{
					averageIndex += mod;
				}

				indexMapping.put(coseEdge, averageIndex);
			}

			IndexedObjectSort sort =
				new IndexedObjectSort(incidentCoseEdges, indexMapping);
			sort.quicksort();
		}
	}

	/**
	 * This method calculates the ideal edge length of each edge. Here we relax
	 * edge lengths in the polishing step and keep the edge lengths of the edges
	 * incident with inner-nodes very short to avoid overlaps.
	 */
	protected void calcIdealEdgeLengths(boolean isPolishingStep)
	{
		Object[] lEdges = this.getAllEdges();
		CiSEEdge edge;

		for (int i = 0; i < lEdges.length; i++)
		{
			edge = (CiSEEdge) lEdges[i];

			// Loosen in the polishing step to avoid overlaps
			if (isPolishingStep)
			{
				edge.idealLength = 1.5 * this.idealEdgeLength *
					this.idealInterClusterEdgeLengthCoefficient;
//				edge.idealLength = this.idealEdgeLength *
//					this.calcIdealEdgeLengthFactor(edge) *
//						this.idealInterClusterEdgeLengthCoefficient;
			}
			else
			{
				edge.idealLength = this.idealEdgeLength *
					this.idealInterClusterEdgeLengthCoefficient;
			}
		}

		Object[] lNodes = this.getInCircleNodes();
		CiSENode node;

		for (int i = 0; i < lNodes.length; i++)
		{
			node = (CiSENode) lNodes[i];

			for (Object obj : node.getEdges())
			{
				edge = (CiSEEdge) obj;
				edge.idealLength = CiSEConstants.DEFAULT_INNER_EDGE_LENGTH;
			}
		}
	}

	double calcIdealEdgeLengthFactor(CiSEEdge edge)
	{
		if (edge.isIntraCluster)
		{
			return 1.5;
		}

		LGraph rootGraph = this.getGraphManager().getRoot();
		CiSECircle srcCluster = (CiSECircle)edge.getSource().getOwner();
		CiSECircle trgCluster = (CiSECircle)edge.getTarget().getOwner();
		int srcSize;
		int trgSize;

		if (srcCluster == rootGraph)
		{
			srcSize = 1;
		}
		else
		{
			srcSize = srcCluster.getNodes().size();
		}

		if (trgCluster == rootGraph)
		{
			trgSize = 1;
		}
		else
		{
			trgSize = trgCluster.getNodes().size();
		}

		int totalSize = srcSize + trgSize;

		if (totalSize <= 8)
		{
			return 1.5;
		}
		
		return 0.12 * totalSize;
	}

	/**
	 * This method runs a modified spring embedder as described by the CiSE
	 * layout algorithm where the on-circle nodes are fixed (pinned down to
	 * the location on their owner circle). Circles, however, are allowed to be
	 * flipped (i.e. nodes are re-ordered in the reverse direction) if reversal
	 * yields a better aligned neighborhood (w.r.t. its inter-graph edges).
	 */
	public void doStep3()
	{
//		System.out.println("Phase 3 started...");
		this.step = CiSELayout.STEP_3;
		this.phase = CiSELayout.PHASE_OTHER;
		this.initSpringEmbedder();
		this.runSpringEmbedder();
	}

	/**
	 * This method runs a modified spring embedder as described by the CiSE
	 * layout algorithm where the neighboring on-circle nodes are allowed to
	 * move by swapping without increasing crossing number but circles are not
	 * allowed to be flipped.
	 */
	public void doStep4()
	{
//		System.out.println("Phase 4 started...");
		this.step = CiSELayout.STEP_4;
		this.phase = CiSELayout.PHASE_OTHER;
		this.initSpringEmbedder();
		this.runSpringEmbedder();
	}

	/**
	 * This method runs a modified spring embedder as described by the CiSE
	 * layout algorithm where the on-circle nodes are fixed (pinned down to
	 * the location on their owner circle) and circles are not allowed to be
	 * flipped.
	 */
	public void doStep5()
	{
//		System.out.println("Phase 5 started...");
		this.step = CiSELayout.STEP_5;
		this.phase = CiSELayout.PHASE_OTHER;
		this.initSpringEmbedder();
		this.runSpringEmbedder();
	}

	/*
	 * This method implements a spring embedder used by steps 3 thru 5 with
	 * potentially different parameters.
	 */
	private void runSpringEmbedder()
	{
		if (this.step == CiSELayout.STEP_4)
		{
			for (int i = 0; i < this.getOnCircleNodes().length ; i++)
			{
				this.getOnCircleNodes()[i].getOnCircleNodeExt().
					updateSwappingConditions();
			}
		}

		this.totalDisplacement = 1000;
		int iterations = 0;

		do
		{
			iterations++;

			if (iterations % FDLayoutConstants.CONVERGENCE_CHECK_PERIOD == 0)
			{
				// In step 4 make sure at least a 1/4 of max iters take place
				boolean notTooEarly = this.step != CiSELayout.STEP_4 ||
					iterations > this.maxIterations / 4;

				if (notTooEarly && this.isConverged())
				{
					break;
				}

				this.coolingFactor = this.initialCoolingFactor *
					((this.maxIterations - iterations) / (double)this.maxIterations);
			}

			this.totalDisplacement = 0;

			if (this.step == CiSELayout.STEP_3)
			{
				if (iterations % CiSEConstants.REVERSE_PERIOD == 0)
				{
					this.checkAndReverseIfReverseIsBetter();
				}
			}
			else if (this.step == CiSELayout.STEP_4)
			{
				// clear history every now and then
				if (iterations % CiSEConstants.SWAP_HISTORY_CLEARANCE_PERIOD == 0)
				{
					this.swappedPairsInLastIteration.clear();
				}

				// no of iterations in this swap period
				int iterationInPeriod = iterations % CiSEConstants.SWAP_PERIOD;

				if (iterationInPeriod >= CiSEConstants.SWAP_IDLE_DURATION)
				{
					this.phase = CiSELayout.PHASE_SWAP_PREPERATION;
				}
				else if (iterationInPeriod == 0)
				{
					this.phase = CiSELayout.PHASE_PERFORM_SWAP;
				}
				else
				{
					this.phase = PHASE_OTHER;
				}
			}

			this.calcSpringForces();
			this.calcRepulsionForces();
			this.calcGravitationalForces();
			this.calcTotalForces();
			this.moveNodes();

			this.animate();
		}
		while (iterations < this.maxIterations);

		this.totalIterations += iterations;
	}

	/**
	 * This method calculates the spring forces applied to end nodes of each
	 * edge. In steps 3 & 5, where on-circle nodes are not allowed to move,
	 * intra-cluster edges are ignored (as their total will equal zero and won't
	 * have an affect on the owner circle).
	 */
	public void calcSpringForces()
	{
		Object[] lEdges = this.getAllEdges();
		CiSEEdge edge;
		CiSENode source;
		CiSENode target;

		for (int i = 0; i < lEdges.length; i++)
		{
			edge = (CiSEEdge) lEdges[i];
			source = (CiSENode) edge.getSource();
			target = (CiSENode) edge.getTarget();

			// Ignore intra-cluster edges (all steps 3 thru 5) except for those
			// incident w/ any inner-nodes
			
			if (edge.isIntraCluster &&
				source.getOnCircleNodeExt() != null &&
				target.getOnCircleNodeExt() != null)
			{
				continue;
			}

			this.calcSpringForce(edge, edge.idealLength);
		}
	}

	/**
	 * This method calculates the repulsion forces for each pair of nodes.
	 * Repulsions need not be calculated for on-circle nodes.
	 */
	public void calcRepulsionForces()
	{
		int i, j;
		CiSENode nodeA, nodeB;
		Object[] lNodes = this.getNonOnCircleNodes();

		for (i = 0; i < lNodes.length; i++)
		{
			nodeA = (CiSENode) lNodes[i];

			for (j = i + 1; j < lNodes.length; j++)
			{
				nodeB = (CiSENode) lNodes[j];

				assert nodeA.getOnCircleNodeExt() == null;
				assert nodeB.getOnCircleNodeExt() == null;

				this.calcRepulsionForce(nodeA, nodeB);
			}
		}
		
		// We need the calculate repulsion forces for in-circle nodes as well
		// to keep them inside circle.
		CiSENode[] inCircleNodes = this.getInCircleNodes();

		for (CiSENode inCircleNode : inCircleNodes)
		{
			CiSECircle ownerCircle = (CiSECircle) inCircleNode.getOwner();

			//TODO: inner nodes repulse on-circle nodes as well, not desired!
			// Calculate repulsion forces with all nodes inside the owner circle
			// of this inner node.

			for (Object childNode : ownerCircle.getNodes())
			{
				CiSENode childCiSENode = (CiSENode) childNode;

				if (childCiSENode != inCircleNode)
				{
					this.calcRepulsionForce(inCircleNode, childCiSENode);
				}
			}
		}
	}

	/**
	 * This method calculates the gravitational forces for each node. On-circle
	 * nodes move with their owner; thus they are not applied separate gravity.
	 */
	public void calcGravitationalForces()
	{
		CiSENode node;
		Object[] lNodes;

		if (!this.getGraphManager().getRoot().isConnected())
		{
			lNodes = this.getNonOnCircleNodes();

			for (int i = 0; i < lNodes.length; i++)
			{
				node = (CiSENode) lNodes[i];

				this.calcGravitationalForce(node);
			}
		}
		
		// Calculate gravitational forces to keep in-circle nodes in the center
		// TODO: is this really helping or necessary?
		lNodes = this.getInCircleNodes();

		for (int i = 0; i < lNodes.length; i++)
		{
			node = (CiSENode) lNodes[i];

			this.calcGravitationalForce(node);
		}

	}

	/**
	 * This method adds up all the forces calculated earlier transferring forces
	 * of on-circle nodes to their owner node (as regular and rotational forces)
	 * when they are not allowed to move. When they are allowed to move,
	 * on-circle nodes will partially contribute to the forces of their owner
	 * circle (no rotational contribution).
	 */
	public void calcTotalForces()
	{
		Object[] allNodes = this.getAllNodes();

		for (int i = 0; i < allNodes.length; i++)
		{
			CiSENode node = (CiSENode)allNodes[i];

//			System.out.printf("\t:s=(%5.1f,%5.1f) r=(%5.1f,%5.1f) g=(%5.1f,%5.1f)\n",
//				new Object [] {
//				node.springForceX, node.springForceY,
//				node.repulsionForceX, node.repulsionForceY,
//				node.gravitationForceX, node.gravitationForceY});

			node.displacementX = this.coolingFactor *
				(node.springForceX + node.repulsionForceX +
					node.gravitationForceX);
			node.displacementY = this.coolingFactor *
				(node.springForceY + node.repulsionForceY +
					node.gravitationForceY);
			node.rotationAmount = 0.0;

			node.springForceX = 0.0;
			node.springForceY = 0.0;
			node.repulsionForceX = 0.0;
			node.repulsionForceY = 0.0;
			node.gravitationForceX = 0.0;
			node.gravitationForceY = 0.0;
		}

//		System.out.println();

		CiSENode[] onCircleNodes = this.getOnCircleNodes();
		CiSENode node;

		for (int i = 0; i < onCircleNodes.length; i++)
		{
			node = onCircleNodes[i];
			CiSENode parentNode = (CiSENode)(node.getOwner().getParent());
			CircularForce values =
				((CiSECircle)(node.getOwner())).decomposeForce(node);

			if (this.phase == CiSELayout.PHASE_SWAP_PREPERATION)
			{
				node.getOnCircleNodeExt().addDisplacementForSwap(
					values.getRotationAmount());
			}

			parentNode.displacementX += values.getDisplacementX();
			parentNode.displacementY += values.getDisplacementY();
			node.displacementX = 0.0;
			node.displacementY = 0.0;

			parentNode.rotationAmount += values.getRotationAmount();
			node.rotationAmount = 0.0;
		}
	}

	/**
	 * This method updates positions of each node at the end of an iteration.
	 * Also, it deals with swapping of two consecutive nodes on a circle in
	 * step 4.
	 */
	public void moveNodes()
	{
		if (this.phase != PHASE_PERFORM_SWAP)
		{
			CiSENode[] nonOnCircleNodes = this.getNonOnCircleNodes();

			// Simply move all non-on-circle nodes.

			for (int i = 0; i < nonOnCircleNodes.length; i++)
			{
				assert nonOnCircleNodes[i].getOnCircleNodeExt() == null;

				nonOnCircleNodes[i].move();

				// Also make required rotations for circles
				if (nonOnCircleNodes[i].getChild() != null)
				{
					((CiSECircle)nonOnCircleNodes[i].getChild()).rotate();
				}
			}

			// Also move all in-circle nodes. Note that in-circle nodes will be
			// empty if this option is not set, hence no negative effect on
			// performance

			CiSENode[] inCircleNodes = this.getInCircleNodes();
			CiSENode inCircleNode;

			for (int i = 0; i < inCircleNodes.length; i++)
			{
				inCircleNode = inCircleNodes[i];
				assert inCircleNode.getOnCircleNodeExt() == null;
				// TODO: workaround to force inner nodes to stay inside
				inCircleNode.displacementX /= 20.0;
				inCircleNode.displacementY /= 20.0;
				inCircleNode.move();
			}
		}
		else
		// If in perform-swap phase of step 4, we have to look for swappings
		// that do not increase edge crossings and is likely to decrease total
		// energy.
		{
			assert this.step == CiSELayout.STEP_4;

			CiSENode[] ciseOnCircleNodes = this.getOnCircleNodes();
			int size = ciseOnCircleNodes.length;

			// Both nodes of a pair are out-nodes, not necessarilly safe due to
			// inter-cluster edge crossings
			TreeSet<CiSEOnCircleNodePair> nonSafePairs =
				new TreeSet<CiSEOnCircleNodePair>();

			// Pairs where one of the on circle nodes is an in-node; no problem
			// swapping these
			ArrayList<CiSEOnCircleNodePair> safePairs =
				new ArrayList<CiSEOnCircleNodePair>();

			// Nodes swapped in this round
			Set<CiSENode> swappedNodes = new HashSet<CiSENode>();

			// Pairs swapped or prevented from being swapped in this round
			Set<CiSEOnCircleNodePair> swappedPairs =
				new HashSet<CiSEOnCircleNodePair>();

			CiSENode firstNode;
			CiSENode secondNode;
			CiSEOnCircleNodeExt firstNodeExt;
			CiSEOnCircleNodeExt secondNodeExt;
			double firstNodeDisp;
			double secondNodeDisp;
			double discrepancy;
			boolean inSameDirection;

			// Check each node with its next node for swapping
			for (int i = 0; i < size; i++)
			{
				firstNode = ciseOnCircleNodes[i];
				secondNode = firstNode.getOnCircleNodeExt().getNextNode();
				firstNodeExt = firstNode.getOnCircleNodeExt();
				secondNodeExt = secondNode.getOnCircleNodeExt();

				// Ignore if the swap is to introduce new intra-edge crossings

				if (!firstNodeExt.canSwapWithNext() ||
					!secondNodeExt.canSwapWithPrev())
				{
					continue;
				}

				firstNodeDisp = firstNodeExt.getDisplacementForSwap();
				secondNodeDisp = secondNodeExt.getDisplacementForSwap();
				discrepancy = firstNodeDisp - secondNodeDisp;

				// Pulling in reverse directions, no swap

				if (discrepancy < 0.0)
				{
					continue;
				}

				// Might swap, create safe or nonsafe node pairs

				inSameDirection = (firstNodeDisp > 0 && secondNodeDisp > 0) ||
					(firstNodeDisp < 0 && secondNodeDisp < 0);
				CiSEOnCircleNodePair pair =
					new CiSEOnCircleNodePair(firstNode,
						secondNode,
						discrepancy,
						inSameDirection);

				// When both are out-nodes, nonsafe; otherwise, safe

				if (firstNodeDisp == 0.0 || secondNodeDisp == 0.0)
				{
					safePairs.add(pair);
				}
				else
				{
					nonSafePairs.add(pair);
				}
			}

			CiSEOnCircleNodePair nonSafePair;
			boolean lookForSwap = true;
			boolean rollback;

			// Look for a nonsafe pair until we swap one
			while (lookForSwap && nonSafePairs.size() > 0)
			{
				// Pick the non safe pair that has the maximum discrepancy.
				nonSafePair = nonSafePairs.last();
				firstNode = nonSafePair.getFirstNode();
				secondNode = nonSafePair.getSecondNode();
				firstNodeExt = firstNode.getOnCircleNodeExt();
				secondNodeExt = secondNode.getOnCircleNodeExt();

				// If this pair is swapped in previous swap phase, don't allow
				// this swap. Also save it for the future as if it is actually
				// swapped in order to prevent future oscilations

				if (this.isSwappedPreviously(nonSafePair))
				{
					nonSafePairs.remove(nonSafePair);
					swappedPairs.add(nonSafePair);
					continue;
				}

				// Check for inter-cluster edge crossings before swapping.
				int int1 =
					firstNodeExt.getInterClusterIntersections(secondNodeExt);

				// Try a swap
				nonSafePair.swap();
				rollback = false;

				// Then re-compute crossings
				int int2 =
					firstNodeExt.getInterClusterIntersections(secondNodeExt);

				// Possible cases regarding discrepancy:
				// first  second  action
				// +      +       both clockwise: might swap if disp > 0
				// +      -       disp > 0: might swap
				// -      -       both counter-clockwise: might swap if disp > 0
				// -      +       disp <= 0: no swap

				assert (nonSafePair.getDiscrepancy() >= 0.0);

				// Under following conditions roll swap back:
				// - swap increases inter-cluster edge crossings
				// - inter-cluster edge number is the same but pulling in the
				// same direction or discrepancy is below pre-determined
				// threshold (not enough for swap)

				rollback = int2 > int1;

				if (!rollback && int2 == int1)
				{
					rollback =
						nonSafePair.inSameDirection() ||
						nonSafePair.getDiscrepancy() <
							CiSEConstants.MIN_DISPLACEMENT_FOR_SWAP;
				}

				if (rollback)
				{
					nonSafePair.swap();
					nonSafePairs.remove(nonSafePair);
					continue;
				}

//				System.out.println("! Nonsafe " + nonSafePair);
				swappedNodes.add(nonSafePair.getFirstNode());
				swappedNodes.add(nonSafePair.getSecondNode());
				swappedPairs.add(nonSafePair);

				// Swap performed, do not look for another nonsafe pair
				lookForSwap = false;
			}

			// Now process all safe pairs
			Iterator<CiSEOnCircleNodePair> iter = safePairs.iterator();

			while (iter.hasNext())
			{
				CiSEOnCircleNodePair safePair = iter.next();

				// Check if discrepancy is above the threshold (enough to swap)
				if (safePair.inSameDirection() ||
					safePair.getDiscrepancy() <
						CiSEConstants.MIN_DISPLACEMENT_FOR_SWAP)
				{
					continue;
				}

				// Check if they were already involved in a swap in this phase
				if (swappedNodes.contains(safePair.getFirstNode()) ||
					swappedNodes.contains(safePair.getSecondNode()))
				{
					continue;
				}

				// Should be swapped if not previously swapped; so
				// Check if they were previously swapped
				if (!this.isSwappedPreviously(safePair))
				{
						safePair.swap();
//						System.out.println("! Safe "+safePair);
						swappedNodes.add(safePair.getFirstNode());
						swappedNodes.add(safePair.getSecondNode());
				}

				// Mark swapped (even if not) to prevent future oscillations
				swappedPairs.add(safePair);
			}

			// Update swap history
			this.swappedPairsInLastIteration.clear();
			this.swappedPairsInLastIteration.addAll(swappedPairs);

			// Reset all discrepancy values of on circle nodes.
			CiSENode node;

			for (int i = 0; i < size; i++)
			{
				node = ciseOnCircleNodes[i];
				node.getOnCircleNodeExt().setDisplacementForSwap(0.0);
				assert ciseOnCircleNodes[i].rotationAmount == 0.0;
			}
		}
	}

	/*
	 * This method returns whether or not the input node pair was previously
	 * swapped.
	 */
	private boolean isSwappedPreviously(CiSEOnCircleNodePair pair)
	{
		Iterator<CiSEOnCircleNodePair> iter =
			this.swappedPairsInLastIteration.iterator();
		CiSEOnCircleNodePair swappedPair;

		while (iter.hasNext())
		{
			swappedPair = iter.next();

			if ((swappedPair.getFirstNode() == pair.getFirstNode() &&
				swappedPair.getSecondNode() == pair.getSecondNode()) ||
				(swappedPair.getSecondNode() == pair.getFirstNode() &&
				swappedPair.getFirstNode() == pair.getSecondNode()))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * This method finds and forms a list of nodes for which gravitation should
	 * be applied. Since gravitation is normally applied to non-on-circle nodes
	 * only, we find out whether the root graph is connected or not to determine
	 * the list of nodes to apply gravitation to.
	 */
	public void calculateNodesToApplyGravitationTo()
	{
		CiSEGraphManager gm = (CiSEGraphManager)this.graphManager;
		LGraph root = gm.getRoot();
		assert gm.getGraphs().get(0) == root;

		root.updateConnected();

		if (!root.isConnected())
		{
			gm.setAllNodesToApplyGravitation(gm.getOnCircleNodes());
		}
		else
		{
			// no nodes to apply gravitation to, thus an empty list
			gm.setAllNodesToApplyGravitation(new LinkedList());
		}
	}

	/**
	 * This method prepares circles for possible reversal by computing the order
	 * matrix of each circle. It also determines any circles that should never
	 * be reversed (e.g. when it has no more than 1 inter-cluster edge).
	 */
	private void prepareCirclesForReversal()
	{
		CiSEGraphManager gm = (CiSEGraphManager)this.getGraphManager();
		Iterator<CiSENode> nodeIterator = gm.getRoot().getNodes().iterator();
		CiSENode node;
		CiSECircle circle;

		while (nodeIterator.hasNext())
		{
			node = nodeIterator.next();
			circle = (CiSECircle) node.getChild();

			if (circle != null)
			{
				if (circle.getInterClusterEdges().size() < 2)
				{
					circle.setMayNotBeReversed();
				}

				circle.computeOrderMatrix();
			}
		}
	}

	/*
	 * This method tries to improve the edge crossing number by reversing a
	 * cluster (i.e., the order of the nodes in the cluster such as C,B,A
	 * instead of A,B,C). No more than one reversal is performed with each
	 * execution. The decision is based on the global sequence alignment
	 * heuristic (typically used in biological sequence alignment). A cluster
	 * that was previsouly reversed is not a candidate for reversal to avoid
	 * oscillations. It returns true if a reversal has been performed.
	 */
	private boolean checkAndReverseIfReverseIsBetter()
	{
		CiSEGraphManager gm = (CiSEGraphManager)this.getGraphManager();

		// For each cluster (in no particular order) check to see whether
		// reversing the order of the nodes on the cluster could improve on
		// inter-graph edge crossing number of that cluster.

		Iterator<CiSENode> nodeIterator = gm.getRoot().getNodes().iterator();
		CiSENode node;
		CiSECircle circle;

		while (nodeIterator.hasNext())
		{
			node = nodeIterator.next();
			circle = (CiSECircle) node.getChild();

			if (circle != null &&
				circle.mayBeReversed() &&
				circle.getNodes().size() <= 52)
			{
				if (circle.checkAndReverseIfReverseIsBetter())
				{
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * This method goes over all circles and tries to find nodes that can be
	 * moved inside the circle. Inner nodes are found and moved inside one at a
	 * time. This process continues for a circle until either there is no inner
	 * node or reached max inner nodes for that circle.
	 */
	private void findAndMoveInnerNodes()
	{
		if (!this.allowNodesInsideCircle)
		{
			return;
		}

		for (Object ciseCircleObject : this.getGraphManager().getGraphs()) 
		{
			CiSECircle ciseCircle = (CiSECircle) ciseCircleObject;
		
			// Count inner nodes not to exceed user defined maximum
			int innerNodeCount = 0;
			
			if (ciseCircle != this.getGraphManager().getRoot())
			{
				// It is a user parameter, retrieve it.
				int maxInnerNodes = (int)(ciseCircle.getNodes().size() *
					this.maxRatioOfNodesInsideCircle);
		
				// Look for an inner node and move it inside
				CiSENode innerNode = this.findInnerNode(ciseCircle);

				while (innerNode != null && innerNodeCount < maxInnerNodes)
				{
					this.moveInnerNode(innerNode);
					innerNodeCount++;
		
					if (innerNodeCount < maxInnerNodes)
					{
						innerNode = this.findInnerNode(ciseCircle);
					}
				}
			}
		}
	}

	/**
	 * This method finds an inner node (if any) in the given circle.
	 */
	private CiSENode findInnerNode(CiSECircle ciseCircle)
	{
		CiSENode innerNode = null;
		int onCircleNodeCount = ciseCircle.getOnCircleNodes().size();
		
		// First sort the nodes in the circle according to their degrees.
		List<Object> sortedNodes = 
			new ArrayList<Object>(ciseCircle.getOnCircleNodes());
		new LNodeDegreeSort(sortedNodes).quicksort();

		// Evaluate each node as possible candidate
		for (int i = onCircleNodeCount - 1; i >= 0 && innerNode == null; i--)
		{
			CiSENode candidateNode = (CiSENode) sortedNodes.get(i);
			
			// Out nodes cannot be moved inside, so just skip them
			if (candidateNode.getOnCircleNodeExt().
				getInterClusterEdges().size() != 0)
			{
				continue;
			}
			
			List<CiSENode> circleSegment = 
				this.findMinimalSpanningSegment(candidateNode);
			
			// Skip nodes with no neighbors (circle segment will be empty)
			if (circleSegment.size() == 0)
			{
				continue;
			}
			
			// For all nodes in the spanning circle segment, check if that node
			// is connected to another node on the circle with an index diff of
			// greater than 1 (i.e. connected to a non-immediate neighbor)

			boolean connectedToNonImmediate = false;

			for (CiSENode spanningNode : circleSegment)
			{
				// Performance improvement: stop iteration if this cannot be
				// an inner node.
				if (connectedToNonImmediate)
				{
					break;
				}
				
				// Look for neighbors of this spanning node.
				for (Object neighborOfSpanningNodeObject :
					spanningNode.getNeighborsList()) 
				{
					CiSENode neighborOfSpanningNode =
						(CiSENode) neighborOfSpanningNodeObject;
					
					// In some case we don't need to look at the neighborhood
					// relationship. We won't care the neighbor of spanning node 
					// if:
					// - It is the candidate node
					// - It is on another circle
					// - It is already an inner node.
					if (neighborOfSpanningNode != candidateNode && 
						neighborOfSpanningNode.getOwner() == ciseCircle &&
						neighborOfSpanningNode.getOnCircleNodeExt() != null)
					{
						int spanningIndex =
							spanningNode.getOnCircleNodeExt().getIndex(); 
						int neighborOfSpanningIndex = neighborOfSpanningNode.
							getOnCircleNodeExt().getIndex(); 
						
						// Calculate the index difference between spanning node
						// and its neighbor
						int indexDiff = spanningIndex - neighborOfSpanningIndex;
						indexDiff += onCircleNodeCount; // Get rid of neg. index
						indexDiff %= onCircleNodeCount; // Mod it
						
						// Give one more chance, try reverse order of nodes 
						// just in case.
						if (indexDiff > 1)
						{
							indexDiff = neighborOfSpanningIndex - spanningIndex;
							indexDiff += onCircleNodeCount; // Get rid of neg.
							indexDiff %= onCircleNodeCount; // Mod it
						}

						// If the diff is still greater 1, this spanning node
						// has a non-immediate neighbor. Sorry but you cannot
						// be an inner node. Poor candidate node !!!
						if (indexDiff > 1)
						{
							connectedToNonImmediate = true;
							// stop computation.
							break;
						}
					}
				}
			}
			
			// If neighbors of candidate node is not connect to a non-immediate
			// neighbor that this can be an inner node.
			if (!connectedToNonImmediate)
			{
				innerNode = candidateNode;
			}
		}
		
		return innerNode;
	}
	
	/**
	 * This method safely removes inner node from circle perimeter (on-circle)
	 * and moves them inside their owner circles (as in-circle nodes)
	 */
	private void moveInnerNode(CiSENode innerNode)
	{
		CiSECircle ciseCircle = (CiSECircle)innerNode.getOwner();
		
		// Remove the node from the circle first. This forces circle to
		// re-adjust its geometry. A costly operation indeed...
		ciseCircle.moveOnCircleNodeInside(innerNode);
		
		// We need to also remove the inner node from on-circle nodes list
		// of the associated graph manager
		List<CiSENode> onCircleNodesList = new ArrayList<CiSENode>(
			Arrays.asList(this.getCiSEGraphManager().getOnCircleNodes()));
		onCircleNodesList.remove(innerNode);
		
		this.getCiSEGraphManager().setOnCircleNodes(
			onCircleNodesList.toArray(new CiSENode[0]));

		// Add the inner node to in-circle node list of the graph manager
		List<CiSENode> inCircleNodesList = new ArrayList<CiSENode>(
			Arrays.asList(this.getCiSEGraphManager().getInCircleNodes()));
		inCircleNodesList.add(innerNode);
		
		this.getCiSEGraphManager().setInCircleNodes(
			inCircleNodesList.toArray(new CiSENode[0]));
	}

	/**
	 * This method returns a circular segment (ordered array of nodes),
	 * which is the smallest segment that spans neighbors of the given node.
	 */
	private List<CiSENode> findMinimalSpanningSegment(CiSENode node)
	{
		List<CiSENode> segment = new ArrayList<CiSENode>();

		// First create an ordered neighbors list which includes given node and
		// its neighbors and ordered according to their indexes in this circle.
		List<Object> orderedNeigbors = 
			new ArrayList<Object>(node.getOnCircleNeighbors());
		
		if (orderedNeigbors.size() == 0)
		{
			return segment;
		}
		
		new CiSENodeSort(orderedNeigbors).quicksort();
		
		// According to the order found, find the start and end nodes of the
		// segment by testing each (order adjacent) neighbor pair. 

		List<CiSENode> orderedNodes = ((CiSECircle)node.getOwner()).getOnCircleNodes();
		CiSENode shortestSegmentStartNode = null;
		CiSENode shortestSegmentEndNode = null;
		int shortestSegmentLength = orderedNodes.size();
		int segmentLength = orderedNodes.size();
		int neighSize = orderedNeigbors.size();
		int i, j;
		CiSENode tempSegmentStartNode;
		CiSENode tempSegmentEndNode;
		int tempSegmentLength;

		for (i = 0; i < neighSize; i++)
		{
			j = ((i - 1) + neighSize) % neighSize;
			
			tempSegmentStartNode = (CiSENode) orderedNeigbors.get(i);
			tempSegmentEndNode = (CiSENode) orderedNeigbors.get(j);
			
			tempSegmentLength =
				(tempSegmentEndNode.getOnCircleNodeExt().getIndex() -
					tempSegmentStartNode.getOnCircleNodeExt().getIndex() +
					segmentLength) % segmentLength + 1;
			
			if (tempSegmentLength < shortestSegmentLength)
			{
				shortestSegmentStartNode = tempSegmentStartNode;
				shortestSegmentEndNode = tempSegmentEndNode;
				shortestSegmentLength = tempSegmentLength;
			}
		}
		
		// After finding start and end nodes for the segment, simply go over 
		// ordered nodes and create an ordered list of nodes in the segment

		boolean segmentEndReached = false;
		CiSENode currentNode = shortestSegmentStartNode;

		while (!segmentEndReached)
		{
			if (currentNode != node)
			{
				segment.add(currentNode);
			}
			
			if (currentNode == shortestSegmentEndNode)
			{
				segmentEndReached = true;
			}
			else
			{
				int nextIndex = currentNode.getOnCircleNodeExt().getIndex() + 1;

				if (nextIndex == orderedNodes.size())
				{
					nextIndex = 0;
				}

				currentNode = orderedNodes.get(nextIndex);
			}
		}

		return segment;
	}

// -----------------------------------------------------------------------------
// Section: Class constants
// -----------------------------------------------------------------------------
	/**
	 * Steps of layout
	 */
	public static final int STEP_NOT_STARTED = 0;
	public static final int STEP_1 = 1;
	public static final int STEP_2 = 2;
	public static final int STEP_3 = 3;
	public static final int STEP_4 = 4;
	public static final int STEP_5 = 5;

	int iterations = 0;

	/**
	 * Phases of a step
	 */
	public static final int PHASE_NOT_STARTED = 0;
	public static final int PHASE_SWAP_PREPERATION = 1;
	public static final int PHASE_PERFORM_SWAP = 2;
	public static final int PHASE_OTHER = 3;
}