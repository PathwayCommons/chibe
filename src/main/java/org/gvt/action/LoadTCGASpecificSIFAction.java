package org.gvt.action;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.biopax.paxtools.pattern.miner.SIFType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.gvt.ChisioMain;
import org.gvt.gui.ItemSelectionDialog;
import org.gvt.model.basicsif.BasicSIFGraph;
import org.gvt.model.basicsif.BasicSIFNode;
import org.gvt.util.*;
import org.patika.mada.algorithm.AlgoRunner;
import org.patika.mada.graph.Graph;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class LoadTCGASpecificSIFAction extends Action
{
	private static final String DEFAULT_PC_FILE_NAME = "PC.sif";
	private static final String BROAD_DIR = "broad-data/";

	/**
	 * Main application.
	 */
	private ChisioMain main;


	public LoadTCGASpecificSIFAction(ChisioMain main)
	{
		super("Load TCGA specific SIF ...");
		setToolTipText(getText());
		this.main = main;
	}

	public void run()
	{
		ItemSelectionDialog dialog = new ItemSelectionDialog(main.getShell(), 200,
			"Available studies", "Please select a study", getStudyCodes(),
			null, false, true, null);

		String study = (String) dialog.open();

		if (study == null) return;

		Set<String> genes = readGenes(study);

		if (genes.isEmpty())
		{
			MessageDialog.openInformation(main.getShell(), "No genes of interest",
				"Cannot find any genes of interest for the current study.");
			return;
		}

		BasicSIFGraph pcGraph = getPCGraph();

		Set<Node> seed = getSeed(pcGraph, genes);

		if (seed.isEmpty())
		{
			MessageDialog.openInformation(main.getShell(), "No genes of interest",
				"Loaded genes of interest do not intersect with genes in SIF data.");
			return;
		}

		Collection<GraphObject> graphObjects =
			AlgoRunner.searchGraphOfInterest(pcGraph, seed, 1, true);

		BasicSIFGraph goi = (BasicSIFGraph) pcGraph.excise(graphObjects, true);
		goi.setName(study);

		System.out.println("GOI has " + goi.getNodes().size() + " nodes and " +
			goi.getEdges().size() + " edges.");

		main.createNewTab(goi);
		new CoSELayoutAction(main).run();

		new FetchFromCBioPortalAction(main, study).run();
	}

	private Set<Node> getSeed(Graph graph, Set<String> symbols)
	{
		Set<Node> seed = new HashSet<Node>();
		for (Node node : graph.getNodes())
		{
			if (symbols.contains(node.getName())) seed.add(node);
		}
		return seed;
	}


	//--------------------- Getting PC SIF graph --------------------------------------------------|

	private BasicSIFGraph getPCGraph()
	{
		SIFReader sifReader = new SIFReader(Arrays.asList(SIFType.CONTROLS_STATE_CHANGE,
			SIFType.CONTROLS_EXPRESSION, SIFType.CONTROLS_DEGRADATION));

		File sifFile = new File(getPCSifFileLocation());

		if (!sifFile.exists())
		{
			downloadPCSIF(sifFile.getPath());
		}

		return (BasicSIFGraph) sifReader.readXMLFile(sifFile);
	}

	private String getPCSifFileLocation()
	{
		String s = Conf.get(Conf.PC_SIF_FILE);
		if (s.equals(Conf.DEFAULT))
		{
			return Conf.getPortalCacheDir() + DEFAULT_PC_FILE_NAME;
		}
		else
		{
			return s;
		}
	}

	private boolean downloadPCSIF(String saveLoc)
	{
		String url = Conf.get(Conf.PC_SIF_FILE_URL);
		return Download.downlaodTextFile(url, saveLoc);
	}

	//--------------------- Getting Mutsig and GISTIC genes ---------------------------------------|

	private List<String> getStudyCodes()
	{
		List<String> list = new ArrayList<String>(30);
		try
		{
			URL url = new URL(Conf.get(Conf.BROAD_DATA_URL) + "ingested_data.tsv");

			URLConnection con = url.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				if (line.isEmpty() || line.startsWith("#")
					|| line.startsWith("Tumor") || line.startsWith("Totals")) continue;

				String study = line.substring(0, line.indexOf("\t"));
				list.add(study);
			}
			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return list;
	}

	private String getBroadDateString()
	{
		String s = Conf.get(Conf.BROAD_DATA_URL);
		s = s.substring(s.indexOf("__") + 2, s.lastIndexOf("/"));
		s = s.replaceAll("_", "");
		return s;
	}

	private String getBroadDataURL(String study)
	{
		return Conf.get(Conf.BROAD_DATA_URL) + "data/" + study + "/" + getBroadDateString() + "/";
	}

	private String getBroadCacheDir()
	{
		String s = Conf.getPortalCacheDir() + BROAD_DIR;
		File f = new File(s);
		if (!f.exists()) f.mkdirs();
		return s;
	}

	private List<String> getBroadAnalysisFileNames(String study)
	{
		List<String> list = new ArrayList<String>(30);
		try
		{
			URL url = new URL(getBroadDataURL(study));

			URLConnection con = url.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				String start = "<li><a href=\"";
				if (line.startsWith(start))
				{
					String file = line.substring(start.length(), line.indexOf("\">"));
					list.add(file);
				}
			}
			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return list;
	}

	private String getGisticFileName(List<String> list)
	{
		for (String s : list) if (s.contains("Gistic2.Level_4")) return s;
		return null;
	}

	private String getMutsigFileName(List<String> list)
	{
		for (String s : list) if (s.contains("MutSigNozzleReportMerged.Level_4")) return s;
		return null;
	}

	private String getCachedGisticFileName(String study)
	{
		return getBroadCacheDir() + study + "_gistic.tar.gz";
	}

	private String getCachedMutsigFileName(String study)
	{
		return getBroadCacheDir() + study + "_mutsig.tar.gz";
	}

	private boolean downloadGistic(String study, List<String> analysisFileNames)
	{
		String s = getGisticFileName(analysisFileNames);
		return s != null &&
			Download.downloadAsIs(getBroadDataURL(study) + s, getCachedGisticFileName(study));
	}

	private boolean downloadMutsig(String study, List<String> analysisFileNames)
	{
		String s = getMutsigFileName(analysisFileNames);
		return s != null &&
			Download.downloadAsIs(getBroadDataURL(study) + s, getCachedMutsigFileName(study));
	}

	private Set<String> readGenes(String study)
	{
		Set<String> genes = new HashSet<String>();
		List<String> analysisFileNames = getBroadAnalysisFileNames(study);
		String file = getCachedMutsigFileName(study);
		if (!new File(file).exists())
		{
			downloadMutsig(study, analysisFileNames);
		}
		if (new File(file).exists())
		{
			genes.addAll(readGenesFromMutsig(file, 0.1));
		}

		if (genes.isEmpty())
		{
			file = getCachedGisticFileName(study);
			if (!new File(file).exists())
			{
				downloadGistic(study, analysisFileNames);
			}
			if (new File(file).exists())
			{
				genes.addAll(readGenesFromGistic(file));
			}
		}

		return genes;
	}

	private Set<String> readGenesFromGistic(String filename)
	{
		Set<String> set = new HashSet<String>();
		String s = FileUtil.readEntryContainingNameInTARGZFile(filename, "amp_genes");
		readGisticData(set, s);
		System.out.println("amp set = " + set.size());

		s = FileUtil.readEntryContainingNameInTARGZFile(filename, "del_genes");
		readGisticData(set, s);
		System.out.println("amp del set = " + set.size());
		return set;
	}

	private Set<String> readGenesFromMutsig(String filename, double qvalThr)
	{
		Set<String> set = new HashSet<String>();
		String s = FileUtil.readEntryContainingNameInTARGZFile(filename, "cosmic_sig_genes");
		if (s == null) s = FileUtil.readEntryContainingNameInTARGZFile(filename, "sig_genes");

		for (String line : s.split("\n"))
		{
			if (line.startsWith("rank")) continue;

			String[] token = line.split("\t");

			double qval = Double.parseDouble(token[token.length - 1]);

			if (qval < qvalThr)
			{
				String symbol = HGNCUtil.getSymbol(token[1]);
				if (symbol != null) set.add(symbol);
			}
		}

		System.out.println("mutsig set = " + set.size());
		return set;
	}

	private void readGisticData(Set<String> set, String s)
	{
		for (String token : s.split("\\s+"))
		{
			String symbol = HGNCUtil.getSymbol(token);
			if (symbol != null) set.add(symbol);
		}
	}
}