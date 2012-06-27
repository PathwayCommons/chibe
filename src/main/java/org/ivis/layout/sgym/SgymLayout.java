/*
 * @(#)SugiyamaLayoutAlgorithm.java 1.0 18-MAY-2004
 *
 * Copyright (c) 2004, Sven Luzar
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of JGraph nor the names of its contributors may be used
 *   to endorse or promote products derived from this software without specific
 *   prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.ivis.layout.sgym;

import java.text.NumberFormat;
import java.util.*;

import org.ivis.layout.LEdge;
import org.ivis.layout.LGraph;
import org.ivis.layout.LGraphManager;
import org.ivis.layout.LNode;
import org.ivis.layout.Layout;
import org.ivis.layout.LayoutOptionsPack;
import org.ivis.util.DimensionD;
import org.ivis.util.PointD;
import org.ivis.util.RectangleD;

/**
 * This class lays out the given Chisio model using the Sugiyama Layout
 * Algorithm.
 *
 * <a href="http://plg.uwaterloo.ca/~itbowman/CS746G/Notes/Sugiyama1981_MVU/">
 * Link to the algorithm</a>
 *
 * @author Sven Luzar
 * @author Gaudenz Alder (modified by)
 * @author Cihan Kucukkececi (modified by)
 * @author Selcuk Onur Sumer (modified by)
 */
public class SgymLayout extends Layout
{
	/**
	 * Field for debug output
	 */
	protected final boolean verbose = false;

	/**
	 * Represents the size of the grid in horizontal grid elements
	 */
	protected int gridAreaSize = Integer.MIN_VALUE;

	/**
	 * A vector with Integer Objects. The Vector contains the history of
	 * movements per loop. It was needed for the progress dialog.
	 */
	Vector movements = null;

	/**
	 * Represents maximum of movements in the current loop. It was needed for
	 * the progress dialog.
	 */
	int movementsMax = Integer.MIN_VALUE;

	/**
	 * Represents current loop number. It was needed for the progress dialog.
	 */
	int iteration = 0;

	/**
	 * orientation of layout.
	 */
	protected boolean vertical = SgymConstants.DEFAULT_VERTICAL;

	private int horizontalSpacing = SgymConstants.DEFAULT_HORIZONTAL_SPACING;

	private int verticalSpacing = SgymConstants.DEFAULT_VERTICAL_SPACING;

// -----------------------------------------------------------------------------
// Section: Constructors and initializations
// -----------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	public SgymLayout()
	{
		super();
	}

	/**
	 * This method creates a new node associated with the input view node.
	 */
	public LNode newNode(Object vNode)
	{
		return new SgymNode(this.graphManager, vNode);
	}
	
	/**
	 * Implementation.
	 *
	 * First of all the Algorithm searches the roots from the Graph. Starting
	 * from this roots the Algorithm creates levels and stores them in the
	 * member <code>levels</code>.
	 * The Member levels contains Vector Objects and the Vector per level
	 * contains Cell Wrapper Objects. After that the Algorithm tries to solve
	 * the edge crosses from level to level and goes top down and bottom up.
	 * After minimization of the edge crosses the algorithm moves each node to
	 * its bary center. Last but not Least the method draws the Graph.
	 */
	public void perform(LGraph graph, boolean bends)
	{
		if (graph == null)
		{
			return;
		}

		List<PointD> spacing = new ArrayList();
		List<SgymNode> nodes = graph.getNodes();

		// Compound nodes inner grahs are layouted
		for (int i = 0; i < nodes.size(); i++)
		{
			perform(nodes.get(i).getChild(), bends);
		}

		// TODO For compound node support, inter-edges are converted to intra-edges
		// reassignEdges(graph);
		
		// Search all roots
		Vector roots = searchRoots(nodes);

		// return if no root found, for example empty compounds have no root
		if (roots.size() == 0)
		{
			return;
		}

		// topologically sort the nodes by using DFS
		List topological = new ArrayList();
		topologicalSortWithDFS(topological, roots);

		// by using topological information, creates levels
		Vector levels = fillLevels(topological);

		// shorten the edge lengths via changing levels of some nodes
		pushUpNodes(levels);

		for (int i = levels.size() - 2; i >= 0; i--)
		{
			spacing.add(new PointD(horizontalSpacing, verticalSpacing));
			Vector v = (Vector) levels.remove(i);
			levels.add(v);
		}

		spacing.add(new PointD(horizontalSpacing, verticalSpacing));

		// if create bendpoints are enabled from options, bendpoints are created
		
		if (bends)
		{
			this.createBendpoints(levels);

			// reset all edges, since the topology has changed
			this.graphManager.resetAllEdges();
		}
		
		
		// minimize number of edge crosses between each two levels via
		// barycentric method
		solveEdgeCrosses(nodes, levels);

		// adjust vertices horizontally to minimize edge lengths
		// move all nodes into the barycenter
		moveToBarycenter(nodes, levels);

		// finds the spacings between levels and nodes
		PointD min = this.findMinimumAndSpacing(nodes, spacing);

		// transers the calculations to l-nodes
		this.drawGraph(nodes, levels, min, spacing);

		// shifts the layers for better understanding
		if (vertical)
		{
			shiftLayers(levels);
		}

		graph.getParent().updateBounds();
	}

