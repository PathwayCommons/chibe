package org.gvt.action;

import org.biopax.paxtools.causality.data.CBioPortalAccessor;
import org.biopax.paxtools.causality.data.CancerStudy;
import org.biopax.paxtools.causality.data.CaseList;
import org.biopax.paxtools.causality.data.GeneticProfile;
import org.biopax.paxtools.causality.model.AlterationPack;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.RelationshipXref;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.gvt.ChisioMain;
import org.gvt.gui.FetchFromCBioPortalDialog;
import org.gvt.util.HGNCUtil;
import org.patika.mada.dataXML.ChisioExperimentData;
import org.patika.mada.dataXML.ObjectFactory;
import org.patika.mada.dataXML.RootExperimentData;
import org.patika.mada.gui.FetchFromGEODialog;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FetchFromCBioPortalAction extends Action {
    ChisioMain main;

   	public FetchFromCBioPortalAction (ChisioMain main) {
   		super("Fetch from cBio Portal...");
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
        java.util.List<String> geneNames = new ArrayList<String>();
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
            }
        }

        // We don't want to merge all data types into a single result
        // so let's extract'em and work one by one
        ArrayList<GeneticProfile> geneticProfiles
                = new ArrayList<GeneticProfile>(currentGeneticProfiles);
        currentGeneticProfiles.clear();

        ObjectFactory expFactory = new ObjectFactory();

        CancerStudy cancerStudy = cBioPortalAccessor.getCurrentCancerStudy();
        CaseList caseList = cBioPortalAccessor.getCurrentCaseList();

        // Now load data
        for (GeneticProfile geneticProfile : geneticProfiles) {
            main.lockWithMessage("Loading " + geneticProfile.getName() + "...");
            cBioPortalAccessor.setCurrentGeneticProfiles(Collections.singletonList(geneticProfile));

            ChisioExperimentData experimentData;
            try {
                experimentData = expFactory.createRootExperimentData();
            } catch (JAXBException e) {
                MessageDialog.openError(main.getShell(), "Error!",
                        "Could not create experiment.");
                return;
            }
            experimentData.setExperimentType(geneticProfile.getType().toString());
            String experimentInfo = cancerStudy.getName() + " | "
                    + caseList.getDescription() + " (" + caseList.getCases().length + "cases ) | "
                    + geneticProfile.getName() + " | "
                    + geneticProfile.getDescription();
            experimentData.setExperimentSetInfo(experimentInfo);
            experimentData.getExperiment();

            // Iterate over genes
            // TODO: optimize this and grab all results with single request.
            for (String gene : geneNames) {
                AlterationPack alterations = cBioPortalAccessor.getAlterations(gene);
            }

            main.unlock();
        }
    }

}
