package org.gvt.gui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.gvt.ChisioMain;
import org.gvt.util.EntityHolder;
import org.gvt.util.PoIOptionsPack;

import java.util.ArrayList;

/**
 * This class maintains PoI Query Dialog for TopMenuBar.
 *
 * @author Merve Cakir
 */
public class PoIQueryParamWithEntitiesDialog extends AbstractQueryParamDialog
{
    /**
     * To handle addition and removal of source entities. Buttons and list from
     * the super class is used for source list.
     */
    private Label sourceLabel;

    /**
     * To handle addition and removal of target entities.
     */
    private Label targetLabel;
    private List targetEntityList;
    private Button targetAddButton;
    private Button targetRemoveButton;

    /**
     * Type and value of stop distance.
     */
    private Group limitTypeGroup;
    private Button lengthLimitButton;
    private Button shortestPlusKButton;
    private Text shortestPlusK;

    /**
     * Type of PoI; strict or not.
     */
    private Button strictButton;


    /**
	 * All entities of graph
	 */
	ArrayList<EntityHolder> allEntities;

	EntityListGroup sourceElg;
	EntityListGroup targetElg;
	
    /**
	 * Getters
	 */

    public java.util.List<EntityHolder> getSourceAddedEntities()
	{
		return this.sourceElg.addedEntities;
	}

    public java.util.List<EntityHolder> getTargetAddedEntities()
	{
		return this.targetElg.addedEntities;
	}

    /**
     * Constructor
     */
    public PoIQueryParamWithEntitiesDialog(ChisioMain main)
	{
		super(main);
		this.allEntities = main.getAllEntities();
	}

