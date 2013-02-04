package org.gvt.action;

import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.RelationshipXref;
import org.cbio.causality.data.CBioPortalAccessor;
import org.cbio.causality.data.CancerStudy;
import org.cbio.causality.data.CaseList;
import org.cbio.causality.data.GeneticProfile;
import org.cbio.causality.model.Alteration;
import org.cbio.causality.model.AlterationPack;
import org.cbio.causality.model.Change;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.gui.FetchFromCBioPortalDialog;
import org.gvt.model.BioPAXGraph;
import org.gvt.util.Conf;
import org.gvt.util.HGNCUtil;
import org.patika.mada.dataXML.*;
import org.patika.mada.util.CBioPortalAlterationData;
import org.patika.mada.util.ExperimentData;
import org.patika.mada.util.ExperimentDataManager;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FetchFromCBioPortalAction extends Action {
    ChisioMain main;

   	public FetchFromCBioPortalAction (ChisioMain main) {
        super("Fetch from cBio Portal...");
        setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class, "icon/cbio_portal.png"));
        this.main = main;
   	}

    public void run() {
        // First things first
        if(main.getRootGraph() == null) {
             MessageDialog.openError(main.getShell(), "Error!",
                     "No BioPAX model loaded.");
             return;
        }

        FetchFromCBioPortalDialog dialog = new FetchFromCBioPortalDialog(main);
        dialog.open();

        CBioPortalAccessor cBioPortalAccessor = dialog.getAccessor();

        // If user clicks on the 'Load' button, this list should not be empty
        // Otherwise, just quit
        List<GeneticProfile> currentGeneticProfiles = cBioPortalAccessor.getCurrentGeneticProfiles();
        if(currentGeneticProfiles.isEmpty()) {
            return;
        }

        // Extract gene names from the current BioPAX model
        List<String> geneNames = new ArrayList<String>();
        HashMap<String, String> geneNameToXrefStr = new HashMap<String, String>();
        Model model = main.getOwlModel();
        for (RelationshipXref xref : model.getObjects(RelationshipXref.class)) {
            if (xref.getDb().startsWith("HGNC")) {
                String[] tokens = xref.getId().split(":");
                // Is ist HGNC:GENE or HGNC:HGNC:123123
                String geneName =
                        (tokens.length > 1)
                                ? HGNCUtil.getSymbol(Integer.parseInt(tokens[1].trim()))
                                : tokens[0].trim();

                geneNames.add(geneName);
                geneNameToXrefStr.put(geneName, xref.getId());
            }
        }

        // Decide on a few things
        String dataName, dataDesc, fileNameSuggestion;
        if(currentGeneticProfiles.size() > 1) {
            dataName = "multiple data types";
            fileNameSuggestion = cBioPortalAccessor.getCurrentCancerStudy().getStudyId() + "_multi.ced";
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

        CancerStudy cancerStudy = cBioPortalAccessor.getCurrentCancerStudy();
        CaseList caseList = cBioPortalAccessor.getCurrentCaseList();

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
            AlterationPack alterations = cBioPortalAccessor.getAlterations(gene);
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
            cBioPortalAccessor.clearAlterationCache();

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
        graphs.add(main.getRootGraph());
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
}