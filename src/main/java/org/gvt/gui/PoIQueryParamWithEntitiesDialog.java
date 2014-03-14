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
import java.util.Collection;

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
	Collection<EntityHolder> allEntities;

    /**
     * Constructor
     */
    public PoIQueryParamWithEntitiesDialog(ChisioMain main, Collection<EntityHolder> allEntities)
	{
		super(main);
		this.allEntities = allEntities;
	}

    /**
     * Constructor
     */
    public PoIQueryParamWithEntitiesDialog(ChisioMain main, boolean forSIF)
	{
		super(main, forSIF);
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

		// group for limit, shortest+k option and strict option
		createLimitTypesGroup();

		// Group for execute, cancel and default buttons
		createExeCancDefGroup(opt, 8);

		// Disable features that are not currently supported
		if (allEntities == null)
		{
			currentViewButton.setEnabled(false);
		}

		//pack dialog
		shell.pack();

		//set initial values from opt OptionsPack
		setInitialValues(opt);
    }

}