package org.gvt.gui;

import org.cbio.causality.data.portal.CBioPortalOptions;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.gvt.ChisioMain;

public class CBioPortalSettingsDialog extends Dialog {
    private ChisioMain main;
    private Shell shell;

    public CBioPortalSettingsDialog(ChisioMain main) {
        super(main.getShell(), SWT.NONE);
        this.main = main;
    }

    public void open() {
        shell = new Shell(getParent(), SWT.RESIZE | SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setText("cBio Portal Settings");

        ImageDescriptor id = ImageDescriptor.createFromFile(
                ChisioMain.class, "icon/cbe-icon.png");
        shell.setImage(id.createImage());

        // If this window is open, then we are pretty sure the accessor has already been initiated
        // thanks to FetchFromCBioPortalDialog (parent caller of this dialog).
        final CBioPortalOptions options = ChisioMain.cBioPortalAccessor.getOptions();

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        shell.setLayout(gridLayout);

        GridData gridData;

        Label expLabel = new Label(shell, SWT.BOLD);
        expLabel.setText("Alteration thresholds for data types");
        gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
        gridData.horizontalSpan = 2;
        expLabel.setLayoutData(gridData);

        // EXP
        Group expGroup = new Group(shell, SWT.NONE);
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
        gridData.horizontalSpan = 1;
        expGroup.setLayoutData(gridData);
        expGroup.setLayout(new GridLayout(2, true));

        Label expDataLabel = new Label(expGroup, SWT.BOLD);
        expDataLabel.setText("Expression:");
        gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
        gridData.horizontalSpan = 2;
        expDataLabel.setLayoutData(gridData);

        Label expUpper = new Label(expGroup, SWT.BOLD);
        expUpper.setText("Upper: ");
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
        gridData.horizontalSpan = 1;
        expUpper.setLayoutData(gridData);

        final Text upperExpLimit = new Text(expGroup, SWT.BORDER);
        upperExpLimit.setText(options.get(CBioPortalOptions.PORTAL_OPTIONS.EXP_UPPER_THRESHOLD).toString());
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
        upperExpLimit.setLayoutData(gridData);

        Label expLower = new Label(expGroup, SWT.BOLD);
        expLower.setText("Lower: ");
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
        gridData.horizontalSpan = 1;
        expLower.setLayoutData(gridData);

        final Text lowerExpLimit = new Text(expGroup, SWT.BORDER);
        lowerExpLimit.setText(options.get(CBioPortalOptions.PORTAL_OPTIONS.EXP_LOWER_THRESHOLD).toString());
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
        lowerExpLimit.setLayoutData(gridData);

        // CNA
        Group cnaGroup = new Group(shell, SWT.NONE);
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
        gridData.horizontalSpan = 1;
        cnaGroup.setLayoutData(gridData);
        cnaGroup.setLayout(new GridLayout(2, true));

        Label cnaDataLabel = new Label(cnaGroup, SWT.BOLD);
        cnaDataLabel.setText("Copy-number:");
        gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
        gridData.horizontalSpan = 2;
        cnaDataLabel.setLayoutData(gridData);

        Label cnaUpper = new Label(cnaGroup, SWT.BOLD);
        cnaUpper.setText("Upper: ");
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
        gridData.horizontalSpan = 1;
        cnaUpper.setLayoutData(gridData);

        final Text cnaUpperLimit = new Text(cnaGroup, SWT.BORDER);
        cnaUpperLimit.setText(options.get(CBioPortalOptions.PORTAL_OPTIONS.CNA_UPPER_THRESHOLD).toString());
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
        cnaUpperLimit.setLayoutData(gridData);

        final Label cnaLower = new Label(cnaGroup, SWT.BOLD);
        cnaLower.setText("Lower: ");
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
        gridData.horizontalSpan = 1;
        cnaLower.setLayoutData(gridData);

        final Text cnaLowerLimit = new Text(cnaGroup, SWT.BORDER);
        cnaLowerLimit.setText(options.get(CBioPortalOptions.PORTAL_OPTIONS.CNA_LOWER_THRESHOLD).toString());
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
        cnaLowerLimit.setLayoutData(gridData);

        // RPPA
        Group rppaGroup = new Group(shell, SWT.NONE);
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
        gridData.horizontalSpan = 1;
        rppaGroup.setLayoutData(gridData);
        rppaGroup.setLayout(new GridLayout(2, true));

        Label rppaDataLabel = new Label(rppaGroup, SWT.BOLD);
        rppaDataLabel.setText("Protein:");
        gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
        gridData.horizontalSpan = 2;
        rppaDataLabel.setLayoutData(gridData);

        final Label rppaUpper = new Label(rppaGroup, SWT.BOLD);
        rppaUpper.setText("Upper: ");
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
        gridData.horizontalSpan = 1;
        rppaUpper.setLayoutData(gridData);

        final Text upperRppaLimit = new Text(rppaGroup, SWT.BORDER);
        upperRppaLimit.setText(options.get(CBioPortalOptions.PORTAL_OPTIONS.RPPA_UPPER_THRESHOLD).toString());
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
        upperRppaLimit.setLayoutData(gridData);

        Label rppaLower = new Label(rppaGroup, SWT.BOLD);
        rppaLower.setText("Lower: ");
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
        gridData.horizontalSpan = 1;
        rppaLower.setLayoutData(gridData);

        final Text lowerRppaLimit = new Text(rppaGroup, SWT.BORDER);
        lowerRppaLimit.setText(options.get(CBioPortalOptions.PORTAL_OPTIONS.RPPA_LOWER_THRESHOLD).toString());
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
        lowerRppaLimit.setLayoutData(gridData);

        // Methylation
        Group methGroup = new Group(shell, SWT.NONE);
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
        gridData.horizontalSpan = 1;
        methGroup.setLayoutData(gridData);
        methGroup.setLayout(new GridLayout(2, true));

        Label methDataLabel = new Label(methGroup, SWT.BOLD);
        methDataLabel.setText("Methylation:");
        gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
        gridData.horizontalSpan = 2;
        methDataLabel.setLayoutData(gridData);

        Label methUpper = new Label(methGroup, SWT.BOLD);
        methUpper.setText("Upper: ");
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
        gridData.horizontalSpan = 1;
        methUpper.setLayoutData(gridData);

        final Text upperMethLimit = new Text(methGroup, SWT.BORDER);
        upperMethLimit.setText(options.get(CBioPortalOptions.PORTAL_OPTIONS.METHYLATION_THRESHOLD).toString());
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
        upperMethLimit.setLayoutData(gridData);

        // Buttons
        Button saveButton = new Button(shell, SWT.NONE);
        saveButton.setText("Save");
        gridData = new GridData(GridData.END, GridData.CENTER, true, false);
        saveButton.setLayoutData(gridData);

        Button cancelButton = new Button(shell, SWT.NONE);
        cancelButton.setText("Cancel");
        gridData = new GridData(GridData.BEGINNING, GridData.CENTER, true, false);
        cancelButton.setLayoutData(gridData);

        cancelButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                shell.close();
            }
        });

        saveButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try
                {
                    int closeCount = 0;
                    
                    if(Double.parseDouble(upperExpLimit.getText()) > Double.parseDouble(lowerExpLimit.getText()))
                    {
                        options.put(CBioPortalOptions.PORTAL_OPTIONS.EXP_UPPER_THRESHOLD,
                                Double.parseDouble(upperExpLimit.getText()));
                        options.put(CBioPortalOptions.PORTAL_OPTIONS.EXP_LOWER_THRESHOLD,
                                Double.parseDouble(lowerExpLimit.getText()));
                        closeCount++;
                    }
                    else
                    {
                        upperExpLimit.setText(options.get(CBioPortalOptions.PORTAL_OPTIONS.EXP_UPPER_THRESHOLD).toString());
                        lowerExpLimit.setText(options.get(CBioPortalOptions.PORTAL_OPTIONS.EXP_LOWER_THRESHOLD).toString());
                    }

                    if (Double.parseDouble(cnaUpperLimit.getText()) > Double.parseDouble(cnaLowerLimit.getText()))
                    {
                        options.put(CBioPortalOptions.PORTAL_OPTIONS.CNA_UPPER_THRESHOLD,
                                Double.parseDouble(cnaUpperLimit.getText()));
                        options.put(CBioPortalOptions.PORTAL_OPTIONS.CNA_LOWER_THRESHOLD,
                                Double.parseDouble(cnaLowerLimit.getText()));
                        closeCount++;
                    }
                    else
                    {
                        cnaUpperLimit.setText(options.get(CBioPortalOptions.PORTAL_OPTIONS.CNA_UPPER_THRESHOLD).toString());
                        cnaLowerLimit.setText(options.get(CBioPortalOptions.PORTAL_OPTIONS.CNA_LOWER_THRESHOLD).toString());
                    }

                    if (Double.parseDouble(upperRppaLimit.getText()) > Double.parseDouble(lowerRppaLimit.getText()))
                    {
                        options.put(CBioPortalOptions.PORTAL_OPTIONS.RPPA_UPPER_THRESHOLD,
                                Double.parseDouble(upperRppaLimit.getText()));
                        options.put(CBioPortalOptions.PORTAL_OPTIONS.RPPA_LOWER_THRESHOLD,
                                Double.parseDouble(lowerRppaLimit.getText()));
                        closeCount++;
                    }
                    else
                    {
                        upperRppaLimit.setText(options.get(CBioPortalOptions.PORTAL_OPTIONS.RPPA_UPPER_THRESHOLD).toString());
                        lowerRppaLimit.setText(options.get(CBioPortalOptions.PORTAL_OPTIONS.RPPA_LOWER_THRESHOLD).toString());
                    }

                    options.put(CBioPortalOptions.PORTAL_OPTIONS.METHYLATION_THRESHOLD,
                            Double.parseDouble(upperMethLimit.getText()));

                    if (closeCount == 3)
                    {
                        shell.close();
                    }
                    else
                    {
                        MessageDialog.openWarning(main.getShell(),"Invalid values!",
                            "Upper thresholds must be greater than lower thresholds.");
                    }
                }
                catch (NumberFormatException e)
                {
                    MessageDialog.openError(main.getShell(), "Error",
                            "An error occurred while parsing the threshold values:\n" + e.getMessage());
                }
            }
        });




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
}