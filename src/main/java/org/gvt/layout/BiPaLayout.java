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
			org = new Organization();

			sortMembers();
			layout();
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

		public void layout()
		{
			// this comment-outed code was used for sorting members from long to short
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

			for (BiPaNode node : members)
			{
				org.insertNode(node);
			}
		}

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
		private double width;
		private double height;

		private List<Double> rowWidth;
		private List<LinkedList<BiPaNode>> rows;

		public Organization()
		{
			this.width = COMPLEX_CHILD_GRAPH_BUFFER * 2;
			this.height = (COMPLEX_CHILD_GRAPH_BUFFER * 2) + COMPLEX_LABEL_HEIGHT;

			rowWidth = new ArrayList<Double>();
			rows = new ArrayList<LinkedList<BiPaNode>>();
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

		private int getShortestRowIndex()
		{
			int r = -1;
			double min = Double.MAX_VALUE;

			for (int i = 0; i < rows.size(); i++)
			{
				if (rowWidth.get(i) < min)
				{
					r = i;
					min = rowWidth.get(i);
				}
			}

			return r;
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
			else if (canAddHorizontal(node.getWidth()))
			{
				insertNodeToRow(node, getShortestRowIndex());
			}
			else
			{
				insertNodeToRow(node, rows.size());
			}
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

		private boolean canAddHorizontal(double extra)
		{
			int sri = getShortestRowIndex();

			if (sri < 0) return true;

			double min = rowWidth.get(sri);

			if (width - min >= extra + COMPLEX_MEM_HORIZONTAL_BUFFER)
			{
				return true;
			}

			return width < DESIRED_COMPLEX_MIN_WIDTH ||
				height + COMPLEX_MEM_VERTICAL_BUFFER + Actor.DEFAULT_HEIGHT >
					min + extra + COMPLEX_MEM_HORIZONTAL_BUFFER;
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

	private static final int DESIRED_COMPLEX_MIN_WIDTH = 100;
	private static final int COMPLEX_MEM_HORIZONTAL_BUFFER = 5;
	private static final int COMPLEX_MEM_VERTICAL_BUFFER = 5;
	private static final double COMPLEX_CHILD_GRAPH_BUFFER = 10;
	private static final double COMPLEX_LABEL_HEIGHT = 20;
	private static final double COMPLEX_MIN_WIDTH = COMPLEX_CHILD_GRAPH_BUFFER * 2;
}
