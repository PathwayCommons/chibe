package org.gvt.action;

import org.cbio.causality.data.portal.BroadAccessor;
import org.cbio.causality.model.Alteration;
import org.cbio.causality.model.AlterationPack;
import org.cbio.causality.model.Change;
import org.cbio.causality.util.Overlap;
import org.cbio.causality.util.Summary;
import org.eclipse.swt.custom.CTabItem;
import org.gvt.ChisioMain;
import org.gvt.gui.ItemSelectionDialog;
import org.gvt.gui.ItemSelectionRunnable;
import org.gvt.model.CompoundModel;
import org.gvt.model.biopaxl3.BioPAXL3Graph;
import org.patika.mada.graph.Edge;
import org.patika.mada.graph.Graph;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;

import java.util.*;

/**
 * @author Ozgun Babur
 */
public class ShowMutexGroupsAction extends TCGASIFAction
{
	public ShowMutexGroupsAction(ChisioMain main)
	{
		super("Show mutex groups in TCGA SIF ...", main);
	}

	@Override
	public void run()
	{
		if (!okToRun(main)) return;

		CTabItem tab = main.getSelectedTab();

		CompoundModel root = (CompoundModel) main.getTabToViewerMap().get(tab).getContents().
			getModel();

		BioPAXL3Graph graph = (BioPAXL3Graph) root;

		// Prepare data

		List<Set<String>> gisticSets = BroadAccessor.getGisticGeneSets(graph.getName(), 1);
		Set<String> genes = getNodeNames(graph);
		Set<Set<String>> empty = new HashSet<Set<String>>();
		for (Set<String> set : gisticSets)
		{
			set.retainAll(genes);
			if (set.isEmpty()) empty.add(set);
		}
		gisticSets.removeAll(empty);

		// Find mutex groups

		main.lockWithMessage("Calculating mutex ...");

		List<NodeGroup> list = getMutexGroups(graph, gisticSets, 0.05);

		main.unlock();

		// Select and highlight the group

		Set<GraphObject> gos = getHighlighted(graph);
		graph.removeHighlights();

		ItemSelectionDialog dialog = new ItemSelectionDialog(main.getShell(), 250,
			"Mutex groups", "Select mutex group to highlight", list, null, false, false,
			new ItemSelectionRunnable()
			{
				NodeGroup prev;

				@Override
				public void run(Collection selectedTerms)
				{
					if (prev != null) prev.highlight(false);

					if (selectedTerms.isEmpty())
					{
						prev = null;
						return;
					}

					Object o = selectedTerms.iterator().next();

					if (o.equals(ItemSelectionDialog.NONE))
					{
						prev = null;
						return;
					}

					NodeGroup group = (NodeGroup) o;

					group.highlight(true);
					prev = group;
				}
			});

		dialog.setUpdateUponSelection(true);
		dialog.open();

		graph.removeHighlights();
		highlight(gos);
	}

	private Set<String> getNodeNames(Graph graph)
	{
		Set<String> names = new HashSet<String>();
		for (Node node : graph.getNodes())
		{
			names.add(node.getName());
		}
		return names;
	}

	private AlterationPack getAltPack(Node node)
	{
		return ChisioMain.cBioPortalAccessor.getAlterations(node.getName());
	}

	private List<NodeGroup> getMutexGroups(Graph graph, List<Set<String>> gisticSets, double pvalThr)
	{
		List<NodeGroup> groups = new ArrayList<NodeGroup>();

		List<Node> nodes = new ArrayList<Node>(graph.getNodes());

		Iterator<Node> iter = nodes.iterator();
		while (iter.hasNext())
		{
			Node node = iter.next();
			if (getAltPack(node) == null)
			{
				iter.remove();
			}
		}

		Collections.sort(nodes, new Comparator<Node>()
		{
			@Override
			public int compare(Node o1, Node o2)
			{
				AlterationPack p1 = getAltPack(o1);
				AlterationPack p2 = getAltPack(o2);

				return new Integer(p2.getAlteredCount(Alteration.GENOMIC)).compareTo(
					p1.getAlteredCount(Alteration.GENOMIC));
			}
		});

		for (Node node : nodes)
		{
			NodeGroup group = new NodeGroup(node, gisticSets);

			for (int i = 0; i < 5; i++)
			{
				addNextBestGene(group, nodes);
			}

			while (group.size() > 1 && group.getWorstMutexPVal() > pvalThr)
			{
				group.removeLast();
			}

			if (group.size() > 1 && !groups.contains(group)) groups.add(group);
		}

		return groups;
	}

	private void addNextBestGene(NodeGroup group, List<Node> nodes)
	{
		Node best = null;
		double bestPval = 1;

		for (Node node : nodes)
		{
			if (!group.contains(node) && getAltPack(node) != null)
			{
				group.addNode(node);

				double pval = group.getMeanMutexPVal();

				if (pval < bestPval)
				{
					bestPval = pval;
					best = node;
				}

				group.removeLast();
			}
		}

		group.addNode(best);
	}

