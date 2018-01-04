package org.gvt.layout;

import org.gvt.model.NodeModel;
import org.gvt.model.biopaxl2.Actor;
import org.ivis.layout.LEdge;
import org.ivis.layout.LGraph;
import org.ivis.layout.LGraphManager;
import org.ivis.layout.LNode;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author Ozgun Babur
 */
public class BiPaLayout extends org.ivis.layout.cose.CoSELayout
{
	/**
	 * For remembering contents of a complex.
	 */
	Map<BiPaNode, LGraph> childGraphMap;
	Map<BiPaNode, MemberPack> memberPackMap;

	public BiPaLayout()
	{
		childGraphMap = new HashMap<BiPaNode, LGraph>();
		memberPackMap = new HashMap<BiPaNode, MemberPack>();
	}

	@Override
	public LNode newNode(Object vNode)
	{
		return new BiPaNode(this.graphManager, vNode);
	}

	public LNode newNode(LGraphManager gm, Point loc, Dimension size, Object vNode)
	{
		return new BiPaNode(gm, loc, size, vNode);
	}

	public LNode createNewLNode(LGraphManager gm, NodeModel model)
	{
		return new BiPaNode(gm, model);
	}

	/**
	 * Empties contents of a complex temporarily.
	 */
	protected void clearComplexes()
	{
		for (Object o : getAllNodes())
		{
			if (!(o instanceof BiPaNode) || !((BiPaNode) o).isComplex()) continue;

			BiPaNode comp = (BiPaNode) o;

			if (comp.getChild().getNodes().isEmpty()) continue;
			
			LGraph childGr = comp.getChild();
			childGraphMap.put(comp, childGr);
			MemberPack pack = new MemberPack(childGr);
			memberPackMap.put(comp, pack);
			getGraphManager().getGraphs().remove(childGr);
			comp.setChild(null);

			comp.setWidth(pack.getWidth());
			comp.setHeight(pack.getHeight());

			 // Redirect the edges of complex members to the complex.

			for (Object ch : childGr.getNodes())
			{
				BiPaNode chNd = (BiPaNode) ch;
				for (Object obj : new ArrayList(chNd.getEdges()))
				{
					LEdge edge = (LEdge) obj;
					if (edge.getSource() == chNd)
					{
						chNd.getEdges().remove(edge);
						edge.setSource(comp);
						comp.getEdges().add(edge);
					}
					else if (edge.getTarget() == chNd)
					{
						chNd.getEdges().remove(edge);
						edge.setTarget(comp);
						comp.getEdges().add(edge);
					}
				}
			}
		}
		getGraphManager().resetAllNodes();
		getGraphManager().resetAllNodesToApplyGravitation();
		getGraphManager().resetAllEdges();
	}

	/**
	 * Reassigns the complex content.
	 */
	protected void repopulateComplexes()
	{
		for (BiPaNode comp : childGraphMap.keySet())
		{
			LGraph chGr = childGraphMap.get(comp);
			comp.setChild(chGr);
			getGraphManager().getGraphs().add(chGr);
			MemberPack pack = memberPackMap.get(comp);
			pack.adjustLocations(comp.getLeft(), comp.getTop());
		}
		getGraphManager().resetAllNodes();
		getGraphManager().resetAllNodesToApplyGravitation();
		getGraphManager().resetAllEdges();
	}

	public boolean layout()
	{
		clearComplexes();
		boolean b = super.layout();
		repopulateComplexes();
		return b;
	}


	protected class MemberPack
	{
		private List<BiPaNode> members;
		private Organization org;

		public MemberPack(LGraph childG)
		{
			members = new ArrayList<BiPaNode>();
			members.addAll(childG.getNodes());
			
			sortMembers();
			
			// choose the organization that approximates to a square shaped complex more
			org = getBestOrganizationForMembers(members);
		}
		
