package org.gvt.action;

import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.RelationshipXref;
import org.cbio.causality.data.portal.CBioPortalAccessor;
import org.cbio.causality.data.portal.CancerStudy;
import org.cbio.causality.data.portal.CaseList;
import org.cbio.causality.data.portal.GeneticProfile;
import org.cbio.causality.model.Alteration;
import org.cbio.causality.model.AlterationPack;
import org.cbio.causality.model.Change;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.CTabItem;
import org.gvt.ChisioMain;
import org.gvt.gui.FetchFromCBioPortalDialog;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;
import org.gvt.model.basicsif.BasicSIFGraph;
import org.gvt.util.Conf;
import org.gvt.util.HGNCUtil;
import org.patika.mada.dataXML.*;
import org.patika.mada.util.CBioPortalAlterationData;
import org.patika.mada.util.ExperimentData;
import org.patika.mada.util.ExperimentDataManager;
import sun.reflect.generics.tree.ReturnType;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FetchFromCBioPortalAction extends Action
{
	ChisioMain main;
	String study;

   	public FetchFromCBioPortalAction (ChisioMain main)
	{
		this(main, null);
	}

   	public FetchFromCBioPortalAction (ChisioMain main, String study)
	{
        super("Fetch from cBio Portal...");
        setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class, "icon/cbio_portal.png"));
        this.main = main;
		if (study != null) this.study = study.toLowerCase();
   	}

    public void run() {
        // First things first
        if(main.getRootGraph() == null && getBasicSIFGraph() == null) {
             MessageDialog.openError(main.getShell(), "Error!",
                     "No BioPAX model loaded.");
             return;
        }

		if (!initPortalAccessor()) return;

		if (study == null)
		{
			FetchFromCBioPortalDialog dialog = new FetchFromCBioPortalDialog(main);
			dialog.open();
		}
		else
		{
			if (!prepareAccessorForStudy(study)) return;
		}

        CBioPortalAccessor acc = ChisioMain.cBioPortalAccessor;

        // If user clicks on the 'Load' button, this list should not be empty
        // Otherwise, just quit
        List<GeneticProfile> currentGeneticProfiles = acc.getCurrentGeneticProfiles();
        if(currentGeneticProfiles.isEmpty()) {
            return;
        }

        // Extract gene names from the current BioPAX model
        List<String> geneNames = new ArrayList<String>();
        HashMap<String, String> geneNameToXrefStr = new HashMap<String, String>();
        Model model = main.getOwlModel();
		if (model != null)
		{
			for (RelationshipXref xref : model.getObjects(RelationshipXref.class)) {
				if (xref.getDb() != null && xref.getDb().startsWith("HGNC"))
				{
					String geneName = HGNCUtil.getSymbol(xref.getId());

					if (geneName != null && !geneNames.contains(geneName))
					{
						geneNames.add(geneName);
						geneNameToXrefStr.put(geneName, xref.getId());
					}
				}
			}
		}
		BasicSIFGraph bsgraph = getBasicSIFGraph();
		if (bsgraph != null)
		{
			for (Object o : bsgraph.getNodes())
			{
				NodeModel node = ((NodeModel) o);
				String text = node.getText();
				String symbol = HGNCUtil.getSymbol(text);

				if (symbol != null)
				{
					geneNames.add(symbol);
					geneNameToXrefStr.put(text, symbol);
				}
			}
		}


        // Decide on a few things
        String dataName, dataDesc, fileNameSuggestion;
        if(currentGeneticProfiles.size() > 1) {
            dataName = "multiple data types";
            fileNameSuggestion = acc.getCurrentCancerStudy().getStudyId() + "_multi.ced";
            dataDesc = "";
            for (GeneticProfile currentGeneticProfile : currentGeneticProfiles) {
                dataDesc += currentGeneticProfile.getName() + " | " + currentGeneticProfile.getDescription() + "\n";
            }
        } else {
            GeneticProfile geneticProfile = currentGeneticProfiles.iterator().next();
            dataName = geneticProfile.getName();
            fileNameSuggestion = geneticProfile.getId() + ".ced";
            dataDesc = geneticProfile.getDescription();
        }

        ObjectFactory expFactory = new ObjectFactory();

        CancerStudy cancerStudy = acc.getCurrentCancerStudy();
        CaseList caseList = acc.getCurrentCaseList();

        // Now load data
        main.lockWithMessage("Loading " + dataName + "...");

        ChisioExperimentData experimentData;
        try {
            experimentData = expFactory.createRootExperimentData();
        } catch (JAXBException e) {
            MessageDialog.openError(main.getShell(), "Error!",
                    "Could not create experiment.");
            return;
        }

        String alterationDataType = ExperimentData.CBIOPORTAL_ALTERATION_DATA;

        experimentData.setExperimentType(alterationDataType);
        String experimentInfo = cancerStudy.getName() + " | "
                + caseList.getDescription() + " (" + caseList.getCases().length + " cases) \n"
                + dataName + "\n"
                + dataDesc;
        experimentData.setExperimentSetInfo(experimentInfo);

        int count = 0;
         // Create sub-experiments for each sample
         for (String caseId : caseList.getCases()) {
             try {
                 Experiment experiment = expFactory.createExperiment();
                 experiment.setNo(count++);
                 experiment.setExperimentName(caseId);
                 experiment.setExperimentInfo("Cancer Study: " + cancerStudy.getName() + "\nCase: " + caseId);

                 experimentData.getExperiment().add(experiment);
             } catch (JAXBException e) {
                 MessageDialog.openError(main.getShell(), "Error!",
                         "Could not create experiment.");
                 return;
             }
         }

        // Iterate over genes
        // TODO: optimize this and grab all results with single request.
        for (String gene : geneNames) {
            AlterationPack alterations = acc.getAlterations(gene);

			if (alterations == null) continue;

            try {
                Row row = expFactory.createRow();
                Reference ref = expFactory.createReference();
                ref.setDb("HGNC");
                ref.setValue(geneNameToXrefStr.get(gene));
                row.getRef().add(ref);

                count = 0;
                for (Change change : alterations.get(Alteration.ANY)) {
                    // TODO: Special value for NO_DATA?
                    double expValue =
                            (change.isAbsent() || !change.isAltered())
                                    ? CBioPortalAlterationData.VALUES.NOT_ALTERED.toDouble()
                                    : CBioPortalAlterationData.VALUES.ALTERED.toDouble();

                    ValueTuple tuple = expFactory.createValueTuple();
                    tuple.setNo(count++);
                    tuple.setValue(expValue);
                    row.getValue().add(tuple);
                }

                experimentData.getRow().add(row);
            } catch (JAXBException e) {
                MessageDialog.openError(main.getShell(), "Error!",
                        "Could not process experiment.");
                return;
            }
        }

        // Advanced setting in order to save some memory for huge graphs
        if(!Conf.getBoolean(Conf.CBIOPORTAL_USE_CACHE))
            acc.clearAlterationCache();

        // Let's try to adjust the settings
        main.setExperimentData(experimentData, fileNameSuggestion);
        ExperimentDataManager dataManager = main.getExperimentDataManager(alterationDataType);
        dataManager.setData(experimentData);
        dataManager.getSecondExpIndices().clear();
        dataManager.getFirstExpIndices().clear();

		for(int i=0; i < caseList.getCases().length; i++)
            dataManager.getFirstExpIndices().add(i);

		dataManager.setAveraging(ExperimentDataManager.MEAN);

        // And apply the coloring
        List<BioPAXGraph> graphs = main.getAllPathwayGraphs();
        if (main.getRootGraph() != null) graphs.add(main.getRootGraph());

        for (BioPAXGraph graph : graphs) {
            dataManager.clearExperimentData(graph);
            dataManager.associateExperimentData(graph);
            if (graph.getLastAppliedColoring() != null) {
                graph.setLastAppliedColoring(null);
                new ColorWithExperimentAction(main, graph, dataManager.getType()).run();
            }
        }
        BioPAXGraph currentGraph = main.getPathwayGraph();
        if (currentGraph != null && currentGraph.getLastAppliedColoring() == null) {
            new ColorWithExperimentAction(main, currentGraph, dataManager.getType()).run();
        }

        // All done, let's quit

        main.unlock();
    }

	private BasicSIFGraph getBasicSIFGraph()
	{
		CTabItem tab = main.getSelectedTab();

		if (tab != null)
		{
			CompoundModel root = (CompoundModel) main.getTabToViewerMap().get(tab).getContents().
				getModel();

			if (root instanceof BasicSIFGraph)
			{
				return (BasicSIFGraph) root;
			}
		}
		return null;
	}

	private boolean initPortalAccessor()
	{
		if (ChisioMain.cBioPortalAccessor == null)
		{
			try
			{
				CBioPortalAccessor.setPortalURL(Conf.get(Conf.CBIOPORTAL_URL));
				ChisioMain.cBioPortalAccessor = new CBioPortalAccessor();
				CBioPortalAccessor.setCacheDir(Conf.getPortalCacheDir());
				return true;
			}
			catch (IOException e)
			{
				MessageDialog.openError(main.getShell(),"Error!",
					"Could not access to cBio Portal.\n" + e.toString());

				return false;
			}
		}
		else return true;
	}

	private boolean prepareAccessorForStudy(String study)
	{
		CBioPortalAccessor acc = ChisioMain.cBioPortalAccessor;

		List<CancerStudy> cancerStudies = acc.getCancerStudies();
		for (CancerStudy cancerStudy : cancerStudies)
		{
			if (cancerStudy.getStudyId().equals(study + "_tcga"))
			{
				acc.setCurrentCancerStudy(cancerStudy);

				try
				{
					for (CaseList caseList : acc.getCaseListsForCurrentStudy())
					{
						if (caseList.getId().contains("cna") && caseList.getId().contains("seq"))
						{
							acc.setCurrentCaseList(caseList);
						}
					}

					if (acc.getCurrentCaseList() == null)
					{
						System.err.println("Cannot find case list.");
						return false;
					}

					ArrayList<GeneticProfile> plist = new ArrayList<GeneticProfile>();
					for (GeneticProfile geneticProfile : acc.getGeneticProfilesForCurrentStudy())
					{
						if (geneticProfile.getId().contains("gistic") ||
							geneticProfile.getId().contains("mutation"))
						{
							plist.add(geneticProfile);
						}
					}

					if (plist.isEmpty())
					{
						System.err.println("Cannot find any profile");
						return false;
					}

					acc.setCurrentGeneticProfiles(plist);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}

			if (acc.getCurrentCancerStudy() == null)
			{
				System.err.println("Cannot find the TCGA study.");
				return false;
			}
		}
		return true;
	}
}