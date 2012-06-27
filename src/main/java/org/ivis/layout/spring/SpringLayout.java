package org.ivis.layout.spring;

import java.util.*;

import org.ivis.layout.LGraph;
import org.ivis.layout.LNode;
import org.ivis.layout.Layout;
import org.ivis.layout.LayoutConstants;
import org.ivis.layout.LayoutOptionsPack;
import org.ivis.util.PointD;

/**
 * This class lays out the given Chisio graph model using a Spring Embedder
 * (taken from the GINY library).
 *
 * @author Class is taken from GINY library
 * @author Cihan Kucukkececi (modified by)
 * @author Selcuk Onur Sumer (modified by)
 */
public class SpringLayout extends Layout
{
	protected int numLayoutPasses, nodeCount, layoutPass;

	protected double averageIterationsPerNode,
		nodeDistanceStrengthConstant,
		nodeDistanceRestLengthConstant,
		disconnectedNodeDistanceSpringStrength;

	protected double nodeDistanceSpringScalars[] =
		SpringConstants.DEFAULT_NODE_DISTANCE_SPRING_SCALARS;

	protected double anticollisionSpringScalars[] =
		SpringConstants.DEFAULT_ANTICOLLISION_SPRING_SCALARS;

	protected double disconnectedNodeDistanceSpringRestLength;

	protected double anticollisionSpringStrength;

	protected double nodeDistanceSpringStrengths[][];

	protected double nodeDistanceSpringRestLengths[][];

	List nodeList;

	public double totalEnergy = 0.0D;

	/**
	 * Passes parameters to super
	 */
	public SpringLayout()
	{
		super();
	}

	public void doLayout()
	{
		nodeList = new ArrayList();
		
		for(Object obj: this.getGraphManager().getAllNodes())
		{
			LNode node = (LNode) obj;
			
			if (node.getChild() == null)
			{
				nodeList.add(node);
			}
		}

		nodeCount = nodeList.size();
		int edgeCount = this.getGraphManager().getAllEdges().length;

		double euclidean_distance_threshold =
			0.5D * (double) (nodeCount + edgeCount);

		int num_iterations = (int)
			(((double) nodeCount * averageIterationsPerNode)
				/ (double) numLayoutPasses);

		List partials_list = new ArrayList();
		SpringNode furthest_node_partials = null;

		for (layoutPass = 0; layoutPass < numLayoutPasses; layoutPass++)
		{
			setupNodeDistanceSprings();
			totalEnergy = 0.0D;
			partials_list.clear();
			Iterator node_views_iterator = nodeList.iterator();

			do
			{
				if (!node_views_iterator.hasNext())
				{
					break;
				}

				SpringNode partials = (SpringNode) node_views_iterator.next();
				calculatePartials(partials, null, false);
				partials_list.add(partials);

				if (furthest_node_partials == null ||
					partials.euclideanDistance >
						furthest_node_partials.euclideanDistance)
				{
					furthest_node_partials = partials;
				}
			}
			while (true);

			for (int iterations_i = 0;
				iterations_i < num_iterations &&
					furthest_node_partials.euclideanDistance >=
						euclidean_distance_threshold;
				iterations_i++)
			{
				furthest_node_partials = moveNode(furthest_node_partials,
					partials_list);
			}
		}

		// Resize compounds according to their children
		List graphs = this.graphManager.getGraphs();

		for (int i = 0; i < graphs.size() - 1; i++)
		{
			LGraph graph = (LGraph) graphs.get(i);
			graph.getParent().updateBounds();
		}
	}

	protected void setupNodeDistanceSprings()
	{
		if (layoutPass != 0)
		{
			return;
		}

		nodeDistanceSpringRestLengths = new double[nodeCount][nodeCount];
		nodeDistanceSpringStrengths = new double[nodeCount][nodeCount];

		if (nodeDistanceSpringScalars[layoutPass] == 0.0D)
		{
			return;
		}

		SpringNodeDistances ind = new SpringNodeDistances(nodeList);
		int node_distances[][] = ind.calculate();

		if (node_distances == null)
		{
			return;
		}

		double node_distance_strength_constant = nodeDistanceStrengthConstant;
		double node_distance_rest_length_constant =
			nodeDistanceRestLengthConstant;

		for (int node_i = 0; node_i < nodeCount; node_i++)
		{
			for (int node_j = node_i + 1; node_j < nodeCount; node_j++)
			{
				if (node_distances[node_i][node_j] ==
					SpringNodeDistances.INFINITY)
				{
					nodeDistanceSpringRestLengths[node_i][node_j] =
						disconnectedNodeDistanceSpringRestLength;
				}
				else
				{
					nodeDistanceSpringRestLengths[node_i][node_j] =
						node_distance_rest_length_constant
							* (double) node_distances[node_i][node_j];
				}

				nodeDistanceSpringRestLengths[node_j][node_i] =
					nodeDistanceSpringRestLengths[node_i][node_j];

				if (node_distances[node_i][node_j] ==
					SpringNodeDistances.INFINITY)
				{
					nodeDistanceSpringStrengths[node_i][node_j]
						= disconnectedNodeDistanceSpringStrength;
				}
				else
				{
					nodeDistanceSpringStrengths[node_i][node_j] =
						node_distance_strength_constant /
							(double) (node_distances[node_i][node_j] *
								node_distances[node_i][node_j]);
				}

				nodeDistanceSpringStrengths[node_j][node_i] =
					nodeDistanceSpringStrengths[node_i][node_j];
			}
		}
	}

