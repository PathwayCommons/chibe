package org.gvt.action;

import org.cbio.causality.data.portal.BroadAccessor;
import org.cbio.causality.util.Download;
import org.gvt.ChisioMain;
import org.gvt.gui.ItemSelectionDialog;
import org.gvt.util.Conf;
import org.patika.mada.graph.Graph;
import org.patika.mada.graph.Node;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class LoadTCGASpecificReactionsAction extends LoadTCGASpecificSIFAction
{
	private static final String DEFAULT_REACTION_2_GENE_FILE_NAME = "reaction2gene.txt";

	public LoadTCGASpecificReactionsAction(ChisioMain main)
	{
		super("Retrieve hot reactions ...", main);
	}

	public void run()
	{
		if (!okToRun(main)) return;

		study = getCurrentStudy();

		assert study != null;

		Set<String> mutsig = BroadAccessor.getMutsigGenes(this.study, 1);

		List<Set<String>> gistic = BroadAccessor.getGisticGeneSets(this.study, 1);

		Graph graph = getGraph(main);

		Set<String> current = new HashSet<String>();
		for (Node node : graph.getNodes())
		{
			current.add(node.getName());
		}

		Set<Reaction> reactions = getReactions(current, mutsig, gistic);

		// sort reactions to sets

		final Map<String, Set<Reaction>> rSets = new HashMap<String, Set<Reaction>>();

		for (Reaction r : reactions)
		{
			if (!rSets.containsKey(r.getKey())) rSets.put(r.getKey(), new HashSet<Reaction>());
			rSets.get(r.getKey()).add(r);
		}

		// keep highest density reactions in each set

		for (String key : rSets.keySet())
		{
			double maxDensity = 0;
			for (Reaction r : rSets.get(key))
			{
				if (r.getDensity() > maxDensity)
					maxDensity = r.getDensity();
			}

			Iterator<Reaction> iter = rSets.get(key).iterator();
			while (iter.hasNext())
			{
				Reaction r = iter.next();
				if (r.getDensity() < maxDensity) iter.remove();
			}
		}

		List<String> keys = new ArrayList<String>(rSets.keySet());
		Collections.sort(keys);
		Collections.sort(keys, new Comparator<String>()
		{
			@Override
			public int compare(String key1, String key2)
			{
				return new Integer(rSets.get(key2).iterator().next().hitCnt).compareTo(
					rSets.get(key1).iterator().next().hitCnt);
			}
		});

		ArrayList<String> selected = new ArrayList<String>();

		ItemSelectionDialog dialog = new ItemSelectionDialog(main.getShell(), 400, "Hot reactions",
			"Plase select reaction to view", keys, selected, true, true, null);

		dialog.open();

		if (dialog.isCancelled() || selected.isEmpty()) return;

		Set<String> ids = new HashSet<String>();

		for (String key : selected)
		{
			for (Reaction r : rSets.get(key))
			{
				ids.addAll(r.ids);
			}
		}

		String title = "";
		for (String key : selected)
		{
			title += " [" + key + "]";
		}
		title = title.trim();

		QueryPCGetAction qa = new QueryPCGetAction(main, false);
		qa.setIDs(ids);
		qa.setNewPathwayName(title);
		qa.run();

		FetchFromCBioPortalAction fa = new FetchFromCBioPortalAction(
			main, FetchFromCBioPortalAction.CURRENT_STUDY);

		fa.run();
	}

	//--------------------- Getting Reactions--- --------------------------------------------------|

	private Set<Reaction> getReactions(Set<String> current, Set<String> mutSig,
		List<Set<String>> gistic)
	{
		File rFile = new File(getReactionFileLocation());

		if (!rFile.exists())
		{
			downloadReactionFile(rFile.getPath());
		}

		try
		{
			Set<Reaction> reactions = new HashSet<Reaction>();
			BufferedReader reader = new BufferedReader(new FileReader(rFile));

			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				String[] token = line.split("\t");
				Reaction r = new Reaction(token[0], Arrays.asList(token).subList(1, token.length),
					current);

				Set<Set> memory = new HashSet<Set>();

				for (String s : r.hit)
				{
					if (mutSig.contains(s)) r.hitCnt++;
					else
					{
						for (Set<String> set : gistic)
						{
							if (set.contains(s))
							{
								if (!memory.contains(set))
								{
									memory.add(set);
									r.hitCnt++;
								}
							}
						}
					}
				}

				if (r.hitCnt > 2) reactions.add(r);
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
		List<String> ids;
		List<String> genes;
		List<String> hit;
		int hitCnt;

		Reaction(String ids, List<String> genes, Set<String> current)
		{
			this.ids = Arrays.asList(ids.split(" "));
			id = this.ids.get(0);

			this.genes = genes;
			hit = new ArrayList<String>(genes);
			hit.retainAll(current);
			Collections.sort(hit);
			hitCnt = 0;
		}

		double getDensity()
		{
			return hit.size() / (double) genes.size();
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
			if (o instanceof Reaction)
			{
				Reaction r = (Reaction) o;
				return new Double(r.getDensity()).compareTo(getDensity());
			}
			return 0;
		}
	}

	static
	{
		String url = BroadAccessor.getBroadDataURL();
		if (url == null || !url.equals(Conf.get(Conf.BROAD_DATA_URL)))
			BroadAccessor.setBroadDataURL(Conf.get(Conf.BROAD_DATA_URL));
	}
}