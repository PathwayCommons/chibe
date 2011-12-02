package org.gvt.layout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.eclipse.draw2d.geometry.*;
import org.gvt.model.CompoundModel;
import org.gvt.model.EdgeBendpoint;
import org.gvt.model.NodeModel;
import org.gvt.util.ChsGeometry;
import org.gvt.util.ChsTransform;

/**
 * This class implements the overall layout process for the CoSE algorithm
 * (Compound Spring Embedder by Dogrusoz et al).
 *
 * @author Ugur Dogrusoz
 * @author Erhan Giral
 * @author Cihan Kucukkececi
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class CoSELayout extends AbstractLayout
{
// -----------------------------------------------------------------------------
// Section: Instance Variables
// -----------------------------------------------------------------------------
	/**
	 * Ideal length of an edge
	 */
	protected double idealEdgeLength = DEFAULT_EDGE_LENGTH;

	/**
	 * Coefficient of ideal edge length, varies from phase to phase
	 */
	private double idealEdgeLengthCoefficient;

	/**
	 * Constant for calculating spring forces
	 */
	protected double springConstant = DEFAULT_SPRING_STRENGTH;

	/**
	 * Maximum distance at which a node may repulse another node
	 */
	protected double repulsionRange;

	/**
	 * Constant for calculating repulsion forces
	 */
	protected double repulsionConstant = DEFAULT_REPULSION_STRENGTH;

	/**
	 * Accuracy of layout iterations; higher accuracy means smaller time steps.
	 */
	protected double accuracy;

	/**
	 *
	 * Constant for calculating gravitation forces
	 */
	protected double gravityConstant = DEFAULT_GRAVITY_STRENGTH;

	/**
	 * Gravitational constant for compound nodes
	 */
	protected double compoundGravityConstant =
		DEFAULT_COMPOUND_GRAVITY_STRENGTH;

	/**
	 * Maximum number of layout iterations allowed
	 */
	protected int maxIteration = 4000;

	/**
	 * Total number of iterations currently performed
	 */
	protected int globalIteration;

	/**
	 * Number of layout iterations that has not been animated (rendered)
	 */
	protected int notAnimatedIterations;

	/**
	 * Maximum iterations that each phase can perform
	 */
	public int maxIterationsPhase1;
	public int maxIterationsPhase2;
	public int maxIterationsPhase3;

	/**
	 * Threshold for convergence (calculated according to graph to be laid out)
	 */
	public double totalDisplacementThreshold = 0;

	/**
	 * Threshold for convergence per node
	 */
	public double displacementThresholdPerNode = 0.5;

	/**
	 * Current phase of the layout process
	 */
	private int phase;

	/**
	 * Tree reduction related variables; subgraphs that are trees are reduced
	 * for efficiency purposes before layout starts and introduced back
	 * gradually later on.
	 */
	private Vector reducedTreeRoots;
	private Vector reducedNodes;
	private Vector deReducedNodes;
	private int noOfReducedNodes;

// -----------------------------------------------------------------------------
// Section: Constructors and Initializations
// -----------------------------------------------------------------------------
	/**
	 * Passes parameters to super
	 */
	public CoSELayout(CompoundModel rootModel)
	{
		super(rootModel);
	}

	/**
	 * Passes parameters to super
	 */
	public CoSELayout(List nodes, List edges)
	{
		super(nodes,edges);
	}

	public LNode createNewLNode(LGraphManager gm, NodeModel model)
	{
		return new CoSENode(gm);
	}

	public LNode createNewLNode(LGraphManager gm,
		NodeModel model,
		Point loc,
		Dimension size)
	{
		return new CoSENode(gm, loc, size);
	}

	public LEdge createNewLEdge(LNode source, LNode target)
	{
		return new CoSEEdge((CoSENode)source, (CoSENode)target);
	}

	public LGraph createNewLGraph(LNode rootNode, LGraphManager lGraphManager)
	{
		return new CoSEGraph((CoSENode)rootNode, lGraphManager);
	}

	public void initialize()
	{
		super.initialize();
		this.reducedTreeRoots = new Vector();
		this.reducedNodes = new Vector();
		this.deReducedNodes = new Vector();
	}

	/**
	 * This method is used to set all layout parameters to default values.
	 */
	public void initParameters()
	{
		super.initParameters();

		if (this.fromChisioModel)
		{
			LayoutOptionsPack.CoSE layoutOptionsPack =
				LayoutOptionsPack.getInstance().getCoSE();

			if (layoutOptionsPack.getIdealEdgeLength() < 10)
			{
				this.idealEdgeLength = DEFAULT_EDGE_LENGTH;
			}
			else
			{
				this.idealEdgeLength = layoutOptionsPack.getIdealEdgeLength();
			}

			this.springConstant =
				transform(layoutOptionsPack.getSpringStrength(),
					DEFAULT_SPRING_STRENGTH);
			this.repulsionConstant =
				transform(layoutOptionsPack.getRepulsionStrength(),
					DEFAULT_REPULSION_STRENGTH);
			this.gravityConstant =
				transform(layoutOptionsPack.getGravityStrength(),
					DEFAULT_GRAVITY_STRENGTH);
			this.compoundGravityConstant =
				transform(layoutOptionsPack.getCompoundGravityStrength(),
					DEFAULT_COMPOUND_GRAVITY_STRENGTH);
		}

		this.accuracy = 20;
		this.repulsionRange = this.idealEdgeLength * 5;

		if (this.layoutQuality == DEFAULT_QUALITY)
		{
			this.repulsionRange = this.idealEdgeLength * 5;
		}
		else if (this.layoutQuality == DRAFT_QUALITY)
		{
			this.repulsionRange = this.idealEdgeLength * 4;
			this.displacementThresholdPerNode += 0.30;
			this.maxIteration -= 2000;
		}
		else
		{
			this.repulsionRange = this.idealEdgeLength * 6;
			this.displacementThresholdPerNode -= 0.30;
			this.maxIteration += 2000;
		}

		this.globalIteration = 0;
		this.notAnimatedIterations = 0;
		this.maxIterationsPhase1 = this.maxIteration / 3;
		this.maxIterationsPhase2 =
			this.maxIterationsPhase3 = this.maxIteration;

		if (this.animationDuringLayout)
		{
			animationOnLayout = false;
		}

		CoSEGraph.setGraphMargin(CompoundModel.MARGIN_SIZE);
	}

	/**
	 * This method estimates the initial node sizes during non-incremental
	 * layout. A compound node can contain arbitrary number of nodes with an
	 * arbitrary nesting hierarchy. Therefore size of a compound node is of
	 * great importance as it defines the space in which its child graph should
	 * be laid out in.
	 */
	private void estimateInitialNodeSizes()
	{
		assert !this.incremental;
		this.lGraphManager.getRoot().getParent().getEstimatedSize();
	}

