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
import org.gvt.util.Conf;

import java.io.IOException;
import java.util.ArrayList;

public class FetchFromCBioPortalDialog extends Dialog {
    private ChisioMain main;
    private Shell shell;
    private static int[] memorizeChoices = {-1, -1, -1};

    public FetchFromCBioPortalDialog(ChisioMain main) {
        super(main.getShell(), SWT.NONE);
        this.main = main;
    }

    public void open() {
        if(ChisioMain.cBioPortalAccessor == null) {
            try {
                CBioPortalAccessor.setPortalURL(Conf.get(Conf.CBIOPORTAL_URL));
                ChisioMain.cBioPortalAccessor = new CBioPortalAccessor();
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
        for (CancerStudy cancerStudy : ChisioMain.cBioPortalAccessor.getCancerStudies()) {
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
                selectCancerStudy(comboDropDown, caseListList, genomicProfilesList, supportedProfiles);
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
                int selectionIndex;

                try {
                    selectionIndex = caseListList.getSelectionIndex();
                    CaseList caseList = ChisioMain.cBioPortalAccessor.getCaseListsForCurrentStudy().get(selectionIndex);
                    ChisioMain.cBioPortalAccessor.setCurrentCaseList(caseList);
                } catch (IOException e) {
                    MessageDialog.openError(
                            main.getShell(),
                            "Error!",
                            "Could not load case lists for current study.\n" + e.toString()
                    );

                    return;
                }

                java.util.List<GeneticProfile> selectedProfiles
                        = ChisioMain.cBioPortalAccessor.getCurrentGeneticProfiles();

                selectedProfiles.clear();
                for (int i : genomicProfilesList.getSelectionIndices()) {
                    GeneticProfile geneticProfile =  supportedProfiles.get(i);
                    selectedProfiles.add(geneticProfile);
                }

                // Remember these for the next time
                memorizeChoices[0] = ChisioMain.cBioPortalAccessor.getCancerStudies()
                        .indexOf(ChisioMain.cBioPortalAccessor.getCurrentCancerStudy());
                memorizeChoices[1] = selectionIndex;
                memorizeChoices[2] = genomicProfilesList.getSelectionIndex();

                // Load data inside action, not here; so let's close this dialog first.
                shell.close();
            }
        });

        cancelButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                shell.close();
            }
        });

        // If we saved the options, then select those before the user does.
        if(memorizeChoices[0] != -1) {
            comboDropDown.select(memorizeChoices[0]);
            selectCancerStudy(comboDropDown, caseListList, genomicProfilesList, supportedProfiles);
            caseListList.select(memorizeChoices[1]);
            genomicProfilesList.select(memorizeChoices[2]);
            loadDataButton.setEnabled(true);
        }
    }

    private void selectCancerStudy(Combo comboDropDown,
                                   Combo caseListList,
                                   List genomicProfilesList,
                                   ArrayList<GeneticProfile> supportedProfiles) {
        CancerStudy cancerStudy = ChisioMain.cBioPortalAccessor.getCancerStudies().get(comboDropDown.getSelectionIndex());
        ChisioMain.cBioPortalAccessor.setCurrentCancerStudy(cancerStudy);
        try {
            caseListList.removeAll();
            for (CaseList caseList : ChisioMain.cBioPortalAccessor.getCaseListsForCurrentStudy()) {
                caseListList.add(caseList.getDescription() + " (" + caseList.getCases().length + " cases)");
            }
            caseListList.select(0);

            genomicProfilesList.removeAll();
            supportedProfiles.clear();
            for (GeneticProfile geneticProfile : ChisioMain.cBioPortalAccessor.getGeneticProfilesForCurrentStudy()) {
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

    public CBioPortalAccessor getAccessor() {
        return ChisioMain.cBioPortalAccessor;
    }
}