		// Create 2 organization for the members one favors horizontal dimension in rounding values during calculations
		// while the other one favors the vertical dimension. Return the best of these 2 that approximates a square shaped
		// complex more.
		private Organization getBestOrganizationForMembers(List<BiPaNode> members) 
		{
			// keeps the best organization
			Organization bestOrg;
			
			Organization horizontalOrg = new Organization(members, Organization.FAVOR_DIM_HORIZONTAL);
			Organization verticalOrg = new Organization(members, Organization.FAVOR_DIM_VERTICAL);
			
			// get ratios of bigger the dimension over the smaller one for both of the organizations 
			double horizontalRatio = horizontalOrg.getRatio();
			double verticalRatio = verticalOrg.getRatio();
			
			// the best ratio is the one that is closer to 1 since none of the is smaller than 1
			// and the best organization is the one that has the best ratio
			if (verticalRatio < horizontalRatio) 
			{
				bestOrg = verticalOrg;
			}
			else 
			{
				bestOrg = horizontalOrg;
			}
			
			return bestOrg;
		}

		private void sortMembers()
		{
			Collections.sort(members, new Comparator<BiPaNode>()
			{
				@Override
				public int compare(BiPaNode o1, BiPaNode o2)
				{
					return o1.getText().compareTo(o2.getText());
				}
			});
		}

//		public void layout()
//		{
//			// this comment-outed code was used for sorting members from long to short
//			ComparableNode[] compar = new ComparableNode[members.size()];
//
//			int i = 0;
//			for (BiPaNode node : members)
//			{
//				compar[i++] = new ComparableNode(node);
//			}
//
//			Arrays.sort(compar);
//
//			members.clear();
//			for (ComparableNode com : compar)
//			{
//				members.add(com.getNode());
//			}
//			
//			for (BiPaNode node : members)
//			{
//				org.insertNode(node);
//			}
//		}

		public double getWidth()
		{
			return org.getWidth();
		}

		public double getHeight()
		{
			return org.getHeight();
		}

		public void adjustLocations(double x, double y)
		{
			org.adjustLocations(x, y);
		}
	}

	protected class ComparableNode implements Comparable
	{
		private BiPaNode node;

		public ComparableNode(BiPaNode node)
		{
			this.node = node;
		}

		public BiPaNode getNode()
		{
			return node;
		}

		/**
		 * Inverse compare function to order descending.
		 */
		public int compareTo(Object o)
		{
			return (new Double(((ComparableNode) o).getNode().getWidth())).
				compareTo(node.getWidth());
		}
	}

	protected class Organization
	{
		public static final int FAVOR_DIM_HORIZONTAL = 0;
		public static final int FAVOR_DIM_VERTICAL = 1;
		
		private double width;
		private double height;
		private double idealRowWidth;

		private List<Double> rowWidth;
		private List<LinkedList<BiPaNode>> rows;
		private List<BiPaNode> members;
		private int favorDim;

		public Organization(List<BiPaNode> members, int favorDim)
		{
			// the members of complex
			this.members = members;
						
			// the dimension that is to be favored while rounding values during calculations
			this.favorDim = favorDim;
			
			this.width = COMPLEX_CHILD_GRAPH_BUFFER * 2;
			this.height = (COMPLEX_CHILD_GRAPH_BUFFER * 2) + COMPLEX_LABEL_HEIGHT;

			rowWidth = new ArrayList<Double>();
			rows = new ArrayList<LinkedList<BiPaNode>>();
			
			// calculate the ideal row width according to the dimensions of members
			this.idealRowWidth = calcIdealRowWidth();
			
			// insert members in order
			for (BiPaNode node : members)
			{
				insertNode(node);
			}
		}

		public double getWidth()
		{
			shiftToLastRow();
			return width;
		}

		public double getHeight()
		{
			return height + (firstRowHasInfo() ? Actor.DEFAULT_INFO_BULB : 0) +
				(lastRowHasInfo() ? Actor.DEFAULT_INFO_BULB : 0);
		}
		