    /**
	 * Open the dialog
	 */
	public PoIOptionsPack open(PoIOptionsPack opt)
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
	 * Buttons, List, Text Field, Radio Buttons, etc.
	 */
	protected void createContents(final PoIOptionsPack opt)
	{
        super.createContents(opt);
        shell.setText("PoI Query Properties");

		//Set Image
		ImageDescriptor id = ImageDescriptor.createFromFile(
			PoIQueryParamWithEntitiesDialog.class,
			"/src/main/resources/org/gvt/icon/cbe-icon.png");
		shell.setImage(id.createImage());

        //layout of shell will contain 8 columns.

        GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 6;
		shell.setLayout(gridLayout);

        //source entity list's label

        sourceLabel = new Label(shell, SWT.NONE);
        sourceLabel.setText("Source");
        GridData gridData = new GridData(GridData.CENTER, GridData.END,
            false, false);
        gridData.horizontalSpan = 1;
        sourceLabel.setLayoutData(gridData);

        //target entity list's label

        targetLabel = new Label(shell, SWT.NONE);
        targetLabel.setText("Target");
        gridData = new GridData(GridData.CENTER, GridData.END, false, false);
        gridData.horizontalSpan = 1;
        targetLabel.setLayoutData(gridData);

        //Group for currentViewButton and newViewButton
        createResultViewGroup(2, 2);

        //Group for lengthLimitButton and shortestPlusKButton

        limitTypeGroup = new Group(shell, SWT.NONE);
        limitTypeGroup.setText("Stop distance");
        gridData = new GridData(GridData.FILL, GridData.BEGINNING, false, false);
		gridData.horizontalSpan = 2;
		gridData.verticalSpan = 2;
        limitTypeGroup.setLayoutData(gridData);
		limitTypeGroup.setLayout(new GridLayout(2, true));

        //Length limit radio button

        lengthLimitButton = new Button(limitTypeGroup, SWT.RADIO);
        lengthLimitButton.setText("Length limit");
        gridData = new GridData(GridData.BEGINNING, GridData.CENTER,
            false, false);
        lengthLimitButton.setLayoutData(gridData);
        
        //Length limit text

        lengthLimit = new Text(limitTypeGroup, SWT.BORDER);
        lengthLimit.addKeyListener(keyAdapter);
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
        lengthLimit.setLayoutData(gridData);

        //Shortest+k radio button

        shortestPlusKButton = new Button(limitTypeGroup, SWT.RADIO);
        shortestPlusKButton.setText("Shortest+k");
        gridData = new GridData(GridData.BEGINNING, GridData.CENTER,
            false, false);
        shortestPlusKButton.setLayoutData(gridData);

        //Shortest+k text

        shortestPlusK = new Text(limitTypeGroup, SWT.BORDER);
        shortestPlusK.addKeyListener(keyAdapter);
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
        shortestPlusK.setLayoutData(gridData);

        //Source enity list
		sourceElg = new EntityListGroup(shell, SWT.NONE, allEntities);
		sourceElg.init();
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.verticalSpan = 5;
		gridData.horizontalSpan = 1;
		gridData.heightHint = sourceElg.entityList.getItemHeight() * 5;
		gridData.widthHint = 150;
		sourceElg.setLayoutData(gridData);

        //Target entity list

		targetElg = new EntityListGroup(shell, SWT.NONE, allEntities);
		targetElg.init();
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.verticalSpan = 5;
		gridData.horizontalSpan = 1;
		gridData.heightHint = targetElg.entityList.getItemHeight() * 5;
		gridData.widthHint = 150;
		targetElg.setLayoutData(gridData);

        //Strict check box

        strictButton = new Button(shell, SWT.CHECK | SWT.WRAP);
        strictButton.setText("Ignore source-source/target-target paths");
        gridData = new GridData(GridData.CENTER, GridData.CENTER, false, false);
        gridData.verticalSpan = 2;
        gridData.horizontalSpan = 4;
        strictButton.setLayoutData(gridData);

		// Group for execute, cancel and default buttons

		exeCancelDefaultGroup = new Group(shell, SWT.NONE);
		gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
		gridData.horizontalSpan = 8;
		exeCancelDefaultGroup.setLayoutData(gridData);
		exeCancelDefaultGroup.setLayout(new GridLayout(3, true));

        //Execute button

        executeButton = new Button(exeCancelDefaultGroup, SWT.NONE);
		executeButton.setText("Execute");
		gridData = new GridData(GridData.END, GridData.CENTER, true, false);
		executeButton.setLayoutData(gridData);
		executeButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				//if no entity is added, show error
				if (getSourceAddedEntities().isEmpty())
				{
					MessageDialog.openError(main.getShell(), "Error!",
					"Add Source Entity!");
					
					return;
				}
				
				//if no entity is added, show error
				if (getTargetAddedEntities().isEmpty())
				{
					MessageDialog.openError(main.getShell(), "Error!",
					"Add Target Entity!");
					
					return;
				}

				//store values in dialog to PoIOptionsPack
				storeValuesToOptionsPack(opt);

				//execute is selected
				opt.setCancel(false);

				shell.close();
			}
		});

        //Cancel button

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
     * PoIOptionsPack.
	 */
	public void storeValuesToOptionsPack(PoIOptionsPack opt)
	{
		//store stop distance according to user's selection
        if (lengthLimitButton.getSelection())
        {
            opt.setLengthLimit(Integer.parseInt(lengthLimit.getText()));
            opt.setLimitType(true);
        }
        else if (shortestPlusKButton.getSelection())
        {
            opt.setShortestPlusKLimit(Integer.parseInt(shortestPlusK.getText()));
            opt.setLimitType(false);
        }

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

        //if strict is selected.
        if (strictButton.getSelection())
        {
            opt.setStrict(true);
        }
        else
        {
            opt.setStrict(false);
        }
    }

	/**
	 * After creating the dialog box, fields are completed with data in
     * opt OptionsPack.
	 */
	public void setInitialValues(PoIOptionsPack opt)
	{
		super.setInitialValues(opt);

		//Strict
        if (opt.isStrict())
        {
            strictButton.setSelection(true);
        }

        //Set both texts' values

        shortestPlusK.setText(String.valueOf(opt.getShortestPlusKLimit()));

        //Length limit or shortest+k

        if (opt.getLimitType())
        {
            lengthLimitButton.setSelection(true);
        }
        else
        {
            shortestPlusKButton.setSelection(true);   
        }
	}

	/**
	 * Set default values into dialog
	 */
	public void setDefaultQueryDialogOptions()
	{
		super.setDefaultQueryDialogOptions();

        shortestPlusK.setText(String.valueOf(DEFAULT_SHORTEST_PLUS_K));
        lengthLimitButton.setSelection(LIMIT_TYPE);
        shortestPlusKButton.setSelection(!LIMIT_TYPE);

        strictButton.setSelection(STRICT);
	}

    /**
	 * This method checks whether physicalEntity is added before to the entity
     * array list.
	 */
	private boolean previouslyAdded(EntityHolder pe,
        ArrayList<EntityHolder> addedEntities)
	{
		for (EntityHolder addedBefore : addedEntities)
		{
			//if entity has been added before, return true
			if (pe == addedBefore)
			{
				return true;
			}
		}
		//if entity is not found in added List, then return false
		return false;
	}    
}
