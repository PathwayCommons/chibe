package org.ivis.layout.cise;

/**
 * This class implements a pair of on-circle nodes used for swapping in phase 4.
 *
 * @author Esat Belviranli
 */
public class CiSEOnCircleNodePair implements Comparable<CiSEOnCircleNodePair>
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/*
	 * The node of the pair which comes first in the ordering of its owner
	 * circle.
	 */
	private CiSENode firstNode;

	/*
	 * The node of the pair which comes second in the ordering of its owner
	 * circle.
	 */
	private CiSENode secondNode;

	/*
	 * The discrepancy of the displacement values of two nodes, indicating the
	 * swapping potential of the two nodes. Higher value means that nodes are
	 * more inclined to swap.
	 */
	private double discrepancy;

	/*
	 * Whether or not the two nodes are pulling in the same direction
	 */
	private boolean inSameDirection;

// -----------------------------------------------------------------------------
// Section: Constructors and initializations
// -----------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	public CiSEOnCircleNodePair(CiSENode first,
		CiSENode second,
		double displacement,
		boolean inSameDirection)
	{
		assert first.getOnCircleNodeExt() != null &&
			second.getOnCircleNodeExt() != null;

		this.firstNode = first;
		this.secondNode = second;
		this.discrepancy = displacement;
		this.inSameDirection = inSameDirection;
	}

// -----------------------------------------------------------------------------
// Section: Accessors
// -----------------------------------------------------------------------------
	public double getDiscrepancy()
	{
		return this.discrepancy;
	}

	public boolean inSameDirection()
	{
		return this.inSameDirection;
	}

	public CiSENode getFirstNode()
	{
		return this.firstNode;
	}

	public CiSENode getSecondNode()
	{
		return this.secondNode;
	}

// -----------------------------------------------------------------------------
// Section: Remaining methods
// -----------------------------------------------------------------------------
	public int compareTo(CiSEOnCircleNodePair other)
	{
		return (int)(this.getDiscrepancy() - other.getDiscrepancy());
	}

	public void swap()
	{
		this.getFirstNode().getOnCircleNodeExt().swapWith(
			this.getSecondNode().getOnCircleNodeExt());
	}

	public boolean equals(Object other)
	{
		boolean result = other instanceof CiSEOnCircleNodePair;

		if (result)
		{
			CiSEOnCircleNodePair pair = (CiSEOnCircleNodePair) other;

			result &= (this.firstNode.equals(pair.getFirstNode()) &&
				this.secondNode.equals(pair.getSecondNode())) ||
				(this.secondNode.equals(pair.getFirstNode()) &&
						this.firstNode.equals(pair.getSecondNode()));
		}

		return result;
	}

	public int hashCode()
	{
		return this.firstNode.hashCode() + this.secondNode.hashCode();
	}

	public String toString()
	{
		String result = "Swap: " + this.getFirstNode().label;
		result += "<->"+ this.getSecondNode().label;
		result +=", "+ this.getDiscrepancy();

		return result;
	}
}