	/**
	 * Method creates a Vector for the list of nodes for each level then fills
	 * this Vector and returns it.
	 */
	public Vector fillLevels(List<SgymNode> topological)
	{
		Vector levels = new Vector();

		SgymNode node = topological.get(0);
		node.rank = 0;
		Vector v = new Vector();
		levels.add(v);
		node.addToEdgeCrossesIndicator(v.size());
		v.add(node);
		// rearranges the levels if there is an edge between two nodes which are
		// belongs to the same level
		for (int i = 1; i < topological.size(); i++)
		{
			node = topological.get(i);
			Iterator iter = node.getSuccessors().iterator();
			int max = -1;

			while (iter.hasNext())
			{
				SgymNode next = (SgymNode) iter.next();

				if (next.rank > max)
				{
					max = next.rank;
				}
			}

			node.rank = max + 1;

			if (levels.size() == node.rank)
			{
				v = new Vector();
				levels.add(v);
			}
			else
			{
				v = (Vector) levels.elementAt(node.rank);
			}

			node.addToEdgeCrossesIndicator(v.size());
			v.add(node);
		}

		SgymNode.levelSize = levels.size();

		return levels;
	}

	/**
	 * This method creates the bendpoints for multi-level edges
	 *
	 * @param levels
	 */
	public void createBendpoints(Vector levels)
	{
		// ok, let's add some fake nodes
		int lev = 0;
		Iterator it = levels.iterator();
		
		while (it.hasNext())
		{
			Vector curLev = (Vector) it.next();
			Iterator it2 = curLev.iterator();

			while (it2.hasNext())
			{
				SgymNode topV = (SgymNode) it2.next();
				HashSet tempSet = new HashSet();
				tempSet.addAll(topV.getOutEdges());
				Iterator it3 = tempSet.iterator();

				while (it3.hasNext())
				{
					LEdge e = (LEdge) it3.next();
					SgymNode botV = (SgymNode) e.getTarget();
					int botLev = botV.getLevel();

					if (botLev > lev + 1)
					{
						int dummyNo = botLev - lev - 1;

						for (int i = 0; i < dummyNo; i++)
						{
							e.getBendpoints().add(new PointD());
						}

						List dummies = this.createDummyNodesForBendpoints(e);
						
						for (int i = 0; i < dummyNo; i++)
						{
							int level = i + lev + 1;
							SgymNode dummy = (SgymNode) dummies.get(i);
							dummy.setLevel(level);
							dummy.visited = true;
							Vector v = (Vector)levels.get(level);
							dummy.addToEdgeCrossesIndicator(v.size());
							v.add(dummy);
						}
					}
				}
			}

			lev++;
		}
	}
	
	public void shiftLayers(Vector levels)
	{
		for (int i = 0; i < levels.size(); i++)
		{
			Vector curLayer = (Vector) levels.elementAt(i);
			double prev = averageTension(curLayer);
			shiftLayer(curLayer, 5);
			double amtShifted = 5;
			double right = averageTension(curLayer);
			shiftLayer(curLayer, -10);
			amtShifted += -10;
			double left = averageTension(curLayer);
			double sign = 1;

			if (left < right)
			{
				sign = sign * -1;
			}

			double cur;

			while (true)
			{
				shiftLayer(curLayer, sign * 5);
				amtShifted += sign * 5;
				cur = averageTension(curLayer);

				if (cur > prev)
				{
					shiftLayer(curLayer, sign * -1 * 5);
					amtShifted += -1 * sign * 5;
					break;
				}

				if(cur == 0 && prev == 0)
				{
					break;
				}

				prev = cur;
			}

			for (int j = i - 1; j >= 0; j--)
			{
				curLayer = (Vector) levels.elementAt(j);
				shiftLayer(curLayer, amtShifted);
			}
		}

		for (int i = levels.size() - 1; i > 0; i--)
		{
			Vector curLayer = (Vector) levels.elementAt(i);
			double prev = averageTension(curLayer);
			shiftLayer(curLayer, 5);
			double amtShifted = 5;
			double right = averageTension(curLayer);
			shiftLayer(curLayer, -10);
			amtShifted += -10;
			double left = averageTension(curLayer);
			double sign = 1;

			if (left < right)
			{
				sign = sign * -1;
			}

			double cur;

			while (true)
			{
				shiftLayer(curLayer, sign * 5);
				amtShifted += sign * 5;
				cur = averageTension(curLayer);

				if (cur > prev)
				{
					shiftLayer(curLayer, sign * -1 * 5);
					amtShifted += -1 * sign * 5;
					break;
				}

				if(cur == 0 && prev == 0)
				{
					break;
				}

				prev = cur;
			}

			for (int j = i + 1; j < levels.size(); j++)
			{
				curLayer = (Vector) levels.elementAt(j);
				shiftLayer(curLayer, amtShifted);
			}
		}
	}

