package org.gvt.gui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.gvt.ChisioMain;

/**
 * Prepares the dialog for "Connect to DAVID" feature.
 *
 * @author Merve Cakir
 */
public class ConnectToDAVIDDialog extends Dialog
{
    private ChisioMain main;

    private Shell shell;

    /**
     * Radio buttons for four different analysis tools
     */
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;

    /**
     * Name of the selected analysis tool
     */
    private String toolName;

    private boolean okPressed;
    
    public ConnectToDAVIDDialog(ChisioMain main)
    {
        super(main.getShell(), SWT.NONE);
        this.main = main;

        this.okPressed = false;
    }

    /**
     * Open the dialog
     */
    public boolean open()
    {
        createContents();

        shell.pack();
        shell.setLocation(
            getParent().getLocation().x + (getParent().getSize().x / 2) -
                (shell.getSize().x / 2),
            getParent().getLocation().y + (getParent().getSize().y / 2) -
                (shell.getSize().y / 2));

        shell.open();

        Display display = getParent().getDisplay();
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch()) display.sleep();
        }

        return okPressed;
    }

    /**
     * Create contents of the dialog
     */
    public void createContents()
    {
        shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

        shell.setText("Connect to DAVID");

        ImageDescriptor id = ImageDescriptor.createFromFile(ChisioMain.class, "icon/cbe-icon.png");
        shell.setImage(id.createImage());

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        shell.setLayout(gridLayout);

        Group buttonGroup = new Group(shell, SWT.NONE);
        buttonGroup.setText("Select one of the DAVID tools");
        GridData gridData = new GridData(GridData.FILL, GridData.BEGINNING, false, false);
        gridData.horizontalSpan = 2;
        buttonGroup.setLayoutData(gridData);
        buttonGroup.setLayout(new GridLayout());

        button1 = new Button(buttonGroup, SWT.RADIO);
        button1.setText("Functional Annotation Summary");
        gridData = new GridData(GridData.BEGINNING, GridData.CENTER, false, false);
        button1.setLayoutData(gridData);

        button2 = new Button(buttonGroup, SWT.RADIO);
        button2.setText("Gene Functional Classification");
        gridData = new GridData(GridData.BEGINNING, GridData.CENTER, false, false);
        button2.setLayoutData(gridData);

        button3 = new Button(buttonGroup, SWT.RADIO);
        button3.setText("Gene Report");
        gridData = new GridData(GridData.BEGINNING, GridData.CENTER, false, false);
        button3.setLayoutData(gridData);

        button4 = new Button(buttonGroup, SWT.RADIO);
        button4.setText("Gene Name Batch Viewer");
        gridData = new GridData(GridData.BEGINNING, GridData.CENTER, false, false);
        button4.setLayoutData(gridData);
              
        Button okButton = new Button(shell, SWT.NONE);
        okButton.setText("OK");
        gridData = new GridData(GridData.END, GridData.CENTER, true, false);
        okButton.setLayoutData(gridData);
        okButton.addSelectionListener(new SelectionAdapter()
        {
            // Specify toolName in accordance with DAVID API style, based on user selection
            public void widgetSelected(SelectionEvent arg0)
            {
                if (button1.getSelection())
                {
                    toolName = "summary";
                }
                else if (button2.getSelection())
                {
                    toolName = "gene2gene";
                }
                else if (button3.getSelection())
                {
                    toolName = "geneReportFull";
                }
                else if (button4.getSelection())
                {
                    toolName = "list";
                }

                okPressed = true;
                shell.close();
            }
        });

        Button cancelButton = new Button(shell, SWT.NONE);
        cancelButton.setText("Cancel");
        gridData = new GridData(GridData.BEGINNING, GridData.CENTER, true, false);
        cancelButton.setLayoutData(gridData);
        cancelButton.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
                shell.close();
            }
        });
    }

    public String getToolName()
    {
        return toolName;
    }
}
