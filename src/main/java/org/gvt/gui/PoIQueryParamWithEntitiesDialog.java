package org.gvt.gui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.gvt.ChisioMain;
import org.gvt.util.EntityHolder;
import org.gvt.util.QueryOptionsPack;

import java.util.ArrayList;

/**
 * This class maintains PoI Query Dialog for TopMenuBar.
 *
 * @author Merve Cakir
 */
public class PoIQueryParamWithEntitiesDialog extends AbstractQueryParamDialog
{

    /**
	 * All entities of graph
	 */
	ArrayList<EntityHolder> allEntities;

    /**
     * Constructor
     */
    public PoIQueryParamWithEntitiesDialog(ChisioMain main, ArrayList<EntityHolder> allEntities)
	{
		super(main);
		this.allEntities = allEntities;
	}

    /**
	 * Create contents of the dialog.
	 * Buttons, List, Text Field, Radio Buttons, etc.
	 */
	protected void createContents(final QueryOptionsPack opt)
	{
        super.createContents(opt);
        shell.setText("Paths From To Query Properties");
        infoLabel.setText("Find all paths of specified length limit from a set of source entities to a set of target entities");

		//Set Image
		ImageDescriptor id = ImageDescriptor.createFromFile(ChisioMain.class, "icon/cbe-icon.png");
		shell.setImage(id.createImage());

        //layout of shell will contain 8 columns.

        GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 6;
		shell.setLayout(gridLayout);

		GridData gridData;

		//Source enity list

		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.verticalSpan = 5;
		gridData.horizontalSpan = 1;
		gridData.widthHint = 150;
		if (allEntities != null)
		{
			sourceElg = new EntityListGroup(shell, SWT.NONE, allEntities);
			sourceElg.init();
			sourceElg.setLayoutData(gridData);
		}
		else
		{
			sourceST = new SymbolText(shell, SWT.NONE);
			sourceST.init("Source symbols:");
			sourceST.setLayoutData(gridData);
		}

		//Target entity list
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.verticalSpan = 5;
		gridData.horizontalSpan = 1;
		gridData.widthHint = 150;

		if (allEntities != null)
		{
			targetElg = new EntityListGroup(shell, SWT.NONE, allEntities);
			targetElg.init();
			targetElg.setLayoutData(gridData);
		}
		else
		{
			targetST = new SymbolText(shell, SWT.NONE);
			targetST.init("Target symbols:");
			targetST.setLayoutData(gridData);
		}

        //Group for currentViewButton and newViewButton
        createResultViewGroup(2, 2);

        //Group for lengthLimitButton and shortestPlusKButton

		Group limitTypeGroup = new Group(shell, SWT.NONE);
        limitTypeGroup.setText("Stop distance");
        gridData = new GridData(GridData.FILL, GridData.BEGINNING, false, false);
		gridData.horizontalSpan = 2;
		gridData.verticalSpan = 2;
        limitTypeGroup.setLayoutData(gridData);
		limitTypeGroup.setLayout(new GridLayout(2, true));

        //Length limit radio button

        lengthLimitButton = new Button(limitTypeGroup, SWT.RADIO);
        lengthLimitButton.setText("Length limit");
        gridData = new GridData(GridData.BEGINNING, GridData.CENTER, false, false);
        lengthLimitButton.setLayoutData(gridData);
        
        //Length limit text

        lengthLimit = new Text(limitTypeGroup, SWT.BORDER);
        lengthLimit.addKeyListener(keyAdapter);
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
        lengthLimit.setLayoutData(gridData);

        //Shortest+k radio button

        shortestPlusKButton = new Button(limitTypeGroup, SWT.RADIO);
        shortestPlusKButton.setText("Shortest+k");
        gridData = new GridData(GridData.BEGINNING, GridData.CENTER, false, false);
        shortestPlusKButton.setLayoutData(gridData);

        //Shortest+k text

        shortestPlusK = new Text(limitTypeGroup, SWT.BORDER);
        shortestPlusK.addKeyListener(keyAdapter);
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
        shortestPlusK.setLayoutData(gridData);

        //Strict check box

        strictButton = new Button(shell, SWT.CHECK | SWT.WRAP);
        strictButton.setText("Ignore source-source/target-target paths");
        gridData = new GridData(GridData.CENTER, GridData.CENTER, false, false);
        gridData.verticalSpan = 2;
        gridData.horizontalSpan = 4;
        strictButton.setLayoutData(gridData);

		// Group for execute, cancel and default buttons
		createExeCancDefGroup(opt, 8);

		// Disable features that are not currently supported
		if (allEntities == null)
		{
			shortestPlusKButton.setEnabled(false);
			strictButton.setEnabled(false);
			currentViewButton.setEnabled(false);
		}

		//pack dialog
		shell.pack();

		//set initial values from opt OptionsPack
		setInitialValues(opt);
    }
}