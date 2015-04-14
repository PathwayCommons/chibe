package org.gvt.action;

import org.cbio.causality.util.Download;
import org.cbio.causality.util.FDR;
import org.cbio.causality.util.FishersExactTest;
import org.cbio.causality.util.FormatUtil;
import org.gvt.ChisioMain;
import org.gvt.gui.GeneSetSelectionDialog;
import org.gvt.gui.ItemSelectionDialog;
import org.gvt.model.BioPAXGraph;
import org.gvt.util.Conf;
import org.patika.mada.util.XRef;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Finds Pathways in PC2 that are enriched by the user selected genes.
 * @author Ozgun Babur
 */
public class EnrichedPathwaysAction extends LoadTCGASpecificSIFAction
{
	private static final String DEFAULT_PATHWAY_2_GENE_FILE_NAME = "pathway2gene.txt";

	public EnrichedPathwaysAction(ChisioMain main)
	{
		super("Find Enriched Pathways ...", main);
	}

	public void run()
	{
		GeneSetSelectionDialog gssd = new GeneSetSelectionDialog(main.getShell());
		List<String> query = gssd.open();

		if (query == null || query.isEmpty()) return;

		Map<String, Pathway> pathways = getPathways(query);

		// assign p-values

		Set<String> allSymbols = new HashSet<String>();
		for (Pathway pathway : pathways.values())
		{
			allSymbols.addAll(pathway.genes);
		}
		int allSize = allSymbols.size();
		int querySize = query.size();
		Map<String, Double> pvals = new HashMap<String, Double>();
		for (Pathway pathway : pathways.values())
		{
			pathway.assignPval(allSize, querySize);
			pvals.put(pathway.id, pathway.pval);
		}

		Map<String, Double> qVals = FDR.getQVals(pvals, null);

		for (String key : qVals.keySet())
		{
			pathways.get(key).qval = FormatUtil.roundToSignificantDigits(qVals.get(key), 1);
		}

		// group pathways

		Map<String, List<Pathway>> groups = new HashMap<String, List<Pathway>>();
		for (Pathway reac : pathways.values())
		{
			if (reac.hit.isEmpty()) continue;

			String key = reac.getKey();
			if (!groups.containsKey(key)) groups.put(key, new ArrayList<Pathway>());
			groups.get(key).add(reac);
		}
		for (List<Pathway> list : groups.values())
		{
			Collections.sort(list);
		}

		// add representing pathways to selection list

		List<SelectionItem> items = new ArrayList<SelectionItem>();
		for (List<Pathway> list : groups.values())
		{
			items.add(new SelectionItem(list.get(0)));
		}

		Collections.sort(items);
		items = items.subList(0, Math.min(100, items.size()));

		ArrayList<SelectionItem> selected = new ArrayList<SelectionItem>();

		ItemSelectionDialog dialog = new ItemSelectionDialog(main.getShell(), 400, "Enriched pathways",
			"Plase select pathway to view", items, selected, true, true, null);

		dialog.open();

		if (dialog.isCancelled() || selected.isEmpty()) return;

		Set<String> ids = new HashSet<String>();
		Set<String> genes = new HashSet<String>();

		for (SelectionItem item : selected)
		{
			for (Pathway reac : groups.get(item.pathway.getKey()))
			{
				Collections.addAll(ids, reac.ids);
				genes.addAll(reac.hit);
			}
		}

		QueryPCGetAction qa = new QueryPCGetAction(main, false, QueryPCAction.QueryLocation.PC_MECH);
		qa.setIDs(ids);
		qa.run();

		Set<XRef> xrefs = new HashSet<XRef>();
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

	private Map<String, Pathway> getPathways(Collection<String> query)
	{
		File rFile = new File(getPathwayFileLocation());

		if (!rFile.exists())
		{
			downloadPathwayFile(rFile.getPath());
		}

		try
		{
			Map<String, Pathway> pathways = new HashMap<String, Pathway>();
			BufferedReader reader = new BufferedReader(new FileReader(rFile));

			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				String[] token = line.split("\t");
				Pathway p = new Pathway(token[0], token[1], Arrays.asList(token).subList(2, token.length),
					query);

				if (p.genes.size() > 1) pathways.put(p.id, p);
			}
			reader.close();
			return pathways;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private String getPathwayFileLocation()
	{
		String s = Conf.get(Conf.PATHWAY_2_GENE_FILE);
		if (s.equals(Conf.DEFAULT))
		{
			return Conf.getPortalCacheDir() + DEFAULT_PATHWAY_2_GENE_FILE_NAME;
		}
		else
		{
			return s;
		}
	}

	private boolean downloadPathwayFile(String saveLoc)
	{
		String url = Conf.get(Conf.PATHWAY_2_GENE_FILE_URL);
		return Download.downlaodTextFile(url, saveLoc);
	}


	class Pathway implements Comparable
	{
		String id;
		String name;

		// with controls
		String[] ids;

		List<String> genes;
		List<String> hit;
		double pval;
		double qval;

		Pathway(String id, String name, List<String> genes, Collection<String> query)
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
			if (!(o instanceof Pathway)) return 0;
			return new Double(qval).compareTo(((Pathway) o).qval);
		}

		public void assignPval(int totalSymbolSize, int querySize)
		{
			this.pval = FishersExactTest.calcEnrichmentPval(totalSymbolSize, querySize,
				genes.size(), hit.size());
		}
	}

	class SelectionItem implements Comparable
	{
		Pathway pathway;

		SelectionItem(Pathway pathway)
		{
			this.pathway = pathway;
		}

		@Override
		public String toString()
		{
			return pathway.getKey() + " [" + FormatUtil.roundToSignificantDigits(pathway.pval, 2) + "]";
		}

		@Override
		public int compareTo(Object o)
		{
			if (!(o instanceof SelectionItem)) return 0;

			return new Double(pathway.qval).compareTo(((SelectionItem) o).pathway.qval);
		}
	}
}