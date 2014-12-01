package org.gvt.action;

import org.biopax.paxtools.pattern.miner.SIFType;
import org.cbio.causality.analysis.Graph;
import org.cbio.causality.network.PathwayCommons;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.gvt.ChisioMain;
import org.gvt.gui.EnrichmentInSIFParameterDialog;
import org.gvt.model.basicsif.BasicSIFGraph;
import org.gvt.model.basicsif.BasicSIFNode;
import org.gvt.model.sifl3.SIFGraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * @author Ozgun Babur
 */
public class FindEnrichedInPCSifAction extends Action
{
	/**
	 * Main application.
	 */
	private ChisioMain main;

	/**
	 * Rule types to consider while reading SIF.
	 */
	private List<SIFType> selectedRuleTypes;

	private String genesfile;
	private String backgroundFile;
	private Double fdr;

	public FindEnrichedInPCSifAction(ChisioMain main)
	{
		super("Search Enriched Network ...");
		setToolTipText(getText());
		this.main = main;
		this.selectedRuleTypes = new ArrayList<SIFType>();
	}

	public FindEnrichedInPCSifAction(ChisioMain main, String genesfile, String backgroundFile,
		List<SIFType> selectedRuleTypes)
	{
		this(main);
		this.backgroundFile = backgroundFile;
		this.genesfile = genesfile;
		this.selectedRuleTypes = selectedRuleTypes;
	}

	public void run()
	{
		EnrichmentInSIFParameterDialog dialog = new EnrichmentInSIFParameterDialog(main.getShell(),
			SIFGraph.getPossibleRuleTypes(),
			selectedRuleTypes,
			genesfile,
			backgroundFile,
			fdr);

		if (dialog.open())
		{
			backgroundFile = dialog.getBackgroundfile();
			genesfile = dialog.getGenefile();
			fdr = dialog.getFdr();

			Graph g = PathwayCommons.getGraph(
				selectedRuleTypes.toArray(new SIFType[selectedRuleTypes.size()]));

			Set<String> bg = null;
			if (backgroundFile != null)
			{
				bg = readFile(backgroundFile);
			}

			Set<String> query = readFile(genesfile);

			Graph.NeighborType nType = Graph.NeighborType.values()[dialog.getNeighTypeIndex()];
			List<String> enriched = g.getEnrichedGenes(query, bg, fdr, nType, 1);

			HashSet<String> set = new HashSet<String>(enriched);
			query.retainAll(nType == Graph.NeighborType.UPSTREAM ? g.getUpstream(set) :
				nType == Graph.NeighborType.DOWNSTREAM ? g.getDownstream(set) : g.getNeighbors(set));

			Set<String> result = new HashSet<String>(enriched);
			result.addAll(query);
			g.crop(result);

			BasicSIFGraph graph = new BasicSIFGraph(g);

			if (graph.getNodes().isEmpty())
			{
				MessageDialog.openInformation(main.getShell(), "Graph empty!",
					"Loaded SIF file does not have any interaction of specified type.");
				return;
			}

			graph.setName("Enriched graph");
			graph.groupSimilarNodes();

			System.out.println("GOI has " + graph.getNodes().size() + " nodes and " +
				graph.getEdges().size() + " edges.");

			boolean cont = MessageDialog.openConfirm(main.getShell(), "Confirm",
				"GOI has " + graph.getNodes().size() + " nodes and " +
					graph.getEdges().size() + " edges. Layout?");

			if (cont)
			{
				main.createNewTab(graph);

				for (Object o : graph.getNodes())
				{
					if (o instanceof BasicSIFNode)
					{
						BasicSIFNode node = (BasicSIFNode) o;
						if (enriched.contains(node.getText()))
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

	private Set<String> readFile(String filename)
	{
		try
		{
			Set<String> genes = new HashSet<String>();
			Scanner sc = new Scanner(new File(filename));
			while (sc.hasNextLine())
			{
				String line = sc.nextLine();
				Collections.addAll(genes, line.split("\\s+"));
			}
			return genes;
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}