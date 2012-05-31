package org.gvt.gui;

import org.biopax.paxtools.causality.data.CBioPortalAccessor;
import org.biopax.paxtools.causality.data.CancerStudy;
import org.biopax.paxtools.causality.data.CaseList;
import org.biopax.paxtools.causality.data.GeneticProfile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.List;
import org.gvt.ChisioMain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FetchFromCBioPortalDialog extends Dialog {
    private ChisioMain main;
    private Shell shell;
    private CBioPortalAccessor cBioPortalAccessor = null;

    public FetchFromCBioPortalDialog(ChisioMain main) {
        super(main.getShell(), SWT.NONE);
        this.main = main;
    }

    public void open() {
        if(cBioPortalAccessor == null) {
            try {
                cBioPortalAccessor = new CBioPortalAccessor();
            } catch (IOException e) {
                MessageDialog.openError(main.getShell(),
                        "Error!",
                        "Could not access to cBio Portal.\n" + e.toString()
                );

                return;
            }
        }
        createContents();

   		shell.pack();
   		shell.setLocation(
   			getParent().getLocation().x + (getParent().getSize().x / 2) -
   				(shell.getSize().x / 2),
   			getParent().getLocation().y + (getParent().getSize().y / 2) -
   				(shell.getSize().y / 2));

   		shell.open();

   		Display display = getParent().getDisplay();
   		while (!shell.isDisposed()) {
   			if (!display.readAndDispatch()) display.sleep();
   		}
   	}

    private void createContents() {
        shell = new Shell(getParent(), SWT.RESIZE | SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setText("Fetch from cBio Portal");

        ImageDescriptor id = ImageDescriptor.createFromFile(
                ChisioMain.class, "icon/cbe-icon.png");
        shell.setImage(id.createImage());

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        shell.setLayout(gridLayout);

        GridData gridData;

        Label cancerStudyLabel = new Label(shell, SWT.BOLD);
        cancerStudyLabel.setText("1) Select a cancer study");
        gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
        gridData.horizontalSpan = 2;
        cancerStudyLabel.setLayoutData(gridData);

        final Combo comboDropDown = new Combo(shell, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
        gridData.horizontalSpan = 2;
        comboDropDown.setLayoutData(gridData);

        comboDropDown.removeAll();
        for (CancerStudy cancerStudy : cBioPortalAccessor.getCancerStudies()) {
            comboDropDown.add(cancerStudy.getName());
        }

        Label caseListLabel = new Label(shell, SWT.NONE);
        caseListLabel.setText("2) Select a case list");
        gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
        gridData.horizontalSpan = 2;
        caseListLabel.setLayoutData(gridData);

        final Combo caseListList = new Combo(shell, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
        gridData.horizontalSpan = 2;
        caseListList.setLayoutData(gridData);

        final ArrayList<GeneticProfile> supportedProfiles = new ArrayList<GeneticProfile>();
        Label genomicProfileLabel = new Label(shell, SWT.NONE);
        genomicProfileLabel.setText("3) Select genomic profile(s)");
        gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
        gridData.horizontalSpan = 2;
        genomicProfileLabel.setLayoutData(gridData);

        final List genomicProfilesList = new List(shell, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
        gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
        gridData.horizontalSpan = 2;
        gridData.heightHint = 100;
        genomicProfilesList.setLayoutData(gridData);

        final Button loadDataButton = new Button(shell, SWT.NONE);
        loadDataButton.setText("Load data");
        gridData = new GridData(GridData.END, GridData.CENTER, true, false);
        loadDataButton.setEnabled(false);
        loadDataButton.setLayoutData(gridData);

        Button cancelButton = new Button(shell, SWT.NONE);
        cancelButton.setText("Cancel");
        gridData = new GridData(GridData.BEGINNING, GridData.CENTER, true, false);
        cancelButton.setLayoutData(gridData);

        comboDropDown.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent selectionEvent) {
                widgetSelected(selectionEvent);
            }

            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                CancerStudy cancerStudy = cBioPortalAccessor.getCancerStudies().get(comboDropDown.getSelectionIndex());
                cBioPortalAccessor.setCurrentCancerStudy(cancerStudy);
                try {
                    caseListList.removeAll();
                    for (CaseList caseList : cBioPortalAccessor.getCaseListsForCurrentStudy()) {
                        caseListList.add(caseList.getDescription() + " (" + caseList.getCases().length + " cases)");
                    }
                    caseListList.select(0);

                    genomicProfilesList.removeAll();
                    supportedProfiles.clear();
                    for (GeneticProfile geneticProfile : cBioPortalAccessor.getGeneticProfilesForCurrentStudy()) {
                        // Currently we only support these guys
                        switch(geneticProfile.getType()) {
                            case COPY_NUMBER_ALTERATION:
                            case MRNA_EXPRESSION:
                            case MUTATION_EXTENDED:
                                genomicProfilesList.add(geneticProfile.getName());
                                supportedProfiles.add(geneticProfile);
                        }
                    }

                } catch (IOException e) {
                    MessageDialog.openError(
                            main.getShell(),
                            "Error!",
                            "Could not load meta data for current study.\n" + e.toString()
                    );

                }

            }
        });

        genomicProfilesList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                loadDataButton.setEnabled(genomicProfilesList.getSelectionCount() > 0);
            }
        });

        loadDataButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    int selectionIndex = caseListList.getSelectionIndex();
                    CaseList caseList = cBioPortalAccessor.getCaseListsForCurrentStudy().get(selectionIndex);
                    cBioPortalAccessor.setCurrentCaseList(caseList);
                } catch (IOException e) {
                    MessageDialog.openError(
                            main.getShell(),
                            "Error!",
                            "Could not load case lists for current study.\n" + e.toString()
                    );

                    return;
                }

                java.util.List<GeneticProfile> geneticProfiles = null;
                java.util.List<GeneticProfile> selectedProfiles = cBioPortalAccessor.getCurrentGeneticProfiles();

                try {
                    geneticProfiles = cBioPortalAccessor.getGeneticProfilesForCurrentStudy();
                } catch (IOException e) {
                    MessageDialog.openError(
                            main.getShell(),
                            "Error!",
                            "Could not load case lists for current study.\n" + e.toString()
                    );

                    return;
                }

                selectedProfiles.clear();
                for (int i : genomicProfilesList.getSelectionIndices()) {
                    GeneticProfile geneticProfile =  supportedProfiles.get(i);
                    selectedProfiles.add(geneticProfile);
                }

                // Load data inside action, not here; so let's close this dialog first.
                shell.close();
            }
        });

        cancelButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                shell.close();
            }
        });
    }

    public CBioPortalAccessor getAccessor() {
        return cBioPortalAccessor;
    }
}