	public void shiftLayer(Vector curLayer, double inc)
	{
		if (curLayer == null)
		{
			return;
		}

		for (int n = 0; n < curLayer.size(); n++)
		{
			SgymNode nh = (SgymNode) curLayer.elementAt(n);
			nh.getRect().x += inc;
			transformChildren(nh, new DimensionD((int) inc, 0));
		}
	}

	public double averageTension(Vector curLayer)
	{
		int segments = 0;
		double totallength = 0;

		for (int n = 0; n < curLayer.size(); n++)
		{
			SgymNode v = (SgymNode) curLayer.elementAt(n);
			Iterator it = v.getNeighborsList().iterator();

			while (it.hasNext())
			{
				SgymNode neig = (SgymNode) it.next();

				if (neig != null)
				{
					double length = Math.sqrt((v.getRect().x - neig.getRect().x) *
						(v.getRect().x - neig.getRect().x) +
						(v.getRect().y - neig.getRect().y) *
						(v.getRect().y - neig.getRect().y));
					totallength += length;
					segments++;
				}
			}
		}

		if (segments == 0)
		{
			return (0);
		}
		else
		{
			return (totallength / segments);
		}
	}

	/**
	 * Debug display for the edge crosses indicators on the System out
	 */
	protected void displayEdgeCrossesValues(Vector levels)
	{
		System.out.println("----------------Edge Crosses Indicator Values");

		for (int i = 0; i < levels.size() - 1; i++)
		{
			// Get the current level
			Vector currentLevel = (Vector) levels.get(i);
			System.out.print("Level (" + i + "):");

			for (int j = 0; j < currentLevel.size(); j++)
			{
				SgymNode sourceWrapper = (SgymNode) currentLevel.get(j);

				System.out.print(NumberFormat.getNumberInstance().format(
					sourceWrapper.getEdgeCrossesIndicator()) + " - ");
			}

			System.out.println();
		}
	}

	/**
	 * Debug display for the grid positions on the System out
	 */
	protected void displayGridPositions(Vector levels)
	{
		System.out.println("----------------GridPositions");

		for (int i = 0; i < levels.size() - 1; i++)
		{
			// Get the current level
			Vector currentLevel = (Vector) levels.get(i);
			System.out.print("Level (" + i + "):");

			for (int j = 0; j < currentLevel.size(); j++)
			{
				SgymNode sourceWrapper = (SgymNode) currentLevel.get(j);
				System.out.print(NumberFormat.getNumberInstance().format(
					sourceWrapper.getGridPosition()) + " - ");
			}

			System.out.println();
		}
	}

	/**
	 * Debug display for the priorities on the System out
	 */
	protected void displayPriorities(Vector levels)
	{
		System.out.println("----------------down Priorities");

		for (int i = 0; i < levels.size() - 1; i++)
		{
			// Get the current level
			Vector currentLevel = (Vector) levels.get(i);
			System.out.print("Level (" + i + "):");

			for (int j = 0; j < currentLevel.size(); j++)
			{
				SgymNode sourceWrapper = (SgymNode) currentLevel.get(j);
				System.out.print(sourceWrapper.getPriority() + " - ");
			}

			System.out.println();
		}
	}

