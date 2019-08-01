package org.gvt;

import org.biopax.paxtools.controller.Completer;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.miner.BlacklistGenerator;
import org.biopax.paxtools.pattern.miner.CommonIDFetcher;
import org.biopax.paxtools.pattern.miner.SIFEnum;
import org.biopax.paxtools.pattern.miner.SIFSearcher;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.cbio.causality.signednetwork.Generator;
import org.gvt.util.ID;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class ResourceFilePreparer
{
	public static final String OUTDIR = "/home/babur/Projects/chibe/";

	@Test
	public void prepare() throws IOException
	{
		String owlFile = "/home/babur/Documents/PC/PathwayCommons9.Detailed.BIOPAX.owl";
		SimpleIOHandler io = new SimpleIOHandler();
		Model model = io.convertFromOWL(new FileInputStream(owlFile));

		// For finding enriched reactions
		generateReactionToGeneFile(model);

		// For pathway enrichment analysis
		generatePathwayToGeneFile(model);

		// For SIF query support
//		Blacklist blacklist = generateLargeSIFGraph(model);
//		Blacklist blacklist = new Blacklist(OUTDIR + "blacklist.txt");

		// For causality graph support
//		Generator.generate(model, blacklist, OUTDIR + "SignedPC.sif");
	}

	private void generateReactionToGeneFile(Model model) throws IOException
	{
		Completer cpt = new Completer(SimpleEditorMap.L3);

		BufferedWriter writer = new BufferedWriter(new FileWriter(OUTDIR + "reaction2gene.txt"));

		boolean first = true;

		for (Conversion conv : model.getObjects(Conversion.class))
		{
			Set<BioPAXElement> set = new HashSet<BioPAXElement>();
			set.add(conv);

			set.addAll(conv.getControlledOf());

			set = cpt.complete(set, model);

			Set<String> symbols = new HashSet<String>();
			Set<Control> controls = new HashSet<Control>();
			for (BioPAXElement ele : set)
			{
				if (ele instanceof ProteinReference)
				{
					Set<String> symbolSet = getSymbols((ProteinReference) ele);
					if (symbolSet != null) symbols.addAll(symbolSet);
				}
				else if (ele instanceof Control)
				{
					controls.add((Control) ele);
				}
			}

			if (!symbols.isEmpty())
			{
				if (first) first = false;
				else writer.write("\n");

				writer.write(ID.get(conv));

				for (Control control : controls)
				{
					writer.write(" " + ID.get(control));
				}

				for (String symbol : symbols)
				{
					writer.write("\t" + symbol);
				}
			}

		}
		writer.close();
	}

	private void generatePathwayToGeneFile(Model model) throws IOException
	{
		Completer cpt = new Completer(SimpleEditorMap.L3);

		BufferedWriter writer = new BufferedWriter(new FileWriter(OUTDIR + "pathway2gene.txt"));

		boolean first = true;

		for (Pathway pathway : model.getObjects(Pathway.class))
		{
			Set<BioPAXElement> set = new HashSet<BioPAXElement>();
			set.add(pathway);

			set = cpt.complete(set, model);

			Set<String> symbols = new HashSet<String>();
			for (BioPAXElement ele : set)
			{
				if (ele instanceof ProteinReference)
				{
					Set<String> symbolSet = getSymbols((ProteinReference) ele);
					if (symbolSet != null) symbols.addAll(symbolSet);
				}
			}

			if (!symbols.isEmpty())
			{
				if (first) first = false;
				else writer.write("\n");

				writer.write(ID.get(pathway) + "\t" + pathway.getDisplayName());

				for (String symbol : symbols)
				{
					writer.write("\t" + symbol);
				}
			}

		}
		writer.close();
	}

	private Set<String> getSymbols(ProteinReference prot)
	{
		Set<String> set = new HashSet<String>();
		for (Xref xref : prot.getXref())
		{
			if (xref.getDb() != null && xref.getDb().equalsIgnoreCase("HGNC Symbol"))
			{
				set.add(xref.getId());
			}
		}
		return set;
	}

	private Blacklist generateLargeSIFGraph(Model model) throws IOException
	{
		BlacklistGenerator gen = new BlacklistGenerator();
		Blacklist blacklist = gen.generateBlacklist(model);
		blacklist.write(new FileOutputStream(OUTDIR + "blacklist.txt"));

		CommonIDFetcher idFetcher = new CommonIDFetcher();
		SIFSearcher s = new SIFSearcher(idFetcher, SIFEnum.values());
		s.setBlacklist(blacklist);
		s.searchSIF(model, new FileOutputStream(OUTDIR + "PC.sif"));
		return blacklist;
	}
}