	protected SpringNode calculatePartials(SpringNode partials,
		List partials_list,
		boolean reversed)
	{
		partials.reset();
		LNode node = partials;
		int node_view_index = nodeList.indexOf(node);
		double node_view_radius = node.getWidth();
		double node_view_x = node.getRect().x;
		double node_view_y = node.getRect().y;
		SpringNode other_node_partials = null;
		SpringNode furthest_partials = null;
		Iterator iterator;

		if (partials_list == null)
		{
			iterator = nodeList.iterator();
		}
		else
		{
			iterator = partials_list.iterator();
		}

		do
		{
			if (!iterator.hasNext())
			{
				break;
			}

			LNode other_node;

			if (partials_list == null)
			{
				other_node = (LNode) iterator.next();
			}
			else
			{
				other_node_partials = (SpringNode) iterator.next();
				other_node = other_node_partials;
			}

			if (nodeList.indexOf(node) != nodeList.indexOf(other_node))
			{
				int other_node_view_index = nodeList.indexOf(other_node);
				double other_node_view_radius = other_node.getWidth();
				double delta_x = node_view_x - other_node.getRect().x;
				double delta_y = node_view_y - other_node.getRect().y;
				double euclidean_distance =
					Math.sqrt(delta_x * delta_x + delta_y * delta_y);
				double euclidean_distance_cubed =
					Math.pow(euclidean_distance, 3D);
				double distance_from_touching = euclidean_distance -
					(node_view_radius + other_node_view_radius);
				double incremental_change =
					nodeDistanceSpringScalars[layoutPass] *
						(nodeDistanceSpringStrengths[node_view_index]
							[other_node_view_index]	*
							(delta_x - (
							nodeDistanceSpringRestLengths[node_view_index]
								[other_node_view_index]	* delta_x) /
								euclidean_distance));

				if (!reversed)
				{
					partials.x += incremental_change;
				}

				if (other_node_partials != null)
				{
					incremental_change = nodeDistanceSpringScalars[layoutPass]
						* (
						nodeDistanceSpringStrengths[other_node_view_index]
							[node_view_index]
							* (-delta_x - (
							nodeDistanceSpringRestLengths[other_node_view_index]
								[node_view_index]
								* -delta_x) / euclidean_distance));

					if (reversed)
					{
						other_node_partials.x -= incremental_change;
					}
					else
					{
						other_node_partials.x += incremental_change;
					}
				}

				if (distance_from_touching < 0.0D)
				{
					incremental_change = anticollisionSpringScalars[layoutPass]
						* (anticollisionSpringStrength * (delta_x -
						((node_view_radius + other_node_view_radius) * delta_x)
							/ euclidean_distance));

					if (!reversed)
					{
						partials.x += incremental_change;
					}

					if (other_node_partials != null)
					{
						incremental_change =
							anticollisionSpringScalars[layoutPass] * (
								anticollisionSpringStrength * (-delta_x - (
									(node_view_radius + other_node_view_radius)
										* -delta_x) / euclidean_distance));

						if (reversed)
						{
							other_node_partials.x -= incremental_change;
						}
						else
						{
							other_node_partials.x += incremental_change;
						}
					}
				}

				incremental_change = nodeDistanceSpringScalars[layoutPass] * (
					nodeDistanceSpringStrengths[node_view_index]
						[other_node_view_index]
						* (delta_y - (
						nodeDistanceSpringRestLengths[node_view_index]
							[other_node_view_index]
							* delta_y) / euclidean_distance));

				if (!reversed)
				{
					partials.y += incremental_change;
				}

				if (other_node_partials != null)
				{
					incremental_change = nodeDistanceSpringScalars[layoutPass]
						* (
						nodeDistanceSpringStrengths[other_node_view_index]
							[node_view_index]
							* (-delta_y - (
							nodeDistanceSpringRestLengths[other_node_view_index]
								[node_view_index]
								* -delta_y) / euclidean_distance));

					if (reversed)
					{
						other_node_partials.y -= incremental_change;
					}
					else
					{
						other_node_partials.y += incremental_change;
					}
				}

				if (distance_from_touching < 0.0D)
				{
					incremental_change = anticollisionSpringScalars[layoutPass]
						* (anticollisionSpringStrength * (delta_y -
						((node_view_radius + other_node_view_radius) * delta_y)
							/ euclidean_distance));

					if (!reversed)
					{
						partials.y += incremental_change;
					}

					if (other_node_partials != null)
					{
						incremental_change =
							anticollisionSpringScalars[layoutPass] * (
								anticollisionSpringStrength * (-delta_y - (
									(node_view_radius + other_node_view_radius)
										* -delta_y) / euclidean_distance));

						if (reversed)
						{
							other_node_partials.y -= incremental_change;
						}
						else
						{
							other_node_partials.y += incremental_change;
						}
					}
				}

				incremental_change = nodeDistanceSpringScalars[layoutPass] * (
					nodeDistanceSpringStrengths[node_view_index]
						[other_node_view_index]
						* (1.0D - (
						nodeDistanceSpringRestLengths[node_view_index]
							[other_node_view_index]
							* (delta_y * delta_y)) / euclidean_distance_cubed));

				if (reversed)
				{
					if (other_node_partials != null)
					{
						other_node_partials.xx -= incremental_change;
					}
				}
				else
				{
					partials.xx += incremental_change;

					if (other_node_partials != null)
					{
						other_node_partials.xx += incremental_change;
					}
				}

				if (distance_from_touching < 0.0D)
				{
					incremental_change = anticollisionSpringScalars[layoutPass]
						* (anticollisionSpringStrength * (1.0D -
						((node_view_radius + other_node_view_radius) * (delta_y
							* delta_y)) / euclidean_distance_cubed));

					if (reversed)
					{
						if (other_node_partials != null)
						{
							other_node_partials.xx -= incremental_change;
						}
					}
					else
					{
						partials.xx += incremental_change;

						if (other_node_partials != null)
						{
							other_node_partials.xx += incremental_change;
						}
					}
				}

				incremental_change = nodeDistanceSpringScalars[layoutPass] * (
					nodeDistanceSpringStrengths[node_view_index]
						[other_node_view_index]
						* (1.0D - (
						nodeDistanceSpringRestLengths[node_view_index]
							[other_node_view_index]
							* (delta_x * delta_x)) / euclidean_distance_cubed));

				if (reversed)
				{
					if (other_node_partials != null)
					{
						other_node_partials.yy -= incremental_change;
					}
				}
				else
				{
					partials.yy += incremental_change;

					if (other_node_partials != null)
					{
						other_node_partials.yy += incremental_change;
					}
				}

				if (distance_from_touching < 0.0D)
				{
					incremental_change = anticollisionSpringScalars[layoutPass]
						* (anticollisionSpringStrength * (1.0D -
						((node_view_radius + other_node_view_radius) * (delta_x
							* delta_x)) / euclidean_distance_cubed));

					if (reversed)
					{
						if (other_node_partials != null)
						{
							other_node_partials.yy -= incremental_change;
						}
					}
					else
					{
						partials.yy += incremental_change;

						if (other_node_partials != null)
						{
							other_node_partials.yy += incremental_change;
						}
					}
				}
				incremental_change = nodeDistanceSpringScalars[layoutPass] * (
					nodeDistanceSpringStrengths[node_view_index]
						[other_node_view_index]
						* ((
						nodeDistanceSpringRestLengths[node_view_index]
							[other_node_view_index]
							* (delta_x * delta_y)) / euclidean_distance_cubed));

				if (reversed)
				{
					if (other_node_partials != null)
					{
						other_node_partials.xy -= incremental_change;
					}
				}
				else
				{
					partials.xy += incremental_change;

					if (other_node_partials != null)
					{
						other_node_partials.xy += incremental_change;
					}
				}

				if (distance_from_touching < 0.0D)
				{
					incremental_change = anticollisionSpringScalars[layoutPass]
						* (anticollisionSpringStrength * (
						((node_view_radius + other_node_view_radius) * (delta_x
							* delta_y)) / euclidean_distance_cubed));

					if (reversed)
					{
						if (other_node_partials != null)
						{
							other_node_partials.xy -= incremental_change;
						}
					}
					else
					{
						partials.xy += incremental_change;

						if (other_node_partials != null)
						{
							other_node_partials.xy += incremental_change;
						}
					}
				}

				double distance_from_rest = euclidean_distance
					- nodeDistanceSpringRestLengths[node_view_index]
					[other_node_view_index];
				incremental_change = nodeDistanceSpringScalars[layoutPass] * ((
					nodeDistanceSpringStrengths[node_view_index]
						[other_node_view_index]
						* (distance_from_rest * distance_from_rest)) / 2D);

				if (reversed)
				{
					if (other_node_partials != null)
					{
						totalEnergy -= incremental_change;
					}
				}
				else
				{
					totalEnergy += incremental_change;

					if (other_node_partials != null)
					{
						totalEnergy += incremental_change;
					}
				}

				if (distance_from_touching < 0.0D)
				{
					incremental_change = anticollisionSpringScalars[layoutPass]
						* ((anticollisionSpringStrength * (
						distance_from_touching * distance_from_touching)) / 2D);

					if (reversed)
					{
						if (other_node_partials != null)
						{
							totalEnergy -= incremental_change;
						}
					}
					else
					{
						totalEnergy += incremental_change;

						if (other_node_partials != null)
						{
							totalEnergy += incremental_change;
						}
					}
				}

				if (other_node_partials != null)
				{
					other_node_partials.euclideanDistance = Math.sqrt(
						other_node_partials.x * other_node_partials.x
							+ other_node_partials.y * other_node_partials.y);

					if (furthest_partials == null ||
						other_node_partials.euclideanDistance >
							furthest_partials.euclideanDistance)
					{
						furthest_partials = other_node_partials;
					}
				}
			}
		}
		while (true);

		if (!reversed)
		{
			partials.euclideanDistance =
				Math.sqrt(partials.x * partials.x + partials.y * partials.y);
		}

		if (furthest_partials == null ||
			partials.euclideanDistance > furthest_partials.euclideanDistance)
		{
			furthest_partials = partials;
		}

		return furthest_partials;
	}