		// get the ratio of the bigger dimension over the smaller one
		public double getRatio()
		{
			// get dimensions and calculate the initial ratio
			double width = getWidth();
			double height = getHeight();
			double ratio = width / height;
			
			// if the initial ratio is less then 1 then inverse it
			if (ratio < 1) 
			{
				ratio = 1 / ratio;
			}
			
			// return the normalized ratio
			return ratio;
		}
		
		/*
		 * Calculates the ideal width for the rows. This method assumes that
		 * each node has the same width and calculates the ideal row width that
		 * approximates a square shaped complex accordingly. However, since nodes would 
		 * have different widths some rows would have different sizes and the resulting
		 * shape would not be an exact square.
		 */
		private double calcIdealRowWidth() 
		{
			// To approximate a square shaped complex we need to make complex width equal to complex height.
			// To achieve this we need to solve the following equation system for hc:
			// (x + bx) * hc - bx = (y + by) * vc - by, hc * vc = n
			// where x is the avarage width of the nodes, y is the avarage height of nodes
			// bx and by are the buffer sizes in horizontal and vertical dimensions accordingly,
			// hc and vc are the number of rows in horizontal and vertical dimensions
			// n is number of members.
			
			// number of members
			int membersSize = members.size();
			
			// sum of the width of all members
			double totalWidth = 0;
			
			// sum of the height of all members
			double totalHeight = 0;
			
			// maximum members width
			double maxWidth = Double.MIN_VALUE;
			
			// traverse all members to calculate total width and total height and get the maximum members width
			for (BiPaNode node : members) 
			{
				totalWidth += node.getWidth();
				totalHeight += node.getHeight();
				
				if (node.getWidth() > maxWidth) 
				{
					maxWidth = node.getWidth();
				}
			}
			
			// average width of the members
			double averageWidth = totalWidth / membersSize;
			
			// average height of the members
			double averageHeight = totalHeight / membersSize;
			
			// solving the initial equation system for the hc yields the following second degree equation:
			// hc^2 * (x+bx) + hc * (by - bx) - n * (y + by) = 0 
			
			// the delta value to solve the equation above for hc			
			double delta = Math.pow((COMPLEX_MEM_VERTICAL_BUFFER - COMPLEX_MEM_HORIZONTAL_BUFFER), 2) 
					+ 4 * (averageWidth + COMPLEX_MEM_HORIZONTAL_BUFFER) * (averageHeight + COMPLEX_MEM_VERTICAL_BUFFER) * membersSize;
			
			// solve the equation using delta value to calculate the horizontal count
			// that represents the number of nodes in an ideal row
			double horizontalCountDouble = (COMPLEX_MEM_HORIZONTAL_BUFFER - COMPLEX_MEM_VERTICAL_BUFFER + Math.sqrt(delta))
					/ (2 * (averageWidth + COMPLEX_MEM_HORIZONTAL_BUFFER));
			
			// round the calculated horizontal count up or down according to the favored dimension
			int horizontalCount;
						
			if (favorDim == FAVOR_DIM_HORIZONTAL) 
			{
				horizontalCount = (int) Math.ceil(horizontalCountDouble);
			}
			else
			{
				horizontalCount = (int) Math.floor(horizontalCountDouble);
			}
			
			// ideal width to be calculated
			double idealWidth = horizontalCount * (averageWidth + COMPLEX_MEM_HORIZONTAL_BUFFER) - COMPLEX_MEM_HORIZONTAL_BUFFER;
			
			// if max width is bigger than calculated ideal width reset ideal width to it
			if (maxWidth > idealWidth) 
			{
				idealWidth = maxWidth;
			}
			
			// add the left-right margins to the ideal row width
			idealWidth += COMPLEX_CHILD_GRAPH_BUFFER * 2;
			
			// return the ideal row width1
			return idealWidth;
		}

		private int getLongestRowIndex()
		{
			int r = -1;
			double max = Double.MIN_VALUE;

			for (int i = 0; i < rows.size(); i++)
			{
				if (rowWidth.get(i) > max)
				{
					r = i;
					max = rowWidth.get(i);
				}
			}

			return r;
		}

