package org.gvt.action;

import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.pattern.miner.SIFType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.gvt.ChisioMain;
import org.gvt.gui.GOIofSIFParameterDialog;
import org.gvt.model.basicsif.BasicSIFGraph;
import org.gvt.model.basicsif.BasicSIFNode;
import org.gvt.model.sifl3.SIFGraph;
import org.gvt.util.SIFReader;
import org.patika.mada.algorithm.AlgoRunner;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;
import org.patika.mada.util.XRef;

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
public class GOIofSIFAction extends Action
{
	/**
	 * Main application.
	 */
	private ChisioMain main;

	/**
	 * Rule types to consider while reading SIF.
	 */
	private List<SIFType> selectedRuleTypes;

	private String siffile;
	private String genesfile;
	private Integer limit;
	private Boolean directed;

	public GOIofSIFAction(ChisioMain main)
	{
		super("Paths-Between Query on SIF File ...");
		setToolTipText(getText());
		this.main = main;
		this.selectedRuleTypes = new ArrayList<SIFType>();
		limit = 1;
		directed = false;
	}

	public GOIofSIFAction(ChisioMain main, String siffile, String genesfile,
		List<SIFType> selectedRuleTypes)
	{
		this(main);
		this.siffile = siffile;
		this.genesfile = genesfile;
		this.selectedRuleTypes = selectedRuleTypes;
	}

	public void run()
	{
		GOIofSIFParameterDialog dialog = new GOIofSIFParameterDialog(main.getShell(),
			SIFGraph.getPossibleRuleTypes(),
			selectedRuleTypes,
			siffile,
			genesfile,
			limit,
			directed);

		if (dialog.open())
		{
			siffile = dialog.getSiffile();
			genesfile = dialog.getGenefile();
			limit = dialog.getLimit();
			directed = dialog.getDirected();

			SIFReader sifReader = new SIFReader(selectedRuleTypes);
			BasicSIFGraph graph = (BasicSIFGraph) sifReader.readXMLFile(new File(siffile));

			if (graph.getNodes().isEmpty())
			{
				MessageDialog.openInformation(main.getShell(), "Graph empty!",
					"Loaded SIF file does not have any interaction of specified type.");
				return;
			}

			Set<XRef> refs = new HashSet<XRef>();

			// Read in xrefs
			try
			{
				BufferedReader reader = new BufferedReader(new FileReader(genesfile));

				String line;
				while ((line = reader.readLine()) != null)
				{
					if (line.startsWith("#"))
					{
						continue;
					}
					for (String ref : line.split(" "))
					{
						refs.add(new XRef("name" + XRef.SEPARATOR + ref));
					}
				}

				reader.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			if (refs.isEmpty())
			{
				MessageDialog.openInformation(main.getShell(), "No reference for genes-of-interest!",
					"Genes-of-interest file does not contain any references.");
				return;
			}

			Set<Node> seed = new HashSet<Node>();

			for (Object o : graph.getNodes())
			{
				Node node = (Node) o;

				for (XRef ref : node.getReferences())
				{
					if (refs.contains(ref))
					{
						seed.add(node);
						break;
					}
				}
			}

			if (seed.isEmpty())
			{
				MessageDialog.openInformation(main.getShell(), "No genes-of-interest!",
					"Loaded references do not match any genes in the loaded SIF file.");
				return;
			}
			else
			{
				System.out.println("Nmber of genes of interest = " + seed.size());
			}

			Collection<GraphObject> graphObjects =
				AlgoRunner.searchGraphOfInterest(graph, seed, limit, directed);

			Set<String> rdfseed = new HashSet<String>();
			for (Node node : seed)
			{
				rdfseed.add(((BasicSIFNode)node).getRdfid());
			}

			BasicSIFGraph goi = (BasicSIFGraph) graph.excise(graphObjects, true);
			goi.setName("Paths between nodes in SIF");

			System.out.println("GOI has " + goi.getNodes().size() + " nodes and " +
				goi.getEdges().size() + " edges.");

			boolean cont = MessageDialog.openConfirm(main.getShell(), "Confirm",
				"GOI has " + goi.getNodes().size() + " nodes and " +
					goi.getEdges().size() + " edges. Layout?");

			if (cont)
			{
				main.createNewTab(goi);

				for (Object o : goi.getNodes())
				{
					if (o instanceof BasicSIFNode)
					{
						BasicSIFNode node = (BasicSIFNode) o;
						if (rdfseed.contains(node.getRdfid()))
						{
							node.setHighlightColor(ChisioMain.higlightColor);
							node.setHighlight(true);
						}
					}
				}

				new CoSELayoutAction(main).run();
			}
		}
	}
}