	protected SpringNode moveNode(SpringNode partials,
		List partials_list)
	{

		SpringNode starting_partials = new SpringNode(partials);
		calculatePartials(partials, partials_list, true);
		simpleMoveNode(starting_partials, partials);

		return calculatePartials(partials,
			partials_list,
			false);
	}

	protected void simpleMoveNode(SpringNode partialsInfo,
		SpringNode partials)
	{
		double denomenator = partialsInfo.xx * partialsInfo.yy -
			partialsInfo.xy * partialsInfo.xy;
		double delta_x = (-partialsInfo.x * partialsInfo.yy	- -partialsInfo.y *
			partialsInfo.xy) / denomenator;
		double delta_y = (-partialsInfo.y * partialsInfo.xx	- -partialsInfo.x *
			partialsInfo.xy) / denomenator;
		PointD p = partials.getLocation();
		partials.setLocation(p.x + delta_x, p.y + delta_y);
	}

	/**
	 * This method creates a new node associated with the input view node.
	 */
	public LNode newNode(Object vNode)
	{
		return new SpringNode(this.graphManager, vNode);
	}

	public boolean layout()
	{
		if (!this.incremental)
		{
			this.positionNodesRandomly();
		}
		
		this.doLayout();
		
		return true;
	}

	public void initParameters()
	{
		super.initParameters();

		LayoutOptionsPack.Spring layoutOptionsPack =
			LayoutOptionsPack.getInstance().getSpring();

		nodeDistanceStrengthConstant =
			SpringConstants.DEFAULT_NODE_DISTANCE_STRENGTH_CONSTANT;
		disconnectedNodeDistanceSpringStrength =
			SpringConstants.DEFAULT_DISCONNECTED_NODE_DISTANCE_SPRING_STRENGTH;
		anticollisionSpringStrength =
			SpringConstants.DEFAULT_ANTICOLLISION_SPRING_STRENGTH;

		nodeDistanceRestLengthConstant =
			layoutOptionsPack.getNodeDistanceRestLength();
		disconnectedNodeDistanceSpringRestLength =
			layoutOptionsPack.getDisconnectedNodeDistanceSpringRestLength();

		if (layoutQuality == LayoutConstants.DEFAULT_QUALITY)
		{
			numLayoutPasses = SpringConstants.DEFAULT_NUM_LAYOUT_PASSES;
			averageIterationsPerNode =
				SpringConstants.DEFAULT_AVERAGE_ITERATIONS_PER_NODE;
		}
		else if (layoutQuality == LayoutConstants.DRAFT_QUALITY)
		{
			numLayoutPasses = SpringConstants.DEFAULT_NUM_LAYOUT_PASSES - 2;
			averageIterationsPerNode =
				SpringConstants.DEFAULT_AVERAGE_ITERATIONS_PER_NODE - 20;
		}
		else
		{
			numLayoutPasses = SpringConstants.DEFAULT_NUM_LAYOUT_PASSES + 2;
			averageIterationsPerNode =
				SpringConstants.DEFAULT_AVERAGE_ITERATIONS_PER_NODE + 20;
		}
	}
}