// -----------------------------------------------------------------------------
// Section: Layout!
// -----------------------------------------------------------------------------
	/**
	 * This method performs layout on constructed l-level compound graph. If
	 * "interactive" flag is false, then layout is assumed to be called for
	 * test purposes.
	 */
	public void layout()
	{
		boolean createBendsAsNeeded = LayoutOptionsPack.getInstance().
			getGeneral().isCreateBendsAsNeeded();

		if (createBendsAsNeeded)
		{
			this.createBendpoints();
		}

		if (!this.incremental)
		{
			ArrayList<ArrayList<LNode>> forest = this.getFlatForest();

			if (forest.size() > 0)
			// The graph associated with this layout is flat and a forest
			{
				this.positionNodesRadially(forest);
			}
			else
			// The graph associated with this layout is not flat or a forest
			{
				this.reduceTrees();
				this.estimateInitialNodeSizes();
				this.positionNodesRandomly();
			}
		}

		this.moveTowardsScreenCenter();
		this.doLayoutPhases();

		System.out.println("CoSE layout finished after " +
			this.globalIteration + " iterations");
	}

	/**
	 * This method performs the actual layout on the l-level compound graph. An
	 * update() needs to be called for changes to be propogated to the v-level
	 * compound graph.
	 */
	public void doLayoutPhases()
	{
		if (!this.incremental)
		{
//			CoSELayout.randomizedMovementCount = 0;
//			CoSELayout.nonRandomizedMovementCount = 0;

			this.doPhase1();
			this.doPhase2();
			this.doPhase3();
		}
		else
		{
			this.doPhase3();
		}
	}

	/**
	 * This is the first phase of our layout algorithm in which the skeleton of
	 * the graph is laid out. This phase consists of two minor phases. These
	 * minor phases share the total number of iterations reserved for this phase
	 * equally. First phase applies only the spring forces on nodes, the second
	 * phase applies both spring and repulsion forces.
	 */
	private void doPhase1()
	{
		this.phase = PHASE_1;
		coolingFactor = 1.0;
//		this.updateAnnealingProbability();
		totalDisplacement = 0;
		this.globalIteration = 0;
		int iteration = 0;

		// Minor Phase 1, iterate over all nodes as long as max iters not hit
		this.idealEdgeLengthCoefficient =
			IDEAL_EDGE_LENGTH_COEFFICIENT_PHASE1_MINOR1;

		while (iteration < this.maxIterationsPhase1 / 2)
		{
			totalDisplacement = 0;

			this.applySpringForces();

			this.moveAllNodes();

			iteration++;
			this.globalIteration++;

			if (iteration % CONVERGENCE_CHECK_PERIOD == 0)
			{
				if (this.isConverged())
				{
					break;
				}
			}

			this.animate();
		}

		// Minor Phase 2
		iteration = 0;
		totalDisplacement = 0;
		this.idealEdgeLengthCoefficient =
			IDEAL_EDGE_LENGTH_COEFFICIENT_PHASE1_MINOR2;

		while (iteration < this.maxIterationsPhase1 / 2)
		{
			totalDisplacement = 0;

			this.applySpringForces();
			this.applyRepulsionForces();
			this.applyGravitationalForces();

			this.moveAllNodes();

			iteration++;
			this.globalIteration++;

			if (iteration % CONVERGENCE_CHECK_PERIOD == 0)
			{
				if (this.isConverged())
				{
					break;
				}

//				// cool down is applied during this last phase only!
//				coolingFactor = (this.maxIterationsPhase1/2 - iteration) /
//					(double)(this.maxIterationsPhase1/2);
//
//				this.updateAnnealingProbability();
			}

			this.animate();
		}
	}

	/**
	 * This is the phase where we grow trees back into our compound graph
	 * gradually.
	 */
	private void doPhase2()
	{
		this.phase = PHASE_2;
		coolingFactor = 1.0;
//		this.updateAnnealingProbability();
		this.idealEdgeLengthCoefficient =
			IDEAL_EDGE_LENGTH_COEFFICIENT_PHASE2;
		this.repulsionRange = this.repulsionRange * 0.85;

		this.growTrees();
	}

	/**
	 * This is the phase for polishing layout.
	 */
	private void doPhase3()
	{
		this.phase = PHASE_3;

		coolingFactor = 1.0;
//		this.updateAnnealingProbability();

		this.idealEdgeLengthCoefficient =
			IDEAL_EDGE_LENGTH_COEFFICIENT_PHASE3;
		this.repulsionRange = this.repulsionRange * 0.85;
		int iteration = 0;
		totalDisplacement = 1000;

		while (iteration < this.maxIterationsPhase3)
		{
			iteration++;
			this.globalIteration++;

			if (iteration % CONVERGENCE_CHECK_PERIOD == 0)
			{
				if (this.isConverged())
				{
					break;
				}

				// cool down is applied during this last phase only!
				coolingFactor = (this.maxIterationsPhase3 -
					iteration) / (double)this.maxIterationsPhase3;

//				this.updateAnnealingProbability();
			}

			totalDisplacement = 0;

			this.applySpringForces();
			this.applyRepulsionForces();
			this.applyGravitationalForces();
			this.moveAllNodes();

			this.animate();
		}
	}

	/**
	 * The tree reduction operation identifies the *skeleton* of the target view
	 * graph. The word skeleton means the strongly constrained nodes that are on
	 * a cycle and/or end vertex of an inter-compartmental edge plus nodes that
	 * are connecting such connected subgraphs. The skeleton of the graph will
	 * be the exclusive target of the remaining operations in initialization and
	 * first phase of layout.
	 */
	private void reduceTrees()
	{
		this.reduceTrees(this.lGraphManager.getNodeList());

		LinkedList linkedList = new LinkedList();
		Iterator iterator = this.reducedNodes.iterator();
		CoSENode lNode;
		Object vNode;

		while (iterator.hasNext())
		{
			lNode = (CoSENode) iterator.next();
			vNode = this.viewMapLtoV.get(lNode);

			if (!linkedList.contains(vNode))
			{
				linkedList.add(vNode);
			}
		}

		this.animate();
	}

	/**
	 * This method reduces the trees that are subgraphs of the graph defined by
	 * the given list of nodes as described earlier.
	 */
	private void reduceTrees(List nodeList)
	{
		this.noOfReducedNodes = 0;
		Iterator itr = nodeList.iterator();

		while (itr.hasNext())
		{
			CoSENode currentNode = (CoSENode) itr.next();

			if (currentNode.isReduced())
			{
				continue;
			}
			else if (currentNode.edges.size() == 0)
			// isolated node
			{
				// non-compound leaf node
				if (currentNode.getChild() == null &&
					currentNode.getOwner() == this.lGraphManager.getRoot())
				{
					this.reducedNodes.add(currentNode);
					currentNode.setReduced(true);
					this.noOfReducedNodes++;
					this.reducedTreeRoots.add(currentNode);
					currentNode.reducedTreeRoot = true;
				}
			}
			else if (currentNode.getNumberOFUnmarkedNeigbors() == 1)
			{
				do
				{
					if (currentNode.isReduced())
					{
						break;
					}

					// compound node or a part of a compound node
					if (currentNode.getChild() != null ||
						currentNode.getOwner() != this.lGraphManager.getRoot())
					{
						break;
					}

					this.reducedNodes.add(currentNode);
					currentNode.setReduced(true);
					this.noOfReducedNodes++;

					if (!this.reducedTreeRoots.contains(currentNode))
					{
						this.reducedTreeRoots.add(currentNode);
						currentNode.reducedTreeRoot = true;
					}

					Iterator edgeItr;
					CoSEEdge lEdge;
					CoSENode previousNode;
					edgeItr = currentNode.edges.iterator();

					while (edgeItr.hasNext())
					{
						lEdge = (CoSEEdge) edgeItr.next();

						if (!((CoSENode)lEdge.getOtherEnd(currentNode)).isReduced())
						{
							previousNode = currentNode;
							currentNode = (CoSENode) lEdge.getOtherEnd(currentNode);

							// remove all occurences
							while (this.reducedTreeRoots.contains(previousNode))
							{
								this.reducedTreeRoots.remove(previousNode);
								previousNode.reducedTreeRoot = false;
							}

							this.reducedTreeRoots.add(currentNode);
							currentNode.reducedTreeRoot = true;

							lEdge.reduced = true;
							break;
						}
					}

					if (currentNode.getNumberOFUnmarkedNeigbors() > 1)
					{
						break;
					}
				}
				while (true);
			}
			else // node cannot start a reduction
			{
				continue;
			}
		}
	}

	/**
	 * This method gradually (level by level) grows the reduced trees back into
	 * the compound graph while performing layout.
	 */
	private void growTrees()
	{
		int iteration = 0;
		Vector tmp = new Vector();
		tmp.addAll(this.reducedTreeRoots);
		Vector evenLevels = tmp;
		Vector oddLevels = new Vector();

		while (!evenLevels.isEmpty())
		{
			while (!evenLevels.isEmpty())
			{
				CoSENode node = (CoSENode) evenLevels.remove(0);
				Iterator iter = node.edges.iterator();
				node.setReduced(false);
				this.noOfReducedNodes--;

				if (!this.deReducedNodes.contains(node))
				{
					this.deReducedNodes.add(node);
				}

				while (iter.hasNext())
				{
					CoSEEdge edge = (CoSEEdge) iter.next();

					if (edge.reduced)
					{
						edge.reduced = false;
						edge.growedFrom = node;
						CoSENode newNode = (CoSENode) edge.getOtherEnd(node);
						newNode.setReduced(false);
						this.noOfReducedNodes--;

						// Unhide

						LinkedList linkedList = new LinkedList();
						linkedList.add(this.viewMapLtoV.get(newNode));

						newNode.migrateTo(node);
						oddLevels.add(newNode);
					}
				}
			}

			while (iteration < TREE_GROWING_FREQUENCY)
			{
				this.applySpringForces();
				this.applyRepulsionForces();
				this.applyGravitationalForces();
				this.moveAllNodes();

				iteration++;
				this.animate();
			}

			this.globalIteration += iteration;
			iteration = 0;

			this.animate();

			while (!oddLevels.isEmpty())
			{
				CoSENode node = (CoSENode) oddLevels.remove(0);
				Iterator iter = node.edges.iterator();
				node.setReduced(false);
				this.noOfReducedNodes--;

				if (!this.deReducedNodes.contains(node))
				{
					this.deReducedNodes.add(node);
				}

				while (iter.hasNext())
				{
					CoSEEdge edge = (CoSEEdge) iter.next();
					if (edge.reduced)
					{
						edge.reduced = false;
						edge.growedFrom = node;
						CoSENode newNode = (CoSENode) edge.getOtherEnd(node);
						newNode.setReduced(false);
						this.noOfReducedNodes--;

						// Unhide

						LinkedList linkedList = new LinkedList();
						linkedList.add(this.viewMapLtoV.get(newNode));

						newNode.migrateTo(node);
						evenLevels.add(newNode);
					}
				}
			}

			while (iteration < TREE_GROWING_FREQUENCY)
			{
				this.applySpringForces();
				this.applyRepulsionForces();
				this.applyGravitationalForces();
				this.moveAllNodes();

				iteration++;

				this.animate();
			}

			this.globalIteration += iteration;

			this.animate();
		}
	}

	/**
	 * This method applies the forces caused by the edges of the nodes.
	 */
	private void applySpringForces()
	{
		// Calculated spring force and its components
		double springForce;
		double springForceX, springForceY;

		// Will be modified for inter-graph edges properly
		double idealLength;

		// Go over all edges and calculate spring forces for end nodes
		Object[] lEdges = this.getEdges();

		for (int i = 0; i < lEdges.length; i++)
		{
			CoSEEdge edge = (CoSEEdge) lEdges[i];

			// Ignore reduced edges
			if (edge.reduced)
			{
				continue;
			}

			CoSENode targetNode = (CoSENode) edge.target;
			CoSENode sourceNode = (CoSENode) edge.source;

			// Apply the ideal edge length coefficient
			idealLength = this.idealEdgeLength *
				this.idealEdgeLengthCoefficient * edge.getIdealLengthModifier();

			// Recalculate and initialize forces and length
			edge.recalculate();

			// Accuracy is used for time step accuracy. A higher accuracy
			// value means smaller incremental steps throughout the algorithm.
			springForce = this.springConstant *
				Math.abs(idealLength - edge.length) / this.accuracy;
//			springForce = this.springConstant *
// 				Math.pow(idealLength - edge.length, 2) / (150 * this.accuracy);

			if (springForce > MAX_SPRING_FORCE)
			{
				springForce = MAX_SPRING_FORCE;
			}

			if (springForce < MIN_SPRING_FORCE)
			{
				springForce = MIN_SPRING_FORCE;
			}

			// Correct direction of force if not correct (edge is streched).
			if (idealLength < edge.length)
			{
				springForce = -springForce;
			}

			if (edge.overlapingTargetAndSource)
			{
				springForce = MAX_SPRING_FORCE;
				edge.overlapingTargetAndSource = false;
			}

			// Project force onto x and y axes, then apply forces on the nodes
			springForceX = springForce * (edge.lengthx / edge.length);
			springForceY = springForce * (edge.lengthy / edge.length);

			// Increment appropriate force components
			targetNode.springForceX += springForceX;
			targetNode.springForceY += springForceY;
			sourceNode.springForceX -= springForceX;
			sourceNode.springForceY -= springForceY;

			if (edge.isInterGraph)
			{
				if (targetNode.getOwner() != this.lGraphManager.getRoot())
				{
					((CoSENode)targetNode.getOwner().getParent()).
						propogateIGEFtoParents(springForceX, springForceY);
				}

				if (sourceNode.getOwner() != this.lGraphManager.getRoot())
				{
					((CoSENode)sourceNode.getOwner().getParent()).
						propogateIGEFtoParents(-springForceX, -springForceY);
				}
			}
		}
	}

	/**
	 * This method applies gravitational forces on each node.
	 */
	private void applyGravitationalForces()
	{
		CoSENode node;
		double ownerCenterX;
		double ownerCenterY;
		double distanceX;
		double distanceY;
		Object[] lNodes = this.getNodes();

		for (int i = 0; i < lNodes.length; i++)
		{
			node = (CoSENode) lNodes[i];

			assert node.gravitationForceX == 0 && node.gravitationForceY == 0;

			if (node.isReduced())
			{
				continue;
			}

			if (node.getOwner() == this.lGraphManager.getRoot())
			// in the root graph
			{
				distanceX = GRAVITY_CENTER_X - node.getCenterX();
				distanceY = GRAVITY_CENTER_Y - node.getCenterY();

				node.gravitationForceX = this.gravityConstant * distanceX;
				node.gravitationForceY = this.gravityConstant * distanceY;
			}
			else
			// inside a compound
			{
				ownerCenterX =
					(node.getOwner().getRight() + node.getOwner().getLeft()) / 2;
				ownerCenterY =
					(node.getOwner().getTop() + node.getOwner().getBottom()) / 2;
				distanceX = ownerCenterX - node.getCenterX();
				distanceY = ownerCenterY - node.getCenterY();

				node.gravitationForceX = this.compoundGravityConstant *
					this.gravityConstant * distanceX;
				node.gravitationForceY = this.compoundGravityConstant *
					this.gravityConstant * distanceY;
			}
		}
	}

	/**
	 * This method applies node-to-node repulsion forces on the nodes.
	 */
	private void applyRepulsionForces()
	{
		// Calculate repulsion forces for each node pair
		int i, j;
		CoSENode nodeA, nodeB;
		boolean overlap;
		double distanceX;
		double distanceY;
		double distanceSquared;
		double distance;
		double repulsionForce;
		double repulsionForceX;
		double repulsionForceY;
		double clipPointAx;
		double clipPointAy;
		double clipPointBx;
		double clipPointBy;
		Object[] lNodes = this.getNodes();
		double[] clipPointCoordinates;

		for (i = 0; i < lNodes.length; i++)
		{
			nodeA = (CoSENode) lNodes[i];

			if (nodeA.isReduced())
			{
				continue;
			}

			for (j = i + 1; j < lNodes.length; j++)
			{
				nodeB = (CoSENode) lNodes[j];

				if (nodeB.isReduced())
				{
					continue;
				}

				// If both nodes are not members of the same graph, skip.
				if (nodeA.getOwner() != nodeB.getOwner())
				{
					continue;
				}
				
                overlap = false;
                
                if(nodeA.getRect().intersects(nodeB.getRect()))
				{
					overlap = true;
				}
				
                if(!overlap)
                {
                	clipPointCoordinates = ChsGeometry.getIntersection(nodeA.getRect(), nodeB.getRect());
				
                	clipPointAx = (int) clipPointCoordinates[0];
                	clipPointAy = (int) clipPointCoordinates[1];
                	clipPointBx = (int) clipPointCoordinates[2];
                	clipPointBy = (int) clipPointCoordinates[3];
                }
                else
                {
                	clipPointAx = (int) nodeA.getRect().getCenterX();
                	clipPointAy = (int) nodeA.getRect().getCenterY();
                	clipPointBx = (int) nodeB.getRect().getCenterX();
                	clipPointBy = (int) nodeB.getRect().getCenterY();
                }

				distanceX = clipPointBx - clipPointAx;
				distanceY = clipPointBy - clipPointAy;

				// Avoid division by 0

				if (distanceX == 0)
				{
					distanceX = Double.MIN_VALUE;
				}

				if (distanceY == 0)
				{
					distanceY = Double.MIN_VALUE;
				}

				// Repulsion forces depend on the square of the distance
				distanceSquared = distanceX * distanceX + distanceY * distanceY;

				repulsionForceX = 0.0;
				repulsionForceY = 0.0;

				// If we are too close, then we dont really want to calculate
				// the forces, since they are going to be huge!

				if (distanceSquared <= 1.0)
				{
					repulsionForceX = MAX_REPULSION_FORCE * sign(distanceX);
					repulsionForceY = MAX_REPULSION_FORCE * sign(distanceY);
				}
				else if (overlap)
				{
					distance = Math.sqrt(distanceSquared);
					repulsionForce = MAX_REPULSION_FORCE;
					repulsionForceX = repulsionForce * distanceX / distance;
					repulsionForceY = repulsionForce * distanceY / distance;
				}
				else if ((Math.abs(distanceX) <
						this.repulsionRange + (nodeA.rect.width + nodeB.rect.width) / 2 &&
					Math.abs(distanceY) <
						this.repulsionRange + (nodeA.rect.height + nodeB.rect.height) / 2))
				{
					distance = Math.sqrt(distanceSquared);

					if (this.phase == PHASE_3)
					{
						repulsionForce = 80 * this.repulsionConstant /
							(this.accuracy * distanceSquared);
					}
					else
					{
						repulsionForce = this.repulsionConstant /
							(this.accuracy * distance);
					}

					// Still too big of a force?
					if (repulsionForce > MAX_REPULSION_FORCE)
					{
						repulsionForce = MAX_REPULSION_FORCE;
					}

					repulsionForceX = repulsionForce * distanceX / distance;
					repulsionForceY = repulsionForce * distanceY / distance;
				}

				nodeA.repulsionForceX -= repulsionForceX;
				nodeA.repulsionForceY -= repulsionForceY;
				nodeB.repulsionForceX += repulsionForceX;
				nodeB.repulsionForceY += repulsionForceY;
			}
		}
	}

	/**
	 * This method updates positions of each node at the end of an iteration.
	 */
	private void moveAllNodes()
	{
		Object[] lNodes = this.getNodes();
		CoSENode node;

		for (int i = 0; i < lNodes.length; i++)
		{
			node = (CoSENode) lNodes[i];
			node.move();
		}
	}

	/**
	 * This method inspects whether the graph has reached to a minima, based on
	 * the phase that the layout manager is currently in. It returns true if
	 * the layout seems to be oscillating as well.
	 */
	private boolean isConverged()
	{
		boolean converged;

		if (this.phase == PHASE_1)
		{
			this.totalDisplacementThreshold =
				this.displacementThresholdPerNode *
					(this.getNodes().length - this.noOfReducedNodes);
		}
		else if (this.phase == PHASE_2)
		{
			// need to allow all reduced nodes to be grown!
			this.totalDisplacementThreshold = 0.0;
		}
		else if (this.phase == PHASE_3)
		{
			this.totalDisplacementThreshold =
				this.displacementThresholdPerNode * this.getNodes().length;
		}

		boolean oscilatting = false;

		if (this.globalIteration > this.maxIteration / 3)
		{
			oscilatting = Math.abs(totalDisplacement -
				oldTotalDisplacement) < 2;
		}

		converged = totalDisplacement <
			this.totalDisplacementThreshold;

		oldTotalDisplacement = totalDisplacement;

		return converged || oscilatting;
	}

	/**
	 * This method updates the v-level compound graph coordinates and refresh
	 * the display if corresponding flag is on.
	 */
	private void animate()
	{
		if (this.animationDuringLayout && this.fromChisioModel)
		{
			if (this.notAnimatedIterations == this.animationPeriod)
			{
				this.update();

				this.notAnimatedIterations = 0;
			}
			else
			{
				this.notAnimatedIterations++;
			}
		}
	}

	private void createBendpoints()
	{
		List<LEdge> edges = new ArrayList(this.lGraphManager.getEdgeList());
		Set visited = new HashSet();

		for (int i = 0; i < edges.size(); i++)
		{
			LEdge edge = edges.get(i);

			if (!visited.contains(edge))
			{
				LNode source = edge.source;
				LNode target = edge.target;

				if (source == target)
				{
					edge.bendpoints.add(new EdgeBendpoint());
					edge.bendpoints.add(new EdgeBendpoint());
					this.createDummyNodesForBendpoints(edge);
					visited.add(edge);
				}
				else
				{
					List edgeList = source.getEdgeListToNode(target);
					edgeList.addAll(target.getEdgeListToNode(source));

					if (!visited.contains(edgeList.get(0)))
					{
						if (edgeList.size() > 1)
						{
							for(int k = 0; k < edgeList.size(); k++)
							{
								LEdge multiEdge = (LEdge)edgeList.get(k);
								multiEdge.bendpoints.add(new EdgeBendpoint());
								this.createDummyNodesForBendpoints(multiEdge);
							}
						}

						visited.addAll(edgeList);
					}
				}
			}

			if (visited.size() == edges.size())
			{
				break;
			}
		}
	}

	/**
	 * This method performs initial positioning of given forest radially. The
	 * final drawing should be centered at the gravitational center.
	 */
	protected void positionNodesRadially(ArrayList<ArrayList<LNode>> forest)
	{
		// We tile the trees to a grid row by row; first tree starts at (0,0)
		Point currentStartingPoint = new Point(0, 0);
		int numberOfColumns = (int) Math.ceil(Math.sqrt(forest.size()));
		int height = 0;
		int currentY = 0;
		int currentX = 0;
		Point point = new Point(0, 0);

		for (int i = 0; i < forest.size(); i++)
		{
			if (i % numberOfColumns == 0)
			{
				// Start of a new row, make the x coordinate 0, increment the
				// y coordinate with the max height of the previous row
				currentX = 0;
				currentY = height;

				if (i !=0)
				{
					currentY += DEFAULT_COMPONENT_SEPERATION;
				}

				height = 0;
			}

			ArrayList<LNode> tree = forest.get(i);

			// Find the center of the tree
			LNode centerNode = AbstractLayout.findCenterOfTree(tree);

			// Set the staring point of the next tree
			currentStartingPoint.x = currentX;
			currentStartingPoint.y = currentY;

			// Do a radial layout starting with the center
			point =
				CoSELayout.radialLayout(tree, centerNode, currentStartingPoint);

			if (point.y > height)
			{
				height = point.y;
			}

			currentX = point.x + DEFAULT_COMPONENT_SEPERATION;
		}

		this.transform(
			new PrecisionPoint(GRAVITY_CENTER_X - point.x / 2,
				GRAVITY_CENTER_Y - point.y / 2));
	}

	/**
	 * This method positions given nodes according to a simple radial layout
	 * starting from the center node. The top-left of the final drawing is to be
	 * at given location. It returns the bottom-right of the bounding rectangle
	 * of the resulting tree drawing.
	 */
	private static Point radialLayout(ArrayList<LNode> tree,
		LNode centerNode,
		Point startingPoint)
	{
		double radialSep = Math.max(maxDiagonalInTree(tree),
			CoSELayout.DEFAULT_RADIAL_SEPARATION);
		CoSELayout.branchRadialLayout(centerNode, null, 0, 359, 0, radialSep);
		Rectangle bounds = LGraph.calculateBounds(tree);

		ChsTransform chsTransform = new ChsTransform();
		chsTransform.setDeviceOrgX(bounds.getLeft().x);
		chsTransform.setDeviceOrgY(bounds.getTop().y);
		chsTransform.setWorldOrgX(startingPoint.x);
		chsTransform.setWorldOrgY(startingPoint.y);

		for (int i = 0; i < tree.size(); i++)
		{
			LNode node = tree.get(i);
			node.transform(chsTransform);
		}

		PrecisionPoint bottomRight =
			new PrecisionPoint(bounds.getBottomRight());

		return chsTransform.inverseTransformPoint(bottomRight);
	}

	/**
	 * This method is recursively called for radial positioning of a node,
	 * between the specified angles. Curent radial level is implied by the
	 * distance given. Parent of this node in the tree is also needed.
	 */
	private static void branchRadialLayout(LNode node,
		LNode parentOfNode,
		double startAngle, double endAngle,
		double distance, double radialSeparation)
	{
		// First, position this node by finding its angle.
		double halfInterval = ((endAngle - startAngle) + 1) / 2;

		if (halfInterval < 0)
		{
			halfInterval += 180;
		}

		double nodeAngle = (halfInterval + startAngle) % 360;
		double teta = (nodeAngle * ChsGeometry.TWO_PI) / 360;

		// Make polar to java cordinate conversion.
		double x = distance * Math.cos(teta);
		double y = distance * Math.sin(teta);

		node.setCenter(x, y);

		// Traverse all neighbors of this node and recursively call this
		// function.

		List<LEdge> neighborEdges = new LinkedList<LEdge>(node.getEdges());
		int childCount = neighborEdges.size();

		if (parentOfNode != null)
		{
			childCount--;
		}

		int branchCount = 0;

		int incEdgesCount = neighborEdges.size();
		int startIndex;

		List edges = node.getEdgesBetween(parentOfNode);

		// If there are multiple edges, prune them until there remains only one
		// edge.
		while (edges.size() > 1)
		{
			neighborEdges.remove(edges.remove(0));
			incEdgesCount--;
			childCount--;
		}

		if (parentOfNode != null)
		{
			assert edges.size() == 1;
			startIndex =
				(neighborEdges.indexOf(edges.get(0)) + 1) % incEdgesCount;
		}
		else
		{
			startIndex = 0;
		}

		double stepAngle = Math.abs(endAngle - startAngle) / childCount;

		for (int i = startIndex;
			branchCount != childCount ;
			i = (++i) % incEdgesCount)
		{
			LNode currentNeighbor =
				neighborEdges.get(i).getOtherEnd(node);

			// Don't back traverse to root node in current tree.
			if (currentNeighbor == parentOfNode)
			{
				continue;
			}

			double childStartAngle =
				(startAngle + branchCount * stepAngle) % 360;
			double childEndAngle = (childStartAngle + stepAngle) % 360;

			branchRadialLayout(currentNeighbor,
					node,
					childStartAngle, childEndAngle,
					distance + radialSeparation, radialSeparation);

			branchCount++;
		}
	}

	/**
	 * This method finds the maximum diagonal length of the nodes in given tree.
	 */
	private static double maxDiagonalInTree(ArrayList<LNode> tree)
	{
		double maxDiagonal = Double.MIN_VALUE;

		for (int i = 0; i < tree.size(); i++)
		{
			LNode node = tree.get(i);
			double diagonal = node.getDiagonal();

			if (diagonal > maxDiagonal)
			{
				maxDiagonal = diagonal;
			}
		}

		return maxDiagonal;
	}