	/**
	 * Searches all Roots for the current Graph
	 * First the method marks any Node as not visited.
	 * Than calls searchRoots for each not visited node. Also finds the
	 * disconnected components. The Roots are stored in the Vector named roots
	 */
	protected Vector searchRoots(List<SgymNode> nodes)
	{
		// get all cells and relations
		Vector roots = new Vector();
		int[] incomingEdgeCount = new int[nodes.size()];
		int[] outgoingEdgeCount = new int[nodes.size()];
		List rootCountPerDisconnected = new ArrayList();
		List<Set> disconnected = new ArrayList();

		// first: mark all as not visited
		for (int i = 0; i < nodes.size(); i++)
		{
			nodes.get(i).setVisited(false);
			incomingEdgeCount[i] = 0;
			outgoingEdgeCount[i] = 0;
		}

		int count = 0;

		for (int i = 0; i < nodes.size(); i++)
		{
			if (nodes.get(i).isVisited() == false)
			{
				Set set = new HashSet();
				searchRoots(nodes,
					nodes.get(i),
					roots,
					incomingEdgeCount,
					outgoingEdgeCount,
					set);
				rootCountPerDisconnected.add(roots.size() - count);
				disconnected.add(set);
				count = roots.size();
			}
		}

		for (int i = 0; i < disconnected.size(); i++)
		{
			if (((Integer) rootCountPerDisconnected.get(i)).intValue() == 0)
			{
				Set set = disconnected.get(i);
				int min = incomingEdgeCount.length;
				int index = -1;
				Iterator iter = set.iterator();

				while (iter.hasNext())
				{
					LNode node = (LNode) iter.next();
					int loc = nodes.indexOf(node);

					if (outgoingEdgeCount[loc] > 0 &&
						incomingEdgeCount[loc] < min)
					{
						min = incomingEdgeCount[loc];
						index = loc;
					}
				}

				if (index > -1)
				{
					roots.add(nodes.get(index));
				}
			}
		}

		return roots;
	}

	/**
	 * Searches Roots for the current Cell.
	 * <p/>
	 * Therefore he looks at all Ports from the Cell.
	 * At the Ports he looks for Edges.
	 * At the Edges he looks for the Target.
	 * If the Ports of the current Cell contains the target ReViewNodePort
	 * he follows the edge to the source and looks at the
	 * Cell for this source.
	 *
	 * @param node The current cell
	 */
	protected void searchRoots(List<SgymNode> nodes,
		SgymNode node,
		Vector roots,
		int[] incomingEdgeCount,
		int[] outgoingEdgeCount,
		Set set)
	{
		// the node already visited
		if (node.isVisited())
		{
			return;
		}

		// mark as visited for cycle tests
		node.setVisited(true);
		set.add(node);
		// Test all relations for where
		// the current node is a target node
		// for roots

		boolean isRoot = true;
		Iterator<LEdge> itrEdges = node.getEdges().iterator();

		while (itrEdges.hasNext())
		{
			LEdge edge = itrEdges.next();

			if (!edge.isInterGraph())
			{
				// if the current node is a target node get the source node and
				// test the source node for roots
				if (edge.getTarget() == node)
				{
					SgymNode source = (SgymNode) edge.getSource();
					searchRoots(nodes,
						source,
						roots,
						incomingEdgeCount,
						outgoingEdgeCount,
						set);
					incomingEdgeCount[nodes.indexOf(node)]++;
					isRoot = false;
				}
				else
				{
					SgymNode target = (SgymNode) edge.getTarget();
					searchRoots(nodes,
						target,
						roots,
						incomingEdgeCount,
						outgoingEdgeCount,
						set);
					outgoingEdgeCount[nodes.indexOf(node)]++;
				}
			}
		}

		// The current node is never a Target Node
		// -> The current node is a root node
		if (isRoot)
		{
			roots.add(node);
		}
	}

	/**
	 *  Topologically sorts the nodes with DFS algorithm
	 * @param topological
	 * @param roots
	 */
	protected void topologicalSortWithDFS(List<SgymNode> topological,
		Vector roots)
	{
		Iterator enumRoots = roots.iterator();

		while (enumRoots.hasNext())
		{
			SgymNode root = (SgymNode) enumRoots.next();

			if (root.color == 0)
			{
				dfsVisit(topological, root);
			}
		}
	}

	private void dfsVisit(List<SgymNode> topological, SgymNode root)
	{
		root.color = 1;
		// iterate any Edge in the port
		Iterator itrEdges = root.getEdges().iterator();

		while (itrEdges.hasNext())
		{
			LEdge edge = (LEdge) itrEdges.next();

			if (!edge.isInterGraph())
			{
				// if the Edge is a forward edge we should follow this edge
				if (root == edge.getSource())
				{
					SgymNode target = (SgymNode) edge.getTarget();

					if (target.color == 0)
					{
						target.ancestor = root;
						dfsVisit(topological, target);
					}
					else if (target.color == 1)
					{
						// Cycle detected!
						this.solveCycle(edge);
					}
				}
			}
		}

		root.color = 2;
		topological.add(root);
	}

