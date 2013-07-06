package org.gvt.gui;

import org.cbio.causality.data.portal.CBioPortalAccessor;
import org.cbio.causality.data.portal.CancerStudy;
import org.cbio.causality.data.portal.CaseList;
import org.cbio.causality.data.portal.GeneticProfile;
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
import java.util.Arrays;

public class FetchFromCBioPortalDialog extends Dialog {
    private ChisioMain main;
    private Shell shell;
    private static int[] memorizeChoices = null;

    public FetchFromCBioPortalDialog(ChisioMain main) {
        super(main.getShell(), SWT.NONE);
        this.main = main;
    }

    public void open()
	{
        if(ChisioMain.cBioPortalAccessor == null)
		{
			System.err.println("Portal accessor is not initialized!");
			return;
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

        final Combo studyCombo = new Combo(shell, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
        gridData.horizontalSpan = 2;
        studyCombo.setLayoutData(gridData);

        studyCombo.removeAll();

        for (CancerStudy cancerStudy : ChisioMain.cBioPortalAccessor.getCancerStudies())
		{
            studyCombo.add(cancerStudy.getName());
        }

        Label caseListLabel = new Label(shell, SWT.NONE);
        caseListLabel.setText("2) Select a case list");
        gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
        gridData.horizontalSpan = 2;
        caseListLabel.setLayoutData(gridData);

        final Combo caseListCombo = new Combo(shell, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
        gridData.horizontalSpan = 2;
        caseListCombo.setLayoutData(gridData);

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

        Group buttonGroup = new Group(shell, SWT.NONE);
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
        gridData.horizontalSpan = 2;
        buttonGroup.setLayoutData(gridData);
		buttonGroup.setLayout(new GridLayout(3, true));

        final Button loadDataButton = new Button(buttonGroup, SWT.NONE);
        loadDataButton.setText("Load data");
        gridData = new GridData(GridData.END, GridData.CENTER, true, false);
        loadDataButton.setEnabled(false);
        loadDataButton.setLayoutData(gridData);

        Button cancelButton = new Button(buttonGroup, SWT.NONE);
        cancelButton.setText("Cancel");
        gridData = new GridData(GridData.CENTER, GridData.CENTER, true, false);
        cancelButton.setLayoutData(gridData);

        Button settingsButton = new Button(buttonGroup, SWT.NONE);
        settingsButton.setText("Settings");
        gridData = new GridData(GridData.BEGINNING, GridData.CENTER, true, false);
        settingsButton.setLayoutData(gridData);

		studyCombo.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetDefaultSelected(SelectionEvent selectionEvent)
			{
				widgetSelected(selectionEvent);
			}

			@Override
			public void widgetSelected(SelectionEvent selectionEvent)
			{
				selectCancerStudy(studyCombo, caseListCombo, genomicProfilesList, supportedProfiles);
			}
		});

        genomicProfilesList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                loadDataButton.setEnabled(genomicProfilesList.getSelectionCount() > 0);
            }
        });

        settingsButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                CBioPortalSettingsDialog cBioPortalSettingsDialog = new CBioPortalSettingsDialog(main);
                cBioPortalSettingsDialog.open();
            }
        });

        loadDataButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                int selectionIndex;

                try {
                    selectionIndex = caseListCombo.getSelectionIndex();
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
				int profileCnt = genomicProfilesList.getSelectionCount();
				memorizeChoices = new int[profileCnt + 2];
                memorizeChoices[0] = ChisioMain.cBioPortalAccessor.getCancerStudies()
                        .indexOf(ChisioMain.cBioPortalAccessor.getCurrentCancerStudy());
                memorizeChoices[1] = selectionIndex;
				for (int i = 0; i < profileCnt; i++)
				{
					memorizeChoices[i + 2] = genomicProfilesList.getSelectionIndices()[i];
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

        // If we saved the options, then select those before the user does.
		if(memorizeChoices != null) {
            studyCombo.select(memorizeChoices[0]);
            selectCancerStudy(studyCombo, caseListCombo, genomicProfilesList, supportedProfiles);
            caseListCombo.select(memorizeChoices[1]);
			int[] select = new int[memorizeChoices.length - 2];
			System.arraycopy(memorizeChoices, 2, select, 0, select.length);
            genomicProfilesList.select(select);
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
                switch(geneticProfile.getType()) {
                    case MRNA_EXPRESSION:
                        if(!geneticProfile.getId().toLowerCase().endsWith("_zscores")) break;
                    case COPY_NUMBER_ALTERATION:
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
}
