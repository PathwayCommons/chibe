package org.ivis.layout.sgym;

import java.awt.Dimension;
import java.awt.Point;
import java.util.*;

import org.ivis.layout.LEdge;
import org.ivis.layout.LGraphManager;
import org.ivis.layout.LNode;


/**
 * This class implements l-level nodes corresponding to Sugiyama Layout.
 *
 * @author Refer to class SgymLayout
 * @author Cihan Kucukkececi (modified by)
 * @author Selcuk Onur Sumer (modified by)
 */
public class SgymNode extends LNode implements Comparable
{
// ----------------------------------------------------------------------------
// Section: Instance variables
// ----------------------------------------------------------------------------
	/**
	 * node is visited or not
	 */
	protected boolean visited;

	/**
	 * sum value for edge Crosses
	 */
	private double edgeCrossesIndicator = 0;

	/**
	 * counter for additions to the edgeCrossesIndicator
	 */
	private int additions = 0;

	/**
	 * current position in the grid
	 */
	int gridPosition = 0;

	/**
	 * priority for movements to the barycenter
	 */
	int priority = 0;

	// used for dfs
	int color = 0;
	SgymNode ancestor = null;

	/**
	 * Level information (levelSize - rank -1)
	 */
	public int rank = -1;

	public static int levelSize;
	
// -----------------------------------------------------------------------------
// Section: Constructors and initialization
// -----------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	public SgymNode(LGraphManager gm, Object vNode)
	{
		super(gm, vNode);
	}

	/**
	 * Alternative constructor
	 */
	public SgymNode(LGraphManager gm, Point loc, Dimension size, Object vNode)
	{
		super(gm, loc, size, vNode);
	}

	public void initialize()
	{
		super.initialize();
		this.visited = false;
	}

// -----------------------------------------------------------------------------
// Section: Remaining methods
// -----------------------------------------------------------------------------
	public boolean isVisited()
	{
		return visited;
	}

	public void setVisited(boolean visited)
	{
		this.visited = visited;
	}

	/**
	 * resets the indicator for edge crosses to 0
	 */
	void resetEdgeCrossesIndicator()
	{
		edgeCrossesIndicator = 0;
		additions = 0;
	}

	/**
	 * retruns the average value for the edge crosses indicator for the wrapped
	 * cell
	 */
	double getEdgeCrossesIndicator()
	{
		if (additions == 0)
			return 0;
		return edgeCrossesIndicator / additions;
	}

	/**
	 * adds a value to the edge crosses indicator for the wrapped cell
	 */
	void addToEdgeCrossesIndicator(double addValue)
	{
		edgeCrossesIndicator += addValue;
		additions++;
	}

	public int getLevel()
	{
		return levelSize - rank - 1;
	}

	public void setLevel(int level)
	{
		rank = levelSize - level - 1;
	}

	/**
	 * gets the grid position for the wrapped cell
	 */
	int getGridPosition()
	{
		return gridPosition;
	}

	/**
	 * Sets the grid position for the wrapped cell
	 */
	void setGridPosition(int pos)
	{
		this.gridPosition = pos;
	}

	/**
	 * increments the the priority of this cell wrapper.
	 * <p/>
	 * The priority was used by moving the cell to its
	 * barycenter.
	 */

	void incrementPriority()
	{
		priority++;
	}

	/**
	 * returns the priority of this cell wrapper. The priority was used by
	 * moving the cell to its barycenter.
	 */
	int getPriority()
	{
		return priority;
	}

	/**
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object compare)
	{
		if (((SgymNode) compare).getEdgeCrossesIndicator()
			== this.getEdgeCrossesIndicator())
			return 0;

		double compareValue =
			(((SgymNode) compare).getEdgeCrossesIndicator()
				- this.getEdgeCrossesIndicator());

		return (int) (compareValue * 1000);
	}

	public Collection getOutEdges()
	{
		List outEdges = new ArrayList();
		List edges = getEdges();

		for (int i = 0; i < edges.size(); i++ )
		{
			LEdge edge = (LEdge) edges.get(i);

			if (edge.getSource() == this)
			{
				outEdges.add(edge);
			}
		}

		return outEdges;
	}

	/**
	 * This method returns the edges to which this node is attached as target.
	 */
	public Collection getInEdges()
	{
		List inEdges = new ArrayList();
		List edges = getEdges();

		for (int i = 0; i < edges.size(); i++ )
		{
			LEdge edge = (LEdge) edges.get(i);

			if (edge.getTarget() == this)
			{
				inEdges.add(edge);
			}
		}

		return inEdges;
	}

	/**
	 * This method finds the number that is one less than the rank of the
	 * closest (in terms of level) parent and sets the new rank of this node if
	 * it is different from its current rank. Returns true if a new rank is set
	 * to this node, false otherwise.
	 */
	public boolean pushNodeUp()
	{
		Collection inEdges = this.getInEdges();
		Iterator iter = inEdges.iterator();
		int minParentRank = Integer.MAX_VALUE;

		if (this.rank == -1 || inEdges.size() == 0)
		{
			return false;
		}

		boolean hasSetNewRank = false;

		while (iter.hasNext())
		{
			LEdge edge = (LEdge)iter.next();
			SgymNode parent = (SgymNode)edge.getOtherEnd(this);

			if (parent.rank < minParentRank)
			{
				minParentRank = parent.rank;
			}
		}

		if (minParentRank - 1 > this.rank)
		{
			hasSetNewRank = true;
			this.rank = minParentRank - 1;
		}

		return hasSetNewRank;
	}
}