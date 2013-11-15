package org.gvt.action;

import org.apache.commons.io.IOUtils;
import org.biopax.paxtools.pattern.miner.SIFType;
import org.cbio.causality.data.portal.BroadAccessor;
import org.cbio.causality.data.portal.CBioPortalAccessor;
import org.cbio.causality.data.portal.CancerStudy;
import org.cbio.causality.data.portal.GeneticProfile;
import org.cbio.causality.model.AlterationPack;
import org.cbio.causality.util.Download;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Color;
import org.gvt.ChisioMain;
import org.gvt.editpart.ChsScalableRootEditPart;
import org.gvt.figure.HighlightLayer;
import org.gvt.gui.ItemSelectionDialog;
import org.gvt.model.NodeModel;
import org.gvt.model.basicsif.BasicSIFGraph;
import org.gvt.util.Conf;
import org.gvt.util.SIFReader;
import org.patika.mada.algorithm.AlgoRunner;
import org.patika.mada.graph.Edge;
import org.patika.mada.graph.Graph;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.graph.Node;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class LoadTCGASpecificSIFAction extends TCGASIFAction
{
	protected String caseID;
	protected String study;
	protected boolean newView;

	public LoadTCGASpecificSIFAction(ChisioMain main)
	{
		this("Load TCGA specific SIF ...", main);
	}

	protected LoadTCGASpecificSIFAction(String text, ChisioMain main)
	{
		super(text, main);
		this.newView = true;
	}

	public void setCaseID(String caseID)
	{
		this.caseID = caseID;
	}

	public void setStudy(String study)
	{
		this.study = study;
	}

	public void setNewView(boolean newView)
	{
		this.newView = newView;
	}

	public void run()
	{
		try
		{
			if (study == null)
			{


				ItemSelectionDialog dialog = new ItemSelectionDialog(main.getShell(), 500,
					"Available studies", "Please select a study",
					prepareStudyList(BroadAccessor.getStudyCodes()), null, false, true, null);

				study = (String) dialog.open();
				if (study != null) study = study.substring(0, study.indexOf(" "));
			}

			if (study == null) return;

			Set<String> genes = BroadAccessor.getMutsigGenes(study, 0.1);

			if (genes.isEmpty())
			{
				MessageDialog.openInformation(main.getShell(), "No genes of interest",
					"Cannot find any genes of interest for the current study.");
				return;
			}

			BasicSIFGraph pcGraph = QueryPCAction.getPCGraph();

			Set<Node> seed = QueryPCAction.getSeed(pcGraph, genes);

			Set<String> gisticGenes = BroadAccessor.getGisticGenes(study, 0.01);

			gisticGenes = getNeighborsOfFirstAlsoInTheSecondSet(genes, gisticGenes, seed);
			keepOverValue(study, gisticGenes, genes, 0.05);
			genes.addAll(gisticGenes);
			seed = QueryPCAction.getSeed(pcGraph, genes);

			Set<String> caseGenes = null;
			Set<String> caseOnly = null;
			if (caseID != null)
			{
				caseGenes = getAlteredGenesForTheCase(caseID);
				caseGenes = getNeighborsOfFirstAlsoInTheSecondSet(genes, caseGenes, seed);
				caseOnly = new HashSet<String>(caseGenes);
				caseOnly.removeAll(genes);
				genes.addAll(caseOnly);
				seed = QueryPCAction.getSeed(pcGraph, genes);

				System.out.println("caseOnly.size() = " + caseOnly.size());
				System.out.println("caseGenes = " + caseGenes.size());
			}

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
			goi.setAsRoot();
			System.out.println("GOI has " + goi.getNodes().size() + " nodes and " +
				goi.getEdges().size() + " edges.");

			if (newView) main.createNewTab(goi);
			else
			{
				// Reset highlight
				HighlightLayer hLayer = (HighlightLayer)
					((ChsScalableRootEditPart) main.getViewer().getRootEditPart()).getLayer(
						HighlightLayer.HIGHLIGHT_LAYER);

				hLayer.removeAll();
				hLayer.highlighted.clear();

				main.getViewer().deselectAll();

				main.getViewer().setContents(goi);
			}

			new CoSELayoutAction(main).run();

			new FetchFromCBioPortalAction(main, study.toLowerCase() + "_tcga").run();

			// Highlight case specific alterations

			if (caseGenes != null && !caseGenes.isEmpty())
			{
				Color color = new Color(null,180, 255, 180);

				for (Object o : goi.getNodes())
				{
					NodeModel node = (NodeModel) o;
					if (caseGenes.contains(node.getText()))
					{
						if (caseOnly.contains(node.getText()))
							node.setHighlightColor(color);
						node.setHighlight(true);
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			study = null;
		}
	}

	//--------------------- Graph operations ------------------------------------------------------|

	private Set<String> getNeighborsOfFirstAlsoInTheSecondSet(Set<String> first, Set<String> second,
		Set<Node> nodes)
	{
		Set<String> neigh = new HashSet<String>();
		for (Node node : nodes)
		{
			for (Edge e : node.getUpstream())
			{
				Node n = e.getSourceNode();
				neigh.add(n.getName());
			}
			for (Edge e : node.getDownstream())
			{
				Node n = e.getTargetNode();
				neigh.add(n.getName());
			}
		}
		neigh.retainAll(second);
		return neigh;
	}

	//--------------------- Portal operations -----------------------------------------------------|

	private void keepOverValue(String study, Set<String> gistic, Set<String> dontTouchThis,
		double altThr)
	{
		CBioPortalAccessor acc = getPortalAccessor();
		acc.configureForStudy(study.toLowerCase() + "_tcga");
		Set<String> keep = new HashSet<String>(dontTouchThis);

		for (String s : gistic)
		{
			AlterationPack alts = acc.getAlterations(s);
			if (alts != null && alts.getAlteredRatio() >= altThr) keep.add(s);
		}

		gistic.retainAll(keep);
	}

	private CBioPortalAccessor getPortalAccessor()
	{
		if (ChisioMain.cBioPortalAccessor == null)
		{
			try
			{
				CBioPortalAccessor.setCacheDir(Conf.getPortalCacheDir());
				ChisioMain.cBioPortalAccessor = new CBioPortalAccessor();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return ChisioMain.cBioPortalAccessor;
	}

	private Set<String> getAlteredGenesForTheCase(String caseID)
	{
		CBioPortalAccessor acc = getPortalAccessor();
		String mutID = null;
		String cnaID = null;
		for (GeneticProfile gp : acc.getCurrentGeneticProfiles())
		{
			if (gp.getId().contains("gistic")) cnaID = gp.getId();
			else if (gp.getId().contains("mutation")) mutID = gp.getId();
		}

		Set<String> set = new HashSet<String>();

			if (mutID != null)
			{
				String url = "http://www.cbioportal.org/public-portal/mutations.json?case_id=" +
					caseID + "&mutation_profile=" + mutID;

				harvestAlteredCaseJSON(url, set);
			}
			if (mutID != null)
			{
				String url = "http://www.cbioportal.org/public-portal/cna.json?case_id=" +
					caseID + "&cna_profile=" + cnaID;

				harvestAlteredCaseJSON(url, set);
			}

		return set;
	}

	private void harvestAlteredCaseJSON(String url, Set<String> set)
	{
		try
		{

			String content = IOUtils.toString(new URL(url), "UTF-8");
			int start = content.indexOf("\"gene\":[\"") + 9;
			int end = content.indexOf("\"]", start);
			content = content.substring(start, end);
			String[] token = content.split("\",\"");
			set.addAll(Arrays.asList(token));
//			for (String gene : token)
//			{
//				String symbol = HGNC.getSymbol(gene);
//				if (symbol != null) set.add(symbol);
//			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private List<String> prepareStudyList(List<String> studyCodes)
	{
		List<String> list = new ArrayList<String>(studyCodes.size());
		try
		{
			CBioPortalAccessor acc = new CBioPortalAccessor();
			for (String code : studyCodes)
			{
				code = code.toLowerCase();
				for (CancerStudy cancerStudy : acc.getCancerStudies())
				{
					if (cancerStudy.getStudyId().equals(code + "_tcga"))
					{
						list.add(code.toUpperCase() + " - " + cancerStudy.getName());
						break;
					}
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return list;
	}

	static
	{
		CBioPortalAccessor.setCacheDir(Conf.getPortalCacheDir());
		BroadAccessor.setBroadDataURL(Conf.get(Conf.BROAD_DATA_URL));
	}
}