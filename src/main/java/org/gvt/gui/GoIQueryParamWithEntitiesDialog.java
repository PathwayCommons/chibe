package org.gvt.gui;

import org.gvt.ChisioMain;
import org.gvt.util.GoIOptionsPack;
import org.gvt.util.EntityHolder;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import java.util.ArrayList;

/**
 * This class maintains GoI Query Dialog for TopMenuBar.
 *
 * @author Merve Cakir
 */
public class GoIQueryParamWithEntitiesDialog extends AbstractQueryParamDialog
{
    /**
     * All entities of graph
     */
    ArrayList<EntityHolder> allEntities;

	EntityListGroup elg;

    /**
     * Getter
     */
    public java.util.List<EntityHolder> getAddedEntities()
    {
        return this.elg.addedEntities;
    }

    /**
     * Create the dialog
     */
    public GoIQueryParamWithEntitiesDialog(ChisioMain main)
    {
        super(main);
        this.allEntities = main.getAllEntities();
    }

    /**
     * Open the dialog
     */
    public GoIOptionsPack open(GoIOptionsPack opt)
    {
        createContents(opt);

        shell.setLocation(
            getParent().getLocation().x + (getParent().getSize().x / 2) -
            (shell.getSize().x / 2),
            getParent().getLocation().y + (getParent().getSize().y / 2) -
            (shell.getSize().y / 2));

        shell.open();

        shell.layout();
        Display display = getParent().getDisplay();
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return opt;
    }

    /**
     * Create contents of the dialog.
     * Buttons, List, Text Field, Radio Buttons, etc
     */
    protected void createContents(final GoIOptionsPack opt)
    {
        super.createContents(opt);
        shell.setText("GoI Query Properties");

        //Set Image

        ImageDescriptor id = ImageDescriptor.createFromFile(
            GoIQueryParamWithEntitiesDialog.class,
			"org/gvt/icon/cbe-icon.png");
        shell.setImage(id.createImage());

        //layout of shell contains 4 columns

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        shell.setLayout(gridLayout);

        //Entity list
		elg = new EntityListGroup(shell, SWT.NONE, allEntities);
		elg.init();
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.verticalSpan = 5;
		gridData.horizontalSpan = 1;
		gridData.heightHint = elg.entityList.getItemHeight() * 5;
		gridData.widthHint = 150;
		elg.setLayoutData(gridData);

        //Group for currentViewButton and newViewButton
        createResultViewGroup(2, 2);

        //Length Limit Label and Text
        createLengthLimit(1, 1, 1, 1, 50);

		// Group for execute, cancel and default buttons

		exeCancelDefaultGroup = new Group(shell, SWT.NONE);
		gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
		gridData.horizontalSpan = 4;
		exeCancelDefaultGroup.setLayoutData(gridData);
		exeCancelDefaultGroup.setLayout(new GridLayout(3, true));

        //Execute Button

        executeButton = new Button(exeCancelDefaultGroup, SWT.NONE);
        executeButton.setText("Execute");
        gridData = new GridData(GridData.END, GridData.CENTER, true, false);
        executeButton.setLayoutData(gridData);
        executeButton.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
				//if no entity is added, show error
				if (getAddedEntities().isEmpty())
				{
					MessageDialog.openError(main.getShell(), "Error!",
					"Add Entity!");
					
					return;
				}
				
                //store values in dialog to optionsPack
                storeValuesToOptionsPack(opt);

                //execute is selected
                opt.setCancel(false);

                shell.close();
            }
        });


        //Cancel Button

        cancelButton = new Button(exeCancelDefaultGroup, SWT.NONE);
		gridData = new GridData(GridData.CENTER, GridData.CENTER, true, false);
        createCancelButton(gridData);
        
        //Default Button

        defaultButton = new Button(exeCancelDefaultGroup, SWT.NONE);
        defaultButton.setText("Default");
        gridData =
            new GridData(GridData.BEGINNING, GridData.CENTER, true, false);
        defaultButton.setLayoutData(gridData);
        defaultButton.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
                //set default values of dialog
                setDefaultQueryDialogOptions();
            }
        });

        //pack dialog
        shell.pack();

        //set initial values from opt OptionsPack
        setInitialValues(opt);
    }

    /**
     * After clicking execute button, all data in dialog is saved to
     * GoIOptionsPack
     */
    public void storeValuesToOptionsPack(GoIOptionsPack opt)
    {
        //store Length Limit
        opt.setLengthLimit(Integer.parseInt(lengthLimit.getText()));

        //if currentView is selected
        if (currentViewButton.getSelection())
        {
            opt.setCurrentView(true);
        }
        //if newView is selected
        else
        {
            opt.setCurrentView(false);
        }
    }
}