	class NodeGroup implements Comparable
	{
		List<Set<String>> closeGenes;
		LinkedList<Node> nodes;
		LinkedList<Change[]> alts;
		List<Edge> edges;
		Set<String> nodeNames;

		NodeGroup(Node node, List<Set<String>> closeGenes)
		{
			this.closeGenes = closeGenes;
			nodes = new LinkedList<Node>();
			nodes.add(node);
			nodeNames = new HashSet<String>();
			nodeNames.add(node.getName());
			alts = new LinkedList<Change[]>();
			alts.add(getAltPack(node).get(Alteration.GENOMIC));
		}

		void addNode(Node node)
		{
			assert edges == null;
			assert !nodes.contains(node);

			if (hasDNAProximity(nodeNames, node.getName()))
			{
				alts.add(getAltPack(node).get(Alteration.MUTATION));
			}
			else
			{
				alts.add(getAltPack(node).get(Alteration.GENOMIC));
			}

			nodes.add(node);
			nodeNames.add(node.getName());

		}

		private boolean hasDNAProximity(Set<String> current, String query)
		{
			for (Set<String> set : closeGenes)
			{
				if (set.contains(query))
				{
					for (String s : current)
					{
						if (set.contains(s)) return true;
					}
				}
			}
			return false;
		}

		void removeLast()
		{
			assert edges == null;

			Node node = nodes.removeLast();
			nodeNames.remove(node.getName());
			alts.removeLast();
		}

		double[] getMutexPvals()
		{
			double[] pval = new double[nodes.size()];

			Iterator<Node> nodeIter = nodes.iterator();
			Iterator<Change[]> altIter = alts.iterator();

			int i = 0;
			while(nodeIter.hasNext())
			{
				Node node = nodeIter.next();
				Change[] alt = altIter.next();
				Change[] others = uniteAltExcluding(node);

				pval[i++] = Overlap.calcMutexPval(alt, others);
			}
			return pval;
		}

		double getMeanMutexPVal()
		{
			return Summary.geometricMean(getMutexPvals());
		}

		double getWorstMutexPVal()
		{
			return Summary.max(getMutexPvals());
		}

		Change[] uniteAltExcluding(Node node)
		{
			assert size() > 1;

			if (size() == 2)
			{
				if (nodes.getFirst() == node) return alts.getLast();
				else return alts.getFirst();
			}

			Change[] ch = new Change[alts.getFirst().length];

			for (int i = 0; i < ch.length; i++)
			{
				ch[i] = Change.NO_CHANGE;

				Iterator<Node> nodeIter = nodes.iterator();
				Iterator<Change[]> altIter = alts.iterator();

				while(nodeIter.hasNext())
				{
					Node n = nodeIter.next();
					Change[] alt = altIter.next();

					if (n == node) continue;

					if (alt[i].isAltered())
					{
						ch[i] = Change.UNKNOWN_CHANGE;
						break;
					}
				}
			}
			return ch;
		}

		int size()
		{
			return nodes.size();
		}

		boolean contains(Node node)
		{
			return nodes.contains(node);
		}

		@Override
		public String toString()
		{
			String s = nodes.get(0).getName();
			for (int i = 1; i < nodes.size(); i++)
			{
				s += " - " + nodes.get(i).getName();
			}
			return s;
		}

		void highlight(boolean onoff)
		{
			if (edges == null) fillEdges();

			for (Node node : nodes)
			{
				node.setHighlight(onoff);
			}
			for (Edge edge : edges)
			{
				edge.setHighlight(onoff);
			}
		}

		private void fillEdges()
		{
			edges = new ArrayList<Edge>();
			for (Node node : nodes)
			{
				for (Edge e : node.getUpstream())
				{
					if (nodes.contains(e.getSourceNode()))
					{
						edges.add(e);
					}
				}
				for (Edge e : node.getDownstream())
				{
					if (nodes.contains(e.getTargetNode()))
					{
						edges.add(e);
					}
				}
			}
		}

		@Override
		public int compareTo(Object o)
		{
			if (o instanceof NodeGroup)
			{
				NodeGroup g = (NodeGroup) o;
				return new Double(getMeanMutexPVal()).compareTo(g.getMeanMutexPVal());
			}
			return 0;
		}

		@Override
		public int hashCode()
		{
			int h = 0;
			for (Node node : nodes)
			{
				h += node.hashCode();
			}
			return h;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof NodeGroup)
			{
				NodeGroup g = (NodeGroup) obj;
				return g.size() == size() && g.nodes.containsAll(nodes);
			}
			return false;
		}
	}
}
