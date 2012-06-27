package org.gvt.layout;

import java.util.*;

/**
 * This class is used in Spring Layout to calculate the euclidean distances
 * between nodes.
 *
 * @author Class is taken from GINY library
 * @author Cihan Kucukkececi (modified by)
 */
public class SpringNodeDistances
{
	public static final int INFINITY = 2147483647;

	protected List nodesList;

	protected int distances[][];

	protected boolean directed, done, canceled;

	protected int currentProgress, lengthOfTask;

	protected String statusMessage;

	/**
	 * @deprecated Method SpringNodeDistances is deprecated
	 */
	public SpringNodeDistances(List nodes_list, int distances[][])
	{
		this(nodes_list, distances, false);
	}

	/**
	 * @deprecated Method SpringNodeDistances is deprecated
	 */
	public SpringNodeDistances(List nodes_list,
		int distances[][],
		boolean directed)
	{
		nodesList = nodes_list;

		if (distances == null)
		{
			this.distances = new int[nodesList.size()][];
		}
		else
		{
			this.distances = distances;
		}

		this.directed = directed;
	}

	public SpringNodeDistances(List nodesList)
	{
		this.nodesList = nodesList;
		this.distances = new int[nodesList.size()][];
		this.directed = false;
	}

	public int getCurrentProgress()
	{
		return currentProgress;
	}

	public int getLengthOfTask()
	{
		return lengthOfTask;
	}

	public String getTaskDescription()
	{
		return "Calculating Node Distances";
	}

	public String getCurrentStatusMessage()
	{
		return statusMessage;
	}

	public boolean isDone()
	{
		return done;
	}

	public void stop()
	{
		canceled = true;
		statusMessage = null;
	}

	public boolean wasCanceled()
	{
		return canceled;
	}

	public int[][] calculate()
	{
		currentProgress = 0;
		lengthOfTask = distances.length;
		done = false;
		canceled = false;
		LNode nodes[] = new LNode[nodesList.size()];
		Integer integers[] = new Integer[nodes.length];

		for (int i = 0; i < nodes.length; i++)
		{
			LNode from_node = (LNode) nodesList.get(i);

			if (from_node == null)
			{
				continue;
			}

			int index = nodesList.indexOf(from_node);

			if (index < 0 || index >= nodes.length)
			{
				System.err.println("WARNING: GraphLNode \"" + from_node
					+ "\" has an index value that is out of range: " + index
					+ ".  Graph indices should be maintained such "
					+ "that no index is unused.");
				return null;
			}

			if (nodes[index] != null)
			{
				System.err.println("WARNING: GraphLNode \"" + from_node
					+ "\" has an index value ( " + index
					+ " ) that is the same as "
					+ "that of another GraphLNode ( \"" + nodes[index]
					+ "\" ).  Graph indices should be maintained such "
					+ "that indices are unique.");
				return null;
			}

			nodes[index] = from_node;
			Integer in = new Integer(index);
			integers[index] = in;
		}

		LinkedList queue = new LinkedList();
		boolean completed_nodes[] = new boolean[nodes.length];

		for (int from_node_index = 0; from_node_index < nodes.length;
			 from_node_index++)
		{
			if (canceled)
			{
				distances = null;
				return distances;
			}

			LNode from_node = nodes[from_node_index];

			if (from_node == null)
			{
				if (distances[from_node_index] == null)
				{
					distances[from_node_index] = new int[nodes.length];
				}

				Arrays.fill(distances[from_node_index], INFINITY);
				continue;
			}

			if (distances[from_node_index] == null)
			{
				distances[from_node_index] = new int[nodes.length];
			}

			Arrays.fill(distances[from_node_index], INFINITY);
			distances[from_node_index][from_node_index] = 0;
			Arrays.fill(completed_nodes, false);
			queue.add(integers[from_node_index]);

			while (!queue.isEmpty())
			{
				if (canceled)
				{
					distances = null;
					return distances;
				}

				int index = ((Integer) queue.removeFirst()).intValue();

				if (!completed_nodes[index])
				{
					completed_nodes[index] = true;
					LNode to_node = nodes[index];
					int to_node_distance = distances[from_node_index][index];

					if (index < from_node_index)
					{
						int i = 0;

						while (i < nodes.length)
						{
							if (distances[index][i] != INFINITY)
							{
								int distance_through_to_node = to_node_distance
									+ distances[index][i];

								if (distance_through_to_node
									<= distances[from_node_index][i])
								{
									if (distances[index][i] == 1)
									{
										completed_nodes[i] = true;
									}

									distances[from_node_index][i] =
										distance_through_to_node;
								}
							}

							i++;
						}
					}
					else
					{
						Iterator neighbors =
							to_node.getNeighborsList().iterator();

						while (neighbors.hasNext())
						{
							if (canceled)
							{
								distances = null;

								return distances;
							}

							LNode neighbor = (LNode) neighbors.next();
							int neighbor_index = nodesList.indexOf(neighbor);

							if (neighbor_index >= 0)
							{
								if (nodes[neighbor_index] == null)
								{
									distances[from_node_index][neighbor_index]
										= INFINITY;
								}
								else if (!completed_nodes[neighbor_index])
								{
									int neighbor_distance =
										distances[from_node_index]
											[neighbor_index];

									if (to_node_distance != INFINITY &&
										neighbor_distance > to_node_distance +1)
									{
										distances[from_node_index]
											[neighbor_index] =
											to_node_distance + 1;
										queue.addLast(integers[neighbor_index]);
									}
								}
							}
						}
					}
				}
			}

			currentProgress++;
			double percentDone = (currentProgress * 100) / lengthOfTask;
			statusMessage = "Completed " + percentDone + "%.";
		}

		done = true;
		currentProgress = lengthOfTask;
		
		return distances;
	}

	public int[][] getDistances()
	{
		return distances;
	}
}