//	private void updateAnnealingProbability()
//	{
//		CoSELayout.annealingProbability = Math.pow(Math.E,
//			CoSELayout.annealingConstant / CoSELayout.coolingFactor);
//	}

// -----------------------------------------------------------------------------
// Section: Class Constants
// -----------------------------------------------------------------------------
	/**
	 * Orientation enumeration list
	 */
	public static final int RIGHT = 0;
	public static final int LEFT = 2;
	public static final int TOP = 3;
	public static final int BOTTOM = 1;

	/**
	 * Phases of layout
	 */
	public static final int PHASE_1 = 1;
	public static final int PHASE_2 = 2;
	public static final int PHASE_3 = 3;

	/**
	 * Number of iterations that should be done in between convergence checks
	 */
	public static final int CONVERGENCE_CHECK_PERIOD = 100;

	/**
	 * Number of iterations that should be done in between growing trees
	 */
	public static final int TREE_GROWING_FREQUENCY = 150;

	/**
	 * Ideal edge length coefficient for different phases
	 */
	public static final int IDEAL_EDGE_LENGTH_COEFFICIENT_PHASE1_MINOR1 = 2;
	public static final int IDEAL_EDGE_LENGTH_COEFFICIENT_PHASE1_MINOR2 = 1;
	public static final int IDEAL_EDGE_LENGTH_COEFFICIENT_PHASE2 = 1;
	public static final int IDEAL_EDGE_LENGTH_COEFFICIENT_PHASE3 = 1;

	/**
	 * Default ideal edge length coefficient per level for intergraph edges
	 */
	public static final double DEFAULT_PER_LEVEL_IDEAL_EDGE_LENGTH_FACTOR = 0.0;

	/**
	 * Minimum legth of an edge
	 */
	public static final int MIN_EDGE_LENGTH = 1;

	/**
	 * Interval that the spring force should be fit into
	 */
	public static final double MAX_SPRING_FORCE = 5000;
	public static final double MIN_SPRING_FORCE = -MAX_SPRING_FORCE;

	/**
	 * Maximum repulsion force that can be applied on a node
	 */
	public static final double MAX_REPULSION_FORCE = 50;

	/**
	 * User customizable layout options
	 */

	// Layout property defaults
	public static final int DEFAULT_EDGE_LENGTH = 5;//60;
	public static final boolean DEFAULT_UNIFORM_LEAFS = false;
	public static final double DEFAULT_SPRING_STRENGTH = 1.0;
	public static final double DEFAULT_REPULSION_STRENGTH = 730; //1000;
	public static final double DEFAULT_GRAVITY_STRENGTH = 0.005;
	public static final double DEFAULT_COMPOUND_GRAVITY_STRENGTH = 2.8;//10;

	/**
	 * Default distance between each level in radial layout
	 */
	public static final double DEFAULT_RADIAL_SEPARATION = DEFAULT_EDGE_LENGTH;

	/**
	 * Default separation of trees in a forest when tiled to a grid
	 */
	public static final int DEFAULT_COMPONENT_SEPERATION = 60;

// -----------------------------------------------------------------------------
// Section: Class Variables
// -----------------------------------------------------------------------------
	/**
	 * Ideal edge length coefficient per level for intergraph edges
	 */
	public static double perLevelIdealEdgeLengthFactor =
		DEFAULT_PER_LEVEL_IDEAL_EDGE_LENGTH_FACTOR;

	/**
	 * Factor used for cooling during only the last phase of layout; starts from
	 * 1.0 and goes down towards zero as we approach maximum iterations.
	 */
	public static double coolingFactor = 1.0;

//	public static double annealingConstant = Math.log(0.1);
//
//	public static double annealingProbability;
//
//	public static boolean simulatedAnnealingOn = true;


	/**
	 * Total displacement made in an iteration
	 */
	public static double totalDisplacement = 0.0;

	/**
	 * Total displacement made in the previous iteration
	 */
	public static double oldTotalDisplacement = 0.0;

//	public static int randomizedMovementCount = 0;

//	public static int nonRandomizedMovementCount = 0;
}
