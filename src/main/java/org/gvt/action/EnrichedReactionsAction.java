package org.gvt.action;

import org.cbio.causality.data.portal.BroadAccessor;
import org.cbio.causality.util.Download;
import org.cbio.causality.util.FDR;
import org.cbio.causality.util.FishersExactTest;
import org.cbio.causality.util.FormatUtil;
import org.gvt.ChisioMain;
import org.gvt.gui.EnrichedReactionsDialog;
import org.gvt.gui.ItemSelectionDialog;
import org.gvt.util.Conf;
import org.patika.mada.graph.Graph;
import org.patika.mada.graph.Node;
import org.patika.mada.util.XRef;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Finds Reactions in PC2 that are enriched by the user selected genes.
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class EnrichedReactionsAction extends LoadTCGASpecificSIFAction
{
	private static final String DEFAULT_REACTION_2_GENE_FILE_NAME = "reaction2gene.txt";

	public EnrichedReactionsAction(ChisioMain main)
	{
		super("Find Enriched Reactions ...", main);
	}

	public void run()
	{
		EnrichedReactionsDialog erd = new EnrichedReactionsDialog(main.getShell());
		List<String> query = erd.open();

		if (query.isEmpty()) return;

		Map<String, Reaction> reactions = getReactions(query);

		// assign p-values

		Set<String> allSymbols = new HashSet<>();
		for (Reaction reaction : reactions.values())
		{
			allSymbols.addAll(reaction.genes);
		}
		int allSize = allSymbols.size();
		int querySize = query.size();
		Map<String, Double> pvals = new HashMap<>();
		for (Reaction reaction : reactions.values())
		{
			reaction.assignPval(allSize, querySize);
			pvals.put(reaction.id, reaction.pval);
		}

		Map<String, Double> qVals = FDR.getQVals(pvals, null);

		for (String key : qVals.keySet())
		{
			reactions.get(key).qval = FormatUtil.roundToSignificantDigits(qVals.get(key), 1);
		}

		// group reactions

		Map<String, List<Reaction>> groups = new HashMap<>();
		for (Reaction reac : reactions.values())
		{
			if (reac.hit.isEmpty()) continue;

			String key = reac.getKey();
			if (!groups.containsKey(key)) groups.put(key, new ArrayList<Reaction>());
			groups.get(key).add(reac);
		}
		for (List<Reaction> list : groups.values())
		{
			Collections.sort(list);
		}

		// add representing reactions to selection list

		List<SelectionItem> items = new ArrayList<>();
		for (List<Reaction> list : groups.values())
		{
			items.add(new SelectionItem(list.get(0)));
		}

		Collections.sort(items);
		items = items.subList(0, Math.min(100, items.size()));

		ArrayList<SelectionItem> selected = new ArrayList<SelectionItem>();

		ItemSelectionDialog dialog = new ItemSelectionDialog(main.getShell(), 400, "Hot reactions",
			"Plase select reaction to view", items, selected, true, true, null);

		dialog.open();

		if (dialog.isCancelled() || selected.isEmpty()) return;

		Set<String> ids = new HashSet<String>();
		Set<String> genes = new HashSet<>();

		for (SelectionItem item : selected)
		{
			for (Reaction reac : groups.get(item.reac.getKey()))
			{
				Collections.addAll(ids, reac.ids);
				genes.addAll(reac.hit);
			}
		}

		String title = "Enriched reactions";

		QueryPCGetAction qa = new QueryPCGetAction(main, false);
		qa.setIDs(ids);
		qa.setNewPathwayName(title);
		qa.run();

		Set<XRef> xrefs = new HashSet<>();
		for (String gene : genes)
		{
			xrefs.add(new XRef("Name", gene));
		}

		HighlightWithRefAction ha = new HighlightWithRefAction(main, main.getPathwayGraph(), xrefs);
		ha.run();

		FetchFromCBioPortalAction fa = new FetchFromCBioPortalAction(
			main, FetchFromCBioPortalAction.CURRENT_STUDY);

		fa.run();
	}

	//--------------------- Getting Reactions--- --------------------------------------------------|

	private Map<String, Reaction> getReactions(Collection<String> query)
	{
		File rFile = new File(getReactionFileLocation());

		if (!rFile.exists())
		{
			downloadReactionFile(rFile.getPath());
		}

		try
		{
			Map<String, Reaction> reactions = new HashMap<>();
			BufferedReader reader = new BufferedReader(new FileReader(rFile));

			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				String[] token = line.split("\t");
				Reaction r = new Reaction(token[0], Arrays.asList(token).subList(1, token.length),
					query);

				reactions.put(r.id, r);

			}
			reader.close();
			return reactions;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private String getReactionFileLocation()
	{
		String s = Conf.get(Conf.REACTION_2_GENE_FILE);
		if (s.equals(Conf.DEFAULT))
		{
			return Conf.getPortalCacheDir() + DEFAULT_REACTION_2_GENE_FILE_NAME;
		}
		else
		{
			return s;
		}
	}

	private boolean downloadReactionFile(String saveLoc)
	{
		String url = Conf.get(Conf.REACTION_2_GENE_FILE_URL);
		return Download.downlaodTextFile(url, saveLoc);
	}


	class Reaction implements Comparable
	{
		String id;

		// with controls
		String[] ids;

		List<String> genes;
		List<String> hit;
		double pval;
		double qval;

		Reaction(String id, List<String> genes, Collection<String> query)
		{
			this.ids = id.split(" ");
			this.id = ids[0];
			this.genes = genes;
			hit = new ArrayList<String>(genes);
			hit.retainAll(query);
			Collections.sort(hit);
		}

		String getKey()
		{
			String k = hit.get(0);
			int i = 0;
			for (String s : hit)
			{
				if (i++ == 0) continue;
				k += " - " + s;
			}
			return k;
		}

		@Override
		public int compareTo(Object o)
		{
			if (!(o instanceof Reaction)) return 0;
			return new Double(qval).compareTo(((Reaction) o).qval);
		}

		public void assignPval(int totalSymbolSize, int querySize)
		{
			this.pval = FishersExactTest.calcEnrichmentPval(totalSymbolSize, querySize,
				genes.size(), hit.size());
		}
	}

	class SelectionItem implements Comparable
	{
		Reaction reac;

		SelectionItem(Reaction reac)
		{
			this.reac = reac;
		}

		@Override
		public String toString()
		{
			return reac.getKey() + " [" + reac.qval + "]";
		}

		@Override
		public int compareTo(Object o)
		{
			if (!(o instanceof SelectionItem)) return 0;

			return new Double(reac.qval).compareTo(((SelectionItem) o).reac.qval);
		}
	}
}