	public void solveCycle(LEdge edge)
	{	
		// reverse the edge by swapping source and target nodes
		this.getGraphManager().getRoot().reverse(edge);
	}

	/**
	 * Calculates the minimum for the paint area.
	 */
	protected PointD findMinimumAndSpacing(List<SgymNode> nodes,
		List<PointD> spacing)
	{
		try
		{
			// variables
			/* represents the minimum x value for the paint area
			 */
			int min_x = 1000000;

			/* represents the minimum y value for the paint area
			 */
			int min_y = 1000000;

			// find the maximum & minimum coordinates

			for (int i = 0; i < nodes.size(); i++)
			{
				// the cellView and their bounds
				SgymNode node = nodes.get(i);
				RectangleD nodeBounds = node.getRect();

				// checking min area
				try
				{
					if (nodeBounds.x < min_x)
					{
						min_x = (int) nodeBounds.x;
					}

					if (nodeBounds.y < min_y)
					{
						min_y = (int) nodeBounds.y;
					}

					if (nodeBounds.width > spacing.get(node.getLevel()).x)
					{
						spacing.get(node.getLevel()).x =
							(int) nodeBounds.width + horizontalSpacing;
					}

					if (nodeBounds.height > spacing.get(node.getLevel()).y)
					{
						spacing.get(node.getLevel()).y =
							(int) nodeBounds.height + verticalSpacing;
					}
				}
				catch (Exception e)
				{
					System.err.println("---------> ERROR in calculateValues.");
					e.printStackTrace();
				}
			}
			// if the cell sice is bigger than the userspacing
			// dublicate the spacingfactor
			return new PointD(min_x, min_y);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	protected void solveEdgeCrosses(List<SgymNode> nodes,
		Vector levels)
	{
		movements = new Vector(100);
		int movementsCurrentLoop = -1;
		movementsMax = Integer.MIN_VALUE;
		iteration = 0;

		while (movementsCurrentLoop != 0)
		{
			// reset the movements per loop count
			movementsCurrentLoop = 0;

			if (verbose)
			{
				System.out.println("---------------------------- vor Sort");
				displayEdgeCrossesValues(levels);
			}

			// top down
			for (int i = 0; i < levels.size() - 1; i++)
			{
				movementsCurrentLoop +=
					solveEdgeCrosses(nodes, true, levels, i);
			}

			// bottom up
			for (int i = levels.size() - 1; i >= 1; i--)
			{
				movementsCurrentLoop +=
					solveEdgeCrosses(nodes, false, levels, i);
			}

			if (verbose)
			{
				System.out.println("---------------------------- nach Sort");
				displayEdgeCrossesValues(levels);
			}
		}
	}

	/**
	 * @return movements
	 */
	protected int solveEdgeCrosses(List<SgymNode> jgraph,
		boolean down,
		Vector levels,
		int levelIndex)
	{
		// Get the current level
		Vector currentLevel = (Vector) levels.get(levelIndex);
		int movements = 0;

		// restore the old sort
		Object[] levelSortBefore = currentLevel.toArray();

		// new sort
		Collections.sort(currentLevel);

		// test for movements
		for (int j = 0; j < levelSortBefore.length; j++)
		{
			if (((SgymNode) levelSortBefore[j]).getEdgeCrossesIndicator() !=
				((SgymNode) currentLevel.get(j)).getEdgeCrossesIndicator())
			{
				movements++;
			}
		}

		// Collecations Sort sorts the highest value to the first value
		for (int j = currentLevel.size() - 1; j >= 0; j--)
		{
			SgymNode source = (SgymNode) currentLevel.get(j);

			Iterator sourceEdges = source.getEdges().iterator();

			while (sourceEdges.hasNext())
			{
				LEdge edge = (LEdge) sourceEdges.next();

				if (!edge.isInterGraph())
				{
					// if it is a forward edge follow it
					SgymNode target = null;

					if (down && source == edge.getSource())
					{
						target = (SgymNode) edge.getTarget();
					}

					if (!down && source == edge.getTarget())
					{
						target = (SgymNode) edge.getSource();
					}

					if (target == null)
					{
						continue;
					}

					//do it only if the edge is a forward edge to a deeper level
					if (down && target != null &&
						target.getLevel() > levelIndex)
					{
						target.addToEdgeCrossesIndicator(
							source.getEdgeCrossesIndicator());
					}

					if (!down && target != null &&
						target.getLevel() < levelIndex)
					{
						target.addToEdgeCrossesIndicator(
							source.getEdgeCrossesIndicator());
					}
				}
			}
		}

		return movements;
	}

	protected void moveToBarycenter(List<SgymNode> nodes,
		Vector levels)
	{
		// iterate any ReViewNodePort
		for (int i = 0; i < nodes.size(); i++)
		{
			if (!(nodes.get(i) instanceof SgymNode))
			{
				continue;
			}

			SgymNode node = nodes.get(i);
			Iterator edges = node.getEdges().iterator();

			while (edges.hasNext())
			{
				LEdge edge = (LEdge) edges.next();
				SgymNode neighbor;

				if (!edge.isInterGraph())
				{
					// if the Edge is a forward edge we should follow this edge
					if (node == edge.getSource())
					{
						neighbor = (SgymNode) edge.getTarget();
					}
					else
					{
						if (node == edge.getTarget())
						{
							neighbor = (SgymNode) edge.getSource();
						}
						else
						{
							continue;
						}
					}

					if (neighbor == node)
					{
						continue;
					}

					if (node == null || node == null ||
						node.getLevel() == neighbor.getLevel())
					{
						continue;
					}

					node.priority++;
				}
			}
		}

		for (int j = 0; j < levels.size(); j++)
		{
			Vector level = (Vector) levels.get(j);

			for (int i = 0; i < level.size(); i++)
			{
				// calculate the initial Grid Positions 1, 2, 3, .... per Level
				SgymNode wrapper = (SgymNode) level.get(i);
				wrapper.setGridPosition(i);
			}
		}

		if (verbose)
		{
			System.out.println("----------------Grid Pos before top down");
			displayPriorities(levels);
			displayGridPositions(levels);
			System.out.println("=======================================");
		}

		movements = new Vector(100);
		int movementsCurrentLoop = -1;
		movementsMax = Integer.MIN_VALUE;
		iteration = 0;

		while (movementsCurrentLoop != 0)
		{
			// reset movements
			movementsCurrentLoop = 0;

			// top down
			for (int i = 1; i < levels.size(); i++)
			{
				movementsCurrentLoop += moveToBarycenter(nodes, levels, i);
			}

			if (verbose)
			{
				System.out.println("----------------Grid Pos after top down");
				displayGridPositions(levels);
				System.out.println("=======================================");
			}

			// bottom up
			for (int i = levels.size() - 1; i >= 0; i--)
			{
				movementsCurrentLoop += moveToBarycenter(nodes, levels, i);
			}

			if (verbose)
			{
				System.out.println("----------------Grid Pos after bottom up");
				displayGridPositions(levels);
				System.out.println("=======================================");
			}
		}
	}

	protected int moveToBarycenter(List<SgymNode> nodes,
		Vector levels,
		int levelIndex)
	{
		// Counter for the movements
		int movements = 0;

		// Get the current level
		Vector currentLevel = (Vector) levels.get(levelIndex);

		for (int currentIndexInTheLevel = 0;
			 currentIndexInTheLevel < currentLevel.size();
			 currentIndexInTheLevel++)
		{
			SgymNode source =
				(SgymNode) currentLevel.get(currentIndexInTheLevel);

			float gridPositionsSum = 0;
			float countNodes = 0;
			Iterator edges = source.getEdges().iterator();

			while (edges.hasNext())
			{
				LEdge edge = (LEdge) edges.next();
				SgymNode neighbor;

				if (!edge.isInterGraph())
				{
					// if the Edge is a forward edge we should follow this edge
					if (source == edge.getSource())
					{
						neighbor = (SgymNode) edge.getTarget();
					}
					else
					{
						if (source == edge.getTarget())
						{
							neighbor = (SgymNode) edge.getSource();
						}
						else
						{
							continue;
						}
					}

					SgymNode target = neighbor;

					if (target == source)
					{
						continue;
					}

					if (target == null || target.getLevel() == levelIndex)
					{
						continue;
					}

					gridPositionsSum += target.getGridPosition();
					countNodes++;
				}
			}
			//----------------------------------------------------------
			// move node to new x coord
			//----------------------------------------------------------

			if (countNodes > 0)
			{
				float tmp = (gridPositionsSum / countNodes);
				int newGridPosition = Math.round(tmp);
				boolean toRight = (newGridPosition > source.getGridPosition());
				boolean moved = true;

				while (newGridPosition != source.getGridPosition() && moved)
				{
					int tmpGridPos = source.getGridPosition();

					moved = move(toRight,
						currentLevel,
						currentIndexInTheLevel,
						source.getPriority());

					if (moved)
					{
						movements++;
					}

					if (verbose)
					{
						System.out.print(
							"try move at Level "
								+ levelIndex
								+ " with index "
								+ currentIndexInTheLevel
								+ " to "
								+ (toRight ? "Right" : "Left")
								+ " CurrentGridPos: "
								+ tmpGridPos
								+ " NewGridPos: "
								+ newGridPosition
								+ " exact: "
								+ NumberFormat.getInstance().format(tmp)
								+ "...");
						System.out.println(moved ? "success" : "can't move");
					}
				}
			}
		}

		return movements;
	}

	protected boolean move(boolean toRight,
		Vector currentLevel,
		int currentIndexInTheLevel,
		int currentPriority)
	{
		SgymNode currentWrapper =
			(SgymNode) currentLevel.get(currentIndexInTheLevel);

		boolean moved;
		int neighborIndexInTheLevel =
			currentIndexInTheLevel + (toRight ? 1 : -1);
		int newGridPosition =
			currentWrapper.getGridPosition() + (toRight ? 1 : -1);

		// is the grid position possible?

		if (0 > newGridPosition || newGridPosition >= gridAreaSize)
		{
			return false;
		}

		// if the node is the first or the last we can move
		if (toRight && currentIndexInTheLevel == currentLevel.size() - 1 ||
			!toRight && currentIndexInTheLevel == 0)
		{
			moved = true;
		}
		else
		{
			// else get the neighbor and ask his gridposition
			// if he has the requested new grid position
			// check the priority

			SgymNode neighborWrapper =
				(SgymNode) currentLevel.get(neighborIndexInTheLevel);

			int neighborPriority = neighborWrapper.getPriority();

			if (neighborWrapper.getGridPosition() == newGridPosition)
			{
				if (neighborPriority >= currentPriority)
				{
					return false;
				}
				else
				{
					moved = move(toRight,
						currentLevel,
						neighborIndexInTheLevel,
						currentPriority);
				}
			}
			else
			{
				moved = true;
			}
		}

		if (moved)
		{
			currentWrapper.setGridPosition(newGridPosition);
		}

		return moved;
	}

	/**
	 * This method draws the graph. For the horizontal position
	 * we are using the grid position from each graphcell.
	 * For the vertical position we are using the level position.
	 */
	protected void drawGraph(List<SgymNode> jgraph,
		Vector levels,
		PointD min,
		List<PointD> spacing)
	{
		for (int rowCellCount = 0;
			 rowCellCount < levels.size();
			 rowCellCount++)
		{
			Vector level = (Vector) levels.get(rowCellCount);

			for (int colCellCount = 0;
				 colCellCount < level.size();
				 colCellCount++)
			{
				SgymNode node = (SgymNode) level.get(colCellCount);
				RectangleD bounds = new RectangleD((int) node.getRect().x,
					(int) node.getRect().y,
					(int) node.getRect().width,
					(int) node.getRect().height);

				PointD prev = new PointD(bounds.x, bounds.y);

				PointD space = this.getSpacing(spacing,
					node.getLevel(),
					node.getGridPosition());
				bounds.x = min.x + space.x;
				bounds.y = min.y + space.y;

				node.setLocation(bounds.x, bounds.y);

				// also compound node's inner nodes are moved
				DimensionD diff = (new PointD(bounds.x, bounds.y)).
					getDifference(prev);
				this.transformChildren(node, diff);
			}
		}
	}

	public PointD getSpacing(List<PointD> spacing, int level, int gridPos)
	{
		PointD space = new PointD(0, 0);

		if (vertical)
		{
			for (int i = 0; i < level; i++)
			{
				space.y += spacing.get(i).y;
			}

			space.x += spacing.get(level).x * gridPos;
		}
		else
		{
			for (int i = 0; i < level; i++)
			{
				space.x += spacing.get(i).x;
			}

			space.y += spacing.get(level).y * gridPos;
		}

		return space;
	}

	public void transformChildren(LNode wrapper, DimensionD difference)
	{
		if (wrapper.getChild() != null)
		{
			for(int i = 0; i < wrapper.getChild().getNodes().size(); i++)
			{
				SgymNode node = (SgymNode) wrapper.getChild().getNodes().get(i);
				PointD prev = node.getLocation().getCopy();
				PointD newLoc = node.getLocation().translate(difference);
				node.setLocation(newLoc.x, newLoc.y);

				DimensionD diff = node.getLocation().getDifference(prev);
				this.transformChildren(node, diff);
			}
		}
	}

	/**
	 * This method convert inter-edges to intra-edges for compound node support.
	 * @param graph
	 */
	public void reassignEdges(LGraph graph)
	{
		List<LEdge> edges = graph.getEdges();
		List<LEdge> copyList = new ArrayList<LEdge>(graph.getEdges());

		List removeList = new ArrayList();
		
		for (int i = 0; i < copyList.size(); i++)
		{
			LEdge edge = copyList.get(i);

			if (edge.isInterGraph())
			{
				Iterator parents = edge.getTarget().getAllParents().iterator();
				boolean done = false;

				LNode prevParent = null;
				LNode parent = (LNode) parents.next();

				while (parent != null)
				{
					if (parent == graph.getParent())
					{						
						LNode newTarget = prevParent;
						this.graphManager.remove(edge);
						
						LEdge newEdge = this.newEdge(edge.vGraphObject);
						
						this.graphManager.add(newEdge,
							edge.getSource(),
							newTarget);
						
						done = true;

						break;
					}

					prevParent = parent;

					if (parents.hasNext())
					{
						parent = (LNode) parents.next();
					}
					else
					{
						parent = null;
					}
				}

				if (!done)
				{
					LNode oldSource = edge.getSource();
					LNode newSource = graph.getParent();

					LEdge newEdge = this.newEdge(edge.vGraphObject);
					
					this.graphManager.add(newEdge,
						newSource,
						edge.getTarget());
					
					oldSource.getEdges().remove(edge);

					removeList.add(edge);
				}
			}
		}

		// remove all edges in the remove list
		for(int i = 0; i < removeList.size(); i++)
		{
			removeList.get(i);
			graph.remove((LNode) removeList.get(i));
		}
		
	}
	
	
	public boolean layout()
	{
		// Check if the graph contains any compound structures; should be flat.
		if (this.graphManager.getGraphs().size() > 1)
		{
			return false;
		}
		
		boolean createBendsAsNeeded = LayoutOptionsPack.getInstance().
			getGeneral().isCreateBendsAsNeeded();

		this.perform(this.graphManager.getRoot(), createBendsAsNeeded);
		
		// TODO check if layout is successful
		// currently perform method does not return boolean value
		return true;
//		assert check();
	}

	public void initParameters()
	{
		super.initParameters();
		
		LayoutOptionsPack.Sgym layoutOptionsPack =
			LayoutOptionsPack.getInstance().getSgym();

		this.horizontalSpacing = layoutOptionsPack.getHorizontalSpacing();
		this.verticalSpacing = layoutOptionsPack.getVerticalSpacing();
		this.vertical = layoutOptionsPack.isVertical();
	}

	/**
	 * This method checks whether the invariants of this object is satisfied.
	 */
	public boolean check()
	{
		// Check whether there is a weird edge (from an owner compound to a node
		// inside it)!

		Object[] lEdges = this.getGraphManager().getAllEdges();
		LEdge edge;

		for (int i = 0; i < lEdges.length; i++)
		{
			edge = (LEdge) lEdges[i];

			if (LGraphManager.isOneAncestorOfOther(edge.getSource(), edge.getTarget()))
			{
				return false;
			}

			if(((SgymNode)edge.getSource()).getLevel() >=
				((SgymNode)edge.getTarget()).getLevel())
			{
				return false;
			}
		}

		// Check whether there is any bend points in the graph
		if (!createBendsAsNeeded)
		{
			for (int i = 0; i < lEdges.length; i++)
			{
				edge = (LEdge) lEdges[i];

				if (edge.getBendpoints().size() != 0)
				{
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * This method tries push up the nodes starting from the 2nd level.
	 * Returns false if no node is moved to a greater rank, true otherwise.
	 */
	public boolean pushUpNodes(Vector levels)
	{
		boolean hasRankChanged = false;
		ArrayList<SgymNode> list;

		for (int i = levels.size() -3 ; i >= 0; i--)
		{
			Vector vector = (Vector)levels.get(i);
			list =  new ArrayList<SgymNode>();

			for (int j = 0; j < vector.size(); j++)
			{
				SgymNode node = (SgymNode)vector.get(j);
				boolean hasThisNodeChangedRank = node.pushNodeUp();

				if (hasThisNodeChangedRank)
				{
					list.add(node);
				}

				hasRankChanged |= hasThisNodeChangedRank;
			}

			Vector temp;

			for (int k = 0; k < list.size(); k++)
			{
				SgymNode node = list.get(k);
				vector.remove(node);
				temp = (Vector)levels.get(node.rank);
				temp.add(node);
			}
		}

		return hasRankChanged;
	}
}