		public void insertNode(BiPaNode node)
		{
			if (rows.isEmpty())
			{
				insertNodeToRow(node, 0);
			}
			else if (canBeAddedToRow(node.getWidth(), rows.size() - 1))
			{
				insertNodeToRow(node, rows.size() - 1);
			}
			else
			{
				insertNodeToRow(node, rows.size());
			}
		}
		
		// checks if a node with given width can be added to the row with the given index
		private boolean canBeAddedToRow(double nodeWidth, int rowIndex)
		{
			// get the current width of the row
			double currentWidth = rowWidth.get(rowIndex);
			
			// check and return if ideal row width will be exceed if the node is added to the row
			return currentWidth + nodeWidth + COMPLEX_MEM_HORIZONTAL_BUFFER <= idealRowWidth;
		}

		private void insertNodeToRow(BiPaNode node, int rowIndex)
		{
			// Add new row if needed

			if (rowIndex == rows.size())
			{
				if (!rows.isEmpty())
				{
					height += COMPLEX_MEM_VERTICAL_BUFFER;
				}
				rows.add(new LinkedList<BiPaNode>());
				height += Actor.DEFAULT_HEIGHT;
				rowWidth.add(COMPLEX_MIN_WIDTH);

				assert rows.size() == rowWidth.size();
			}

			// Update row width

			double w = rowWidth.get(rowIndex) + node.getWidth();
			if (!rows.get(rowIndex).isEmpty()) w += COMPLEX_MEM_HORIZONTAL_BUFFER;
			rowWidth.set(rowIndex, w);

			// Insert node
			rows.get(rowIndex).add(node);

			// Update complex width

			if (width < w)
			{
				width = w;
			}
		}

		private void shiftToLastRow()
		{
			int longest = getLongestRowIndex();
			int last = rowWidth.size() - 1;
			LinkedList<BiPaNode> row = rows.get(longest);
			BiPaNode node = row.getLast();
			double diff = node.getWidth() + COMPLEX_MEM_HORIZONTAL_BUFFER;

			if (width - rowWidth.get(last) > diff)
			{
				row.removeLast();
				rows.get(last).add(node);
				rowWidth.set(longest, rowWidth.get(longest) - diff);
				rowWidth.set(last, rowWidth.get(last) + diff);

				width = rowWidth.get(getLongestRowIndex());

				shiftToLastRow();
			}
		}

		private boolean firstRowHasInfo()
		{
			return rowHasInfo(rows.get(0));
		}
		private boolean lastRowHasInfo()
		{
			return rowHasInfo(rows.get(rows.size() - 1));
		}

		private boolean rowHasInfo(LinkedList<BiPaNode> row)
		{
			for (BiPaNode node : row)
			{
				if (node.hasInfo()) return true;
			}
			return false;
		}

		public void adjustLocations(double x, double y)
		{
			x += COMPLEX_CHILD_GRAPH_BUFFER;
			y += COMPLEX_CHILD_GRAPH_BUFFER + (firstRowHasInfo() ? Actor.DEFAULT_INFO_BULB : 0);

			double left = x;

			for (LinkedList<BiPaNode> row : rows)
			{
				x = left;

				for (BiPaNode node : row)
				{
					double yy = node.getHeight() - 0.0001 > Actor.DEFAULT_HEIGHT ?
						y - Actor.DEFAULT_INFO_BULB : y;

					node.setLocation(x, yy);
					x += node.getWidth() + COMPLEX_MEM_HORIZONTAL_BUFFER;
				}

				y += Actor.DEFAULT_HEIGHT + COMPLEX_MEM_VERTICAL_BUFFER;
			}
		}
	}

	private static final int COMPLEX_MEM_HORIZONTAL_BUFFER = 5;
	private static final int COMPLEX_MEM_VERTICAL_BUFFER = 5;
	private static final double COMPLEX_CHILD_GRAPH_BUFFER = 10;
	private static final double COMPLEX_LABEL_HEIGHT = 20;
	private static final double COMPLEX_MIN_WIDTH = COMPLEX_CHILD_GRAPH_BUFFER * 2;
}
